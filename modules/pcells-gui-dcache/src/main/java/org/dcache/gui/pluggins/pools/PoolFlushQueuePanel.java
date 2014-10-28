package org.dcache.gui.pluggins.pools ;

import org.pcells.services.gui.* ;
import org.pcells.services.gui.util.* ;

import diskCacheV111.pools.* ;

import java.util.*;

import java.awt.*;
import java.text.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.table.*;

import diskCacheV111.vehicles.JobInfo ;
import diskCacheV111.vehicles.IoJobInfo ;

import org.pcells.services.connection.DomainConnection ;
import org.pcells.services.connection.DomainConnectionListener ;
import org.pcells.services.connection.DomainEventListener ;
import org.pcells.services.gui.* ;

public class PoolFlushQueuePanel extends JPanel implements ListSelectionListener {
 
   private SimpleDateFormat   _formatter  = new SimpleDateFormat ("MM.dd HH:mm:ss");
 
   public class FlushQueueListModel extends RowObjectTableModel {
       public FlushQueueListModel(  ){
          super(new String[]{ "Hsm" , "Storage Class" , 
                              "Requests" , "Active" , "Failed" , "Error Count" ,
                              "Last Submitted"});
       }   
       public FlushQueueEntryInfo getQueueInfoAt( int pos ){
          return (FlushQueueEntryInfo)getRowAt(pos) ;
       }
   }
   public class FlushQueueEntryInfo implements RowObjectTableModel.SimpleTableRow {
       private Object [] _values = null ;
       private FlushQueueEntryInfo( Object [] info ){
          _values = new Object[info.length] ; 
          _values[0] = info[0] ;  // hsm
          _values[1] = info[1] ;  // class
          _values[2] = info[3] ;  // requests
          _values[3] = info[5] ;  // active
          _values[4] = info[4] ;  // failed
          _values[5] = info[6] ;  // errors
          _values[6] = info[2] ;  // since
       }
       public Component renderCell(Component component , Object value , boolean isSelected , 
                                   boolean isFocussed , int row , int column ){
          
          JLabel label = (JLabel)component ;
          
          switch( column ){
          
             case 0 :  // HSM
             case 1 :  // Storage Class
                 label.setHorizontalAlignment( JLabel.CENTER ) ;
             
             break ;

             case 2 :  // Pending Since
             case 3 :  // active
             case 4 :  // failed
             case 5 :  // errors
                 long x = ((Long)value).longValue() ;
                 label.setText( ""+x) ;
                 label.setHorizontalAlignment( JLabel.RIGHT ) ;
             break ;
             case 6 : 
                 label.setText( toTimeDifference( (Long)value ) ) ;
                 label.setHorizontalAlignment( JLabel.RIGHT ) ;
             break ;
          }          
          return component ;                           
       }
       public String toString(){ return _values[0]+"@"+_values[1] ; }
       public Object getValueAtColumn( int column ){
           return column < _values.length ? _values[column] : null ;
       }
   }
   public class FlushClassQueueListModel extends RowObjectTableModel {
       public FlushClassQueueListModel(  ){
          super(new String[]{ "Hsm" , "Storage Class" ,
                              "Requests" , "Active" , "Failed" ,
                              "Expire Time" , 
                              "Total Pending Size" , "Max Pending" , 
                              "Max Allowed" ,
                              "Last Submitted" ,
                              "Oldest Timestamp",
                              "FlushId" });
       }   
       public FlushClassQueueListModel getQueueInfoAt( int pos ){
          return (FlushClassQueueListModel)getRowAt(pos) ;
       }
   }
   public class FlushClassQueueEntryInfo implements RowObjectTableModel.SimpleTableRow {
   
       private Object []             _values = null ;
       private StorageClassFlushInfo _info   = null ;
       
