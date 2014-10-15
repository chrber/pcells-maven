// $Id: CellIcon.java,v 1.2 2004/06/21 22:30:27 cvs Exp $
//
package org.pcells.services.gui ;
//

import javax.swing.*;
import java.awt.*;


public class CellIcon implements Icon {

   private int _height = 0 ;
   private int _width  = 0 ;
   private Color _color  = new Color( 0 , 0 , 255 ) ;

   public CellIcon( int width , int height ){
      _height = height ;
      _width  = width ;
   }
   public CellIcon( int width , int height , Color color ){
      _height = height ;
      _width  = width ;
      if( color != null )_color  = color ;
   }
   public void paintIcon( Component c , Graphics gin , int xi , int yi ){
      Graphics2D g = (Graphics2D) gin ;

//      g.setColor( Color.white ) ;
//      g.fillRect(  xi , yi , _width - 1 , _height - 1 ) ;
//      g.setColor( Color.black ) ;    
//      paintLogo( g , 2 , 2 , _width - 1 , _height - 1 ) ;
      g.setColor( Color.blue ) ;    
      paintLogo( g , 0 , 0 , _width - 1 , _height - 1 ) ;
    }
    private static final double DEVINES_PORTION =
           ( Math.sqrt(5.0) - 1.0 ) / 2.0 ;
    private static final double DEVINE_WIDTH  = 2.0 + DEVINES_PORTION ;
    private static final double DEVINE_HEIGHT = 2.0 - DEVINES_PORTION*DEVINES_PORTION;
    public void paintLogo( Graphics2D g , int x , int y , int width , int height ){
    
       int xbox = x ;
       int ybox = y ;
       int hx = (int)( (double)width  / DEVINE_WIDTH ) ;
       int hy = (int)( (double)height / DEVINE_HEIGHT ) ;
       int hbox = 0 ;
       if( hx < hy ){
          hbox = hx ;
          ybox += ( height - (int)( (double)width / DEVINE_WIDTH * DEVINE_HEIGHT) )/2;
       }else{
          hbox = hy ;
          xbox += ( width - (int)( (double)height / DEVINE_HEIGHT * DEVINE_WIDTH) )/2;          
       }
       for( int i = 0 ; hbox > 5 ; i++ ){
       
          g.fillOval( xbox , ybox , hbox , hbox ) ;
          
          xbox += hbox ;
       
          hbox = (int)( DEVINES_PORTION * (double)hbox ) ;
                    
          ybox += hbox ;
       }
    }
   public void paintIcon2( Component c , Graphics gin , int xi , int yi ){
      Graphics2D g = (Graphics2D) gin ;

//         g.setColor( c.getBackground() ) ;
      g.fillRect(  xi , yi , _width - 1 , _height - 1 ) ;
      int x = xi + 4 ;
      int y = yi + 4 ;
      int width = _width - 8 ;
      int height = _height - 8 ;

      Color col = _color ;

      while( width > 0 ){
         g.setColor( col ) ;
         width = width / 2 ; height = height / 2 ;
         g.fillOval( x , y , width , height ) ;
         x = x + width  ; y = y + height   ;
         col = col.brighter() ;
      }
    }
   public int getIconWidth(){ return _height ; }
   public int getIconHeight(){ return _width ; }
}
