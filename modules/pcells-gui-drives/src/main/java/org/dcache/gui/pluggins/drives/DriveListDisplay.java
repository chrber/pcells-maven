// $Id: DriveListDisplay.java,v 1.3 2007/02/28 08:38:34 cvs Exp $

package org.dcache.gui.pluggins.drives;

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

public class DriveListDisplay extends JPanel implements ActionListener {


     private  Font              _bigFont   = null ;
     private  Font              _smallFont = null ;
     private  DrivePanelParas   _DC = null ;
     private  javax.swing.Timer _timer = null ;
     private  boolean           _blink = true ;
     private  Color             _backgroundColor = Color.white ;
     private  Color             _foregroundColor = Color.black ;
     private  Color             _blinkColor      = Color.red ;

     private  DriveStatusInfo [] _driveInfoList = null ;
     private  Object             _listLock      = new Object() ;
     
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
             
             dimension.height = 3 * edge + bigHeight ;
             dimension.width  = Math.max( 
                      2 * smallWidth + 1 * edge ,
                      bigWidth + 40  ) ;
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
      public DriveListDisplay(){
          _timer = new javax.swing.Timer( 1000 , this ) ;
          _timer.start() ;
      }
      public void actionPerformed( ActionEvent event ){
            _blink = ! _blink ;
            repaint();
      }
      public void setDriveList( DriveStatusInfo [] info ){
         synchronized( _listLock ){
            _driveInfoList = info ;
            repaint();
         }
      }
      public void update(){ repaint() ; }
      public void paintComponent( Graphics gin ){
      
          super.paintComponent(gin);
          
          Dimension d = getSize() ;
          gin.setColor( Color.gray ) ;

          Graphics2D g = (Graphics2D) gin ;
          g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                              RenderingHints.VALUE_ANTIALIAS_ON);

            
          if( _DC == null )_DC = new DrivePanelParas( g ) ;
         
          DriveStatusInfo [] list = null ;
          synchronized( _listLock ){
             list = _driveInfoList ;
             if( list == null )return ;
          }  
          
          Point boxPosition = new Point( 20 , 10 ) ;
          for( int i = 0 , n = list.length ; i< n ; i++ ){
           
             DriveStatusInfo info = list[i] ;
             
             g.translate( boxPosition.x , boxPosition.y ) ;
           
             g.setColor( Color.gray);
           
             g.fillRoundRect( 5  , 5  , 
                             _DC.dimension.width , _DC.dimension.height, 
                             _DC.edge , _DC.edge  ) ;
                           
             g.setColor( _backgroundColor ) ;
           
           
             g.fillRoundRect( 0 , 0 , _DC.dimension.width , _DC.dimension.height, _DC.edge , _DC.edge  ) ;
           
           
             g.setColor( _foregroundColor ) ;
             g.setFont( _smallFont ) ;
             
             String name = info.getName() ;
             Point pos = _DC.getDrivePosition(name) ;
             g.drawString( name , pos.x , pos.y ) ;
           
           
             name = info.isEmpty() ? "-" : info.getTapeName() ;
             g.setFont( _bigFont ) ;
             pos = _DC.getTapePosition(name) ;
             g.drawString( name , pos.x , pos.y ) ;
           
             if( _blink && info.isAction() ){
               g.setColor( Color.red ) ;
               name =  info.getAction() ;
               g.setFont( _smallFont ) ;
               pos = _DC.getActionPosition(name) ;
               g.drawString( name , pos.x , pos.y ) ;
             }
             g.translate( -boxPosition.x , -boxPosition.y ) ;
             
             boxPosition.y += ( _DC.dimension.height + 10 ) ;
         }

     }
}