       private FlushClassQueueEntryInfo( StorageClassFlushInfo info ){
          _info   = info ;
          _values = new Object[12] ; 
          _values[0] = info.getHsm() ;  // hsm
          _values[1] = info.getStorageClass() ;  // class
          _values[2] = new Long( info.getRequestCount() );  // requests
          _values[3] = new Long( info.getActiveCount() );  // active
          _values[4] = new Long( info.getFailedRequestCount() );  // failed
          _values[5] = new Long( info.getExpirationTime() );  // errors
          _values[6] = new Long( info.getTotalPendingFileSize() );  // errors
          _values[7] = new Long( info.getMaximumPendingFileCount() );  // errors
          _values[8] = new Long( info.getMaximumAllowedPendingFileSize() );  // errors
          _values[9] = new Long( info.getLastSubmittedTime() );  // errors
          _values[10] = new Long( info.getLastSubmittedTime() );  // errors
          _values[11] = new Long( info.getFlushId() );  // since
       }
       public Component renderCell(Component component , Object value , boolean isSelected , 
                                   boolean isFocussed , int row , int column ){
          
          JLabel label = (JLabel)component ;
          
          switch( column ){
          
             case 0 :  // HSM
             case 1 :  // Storage Class
                 label.setHorizontalAlignment( JLabel.CENTER ) ;
             
             break ;

             case 9 : 
             case 10 : 
                 label.setText( toTimeDifference( (Long)value ) ) ;
                 label.setHorizontalAlignment( JLabel.RIGHT ) ;
             break ;
             case 5 :
                 label.setHorizontalAlignment( JLabel.CENTER ) ;
                 label.setText( _formatter.format( new Date(((Long)value).longValue() ) ) ) ;
             break ;
             default :
                 long x = ((Long)value).longValue() ;
                 label.setText( ""+x) ;
                 label.setHorizontalAlignment( JLabel.RIGHT ) ;
             
          }          
          return component ;                           
       }
       public String toString(){ return _values[0]+"@"+_values[1] ; }
       public Object getValueAtColumn( int column ){
           return column < _values.length ? _values[column] : null ;
       }
   }
   public String toTimeDifference( Long value ){
      long x = ((Long)value).longValue() / 1000L ;
      String seconds = "" + ( x % 60 ) ;  //seconds
      if( seconds.length() == 1 )seconds = "0"+seconds;
      x = x / 60 ;
      String minutes = "" + ( x % 60 ) ;  //minutes
      if( minutes.length() == 1 )minutes = "0"+minutes;
      x = x / 60 ;
      String hours = "" + ( x % 24 ) ;  //hours
      x = x / 24 ;

      String days = ( x > 0 ) ? ( ""+x+" days" ) : "" ;

      return days +" "+hours+":"+minutes+":"+seconds ;
   }

   private FlushQueueListModel   _model      = new FlushQueueListModel(  ) ;
   private DomainConnection _connection = null ;
   private JTable           _table      = null ;
   
   public PoolFlushQueuePanel(DomainConnection connection , java.util.prefs.Preferences pref){
   
       setLayout( new BorderLayout(10,10) ) ;
      
       setBorder( BorderFactory.createTitledBorder( BorderFactory.createLineBorder( Color.blue , 1 ) , "Flush Queue" ) ) ;
       
       _table = new JTable() ;
       _table.setSelectionMode( ListSelectionModel.SINGLE_SELECTION);
       _table.setModel( _model ) ;
       _table.getTableHeader().addMouseListener(_model);
       
       TableColumnModel columnModel = _table.getColumnModel() ;
       for( int i= 0 , n = columnModel.getColumnCount() ; i < n ; i++ ){
           columnModel.getColumn(i).setCellRenderer(_model.getRenderer());
       }

       
       ListSelectionModel selectionModel = _table.getSelectionModel() ;
       
       selectionModel.addListSelectionListener(this) ;
       JScrollPane scroll = new JScrollPane( _table ) ;
       
       scroll.setPreferredSize( new Dimension(0,10) ) ;
       add( scroll , "Center");


    }
    private void addQueueInfo( FlushQueueEntryInfo info ){
       _model.add(info);
    }
    private void addExtendedQueueInfo( FlushClassQueueEntryInfo info ){
    }
    public void setQueueInfo( Object [] flushQueueInfo ){
    
        _model.clear() ;
        if( flushQueueInfo == null )return ;
        
        for( int i= 0 ; i < flushQueueInfo.length ; i++ ){
            addQueueInfo( new FlushQueueEntryInfo( (Object [])flushQueueInfo[i]) ) ;
        }
        _model.fire();
    }
    public void setExtendedQueueInfo( StorageClassFlushInfo [] flushInfo ){
        _model.clear() ;
        if( flushInfo == null )return ;
        for( int i = 0 ; i < flushInfo.length ; i++ ){
            //System.err.println(" FLUSH : "+i+" : "+flushInfo[i]);
            addExtendedQueueInfo( new FlushClassQueueEntryInfo( flushInfo[i] ) ) ;
        }
        _model.fire();
    } 
    public void valueChanged( ListSelectionEvent event ){
    }

}
