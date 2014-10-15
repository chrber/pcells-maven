// $Id: JTopologyPane.java,v 1.2 2004/06/21 22:30:27 cvs Exp $
//
package org.pcells.services.gui ;
//

import dmg.cells.network.CellDomainNode;
import dmg.cells.nucleus.CellDomainInfo;
import dmg.cells.nucleus.CellTunnelInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;

public class       JTopologyPane 
        extends    JComponent 
        implements MouseListener{
        
   private String    _message  = "Not yet initialized";
   private Font      _bigFont  = new Font( "Times" , Font.BOLD | Font.ITALIC , 26 ) ;
   private Font      _niceFont = new Font( "SansSerif" , Font.BOLD , 14 ) ;
   private Topology  _topology = null ;
   private boolean   _drawIcon = false ;
   
   public JTopologyPane(){
        addMouseListener(this);
   }
   private class CellIcon implements Icon {
      private int _height = 0 ;
      private int _width  = 0 ;
      private Color _color  = new Color( 0 , 0 , 255 ) ;
      private CellIcon( int width , int height ){
         _height = height ;
         _width  = width ;
      }
      private CellIcon( int width , int height , Color color ){
         _height = height ;
         _width  = width ;
         if( color != null )_color  = color ;
      }
      public void paintIcon( Component c , Graphics gin , int xi , int yi ){
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
   public void paintComponent( Graphics gin ){
       Graphics2D g = (Graphics2D) gin ;
       g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                          RenderingHints.VALUE_ANTIALIAS_ON);

      Topology topo = null ;

      if( _message != null ){

         drawWaiting( g , _message ) ;
         return ;

      }else if( ( topo = _topology ) == null ){
         drawWaiting( g , "Waiting" ) ;
         return ;
      }else{
         Dimension d = getSize() ;
//         g.setColor(new Color(255,230,130));
         g.setColor( Color.black );
         g.fill3DRect( 0 , 0 , d.width-1 , d.height-1 , false ) ;
         drawTopology( g , topo );
      }
      /*
      Dimension d = getSize() ;
      g.setColor(Color.blue);
      g.drawRect(0,0,d.width-1,d.height-1);
      g.drawLine( 0 , d.height / 2 , d.width-1 , d.height / 2  ) ;  
      g.drawLine( d.width / 2 , 0 , d.width / 2 , d.height-1  ) ;  
      drawDomain( g , d.width / 2 ,d.height / 2 , "DomainName" ) ;

      */
   }
   private void drawTopology( Graphics2D g , Topology t ){
       Dimension d = getSize() ;
       if( ( t.validatedDimension == null ) || 
            ! t.validatedDimension.equals(d)   ){
          //
          // recalculate
          //
          t.validatedDimension = d ;
          Frame [] frame = t.frame ;
          g.setFont( _niceFont ) ;
          FontMetrics fm = g.getFontMetrics() ;
          int height = fm.getAscent() - fm.getDescent() ;
          int width  = 0;
          for( int i = 0 ; i < frame.length ; i++ ){

             Frame f = frame[i] ;

             f.x = (int)(( ( ( f.fx * 0.9 ) + 1.0 ) / 2.0  ) * (double)d.width  );
             f.y = (int)(( ( 1.0 - f.fy * 0.9 ) / 2.0 ) * (double)d.height );

             int fontWidth = fm.stringWidth(f.node.getName()) ;
             width = Math.max( width , fontWidth ) ;

             f.fontDimension = new Dimension( fontWidth , height ) ;
          }
          width += 12 ;
          height+= 12 ; 
          for( int i = 0 ; i < frame.length ; i++ ){

             Frame f = frame[i] ;

             f.rectangle = new Rectangle( f.x - width/2 , f.y - height/2 , width , height ) ; 
          }
          t.validated = true ;
       }

       Frame [] frame = t.frame ;
       int [] [] link = t.reduced ;
       g.setColor(Color.blue);
       for( int i = 0 ; i < link.length ; i++ ){
           g.drawLine( frame[link[i][0]].x , frame[link[i][0]].y ,
                       frame[link[i][1]].x , frame[link[i][1]].y   );
       }
       g.setFont( _niceFont ) ;
       for( int i = 0 ; i < frame.length ; i++ ){

          renderFrame3( g , frame[i] ) ;
       }
   }
   private void renderFrame1( Graphics2D g , Frame f ){
      Rectangle r = f.rectangle ;
      Color back = getBackground();
      g.setColor(back);
      g.fillRect( r.x , r.y , r.width - 1 , r.height - 1 ) ;
      g.setColor( back.brighter() ) ;
      g.drawLine( r.x , r.y , r.x + r.width - 1 , r.y ) ;
      g.drawLine( r.x , r.y , r.x  , r.y + r.height - 1) ;
      g.setColor( back.darker() ) ;
      g.drawLine( r.x + r.width - 1 , r.y , r.x + r.width - 1 , r.y + r.height - 1 ) ;
      g.drawLine( r.x + r.width - 1 , r.y + r.height - 1 , r.x  , r.y + r.height - 1) ;
      g.setColor( Color.red ) ;
      g.drawString( f.node.getName() , f.x - f.fontDimension.width/2 ,
                                       f.y + f.fontDimension.height/2 );
   }
   private void renderFrame2( Graphics2D g , Frame f ){
      Rectangle r = f.rectangle ;
      Color back = Color.green;
      g.setColor(back);
      g.fillRect( r.x , r.y , r.width - 1 , r.height - 1 ) ;
      g.setColor( back.brighter() ) ;
      g.drawLine( r.x , r.y , r.x + r.width - 1 , r.y ) ;
      g.drawLine( r.x , r.y , r.x  , r.y + r.height - 1) ;
      g.setColor( back.darker() ) ;
      g.drawLine( r.x + r.width - 1 , r.y , r.x + r.width - 1 , r.y + r.height - 1 ) ;
      g.drawLine( r.x + r.width - 1 , r.y + r.height - 1 , r.x  , r.y + r.height - 1) ;
      g.setColor( Color.red ) ;
      g.drawString( f.node.getName() , f.x - f.fontDimension.width/2 ,
                                       f.y + f.fontDimension.height/2 );
   }
   private void renderFrame3( Graphics2D g , Frame f ){
      Rectangle r = f.rectangle ;
      Color back = Color.blue;
      g.setColor(back );
      g.fill3DRect( r.x , r.y , r.width - 1 , r.height - 1, true ) ;
      g.setColor( Color.white ) ;
      g.drawString( f.node.getName() , f.x - f.fontDimension.width/2 ,
                                       f.y + f.fontDimension.height/2 );
   }
  private class Topology {
      private Frame [] frame ;
      private int [][] link , reduced ;
      private boolean validated = false ;
      private Dimension validatedDimension = null ;
      private Topology( Frame [] frame , int [] [] link , int [] [] reduced ){
        this.frame = frame ;
        this.link  = link ;
        this.reduced = reduced ;
      }
      public String toString(){
         StringBuffer sb = new StringBuffer() ;
         sb.append(" /////////// Topology //////////////\n");
         for( int i = 0 ; i < frame.length ; i++ ){
            sb.append( "frame["+i+"] "+frame[i]+"\n") ;
         }
         for( int i = 0 ; i < reduced.length ; i++ ){
           int [] x = reduced[i] ;
           sb.append( "link["+i+"] "+x[0]+" <> "+x[1]+"\n" );
         }
         return sb.toString();
      }
  }
   private class Frame {
      public Frame( CellDomainNode node ){ this.node = node ; }
      private CellDomainNode node = null ;
      private int link      = 0 ;
      private int linkCount = 0 ;
      private double fx = 0.0 , fy = 0.0 ;
      private int     x = 0   , y  = 0 ;
      private Rectangle rectangle     = null ;
      private Dimension fontDimension = null ;
      public String toString(){
         StringBuffer sb = new StringBuffer() ;
         sb.append(node.getName()).
            append(" link=("+link+","+linkCount+")").
            append(" cord=("+x+","+y+") ("+fx+","+fy+")").
            append(" rec="+(rectangle==null?"?":rectangle.toString())).
            append(" fd="+(fontDimension==null?"?":fontDimension.toString())).
            append("\n");
        return sb.toString();
      }
   }
   public void setTopology( java.util.List list ){
      _message = null ;
      HashMap map = new HashMap() ;
      Frame [] frame = new Frame[list.size()];
      for( int i = 0 , n = frame.length ; i < n ; i++){
         frame[i]  = new Frame( (CellDomainNode)list.get(i));
         map.put( frame[i].node.getName() , new Integer(i) );
      }
      ArrayList linkarray = new ArrayList();
      for( int i = 0 , n = frame.length , linkPosition = 0 ; i < n ; i++){
         Frame f = frame[i] ;
         f.link = linkPosition ;
         CellTunnelInfo [] tunnel = (CellTunnelInfo[])f.node.getLinks();
         if( tunnel == null )continue ;
         for( int j = 0 , m = tunnel.length ; j < m ; j++ ){
            CellTunnelInfo t = tunnel[j] ;
            CellDomainInfo remote = t.getRemoteCellDomainInfo() ;
            if( remote == null )continue ;
            Integer x = (Integer)map.get(remote.getCellDomainName());
            if( x == null )continue ;
            int [] a = new int[2] ;
            a[0] = i ;
            a[1] = x.intValue() ;
            linkarray.add( a ) ;
            f.linkCount ++ ;
            linkPosition ++ ;
         } 
      }
      int [][] links = (int [][])linkarray.toArray( new int[0][] ) ;
      Topology t = new Topology( frame , links , reduceLinks( links ) ) ;
      _topology = createTopology( t ) ;
      repaint();
   }
   private int [] [] reduceLinks( int [] [] links ){
      ArrayList list = new ArrayList() ;
      for( int i = 0 , n = links.length ; i < n ; i++ ){
         int [] x = links[i] ;
         if( x[0] < x[1] )list.add( links[i] ) ;
      }
      return (int [][])list.toArray( new int[0][] ) ;
   }
   private Topology createTopology( Topology topology ){
     int frameCount = topology.frame.length ;
     if( frameCount < 2 ){
        return topology ;
     }else if( frameCount == 2 ){        
        topology.frame[0].fx = 0.0 ;
        topology.frame[0].fy = 0.5 ;
        topology.frame[1].fx = 0.0 ;
        topology.frame[1].fy = -1.0 ;
     }else if( frameCount == 3 ){
        topology.frame[0].fx = 0.0 ;
        topology.frame[0].fy = 0.5 ;
        topology.frame[1].fx = -0.5 ;
        topology.frame[1].fy = -0.5 ;
        topology.frame[2].fx =  0.5 ;
        topology.frame[2].fy = -0.5 ;
     }else{
        //
        // who  has highest link count
        //
        int maximumLinks = 0 ;
        int highScore    = 0 ;
        for( int i= 0 , n = topology.frame.length ; i < n ; i++ ){
           Frame f = topology.frame[i] ;
           if( f.linkCount > maximumLinks ){
               maximumLinks = f.linkCount ;
               highScore    = i ;
           }
        }
        if( maximumLinks < 3 ){
           //
           // not really a star
           //
           int [] array = new int[topology.frame.length] ;
           for( int i= 0 , n = array.length ; i < n ; i++ ){
              array[i] = i ;
           }
           prepareCircle( topology , array ) ;

        }else{

           //
           // at least somehow star like
           //
           topology.frame[highScore].fx = 0.0 ;
           topology.frame[highScore].fy = 0.0 ;
           int [] array = new int[topology.frame.length-1] ;
           for( int i= 0 , n = topology.frame.length , c = 0 ; i < n ; i++ ){
              if( i != highScore )array[c++] = i ;
           }
           prepareCircle( topology , array ) ;

        }
     }
     return topology ;
   }
   private void prepareCircle( Topology t , int [] a ){
      double step   = Math.PI * 2.0 / (double)a.length ;
      double factor = 0.75 ;
      double angle  = Math.PI * 0.25 ;
      for( int i = 0 , n = a.length ; i < n ; i++ ){

         Frame f = t.frame[a[i]] ;
         f.fx = factor * Math.cos( angle ) ; 
         f.fy = factor * Math.sin( angle ) ; 
         angle += step ;

      }
   }
   public void setMessage( String message ){
      _message = message ;
      repaint() ;
   }
   private void drawWaiting( Graphics g , String message ){

      Dimension d = getSize() ;
      
      g.setColor( Color.black ) ;
      
      Icon icon = new CellIcon( d.width , d.height ) ;
      icon.paintIcon( this , g , 0 , 0 );

      setFont(_bigFont);
      
      FontMetrics fm = g.getFontMetrics() ;
      int stringWidth = fm.stringWidth(message);
      int height = fm.getAscent() - fm.getDescent() ;

      g.setColor( Color.red ) ;
      g.drawString(message, d.width/2 - stringWidth/2 , d.height/2 + ( height / 2 ) );

   }
   private void drawDomain( Graphics g , int x , int y , String domainName ){
      FontMetrics fm = g.getFontMetrics() ;
      int stringWidth = fm.stringWidth(domainName);
      int height = fm.getAscent() - fm.getDescent() ;

      int w = stringWidth + 8 ;
      int h = height + 8 ;



      g.fill3DRect( x - w / 2 , y - h / 2 , w  , h  , true ) ; 

      Color col = g.getColor() ;
      g.setColor( Color.white ) ;                        
      g.drawString(domainName, x - stringWidth/2 , y + ( height / 2 ) );
      g.setColor(col);               

   }
   private ActionListener _actionListener = null;

   public synchronized void addActionListener(ActionListener l) {
      _actionListener = AWTEventMulticaster.add( _actionListener, l);
   }
   public synchronized void removeActionListener(ActionListener l) {
      _actionListener = AWTEventMulticaster.remove( _actionListener, l);
   }
   public void processEvent( ActionEvent e) {
      if( _actionListener != null)
        _actionListener.actionPerformed( e );
   }
   public class TopologyEvent extends ActionEvent {
      private MouseEvent _mouseEvent  = null ;
      private String     _domainName  = null ;
      private TopologyEvent( Object source , String command ,
                             String domain , MouseEvent mouseEvent ){
         super( source , 0 , command ) ;
         _domainName  = domain ;
         _mouseEvent  = mouseEvent ;
      }
      public MouseEvent getMouseEvent(){
        return _mouseEvent ;
      }
      public String getDomainName(){
         return _domainName;
      }
      public String toString(){
        return super.toString()+" dn="+_domainName+" me="+_mouseEvent;
      }
   }
   public void mouseClicked( MouseEvent event ){
      TopologyEvent te = null ; 
//      System.out.println("JTopo : "+event);
      Topology t = _topology ;
      
      if( ( t == null ) || ! t.validated ){
         te = new TopologyEvent(this,"invalid",null,event);
      }else{
         Frame [] f = t.frame ;
         for( int i = 0 , n = f.length ; i < n ; i++ ){
            if( f[i].rectangle.contains(event.getPoint()) ){
               String domain = f[i].node.getName() ;
               te = new TopologyEvent(this,"domain",domain,event);
               break ;
            }
         }
         if( te == null )te = new TopologyEvent(this,"background",null,event);
      }
      processEvent(  te  );
   }
   public void mouseEntered( MouseEvent event ){
   }
   public void mouseExited( MouseEvent event ){
   }
   public void mousePressed( MouseEvent event ){
   }
   public void mouseReleased( MouseEvent event ){
   }

}
