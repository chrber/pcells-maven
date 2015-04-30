// $Id: JHistoryComboBox2.java,v 1.2 2006/12/23 18:01:50 cvs Exp $
//
package org.pcells.services.gui ;
//

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import java.awt.AWTEventMulticaster;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class JHistoryComboBox2 
       extends JComboBox 
       implements ActionListener, ItemListener {

   private YComboBoxModel _model = null ;
   
   
   public void itemStateChanged( ItemEvent event ){
     // System.out.println("Item State changed "+event.getStateChange()+" -> "+event.getItem());       
      if( event.getStateChange() != ItemEvent.SELECTED )return ;
      String name = (String)event.getItem() ;
     // System.out.println("Item State changed --> "+name);       
      processEvent(new ActionEvent( this , 0 , "destination" ) );
   }
   public JHistoryComboBox2(){
   
       super() ;
       
       setModel( _model = new YComboBoxModel() ) ;
       addItemListener(this);
       setEditable(true);
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
   private class YComboBoxModel extends DefaultComboBoxModel {
       
      public synchronized void setSelectedItem( Object obj ){
         if( obj.toString().length() > 0 ){
            int pos = getIndexOf( obj ) ;
            if( pos < 0 )insertElementAt(obj,0);
            else if( pos > 0 ){
                removeElementAt(pos) ;
                insertElementAt(obj,0);
            }
            int size = getSize() ;
            if( size > 20 )removeElementAt(size-1);
         }
         super.setSelectedItem(obj);
      }
   }

}
