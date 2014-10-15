// $Id: ActionEventDistributer.java,v 1.1 2007/01/14 08:17:46 cvs Exp $
//
package org.pcells.services.gui ;
//

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ActionEventDistributer { 

     ActionListener actionListener = null;
     
     private class Runner implements Runnable {
         private ActionEvent _event = null ;
         public Runner( ActionEvent event ){ _event = event ; }
         public void run(){
            processEvent( _event ) ;
         }
     }
     public synchronized void addActionListener(ActionListener l) {
	   actionListener = AWTEventMulticaster.add(actionListener, l);
     }
     public synchronized void removeActionListener(ActionListener l) {
  	   actionListener = AWTEventMulticaster.remove(actionListener, l);
     }
     public void processEvent(ActionEvent e) {
         // when event occurs which causes "action" semantic
         ActionListener listener = actionListener;
         if (listener != null) {
             listener.actionPerformed(e);
         }
     }
     public void fireEvent( ActionEvent e ){
        SwingUtilities.invokeLater( new Runner( e ) ) ;
     }
 }
