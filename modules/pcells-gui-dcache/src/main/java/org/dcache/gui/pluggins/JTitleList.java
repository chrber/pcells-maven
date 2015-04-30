// $Id: JTitleList.java,v 1.2 2005/04/17 22:04:40 cvs Exp $
//
package org.dcache.gui.pluggins;
//

import org.pcells.services.gui.CellBorder;
import org.pcells.services.gui.CellGuiSkinHelper;

import javax.swing.AbstractListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

public class JTitleList 
        extends CellGuiSkinHelper.CellPanel 
        implements   ActionListener,
                     ListSelectionListener {

    private JTextField   _text      = new JTextField("*") ;
    private Pattern      _pattern   = null ;
    private JList        _listPanel = null ;
    private SetListModel _listModel = null ;
    private TreeSet      _set       = new TreeSet() ;

    public JTitleList( String title ){
       BorderLayout l = new BorderLayout() ;
       l.setVgap(20) ;
       l.setHgap(20);
       setLayout(l) ;
       setBorder( new CellBorder(title,20) ) ; 

       _text.addActionListener( this ) ;

       _listModel = new SetListModel() ;
       _listPanel = new JList( _listModel ) ;
       _listPanel.addListSelectionListener(this);

       add( _text , "North" ) ;

       JScrollPane sp = new JScrollPane();
       sp.getViewport().add( _listPanel );

       add( sp  , "Center") ;
    }
    public void setEnabled( boolean enabled ){
       _text.setEnabled(enabled);
       _listPanel.setEnabled(enabled);
    }
    public void setPrototypeCellValue( String value ){
       _listPanel.setPrototypeCellValue( value ) ;
    }
    public void setBackground( Color color ){
       super.setBackground(color);
       if( _listPanel != null )_listPanel.setBackground( color.brighter().brighter() ) ;
    }
    public void addListSelectionListener( ListSelectionListener listener ){
       _listPanel.addListSelectionListener(listener);
    }
    public void clear(){ 
       _set.clear() ;
       _listModel.clear() ;
       _listModel.apply() ;
       _listPanel.clearSelection() ;
    }
    public JList getJList(){ return _listPanel ; }
    public Set set(){ return _set  ; } ;
    public void apply(){
       _listModel.clear() ;
       if( _pattern == null ){
          _listModel.addAll( _set ) ;
          _listModel.apply() ;
       }else{
          Iterator i = _set.iterator() ;
          while( i.hasNext() ){
             Object o = i.next() ;
             if( _pattern.matcher(o.toString()).matches() )
                _listModel.add( o.toString() ) ;

          }
       }
       _listModel.apply() ;
    }
    public void actionPerformed( ActionEvent event ){
//       System.out.println("Action");
       if( event.getSource() == _text ){
          _listPanel.clearSelection();
          String text = _text.getText() ;
          try{
             _pattern = text.equals("") ?
                        null :
                        Pattern.compile(text) ;
          }catch(Exception ee ){
             _text.setText(".*") ;
             _pattern = null ;
          }
       }
       apply() ;
    }
    public void valueChanged( ListSelectionEvent event ){
       JList list = (JList)event.getSource() ;
       Object [] selected = list.getSelectedValues() ;
//       System.out.println("Selection event");
       if( selected == null ){
           System.out.println("NOthing selected");
       }else{
//           for( int i = 0 ; i < selected.length ; i++ ){
//              System.out.println( ""+i+" "+selected[i] ) ;
//           }
       }
    }
    private class SetListModel extends AbstractListModel {
       private ArrayList _list = new ArrayList() ;
       public int getSize(){ 
          return _list.size() ;
       }
       public Object getElementAt( int index ){
          return _list.get(index) ;
       }
       public void clear(){ _list.clear() ; }
       public void addAll( Collection collection ){
          _list.addAll(collection);
       } 
       public void add( Object obj ){ _list.add( obj ) ; } 
       public void apply(){
          fireContentsChanged(_listPanel,0,_list.size());
       }        
    }

}
