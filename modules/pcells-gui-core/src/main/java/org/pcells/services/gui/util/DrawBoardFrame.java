// $Id: DrawBoardFrame.java,v 1.8 2008/08/04 18:46:28 cvs Exp $
//
package org.pcells.services.gui.util ;

//

import javax.swing.JFrame;

import java.awt.Component;
import java.awt.event.ContainerListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class DrawBoardFrame extends JFrame  {

   public static DrawBoardFrame __sharedStoryBoard = null ;
   
   private ShadowedPanel _movingObjects    = null ;

   public DrawBoardFrame(String title ){
   
        super(title);
        
        //_movingObjects = new MovingObjectPanel();

        _movingObjects = new ShadowedPanel() ;
	
        getContentPane().add( _movingObjects , "Center" );
        pack();
        setLocation(200,200);
        setSize(300,200);
               
        addWindowListener(
            new WindowAdapter(){
               public void windowClosing(WindowEvent e) {
                   setVisible(false) ;
               }
            }
        );
                
        __sharedStoryBoard = this ;   
    }

   public void refreshProperties(){
       _movingObjects.refreshProperties() ;
   }

   public void addToDrawboard( Component component , String name ){
      _movingObjects.addOverlayComponent( component , name );
   }
   public void removeFromDrawboard( Component component ){
      //_movingObjects.removeOverlayComponent( component ) ;
   }
   public void addContainerListener( ContainerListener listener ){
      _movingObjects.addContainerListener(listener);
   }
   public void removeContainerListener( ContainerListener listener ){
      _movingObjects.removeContainerListener(listener);
   }
   public Component getMasterComponent(){ 
      return _movingObjects ; 
   }
  
}

