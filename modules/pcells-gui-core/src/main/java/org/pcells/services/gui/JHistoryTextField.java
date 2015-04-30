// $Id: JHistoryTextField.java,v 1.3 2005/05/09 05:47:54 cvs Exp $
//
package org.pcells.services.gui ;
//

import javax.swing.JTextField;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;

public class JHistoryTextField 
       extends JTextField
       implements KeyListener , ActionListener {

   private  Vector        _history  = new Vector() ;
   private ActionListener _listener = null ;
   private int            _position = 0 ;

   public JHistoryTextField(){
      super() ;
      addKeyListener( this ) ;
      super.addActionListener( this ) ;
   }

   public void addActionListener( ActionListener listener ){
       _listener = listener ;
   }
   public void keyPressed( KeyEvent event ){
       if( event.getKeyCode() == KeyEvent.VK_UP ){
          if( _position < _history.size() )
             setText( (String)_history.elementAt(_position++) ) ;

       }else if( event.getKeyCode() == KeyEvent.VK_DOWN ){
          if( _position > 0 )
             setText( (String)_history.elementAt(--_position) ) ;
          else if( _position == 0 )
             setText( "" ) ;
       }
   }
   public Vector getVector(){ return _history ; }
   public void setVector( Vector v ){ _history = v ; }
   public void keyReleased( KeyEvent event ){}
   public void keyTyped( KeyEvent event ){}
   public void actionPerformed( ActionEvent event ){
        String command = getText() ;
        if(  ( ! command.equals("") ) &&
             ( ( _history.size() == 0 ) ||
               ! _history.elementAt(0).equals(command) ) )
           _history.insertElementAt( getText() , 0  ) ;

        if( _listener != null )_listener.actionPerformed( event ) ;
        _position = 0 ;
   }


}
