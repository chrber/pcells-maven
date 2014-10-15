package org.pcells.services.gui ;

import javax.swing.*;
import java.awt.*;

public class PieChartPanel extends JComponent {

    private static Font   __defaultFont = new Font( "SansSerif" , Font.PLAIN  , 20 ) ;
    private Insets _internalInsets = new Insets( 10 , 10 , 10 , 10 ) ;
    static public class Section {
       private String _title   = null ;
       private Color  _color   = null ;
       private long   _amount  = 0L ;
       private int    _start   = 0 ;
       private int    _length  = 0 ;
       public Section( String title , long amount , Color color ){
          _color  = color ;
          _title  = title ;
          _amount = amount ;
       }
       public Section( Section section ){
          _title  = section._title ;
          _color  = section._color ;
          _amount = section._amount ;
       }
    }
    public PieChartPanel(){
      // setBorder( BorderFactory.createTitledBorder( BorderFactory.createLineBorder( Color.blue , 30 ) , "Hallo" ) ) ;
    }
    static public Section createSection( String title , long amount , Color color ){
       return new Section( title , amount , color ) ;
    }
 //   public Dimension getPreferredSize(){ return new Dimension( 150 , 150 ) ; }
    private Section [] _sections = null ;
    public void drawSection( Section [] sections ){

       long total = 0L ;
       double [] fraction  = new double[sections.length] ;
                 _sections = new Section[sections.length] ;
       for( int i = 0 ; i  < sections.length ; i++ ){
          total += sections[i]._amount  ;
          _sections[i] = new Section( sections[i] ) ;
       }
       double dtotal = (double)total;

       int start =  90 ;
       for( int i = 0 ; i  < _sections.length ; i++ ){
          int part = (int) ( (double)_sections[i]._amount / dtotal * (double)360.0 )  ;
          _sections[i]._start   = start ;
          _sections[i]._length  = - part ;
          start -=  part  ;
       }
       repaint();
    }
    public void paintComponent( Graphics gin ){
    

       Graphics2D g = (Graphics2D) gin ;
       g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                          RenderingHints.VALUE_ANTIALIAS_ON);


       Insets insets = getInsets() ;
       insets.top    += _internalInsets.top ;
       insets.bottom += _internalInsets.bottom ;
       insets.left   += _internalInsets.left ;
       insets.right  += _internalInsets.right ;
       
       g.translate( insets.left , insets.top ) ;
       
       Dimension size = getSize() ;
       
       size.width  -= ( insets.right + insets.left ) ;
       size.height -= ( insets.top   + insets.bottom ) ;
       
       int dim = Math.min( size.height , size.width ) ;
       
        g.setColor( Color.gray ) ;
        g.fillOval(  0 + 3 , 0 - 3 , dim , dim ) ;

        if( _sections == null )return ;

        for( int i = 0 ; i < _sections.length ; i++ ){
            g.setColor( _sections[i]._color ) ;
            g.fillArc( 0, 0 , dim , dim , _sections[i]._start , _sections[i]._length  ) ;
        }      

        drawAgenda( g , dim + 20 , 0 , new Dimension( size.width - dim - 20 , size.height  ) ) ;
        g.setClip( null) ;

        g.translate( -insets.left , -insets.top ) ;

    }
    private void drawAgenda( Graphics g , int x , int y , Dimension size ){

       g.setColor( Color.gray ) ;
       g.fillRect( x + 3 , y - 3, size.width , size.height ) ;
       g.setColor( Color.white ) ;
       g.fillRect( x , y , size.width , size.height ) ;
       g.setClip( x , y , size.width , size.height ) ;

       int diff = size.height / _sections.length ;
       diff = Math.min( diff , 200 ) ;
       int box  = diff / 2 ;
       
       Font thisFont = __defaultFont.deriveFont( (float)( diff - 4 ) ) ;
       g.setFont( thisFont ) ;
       FontMetrics metrics = g.getFontMetrics();
       int fontHeight = metrics.getHeight() ;
       int fontOffset = metrics.getDescent() ;
       
       for( int i = 0 ; i < _sections.length ; i++ ){
       
          g.setColor( _sections[i]._color ) ;
          y += diff ;
          
          g.fillRect( x + box/2 , y - box/2 - box , box , box ) ;
          
          g.setColor( Color.black ) ;
          g.drawString( _sections[i]._title , x + 2 * box , y - fontOffset  ) ; 
       }
    }
}

