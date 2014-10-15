// $Id: JFlushMonitorPanel.java,v 1.2 2006/05/24 13:06:28 cvs Exp $
//
package org.dcache.gui.pluggins.flush;
//

import diskCacheV111.hsmControl.flush.HsmFlushControlCore;
import diskCacheV111.pools.PoolCellInfo;
import diskCacheV111.pools.PoolCostInfo;
import diskCacheV111.pools.StorageClassFlushInfo;
import org.pcells.services.connection.DomainConnection;
import org.pcells.services.connection.DomainConnectionListener;
import org.pcells.services.gui.CellGuiSkinHelper;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

public class      JFlushMonitorPanel
       extends    CellGuiSkinHelper.CellPanel {
                  
   private DomainConnection   _connection = null ;
   private JFlushControlPanel.Controller _controller = null ;
   private PoolTable  _poolTable   = null ;
   private FlushTable _flushTable  = null ;

   /*
    *   Master Layout
    */
   public JFlushMonitorPanel( JFlushControlPanel.Controller controller , DomainConnection connection , java.util.prefs.Preferences pref){
      _connection = connection ;
      _controller = controller ;


      BorderLayout l = new BorderLayout() ;
      /*
      l.setVgap(10) ;
      l.setHgap(10);
      */
      setLayout(l) ;
      /*
      setBorder( new CellBorder("Flush Control",35 ) ) ;
      */
      _poolTable  = new PoolTable() ;
      _flushTable = new FlushTable() ;


      JSplitPane split = new JSplitPane(
                               JSplitPane.VERTICAL_SPLIT ,
                                 new JScrollPane( _poolTable ) ,
                                 new JScrollPane( _flushTable)  ) ;

      add( split , "Center"  ) ;

      split.setDividerLocation( (double) 0.25 ) ;
   }
   public void setRowSelectionAllowed( boolean allowed ){
      _poolTable.setRowSelectionAllowed(allowed) ;
      _flushTable.setRowSelectionAllowed(allowed) ;
      if( ! allowed ){
         _poolTable.clearSelection();
         _flushTable.clearSelection();
      }
   }
   /**
     *  Helpers and their comparators
     */
   private class PoolEntry {

      private String  _poolName     = null ;
      private long    _total        = 0 ;
      private long    _precious     = 0 ;
      private int     _flushing     = 0 ;
      private boolean _isReadOnly   = false ;
      private String  _storageClass = null ;

   }
   abstract private class EntryComparator implements Comparator {
       boolean _topHigh    = true ;
       int     _sortColumn = 0 ;
       private void setColumn( int column ){

          if( _sortColumn == column ){
              _topHigh = ! _topHigh ;
          }else{
              _sortColumn = column ;
              _topHigh    = false ;
          }
       }
       abstract public int compare( Object a , Object b ) ;
       int compareBoolean( boolean a , boolean b ){
          return a ^ b ? ( a ? 1 : -1 ) : 0 ;
       }
       int compareInt( int a , int b ){
          return a == b ? 0 : a > b ? 1 : -1 ;
       }
       int compareLong( long a , long b ){
          return a == b ? 0 : a > b ? 1 : -1 ;
       }
       int compareDouble( double a , double b ){
          return a == b ? 0 : a > b ? 1 : -1 ;
       }
   }
   private class PoolEntryComparator extends EntryComparator {

       public int compare( Object a , Object b ){
          PoolEntry [] info1 = { (PoolEntry)a , (PoolEntry)b  } ;
          PoolEntry [] info2 = { (PoolEntry)b , (PoolEntry)a  } ;
	  PoolEntry [] info  = _topHigh ? info1 : info2 ;
	  int t = 0 ;
	  switch(_sortColumn){
	     case 0 :
	        return info[0]._poolName.compareTo( info[1]._poolName ) ;
	     case 1 :
	        t = compareBoolean( info[0]._isReadOnly , info[1]._isReadOnly ) ;
	        return  t == 0 ? info[0]._poolName.compareTo( info[1]._poolName ) : t ;
	     case 2 :
	        t = compareInt( info[0]._flushing , info[1]._flushing ) ;
	        return  t == 0 ? info[0]._poolName.compareTo( info[1]._poolName ) : t ;
	     case 3 :
	        t = compareLong( info[0]._total , info[1]._total ) ;
 	        return  t == 0 ? info[0]._poolName.compareTo( info[1]._poolName ) : t ;
	     case 4 :
	        t = compareLong( info[0]._precious , info[1]._precious)  ;
 	        return  t == 0 ? info[0]._poolName.compareTo( info[1]._poolName ) : t ;
	     case 5 :
	        return compareDouble(
                            (double)info[0]._precious / (double)info[0]._total  ,
                            (double)info[1]._precious / (double)info[1]._total     ) ;
	     default : return 0 ;
	  }

       }
   }
   private class FlushEntryComparator extends EntryComparator {

       public int compare( Object a , Object b ){
          FlushEntry [] info1 = { (FlushEntry)a , (FlushEntry)b  } ;
          FlushEntry [] info2 = { (FlushEntry)b , (FlushEntry)a  } ;
	  FlushEntry [] info  = _topHigh ? info1 : info2 ;
	  int t = 0 ;
	  switch(_sortColumn){
	     case 0 :
	        t = info[0]._poolName.compareTo( info[1]._poolName ) ;
                return t == 0 ? info[0]._storageClass.compareTo( info[1]._storageClass ) : t ;
	     case 1 :
	        t = info[0]._storageClass.compareTo( info[1]._storageClass ) ;
                return t == 0 ? info[0]._poolName.compareTo( info[1]._poolName ) : t ;
	     case 2 :
	        t = compareBoolean( info[0]._isFlushing , info[1]._isFlushing ) ;
                t = t == 0 ? info[0]._poolName.compareTo( info[1]._poolName ) : t ;
	        return  t == 0 ? info[0]._storageClass.compareTo( info[1]._storageClass ) : t ;
	     case 3 :
	        t = compareLong( info[0]._total , info[1]._total ) ;
 	        t = t == 0 ? info[0]._poolName.compareTo( info[1]._poolName ) : t ;
	        return  t == 0 ? info[0]._storageClass.compareTo( info[1]._storageClass ) : t ;
	     case 4 :
	        t = compareLong( info[0]._precious , info[1]._precious)  ;
 	        t = t == 0 ? info[0]._poolName.compareTo( info[1]._poolName ) : t ;
	        return  t == 0 ? info[0]._storageClass.compareTo( info[1]._storageClass ) : t ;
	     case 5 :
	        t = compareInt( info[0]._active , info[1]._active)  ;
 	        t = t == 0 ? info[0]._poolName.compareTo( info[1]._poolName ) : t ;
	        return  t == 0 ? info[0]._storageClass.compareTo( info[1]._storageClass ) : t ;
	     case 6 :
	        t = compareInt( info[0]._pending , info[1]._pending)  ;
 	        t = t == 0 ? info[0]._poolName.compareTo( info[1]._poolName ) : t ;
	        return  t == 0 ? info[0]._storageClass.compareTo( info[1]._storageClass ) : t ;
	     case 7 :
	        t = compareInt( info[0]._failed , info[1]._failed)  ;
 	        t = t == 0 ? info[0]._poolName.compareTo( info[1]._poolName ) : t ;
	        return  t == 0 ? info[0]._storageClass.compareTo( info[1]._storageClass ) : t ;
 	     case 8 :
	        return compareDouble(
                            (double)info[0]._precious / (double)info[0]._total  ,
                            (double)info[1]._precious / (double)info[1]._total     ) ;
	     default : return 0 ;
	  }

       }
   }
   private class FlushEntry {

      private String  _poolName     = null ;
      private boolean _isFlushing   = false ;
      private String  _storageClass = null ;
      private long    _total        = 0 ;
      private long    _precious     = 0 ;
      private int     _active       = 0 ;
      private int     _pending      = 0 ;
      private int     _failed       = 0 ;

   }
   /** ----------------------------------------------------------------------------------------------
     *                           Cell Renderers
     */
   private class HighlightEntry {
      private String  _value     = null ;
      private boolean _highlight = false ;
      public HighlightEntry( String value , boolean highlighted ){
         _value     = value ;
         _highlight = highlighted ;
      }
   }
   private class HighlightCellRenderer extends DefaultTableCellRenderer {

       public Component getTableCellRendererComponent(
                            JTable table ,
			    Object value ,
			    boolean isSelected ,
			    boolean isFocused ,
			    int row , int column ){

            if( ( value == null ) || ! ( value instanceof HighlightEntry ) )return  this ;

	   Component component =
	         super.getTableCellRendererComponent(table,value,isSelected,isFocused,row,column);

            HighlightEntry entry = (HighlightEntry)value ;
            JLabel         label = (JLabel)component ;

            if( ! isSelected )label.setBackground( entry._highlight ? Color.orange : Color.white ) ;
            label.setText( entry._value ) ;

            return component ;
       }
   }
   private class GrowthBarCellRenderer extends Component implements TableCellRenderer {

       private double _fraction = 0.0 ;

       public void paint( Graphics g ){
          Rectangle r = g.getClipBounds() ;
          g.setColor( Color.green ) ;
          g.fillRect( r.x , r.y , r.width  , r.height ) ;
          g.setColor( Color.red ) ;
          g.fillRect( r.x , r.y , (int)(r.width * _fraction)  , r.height ) ;
       }
       public Component getTableCellRendererComponent(
                            JTable table ,
			    Object value ,
			    boolean isSelected ,
			    boolean isFocused ,
			    int row , int column ){

            if( value instanceof PoolEntry ){
                PoolEntry entry = (PoolEntry)value ;
                _fraction = (double)entry._precious / (double)entry._total ;
            }else if( value instanceof Double ){
                _fraction = ((Double)value).doubleValue() ;
            }
            return this ;
       }
   }
   public class ByteValueCellRenderer extends DefaultTableCellRenderer {

       private String [] _longText = { "" , "B" , "KB" , "MB" , "GB" , "TB" , "PB" } ;

       private String longToHuman( long in ){
           if( in <= 0L )return "0 B" ;
           long start = 1L ;
           int i = 0 ;
           for( int l = _longText.length ; ( i < l ) && ( ( in / start ) > 0 ) ; i++ , start *= 1024L ) ;
           start /= 1024 ;
           return ""+(in/start)+" "+_longText[i] ;
       }

       public Component getTableCellRendererComponent(
                            JTable table ,
			    Object value ,
			    boolean isSelected ,
			    boolean isFocused ,
			    int row , int column ){
           if( ( value == null ) || ! ( value instanceof Long ) )return this ;
	   Component component =
	         super.getTableCellRendererComponent(table,value,isSelected,isFocused,row,column);

	   ((JLabel)component).setText( longToHuman( ((Long)value).longValue() ) ) ;

           return component ;
      }
   }
   private class PopupActionDescriptor {
       private String         _text     = null ;
       private String         _command  = null ;
       private ActionListener _listener = null ;;
       private PopupActionDescriptor( String text , String actionCommand , ActionListener listener ){
          _text     = text ;
          _command  = actionCommand ;
          _listener = listener ;
       }
   }
   /** ----------------------------------------------------------------------------------------------
     *
     *                           Instances of cell renderes (they are state less)
     */
   /** ----------------------------------------------------------------------------------------------
     *
     *                           Basic Table
     */
   private class HappyTable extends JTable implements Runnable {

      private java.util.List     _list              = null ;
              java.util.List     _entryList         = null ;
      private EntryComparator    _currentComparator = null ;
      private AbstractTableModel _tableModel        = null ;

      DefaultTableCellRenderer _stringRenderer    = new DefaultTableCellRenderer() ;
      DefaultTableCellRenderer _byteValueRenderer = new ByteValueCellRenderer() ;
      DefaultTableCellRenderer _highlightRenderer = new HighlightCellRenderer() ;

      JPopupMenu _popup = null ;

      public HappyTable(){
         _stringRenderer.setHorizontalAlignment( JLabel.CENTER ) ;
         _byteValueRenderer.setHorizontalAlignment( JLabel.CENTER ) ;
         _highlightRenderer.setHorizontalAlignment( JLabel.CENTER ) ;

         getTableHeader().addMouseListener( new HeaderMouseHandler() );

         addMouseListener( new BodyMouseHandler() ) ;

      }
      public void setModel( AbstractTableModel model ){
         _tableModel = model ;
         super.setModel( model ) ;
      }
      public void setComparator( EntryComparator comparator ){
          _currentComparator = comparator ;
      }
      public void setList(  java.util.List list ){
          _list = list ;
          SwingUtilities.invokeLater(  this ) ;
      }
      public void run(){
         if( _entryList != null ){
             if( _entryList.size() > 0 )_tableModel.fireTableRowsDeleted(0,_entryList.size()-1);
         }
         _entryList = _list ;
         if( _entryList != null ){
             if( _entryList.size() > 0 )_tableModel.fireTableRowsInserted(0,_entryList.size()-1);
         }
         _tableModel.fireTableDataChanged() ;
         sort() ;
      }

      private void sort(){
         if( ( _entryList == null ) ||  ( _currentComparator == null ) )return ;
         Collections.sort( _entryList , _currentComparator ) ;
         _tableModel.fireTableDataChanged() ;
      }
      private class HeaderMouseHandler extends MouseAdapter {
         public void mouseClicked( MouseEvent event ){

            int column = getTableHeader().columnAtPoint(event.getPoint()) ;
            _currentComparator.setColumn( column ) ;

            sort() ;

         }
      }
      private class BodyMouseHandler extends MouseAdapter {
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

             }
          }
      }
      public java.util.List getSelectedList(){

         int [] row = getSelectedRows()  ;
         if( ( row == null ) || ( row.length == 0 ) ){
             JOptionPane.showMessageDialog(
                              HappyTable.this,
                              "Nothing selected\nBTW: you can only select items if 'autoupdate' is disabled",
                              "Help Message",
                              JOptionPane.INFORMATION_MESSAGE);
             return null ;
         }
         java.util.List list = new ArrayList() ;
         if( row != null ){
            for( int i = 0 ; i < row.length ; i++ ){
               try{
                    list.add( _entryList.get(row[i]) ) ;
               }catch(Exception e ){

               }
            }
         }
         return list ;
      }
      private void doOnPopupTrigger( MouseEvent event ){
          System.out.println("Popup : "+event);
          if( _popup != null )_popup.show(this,event.getPoint().x,event.getPoint().y);
      }
      void registerPopupActions( PopupActionDescriptor [] actions ){

         _popup = new JPopupMenu("Edit") ;
         _popup.setBorderPainted(true);

         JMenuItem item = _popup.add("Action on selected queue entries") ;

         item.setForeground(Color.red);
         item.setBackground(Color.blue);

         _popup.addSeparator() ;

         for( int i = 0 ; i < actions.length ; i++ ){
            JMenuItem mi = _popup.add( new JMenuItem( actions[i]._text ) ) ; ;
            mi.setActionCommand(actions[i]._command);
            if( actions[i]._listener != null )mi.addActionListener( actions[i]._listener ) ;
         }
      }
      void sayErrorDialog( final String message ){
         SwingUtilities.invokeLater(
             new Runnable(){
                 public void run(){
               JOptionPane.showMessageDialog(JFlushMonitorPanel.this,
                                             message,
                                             "ERROR Message",
                                             JOptionPane.ERROR_MESSAGE);

                 }
             }
         )  ;

      }
   }
   private class FlushTable extends HappyTable {

      private class PopupAction implements ActionListener, DomainConnectionListener {

         public void actionPerformed( ActionEvent event ){
            java.util.List list = getSelectedList() ;

            if( list == null )return ;

            String command = event.getActionCommand() ;

            if( command.equals("flushall") ){

                flushList( list , 0 ) ;

            }else if( command.equals("flush") ){

               String result = JOptionPane.showInputDialog(
                                 FlushTable.this,
                                 "Number of files to flush per StorageClass",
                                 "0");

                try{
                    flushList( list , Integer.parseInt( result ) ) ;
                }catch(Exception ee ){
                    sayErrorDialog("Not a serious number : "+result ) ;
                }
            }
         }
         private void flushList( java.util.List list , int count ){
             for( Iterator i = list.iterator() ; i.hasNext() ; ){
                 flushEntry( (FlushEntry)i.next() , count ) ;
             }
         }
         private void flushEntry( FlushEntry entry , int count ){
            try{
               _connection.sendObject( _controller.getFlushControllerName() ,
                                       "flush pool "+entry._poolName+" "+entry._storageClass +
                                        ( count > 0  ? ( " -count="+count ) : "" ) ,
                                       this ,
                                       6 );
	    }catch(Exception ee ){
	       System.out.println("Exception send "+ee ) ;
	    }

         }
         public void domainAnswerArrived( Object obj , int subid ){
	    System.out.println( "Answer ("+subid+") : "+obj.toString() ) ;
            try{
               if( subid == 6 ){
                  if( ! ( obj instanceof String ) ){
                      sayErrorDialog( obj.toString() ) ;
                  }else{
                      // _controller.askForInfos() ;
                  }
               }

           }catch(Exception ee ){
               ee.printStackTrace();
           }
         }
      }
      private PopupAction _popupAction = new PopupAction() ;
      private PopupActionDescriptor [] _actions = {
          new PopupActionDescriptor( "Flush"     , "flush"     , _popupAction ) ,
          new PopupActionDescriptor( "Flush All" , "flushall"  , _popupAction ) ,

      } ;
      public FlushTable(){

         setModel( new FlushTableModel() );

         setComparator( new FlushEntryComparator() ) ;

         getColumnModel().getColumn(0).setCellRenderer( _stringRenderer ) ;
         getColumnModel().getColumn(1).setCellRenderer( _stringRenderer ) ;
         getColumnModel().getColumn(2).setCellRenderer( _highlightRenderer ) ;
         getColumnModel().getColumn(3).setCellRenderer( _byteValueRenderer ) ;
         getColumnModel().getColumn(4).setCellRenderer( _byteValueRenderer ) ;
         getColumnModel().getColumn(5).setCellRenderer( _stringRenderer ) ;
         getColumnModel().getColumn(6).setCellRenderer( _stringRenderer ) ;
         getColumnModel().getColumn(7).setCellRenderer( _stringRenderer ) ;
         getColumnModel().getColumn(8).setCellRenderer( new GrowthBarCellRenderer() ) ;

         registerPopupActions( _actions ) ;

      }

      public class FlushTableModel extends AbstractTableModel {

         private String [] _title = {
          "PoolName" , "Storage Class" , "State" , "Precious" , "Total", "Active" , "Pending" , "Failed"  ,"Fraction"
         } ;
         public String getColumnName( int index ){
              return _title[index] ;
         }
         public int getRowCount(){
           return _entryList == null ? 0 : _entryList.size() ;
         }
         public int getColumnCount(){
           return _title.length ;
         }
         public Class getColumnClass(int column ){
           switch( column ){
              case 0 : return String.class ;
              case 1 : return String.class ;
              case 2 : return JFlushMonitorPanel.HighlightEntry.class ;
              case 3 : return Long.class ;
              case 4 : return Long.class ;
              case 5 : return String.class ;
              case 6 : return String.class ;
              case 7 : return String.class ;
              case 8 : return Double.class ;
           }
           return String.class ;
         }
         public Object getValueAt( int rowIndex , int columnIndex ){
           FlushEntry entry = (FlushEntry) _entryList.get(rowIndex) ;
           switch( columnIndex ){
              case 0 : return entry._poolName ;
              case 1 : return entry._storageClass ;
              case 2 : return new HighlightEntry( entry._isFlushing ? "Flushing" : "Idle"  , entry._isFlushing  ) ;
              case 3 : return new Long( entry._precious ) ;
              case 4 : return new Long( entry._total ) ;
              case 5 : return ""+entry._active ;
              case 6 : return ""+entry._pending ;
              case 7 : return ""+entry._failed ;
              case 8 : return new Double( (double) entry._precious / (double) entry._total ) ;

           }

           return entry ;
         }
      }
   }
   private class PoolTable extends HappyTable {

      public class PoolTableModel extends AbstractTableModel {
         private String [] _title = {
          "PoolName" , "Pool Mode" ,  "Flushing" , "Total Size" , "Precious Size" , "Fraction"
         } ;
         public String getColumnName( int index ){
              return _title[index] ;
         }
         public int getRowCount(){
           return _entryList == null ? 0 : _entryList.size() ;
         }
         public int getColumnCount(){
           return _title.length ;
         }
         public Class getColumnClass(int column ){
           switch( column ){
              case 0 : return String.class ;
              case 1 : return JFlushMonitorPanel.HighlightEntry.class ;
              case 2 : return JFlushMonitorPanel.HighlightEntry.class  ;
              case 3 : return Long.class ;
              case 4 : return Long.class ;
              case 5 : return Double.class ;
           }
           return String.class ;
         }
         public Object getValueAt( int rowIndex , int columnIndex ){
           PoolEntry entry = (PoolEntry) _entryList.get(rowIndex) ;
           switch( columnIndex ){
              case 0 : return entry._poolName ;
              case 1 : return new HighlightEntry( entry._isReadOnly ? "R-" : "RW" , entry._isReadOnly ) ;
              case 2 : return new HighlightEntry( ""+entry._flushing  , entry._flushing > 0 ) ;
              case 3 : return new Long( entry._total ) ;
              case 4 : return new Long( entry._precious ) ;
              case 5 : return new Double( (double) entry._precious / (double) entry._total ) ;
           }
           
           return entry ;
         }   
      }
      private PopupAction _popupAction = new PopupAction() ;
      private PopupActionDescriptor [] _actions = {
          new PopupActionDescriptor( "Set ReadOnly"  , "rdonly"    , _popupAction ) ,
          new PopupActionDescriptor( "Set ReadWrite" , "readwrite" , _popupAction ) ,
          new PopupActionDescriptor( "Flush All"     , "flushall"  , _popupAction ) ,
      
      } ;
      private class PopupAction implements ActionListener, DomainConnectionListener {
      
         public void actionPerformed( ActionEvent event ){
            java.util.List list = getSelectedList() ;
            
            if( list == null )return ;
            
            String command = event.getActionCommand() ;
            
            if( command.equals("rdonly") ){
                setRdOnly( list , true ) ;
            }else if( command.equals("readwrite") ){
                setRdOnly( list , false ) ;
            }else if( command.equals( "flushall") ){
               JOptionPane.showMessageDialog(JFlushMonitorPanel.this,
                                             "Command not supported. Please select individual Storage Classes",
                                             "ERROR Message",
                                             JOptionPane.ERROR_MESSAGE);
            
            }
         }
         private void setRdOnly( java.util.List list , boolean rdOnly ){
             for( Iterator i = list.iterator() ; i.hasNext() ; ){
                 PoolEntry entry = (PoolEntry)i.next() ;
                 setRdOnly( entry._poolName , rdOnly ) ;
             }
         }
         private void setRdOnly( String poolName , boolean rdOnly ){
            try{
               _connection.sendObject( _controller.getFlushControllerName() ,
                                       "set pool "+poolName+( rdOnly ? " rdonly" : " rw" ) ,
                                       this ,
                                       5 );
	    }catch(Exception ee ){
	       System.out.println("Exception send "+ee ) ;
	    }

         }
         public void domainAnswerArrived( Object obj , int subid ){
	    System.out.println( "Answer ("+subid+") : "+obj.toString() ) ;
            try{
               if( subid == 5 ){
                  if( ! ( obj instanceof String ) ){
                      sayErrorDialog( obj.toString() ) ;
                  }else{
                      _controller.askForInfos() ;
                  }
               }

           }catch(Exception ee ){
               ee.printStackTrace();
           }
         }
      }
      
      public PoolTable(){
      
         setModel( new PoolTableModel() );    
         setComparator( new PoolEntryComparator() ) ; 
         
         getColumnModel().getColumn(0).setCellRenderer( _stringRenderer ) ;
         getColumnModel().getColumn(1).setCellRenderer( _highlightRenderer ) ;
         getColumnModel().getColumn(2).setCellRenderer( _highlightRenderer ) ;
         getColumnModel().getColumn(3).setCellRenderer( _byteValueRenderer ) ;
         getColumnModel().getColumn(4).setCellRenderer( _byteValueRenderer ) ;
         getColumnModel().getColumn(5).setCellRenderer( new GrowthBarCellRenderer() ) ;
          
         registerPopupActions( _actions ) ;          
      }
   
   }
   void preparePoolList( java.util.List list ){

      ArrayList pools  = new ArrayList() ;
      ArrayList flushs = new ArrayList() ;

      for( Iterator i = list.iterator() ; i.hasNext() ; ){

          HsmFlushControlCore.PoolDetails pool = (HsmFlushControlCore.PoolDetails)i.next() ;

          String       poolName   = pool.getName() ;
          boolean      isReadOnly = pool.isReadOnly() ;
          PoolCellInfo cellInfo   = pool.getCellInfo() ;
          if( cellInfo == null )continue ;
          PoolCostInfo costInfo   = cellInfo.getPoolCostInfo() ;
          if( costInfo == null )continue ;

          PoolCostInfo.PoolSpaceInfo spaceInfo = costInfo.getSpaceInfo() ;
          PoolCostInfo.PoolQueueInfo queueInfo = costInfo.getStoreQueue() ;

          long totalSpace    = spaceInfo.getTotalSpace() ;
          long preciousSpace = spaceInfo.getPreciousSpace() ;

          PoolEntry pentry = new PoolEntry() ;
          pentry._poolName     = pool.getName() ;
          pentry._total        = totalSpace ;
          pentry._precious     = preciousSpace ;
          pentry._isReadOnly   = isReadOnly ; 
          pentry._flushing     = 0 ; 

          pools.add( pentry ) ;

          java.util.List flushes = pool.getFlushInfos() ;
          if( ( flushes == null ) || ( flushes.size() == 0 ) )continue ;

          for( Iterator j = flushes.iterator() ; j.hasNext() ; ){

             HsmFlushControlCore.FlushInfoDetails flush = (HsmFlushControlCore.FlushInfoDetails)j.next() ;
             StorageClassFlushInfo info = flush.getStorageClassFlushInfo() ;

             FlushEntry fentry = new FlushEntry() ;
             fentry._poolName     = pool.getName() ;
             fentry._storageClass = flush.getName() ;
             fentry._isFlushing   = flush.isFlushing() ;
             fentry._total        = totalSpace ;
             fentry._precious     = info.getTotalPendingFileSize() ;
             fentry._pending      = info.getRequestCount() ;
             fentry._active       = info.getActiveCount() ;
             fentry._failed       = info.getFailedRequestCount() ;

             if( fentry._isFlushing )pentry._flushing ++ ;

             flushs.add( fentry ) ;
          }
      }
      _poolTable.setList( pools ) ;
      _flushTable.setList( flushs ) ;
   }
   
   
}
