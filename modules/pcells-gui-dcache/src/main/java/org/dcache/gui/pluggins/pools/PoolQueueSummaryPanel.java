package org.dcache.gui.pluggins.pools ;

import diskCacheV111.pools.PoolCostInfo;
import org.pcells.services.gui.util.RowObjectTableModel;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.JTableHeader;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.Iterator;
import java.util.Map;

public class PoolQueueSummaryPanel extends JComponent implements ListSelectionListener {

   private JTable             _table          = new JTable() ;
   private QueueSummaryModel  _model          = new QueueSummaryModel(  ) ;

   public class QueueSummaryModel extends RowObjectTableModel {
       public QueueSummaryModel(  ){
          super(new String[]{ "Name" , "Active" , "Limit" , "Waiting" });
       }   
       public QueueInfo getQueueInfoAt( int pos ){
          return (QueueInfo)getRowAt(pos) ;
       }
   }
   public class QueueInfo implements RowObjectTableModel.SimpleTableRow {
       private String    _name   = null ;
       private Object [] _values = null ;
       private PoolCostInfo.PoolQueueInfo _info = null ;
       private QueueInfo( String name , PoolCostInfo.PoolQueueInfo info ){
          _name = name ;
          _info = info ;
          init() ;
       }
       private QueueInfo( PoolCostInfo.NamedPoolQueueInfo info ){
          _name = info.getName() ;
          _info = info ;
          init() ;
       }
       private void init(){
          _values = new Object[4] ;
          _values[0] = _name ;
          _values[1] = new Integer( _info.getActive() ) ;
          _values[2] = new Integer( _info.getMaxActive() ) ;
          _values[3] = new Integer( _info.getQueued() ) ;
       }
       public String getName(){ return _name ; }
       public PoolCostInfo.PoolQueueInfo getPoolQueueInfo(){ return _info ; }
       public String toString(){ return _name +"="+_info ; }
       public Object getValueAtColumn( int column ){
           return column < _values.length ? _values[column] : null ;
       }
       public Component renderCell(Component component , Object value , boolean isSelected , 
                                   boolean isFocussed , int row , int column ){
          return component ;                           
       }

   }
   public PoolQueueSummaryPanel(){
   
  
       setLayout( new BorderLayout() ) ;
      
       setBorder( BorderFactory.createTitledBorder( BorderFactory.createLineBorder( Color.blue , 1 ) , "Queue Summary" ) ) ;
       
       _table.setSelectionMode( ListSelectionModel.SINGLE_SELECTION);
       _table.setModel( _model ) ;
       JTableHeader header = _table.getTableHeader() ;
       header.addMouseListener(_model);
       //header.setDefaultRenderer(_model.getRenderer());

       
       ListSelectionModel selectionModel = _table.getSelectionModel() ;
       
       selectionModel.addListSelectionListener(this) ;
       JScrollPane scroll = new JScrollPane( _table ) ;
       
       scroll.setPreferredSize( new Dimension(0,10) ) ;
       add( scroll , "Center");

    }
    public QueueInfo getQueueInfoAt( int pos ){
        return ((QueueSummaryModel)_model).getQueueInfoAt(pos) ;
    }
    public ListSelectionModel getSelectionModel(){ return _table.getSelectionModel() ; }
    public void valueChanged( ListSelectionEvent event ) {
       Object source = event.getSource() ;
    }
    private void addQueueInfo( QueueInfo info ){
       _model.add(info);
       _model.fire() ;
    }
    public void setCostInfo( PoolCostInfo costInfo ){
    
        _model.clear() ;
        addQueueInfo( new QueueInfo( "Mover"     , costInfo.getMoverQueue() ) ) ;
        addQueueInfo( new QueueInfo( "Restore"   , costInfo.getRestoreQueue() ) ) ;
        addQueueInfo( new QueueInfo( "Store"     , costInfo.getStoreQueue() ) ) ;
        addQueueInfo( new QueueInfo( "P2p"       , costInfo.getP2pQueue() ) ) ;
        addQueueInfo( new QueueInfo( "P2pClient" , costInfo.getP2pClientQueue() ) ) ;
        
        Map map = costInfo.getExtendedMoverHash() ;
        if( map != null ){
           for( Iterator i = map.values().iterator() ; i.hasNext() ; ){
               addQueueInfo( new QueueInfo( (PoolCostInfo.NamedPoolQueueInfo)(i.next()) ) ) ;
           }  
        }
    }
}
