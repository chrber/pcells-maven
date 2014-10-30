// $Id: OptionList.java,v 1.2 2008/06/25 06:49:40 cvs Exp $
//
package org.dcache.gui.pluggins.poolManager ;
//
import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.util.* ;
import java.util.regex.* ;

import org.pcells.services.gui.* ;

public class OptionList 
       extends CellGuiSkinHelper.CellPanel {
       
   private String       _title  = null ;
   private TitledBorder _border = null ;
   private JList        _list   = null ;
   private OlListModel  _model  = new OlListModel() ;
   
   public OptionList( String title ){
   
      _title = title ;
      
      BorderLayout bl = new BorderLayout();
      
      setLayout( bl ) ;
      
      _border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder( Color.blue , 1 ) , _title
                ) ;
               
      setBorder( _border ) ;
      
      _list = new JList( _model ) ;
      
      add( new JScrollPane( _list ) , "Center" ) ;

       _list.setCellRenderer( new OlCellRenderer() ) ;
       //_list.addListSelectionListener( new OlListSelectionListener() ) ;
       _list.setSelectionModel( new OlListSelectionModel() ) ;
      
   } 
   public interface ListEntry {
       public boolean isEnabled() ;
       public boolean isHighlighted() ;
   }
   public JList getJList(){ return _list ; }
   public void clear(){ _model.clear() ; }
   public void add( ListEntry entry ){ _model.add(entry) ; }
   public void addAll( Collection c ){
     // _model.addAll(c) ;
   }  
   public void commit(){ _model.commit() ; }
   /**
    *
    * The Model
    */
   private class OlListModel extends AbstractListModel {
       private boolean _changed = false ;
       private ArrayList _list  = new ArrayList() ;
       private TreeMap   _map   = new TreeMap() ;
       
       public synchronized void add( ListEntry entry ){
          _changed = true ;
          _map.put( entry.toString() , entry ) ;
       }
       public synchronized void clear(){
          _map.clear();
          _changed = true ;
       }
       public synchronized void addAll( Collection c ){
          //_map.addAll(c) ;
       }
       public synchronized void commit(){
          if( _changed ){
              _list.clear() ;
              _list.addAll( _map.values() ) ;
          }
          fireContentsChanged(this,0,_list.size()-1);
          _changed = false ;
       }
       public int getSize(){ return _list.size() ; }
       public Object getElementAt( int i ){  return _list.get(i) ; }
   }
   /**
    *   The renderer
    */
   private class OlCellRenderer extends DefaultListCellRenderer {

       /* This is the only method defined by ListCellRenderer.  We just
        * reconfigure the Jlabel each time we're called.
        */
       public Component getListCellRendererComponent(
           JList list,
	   Object value,   // value to display
	   int index,      // cell index
	   boolean iss,    // is the cell selected
	   boolean chf)    // the list and the cell have the focus
       {
           /* The DefaultListCellRenderer class will take care of
            * the JLabels text property, it's foreground and background
            * colors, and so on.
            */
           super.getListCellRendererComponent(list, value, index, iss, chf);

           /* We additionally set the JLabels icon property here.
            */
 
           if( value instanceof ListEntry ){
           
               ListEntry entry = (ListEntry)value ;
 
               if( ! entry.isEnabled() )setEnabled(false);
               
               if( entry.isHighlighted() )setForeground(Color.red);
               
           }
	   return this;
       }
   }  
   /**
    * 
    * The Selection Model 
    *   Takes care that the disabled entries are not selectable
    */ 
   public class OlListSelectionModel extends DefaultListSelectionModel{
       public void addSelectionInterval(int index0, int index1){
          for( int i = index0 ; i <= index1 ; i++ ){
             if( isSettingOk(i) )super.addSelectionInterval(i,i) ;
          }
       }
       public void setSelectionInterval(int index0, int index1){
          super.clearSelection() ;
          for( int i = index0 ; i <= index1 ; i++ ){
             if( isSettingOk(i) )super.addSelectionInterval(i,i) ;
          }
       }
       private boolean isSettingOk( int index ){
          ListEntry entry = (ListEntry) _model.getElementAt(index) ;
          return entry.isEnabled() ;
       }
   }



}
