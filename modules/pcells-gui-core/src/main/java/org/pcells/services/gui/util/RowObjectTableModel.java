
package org.pcells.services.gui.util ;


import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class RowObjectTableModel extends AbstractTableModel implements MouseListener, Comparator {
   
    public interface SimpleTableRow {
        public Object getValueAtColumn( int column );
        public Component renderCell(Component component , Object value , boolean isSelected , boolean isFocussed , int row , int column ) ;
    }

    public class Renderer extends DefaultTableCellRenderer {
          public Component getTableCellRendererComponent(
                            JTable table ,
                            Object value ,
                            boolean isSelected ,
                            boolean isFocussed ,
                            int row , int column ){
//              System.out.println("getTableCellRendererComponent : "+row+" "+column+" "+value.getClass().getName()+" value : "+value);
              Component component =
                 super.getTableCellRendererComponent(table,value,isSelected,isFocussed,row,column);

              if( ( component == null ) || ( value == null ) || ( row < 0 ) )return component ;
              JLabel label = (JLabel)component;
             
              SimpleTableRow rowObject = (SimpleTableRow)_list.get(row) ;
              
              return rowObject.renderCell( component , value , isSelected , isFocussed , row , column ) ;

            //    label.setFont(_font) ;
            //    if( ! isSelected )label.setBackground( row % 2 == 0 ? Color.white : _myGray ) ;
            //    label.setHorizontalAlignment( JLabel.CENTER);
            //    label.setText(restoreHandlerInfoToString(info,column));
          }
       
    }
       
       private String [] _titles = null ;
       private ArrayList _list   = new ArrayList() ;
       private Renderer  _renderer = new Renderer() ;
       
       public TableCellRenderer getRenderer(){ return _renderer ; }
       public TableModel getModel(){ return this ; }
       public MouseListener getHeadMouseListener(){ return this ; }
       
       public RowObjectTableModel( String [] titles ){
          _titles = titles ;
       }
       public Object getValueAt( int row , int column ){
           SimpleTableRow rowObject = (SimpleTableRow)_list.get(row) ;
           return rowObject.getValueAtColumn(column) ;
       }
       public int getRowCount(){ return _list.size() ; }
       public int getColumnCount(){ return _titles.length ; }
       public String getColumnName( int pos ){
           return pos <= _titles.length ? _titles[pos] : "" ;
       }
       public Object getRowAt( int pos ){
          return pos <= _list.size() ? _list.get(pos) : null ;
       }
       public void add( Object obj ){ 
           _list.add(obj) ; 
       }
       public void clear(){ 
          int size = _list.size() ;
          _list.clear() ; 
          if( size > 0 )fireTableRowsDeleted(0,size-1);
       }
       public void fire(){ 
           int size = _list.size() ;
           if( size > 0 )fireTableRowsInserted(0,size-1) ; 
       }
       private int     _currentCompareColumn    = -1 ;
       private boolean _currentCompareDirection = true ;
       public int compare( Object o1 , Object o2 ){
       
          Comparable x1 = (Comparable)((SimpleTableRow)o1).getValueAtColumn(_currentCompareColumn) ;
          Comparable x2 = (Comparable)((SimpleTableRow)o2).getValueAtColumn(_currentCompareColumn) ;
          
          System.out.println("Comparing : "+x1+" <-> "+x2 ) ;
          if( _currentCompareDirection )
             return x1.compareTo( x2 );
          else
             return x2.compareTo( x1 ) ;
       }

       public void mouseClicked( MouseEvent event ){
            //System.err.println("Source of mouse event : "+event.getSource() ) ;
            // if( ! _canBeSorted )return ;
            JTableHeader header = (JTableHeader)event.getSource() ;
            int column = header.columnAtPoint(event.getPoint()) ;
            
            if( _currentCompareColumn != column ){
              _currentCompareDirection = false ;
            }else{
              _currentCompareDirection = ! _currentCompareDirection ;
            }
            _currentCompareColumn = column ;
            Collections.sort( _list , this ) ;
            fireTableDataChanged() ;
            
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
