// $Id: PoolQueueListPanel.java,v 1.4 2007/02/18 07:50:24 cvs Exp $

package org.dcache.gui.pluggins.pools ;

import org.pcells.services.gui.util.RowObjectTableModel;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumnModel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.text.SimpleDateFormat;
import java.util.Date;


public class PoolQueueListPanel extends JPanel implements ListSelectionListener {
 
   private SimpleDateFormat   _formatter  = new SimpleDateFormat ("MM.dd HH:mm:ss");
 
   public class QueueListModel extends RowObjectTableModel {
       public QueueListModel(  ){
          super(new String[]{ "Job ID" , "Client Name" , "Client ID" , 
                              "Start Time" , "Submit Time" , "Status" , "Transfer Time" , "Bytes" , "Last Accessed" });
       }   
       public QueueListModel getQueueInfoAt( int pos ){
          return (QueueListModel)getRowAt(pos) ;
       }
   }
   public class QueueEntryInfo implements RowObjectTableModel.SimpleTableRow {
       private Object    [] _values = null ;
       private GuiJobInfo   _info   = null ;
       private QueueEntryInfo( GuiJobInfo info ){
          _info = info ;
          init() ;
       }
       private void init(){
          _values = new Object[9] ;
          _values[0] = new Long( _info.getJobId() ) ;
          _values[1] = _info.getClientName();
          _values[2] = new Long( _info.getClientId() ) ;
          _values[3] = new Long( _info.getStartTime() ) ;
          _values[4] = new Long( _info.getSubmitTime() ) ;
          _values[5] = _info.getStatus()  ;
          if( _info.isIoJobInfo() ){
             _values[6] = new Long( _info.getTransferTime() )  ;
             _values[7] = new Long( _info.getBytesTransferred() )  ;
             _values[8] = new Long( System.currentTimeMillis() - _info.getLastTransferred() )  ;
          }else{
             _values[6] = ""  ;
             _values[7] = ""  ;
             _values[8] = ""  ;
          }
       }
       public Component renderCell(Component component , Object value , boolean isSelected , 
                                   boolean isFocussed , int row , int column ){
          
          JLabel label = (JLabel)component ;
          
          if( _values[5].toString().equals("W") ){
             label.setForeground( Color.red ) ;
          }else{
             label.setForeground( Color.black ) ;
          }
 
          
          label.setHorizontalAlignment( JLabel.CENTER ) ;
          switch( column ){
          
             case 0 : 
             
             break ;
             case 1 : 
             
             break ;
             case 2 : 
             
             break ;
             case 3 : 
             case 4 : 
                 label.setHorizontalAlignment( JLabel.CENTER ) ;
                 long st = ((Long)value).longValue() ;
                 label.setText( st == 0L ? "-" : _formatter.format( new Date( st ) ) ) ;
             break ;
             case 8 : 
                 label.setHorizontalAlignment( JLabel.CENTER ) ;
                 if( value instanceof java.lang.Long ){
                    label.setText(  value.toString() ) ;
                 }else{
                    label.setText("-");
                 }
             break ;
             case 5 : 
             
             break ;
             case 6 : 
             
             break ;
             case 7 : 
                 if( value instanceof java.lang.Long ){
                    label.setHorizontalAlignment( JLabel.RIGHT ) ;
                    label.setText( longToByteString( (Long)value )  ) ;
                 }else{
                    label.setHorizontalAlignment( JLabel.CENTER ) ;
                    label.setText("-");
                 }
             
             break ;
          }          
          return component ;                           
       }
       public GuiJobInfo getJobInfo(){ return _info ; }
       public String toString(){ return _info.toString() ; }
       public Object getValueAtColumn( int column ){
           return column < _values.length ? _values[column] : null ;
       }
       public String toTimeDifference( Long value ){
          long x = Math.abs(((Long)value).longValue());
          boolean neg = x < 0L ;
          x = x / 1000L ;
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
       private String longToByteString( Long value ){
          String b = value.toString() ;
          StringBuffer sb = new StringBuffer() ;
          int count = 0 ;
          for( int i = b.length()  - 1 ; i >= 0 ; i-- ){
             char c = b.charAt(i) ;
             if( ( count > 0 ) && ( count % 3 ) > 0 )sb.append('.');
             sb.append( c ) ;
          }
          return sb.reverse().toString();
       }
   }
   private JTable          _table  = null ;
   private QueueListModel  _model  = new QueueListModel(  ) ;
   
   public PoolQueueListPanel(){
   
       setLayout( new BorderLayout(10,10) ) ;
      
       setBorder( BorderFactory.createTitledBorder( BorderFactory.createLineBorder( Color.blue , 1 ) , "Queue List" ) ) ;
       
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
    private void addQueueInfo( QueueEntryInfo info ){
       _model.add(info);
     //  _model.fire() ;
    }
    public void setQueueInfo( GuiJobInfo [] jobInfo ){
    
        _model.clear() ;
        if( jobInfo == null )return ;
        
        for( int i= 0 ; i < jobInfo.length ; i++ ){
            addQueueInfo( new QueueEntryInfo(jobInfo[i]) ) ;
        }
        _model.fire();
    }
    public void valueChanged( ListSelectionEvent event ){
    }

}
