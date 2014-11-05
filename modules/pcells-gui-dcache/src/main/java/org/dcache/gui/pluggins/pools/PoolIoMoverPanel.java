// $Id: PoolIoMoverPanel.java,v 1.5 2007/04/29 11:39:25 cvs Exp $
//
package org.dcache.gui.pluggins.pools ;
//
import diskCacheV111.poolManager.PoolManagerCellInfo;
import diskCacheV111.vehicles.IoJobInfo;
import org.dcache.gui.pluggins.JHistogramDisplay;
import org.dcache.gui.pluggins.JSwitchUpdatePanel;
import org.pcells.services.connection.DomainConnection;
import org.pcells.services.connection.DomainConnectionListener;
import org.pcells.services.gui.CellGuiSkinHelper;
import org.pcells.services.gui.util.RowObjectTableModel;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class      PoolIoMoverPanel 
       extends    JSwitchUpdatePanel 
       implements ActionListener, 
                  DomainConnectionListener  {

   private DomainConnection     _connection    = null ;
   private JMoverDisplay        _display       = null ;
   private JSimpleIoMoverTable  _table         = null ;
   private IoJobInfo []         _moverInfo     = null ;
   private Map                  _poolIdMapping = null ;
   private JComponent           _ourMaster     = this ;
      
   private PoolMoverInfoEntry [] _moverInfoList        = null ;
   
   private String                _selectString  = null ;

   private Object    _lock       = new Object() ;
   private int       _waitingFor = 0 ;
   private ArrayList _waitList   = null ;
   static Color     __myGray     = new Color( 230 , 230 , 230 ) ;      
 
   private SimpleDateFormat  _formatter   = new SimpleDateFormat ("MM.dd HH:mm:ss");
   private JPopupMenu        _popup       = null ;
   
   private void doOnPopupTrigger( MouseEvent event ){
      _popup.show(this,event.getPoint().x,event.getPoint().y);
   }

   private class JSimpleIoMoverTable extends JTable {
   
      private IoMoverModel _model = null ;
      
      public class PopupTrigger extends MouseAdapter {
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
      public class IoMoverModel extends RowObjectTableModel {
          public IoMoverModel(  ){
             super(new String[]{ "Name" , "Client ID" , 
                                 "Pool Name" , "Job ID" , "PnfsId" , "Status" , 
                                 "Started" , "Submitted" , "Transfer Time" , "Bytes" , "Last" });
          }   
          public IoJobInfoRow getQueueInfoAt( int pos ){
             return (IoJobInfoRow)getRowAt(pos) ;
          }
      }
      public class MyRenderer extends DefaultTableCellRenderer {
          public MyRenderer(){

          }
          public Component getTableCellRendererComponent(
                            JTable table ,
                            Object value ,
                            boolean isSelected ,
                            boolean isFocused ,
                            int row , int column ){
               //System.out.println("getTableCellRendererComponent : "+row+" "+column+" "+value.getClass().getName());
               Component component =
                   super.getTableCellRendererComponent(table,value,isSelected,isFocused,row,column);

               JLabel label = (JLabel)component;

               if( ! isSelected )label.setBackground( row % 2 == 0 ? Color.white : __myGray ) ;
               label.setHorizontalAlignment( JLabel.CENTER);
             
               switch( column ){
                  case 0 :
                  case 2 :
                      label.setHorizontalAlignment( JLabel.LEFT);
                  break ;
                  case 6 :
                  case 7 :
                      label.setText( _formatter.format( new Date(((Long)value).longValue() )) ) ;
                  break ;
                  case 8 :
                      { 
                         long tt = ((Long)value).longValue() ;
                         if( tt > 1000000000000L )tt = 0 ;
                         label.setText( toTimeDifference( tt / 1000L ) ) ;
                         label.setHorizontalAlignment( JLabel.RIGHT);
                      }
                  break ;
                  case 9 :
                      label.setText( longToByteString( (Long) value ) ) ;
                      label.setHorizontalAlignment( JLabel.RIGHT);
                  break ;
                  case 10 :
                      { 
                         long tt = ((Long)value).longValue() ; 
                         tt = ( System.currentTimeMillis() - tt ) / 1000L ;
                         label.setText( toTimeDifference( tt )  ) ;
                         label.setHorizontalAlignment( JLabel.RIGHT);
                      }
                  break ;
               }
               // label.setText(restoreHandlerInfoToString(info,column));
            
               return component ;

          }
          private String longToByteString( Long value ){
             String b = value.toString() ;
             StringBuffer sb = new StringBuffer() ;
             int count = 0 ;
             for( int i = b.length()  - 1 ; i >= 0 ; i-- , count++ ){
                char c = b.charAt(i) ;
                if( ( count > 0 ) && ( ( count % 3 ) == 0 ) )sb.append('.');
                sb.append( c ) ;
             }
             return sb.reverse().toString();
          }
          public String toTimeDifference( long x ){
             boolean neg = x < 0L ;
             x = Math.abs(x)  ;
             String seconds = "" + ( x % 60 ) ;  //seconds
             if( seconds.length() == 1 )seconds = "0"+seconds;
             x = x / 60 ;
             String minutes = "" + ( x % 60 ) ;  //minutes
             if( minutes.length() == 1 )minutes = "0"+minutes;
             x = x / 60 ;
             String hours = "" + ( x % 24 ) ;  //hours
             x = x / 24 ;

             String days = ( x > 0 ) ? ( ""+x+" days" ) : "" ;

             return ( neg ? "- " : "" ) + days +" "+hours+":"+minutes+":"+seconds ;
          }
          
      }
      
     public class IoJobInfoRow implements RowObjectTableModel.SimpleTableRow {
          private Object []           _values = null ;
          private PoolMoverInfoEntry  _info   = null ;
          private String              _poolName = null ;
          private IoJobInfoRow( String poolName , PoolMoverInfoEntry info ){
             _info     = info ;
             _poolName = poolName ;
             init() ;
          }
          private void init(){
             _values = new Object[11] ;
             _values[0] = _info.info.getClientName() ;
             _values[1] = new Long( _info.info.getClientId() ) ;
             _values[2] = _poolName ;
             _values[3] = new Long( _info.info.getJobId() ) ;
             _values[4] = _info.info.getPnfsId().toString() ;
             _values[5] = _info.info.getStatus();
             _values[6] = new Long( _info.info.getStartTime() ) ;
             _values[7] = new Long( _info.info.getSubmitTime() ) ;
             _values[8] = new Long( _info.info.getTransferTime() ) ;
             _values[9] = new Long( _info.info.getBytesTransferred() ) ;
             _values[10] = new Long( _info.info.getLastTransferred() ) ;
             
          }
          //public String getName(){ return _name ; }
          //public PoolCostInfo.PoolQueueInfo getPoolQueueInfo(){ return _info ; }
          public String toString(){ return _info.info.toString() ; }
          public Object getValueAtColumn( int column ){
              return column < _values.length ? _values[column] : null ;
          }
          public Component renderCell(Component component , Object value , boolean isSelected , 
                                      boolean isFocussed , int row , int column ){

             /*
             component =
                 super.getTableCellRendererComponent(table,value,isSelected,isFocused,row,column);
             */
             JLabel label = (JLabel)component;

             IoJobInfo info = (IoJobInfo)value ;
             //label.setFont(_font) ;
             if( ! isSelected )label.setBackground( row % 2 == 0 ? Color.white : Color.gray ) ;
             label.setHorizontalAlignment( JLabel.CENTER);
             
             switch( column ){
                case 6 :
                    label.setText( _formatter.format( new Date(((Long)value).longValue() )) ) ;
                break ;
             }
            // label.setText(restoreHandlerInfoToString(info,column));
            
             return component ;
          }

      }

      private JSimpleIoMoverTable( DomainConnection connection ){
  
          setModel( _model = new IoMoverModel() ) ;
  
          JTableHeader header = getTableHeader() ;
          header.addMouseListener(_model);
  
          setDefaultRenderer( java.lang.Object.class , new MyRenderer() );
  
          addMouseListener( new PopupTrigger() ) ;
          
          createPopup() ;

      }
      public void setMoverList( PoolMoverInfoEntry [] list ){
         _model.clear() ;
         for( int i = 0 ; i < list.length ; i++ ){
            _model.add( new IoJobInfoRow( list[i].poolName , list[i] )  ) ;
         }
         _model.fire() ;
      }
      public void setInteractiveEnabled( boolean enable ){
          _popup.setEnabled(enable);
      }

   
      private void createPopup(){
         _popup = new JPopupMenu("Edit") ;
         _popup.setBorderPainted(true);
         JMenuItem item = _popup.add("Action on selected queue entries") ;
         item.setForeground(Color.red);
         item.setBackground(Color.blue);
         _popup.addSeparator() ;

         ActionListener al = new PopupAction() ;

         JMenuItem mi = _popup.add( new JMenuItem("Cancel Selected Movers(s)") ) ;
         mi.setActionCommand("cancel");
         mi.addActionListener(al) ;      

         mi = _popup.add( new JMenuItem("Check Door") ) ;
         mi.setActionCommand("checkdoor");
         mi.addActionListener(al) ;      

      }
      private class PopupAction implements ActionListener, DomainConnectionListener {
         public void domainAnswerArrived( Object obj , int id ){
            System.out.println("Popup domainAnswer arrived : "+id+"("+obj.getClass().getName()+"); : "+obj);
            if( id == 22123 ){
               if( obj instanceof dmg.cells.nucleus.NoRouteToCellException ){
                 JOptionPane.showMessageDialog(JSimpleIoMoverTable.this,
                                               "Door nor longer present",
                                               "Mover Help Message",
                                               JOptionPane.INFORMATION_MESSAGE);
               
               }else{
                   showInWindow(obj.toString(), null);
               }
            }
         }
         public void actionPerformed( ActionEvent event ){

             if( ! _popup.isEnabled() ){
                 JOptionPane.showMessageDialog(JSimpleIoMoverTable.this,
                                               "Please switch OFF autoupdate first",
                                               "Restore Help Message",
                                               JOptionPane.INFORMATION_MESSAGE);
                 return ;
             }

             int [] x = getSelectedRows() ;

             if( ( x == null ) || ( x.length == 0 ) ){
                JOptionPane.showMessageDialog(JSimpleIoMoverTable.this,
                                              "No Requests are selected",
                                              "Mover Manager Help Message",
                                              JOptionPane.INFORMATION_MESSAGE);

                return ;
             }
             String action = event.getActionCommand() ;

             if( action.equals("cancel") ){

                 int response =
                     JOptionPane.showConfirmDialog(
                           JSimpleIoMoverTable.this,
                           "Do you really want to 'cancel' selected items" ) ;
                           
                 if( response != JOptionPane.YES_OPTION )return ;
                 sendCancelFor( x ) ;
             }else if( action.equals("checkdoor") ){
             
                 if( x.length > 1 )
                 JOptionPane.showMessageDialog(JSimpleIoMoverTable.this,
                                              "Only a single entry can be selected",
                                              "Mover Manager Help Message",
                                              JOptionPane.INFORMATION_MESSAGE);

                 sendCheckRequest( x[0] ) ;
             }
         }
         private void sendCheckRequest( int row  ){
         
                 try{ 
                     PoolMoverInfoEntry entry =  _model.getQueueInfoAt(row)._info ;
                     _connection.sendObject( entry.info.getClientName() ,
                                             "info" ,
                                             this ,
                                             22123 ) ;
                 }catch(Exception ee ){
                     JOptionPane.showMessageDialog(JSimpleIoMoverTable.this,
                                              "An error occured while sending the request : "+ee,
                                              "Mover Manager Help Message",
                                              JOptionPane.INFORMATION_MESSAGE);            
                 }
             
         }
         private void sendCancelFor( int [] rows  ){
         
             if( ( rows == null ) || ( rows.length == 0 )  )return ;
             int errors = 0 ;
             for( int i = 0 ; i < rows.length ; i++ ){
             
                 int row = rows[i] ;
                 
                 PoolMoverInfoEntry entry =  _model.getQueueInfoAt(row)._info ;
                
                 System.out.println("Sending '"+("mover kill "+entry.info.getJobId())+"' to "+entry.poolName);
                 
                 try{ 
                     _connection.sendObject( entry.poolName ,
                                             "mover kill "+entry.info.getJobId() ,
                                             this ,
                                             10666 ) ;
                 }catch(Exception ee ){
                     ee.printStackTrace() ;
                     errors ++ ;
                 }
                   
             
             }
             if( errors > 0 ){
                JOptionPane.showMessageDialog(JSimpleIoMoverTable.this,
                                              "Errors have been reported (check console output)",
                                              "Mover Manager Help Message",
                                              JOptionPane.INFORMATION_MESSAGE);            
             }
         }
      } 
   }
   private class JMoverDisplay extends JHistogramDisplay {
      private JMoverDisplay( DomainConnection connection ){
         super("Io Mover Queue");
      }
      public void setMoverList( PoolMoverInfoEntry [] list ){
          if( list == null )return ;
          int [] values = new int[list.length] ;
          int [] flags  = new int[list.length] ;
          System.out.println("setMoverList : setting "+list.length+" entries");
          long start = System.currentTimeMillis();
          for( int i= 0 ; i < list.length ; i++ ){
              IoJobInfo info = list[i].info ;
              long startTime = info.getStartTime() ;
              values[i] = (int)Math.max( 0L , startTime == 0L ? 0L : ( start - startTime)/ 1000L ) ;
              flags[i]  = info.getStatus().equals("W") ? 3 : 1 ;
          }
          prepareHistogram( values , flags , 2 , 60 ) ;
      }
   }
   
   public PoolIoMoverPanel( DomainConnection connection ){
      _connection = connection ;
      _table   = new JSimpleIoMoverTable(connection) ;
      _display = new JMoverDisplay(connection);
      
      addActionListener(this);
      addCard( _display ) ;
      addCard( new JScrollPane(_table) ) ;

      setMinimumUpdateTime( 1 ) ; // seconds      
      
   }
   private static final int POOL_MANAGER_REQUEST = 20000 ;
   private static final int POOL_MOVER_REQUEST   = 20001 ;
   private void getTransferInfo(){
      setStatus( STATUS_IN_PROGRESS ) ;
 
      try{
         _connection.sendObject( "PoolManager" ,
                                 "xgetcellinfo" , 
                                 this ,
                                 POOL_MANAGER_REQUEST );
      }catch(Exception ee){
         setStatus( STATUS_IDLE ) ;
         ee.printStackTrace() ;
      }
   }
   public void domainAnswerArrived( Object obj , int id ){
       if( id == POOL_MANAGER_REQUEST ){
           handlePoolManagerReply( obj , id ) ;
       }else if( id >= POOL_MOVER_REQUEST ){
	   handlePoolMoverReply( obj , id );
           //selectAndUpdate() ;
       }
   }
   
   private static int STATUS_IDLE = 0 ;
   private static int STATUS_IN_PROGRESS = 1 ;
   
   private void setStatus( int status ){
      setStatus( status , 0 , null ) ;
   }
   private void setStatus( int status , int errorCode , String errorMessage ){
      if( status == STATUS_IN_PROGRESS ){
         setEnabled(false) ;
      }else if( status == STATUS_IDLE ){
         setEnabled(true) ;
      }
      if( errorCode > 0 )System.err.println("Status changed to "+status+" with error code "+errorCode+" and message "+errorMessage);
   }
   private class PoolMoverInfo {
       private IoJobInfo [] ioInfo   = null ;
       private String       poolName = null ;
       private PoolMoverInfo( String poolName , IoJobInfo [] info ){
           this.ioInfo   = info ;
           this.poolName = poolName ;
       }
   }
   private class PoolMoverInfoEntry {
       private String poolName = null ;
       private IoJobInfo info  = null ;
       private PoolMoverInfoEntry( String name , IoJobInfo info ){
          this.poolName = name ;
          this.info     = info ;
       }
       public String toString(){
          return this.poolName+this.info.toString();
       }
       
   }
   private void handlePoolMoverReply( Object obj , int id ){

      //System.out.println("handlePoolMoverReply : "+id+" obj "+( obj == null ? "null" : obj.getClass().getName() ) ) ;
      synchronized( _lock ){
          _waitingFor -- ;
          if( obj == null ){
             _waitList.add(new Exception("Timeout"));
          }else{
              String poolName = (String)_poolIdMapping.remove(new Integer(id));
              if( ! ( obj instanceof IoJobInfo [] ) ){
                 System.err.println("handlePoolMoverReply : Unexpected message from "+poolName+" "+obj.getClass().getName()+" says "+obj.toString());
                 return;
              }
              IoJobInfo [] jobInfo = (IoJobInfo [])obj ;
              System.out.println("handlePoolMoverReply : Arrived ["+poolName+"] jobs = "+jobInfo.length);     
             _waitList.add( new PoolMoverInfo( poolName , jobInfo ) );
          }
          System.out.println("handlePoolMoverReply : waitingFor "+_waitingFor+" collected " +_waitList.size() );
          if( _waitingFor <= 0 ){
          
              int       i         = 0 ;
              long      start     = System.currentTimeMillis();
              ArrayList results   = new ArrayList() ;
              
                    
              for( Iterator ii = _waitList.iterator() ; ii.hasNext() ; i++ ){
              
                   PoolMoverInfo info = (PoolMoverInfo)ii.next() ;
                   for( int j = 0 , n = info.ioInfo.length ; j < n ; j++ ){
                        System.out.println("Adding to list : "+info.poolName+" "+info.ioInfo[j] ) ;
                        results.add( new PoolMoverInfoEntry( info.poolName , info.ioInfo[j] ) ) ;
                   }
               }
               _moverInfoList = (PoolMoverInfoEntry []) results.toArray( new PoolMoverInfoEntry[results.size()] ) ;
               
               setStatus( STATUS_IDLE ) ;
               System.out.println("handlePoolMoverReply : running selectAndUpdate");
               selectAndUpdate();
          }
      }
   }
   private void handlePoolManagerReply( Object obj , int id ){
      System.out.println("handlePoolManagerReply : "+id+" obj "+( obj == null ? "null" : obj.getClass().getName() ) ) ;
      if( obj == null ){
         setStatus( STATUS_IDLE , 1 , "Timeout" ) ;
      }else if( obj instanceof Exception ){
         setStatus( STATUS_IDLE , 2 , ((Exception)obj).getMessage() ) ;
      }else if( obj instanceof PoolManagerCellInfo ){
          PoolManagerCellInfo poolManagerCellInfo = (PoolManagerCellInfo)obj ;
          String[] poolList = poolManagerCellInfo.getPoolNames().toArray(new String[0]);
          synchronized( _lock ){
              _waitingFor    = poolList.length ;
              _waitList      = new ArrayList() ;
              _poolIdMapping = new HashMap() ;
          } 
          int     newId  = 0 ;
          Integer nextId = null ;
          for( int i = 0 ; i < poolList.length ; i++ ){
          
              newId  = POOL_MOVER_REQUEST + i ;
              nextId = new Integer( newId ) ;
              try{
              
                 _poolIdMapping.put( nextId ,  poolList[i]  ) ;                 
                 _connection.sendObject( poolList[i] ,  "mover ls -binary" , this ,  newId ) ;
                 
               }catch(Exception ee ){
                   synchronized( _lock ){
                        _waitingFor -- ;
                        _poolIdMapping.remove( nextId ) ; 
                   }
                   ee.printStackTrace();
               }
           }
       }else{
           setStatus( STATUS_IDLE , 3 , "FATAL ERROR : wrong class arrived : "+obj.getClass().getName() ) ;
       }         
   
   }
   private void selectAndUpdate( ){
      String sel = _selectString ;
      if( ( sel == null ) || sel.equals("") ){
          try{
              _display.setMoverList( _moverInfoList);
          }catch(Exception ee){
               ee.printStackTrace() ;
          }
          try{
             _table.setMoverList( _moverInfoList ) ; 
          }catch(Exception ee){
               ee.printStackTrace() ;
          }

      }else{
          ArrayList list = new ArrayList() ;
          for( int i = 0 , n = _moverInfoList.length ; i < n ; i++ )
             if( _moverInfoList[i].toString().indexOf(sel) > -1 )list.add(_moverInfoList[i]);

          PoolMoverInfoEntry [] entries = (PoolMoverInfoEntry [] )list.toArray( new PoolMoverInfoEntry[list.size()] ) ;

          _display.setMoverList(entries);
          _table.setMoverList(entries) ;
      }
   }
   private void doSelection( String selection ){
       _selectString = selection ;
       selectAndUpdate() ;
   }
   public void actionPerformed( ActionEvent event ){
      Object source = event.getSource() ;
      String command = event.getActionCommand() ;
      if( command.equals("update") ){
      
          getTransferInfo() ;  
           
      }else if( command.equals( "modechanged" ) ){
      
          _table.setInteractiveEnabled( ! isAutoUpdate() );
          
      }else if( command.equals( "select" ) ){
          JSwitchUpdatePanel.TextSelectionEvent e = (JSwitchUpdatePanel.TextSelectionEvent)event ;
          doSelection(e.getSelectedText());
      }

    }
    private MessageWindow _messageWindow = null ;
       private void showInWindow( String message , Point point ){
       
          if( _messageWindow == null )_messageWindow = new MessageWindow( "Message Window" );

          _messageWindow.setText(message, point );
       }
   public class MessageWindow extends JFrame  implements ActionListener  {
       private JTextArea _textField     = new JTextArea() ;
       private JButton   _disposeButton = new JButton("Dispose Window") ;
       public class WindowActions extends WindowAdapter {
           public void windowDeactivated( WindowEvent event ){
              setVisible(false);
           }
       }
       public class WindowMouseListener extends MouseAdapter {
           public void mouseClicked( MouseEvent event ){
             setVisible(false);
           }
       }
       public MessageWindow( String title ){

          super( title );

          Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

          int windowWidth  = 300 ;
          int windowHeight = 200 ;

          _textField.setEditable(false);

          JPanel centerPanel = new JPanel(new BorderLayout());

          centerPanel.setBorder( new CellGuiSkinHelper.CellBorder("Detailed Results" ,  20  ) ) ;
          centerPanel.add( new JScrollPane( _textField ) , "Center"  ) ;
          getContentPane().add( centerPanel , "Center" ) ;



          pack() ;

          Point p = _ourMaster.getLocation() ;

          setLocation( p.x , p.y  );
          setSize( windowWidth , windowHeight );
          addWindowListener( new WindowActions() );

          WindowMouseListener mouseListener = new WindowMouseListener() ;
          addMouseListener(mouseListener);
          _textField.addMouseListener(mouseListener);
          _disposeButton.addMouseListener(mouseListener) ;


       }
        public void actionPerformed( ActionEvent event ){
           setVisible(false);
       }
       public void setText( String text , Point point ){
           _textField.setText(text);
           setVisible(true);
       }
   }  
}
