// $Id: SpaceReservationTable.java,v 1.4 2008/07/06 21:28:20 cvs Exp $
//
package org.dcache.gui.pluggins.poolManager;
//

import diskCacheV111.vehicles.IoJobInfo;
import org.pcells.services.connection.DomainConnection;
import org.pcells.services.gui.util.RowObjectTableModel;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;


public class SpaceReservationTable extends JTable {
   
   private SpaceReservationModel _model     = null ;
   private SimpleDateFormat      _formatter = new SimpleDateFormat ("MM.dd HH:mm:ss");
   private Color                 _veryLightGray = new Color( 0xdd , 0xdd , 0xdd ) ;
   public class SpaceReservationModel extends RowObjectTableModel {
       public SpaceReservationModel(  ){
          super(new String[]{ "ID" , 
	                      "Description",
                              "LinkGroupName" , 
			      "Storage" ,
			      "VoGroup(Role)" , 
			      "State" ,
			      "Size",
			      "Used",
			      "Allocated",
			      "Created",
			      "Lifetime",
			      "Expiration" }
	       );
       }   
       public SpaceReservation getQueueInfoAt( int pos ){
          return (SpaceReservation)getRowAt(pos) ;
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

            Component component =
                super.getTableCellRendererComponent(table,value,isSelected,isFocused,row,column);

            JLabel label = (JLabel)component;

            if( ! isSelected )label.setBackground( row % 2 == 0 ? Color.white : _veryLightGray ) ;
            label.setHorizontalAlignment( JLabel.CENTER);

            switch( column ){
               case 7 :
               case 8 :
               case 6 :
                   label.setHorizontalAlignment( JLabel.RIGHT);
                   label.setText( longToByteString( (Long)value ) ) ;
	       break ;
               case 9 :
	       case 11 :
                   label.setText( _formatter.format( new Date(((Long)value).longValue() )) ) ;
               break ;
               case 10 :
                   label.setText( toTimeDifference( ((Long)value).longValue() / 1000L ) ) ;
                   label.setHorizontalAlignment( JLabel.RIGHT);
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

  public class SpaceReservationRow implements RowObjectTableModel.SimpleTableRow {
       private Object []       _values = null ;
       private SpaceReservation _info   = null ;
       private SpaceReservationRow( SpaceReservation info ){
          _info     = info ;
          init() ;
       }
       private void init(){
          _values = new Object[12] ;
          _values[0] = new Long( _info.getId() ) ;
          _values[1] = _info.getTokenDescription() ;
          _values[2] = _info.getLinkGroupName() ;
          _values[3] = _info.getStorageAttributes() ;
	  String t = _info.getVoRole();
          _values[4] = _info.getVoGroup()+( (t==null)||(t.equals("null")) ? "" : ( "("+t+")" ) );
          _values[5] = _info.getState().substring(0,1);
          _values[6] = new Long( _info.getSize() ) ;
          _values[7] = new Long( _info.getUsed() ) ;
          _values[8] = new Long( _info.getAllocated() ) ;
          _values[9] = new Long( _info.getCreated() ) ;
          _values[10] = new Long( _info.getLifetime() ) ;
          _values[11] = new Long( _info.getExpiration() ) ;

       }
       public String toString(){ return _info.toString() ; }
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
	    case 6 : break ;
          }
         // label.setText(restoreHandlerInfoToString(info,column));

          return component ;
       }

   }

   public SpaceReservationTable( DomainConnection connection ){

       setModel( _model = new SpaceReservationModel() ) ;

       JTableHeader header = getTableHeader() ;
       header.addMouseListener(_model);

       setDefaultRenderer( Object.class , new MyRenderer() );

  //     addMouseListener( new PopupTrigger() ) ;

  //     createPopup() ;

   }
   public void setSpaceReservationList( SpaceReservation[] list ){
      _model.clear() ;
      for( int i = 0 ; i < list.length ; i++ ){
         _model.add( new SpaceReservationRow( list[i] )  ) ;
      }
      _model.fire() ;
   }
   public void setSpaceReservationList( java.util.List list ){
      _model.clear() ;
      for( Iterator it = list.iterator() ; it.hasNext() ; ){
         SpaceReservation sr = (SpaceReservation)it.next() ;
	 _model.add( new SpaceReservationRow(sr) ) ;	 
      }
      _model.fire() ;
   }
}

