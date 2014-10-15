// $Id: PnfsManagerPanel.java,v 1.1 2008/11/09 08:23:58 cvs Exp $
//
package org.dcache.gui.pluggins.pnfs;
//

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;


public class PnfsDetailPanel extends JPanel {

   private OurDataModel _detailDataModel = new OurDataModel() ;
   private JTable       _detailTable     = null ;

   public PnfsDetailPanel(){

      setLayout( new BorderLayout(4,4) ) ;

      _detailTable = new JTable( _detailDataModel ) ;
      _detailTable.setDefaultRenderer( Object.class ,  new Renderer() ) ;

        add( new JScrollPane( _detailTable ) , "Center" ) ;
   }
   public void setMap( Map map ){
      _detailDataModel.setMap( map ) ;
   }
   private void displayTimeDiff( long diff ){
   }
   private class OurDataModel extends AbstractTableModel {
      private long  [][]  _counters  = null;
      private float [][]  _rates     = null;
      private String []   _labels     = new String[0] ;
      private Map         _previousMap     = null ;
      private long        _lastTime        = 0L ;

      public void setMap( Map map ){

         long now      = System.currentTimeMillis() ;
         long timeDiff = _lastTime == 0L ? 0L : now - _lastTime ;

         Map result = _previousMap == null ? map : PnfsManagerInfo.calculateDetailDiff(_previousMap, map) ;

         _previousMap = map ;
         _lastTime    = now ;

         if( timeDiff == 0L )return ;

         _labels   = new String[map.size()] ;
         _counters = new long[map.size()][];
         _rates    = new float[map.size()][];

          Iterator iter = new TreeMap( map ).entrySet().iterator() ;
          for( int i = 0 ; iter.hasNext() ; i++ ){
              Map.Entry e = (Map.Entry)iter.next() ;
              _labels[i]   = (String)e.getKey() ;
              _counters[i] = (long [])e.getValue() ;

              long [] diffs = (long [])result.get(_labels[i]) ;              
              if( diffs == null )continue ;
 
              _rates[i] = new float[2] ; 

              _rates[i][0] = (float)diffs[0] / (float)timeDiff * (float) 1000.0 ; 
              _rates[i][1] = (float)diffs[1] / (float)timeDiff * (float) 1000.0 ; 
          }
          fireTableDataChanged() ;
      }
      public String getColumnName( int i ){
        switch(i){
           case 0 : return "Pnfs Action" ;
           case 1 : return "Requests/Sec" ;
           case 2 : return "Errors/Sec" ;
           case 3 : return "Requests" ;
           case 4 : return "Errors" ;
           default : return "Unknown" ;
        }
      }
      public int getColumnCount() { return 5 ; }
      public int getRowCount() { return _labels.length;}
      public Object getValueAt(int row, int col) {
        if( col == 0 ){
           return _labels[row] ;
        }else if( col < 3 ){
           if( _rates[row] == null )return "?";
           return _rates[row][col-1];
        }else{
           if( _counters[row] == null )return "?";
           return _counters[row][col-3];
        }
      }

   }


   private class Renderer extends DefaultTableCellRenderer {

          private Color _gray = new Color( 0xcc , 0xcc , 0xcc ) ;

          public Component getTableCellRendererComponent(
                            JTable table ,
                            Object value ,
                            boolean isSelected ,
                            boolean isFocussed ,
                            int row , int column ){

              Component component =
                 super.getTableCellRendererComponent(table,value,isSelected,isFocussed,row,column);

              if( ( component == null ) || ( value == null ) || ( row < 0 ) )return component ;

              JLabel label = (JLabel)component;

              label.setHorizontalAlignment( column == 0 ? JLabel.LEFT : JLabel.RIGHT ) ;
              label.setBackground( row % 2 == 0 ? Color.white : _gray ) ;

              return component ;
          }


    }


}

