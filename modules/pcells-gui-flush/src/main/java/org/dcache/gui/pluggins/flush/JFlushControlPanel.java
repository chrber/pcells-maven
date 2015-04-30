// $Id: JFlushControlPanel.java,v 1.8 2006/12/23 18:14:08 cvs Exp $
//
package org.dcache.gui.pluggins.flush ;
//

import diskCacheV111.hsmControl.flush.FlushControlCellInfo;
import org.pcells.services.connection.DomainConnection;
import org.pcells.services.connection.DomainConnectionListener;
import org.pcells.services.connection.DomainEventListener;
import org.pcells.services.gui.CellBorder;
import org.pcells.services.gui.CellGuiSkinHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;

public class      JFlushControlPanel
        extends    CellGuiSkinHelper.CellPanel
        implements DomainConnectionListener,
        DomainEventListener{

    private Logger _logger;

    private DomainConnection   _connection  = null ;
    private JFlushMonitorPanel _monitor     = null ;
    private Controller         _controller  = new Controller() ;
    private JFlushStatusPanel2 _status      = null ;
    private CardLayout         _switchCard  = new CardLayout();
    private JPanel             _switchPanel = new CellGuiSkinHelper.CellPanel( _switchCard ) ;

    /*
     *   Master Layout
     */
    public JFlushControlPanel( DomainConnection connection , java.util.prefs.Preferences pref) {

        _logger = LoggerFactory.getLogger(JFlushControlPanel.class);

        _connection = connection;

        BorderLayout l = new BorderLayout();
        l.setVgap(10);
        l.setHgap(10);
        setLayout(l);

        setBorder(new CellBorder("Flush Control", 35));

        _monitor = new JFlushMonitorPanel(_controller, _connection, pref);
        _status = new JFlushStatusPanel2(_controller, _connection, pref);

        _switchPanel.add(_monitor, "monitor");
        _switchPanel.add(_status, "status");

        add(_switchPanel, "Center");
        add(_controller, "South");

        _switchCard.show(_switchPanel, "status");

        connection.addDomainEventListener(this);
    }
    public void setDestination( String destination ){
        _controller._flushController.setText( destination ) ;
    }
    class   Controller
            extends    CellGuiSkinHelper.CellPanel
            implements ActionListener ,
            DomainConnectionListener{

        private JButton    _scanButton      = new CellGuiSkinHelper.CellButton("Update");
        private JButton    _toggleButton    = new CellGuiSkinHelper.CellButton("Toggle View");
        private JTextField _textField       = new JTextField(6);
        private JTextField _flushController = new JTextField(16);
        private JLabel     _flushName       = new JLabel("FlushControllerName" , JLabel.RIGHT ) ;
        private JComboBox  _sortingBox      = null ;
        private Object     _autoUpdateLock  = new Object() ;
        private boolean    _isAutoUpdated   = false ;

        private Controller(){

            Object [] items = { "Generic" , "Top" } ;

            CellGuiSkinHelper.setComponentProperties( _flushName ) ;

            _sortingBox  = new JComboBox(items) ;
            _sortingBox.setSelectedItem(items[0]);
            _sortingBox.addActionListener(this);

            _scanButton.addActionListener(this) ;
            _scanButton.setOpaque(false);

            _toggleButton.addActionListener(this) ;
            _toggleButton.setOpaque(false);

            _textField.addActionListener(this);
            _textField.setText("30");
            _textField.setHorizontalAlignment( JTextField.CENTER ) ;

            GridLayout gl = new GridLayout(0,1) ;
            gl.setVgap(10) ;
            gl.setHgap(10);
            setLayout(gl);


            BorderLayout bl2 = new BorderLayout();
            bl2.setVgap(10) ;
            bl2.setHgap(10);
            JPanel left = new CellGuiSkinHelper.CellPanel(bl2);

            left.add( _toggleButton  , "West" ) ;
            left.add( _textField  , "Center" ) ;
            left.add( _scanButton , "East" ) ;


            BorderLayout bl = new BorderLayout();
            bl.setVgap(10) ;
            bl.setHgap(10);

            JPanel top = new CellGuiSkinHelper.CellPanel(bl);


            _flushController.setText( "FlushManager" ) ;
            _flushController.setHorizontalAlignment( JLabel.CENTER ) ;

            top.add( left             , "West" );
            top.add( _flushName       , "Center" ) ;
            top.add( _flushController , "East" ) ;

            // top.add( _sortingBox , "East"   ) ;

            add( top ) ;

            new Thread( new Updated() ).start() ;

        }
        public String getFlushControllerName(){ return _flushController.getText() ; }
        private int _updateInterval = 30 ;
        public void actionPerformed( ActionEvent event ){
            Object source = event.getSource() ;
            if( source == _toggleButton ){
                _switchCard.next( _switchPanel  ) ;
            }else if( source == _scanButton ){

                if( _textField.isEnabled() ){
                    askForInfos() ;
                }else{
                    _textField.setEnabled(true);
                    _scanButton.setText("Update");
                    _scanButton.setForeground(Color.black);
                    synchronized( _autoUpdateLock ){
                        _isAutoUpdated = false ;
                        _monitor.setRowSelectionAllowed(true);
                    }
                }
            }else if( source == _textField ){
                _scanButton.setText("Stop") ;
                _scanButton.setForeground(Color.red) ;
                _textField.setEnabled(false);
                try{ _updateInterval = Integer.parseInt( _textField.getText() ) ; }
                catch(Exception ee ){ _updateInterval = 20 ; }
                synchronized( _autoUpdateLock ){
                    _isAutoUpdated = true ;
                    _monitor.setRowSelectionAllowed(false);
                }
            }
        }
        public void askForInfos(){
            try{
                _connection.sendObject( _flushController.getText() ,
                        "ls pool -l -binary" ,
                        this ,
                        4 );
            }catch(Exception ee ){
                _logger.error("Exception send "+ee ) ;
            }
            askForStatus() ;
        }
        public void askForStatus(){
            try{
                _connection.sendObject( _flushController.getText() ,
                        "xgetcellinfo" ,
                        this ,
                        5 );
            }catch(Exception ee ){
                _logger.error("Exception send " + ee) ;
            }

        }
        public void domainAnswerArrived( Object obj , int subid ){
            _logger.debug( "Answer ("+subid+") : "+obj.toString() ) ;
            try{

                _scanButton.setEnabled(true) ;

                if( subid == 4 ){

                    if( obj instanceof java.util.List )
                    {
                        java.util.List list = (java.util.List)obj ;
                        for( Iterator i = list.iterator() ; i.hasNext() ; ){
                            _logger.debug("Pool : "+i.next() ) ;
                        }
                        _monitor.preparePoolList( list ) ;
                    }
                    else if ( obj == null )
                    {
                        _logger.error("FlushManager could not be found: {}", obj.getClass().getName());
                    }
                    else
                    {
                        _logger.error("Unexpected message id=4 : "+obj.getClass().getName()+" : "+obj);
                        _monitor.preparePoolList( new ArrayList() );
                    }

                }
                else if( subid == 5 )
                {
                    _logger.debug("Message ID is {}", subid);
                    if( obj instanceof diskCacheV111.hsmControl.flush.FlushControlCellInfo )
                    {

                        FlushControlCellInfo status = (FlushControlCellInfo)obj ;

                        _status.prepareFlushStatus( status ) ;

                    }
                    else if ( obj == null )
                    {
                        _logger.error("FlushManager could not be found: {}", obj.getClass().getName());
                    }
                    else
                    {
                        _logger.error("Unexpected message id=5 : "+obj.getClass().getName()+" : "+obj);
                        _status.prepareFlushStatus( null ) ;
                    }
                }

            }catch(Exception ee ){
                ee.printStackTrace();
            }
        }
        private class Updated implements Runnable {

            private UpdateInAWT _awtUpdate = new UpdateInAWT() ;

            private class UpdateInAWT implements Runnable {
                public void run(){
                    try{
                        int res = Integer.parseInt(_textField.getText())  - 1;
                        if( res <= 0 ){
                            _textField.setText(""+_updateInterval);
                            askForInfos() ;
                        }else{
                            _textField.setText(""+res) ;
                        }
                    }catch(Exception ee ){
                        _textField.setText(""+_updateInterval);
                    }
                }
            }

            public void run(){
                while( ! Thread.interrupted() ){
                    try{
                        Thread.sleep(1000L) ;
                    }catch(Exception ee ){
                        break ;
                    }
                    boolean x ;
                    synchronized( _autoUpdateLock ){ x = _isAutoUpdated ; }
                    if(x)SwingUtilities.invokeLater(  _awtUpdate ) ;
                }
            }
        }

    }
    public void connectionOpened( DomainConnection connection ){
        _logger.debug("Connection opened");
        //_controller.askForPoolDecision() ;
    }
    public void connectionClosed( DomainConnection connection ){
        _logger.debug("Connection closed" ) ;
    }
    public void connectionOutOfBand( DomainConnection connection, Object obj ){
        _logger.debug("Connection connectionOutOfBand "+obj ) ;
    }
    public void domainAnswerArrived( Object obj , int subid ){
        _logger.debug( "Answer ("+subid+") : "+obj.toString() ) ;
    }


}
