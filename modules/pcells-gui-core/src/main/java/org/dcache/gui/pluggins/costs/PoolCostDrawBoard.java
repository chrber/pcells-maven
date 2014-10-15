// $Id: PoolCostDrawBoard.java,v 1.1 2008/08/04 19:02:57 cvs Exp $
//
package org.dcache.gui.pluggins.costs;
//

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;


public class PoolCostDrawBoard extends JComponent {

   private float _line          = (float)0.0 ;
   private float _p2pServerLine = (float)0.0 ;
   private float _maxValue      = (float)0.001 ;
   private float _maxScale      = (float)1.0 ;
   private Font  _labelFont     = new Font( "Monospaced" , Font.PLAIN , 12 ) ;
   private ListModel _model     = null ;
   
   private boolean _drawCostByMover = true ;
   private boolean _drawCostDetail  = true ;

   private ArrayList<OurPoolInfo> _poolInfoList = new ArrayList<OurPoolInfo>() ;
   
   private DrawBoardMouseListener _events = new DrawBoardMouseListener() ;
   
   private class OurPoolInfo {
       private OurPoolInfo( ExtendedPoolCostInfo info ){
           this.name = info.getName() ;
	   this.info = info ;
       }
       private void setValue( float [] value ){
          this.value = value ;
       }
       private void calculateCost(){
          this.value = costByMatrix( info.getPoolCostArray() );
       }
       private Rectangle box = null ;
       private String    name ; 
       private boolean   isShowName = false ;
       private float []   value ;
       private ExtendedPoolCostInfo info ;
   }
   private class DrawBoardMouseListener extends MouseAdapter implements ListDataListener {
   
      public void mouseClicked( MouseEvent event ){

	 for( OurPoolInfo info : _poolInfoList ){
	    if( info.box == null )continue ;
	    if( info.box.contains(event.getPoint()) ){
	       if( info.isShowName )info.isShowName = false ;
	       else info.isShowName = true ;
	       break ;
	    }
	 }
	 repaint() ;

      }
      public void contentsChanged( ListDataEvent event ){
      
         ArrayList<OurPoolInfo> poolInfoList = new ArrayList<OurPoolInfo>() ;
	 
         int n = _model.getSize() ;
         for( int i = 0 ; i < n ; i++ ){
	 
	    ExtendedPoolCostInfo info = (ExtendedPoolCostInfo)_model.getElementAt(i);
	    
	    OurPoolInfo intern = new OurPoolInfo(info) ;
	    
	    intern.calculateCost() ;
	    
	    poolInfoList.add( intern ) ;	    
	 }
	 
	 _poolInfoList = poolInfoList ;
	 _maxValue = calculateMaxValue(poolInfoList) ;
	 
         repaint();
      }
      public void intervalAdded( ListDataEvent event ){
        contentsChanged(event);
      }
      public void intervalRemoved( ListDataEvent event ){
        contentsChanged(event);
      }
   }
   public PoolCostDrawBoard(){
      addMouseListener( _events );
   }
   public void setCostByMover( boolean costByMover ){
      _drawCostByMover = costByMover ;
      System.err.println("Cost by mover : "+costByMover);
      repaint();
   }
   public void setCostDetails( boolean costDetail ){
      _drawCostDetail = costDetail ;
      System.err.println("Cost details : "+costDetail);
      repaint();
   }
   public void setModel( ListModel model ){
   
      if( _model != null )_model.removeListDataListener(_events);
      
      _model = model ;
      
      _model.addListDataListener(_events);
   }
   public void setP2p(float line){
      _p2pServerLine = line ;
      repaint() ;
   }
   public void setScale(float line){
      _maxScale = line ;
      repaint() ;
   }
   public void setLine( float line ){

      _line = line ;
      repaint() ;
   }
   private void recalculateCost(){
      for( OurPoolInfo intern : _poolInfoList )intern.calculateCost() ;
   }
   private float calculateMaxValue(ArrayList<OurPoolInfo> poolInfoList ){

      float maxValue = (float)0.001;
      
      for( OurPoolInfo poolInfo : poolInfoList ){

	 if( poolInfo.value != null )maxValue = Math.max( maxValue , poolInfo.value[0] ) ;

      }
      maxValue *= (float)1.2 ;
      return maxValue ;
   }
   public void paintComponent( Graphics gin ){

      Graphics2D g = (Graphics2D) gin ;
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                     RenderingHints.VALUE_ANTIALIAS_ON);

      Dimension d = getSize();

      int bars = _poolInfoList.size() ;
      if( bars == 0 ){
	 g.drawString("No pools in this section",20,d.height/2);
	 return ;
      }

     
      float maxValue = _maxValue * _maxScale ;

      if( maxValue < 0.000000001 )return ;	    

      g.setFont( _labelFont ) ;

