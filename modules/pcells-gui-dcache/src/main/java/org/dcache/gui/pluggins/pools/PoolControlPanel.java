// $Id: PoolControlPanel.java,v 1.6 2007/02/18 07:50:24 cvs Exp $
//
package org.dcache.gui.pluggins.pools ;
//
import diskCacheV111.pools.PoolCellInfo;
import diskCacheV111.pools.PoolCostInfo;
import diskCacheV111.pools.StorageClassFlushInfo;
import diskCacheV111.vehicles.JobInfo;
import diskCacheV111.vehicles.PoolFlushGainControlMessage;
import org.pcells.services.connection.DomainConnection;
import org.pcells.services.connection.DomainConnectionListener;
import org.pcells.services.connection.DomainEventListener;
import org.pcells.services.gui.CellBorder;
import org.pcells.services.gui.CellGuiSkinHelper;
import org.pcells.services.gui.EasyCommander;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.prefs.Preferences;

public class      PoolControlPanel
        extends    CellGuiSkinHelper.CellPanel
        implements DomainConnectionListener,
        DomainEventListener,
        ActionListener{

    private Logger _logger;

    private DomainConnection   _connection  = null ;
    private Preferences        _preferences = null ;

    //private JTextField         _destinationField   = new JTextField("") ;
    private JLabel             _destinationField   = new JLabel("", JLabel.CENTER) ;
    private JButton            _updateButton       = new JButton("Update");
    private JTabbedPane        _allFlush           = new JTabbedPane() ;
    private JTabbedPane        _allMainPanels      = new JTabbedPane() ;
    private CardLayout         _switchGenericCard  = new CardLayout();
    private JPanel             _switchGenericPanel = new CellGuiSkinHelper.CellPanel( _switchGenericCard ) ;
    private PoolSpacePanel     _spacePanel         = new PoolSpacePanel() ;

    private PoolQueueSummaryPanel _queueSummary = new PoolQueueSummaryPanel() ;
    private PoolFlushQueuePanel   _flushQueue   = null ;
    private PoolQueueListPanel    _queueList    = new PoolQueueListPanel() ;
    private PoolGenericInfoPanel  _genericInfo  = new PoolGenericInfoPanel() ;
    private EasyCommander         _commander    = null ;

    private FlushQueueDetailsPanel _detailedFlush = null ;

    private class ListSelectionAction implements ListSelectionListener {

        public void valueChanged( ListSelectionEvent event ){
            if( event.getValueIsAdjusting() )return ;

            ListSelectionModel model = (ListSelectionModel)event.getSource() ;

            int pos = model.getMinSelectionIndex() ;

            if( pos < 0 ){
                _switchGenericCard.show( _switchGenericPanel , "details" ) ;
                return ;
            }

            PoolQueueSummaryPanel.QueueInfo info = _queueSummary.getQueueInfoAt( pos ) ;

            _logger.debug("PoolControlPanel "+info );

            String name = info.getName() ;
            int x = 0 ;
            _queueList.setQueueInfo( null ) ;
            try{
                _storeJobInfoStrings[0] = _storeJobInfoStrings[1] = null ;
                String [] command = null ;
                if( name.equals("Mover") ){
                    command = new String[]{ "mover ls -binary" } ;
                    x = 10 ;
                }else if( name.equals("Store" ) ){
                    command = new String[]{ "st ls" , "st jobs ls -binary" } ;
                    x = 20 ;
                }else if( name.equals("Restore" ) ){
                    command = new String[]{ "rh ls" , "rh jobs ls -binary" } ;
                    x = 30 ;
                }else if( name.equals("P2p" ) ){
                    command = new String[]{ "p2p ls -binary" } ;
                    x = 40 ;
                }else if( name.equals("P2pClient" ) ){
                    return ;
                    //command = "mover ls -binary" ;
                }else{
                    return ;
                }
                for( int i = 0 ; i < command.length ; i++ ){
                    _connection.sendObject( _destinationField.getText() ,
                            command[i] ,
                            PoolControlPanel.this ,
                            700 + x + i );
                }
            }catch(Exception ee ){
                _logger.error("Exception send "+ee ) ;
            }
        }
    }
    public class RedPanel extends JPanel {
        public RedPanel(){
            setOpaque(true);
            setBackground(Color.red);
        }
    }
    /*
     *   Master Layout
     */
    public PoolControlPanel( DomainConnection connection , java.util.prefs.Preferences pref) {

        _logger = LoggerFactory.getLogger(PoolControlPanel.class);

        _connection = connection;
        _preferences = pref;

        BorderLayout l = new BorderLayout();
        l.setVgap(10);
        l.setHgap(10);
        setLayout(l);

        setBorder(new CellBorder("Pool Controller", 25));


        _commander = new EasyCommander(_connection);
        _flushQueue = new PoolFlushQueuePanel(connection, pref);
        _detailedFlush = new FlushQueueDetailsPanel(connection);

        JPanel controlLeftPanel = new JPanel(new FlowLayout(4, 4, FlowLayout.LEFT));

        controlLeftPanel.add(_updateButton);

        JPanel controlPanel = new JPanel(new BorderLayout(4, 4));

        controlPanel.add(controlLeftPanel, "West");
        controlPanel.add(_destinationField, "Center");


        JPanel costPanel = new JPanel(new BorderLayout(4, 4));
        costPanel.add(_spacePanel, "West");
        costPanel.add(_queueSummary, "Center");

        _switchGenericPanel.add(_genericInfo, "info");
        _switchGenericPanel.add(_queueList, "queue");


        JPanel mainPanel = new JPanel(new BorderLayout(4, 4));

        mainPanel.add(costPanel, "North");
        mainPanel.add(_switchGenericPanel, "Center");

        _allFlush.addTab("Generic Flush Info", _flushQueue);
        _allFlush.addTab("Detailed Flush Info", _detailedFlush);

        _allMainPanels.addTab("Generic Infos", mainPanel);
        _allMainPanels.addTab("Flush Infos", _allFlush);
        _allMainPanels.addTab("Commander", _commander);

        add(controlPanel, "North");
        add(_allMainPanels, "Center");

        _switchGenericCard.show(_switchGenericPanel, "info");

        CellGuiSkinHelper.setComponentProperties(controlLeftPanel);
        CellGuiSkinHelper.setComponentProperties(_flushQueue);
        CellGuiSkinHelper.setComponentProperties(_updateButton);
        CellGuiSkinHelper.setComponentProperties(mainPanel);
        CellGuiSkinHelper.setComponentProperties(costPanel);
        CellGuiSkinHelper.setComponentProperties(controlPanel);
        CellGuiSkinHelper.setComponentProperties(_queueSummary);
        CellGuiSkinHelper.setComponentProperties(_spacePanel);
        CellGuiSkinHelper.setComponentProperties(_genericInfo);
        CellGuiSkinHelper.setComponentProperties(_queueList);
        CellGuiSkinHelper.setComponentProperties(_allFlush);
        CellGuiSkinHelper.setComponentProperties(_allMainPanels);

        _destinationField.setEnabled(true);
        _queueSummary.getSelectionModel().addListSelectionListener(new ListSelectionAction());
        _updateButton.addActionListener(this);

        connection.addDomainEventListener(this);
    }
    public void setDestination( String destination ){
        _destinationField.setText( destination );
        _commander.setDestination( destination );
        _detailedFlush.setDestination( destination ) ;
        _genericInfo.setGenericInfo(null);
        _switchGenericCard.show( _switchGenericPanel , "info" ) ;

        askForXCellInfo();
    }
    private String [] _storeJobInfoStrings = new String[2] ;

    private class DoLaterTransfer implements Runnable {
        private Object _object = null ;
        private int    _subid  = 0 ;
        public DoLaterTransfer( Object obj , int subid ){
            _object = obj ;
            _subid  = subid ;
        }
        public void run(){
            try{
                if( _subid == 5 ){
                    if( _object instanceof PoolCellInfo ){
                        poolCellInfoArrived( (PoolCellInfo)_object);
                    }else if( _object instanceof PoolFlushGainControlMessage ){
                        PoolFlushGainControlMessage info = (PoolFlushGainControlMessage)_object ;
                        PoolCellInfo cellInfo = info.getCellInfo() ;
                        poolCellInfoArrived( cellInfo ) ;
                        StorageClassFlushInfo []  flushInfo = info.getFlushInfos() ;
                        if( flushInfo != null )_flushQueue.setExtendedQueueInfo( flushInfo ) ;
                    }
                }else if( _subid == 6 ){
                    _genericInfo.setGenericInfo( new PoolGenericInfo( _object.toString() ) ) ;
                }else if( _subid/100 == 7 ){
                    if( _object instanceof JobInfo [] ){

                        GuiJobInfo [] info = new GuiJobInfo[ ((JobInfo[])_object).length ] ;
                        for( int i = 0 ; i < info.length ; i++ )info[i] = new GuiJobInfo(((JobInfo[])_object)[i]);
                        _queueList.setQueueInfo( info ) ;
                        _switchGenericCard.show( _switchGenericPanel , "queue" ) ;
                    }else if( _object instanceof String ){

                        _storeJobInfoStrings[_subid%10] = _object.toString() ;
                        if( ( _storeJobInfoStrings[0] != null ) &&
                                ( _storeJobInfoStrings[1] != null )  ){

                            _queueList.setQueueInfo(
                                    GuiJobInfo.newInstanceByString( _storeJobInfoStrings[0],
                                            _storeJobInfoStrings[1]  )
                            ) ;
                            _switchGenericCard.show( _switchGenericPanel , "queue" ) ;

                        }
                    }else{
                        _queueList.setQueueInfo( null ) ;
                        _logger.error("Unexpected message class arrived : "+_object.getClass().getName() ) ;
                    }
                }else if( _subid == 8 ){
                    if( _object instanceof Object [] ){
                        _flushQueue.setQueueInfo( (Object [])_object ) ;
                    }else{
                        _flushQueue.setQueueInfo( null ) ;
                        _logger.error("Unexpected message class arrived : "+_object.getClass().getName() ) ;
                    }
                }else if( _subid == 10){
                    _logger.debug( _object.toString() ) ;
                    PoolGenericInfo i = new PoolGenericInfo() ;
                    java.util.List list = i.scanFlushInfo( _object.toString() ) ;
                    _detailedFlush.setFlushClassList(list) ;
                    _logger.debug("Start list");
                    for( Iterator nn = list.iterator() ; nn.hasNext() ;  ){
                        PoolGenericInfo.FlushClass fc = (PoolGenericInfo.FlushClass)nn.next() ;
                        _logger.debug(fc.toString());
                    }
                    _logger.debug("End list");
                }

            }catch(Exception ee ){
                ee.printStackTrace();
            }


        }

    }
    public void domainAnswerArrived( Object obj , int subid ){
        _logger.debug( "Answer ("+subid+") : "+obj.toString() ) ;
        DoLaterTransfer doLater = new DoLaterTransfer( obj , subid ) ;
        SwingUtilities.invokeLater( doLater ) ;
    }
    private void poolCellInfoArrived( PoolCellInfo cellInfo ){

        PoolCostInfo costInfo = cellInfo.getPoolCostInfo() ;

        PoolCostInfo.PoolSpaceInfo space = costInfo.getSpaceInfo() ;
        _spacePanel.setSpaces( space ) ;

        _queueSummary.setCostInfo( costInfo ) ;
        _queueList.setQueueInfo( null ) ;

    }
    public void actionPerformed( ActionEvent event ){
        Object source = event.getSource() ;
        if( source == _updateButton ){
            askForXCellInfo() ;
        }/*
      else if( source == _switchButton ){
         _switchGenericCard.next( _switchGenericPanel );
      }else if( source == _commanderButton ){
         _switchMainCard.show( _switchMainPanel , "commander" );
      }else if( source == _genericInfoButton ){
         _switchMainCard.show( _switchMainPanel , "generic" );
      }else if( source == _flushInfoButton ){
         _switchMainCard.show( _switchMainPanel , "flush" );
      }
      */

    }
    public void  askForXCellInfo(){

        try{
            _connection.sendObject( _destinationField.getText() ,
                    "xgetcellinfo" ,
                    //new PoolFlushGainControlMessage("POOL",0L),
                    this ,
                    5 );
        }catch(Exception ee ){
            _logger.error("Exception send "+ee ) ;
        }
        try{
            _connection.sendObject( _destinationField.getText() ,
                    "info" ,
                    this ,
                    6 );
        }catch(Exception ee ){
            _logger.error("Exception send "+ee ) ;
        }
        try{
            _connection.sendObject( _destinationField.getText() ,
                    "flush ls -binary" ,
                    this ,
                    8 );
        }catch(Exception ee ){
            _logger.error("Exception send "+ee ) ;
        }

        try{
            _connection.sendObject( _destinationField.getText() ,
                    "queue ls -l queue" ,
                    this ,
                    10 );
        }catch(Exception ee ){
            _logger.error("Exception send "+ee ) ;
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


}
