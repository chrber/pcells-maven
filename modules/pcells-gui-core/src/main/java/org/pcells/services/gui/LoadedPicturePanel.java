 // $Id: LoadedPicturePanel.java,v 1.2 2008/07/08 15:54:34 cvs Exp $
//
package org.pcells.services.gui ;

 import javax.swing.*;
 import java.awt.*;
 import java.net.URL;

//


public class LoadedPicturePanel extends JPanel {

   private Icon _icon   = null ;
   private Font _font   = new Font( "Times" , Font.BOLD | Font.ITALIC , 16 ) ;
   private Font _small  = new Font( "Times" , Font.PLAIN , 8 ) ;
   private int  _height = 0 ;
   private Color _ourGray = new Color( 0x33, 0x33 , 0x33 ) ;
    
   private String _copyrightString = "p-Cell Graphics Interface, (c) 2004-2008" ;
   private String _labelString     = null ;
   private double _progress        = -1.0 ;
   
   public LoadedPicturePanel( String picturePath , int height){

      URL imageUrl = getClass().getResource( picturePath );
      if( imageUrl != null ){
          ImageIcon iicon = new ImageIcon(imageUrl) ;
          Image     im    = iicon.getImage() ;
          im = im.getScaledInstance(  -1 , height - 20  , Image.SCALE_SMOOTH ) ;
          _icon = (Icon)new ImageIcon(im);
       }
   }
   public void setMessage(String message){
      _labelString = message ;
      repaint() ;
   }
   public void setProgress( double progress ){
      _progress = progress ;
      repaint() ;
   }
   public void paintComponent( Graphics gin ){

      Graphics2D g = (Graphics2D) gin ;

      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                         RenderingHints.VALUE_ANTIALIAS_ON);

      Dimension d = getSize() ;

      //
      // draw background
      //
      g.setColor( Color.white ) ;
      g.fillRect( 0 , 0 , d.width-1 , d.height-1 );
      //
      // draw icon
      //
      Icon icon   = _icon == null ? new CellIcon( d.width , d.height ) : _icon ;
      int  width  = icon.getIconWidth() ;
      int  height = icon.getIconHeight() ;

      icon.paintIcon( this , g , ( d.width - width ) / 2  , ( d.height-height)/2 );
      
      int yLocation = ( d.height + height ) / 2 ;
      //
      // status string
      //
      //_labelString = "Hallo dickes schweinchen";
      //_progress    = (double) 0.3 ;
      if( _labelString != null ){
      
 	 
	 g.setFont( _font ) ;
	 g.setColor( _ourGray ) ;
	 FontMetrics metrics = g.getFontMetrics() ;
	 int     stringWidth = metrics.stringWidth(_labelString) ;
	 
	 int asc = metrics.getAscent() ;
	 int des = metrics.getDescent() ;

         yLocation += 10 + asc ; 
	 
         g.drawString(_labelString , ( d.width - stringWidth ) / 2  , yLocation ) ; 
         
	 yLocation += des + 10 ;
	 
      }
      
      if( _progress > -0.5 ){
         g.setColor( _ourGray ) ;
         g.fillRect( ( d.width - width ) / 2 , yLocation , (int)(_progress*(double)width) , 10 ) ;
         g.setColor( Color.blue ) ;
         g.drawRect( ( d.width - width ) / 2 , yLocation , width , 10 ) ;
      }
  
      //
      // draw copyright
      //
      g.setFont( _small) ;
      g.setColor( Color.black ) ;
      FontMetrics metrics     = g.getFontMetrics() ;
      int stringWidth = metrics.stringWidth(_copyrightString) ;
      g.drawString(_copyrightString ,  
                   ( d.width - stringWidth ) / 2  , 
                   d.height - metrics.getAscent() - metrics.getDescent() - 5 ) ; 
   }


}

