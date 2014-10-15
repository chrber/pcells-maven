 // $Id: JRestoreQueueTable.java,v 1.5 2005/07/02 10:33:00 cvs Exp $
//
package org.dcache.gui.pluggins;
//

 import diskCacheV111.vehicles.RestoreHandlerInfo;
 import org.pcells.services.connection.DomainConnection;
 import org.pcells.services.connection.DomainConnectionListener;
 import org.pcells.services.gui.EasyCommander;

 import javax.swing.*;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.DefaultTableCellRenderer;
 import java.awt.*;
 import java.awt.event.*;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.Date;

 public class      JRestoreQueueTable
        extends    JTable
        implements MouseListener {

    private String [] _title = {
        "PnfsId/Net" , "Pool" , "Started" , "Status" , "RC" , "Error"
    } ;

    private Font _font = new Font( "Monospaced" , Font.PLAIN , 12 ) ;

    private boolean            _noaction   = false ;
    private SimpleDateFormat   _formatter  = new SimpleDateFormat ("MM.dd HH:mm:ss");
    private JPopupMenu         _popup      = null ;

    private InfoComparator         _currentComparator = null ;
    private DomainConnection              _connection = null ;
    private RestoreHandlerInfo [] _restoreHandlerInfo = null ;

    private AbstractTableModel _ourModel = new OurModel() ;

    //
    //
    private final static int T_NAME           = 0 ;
    private final static int T_POOL           = 1 ;
    private final static int T_START_TIME     = 2 ;
    private final static int T_STATUS         = 3 ;
    private final static int T_ERROR_CODE     = 4 ;
    private final static int T_ERROR_MESSAGE  = 5 ;
    public class OurModel extends AbstractTableModel {
       public String getColumnName( int index ){
            return _title[index] ;
       }
       public Class getColumnClass(int column ){
           return diskCacheV111.vehicles.RestoreHandlerInfo.class ;
       }
       public int getRowCount(){
         return _restoreHandlerInfo == null ? 0 : _restoreHandlerInfo.length ;
       }
       public int getColumnCount(){
         return 6 ;
       }
       public Object getValueAt( int rowIndex , int columnIndex ){
         if( _restoreHandlerInfo == null )return null ;
     return rowIndex <_restoreHandlerInfo.length? _restoreHandlerInfo[rowIndex] : null ;
       }
    }
    private String restoreHandlerInfoToString( RestoreHandlerInfo info , int columnIndex ){
        switch(columnIndex){
       case T_NAME :
          return info.getName() ;
       case T_POOL :
          return info.getPool() ;
       case T_START_TIME :
          return _formatter.format( new Date(info.getStartTime())) ;
       case T_STATUS :
          return info.getStatus() ;
       case T_ERROR_CODE :
          return ""+info.getErrorCode() ;
       case T_ERROR_MESSAGE  :
          return info.getErrorMessage() == null ? "" : info.getErrorMessage()  ;
       default : return "Unknown" ;
      }

    }
    public void setInteractiveEnabled( boolean enable ){
       _popup.setEnabled(enable);

    }
    private class PopupAction implements ActionListener, DomainConnectionListener {
       public void actionPerformed( ActionEvent event ){

           if( ! _popup.isEnabled() ){
               JOptionPane.showMessageDialog(JRestoreQueueTable.this,
                                             "Please switch OFF autoupdate first",
                                             "Restore Help Message",
                                             JOptionPane.INFORMATION_MESSAGE);
               return ;
           }

           int [] x = getSelectedRows() ;

           if( ( x == null ) || ( x.length == 0 ) ){
              JOptionPane.showMessageDialog(JRestoreQueueTable.this,
                                            "No Requests are selected",
                                            "Restore Help Message",
                                            JOptionPane.INFORMATION_MESSAGE);


              return ;
           }
       String action = event.getActionCommand() ;

           if( action.equals("retry") ){

               int response =
                   JOptionPane.showConfirmDialog(
                     JRestoreQueueTable.this,
                 "Do you really want to 'retry' selected items" ) ;
               if( response == JOptionPane.YES_OPTION )sendRetryFor( x , false ) ;

           }else if( action.equals("retrysi") ){

               int response =
                   JOptionPane.showConfirmDialog(
                     JRestoreQueueTable.this,
                 "Do you really want to 'retry' selected items" ) ;
               if( response == JOptionPane.YES_OPTION )sendRetryFor( x , true ) ;

           }else if( action.equals("cancel") ){

               String response =
                   JOptionPane.showInputDialog(
                     JRestoreQueueTable.this,
                 "Do you really want to 'cancel' selected items" ,
                         "Cancelled by operator" ) ;
               if( response == null )return ;
               sendCancelFor( x , 1020 , response ) ;
           }
       }
       private Object _lock    = new Object() ;
       private int    _pending = 0 ;
       private void sendCancelFor( int [] rows , int rcCode , String rcMessage ){
          if( rows == null )return ;
      if( ! _noaction )_popup.setEnabled(false);
          rcMessage = rcMessage == null ? "Cancelled by operator" : rcMessage ;
          _pendingResults = new StringBuffer() ;
          _pending = 0 ;
      for( int i = 0 , n = rows.length ; i < n ; i++ ){
         String command  = "rc failed "+_restoreHandlerInfo[rows[i]].getName()+
                               " "+rcCode+" \""+rcMessage+"\"" ;

         try{

            synchronized( _lock ){
                   if( ! _noaction ){
                  _connection.sendObject( "PoolManager" ,
                                          command ,
                                          this ,
                                          10000 ) ;

                   }
           _pending ++ ;
            }

         System.out.println("sendCancelFor to PoolManager : "+command);
         }catch(Exception ee){
              ee.printStackTrace() ;
         }
      }
       }
       private StringBuffer _pendingResults = null ;
       private void sendRetryFor( int [] rows , boolean newSi ){
          if( rows == null )return ;
      if( ! _noaction )_popup.setEnabled(false);
          _pendingResults = new StringBuffer() ;
          _pending = 0 ;
      for( int i = 0 , n = rows.length ; i < n ; i++ ){
         String command  = "rc retry "+(newSi?"-update-si ":"")+_restoreHandlerInfo[rows[i]].getName() ;

         try{

            synchronized( _lock ){
                   if( ! _noaction ){
                   _connection.sendObject( "PoolManager" ,
                                           command ,
                                           this ,
                                           10000 ) ;
           }
           _pending ++ ;
            }

         System.out.println("Command : "+command);
         }catch(Exception ee){
              ee.printStackTrace() ;
         }
      }
       }
       public void domainAnswerArrived( Object obj , int id ){
           synchronized( _lock ){
           System.out.println("Reply : "+obj);
           _pending -- ;
               String res = obj.toString().trim() ;
               if( res.length() != 0 )_pendingResults.append(res).append("\n");
           if( _pending == 0 ){
                  res = _pendingResults.toString() ;
                  if( res.length() == 0 )tell("Ok");
                  else tell("Result : "+res);
                  _popup.setEnabled(true);
               }
       }
       }
    }
    private void createPopup(){
       _popup = new JPopupMenu("Edit") ;
       _popup.setBorderPainted(true);
       JMenuItem item = _popup.add("Action on selected queue entries") ;
       item.setForeground(Color.red);
       item.setBackground(Color.blue);
       _popup.addSeparator() ;
       ActionListener al = new PopupAction() ;
       JMenuItem mi = null ;
       /*
       mi = _popup.add( new JMenuItem("Retry Selected") ) ;
       mi.setActionCommand("retry");
       mi.addActionListener(al) ;
       */
       mi = _popup.add( new JMenuItem("Retry Selected Request(s)") ) ;
       mi.setActionCommand("retrysi");
       mi.addActionListener(al) ;
       mi = _popup.add( new JMenuItem("Cancel Selected Request(s)") ) ;
       mi.setActionCommand("cancel");
       mi.addActionListener(al) ;
       /*
       mi = _popup.add( new JMenuItem("Retry All Failed") ) ;
       mi.setActionCommand("retryerror");
       mi.addActionListener(al) ;
       mi = _popup.add( new JMenuItem("Retry All Failed (update StorageInfo)") ) ;
       mi.setActionCommand("retryerrorsi");
       mi.addActionListener(al) ;
       mi = _popup.add( new JMenuItem("Retry All") ) ;
       mi.setActionCommand("retryall");
       mi.addActionListener(al) ;
       mi = _popup.add( new JMenuItem("Retry All (update StorageInfo)") ) ;
       mi.setActionCommand("retryallsi");
       mi.addActionListener(al) ;
       */
    }
    private class InfoComparator implements Comparator {
        private boolean _topHigh = false ;
        private int     _sort    = 0 ;
        private InfoComparator( int column , boolean  topHigh ){
            _sort = column ;
        _topHigh = topHigh ;
        }
        private void swap(){ _topHigh = ! _topHigh ; }
        public int compare( Object a , Object b ){
           RestoreHandlerInfo [] info1 = { (RestoreHandlerInfo)a , (RestoreHandlerInfo)b  } ;
           RestoreHandlerInfo [] info2 = { (RestoreHandlerInfo)b , (RestoreHandlerInfo)a  } ;
       RestoreHandlerInfo [] info  = _topHigh ? info1 : info2 ;

       switch(_sort){
          case T_NAME :
             return info[0].getName().compareTo( info[1].getName() ) ;
          case T_POOL :
         return info[0].getPool().compareTo( info[1].getPool() )  ;
          case T_START_TIME :
         return  new Long(info[0].getStartTime()).compareTo( new Long(info[1].getStartTime()) ) ;
          case T_STATUS :
         return info[0].getStatus().compareTo( info[1].getStatus() )  ;
          case T_ERROR_CODE :
         return new Integer(info[0].getErrorCode()).compareTo( new Integer(info[1].getErrorCode()) ) ;
          case T_ERROR_MESSAGE  :
         return 0  ;
          default : return 0 ;
       }

        }
    }
    private Color _myGray = new Color( 230 , 230  , 230 ) ;
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

         RestoreHandlerInfo info = (RestoreHandlerInfo)value ;
         label.setFont(_font) ;
         if( ! isSelected )label.setBackground( row % 2 == 0 ? Color.white : _myGray ) ;
         label.setHorizontalAlignment( JLabel.CENTER);
         label.setText(restoreHandlerInfoToString(info,column));

          return component ;
        }
    }
    public JRestoreQueueTable(DomainConnection connection){

        _connection = connection ;
        setModel(_ourModel);

        setDefaultRenderer(diskCacheV111.vehicles.RestoreHandlerInfo.class , new MyRenderer() );

        getTableHeader().addMouseListener(this);
        getColumnModel().getColumn(4).setPreferredWidth(10) ;

        createPopup() ;

        addMouseListener( new PopupTrigger() ) ;

    }
    private void tell( String message ){
       StringBuffer sb = new StringBuffer() ;
       for(int i = 1 , n = message.length() ; i<= n ; i++ ){
           if( ( i % 80 ) == 0 )sb.append("\n");
           sb.append( message.charAt(i-1) ) ;
       }
       JOptionPane.showMessageDialog(JRestoreQueueTable.this,sb.toString());
    }
     public void setRestoreQueue(  RestoreHandlerInfo [] info ){
        if( _restoreHandlerInfo != null ){
            if( _restoreHandlerInfo.length > 0 )_ourModel.fireTableRowsDeleted(0,_restoreHandlerInfo.length-1);
        }
        _restoreHandlerInfo = info ;
        if( _restoreHandlerInfo != null ){
            if( _restoreHandlerInfo.length > 0 )_ourModel.fireTableRowsInserted(0,_restoreHandlerInfo.length-1);
        }
        sort() ;
     }
     private void sort(){
        if( ( _restoreHandlerInfo == null ) ||  ( _currentComparator == null ) )return ;
        Arrays.sort( _restoreHandlerInfo , _currentComparator ) ;
        _ourModel.fireTableDataChanged() ;
     }
     private void doOnPopupTrigger( MouseEvent event ){
        _popup.show(this,event.getPoint().x,event.getPoint().y);
     }
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
          public void mouseClicked( MouseEvent event ){
             if( event.getClickCount() > 1 ){
                 int row = rowAtPoint( event.getPoint() );
                 if( row < 0 )return ;
                 RestoreHandlerInfo [] infoArray  =  _restoreHandlerInfo ;
                 if( infoArray == null )return ;

                 RestoreHandlerInfo info = infoArray[row]  ;
                 String pool = info.getPool() ;
                 if( ( pool == null ) || pool.equals("") || pool.startsWith("<") ){
                    JOptionPane.showMessageDialog(JRestoreQueueTable.this,
                                                  "Pool name not yet available",
                                                  "Restore Help Message",
                                                  JOptionPane.INFORMATION_MESSAGE);
                    return ;
                 }
                 String [] dest = { info.getPool() } ;
                 showCommander(dest);

             }
          }
     }
     private JFrame        _commanderFrame = null ;
     private EasyCommander _easyCommander = null ;
     private void showCommander( String [] destinations ){
         WindowListener l = new WindowAdapter() {
             public void windowClosing(WindowEvent e) { _commanderFrame.setVisible(false);}
         };
         if( _commanderFrame == null ){
             _easyCommander  = new EasyCommander( _connection ) ;
             _commanderFrame = new JFrame("EasyCommander") ;
             _commanderFrame.getContentPane().add( _easyCommander ) ;
             _commanderFrame.addWindowListener(l);
             _commanderFrame.setLocation(100,100);
             _commanderFrame.setSize(600,400);
         }
         if( destinations != null ){
            for( int i = 0 , n = destinations.length ; i < n ; i++ )
               if( destinations[i]  != null )_easyCommander.setDestination( destinations[i] ) ;
         }
         _commanderFrame.setVisible(true);
     }
     public void mouseClicked( MouseEvent event ){
        int column = getTableHeader().columnAtPoint(event.getPoint()) ;
        if( _currentComparator == null ){
           _currentComparator = new InfoComparator( column , true ) ;
        }else if( _currentComparator._sort == column ){
            _currentComparator.swap() ;
        }else{
           _currentComparator = new InfoComparator( column , true ) ;
        }
        sort() ;
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
