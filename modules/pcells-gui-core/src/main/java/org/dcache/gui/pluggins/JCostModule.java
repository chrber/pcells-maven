// $Id: JCostModule.java,v 1.1 2004/09/08 19:44:10 cvs Exp $
//
package org.dcache.gui.pluggins;
//
import diskCacheV111.poolManager.PoolManagerCellInfo;
import org.pcells.services.connection.DomainConnection;
import org.pcells.services.connection.DomainConnectionListener;
import org.pcells.services.connection.DomainEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;

public class      JCostModule
        extends    JPanel
        implements DomainConnectionListener,
        DomainEventListener {

    private Logger _logger;

    private DomainConnection _connection = null ;
    private Font        _bigFont      = new Font( "Courier" , Font.BOLD , 26 ) ;
    private Font        _fixFont      = new Font( "Monospaced" , Font.PLAIN , 12 ) ;
    private JTextField  _commandField = new JTextField() ;
    private JTextArea   _displayArea  = new JTextArea() ;
    private JScrollPane _scrollPane   = null ;
    private JButton     _clearButton  = new JButton("Clear") ;
    private JTextField  _destination  = new JTextField("PoolManager") ;
    private JButton     _scanButton   = new JButton("Scan");
    private JButton     _reportButton = new JButton("Report");
    private JComboBox   _sortingBox   = null ;
    private Object      _scanLock     = new Object() ;
    private boolean     _scanBusy     = false ;
    private boolean     _isReport     = true ;
    private JCostDisplay _costDisplay  = new JCostDisplay() ;
    private JPanel createSouth(){
        GridBagLayout lo = new GridBagLayout() ;
        GridBagConstraints c = new GridBagConstraints()  ;
        JPanel panel = new JPanel( lo ) ;

        Object [] items = { "None" , "Cpu" , "Space" } ;
        _sortingBox  = new JComboBox(items) ;
        _sortingBox.setSelectedItem("None");

        c.gridheight = 1 ;
        c.insets     = new Insets(4,4,4,4) ;

        c.gridwidth  = 1 ; c.gridx = 0 ; c.gridy = 0 ;
        panel.add( _clearButton , c ) ;
        c.gridwidth  = 1 ; c.gridx = 1 ; c.gridy = 0 ;
        panel.add( _scanButton , c ) ;
        c.gridwidth  = 1 ; c.gridx = 2 ; c.gridy = 0 ;
        panel.add( _reportButton , c ) ;
        c.gridwidth  = 1 ; c.gridx = 3 ; c.gridy = 0 ;
        panel.add( _sortingBox , c ) ;
        c.gridwidth  = 1 ; c.gridx = 4 ; c.gridy = 0 ;
        panel.add( new JLabel("Destination") , c ) ;

        c.weightx = 1.0 ;
        c.weighty = 0.0 ;
        c.gridwidth  = 1 ; c.gridx = 5 ; c.gridy = 0 ;
        c.fill = GridBagConstraints.HORIZONTAL ;
        panel.add( _destination , c ) ;

        c.gridwidth  = 6 ; c.gridx = 0 ; c.gridy = 1 ;
        panel.add( _commandField , c ) ;

        JPanel jp = new JPanel( new BorderLayout() ) ;
        jp.add( panel , "Center" ) ;
        return jp ;
    }
    public JCostModule( DomainConnection connection ) {

        _logger = LoggerFactory.getLogger(JCostModule.class);

        _connection = connection;
        BorderLayout l = new BorderLayout();
        l.setVgap(10);
        l.setHgap(10);
        setLayout(l);
        _connection.addDomainEventListener(this);
        _displayArea.setEditable(false);
        _displayArea.setFont(_fixFont);
        _scrollPane = new JScrollPane(_displayArea);
        JLabel label = new JLabel("Cost Module", JLabel.CENTER);
        label.setFont(_bigFont);

        add(label, "North");
        //
        //
        add(_scrollPane, "Center");
        //
        //
        add(createSouth(), "South");

        _clearButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        _displayArea.setText("");
                    }
                }
        );
        _scanButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        _displayArea.setText("");
                        runScanner();
                    }
                }
        );
        _reportButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        if (_isReport) {
                            remove(_scrollPane);
                            add(_costDisplay);
                            _isReport = false;
                        } else {
                            remove(_costDisplay);
                            add(_scrollPane);
                            _isReport = true;
                        }
                        validate();
                        repaint();
                    }
                }
        );
        _sortingBox.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        String sort = _sortingBox.getSelectedItem().toString();
                        if (sort.equals("Cpu"))
                            _costDisplay.setSorting(JCostDisplay.CPU);
                        else if (sort.equals("Space"))
                            _costDisplay.setSorting(JCostDisplay.SPACE);
                        else
                            _costDisplay.setSorting(JCostDisplay.NONE);
                    }
                }
        );
        _commandField.addActionListener(

                new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        String text = _commandField.getText();
                        _commandField.setText("");
                        try {
                            String destination = _destination.getText();
                            if (destination.equals("")) {
                                _connection.sendObject(text, new OurListener(), 4);
                            } else {
                                _logger.debug("Sending to " + destination);
                                _connection.sendObject(destination, text, new OurListener(), 4);
                            }
                        } catch (Exception ee) {
                            _logger.error("Error in sending : " + ee);
                        }
                    }
                }
        );
    }

    private void runScanner(){
        synchronized( _scanLock ){
            if( _scanBusy )return ;
            _scanButton.setEnabled(false);
            _scanBusy = true ;
            _displayArea.setText("\n\n   Please Wait\n");
        }
        new Thread( new ScannerThread() ,"Scanner").start() ;

    }
    private void scannerDone(){
        synchronized( _scanLock ){
            _scanButton.setEnabled(true);
            _scanBusy = false ;
        }
    }
    private class ScannerThread
            implements Runnable,
            DomainConnectionListener {

        private ArrayList _costList = new ArrayList() ;
        private Object    _ioLock   = new Object() ;
        private int       _state    = 0 ;
        private PoolManagerCellInfo _poolInfo = null ;
        private int       _outStanding = 0 ;
        private String    _ok = null ;

        public void domainAnswerArrived( Object obj , int subid ){
            synchronized( _ioLock ){
                if( obj instanceof PoolManagerCellInfo ){
                    _poolInfo = (PoolManagerCellInfo)obj ;
                    _state++ ;
                }else if( obj instanceof Object [] ){
                    _costList.add( obj ) ;
                    _outStanding-- ;
                }else{
                    if( _state == 1 ){
                        _ok = "Unexpected Message arrived while waiting for PoolManagerInfo "+
                                obj.getClass().getName() ;
                        _logger.debug(_ok);
                    }else{
                        _logger.debug("Unexpected Message arrived "+obj.getClass().getName()) ;
                        _outStanding-- ;
                    }
                }
                _ioLock.notifyAll() ;
            }
        }
        public void run(){

            try{
                String [] poolList = null ;
                synchronized( _ioLock ){
                    _state = 1 ;
                    _connection.sendObject( "PoolManager" ,
                            "xgetcellinfo" ,
                            this ,
                            4 ) ;
                    _ioLock.wait(10000L);
                    if( _poolInfo == null )
                        throw new
                                Exception("Problem with cellinfo from PoolManager "+
                                ( _ok != null ? _ok : "" ) );


                }
                poolList = (String[]) _poolInfo.getPoolCells().toArray();
                if( poolList == null )
                    throw new
                            Exception("Got empty pool list");

                for( int i = 0 ; i < poolList.length ; i++ ){
                    synchronized( _ioLock ){
                        _logger.debug("Going to send to "+poolList[i]);
                        _connection.sendObject( "PoolManager" ,
                                "xcm ls "+poolList[i] ,
                                this ,
                                4 ) ;
                        _outStanding++ ;
                        _logger.debug("Send ok to "+poolList[i]);
                    }
                    Thread.currentThread().sleep(100);
                }
                synchronized( _ioLock ){
                    long waitUntil = System.currentTimeMillis() + 10000L ;
                    while( ( _outStanding > 0 ) &&
                            ( System.currentTimeMillis() < waitUntil ) )
                        _ioLock.wait(waitUntil-System.currentTimeMillis()) ;


                }
                _displayArea.setText("");
                Iterator i = _costList.iterator() ;
                while( i.hasNext() ){
                    Object [] array = (Object [] )i.next() ;
                    StringBuffer sb = new StringBuffer() ;
                    for( int j = 0 ; j < array.length ; j++ )
                        sb.append(array[j].toString()).append("  ") ;
                    append(sb.toString()+"\n");
                }
                _costDisplay.setList( _costList ) ;
            }catch(Exception ee ){
                _logger.error("Problem in scan : "+ee ) ;
                ee.printStackTrace() ;

            }finally{
                scannerDone() ;
            }
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
    private class OurListener implements DomainConnectionListener {

        public void domainAnswerArrived( Object obj , int subid ){
//         _logger.debug( "Answer ("+subid+") : "+obj.toString() ) ;

            if( obj instanceof Object [] ){
                Object [] array = (Object []) obj ;
                StringBuffer sb = new StringBuffer() ;
                for( int i = 0 ; i < array.length ; i++ )
                    sb.append(array[i].toString()).append("  ");
                append(sb.toString()+"\n");
            }else{
                append(obj.toString()+"\n");
            }
        }
    }
    public Insets getInsets(){ return new Insets(5,5,5,5) ; }
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
