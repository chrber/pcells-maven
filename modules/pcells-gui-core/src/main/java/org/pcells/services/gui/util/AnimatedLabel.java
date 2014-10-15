// $Id: AnimatedLabel.java,v 1.1 2007/02/18 07:46:16 cvs Exp $
//
package org.pcells.services.gui.util ;
//

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class AnimatedLabel extends JPanel implements ActionListener {
      private javax.swing.Timer _timer   = new javax.swing.Timer( 250 , this);
      private String _message = "" ;
      private int    _status  = 0 ;
      private int    _currentPosition = 0 ;
      private float  _currentLambda   = (float)0.0;
      
      public AnimatedLabel(String message ){
          _message = message ;
          
          _timer.start() ;
      }
      public void actionPerformed( ActionEvent event ){
         next() ;
         repaint();
      }
      public void paintComponent( Graphics gin ){

         Graphics2D g = (Graphics2D) gin ;

         g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

         Dimension d = getSize() ;

          g.setColor( getBackground() ) ;
          g.fillRect( 0 , 0 , d.width , d.height );


          g.setColor( calculateColorVector( _currentLambda ) ) ;

         Font f = g.getFont().deriveFont( (float)( d.height - 5 ) ) ;
         
         g.setFont( f ) ;
         
         FontMetrics fm = g.getFontMetrics() ;
         
         int baseline = d.height - fm.getDescent() - fm.getLeading();

         String line = _message ;
         if( ( line == null ) || ( _currentPosition <= 0 ) )return ;
         
         line =  line.substring(0,_currentPosition)  ;
                   
         g.drawString( line , 5 , baseline ) ;
      }
      private Color calculateColorVector( float lambda ){
         Color foreground = getForeground() ;
         Color background = getBackground() ;
         
         int rRed = (int) ( (float)( background.getRed() - foreground.getRed() ) * lambda +
                            (float)( foreground.getRed() ) ) ;
         int rBlue = (int) ( (float)( background.getBlue() - foreground.getBlue() ) * lambda +
                            (float)( foreground.getBlue() ) ) ;
         int rGreen = (int) ( (float)( background.getGreen() - foreground.getGreen() ) * lambda +
                            (float)( foreground.getGreen() ) ) ;
                            
         return new Color( Math.min(rRed,255) , Math.min(rGreen,255) , Math.min(rBlue,255) ) ;
      }
      public boolean next(){
      
         String line = _message ;
         if( line == null )return true ;
         
         if( _status == 0 ){
            if( _currentPosition < line.length() ){
               _currentPosition++ ;
            }else{
               _status = 1 ;
            }
         }else if( _status == 1 ){
            if( _currentLambda < (float)1.0 ){
                _currentLambda += (float)0.05 ;
            }else{
                _status = 0 ;
                _currentLambda = (float)0.0 ;
                _currentPosition = 0 ;
            }
         }
         return false ;
      }
}

