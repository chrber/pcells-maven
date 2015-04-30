// $Id: JDeepScan.java,v 1.2 2004/06/21 22:30:27 cvs Exp $
//
package org.pcells.services.gui ;
//

import org.pcells.services.connection.DomainConnection;
import org.pcells.services.connection.DomainConnectionListener;
import org.pcells.services.connection.DomainEventListener;
import org.pcells.util.Logable;
import org.slf4j.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class      JDeepScan
        extends    JPanel
        implements DomainConnectionListener,
        DomainEventListener ,
        ActionListener    {

    private Logger _logger;

    private DomainConnection _connection = null ;
    private Font         _bigFont      = new Font( "Times" , Font.BOLD , 26 ) ;
    private DisplayPanel _displayPanel = null ;
    private JButton      _scanButton   = new JButton("Deep Space Scan") ;
    private JButton      _switchButton = new JButton("Toggle Display");
    private DeepSpaceScan _deepSpaceScan = null ;
    private CardLayout    _cards         = new CardLayout() ;
    private JPanel        _cardBoard     = null ;
    private JTopologyPane _topology    = null ;
    private int           _displayMode = 0 ;

    public JDeepScan( DomainConnection connection ){

        _connection = connection ;
        _connection.addDomainEventListener(this) ;

        BorderLayout l = new BorderLayout() ;
        l.setVgap(10) ;
        l.setHgap(10);
        setLayout(l) ;

        JLabel label = new JLabel( "Deep Space Scan" , JLabel.CENTER ) ;
        label.setFont( _bigFont ) ;

        add( label , "North" ) ;

        _cardBoard = new JPanel( _cards ) ;

        _displayPanel = new DisplayPanel() ;
        _topology     = new JTopologyPane();
        _topology.setMessage("No yet initialized");
        _topology.addActionListener(this);

        _cardBoard.add( _displayPanel , "blackboard" ) ;
        _cardBoard.add( new JTopology()     , "topology" ) ;

        _cards.show( _cardBoard , "blackboard" ) ;

        add( _cardBoard , "Center" ) ;

        JPanel south = new JPanel( new BorderLayout(10,10) ) ;
        south.add( _scanButton   , "West");
        south.add( _switchButton , "East");

        add( south , "South" ) ;
        _scanButton.addActionListener(this);
        _switchButton.addActionListener(this);
        _deepSpaceScan = new DeepSpaceScan( _connection , new Log()) ;
        _deepSpaceScan.addActionListener(this);


    }
    private class JTopology extends JPanel  {
        private JTopology(){
            super(new BorderLayout());
            add( _topology ,"Center");
            setBorder(

                    BorderFactory.createCompoundBorder(
                            BorderFactory.createTitledBorder("Topology") ,
                            BorderFactory.createEmptyBorder(8,8,8,8)
                    )

            ) ;

        }
    }
    private class DisplayPanel extends JPanel
            implements ActionListener, MouseListener {

        private JTextArea    _displayArea  = new JTextArea() ;
        private JScrollPane  _scrollPane   = null ;
        private JButton      _clearButton  = new JButton("Clear") ;

        private DisplayPanel(){

            BorderLayout l = new BorderLayout() ;
            l.setVgap(10) ;
            l.setHgap(10);
            setLayout(l) ;
            _displayArea.setEditable(false);

            _scrollPane = new JScrollPane( _displayArea ) ;
            add( _scrollPane   , "Center" ) ;

            _displayArea.addMouseListener(this);
            _clearButton.addActionListener(this);

            JPanel south = new JPanel() ;
            l = new BorderLayout() ;
            l.setVgap(10) ;
            l.setHgap(10);

            south.setLayout(l) ;

            south.add( _clearButton  , "East" ) ;

            south.add( _scanButton  , "West" ) ;

            add( south , "South" ) ;

            setBorder(

                    BorderFactory.createCompoundBorder(
                            BorderFactory.createTitledBorder("Blackboard") ,
                            BorderFactory.createEmptyBorder(8,8,8,8)
                    )

            ) ;

        }
        public void actionPerformed( ActionEvent event ){
            Object source = event.getSource() ;
            if( source == _clearButton ){
                _displayArea.setText("");
            }
        }
        private void append( String text ){
            _displayArea.append(text);
            SwingUtilities.invokeLater(

                    new Runnable(){
                        public void run(){
                            Rectangle rect = _displayArea.getBounds() ;
                            rect.y = rect.height - 30 ;
                            _scrollPane.getViewport().scrollRectToVisible( rect ) ;
                        }
                    }
            ) ;
        }
        public void mouseClicked( MouseEvent event ){
            if( event.getClickCount() > 1 )_displayArea.setText("");
        }
        public void mouseEntered( MouseEvent event ){
        }
        public void mouseExited( MouseEvent event ){
        }
        public void mousePressed( MouseEvent event ){
        }
        public void mouseReleased( MouseEvent event ){
        }
    }
    private class Log implements Logable {
        public void log( String message ){
            say(message);
        }
        public void elog( String message ){
            say(message);
        }
        public void plog( String message ){
            say(message);
        }
    }
    public void say( String message ){
        append(message+"\n");
    }
    public void actionPerformed( ActionEvent event ){
        Object source = event.getSource() ;

        if( source == _topology ){
            _logger.debug("Topology event : "+event);
        }else if( source == _switchButton ){
            if( _displayMode == 0 ){
                _cards.show( _cardBoard , "topology" ) ;
                _displayMode = 1 ;
            }else{
                _cards.show( _cardBoard , "blackboard" ) ;
                _displayMode = 0 ;
            }
        }else if( source == _scanButton ){
            _topology.setMessage("S c a n n i n g");
            say("Trigging deep space scan");
            try{
                _scanButton.setEnabled(false);
                _deepSpaceScan.scan() ;
            }catch(Exception ee){
                say("Deep Scan space reported error : "+ee.getMessage());
            }
        }else if( source == _deepSpaceScan ){
            _scanButton.setEnabled(true);
            DeepSpaceScan.ScanFinishedEvent e = (DeepSpaceScan.ScanFinishedEvent)event ;
            java.util.List list = e.getDomainList() ;
            say(list.toString());
            _topology.setTopology( list );
        }
    }

    private void append( String text ){ _displayPanel.append( text ) ; }

    private class OurListener implements DomainConnectionListener {
        public void domainAnswerArrived( Object obj , int subid ){
//         _logger.debug( "Answer ("+subid+") : "+obj.toString() ) ;
            if( obj instanceof Object [] ){
                Object [] array = (Object [])obj ;
                for( int i = 0 , n = array.length ; i < n ; i++ ){
                    append(array[i].toString()+"\n");
                }
            }else{
                append(obj.toString()+"\n");
            }
        }
    }
    public void connectionOpened( DomainConnection connection ){
        _logger.debug("Connection opened");
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
