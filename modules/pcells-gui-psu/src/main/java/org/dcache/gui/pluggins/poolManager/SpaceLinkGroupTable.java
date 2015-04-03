// $Id: SpaceLinkGroupTable.java,v 1.4 2008/07/06 21:28:20 cvs Exp $
//
package org.dcache.gui.pluggins.poolManager ;
//
import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.util.*;
import java.text.*;
import java.io.* ;
import org.pcells.services.connection.DomainConnection ;
import org.pcells.services.connection.DomainConnectionListener ;
import org.pcells.services.connection.DomainEventListener ;
import dmg.cells.network.CellDomainNode ;
import dmg.cells.nucleus.CellTunnelInfo ;
import dmg.cells.nucleus.CellDomainInfo ;   
import dmg.cells.nucleus.CellInfo ;   
import diskCacheV111.vehicles.RestoreHandlerInfo ;
import diskCacheV111.vehicles.IoJobInfo ;
import diskCacheV111.util.PnfsId ;
import org.pcells.services.gui.* ;
import org.dcache.gui.pluggins.*;
import org.pcells.services.gui.util.* ;


public class SpaceLinkGroupTable extends JTable {
   
      private SpaceLinkGroupModel _model = null ;
      private Color               _veryLightGray = new Color( 0xdd , 0xdd , 0xdd ) ;

      public class SpaceLinkGroupModel extends RowObjectTableModel {
          public SpaceLinkGroupModel(  ){
             super(new String[]{ "Name" , 
	                         "ID" , 
                                 "Allowed" , 
				 "VOs" ,
				 "Available" ,
				 "Reserved" , 
                                 "Free" ,
                                 "Total" }
	          );
          }   
          public SpaceLinkGroupRow getSpaceLinkGroupRowAt( int pos ){
             return (SpaceLinkGroupRow)getRowAt(pos) ;
          }
      }
      public SpaceLinkGroup getSpaceLinkGroupAt( int pos ){
         SpaceLinkGroupRow  row = _model.getSpaceLinkGroupRowAt(pos);
         return row.getSpaceLinkGroupAt(pos) ;
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
                  case 0 :
                  case 1 :
                  case 2 :
                  case 3 :
                      //label.setHorizontalAlignment( JLabel.LEFT);
                  break ;
                  case 4 :
                  case 5 :
                  case 6 :
                  case 7 :
                     label.setHorizontalAlignment( JLabel.RIGHT);
		     label.setText( longToByteString( (Long)value ) ) ;
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
      
     public class SpaceLinkGroupRow implements RowObjectTableModel.SimpleTableRow {
          private Object []       _values = null ;
          private SpaceLinkGroup  _info   = null ;
          private SpaceLinkGroupRow( SpaceLinkGroup info ){
             _info     = info ;
             init() ;
          }
	  public SpaceLinkGroup getSpaceLinkGroupAt( int pos ){
	    return _info ;
	  }
          private void init(){
             _values = new Object[8] ;
             _values[0] = _info.getLinkGroupName() ;
             _values[1] = new Long( _info.getLinkGroupId() ) ;
             _values[2] = _info.getAllowedString() ;
             _values[3] = _info.getVOs() ;
             _values[4] = new Long( _info.getAvailable() );
             _values[5] = new Long( _info.getReserved() ) ;
             _values[6] = new Long( _info.getFree() ) ;
             _values[7] = new Long( _info.getTotal() ) ;
             
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
                case 6 :
                   // label.setText( _formatter.format( new Date(((Long)value).longValue() )) ) ;
                break ;
             }
            // label.setText(restoreHandlerInfoToString(info,column));
            
             return component ;
          }

      }

      public SpaceLinkGroupTable( DomainConnection connection ){
  
          setModel( _model = new SpaceLinkGroupModel() ) ;
  
          JTableHeader header = getTableHeader() ;
          header.addMouseListener(_model);
  
          setDefaultRenderer( java.lang.Object.class , new MyRenderer() );
  
     //     addMouseListener( new PopupTrigger() ) ;
          
     //     createPopup() ;

      }
      public void setSpaceLinkGroupList( SpaceLinkGroup [] list ){
         _model.clear() ;
         for( int i = 0 ; i < list.length ; i++ ){
            _model.add( new SpaceLinkGroupRow( list[i] )  ) ;
         }
         _model.fire() ;
      }
      public void setSpaceLinkGroupMap( Map map ){
         _model.clear() ;
         for( Iterator it = map.values().iterator() ; it.hasNext() ; ){
            _model.add( new SpaceLinkGroupRow( (SpaceLinkGroup) it.next() )  ) ;
         }
         _model.fire() ;
      }
}

