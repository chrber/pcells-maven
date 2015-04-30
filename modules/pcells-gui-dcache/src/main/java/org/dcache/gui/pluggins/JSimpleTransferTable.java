 // $Id: JSimpleTransferTable.java,v 1.4 2005/07/02 10:33:00 cvs Exp $
//
package org.dcache.gui.pluggins;
//

 import diskCacheV111.vehicles.IoJobInfo;
 import org.pcells.services.connection.DomainConnection;
 import org.pcells.services.connection.DomainConnectionListener;
 import org.pcells.services.gui.EasyCommander;

 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JTable;
 import javax.swing.SwingConstants;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.DefaultTableCellRenderer;

 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Comparator;

public class      JSimpleTransferTable 
       extends    JTable
       implements ActionListener, 
                  MouseListener {

   private String [] _title = {
       "Cell Name" , "Seq ID" , "PnfsId" , "Protocol" , "UserId" , "Pid" , "Pool" , "Client Host" ,
       "Status" , "Time" , "Mode"
   } ;
   
   private boolean _noaction = false ;
   private Font    _font     = new Font( "Monospaced" , Font.PLAIN , 12 ) ;
   
   private  SimpleDateFormat  _formatter  = new SimpleDateFormat ("MM.dd HH:mm:ss");
   private  JPopupMenu        _popup      = null ;

   private InfoComparator     _currentComparator = null ;
   private DomainConnection   _connection = null ;
   private TableEntry  []     _transferInfo = null ;
   
   private ListOptionPanel    _listOptionPanel = new ListOptionPanel();
   //
   //
   
   public class TransferTableModel extends AbstractTableModel {
      public String getColumnName( int index ){
           return _title[index] ;
      }
      public Class getColumnClass(int column ){
          return org.dcache.gui.pluggins.JTransferPanel.TransferInfo.class ; 
      }
      public int getRowCount(){
        return _transferInfo == null ? 0 : _transferInfo.length ;
      }
      public int getColumnCount(){
        return 11 ;
      }
      public Object getValueAt( int rowIndex , int columnIndex ){
        if( _transferInfo == null )return null ;
	return _transferInfo[rowIndex] ;
      }   
   }
   private static final String __byteValues [] = { "B" , "KB" , "MB" , "GB" , "TB" , "PB" , "EB"  } ;
   private String bytesToString( long value ){
      for( int i = 0 ,  n = __byteValues.length ; i < n ; i++ ){
         if(  value < 1024 )return ""+value+" "+__byteValues[i] ;
         value = value / 1024 ;
      }
      return "BigNumber";
   }
   private String intervalToString( long value ){
      value = Math.max( 0L , value ) ;
      
      int sec = (int)( value % 60L ) ;
      String secString = ""+sec ;
      if( secString.length() == 1 )secString = "0"+secString ;
      value = value / 60L ;
      
      sec = (int)(value % 60L) ;
      
      String minString = ""+sec ;
      if( minString.length() == 1 )minString = "0"+minString ;
      value = value / 60L ;
      
      sec = (int)value ;
      
      return ""+sec+":"+minString+":"+secString ;
   }
   private class TableEntry {
       JTransferPanel.TransferInfo info ;
       String [] infoString = new String[11]  ;
       boolean   isSpecial = false ;
       private TableEntry( JTransferPanel.TransferInfo info ){
           this.info = info ;
           infoString[T_NAME]     = info._cellName+"@"+info._domainName ;
           infoString[T_SEQNUM]   = info._serialId.toString() ;
           infoString[T_PNFSID]   = info._pnfsId.toString() ;
           infoString[T_PROTOCOL] = info._protocol.toString()  ;
           infoString[T_UID]      = info._uid.toString()  ;
           infoString[T_PID]      = info._pid.toString()  ;
           infoString[T_POOL]     = info._pool.toString() ;
           infoString[T_CLIENT]   = info._client.toString() ;
           String status = info._status ;
           infoString[T_STATUS] =
                status.equals("WaitingForDoorTransferOk") ? "T" :
                status.equals("WaitingForPnfs")  ? "P" :
                status.equals("WaitingForOpenFile")  ? "O" :
                status.equals("WaitingForGetPool")  ? "S" : status ;
           infoString[T_START_TIME]   = intervalToString(info._timer.longValue()/1000L) ;
           
           String mode = info._mode ;
           if( mode.startsWith("N") ){
              infoString[T_MODE] = "No Mover" ;
           }else if( mode.startsWith("W") ){
              infoString[T_MODE] =  "Mover Queued" ;
           }else{
              if( ( info._bytesTransferred != null ) &&
                  ( info._transferStarted != null  )   ){

                  infoString[T_MODE] =  bytesToString( info._bytesTransferred.longValue() )+" / "+
                         intervalToString( info._transferStarted.longValue()/1000L ) ;
              }else{
                 infoString[T_MODE] =  "?";
              }
           }
           isSpecial = status.equals("WaitingForDoorTransferOk") && 
                       mode.startsWith("N") ;
      }
   }
   private final static int T_NAME         = 0 ;
   private final static int T_SEQNUM       = 1 ;
   private final static int T_PNFSID       = 2 ;
   private final static int T_PROTOCOL     = 3 ;
   private final static int T_UID          = 4 ;
   private final static int T_PID          = 5 ;
   private final static int T_POOL         = 6 ;
   private final static int T_CLIENT       = 7 ;
   private final static int T_STATUS       = 8 ;
   private final static int T_START_TIME   = 9 ;
   private final static int T_MODE         = 10 ;

   public void setInteractiveEnabled( boolean enable ){
      _popup.setEnabled(enable);
   
   }
   private static final int R_RETRY    = 10000 ;
   private static final int R_JOBINFO  = 3001 ;
   private static final int R_JOBKILL  = 3002 ;
   private static final int R_DOORKILL = 3003 ;
   private void tell( String message ){
      StringBuffer sb = new StringBuffer() ;
      for(int i = 1 , n = message.length() ; i<= n ; i++ ){
          if( ( i % 80 ) == 0 )sb.append("\n");
          sb.append( message.charAt(i-1) ) ;
      }
      JOptionPane.showMessageDialog(JSimpleTransferTable.this,sb.toString());
   }
   private class ListOptionPanel {
      private JOptionPane _optionPane = null ;
      private JPanel      _optionPanel = null ;
      private class XLabel extends JLabel {
          public XLabel( String text , int pos ){
             super( text , pos ) ;
             setBackground(Color.white) ;
             setOpaque(true);
          }
          public Insets getInsets(){ System.out.println("INsets"); return new Insets(10,10,10,10) ; }

      }
      private ListOptionPanel(){
         _optionPanel = new JPanel( new GridLayout(0,2,5,5) ) ;
         _optionPane  = new JOptionPane( _optionPanel , JOptionPane.QUESTION_MESSAGE , JOptionPane.OK_CANCEL_OPTION ) ;
      }
      private void clear(){ _optionPanel.removeAll() ; }
      private void addRow( String key , String value ){
         _optionPanel.add(  new XLabel( key   , SwingConstants.LEFT ) ) ;
         _optionPanel.add(  new XLabel( value , SwingConstants.RIGHT ) ) ;
      }
      private int showDialog(){
      
        JDialog dialog = _optionPane.createDialog( JSimpleTransferTable.this , "Do you really want to kill this Job" ) ;
        dialog.show() ;
        Object o = _optionPane.getValue() ;
        return ( o instanceof Integer ) ? ( (Integer)o ).intValue() : -1 ;
      
      }
   }
   private int ask( String message ){
      return JOptionPane.showConfirmDialog(JSimpleTransferTable.this,message);
   }
   private class PopupAction implements ActionListener, DomainConnectionListener {
   
      public void actionPerformed( ActionEvent event ){
      
          String command = event.getActionCommand();
          
          if( ! _popup.isEnabled() ){
             tell("Please disable autoupdate first");
             return ;
          }
          int [] x = getSelectedRows() ;
          if( ( x == null ) || ( x.length == 0 ) ){
             tell( "Please select at least one transfer request");
             return ;
          }
          if( command.equals("retry") ){
          
               int response = ask( "Do you really want to 'retry' all selected queue items");
                         
               if( response == JOptionPane.YES_OPTION )sendRetryFor( x ) ;
               
          }else if( command.equals("jobinfos") ){
          
               if( x.length != 1 ){
                  tell("Please select exactly ONE request");
                  return ;
               }
               int row = x[0] ;
               if( row < 0 ){
                  tell("INTERNAL PROBLEM : Please try again selecting one queue item");
                  return ;
               }
               JTransferPanel.TransferInfo info = _transferInfo[row].info ;
               if( info._pool.startsWith("<") ){
                  tell("The selected job doesn't have a pool attached yet");
                  return ;
               }
               sendQueryJobInfos( info ) ;
               
          }else if( command.equals("killdoor") ){
          
               if( x.length != 1 ){
                  tell("Please select exactly ONE request");
                  return ;
               }
               int row = x[0] ;
               if( row < 0 ){
                  tell("INTERNAL PROBLEM : Please try again selecting one queue item");
                  return ;
               }
               int response = ask( "Do you really want to kill the selected door ?");
                         
               if( response != JOptionPane.YES_OPTION )return ;
               
               JTransferPanel.TransferInfo info = _transferInfo[row].info ;
               sendKillDoor( info ) ;
               
          }
      }
      private Object       _lock    = new Object() ;
      private int          _pending = 0 ;
      private StringBuffer _pendingResults = null ;
      
      private JTransferPanel.TransferInfo _transferInfoOnKill = null ;
      
      private void sendQueryJobInfos( JTransferPanel.TransferInfo info ){
	    try{
 	       synchronized( _lock ){
                   String command = "mover ls -binary" ;
        	  _connection.sendObject( info._pool ,
                                	  command , 
                                	  this , 
                                          R_JOBINFO ) ;
                 _transferInfoOnKill = info ;
 		  System.out.println("Sending Command ("+info._pool+") : "+command);
	       }
            }catch(Exception ee){
        	 ee.printStackTrace() ;
	    }
      }
      private void sendKillDoor( JTransferPanel.TransferInfo info ){
	    try{
 	       synchronized( _lock ){
                
                   String command     = "kill "+info._cellName ;
                   String destination = "System@"+info._domainName ;

                   System.out.println("sendKillDoor : "+destination+" : "+command );
                   if( ! _noaction ){
        	     _connection.sendObject( destination ,
                                	     command , 
                                	     this , 
                                             R_DOORKILL ) ;
 		   }
	       }
            }catch(Exception ee){
               tell("Internal Error sending DOORKILL : "+ee);
               ee.printStackTrace() ;
	    }
      
      }
      private void sendKill( JTransferPanel.TransferInfo info , IoJobInfo job ){
	    try{
 	       synchronized( _lock ){
                   String command = "mover kill "+job.getJobId();
                   if( ! _noaction ){
        	      _connection.sendObject( info._pool ,
                                	      command , 
                                	      this , 
                                              R_JOBKILL ) ;
 		   }
                  System.out.println("sendKill : "+info._pool+" : "+command);
	       }
            }catch(Exception ee){
        	 ee.printStackTrace() ;
	    }
      
      }
      private void sendRetryFor( int [] rows ){
         if( rows == null )return ;
	 if( ! _noaction )JSimpleTransferTable.this.setEnabled(false);
         _pending = 0 ;
         _pendingResults = new StringBuffer() ;
	 for( int i = 0 , n = rows.length ; i < n ; i++ ){
            JTransferPanel.TransferInfo info = _transferInfo[rows[i]].info ;
            
	    String command  = "retry "+info._serialId ;
	    String destination = info._cellName+"@"+info._domainName ;
	    try{
	       
	       synchronized( _lock ){
               
                   if( ! _noaction ){
        	      _connection.sendObject( destination ,
                                	      command , 
                                	      this , 
                                              R_RETRY ) ;
                   }		
		  _pending ++ ;
	       }
		
		System.out.println("sendRetryFor"+destination+") : "+command);
	    }catch(Exception ee){
        	 ee.printStackTrace() ;
	    }
	 }
      }
      public void domainAnswerArrived( Object obj , int id ){
        try{
           domainAnswerArrivedException(obj,id);
        }catch(Exception ee ){
           ee.printStackTrace() ;
        }
      }
      public void domainAnswerArrivedException( Object obj , int id ){
         if( id == R_RETRY ){
         
            synchronized( _lock ){
            
	        System.out.println("Reply : "+obj);
                String res = obj.toString().trim() ;
                if( res.length() > 0 )_pendingResults.append(res).append("\n");
	        _pending -- ;
	        if( _pending == 0 ){
                   res = _pendingResults.toString() ;
                   if( res.length() == 0 )tell("Retry Door succeeded");
                   else tell(_pendingResults.toString());
                   JSimpleTransferTable.this.setEnabled(true);
                }
            
            }
            
         }else if( id == R_JOBKILL){
         
            System.out.println("Result of jobkill : "+obj);
            if( obj.getClass() != java.lang.String.class ){
               tell("Reply from 'mover kill' : "+obj.getClass().getName()+" : "+obj.toString() );   
            }else{
               tell("Reply from 'mover kill' : "+obj.toString() ) ;
            }   
            
         }else if( id == R_DOORKILL ){
            if( obj.getClass() != java.lang.String.class ){
               tell("Problem : Reply on 'kill door' "+obj.getClass().getName()+" : "+obj.toString() );   
            }else{
               String res = ((String)obj).trim() ;
               if( res.length() == 0 )tell("Kill Door succeeded");
               else tell("Reply on 'door kill "+res ) ;
            }   
         }else if( id == R_JOBINFO ){
 
            System.out.println("Result of JOBINFO : "+obj);
            if( obj instanceof IoJobInfo [] ){
            
               synchronized( _lock ){
               
                   if( _transferInfoOnKill == null ){
                       tell("Internal Error : transfer info not set (4523)");
                       return ;
                   }
                   IoJobInfo [] x = (IoJobInfo [])obj ;
                   String door = _transferInfoOnKill._cellName+"@"+_transferInfoOnKill._domainName ;
                   IoJobInfo found = null ;
                   for( int i= 0 ; i < x.length ; i++ ){
                       if( x[i].getClientName().equals(door) && 
                           ( _transferInfoOnKill._serialId == x[i].getClientId() ) ){
                           found = x[i] ;   
                           break ;
                       }
                   }
                   if( found != null ){
                   
                      //String infoString = "Do you really want to kill\n" + ioJobInfoToString(found) ;
                      prepareListOptionPanel( found ) ;
                      int result = _listOptionPanel.showDialog() ;
                      if( result == JOptionPane.YES_OPTION ){
                         sendKill(_transferInfoOnKill,found);
                         System.out.println("Sending kill of "+found);
                      }else{
                         System.out.println("Not sending kill of "+found);
                      }
                   }else{
                       tell("Problem : Mover no longer active on pool");            
                   }
                   _transferInfoOnKill = null ;
               }
            }else{
               tell("Problem : Reply on 'mover ls' "+obj.getClass().getName());
            }
         }
      }
   }
   private void prepareListOptionPanel( IoJobInfo jobInfo ){
      _listOptionPanel.clear() ;
      _listOptionPanel.addRow( "Door Name" , jobInfo.getClientName() ) ;
      _listOptionPanel.addRow( "Door seq ID" , ""+jobInfo.getClientId() ) ;
      _listOptionPanel.addRow( "PnfsId" , jobInfo.getPnfsId().toString() ) ;
      _listOptionPanel.addRow( "Bytes Transferred" , bytesToString(jobInfo.getBytesTransferred() ) ) ;
      _listOptionPanel.addRow( "TransferTime" , intervalToString(jobInfo.getTransferTime()/1000L) ) ;
      long active = System.currentTimeMillis() - jobInfo.getLastTransferred() ;
      _listOptionPanel.addRow( "Last Accessed" , intervalToString(active/1000L) ) ;
   } 
   private String ioJobInfoToString( IoJobInfo jobInfo ){
      StringBuffer sx = new StringBuffer() ;
      sx.append(jobInfo.getClientName()).append("\n").
         append("ID : ").append(jobInfo.getClientId()).append("\n").
         append(jobInfo.getPnfsId().toString()).append("\n").
         append(bytesToString(jobInfo.getBytesTransferred())).append(" transferred\n").
         append(intervalToString(jobInfo.getTransferTime()/1000L)).append("\n").
         append(intervalToString(jobInfo.getLastTransferred()/1000L)).append(" last access\n");
      return sx.toString() ;
   }
   private void createPopup(){
      _popup = new JPopupMenu("Edit") ;
      _popup.setBorderPainted(true);
      JMenuItem item = _popup.add("Action on selected queue entries") ;
      item.setForeground(Color.red);
      item.setBackground(Color.blue);
      _popup.addSeparator() ;
      ActionListener al = new PopupAction() ;
      JMenuItem mi = _popup.add( new JMenuItem("Retry selected mover(s)") ) ;
      mi.setActionCommand("retry");
      mi.addActionListener(al) ;
      mi = _popup.add( new JMenuItem("Kill selected mover(s)") ) ;
      mi.setActionCommand("jobinfos");
      mi.addActionListener(al) ;
      mi = _popup.add( new JMenuItem("Kill selected door") ) ;
      mi.setActionCommand("killdoor");
      mi.addActionListener(al) ;
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
          TableEntry [] info1 = { (TableEntry)a , (TableEntry)b  } ;
          TableEntry [] info2 = { (TableEntry)b , (TableEntry)a  } ;
	  TableEntry [] info  = _topHigh ? info1 : info2 ;
	  
	  switch(_sort){ 
            case T_NAME :
            return 
               info[0].infoString[_sort].equals( info[1].infoString[_sort] ) ?
               info[0].info._serialId.compareTo( info[1].info._serialId ) :
               info[0].infoString[_sort].compareTo( info[1].infoString[_sort] ) ;
            case T_PNFSID :
            case T_PROTOCOL :
            case T_CLIENT :
            case T_STATUS :
            case T_POOL :
            return info[0].infoString[_sort].compareTo( info[1].infoString[_sort] ) ;
            
            case T_SEQNUM :           
            return info[0].info._serialId.compareTo( info[1].info._serialId ) ;
            case T_UID :           
            return info[0].info._uid.compareTo( info[1].info._uid ) ;
            case T_PID :           
            return info[0].info._pid.compareTo( info[1].info._pid ) ;
            case T_START_TIME :           
            return info[0].info._timer.compareTo( info[1].info._timer ) ;
            case T_MODE:
              if( ( info[0].info._transferSpeed != null ) &&
                  ( info[1].info._transferSpeed != null    ) )
                  return  info[0].info._transferSpeed .compareTo( info[1].info._transferSpeed ) ;
 
              if( info[0].infoString[T_MODE].equals( info[1].infoString[T_MODE] ) )
                 return info[0].info._timer.compareTo( info[1].info._timer ) ;
 	     default : return 0 ;
	  }
	  
       }
   }
   private Color _myGray = new Color( 230 , 230  , 230 ) ;
   private Color _myRed  = new Color( 240 , 190 , 190 );
   public class MyRenderer extends DefaultTableCellRenderer {
       public MyRenderer(){
       
       }
       public Component getTableCellRendererComponent(
                            JTable table ,
			    Object value ,
			    boolean isSelected ,
			    boolean isFocused ,
			    int row , int column ){
                            
	     Component component = 
	         super.getTableCellRendererComponent(table,value,isSelected,isFocused,row,column);
             if( value == null )return component;
             
	     JLabel label = (JLabel)component;
	     
		TableEntry info = (TableEntry)value ;
		label.setFont(_font) ;
		if( ! isSelected ){
                   label.setBackground( info.isSpecial ? _myRed : ( row % 2 == 0 ) ? Color.white : _myGray ) ;
                }
		label.setHorizontalAlignment( JLabel.CENTER);
		label.setText( info.infoString[column] );

	     return component ;		    
       }
   }
   private AbstractTableModel _transferTableModel = new TransferTableModel() ;
   public JSimpleTransferTable(DomainConnection connection){

       _connection = connection ;
       setModel(_transferTableModel);

       setDefaultRenderer(JTransferPanel.TransferInfo.class , new MyRenderer() );

       getTableHeader().addMouseListener(this);
       getColumnModel().getColumn(4).setPreferredWidth(10) ;
       
       createPopup() ;
       
       addMouseListener( new PopupTrigger() ) ;

   }
   public void setTransferList(  JTransferPanel.TransferInfo [] info ){
      if( info == null )return ;
      if( ( _transferInfo != null ) && ( _transferInfo.length > 0 ) ){
          _transferTableModel.fireTableRowsDeleted(0,_transferInfo.length-1);      
      }
      _transferInfo = new TableEntry[info.length];
      for( int i= 0 , n = info.length ; i < n ; i++ )
         _transferInfo[i] = new TableEntry(info[i]) ;
         
      if( ( _transferInfo != null ) && ( _transferInfo.length > 0 ) ){
          _transferTableModel.fireTableRowsInserted(0,_transferInfo.length-1);
      }
      sort() ;
   }
   public void actionPerformed( ActionEvent event ){
      Object source = event.getSource() ;
      
    }  
    private void sort(){
       if( ( _transferInfo == null ) ||  ( _currentComparator == null ) )return ;
       Arrays.sort( _transferInfo , _currentComparator ) ;
       _transferTableModel.fireTableDataChanged() ;
    }
    private void doOnPopupTrigger( MouseEvent event ){
       //if( ! _popup.isEnabled() )return ;
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
                JTransferPanel.TransferInfo info = _transferInfo[row].info ;
                String door = info._cellName+"@"+info._domainName ;
                String [] dest = null ;
                if( info._pool.startsWith("<") ){
                   dest = new String[1] ;
                   dest[0] = door ;
                }else{
                   dest = new String[2] ;
                   dest[0] = info._pool ;
                   dest[1] = door ;
                }
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
