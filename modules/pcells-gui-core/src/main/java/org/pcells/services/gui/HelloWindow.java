// $Id: HelloWindow.java,v 1.3 2008/07/06 21:25:02 cvs Exp $
//
package org.pcells.services.gui ;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JWindow;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

//
 
public class HelloWindow extends JWindow {

    private javax.swing.Timer _timer  = new javax.swing.Timer( 50 , new Scheduler() );
    private PicturePanel  _picture    = null ;
    
    private class Scheduler implements ActionListener {
       private int _state   = 0 ;
       private int _counter = 0 ;
       public void actionPerformed( ActionEvent event ){

           if( ! _picture.nextStringPosition() )return ;

           if( _state == 0 ){                  
              _picture.setSheepPosition(0) ;
              if( _counter ++ > 3 )_state   = 1 ; 
              _picture.repaint();
           }else if( _state == 1 ){
           
              HelloWindow.this.setVisible(false);
              _timer.stop();
              
              return ;               
           }

       }
    }
    public HelloWindow( Window top ){
        super(top);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        int windowWidth  = 400 ; 
        int windowHeight = 300 ;

        _picture =  new PicturePanel( new String[]{"/images/sheep001.png"} , windowHeight ) ;
        getContentPane().add( _picture ) ;

        pack() ;

        setLocation( ( screenSize.width - windowWidth ) / 2 , ( screenSize.height - windowHeight ) / 2 );
        setSize( windowWidth , windowHeight );
        setVisible(true);

        _timer.start() ;

    }
    public void setVersion( JMultiLogin.VersionUpdate version ){}
    public void setStringList( String [] list ){ _picture.setStringList( list ) ; }
    public class PicturePanel extends JPanel {

       public  Icon _icon  = null ;
       private Font _font  = new Font( "Times" , Font.BOLD | Font.ITALIC , 16 ) ;
       private Font _small = new Font( "Times" , Font.PLAIN , 8 ) ;
       private int  _iconPosition = 1 ;
       private int  _maxStrings   = -1 ;
       private String _copyrightString = "p-Cell Graphics Interface, (c) 2004-2008" ;
       private String  [] _stringList  =  null ;

       public PicturePanel( String [] picturePaths , int height){

          URL imageUrl = getClass().getResource( picturePaths[0] );
          System.out.println("Sheep : "+imageUrl);
          if( imageUrl != null ){
              ImageIcon iicon = new ImageIcon(imageUrl) ;
              Image im    = iicon.getImage() ;
              im = im.getScaledInstance(  -1 , height - 20  , Image.SCALE_SMOOTH ) ;
              _icon = (Icon)new ImageIcon(im);
           }
       }
       private void setStringList( String [] list ){ _stringList = list ; }
       private boolean nextStringPosition(  ){
           if( _stringList == null )return false ;
           _maxStrings ++ ;
           repaint();
           return _maxStrings > _stringList.length ; 
       }
       private void setSheepPosition( int position ){ _iconPosition = position ; }
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
          FontMetrics metrics = null ;

          icon.paintIcon( this , g , _iconPosition * ( d.width - width - 1 )  , 10 /* ( d.height-height)/2 */ );

          if( _iconPosition == 0 ){
             g.setFont( _font ) ;
             g.setColor( Color.red ) ;
             g.drawString("Don't panic" ,  d.width/2 , d.height/2 ) ; 
          }else if( _stringList != null ){
             g.setColor( Color.red ) ;
             g.setFont( _font ) ;
             metrics = g.getFontMetrics() ;
             for( int i = 0  , position = metrics.getAscent() + 15 ; 
                  ( i < _stringList.length ) && ( i < _maxStrings ) ; 
                  i++ ){
                 g.drawString( _stringList[i] , 10 , position ) ;
                 position += metrics.getAscent() + metrics.getDescent() + 5 ;
             }
          }
          //
          // draw copyright
          //
          g.setFont( _small) ;
          g.setColor( Color.black ) ;
          metrics = g.getFontMetrics() ;
          int stringWidth = metrics.stringWidth(_copyrightString) ;
          g.drawString(_copyrightString ,  
                       ( d.width - width ) / 2  , 
                       d.height - metrics.getAscent() - metrics.getDescent() - 5 ) ; 
       }


    }
}

