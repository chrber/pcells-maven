// $Id: HipBorder.java,v 1.1 2004/09/08 19:44:10 cvs Exp $
//
package org.dcache.gui.pluggins;
//

import javax.swing.border.Border;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.RenderingHints;


public class HipBorder implements Border {

   private int     _dim      = 50 ;
   private int     _diff     = 5 ; 
   private String  _title    = null ;
   private Object  AntiAlias = RenderingHints.VALUE_ANTIALIAS_ON;
   private Object  Rendering = RenderingHints.VALUE_RENDER_SPEED;
   private Font   _defaultFont = new Font( "SansSerif" , Font.ITALIC | Font.BOLD , 8 ) ;

   public HipBorder( String title , int height ){
      _title = title ;
      _dim   = height ;
      _diff  = _dim / 10 ;
      _defaultFont = _defaultFont.deriveFont( (float)( _dim - 2 * _diff ) ) ;
   }
   public void paintBorder( Component c ,
                            Graphics g ,
                            int x , int y , int width , int height ){

//          System.out.println("Graphics : "+g.getClass().getName()+ " is 2D " +
//                       ( g instanceof java.awt.Graphics2D ) ) ;

       Graphics2D g2 = (Graphics2D) g ;

       g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, AntiAlias);
//          g2.setRenderingHint(RenderingHints.KEY_RENDERING, Rendering);


       g2.setColor( c.getBackground().darker() ) ;

       Polygon borderShape = new Polygon() ;
       borderShape.addPoint( _diff , _diff ) ;
       borderShape.addPoint( width - 2 * _diff -1 , _diff ) ;
       borderShape.addPoint( _dim - _diff , _dim - _diff ) ;
       borderShape.addPoint( _diff , height - 2 * _diff - 1 ) ;

       g2.fill( borderShape ) ;
       g2.setColor( g2.getColor().darker() ) ;

       g2.setFont( _defaultFont ) ;
       FontMetrics fm = c.getFontMetrics( _defaultFont );
       int strH = (int) (fm.getAscent()-fm.getDescent());
       int sw   = fm.stringWidth(_title);

       g2.setColor( Color.white ) ;
       g2.drawString( _title , width/2 - sw/2 , _dim / 2 + strH/2 ) ;

//          System.out.println("x="+x+";y="+y+";w="+width+";h"+height ) ;


   }
   public Insets getBorderInsets( Component c ){
      return new Insets( _dim , _dim , _dim/2 , _dim/2  ) ;
   }
   public boolean isBorderOpaque(){ return false ; }
}
