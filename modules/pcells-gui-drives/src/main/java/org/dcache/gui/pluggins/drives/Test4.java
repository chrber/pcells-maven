
package org.dcache.gui.pluggins.drives ;

import org.dcache.gui.pluggins.monitoring.*;
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.util.prefs.*;
import dmg.cells.applets.login.* ;
import java.lang.reflect.*;

public class Test4 extends JFrame {

   public class MovingPanel extends JPanel implements ActionListener {
        private  Font _bigFont   = null ;
        private  Font _smallFont = null ;
        private  DrivePanelParas _DC = null ;
        private javax.swing.Timer _timer = null ;
        private  boolean _blink = true ;
        private class DrivePanelParas {
        
          private int edge        = 10 ;
          private int smallHeight = 0 , smallWidth = 0 ;
          private int bigHeight   = 0 , bigWidth  = 0 ;
          private Point drive  = new Point(0,0);
          private Point action = new Point(0,0);
          private Point tape   = new Point(0,0);
          private Dimension dimension = new Dimension(0,0);
          
          private FontMetrics smallMetrics = null , bigMetrics = null ;

          private DrivePanelParas( Graphics g ){
             _bigFont   = g.getFont() ;
             _smallFont = _bigFont.deriveFont( (float)15 ).deriveFont(Font.BOLD|Font.ITALIC) ;
             _bigFont = _bigFont.deriveFont( (float)20 ).deriveFont(Font.BOLD|Font.ITALIC) ;
             
             g.setFont( _smallFont ) ;
             smallMetrics = g.getFontMetrics() ;
             smallHeight = smallMetrics.getAscent() + smallMetrics.getDescent() ;
             smallWidth  = smallMetrics.stringWidth("XXXXXXXXXX");
             
             g.setFont( _bigFont ) ;
             bigMetrics = g.getFontMetrics() ;
             bigHeight = bigMetrics.getAscent() + bigMetrics.getDescent() ;
             bigWidth  = bigMetrics.stringWidth("XXXXXXXXXX");
             
             edge = smallHeight ;
             
             drive.x = edge ;
             drive.y = action.y = smallHeight + smallHeight / 2 ;
             tape.y  = bigHeight + 2 * smallHeight ;
             
             dimension.height = 4 * edge + bigHeight ;
             dimension.width  = Math.max( 
                      2 * smallWidth + 2 * edge ,
                      bigWidth  + bigWidth/2  ) ;
          }
          private Point getTapePosition(String tapeName ){
             tape.x = ( dimension.width - bigMetrics.stringWidth(tapeName) ) / 2  ;
             return tape ;
          }
          private Point getDrivePosition(String driveName ){
             return drive ;
          }
          private Point getActionPosition( String actionString ){
             action.x = dimension.width - edge - smallMetrics.stringWidth(actionString);
             return action;
          }
          private Dimension getSize(){ return dimension ; }
          
        }
        public void actionPerformed( ActionEvent event ){
            _blink = ! _blink ;
            repaint();
        }
       public MovingPanel(){
          _timer = new javax.swing.Timer( 1000 , this ) ;
          _timer.start() ;
       }
       public void paintComponent( Graphics gin ){
          super.paintComponent(gin);
          Dimension d = getSize() ;
          gin.setColor( Color.gray ) ;

           Graphics2D g = (Graphics2D) gin ;
           g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                              RenderingHints.VALUE_ANTIALIAS_ON);


           if( _DC == null )_DC = new DrivePanelParas( g ) ;
           
           Point boxPosition = new Point( 20 , 20 ) ;
           
           g.translate( boxPosition.x , boxPosition.y ) ;
           
           g.setColor( Color.gray);
           
           g.fillRoundRect( 5  , 5  , 
                           _DC.dimension.width , _DC.dimension.height, 
                           _DC.edge , _DC.edge  ) ;
                           
           g.setColor( Color.green ) ;
           
           
           g.fillRoundRect( 0 , 0 , _DC.dimension.width , _DC.dimension.height, _DC.edge , _DC.edge  ) ;
           
           g.setColor( Color.blue ) ;
           g.setFont( _smallFont ) ;
           String name = "drive-x-a" ;
           Point pos = _DC.getDrivePosition(name) ;
           g.drawString( "drive-x-a" , pos.x , pos.y ) ;
           
           
           name = "U2PS44" ;
           g.setFont( _bigFont ) ;
           pos = _DC.getTapePosition(name) ;
           g.drawString( name , pos.x , pos.y ) ;
           
           if( _blink ){
              g.setColor( Color.red ) ;
              name = "Loading" ;
              g.setFont( _smallFont ) ;
              pos = _DC.getActionPosition(name) ;
              g.drawString( name , pos.x , pos.y ) ;
           }
           g.translate( boxPosition.x , boxPosition.y ) ;

       }
   }
   public Test4( String [] args ) throws IOException{
   
      super("Test4 ...");

      WindowListener l = new WindowAdapter() {
            public void windowClosing(WindowEvent e) {System.exit(0);}
      };
      addWindowListener(l);
       
      getContentPane().add( new MovingPanel() ,  "Center" );
      pack();
      setSize(new Dimension(900,500));
      setVisible(true);
      
           
   }
    public static void main(String s[]) throws Exception  {
         new Test4(s);
    }
}
