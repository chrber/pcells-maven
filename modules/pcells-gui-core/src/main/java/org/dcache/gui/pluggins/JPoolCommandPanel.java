// $Id: JPoolCommandPanel.java,v 1.7 2007/01/02 07:49:54 cvs Exp $
//
package org.dcache.gui.pluggins;
//
import org.pcells.services.connection.DomainConnection;
import org.pcells.services.connection.DomainConnectionListener;
import org.pcells.services.gui.CellGuiSkinHelper;
import org.pcells.services.gui.JHistoryTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.prefs.Preferences;

public class      JPoolCommandPanel
        extends    CellGuiSkinHelper.CellPanel
        implements DomainConnectionListener,
        ActionListener{

    private Logger _logger;

    private DomainConnection _connection  = null ;
    private JPoolGroupList _groupList     = null ;
    private CommandPanel   _commandFrame  = null ;
    private JProgressBar   _progressBar   = new JProgressBar() ;
    private CommandModel   _ourModel      = new CommandModel() ;
    private Preferences    _preferences   = null ;

    public JPoolCommandPanel( DomainConnection connection , Preferences preferences ) {

        _logger = LoggerFactory.getLogger(JPoolCommandPanel.class);

        _connection = connection;
        _preferences = preferences;
        BorderLayout l = new BorderLayout(10, 10);
        setLayout(l);

        setBorder(new CellGuiSkinHelper.CellBorder("Pool Commander", 25));

        _groupList = new JPoolGroupList(connection, true);
        _groupList.addActionListener(this);
        _groupList.setProgressBar(_progressBar);

        _commandFrame = new CommandPanel(connection);

        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                _groupList,
                _commandFrame);


        CellGuiSkinHelper.setComponentProperties(split);

        add(split, "Center");
        add(_progressBar, "South");

        split.resetToPreferredSizes();

        //connection.addDomainEventListener(this);

    }

    private class PoolInfo {
        private String _poolName = null ;
        private long   _time     = 0L ;
        private Object _result   = null ;
        private long   _started  = 0L ;
        private PoolInfo( String pool ){
            _poolName = pool ;
        }
        private void touch(){ _time = System.currentTimeMillis() ; }
    }

    public class CommandModel extends AbstractTableModel {
        private String   []  _title = { "Pool" , "Time" , "Result" } ;
        private PoolInfo []  _poolInfoArray = null ;
        public String getColumnName( int index ){
            return _title[index] ;
        }
        public Class getColumnClass(int column ){
            return JPoolCommandPanel.PoolInfo.class ;
        }
        public int getRowCount(){
            return _poolInfoArray == null ? 0 : _poolInfoArray.length ;
        }
        public int getColumnCount(){
            return 3 ;
        }
        public Object getValueAt( int rowIndex , int columnIndex ){
            if( _poolInfoArray == null )return null ;
            return rowIndex <_poolInfoArray.length? _poolInfoArray[rowIndex] : null ;
        }
        public synchronized void setTable( PoolInfo [] list ){
            if( ( _poolInfoArray != null ) && ( _poolInfoArray.length > 0 ) ){
                fireTableRowsDeleted(0,_poolInfoArray.length-1);
            }
            _poolInfoArray = list ;
            if( ( _poolInfoArray != null ) && ( _poolInfoArray.length > 0 ) ){
                fireTableRowsInserted(0,_poolInfoArray.length-1);
            }
        }
        public PoolInfo [] getPoolInfoArray(){ return _poolInfoArray == null ? new PoolInfo[0] : _poolInfoArray ; }
    }
    private Color _myGray  = new Color( 230 , 230 , 230 ) ;
    private Color _myRed   = new Color( 240 , 190 , 190 );
    private Color _myBlue  = new Color( 190 , 190 , 240 );
    private Color _myGreen = new Color( 190 , 240 , 190 );
    private class TextWindow extends JFrame {
        private JTextArea _text    = new JTextArea() ;
        private JPanel    _inside  = new CellGuiSkinHelper.CellPanel( new BorderLayout(10,10) ) ;
        private TextWindow(){
            setLocation( 40,40) ;
            setSize( new Dimension( 400 , 400 ) ) ;
            _text.setFont( new Font( "Monospaced" , Font.PLAIN , 12 ) ) ;
            _inside.setBorder( new CellGuiSkinHelper.CellBorder("Pool Commander" ,25 ) );
            _inside.add( new JScrollPane( _text ) , "Center" ) ;
            getContentPane().add( _inside , "Center");
            addWindowFocusListener(new Listener());
        }
        private void setText( String text ){
            _text.setText(text);
        }
        public void setVisible( boolean visible ){
            super.setVisible(visible) ;
            requestFocusInWindow() ;

        }
        private class Listener extends WindowAdapter {
            public Listener(){
                _logger.debug("Listener instALLED");
            }
            public void windowGainedFocus( WindowEvent event ){
                _logger.debug("event : "+event);
                //     setVisible(false);
            }
            public void windowLostFocus( WindowEvent event ){
                _logger.debug("event : "+event);
                setVisible(false);
            }
        }



    }
    private TextWindow _textWindow = new TextWindow() ;
    public class CommandRenderer extends DefaultTableCellRenderer {
        public CommandRenderer(){}
        public Component getTableCellRendererComponent(
                JTable table ,
                Object value ,
                boolean isSelected ,
                boolean isFocused ,
                int row , int column ){
            //_logger.debug("getTableCellRendererComponent : "+row+" "+column+" "+value.getClass().getName());
            Component component =
                    super.getTableCellRendererComponent(table,value,isSelected,isFocused,row,column);

            JLabel label = (JLabel)component;

            PoolInfo info = (PoolInfo)value ;
            //label.setFont(_font) ;
            if( ! isSelected ){
                if( column < 2 ){
                    label.setForeground( Color.black ) ;
                    label.setBackground( row % 2 == 0 ? Color.white : _myGray ) ;
                }else{
                    if( info._result == null ){
                        label.setForeground( Color.black ) ;
                        label.setBackground( _myGreen ) ;
                    }else if( info._result instanceof String ){
                        label.setForeground( Color.black ) ;
                        label.setBackground( row % 2 == 0 ? Color.white : _myGray ) ;
                    }else if( info._result instanceof dmg.cells.nucleus.NoRouteToCellException ){
                        label.setBackground( _myRed ) ;
                        label.setForeground( Color.black ) ;
                    }else if( info._result instanceof Exception ){
                        label.setBackground( Color.yellow ) ;
                        label.setForeground( Color.blue ) ;
                    }else{
                        label.setBackground( Color.red ) ;
                        label.setForeground( Color.white ) ;
                    }
                }
            }
            switch( column ){
                case 0 :
                    label.setHorizontalAlignment( JLabel.CENTER) ;
                    label.setText( info._poolName );
                    break ;
                case 1 :
                    label.setHorizontalAlignment( JLabel.RIGHT);
                    if( info._started == 0L ){
                        label.setText("");
                    }else if( info._result == null ){
                        label.setText(""+ ( System.currentTimeMillis() - info._started ) );
                    }else{
                        label.setText( ""+info._time);
                    }
                    break ;
                case 2 :
                    label.setHorizontalAlignment( JLabel.LEFT);
                    label.setText( info._result == null ? "" : info._result.toString() ) ;
                    break ;


            }

            return component ;
        }
    }


    private class      CommandPanel
            extends    CellGuiSkinHelper.CellPanel
            implements ActionListener ,
            DomainConnectionListener {

        private ResultTable _table   = new ResultTable() ;
        private JTextField  _command = new JHistoryTextField() ;
        private JButton     _toggle  = new CellGuiSkinHelper.CellButton("To Your Selection") ;
        private PoolInfo [] _selectionPoolInfos = null ;
        private PoolInfo [] _poolGroupPoolInfos = null ;
        private boolean     _poolGroupMode      = true ;
        private PopupManager _popup             = new PopupManager() ;

        private class PopupManager  implements ActionListener {
            private JPopupMenu  _poolGroupPopup     = null ;
            private JPopupMenu  _selectionPopup     = null ;

            private PopupManager(){
                _poolGroupPopup = new JPopupMenu("Group") ;
                _poolGroupPopup.setBorderPainted(true);
                JMenuItem item = _poolGroupPopup.add("Action on group selection") ;
                item.setForeground(Color.red);
                item.setBackground(Color.blue);
                _poolGroupPopup.addSeparator() ;
                JMenuItem mi = null ;
                mi = _poolGroupPopup.add( new JMenuItem("Add selected items to your private selection") ) ;
                mi.setActionCommand("add");
                mi.addActionListener( this ) ;
                mi = _poolGroupPopup.add( new JMenuItem("Clear private selection") ) ;
                mi.setActionCommand("clear");
                mi.addActionListener( this ) ;


                _selectionPopup = new JPopupMenu("Selected") ;
                _selectionPopup.setBorderPainted(true);
                item = _selectionPopup.add("Action on group selection") ;
                item.setForeground(Color.red);
                item.setBackground(Color.blue);
                _selectionPopup.addSeparator() ;
                mi = _selectionPopup.add( new JMenuItem("Remove selected items from your private selection") ) ;
                mi.setActionCommand("remove");
                mi.addActionListener( this ) ;
                mi = _selectionPopup.add( new JMenuItem("Clear private selection") ) ;
                mi.setActionCommand("clear");
                mi.addActionListener( this ) ;
            }
            public void actionPerformed( ActionEvent event ){
                String action = event.getActionCommand() ;

                if( action.equals("add") ){
                    int [] rows = _table.getSelectedRows() ;
                    if( ( rows == null ) || ( rows.length == 0 ) )return ;
                    TreeMap map = new TreeMap() ;
                    if( _selectionPoolInfos != null ){
                        for( int i = 0 , n = _selectionPoolInfos.length ; i<n ;i++ )
                            map.put( _selectionPoolInfos[i]._poolName , _selectionPoolInfos[i] ) ;
                    }
                    PoolInfo [] infos = _ourModel.getPoolInfoArray() ;
                    if( infos == null )return ;
                    for( int i = 0 , n = rows.length ; i<n;i++){
                        PoolInfo info = infos[rows[i]];
                        map.put( info._poolName , info ) ;
                    }

                    _selectionPoolInfos = (PoolInfo [])map.values().toArray( new PoolInfo[0] ) ;
                }else if( action.equals( "remove" ) ){
                    int [] rows = _table.getSelectedRows() ;
                    if( ( rows == null ) || ( rows.length == 0 ) )return ;
                    TreeMap map = new TreeMap() ;
                    if( _selectionPoolInfos != null ){
                        for( int i = 0 , n = _selectionPoolInfos.length ; i<n ;i++ )
                            map.put( _selectionPoolInfos[i]._poolName , _selectionPoolInfos[i] ) ;
                    }
                    PoolInfo [] infos = _ourModel.getPoolInfoArray() ;
                    if( infos == null )return ;
                    for( int i = 0 , n = rows.length ; i<n;i++){
                        PoolInfo info = infos[rows[i]];
                        map.remove( info._poolName  ) ;
                    }

                    _selectionPoolInfos = (PoolInfo [])map.values().toArray( new PoolInfo[0] ) ;
                    setPoolGroupTableMode(false);

                }else if( action.equals( "clear" ) ){
                    _selectionPoolInfos = null ;
                    setPoolGroupTableMode(true);
                }
            }
            public void show( JComponent comp , int x , int y ){
                if( _poolGroupMode )  _poolGroupPopup.show( comp , x ,  y ) ;
                else _selectionPopup.show( comp , x ,  y ) ;
            }
        }
        private class ResultTable extends JTable {
            private ResultTable(){
                CellGuiSkinHelper.setComponentProperties(this);
                setModel(_ourModel);
                setDefaultRenderer( JPoolCommandPanel.PoolInfo.class , new CommandRenderer() );
                addMouseListener(new MouseDoubleClick());
            }
            private class MouseDoubleClick extends MouseAdapter {
                public void mouseClicked( MouseEvent event ){

                    if( event.getClickCount() > 1 ){
                        int row = rowAtPoint( event.getPoint() );
                        if( row < 0 )return ;
                        PoolInfo info =  _ourModel.getPoolInfoArray()[row];
                        if( ( info == null ) || ( info._result == null ) )return ;
                        _textWindow.setText(info._result.toString());
                        _textWindow.setVisible(true);
                    }
                }
                public void mousePressed( MouseEvent event ){
                    if( event.isPopupTrigger() ){
                        doOnPopupTrigger( event ) ;
                    }
                }
                public void mouseReleased( MouseEvent event ){
                    if( event.isPopupTrigger() ){
                        doOnPopupTrigger( event ) ;
                    }

                }

            }
            private void doOnPopupTrigger( MouseEvent event ){
                _popup.show(this,event.getPoint().x,event.getPoint().y);
            }
        }
        private DomainConnection _connection = null ;
        private CommandPanel( DomainConnection connection ){
            super( new BorderLayout(10,10) ) ;
            _connection = connection ;
            add( new JScrollPane(_table) , "Center" ) ;
            JPanel south = new CellGuiSkinHelper.CellPanel( new BorderLayout(4,4) ) ;
            south.add( _command , "Center");
            south.add( _toggle  , "East" ) ;

            add( south  , "South" ) ;
            _command.addActionListener(this);
            _toggle.addActionListener(this);
        }
        private void setPoolGroupTable( PoolInfo [] infos ){
            _poolGroupPoolInfos = infos ;
        }
        private void setPoolGroupTableMode( boolean poolGroupTableMode ){
            _poolGroupMode = poolGroupTableMode ;
            _toggle.setText( poolGroupTableMode ? "To Your Selection" : "To PoolGroup Selection");
            _groupList.setEnabled(poolGroupTableMode);
            _ourModel.setTable( poolGroupTableMode ? _poolGroupPoolInfos : _selectionPoolInfos ) ;
        }
        private boolean _doOnSelectionOnly = false ;
        public void actionPerformed( ActionEvent event ){
            Object source = event.getSource() ;
            if( source == _toggle ){
                setPoolGroupTableMode( ! _poolGroupMode ) ;
            }else if( source == _command ){
                String text = _command.getText() ;
                if( ( text == null ) || ( text.length() == 0 ) )return ;
                if( text.equals("hallo") ){
                    _textWindow.setText("hallo otto");
                    _textWindow.setVisible(true);
                }
                _command.setText("");

                PoolInfo [] list = _ourModel.getPoolInfoArray() ;

                if( list == null )return ;
                int [] rows = _table.getSelectedRows() ;

                if( ( rows != null ) && ( rows.length > 0 ) && _doOnSelectionOnly ){
                    for( int pos = 0 , n = rows.length ; pos < n ; pos++ ){
                        PoolInfo info = list[rows[pos]] ;
                        info._started = System.currentTimeMillis() ;
                        sendCommandToPool( info._poolName , rows[pos] , text ) ;
                    }
                }else{
                    for( int pos = 0 , n = list.length ; pos < n ; pos++ ){
                        PoolInfo info = list[pos] ;
                        info._started = System.currentTimeMillis() ;
                        sendCommandToPool( info._poolName , pos , text ) ;
                    }
                }
            }

        }
        private void sendCommandToPool( String poolName , int position , String command ){
            try{
                _connection.sendObject( poolName ,
                        command ,
                        this ,
                        30000 + position ) ;
            }catch(Exception ee){
                ee.printStackTrace() ;
            }

        }
        public void domainAnswerArrived( Object obj , int subid ){
            int pos = subid - 30000 ;
            PoolInfo [] infos = _ourModel.getPoolInfoArray() ;
            if( ( infos == null ) || ( pos >= infos.length ) )return ;
            PoolInfo info = infos[pos] ;

            if( obj == null ){
                info._result = new Exception("Timeout");
            }else if( obj instanceof Exception ){
                info._result = obj ;
            }else{
                info._result = obj.toString();
            }
            info._time = System.currentTimeMillis() - info._started ;
            _ourModel.fireTableDataChanged() ;

        }
    }
    public void actionPerformed( ActionEvent event ){
        Object source = event.getSource() ;
        _logger.debug("action : "+source.getClass().getName()+" "+event);
        if( source == _groupList ){
            _logger.debug(_groupList.getPoolSet().toString());
            Iterator  pools = new TreeSet( _groupList.getPoolSet() ).iterator() ;
            ArrayList list  = new ArrayList() ;
            while( pools.hasNext() ){
                list.add( new PoolInfo( pools.next().toString() ) );
            }
            // _poolGroupPoolInfos = (PoolInfo []) list.toArray( new PoolInfo[0] ) ;
            _commandFrame.setPoolGroupTable( (PoolInfo []) list.toArray( new PoolInfo[0] ) ) ;
            _commandFrame.setPoolGroupTableMode( true ) ;
        }
    }
    public void domainAnswerArrived( Object obj , int subid ){
        _logger.debug( "Answer ("+subid+") : "+obj.toString() ) ;
    }
    public void connectionOpened( DomainConnection connection ){
        _logger.debug("Connection opened");
        //    _controller.askForPoolDecision() ;
    }
    public void connectionClosed( DomainConnection connection ){
        _logger.debug("Connection closed" ) ;
    }
    public void connectionOutOfBand( DomainConnection connection, Object obj ){
        _logger.debug("Connection connectionOutOfBand "+obj ) ;
    }


}
