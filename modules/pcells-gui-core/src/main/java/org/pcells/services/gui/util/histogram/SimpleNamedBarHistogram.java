// $Id: SimpleNamedBarHistogram.java,v 1.1 2008/08/04 18:38:59 cvs Exp $

package org.pcells.services.gui.util.histogram ;

import javax.swing.JComponent;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;


public class SimpleNamedBarHistogram 
       extends JComponent {
       
       
   private BasicHistogramModel     _model  = null ;
   private ArrayList<NamedBarInfo> _bins   = new ArrayList<NamedBarInfo>();
   private HistogramMouseListener  _events = new HistogramMouseListener() ;

   private float _line          = (float)0.0 ;
   private float _p2pServerLine = (float)0.0 ;
   private float _maxValue      = (float)0.001 ;
   private float _maxScale      = (float)1.0 ;
   private Font  _labelFont     = new Font( "Monospaced" , Font.PLAIN , 12 ) ;
   
   private int _drawArrayIndex0  = 0 ;
   private int _drawArrayIndex1  = 0 ;
   
   //
   // Layout steering parameters.
   //
   private boolean _splitBars = false ;
   
   private class NamedBarInfo {

       private Rectangle box = null ;
       private String    name ; 
       private boolean   isShowName = false ;
       private float []  value ;

       private NamedBarInfo( String name , float [] value ){
           this.name  = name ;
	   this.value = value ;
       }
       private void setValue( float [] value ){
	  //
	  // WRONG : needs to be arraycopy
	  //
          this.value = value ;
       }
   }

   private class HistogramMouseListener extends MouseAdapter implements HistogramListener {
   
      public void mouseClicked( MouseEvent event ){

	 for( NamedBarInfo info : _bins ){
	    if( info.box == null )continue ;
	    if( info.box.contains(event.getPoint()) ){
	       if( info.isShowName )info.isShowName = false ;
	       else info.isShowName = true ;
	       break ;
	    }
	 }
	 repaint() ;

      }
      public void histogramContentsChanged( HistogramEvent event ){
         
	 histogramValuesChanged( (BasicHistogramModel)event.getSource() , event ) ;
	 
	 calculateMaxValue() ;
	 
	 repaint() ;
	 
      }
      public void histogramStructureChanged( HistogramEvent event ){

         BasicHistogramModel model = (BasicHistogramModel)event.getSource() ;
	 
	 changeParameter( model , event ) ;
	 
	 histogramLayoutChanged( model , event ) ;
	 
	 // is incuded : histogramValuesChanged( model , event ) ;
	 
	 calculateMaxValue() ;
	 
	 repaint() ;
      }

      public void histogramParametersChanged( HistogramEvent event ){
      
         changeParameter( (BasicHistogramModel)event.getSource() , event ) ;
	 
	 calculateMaxValue() ; // range could have changed
	 
	 repaint() ;
      }
      private void changeParameter( BasicHistogramModel model , HistogramEvent event ){
	 //
	 // get and set the parameter.
	 //
         int mode         = (Integer)model.getParameterAt(0) ;
	 
         _drawArrayIndex0 = (Integer)model.getParameterAt(1) ;
         _drawArrayIndex1 = (Integer)model.getParameterAt(2) ;
	 
	 _splitBars = ( mode & 0x1 ) != 0 ;
	 
	 _maxScale = (Float)model.getParameterAt(3) ;
      }
      private void histogramValuesChanged( BasicHistogramModel model , HistogramEvent event ){
      
	 int n = model.getDataCount() ;
	 
	 if( n != _bins.size() ){
	    System.err.println("Problem : Provided number of values differ from existing ones");
	    return ;
	 }

	 for( int i = 0 ; i < n ; i++ )_bins.get(i).setValue( model.getDataAt(i) ) ;

      }
      private void histogramLayoutChanged( BasicHistogramModel model , HistogramEvent event ){      
	 //
	 //  build the info list.
	 //
	 _bins.clear() ;

	 for( int i = 0 , n = model.getDataCount() ; i < n ; i++ ){
	 	    
	    _bins.add( new NamedBarInfo( model.getNameAt(i) , model.getDataAt(i) ) ) ;

	 }

      }
      private void calculateMaxValue(){
         if( _splitBars )calculateMaxValueSplit() ;
	 else calculateMaxValueRegular() ;
      }
      private void calculateMaxValueRegular(){
      
         float maxValue = (float)0.001 ;
	 
	 if( _bins.size() == 0 )return ;
	 
         for( int i = 0 , n = _bins.size() ; i < n ; i++ ){
	  
	     float [] value = _bins.get(i).value ;
	     if( value == null )continue ;
	     
	     float sum = (float)0.0 ;
	     
	     for( int j = _drawArrayIndex0 ; j <= _drawArrayIndex1 ; j++ )sum+=value[j] ;
	     
	     maxValue = Math.max( maxValue , sum ) ;
	 }
	 _maxValue = maxValue * (float)1.2 ;
      }
    
      private void calculateMaxValueSplit(){
      
         float maxValue = (float)0.001 ;
	 
	 if( _bins.size() == 0 )return ;
	 
         for( int i = 0 , n = _bins.size() ; i < n ; i++ ){
	  
	     float [] value = _bins.get(i).value ;
	     if( value == null )continue ;
	     
	     float sum = Math.max( value[_drawArrayIndex0] , value[_drawArrayIndex1]) ;
	     
	     maxValue = Math.max( maxValue , sum ) ;
	 }
	 _maxValue = maxValue * (float)1.2 ;
      }
    }

    public SimpleNamedBarHistogram(){
       addMouseListener( _events );
    }
     
    public void setModel( BasicHistogramModel model ){
       if( _model != null )_model.removeHistogramListener(_events) ;
       _model = model ;
       _model.addHistogramListener(_events) ;
    }
    public void paintComponent( Graphics gin ){

       Graphics2D g = (Graphics2D) gin ;
       g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                      RenderingHints.VALUE_ANTIALIAS_ON);

       Dimension d = getSize();

       int bars = _bins.size() ;
       if( bars == 0 ){
	  g.drawString("No pools in this section",20,d.height/2);
	  return ;
       }


       float maxValue = _maxValue * _maxScale ;

       if( maxValue < 0.000000001 )return ;	    

       g.setFont( _labelFont ) ;

       AxisScale        df = new AxisScale(maxValue) ;
       int intervalCount   = df.getIntervalCount() ;
       FontMetrics metrics = g.getFontMetrics() ;

       int maxString = 0 ;
       for( int i= 0 ; i < (intervalCount+1) ; i++ ){
	  maxString = Math.max( maxString , metrics.stringWidth(df.getValueLabelAt(i)));
       }

       int xOff   = 5 + maxString ;
       int yOff   = 10 ; 
       int width  = d.width  - 30 - xOff ;
       int height = d.height - 30 ;

       int     sizeOfBar = ( width - 4 ) / bars ;
       boolean useShadow = true ;

       if( sizeOfBar > 20 ){
	 sizeOfBar = 20 ;
       }else if( ( sizeOfBar < 2 ) || ( _splitBars && (sizeOfBar < 4) ) ){
	 sizeOfBar = 0 ;
	 useShadow = false ;
       }else if( sizeOfBar < 4 ){
	 useShadow = false ;
       }else{
	 sizeOfBar -= 2 ;
       }
       //if( _splitBars )useShadow = false ;
       //
       // histgram doesn't fit.
       //
       if( sizeOfBar == 0 ){
	  g.drawString("Histogram doesn't fit.",20,d.height/2);
	  return ;
       }
       
       int xPosition = xOff ;
       //
       // drawing begins here.
       //
       g.setColor(Color.white) ;	    
       g.fillRect(0,0,d.width-1,d.height-1);	    

       for( NamedBarInfo barInfo : _bins ){

          if( barInfo.value == null )continue ;

          int barHeight  = 0 ;
	  int barHeight2 = 0 ;
          float valueSum = (float)0.0 ;
	  if( _splitBars ){
	     barHeight  = (int) ( barInfo.value[_drawArrayIndex0] / maxValue * (float)height) ;
	     barHeight2 = (int) ( barInfo.value[_drawArrayIndex1] / maxValue * (float)height) ;
	  }else{		  
	     for( int i = _drawArrayIndex0  ; i <= _drawArrayIndex1 ; i++ ){
	        valueSum += barInfo.value[i] ;
	     }
	     barHeight = (int) ( valueSum / maxValue * (float)height) ;
	  }
	  int yy = d.height - barHeight  - yOff + 1 ;
	  int yz = d.height - barHeight2 - yOff + 1 ;
	  //
	  // store the location for the 'click'
	  //
	  barInfo.box = 
	      new Rectangle(xPosition , Math.min(yy,yz) , 
	                    sizeOfBar , Math.max(barHeight,barHeight2)
			   );
	  //
	  // draw shadow if needed.
	  //
 	  if( useShadow & ( barHeight > 0 ) ){
             g.setColor(Color.gray);
	     if( _splitBars ){
	        g.fillRect( xPosition + 1 , yy-1 , sizeOfBar/2 , barHeight+1 ) ;
	        g.fillRect( xPosition + 1 + sizeOfBar/2, yz-1 , sizeOfBar/2 , barHeight2+1 ) ;
	     }else{
	        g.fillRect( xPosition + 1 , yy-1 , sizeOfBar , barHeight+1 ) ;
             }
	  } 
	  //
	  // draw the actual bar.
	  //
	  if( barHeight == 0 ){
	     g.setColor(Color.red);
             g.fillRect( xPosition , yy-2 , sizeOfBar , 1 ) ;
	  }else if( _splitBars ){
	  
	     int sob = sizeOfBar/2 ;
	     int i   = _drawArrayIndex0 ;
	     int xP  = xPosition ;
	     
             g.setColor( getColorAt(i) ) ;
             barHeight = (int) ( barInfo.value[i] / maxValue * (float)height) ;
	     g.fillRect( xP , yy , sob , barHeight ) ;
	     
	     xP += sob ;
	     //sob = sizeOfBar - sob ;
	     i = _drawArrayIndex1 ;
             g.setColor( getColorAt(i) ) ;
             barHeight = (int) ( barInfo.value[i] / maxValue * (float)height) ;
	     g.fillRect( xP , yz , sob , barHeight ) ;
	     
	  }else{
	     for( int i  = _drawArrayIndex1 , y1 = yy ; i >= _drawArrayIndex0 ; i-- ){
		g.setColor( getColorAt(i) ) ;
		barHeight = (int) ( barInfo.value[i] / maxValue * (float)height) ;
		g.fillRect( xPosition , y1 , sizeOfBar , barHeight ) ;
		y1 += barHeight ;
	     }
          }

	  if( useShadow )xPosition += 2 ;
	  xPosition += sizeOfBar ;
       }
       g.setColor(Color.darkGray);
       for( NamedBarInfo barInfo : _bins ){
          //
	  //  draw the name if requested.
	  //
	  if( barInfo.isShowName && ( barInfo.box != null ) && ( barInfo.box.height > 0)){
	     g.drawString( barInfo.name , barInfo.box.x , barInfo.box.y - 4 ) ;
	  }
       }
       g.setColor(Color.blue);

       //int barHeight = (int) ( _p2pServerLine / maxValue * (float)height) ;
       //int y = d.height - barHeight -yOff - 1 ;
       //g.drawLine( xOff , y , xOff + width , y ) ;

       g.setColor(Color.blue) ;	    
       g.drawRect(xOff,d.height-height-yOff,width,height);

       g.setColor(Color.red);
       for( int i = 0 ; i < (intervalCount+1) ; i++ ){
	  double v = df.getValueAt(i) ;
	  String label = df.getValueLabelAt(i) ;
	  int yyy = d.height - (int)(v / maxValue * (float)height ) - yOff ;
	  g.drawLine( xOff - 4 , yyy , xOff + 4 , yyy  ) ;
	  g.drawString( label , xOff - metrics.stringWidth(label) - 4 - 2 , yyy ) ;
       }
    }
    private Color [] _colorSchema = {
        Color.lightGray ,Color.lightGray ,
	Color.orange , Color.red ,
	Color.yellow, Color.red ,
	Color.blue , Color.red ,
	Color.pink , Color.red ,
	Color.green , Color.red 
    };
    private Color getColorAt( int queueIndex ){
       queueIndex = queueIndex % _colorSchema.length ;
       return _colorSchema[queueIndex] ;
    }




} 
