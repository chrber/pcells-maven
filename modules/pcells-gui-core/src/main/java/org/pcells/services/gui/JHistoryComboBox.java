// $Id: JHistoryComboBox.java,v 1.4 2006/12/23 18:01:50 cvs Exp $
//
package org.pcells.services.gui ;
//

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

public class JHistoryComboBox 
       extends JComboBox 
       implements ActionListener, ItemListener {

   private XComboBoxModel _model = null ;
   
   private class ComboEntry implements Comparable {
      private long date  = System.currentTimeMillis() ;
      private int position = 0 ;
      private String name  = null ;
      private ComboEntry( String name ){ this.name = name ; }
      public int compareTo( Object obj ){
         ComboEntry e = (ComboEntry)obj ;
         return e.date > date ?  1 : 
                e.date < date ? -1 :
                e.name.compareTo(name) ;
      }
      public void touch(){
        date  = System.currentTimeMillis() ;
      }
      public boolean equals(Object other ){
         return ((ComboEntry)other).name.equals(name) ;
      }
      public String toString(){ return name ; }
   }
   public void actionPerformed( ActionEvent event ){
      super.actionPerformed(event);
      String name = (String)_model.getSelectedItem() ;
  //    System.out.println("Action performed : "+name);
      if( ( name == null ) || ( name.equals("") ) )return ;     
      _model.addItem(name);
      _model.reorder();
      processEvent(new ActionEvent( this , 0 , "destination" ) );
   }
   public void itemStateChanged( ItemEvent event ){
  //    System.out.println("Item State changed "+event.getStateChange()+" -> "+event.getItem());       
      if( event.getStateChange() != ItemEvent.SELECTED )return ;
      String name = (String)event.getItem() ;
      _model.touch(name);  
      _model.reorder();
   //   System.out.println("Item State changed --> "+name);       
      processEvent(new ActionEvent( this , 0 , "destination" ) );
   }
   public JHistoryComboBox(){
   
       super() ;
       
       setModel( _model = new XComboBoxModel() ) ;
       addItemListener(this);
       setEditable(true);
   }
   public void addTopItem( String name ){
      _model.addTopItem(name);
   }
   public void addItem( String name ){
      _model.addItem(name);
   }
   private int _maxItemCount = 10 ;
   public void setMaxItemCount( int count ){
      _maxItemCount = count ;
   }
   private ActionListener _actionListener = null;

   public synchronized void addActionListener(ActionListener l) {
      _actionListener = AWTEventMulticaster.add( _actionListener, l);
   }
   public synchronized void removeActionListener(ActionListener l) {
      _actionListener = AWTEventMulticaster.remove( _actionListener, l);
   }
   public void processEvent( ActionEvent e) {
      if( _actionListener != null)
        _actionListener.actionPerformed( e );
   }
   private class XComboBoxModel extends AbstractListModel implements ComboBoxModel  {
   
      private HashMap   _map    = new HashMap() ;
      private HashMap   _topMap = new HashMap() ;
      private ArrayList _list   = new ArrayList() ;
      private String    _first  = null ;
      
      public void setSelectedItem( Object obj ){
         _first = obj.toString() ;
      }
      public Object getSelectedItem(){
         return _first != null ?
                _first :
                _list.size() == 0 ? null :
                _list.get(0).toString() ;
      }
      public Object getElementAt( int i ){
         ComboEntry entry = (ComboEntry)_list.get(i);
         return entry.name ;
      }
      public int getSize(){ 
         return _list.size() ;
      }
      public void touch( String name ){
         ComboEntry entry = (ComboEntry)_map.get(name);
         if( entry != null ){            
            entry.touch();
            return ;            
         }         
      }
      public void addTopItem( String name ){
         ComboEntry entry = (ComboEntry)_map.get(name);
         if( entry == null ){
            _map.put( name , entry = new ComboEntry(name) ) ;
            _list.add( entry ) ;
         }
         _topMap.put( name , entry ) ;
         fireIntervalAdded(this,0,_list.size()-1);
      }
      public void touchTopMap(){
         Iterator iter = _topMap.values().iterator() ;
         while( iter.hasNext() )((ComboEntry)iter.next()).touch() ;
      }
      public void addItem( String name ){
         ComboEntry entry = (ComboEntry)_map.get(name);
         if( entry != null ){            
            entry.touch();
            return ;            
         }         
         _map.put( name , entry = new ComboEntry(name) ) ;
         _list.add( entry ) ;
         fireIntervalAdded(this,0,_list.size()-1);
      }
      public void reorder(){
        touchTopMap();
        int size = _list.size() ;
        if( ( size == 0 ) || ( size == 1 ) )return ;
        TreeSet tree = new TreeSet( _list ) ;
        _list.clear() ;
        if( size > _maxItemCount ){
           Iterator iter = tree.iterator() ;
          for( int i = 0 ; i < _maxItemCount ; i++ )_list.add(iter.next());
        }else{
           _list.addAll(tree);
        }
        fireContentsChanged(this,0,size-1);
      }
      
   }

}
