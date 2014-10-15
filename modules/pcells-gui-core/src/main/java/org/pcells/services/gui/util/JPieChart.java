package org.pcells.services.gui.util ;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;

public class JPieChart extends JComponent  implements ListDataListener {

    private static Font   __defaultFont   = new Font( "SansSerif" , Font.PLAIN  , 20 ) ;
    private Insets        _internalInsets = new Insets( 10 , 10 , 10 , 10 ) ;
    private PieChartModel _pieChartModel  = new DefaultPieChartModel() ;
    private boolean       _modelValid     = false ;
    private Section []    _sections       = null ;
    private boolean       _drawLegend     = false ;
    private int           _preferredWidth = 0 ;    
    public JPieChart(){
      // setBorder( BorderFactory.createTitledBorder( BorderFactory.createLineBorder( Color.blue , 30 ) , "Hallo" ) ) ;
//      _pieChartModel.addElement( new Object() );
       if( _pieChartModel != null )_pieChartModel.addListDataListener( this ) ;
    }
    public void setModel( PieChartModel model ){ 
       if( _pieChartModel != null )_pieChartModel.removeListDataListener( this ) ;
       _pieChartModel = model ; 
       if( _pieChartModel != null )_pieChartModel.addListDataListener( this ) ;
    }
    public PieChartModel getModel(){ return _pieChartModel ; }

    private class Section {
       private PieChartModel.PieChartItem _item   = null ;
       private int    _start   = 0 ;
       private int    _length  = 0 ;
       private Color  _color   = null ;
       private long   _amount  = 0L ;
       private Section( PieChartModel.PieChartItem item ){
          _item   = item ;
          _color  = _item.getColor() ;
          _amount = _item.getLongValue() ;
       }
    }
    public void contentsChanged( ListDataEvent event ){
       System.out.println("Content Changed : "+event ) ;
       updateValues();
       repaint() ;
    }
    public void intervalAdded( ListDataEvent event ){
      // intervalRemoved( event ) ;
    }
    public void intervalRemoved( ListDataEvent event ){
       System.out.println("intervalChanged : "+event ) ;
       _modelValid  = false ;
       repaint();
    }
    private void updateValues(){

       if( _pieChartModel == null )return ;
       int  size  = _pieChartModel.getSize() ;
    
       if( ( _sections == null ) ||
           ( _sections.length  != size ) ){
           
            validateModel() ;
            return ;
       }
       //
       // could be more sofisticated ..
       //
       validateModel() ;
    }
    private void validateModel(){
    
       if( _pieChartModel == null )return ;
       int  size  = _pieChartModel.getSize() ;
       
       long total = 0L ;
       
       double [] fraction  = new double[size] ;
        
       _sections = new Section[size] ;
                 
       for( int i = 0 ; i  < size ; i++ ){
           Object obj = _pieChartModel.getElementAt(i) ;
           if( ! ( obj instanceof PieChartModel.PieChartItem ) )continue ;
           PieChartModel.PieChartItem item = (PieChartModel.PieChartItem)obj ;         
           total += item.getLongValue() ;
           _sections[i] = new Section( item ) ;
       }
       double dtotal = (double)total;

       int start =  90 ;
       for( int i = 0 ; i  < _sections.length ; i++ ){
          int part = (int) ( (double)_sections[i]._amount / dtotal * (double)360.0 )  ;
          _sections[i]._start   = start ;
          _sections[i]._length  = - part ;
          start -=  part  ;
       }
       _modelValid = true ;
    }
//    public Dimension getPreferredSize(){ return new Dimension( _preferredWidth  , 0 ) ; }
    public void paintComponent( Graphics gin ){
    
       if( ! _modelValid )validateModel() ;
       
       Graphics2D g = (Graphics2D) gin ;
       g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                          RenderingHints.VALUE_ANTIALIAS_ON);


       Insets insets = getInsets() ;
       insets.top    += _internalInsets.top ;
       insets.bottom += _internalInsets.bottom ;
       insets.left   += _internalInsets.left ;
       insets.right  += _internalInsets.right ;

       Dimension size = getSize() ;
       _preferredWidth = size.height ;
       
       g.setColor( getBackground() ) ;
       g.fillRect( 0 , 0 , size.width , size.height ) ;
       
       g.translate( insets.left , insets.top ) ;
       
       
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

        if( _drawLegend ){
           drawAgenda( g , dim + 20 , 0 , new Dimension( size.width - dim - 20 , size.height  ) ) ;
           g.setClip( null) ;
        }
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
          g.drawString( _sections[i]._item.toString() , x + 2 * box , y - fontOffset  ) ; 
       }
    }
}