      DiagramFormat df = new DiagramFormat(maxValue) ;
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
      }else if( sizeOfBar < 2 ){
	sizeOfBar = 0 ;
	useShadow = false ;
      }else if( sizeOfBar < 4 ){
	useShadow = false ;
      }else{
	sizeOfBar -= 2 ;
      }
      int xPosition = xOff ;
      //
      // drawing begins here.
      //
      g.setColor(Color.white) ;	    
      g.fillRect(0,0,d.width-1,d.height-1);	    

      for( OurPoolInfo poolInfo : _poolInfoList ){

         if( poolInfo.value == null )continue ;
        
	 int barHeight = (int) ( poolInfo.value[0] / maxValue * (float)height) ;

	 int yy = d.height - barHeight - yOff - 1 ;
	 //
	 // store the location for the 'click'
	 //
	 poolInfo.box = new Rectangle(xPosition , yy , sizeOfBar , barHeight);
	 //
	 // draw shadow if needed.
	 //
 	 if( useShadow & ( barHeight > 0 ) ){
            g.setColor(Color.gray);
	    g.fillRect( xPosition + 1 , yy-1 , sizeOfBar , barHeight+1 ) ;
	 } 
	 //
	 // draw the actual bar.
	 //
         g.setColor(Color.lightGray);
	 if( barHeight == 0 ){
             g.setColor(Color.red);
	     barHeight = 1 ;
	 }
	 if( ! _drawCostDetail ){
	    g.fillRect( xPosition , yy , sizeOfBar , barHeight ) ;
	 }else{
	    //
	    // split costs into mover,p2p,p2pclient etc
	    //
	    int y1 = yy ;
	    for( int i = poolInfo.value.length -1 ; i > 0 ; i-- ){
	       g.setColor( i % 2 == 0 ? Color.red : getColorByQueue(i/2) ) ;
	       barHeight = (int) ( poolInfo.value[i] / maxValue * (float)height) ;
	       g.fillRect( xPosition , y1 , sizeOfBar , barHeight ) ;
	       y1 += barHeight ;
	    }

	 } 

	 if( useShadow )xPosition += 2 ;
	 xPosition += sizeOfBar ;
      }
      g.setColor(Color.darkGray);
      for( OurPoolInfo poolInfo : _poolInfoList ){
         //
	 //  draw the name if requested.
	 //
	 if( poolInfo.isShowName && ( poolInfo.box != null ) && ( poolInfo.box.height > 0)){
	    g.drawString( poolInfo.name , poolInfo.box.x , poolInfo.box.y - 4 ) ;
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
   private float [] costByMatrix( long [] [] cost){
      if( _drawCostByMover ){
	 return complexMoverCostByMatrix( cost ) ;
      }else{
         return complexCostByMatrix( cost ) ;
      }
   }
   private float simpleCostByMatrix( long [] [] cost){
       float avarage   = (float)0.0 ;
       int costCounter = 0 ;
       for( int i = 0 ; i < cost.length ; i++ ){
          long  [] x = cost[i] ;
	  if( x[1] == 0 )continue ;
          avarage += (float) (x[0]+x[2]) / (float)x[1] ;
	  costCounter ++ ;
       }
       avarage = avarage / (float)costCounter ;
       
       return avarage ;
   
   }
   private float [] complexCostByMatrix( long [] [] cost){
       float avarage   = (float)0.0 ;
       int costCounter = 0 ;
       float [] result = new float[cost.length*2+1] ;
       
       for( int i = 0 ; i < cost.length ; i++ ){
        
          long  [] x = cost[i] ;
	  if( x[1] == 0 )continue ;
	  
          avarage += (float) (x[0]+x[2]) / (float)x[1] ;
	  result[1+i*2]   = (float)x[0] / (float)x[1] ;
	  result[1+i*2+1] = (float)x[2] / (float)x[1] ;
	  
	  costCounter ++ ;
       }
       result[0] = avarage ;
       for( int i = 0 ; i < result.length ; i++ )result[i] = result[i]/(float)costCounter;

       return result ;
   
   }
   private float [] complexMoverCostByMatrix( long [] [] cost){
       float avarage   = (float)0.0 ;
       float [] result = new float[cost.length*2+1] ;
       
       for( int i = 0 ; i < cost.length ; i++ ){
        
          long  [] x = cost[i] ;
	  if( x[1] == 0 )continue ;
	  
          avarage += (float) (x[0]+x[2]) ;
	  result[1+i*2]   = (float)x[0]  ;
	  result[1+i*2+1] = (float)x[2]  ;
	  
       }
       result[0] = avarage ;

       return result ;
   
   }
   private Color getColorByQueue( int queueIndex ){
      switch( queueIndex ){
         case 0 : return Color.orange ; // Store Queue 
	 case 1 : return Color.yellow ; // restore Queue
	 case 2 : return Color.blue ;   // p2p server queue
	 case 3 : return Color.pink ;   // p2p client queue
	 default : return Color.green ; // reqular mover queues
      }
   }

}
