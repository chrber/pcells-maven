// $Id: ShadowedPanel.java,v 1.1 2008/08/04 18:46:28 cvs Exp $ 

package org.pcells.services.gui.util ;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager2;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Iterator;
import java.util.LinkedList;


public class ShadowedPanel extends JComponent implements ActionListener{

   /**
     * variables for the layout
     */
   private CardLayout   _cards        = new CardLayout() ;
   private BasicPanel   _basicPanel   = null ;
   private ShadowPanel  _shadowPanel  = null ;
   private JPanel       _switchPanel  = null ;
   private ShadowList   _shadowList   = new ShadowList() ;
   private MouseActions _mouseActions = new MouseActions() ;
   private ShadowLayout _shadowLayout = new ShadowLayout() ;
   private JButton      _switchButton = null ;

   /**
     * status variables.
     */
   private boolean   _regularMode = true ;
   private boolean   _wasClicked  = false ;
   private boolean   _isResizing  = false ;
   private Point     _currentPosition = null ;
   private Point     _wasClickedAt    = new Point(0,0);

   /**
     *  Main Class
     */
   public ShadowedPanel(){
           
       setLayout(new BorderLayout(10,10)) ;
       
      _basicPanel = new BasicPanel() ;
      _basicPanel.addMouseListener( _mouseActions);
      _basicPanel.addMouseMotionListener( _mouseActions);
       
      _shadowPanel = new ShadowPanel() ;
      _shadowPanel.addMouseListener(_mouseActions) ;
      _shadowPanel.addMouseMotionListener(_mouseActions);


      _switchPanel = new JPanel( _cards ) ;
            
      _switchPanel.add( _basicPanel  , "basic" ) ;
      _switchPanel.add( _shadowPanel , "shadow" ) ;
      
      _cards.show( _switchPanel , "shadow" ) ;
      
      add(_switchPanel, "Center" ) ;
      
      _switchButton = new JButton("Switch") ;
      add( _switchButton, "North" ) ;
      
      _switchButton.addActionListener(this);

   }
   public void addOverlayComponent( Component component , String name ){
   
      ComponentShadow shadow = new ComponentShadow( name , component ) ;
      
      _basicPanel.add( component ) ;
      _shadowList.add(shadow);
      
   }
   public void addContainerListener( ContainerListener listener ){
      _basicPanel.addContainerListener(listener);
   }
   public void removeContainerListener( ContainerListener listener ){
      _basicPanel.removeContainerListener(listener);
   }
   public JComponent getMasterComponent(){ return _basicPanel ; }
   public void refreshProperties(){
   }
   private void switchNextPanel(){
      _cards.next(_switchPanel);
   }
   public void actionPerformed(ActionEvent event ){
      _cards.next(_switchPanel);
   }
   /**
     * The real panel
     */
   private class BasicPanel extends JPanel {
      private Font _header = new Font( "Times" , Font.ITALIC , 18 ) ;
      private BasicPanel(){
         setLayout( _shadowLayout ) ;
      }
      public void paintComponent( Graphics g ){
      
         super.paintComponent(g);
	 
	 g.setFont( _header ) ;
	 
	 for( Iterator<ComponentShadow> shadowIt = _shadowList.iterator() ;

	      shadowIt.hasNext() ; ){

	    ComponentShadow shadow = shadowIt.next() ;

	    drawShadowFrame( g , shadow ) ;

	 }
      
      }
      private void drawShadowFrame( Graphics g , ComponentShadow shadow ){
      
         Graphics2D g2 = (Graphics2D) g ;
         g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);
			     
         Rectangle rect = shadow.getRectangle() ;
	 
	 g.setColor( Color.blue ) ;
	 FontMetrics metrics = g.getFontMetrics() ;
	 g.drawString( shadow.getName() , 
	             rect.x + 5 , 
		     rect.y + metrics.getDescent() - metrics.getAscent() ) ;
		     
	 g.drawRect( rect.x -1 , rect.y-1 , rect.width + 1 , rect.height + 1 ) ;
      }
   }
   /**
     * and the shadow panel
     */
   private class ShadowPanel extends JPanel {

      public ShadowPanel(){
         //setOpaque(false);
      }
      public void paintComponent( Graphics g ){

         Dimension d = getSize() ;

	 if( _regularMode ){
	    //
	    // background
	    //
	    g.setColor( Color.green ) ;
	    g.fillRect( 0 , 0 , d.width - 1 , d.height -1 ) ;
	 }
	 //
	 // Are the still members ?
	 //
	 if( _shadowList.size() == 0 )return ;

	 ComponentShadow lastShadow = _shadowList.getLast() ;
         Rectangle       lastRect   = lastShadow.getRectangle() ;

	 if( _regularMode ){
	    //
	    // draw all shadows except for the last one.
	    //
	    //
	    g.setPaintMode();
	    for( Iterator<ComponentShadow> shadowIt = _shadowList.iterator() ;
	    
	         shadowIt.hasNext() ; ){
		 
	       ComponentShadow shadow = shadowIt.next() ;
	       
	       if( shadow != lastShadow )drawShadow( g , shadow ) ;
	       
	    }
	    //
	    // our thing
	    //
	    if( _wasClicked ){
	       //
	       // prepare moving 
	       //
	       if( _isResizing ){
                  _wasClickedAt.x = lastRect.x + lastRect.width  - _wasClickedAt.x ;
                  _wasClickedAt.y = lastRect.y + lastRect.height - _wasClickedAt.y ;
	       }else{
                  _wasClickedAt.x = _wasClickedAt.x - lastRect.x ;
                  _wasClickedAt.y = _wasClickedAt.y - lastRect.y ;
	       }
               g.setXORMode( Color.blue ) ;
	       drawShadow(g,lastShadow);
               g.setPaintMode();

	       _regularMode = false ;
	       _wasClicked  = false ;
	    }else{
	       drawShadow(g, lastShadow );
	    }
	 }else{
	    //
	    // do the moving
	    //
            g.setXORMode( Color.blue ) ;

	    drawShadow(g,lastShadow) ;

	    if( _isResizing ){

	       lastRect.width  = _currentPosition.x - lastRect.x + _wasClickedAt.x ;
	       lastRect.height = _currentPosition.y - lastRect.y + _wasClickedAt.y ;

	       lastRect.width  = Math.max( 40 , lastRect.width );
	       lastRect.height = Math.max( 40 , lastRect.height ) ;
	    }else{
	       lastRect.x = _currentPosition.x - _wasClickedAt.x ;
	       lastRect.y = _currentPosition.y - _wasClickedAt.y ;
            }
	    drawShadow(g,lastShadow);

 	    g.setPaintMode();
	}
     }
     private void drawShadow( Graphics g , ComponentShadow shadow ){

        Graphics2D g2 = (Graphics2D) g ;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        Rectangle rect = shadow.getRectangle() ;

        g.translate( rect.x , rect.y ) ;
	g.setClip( 0 , 0 , rect.width , rect.height ) ;

	shadow.drawComponent( g ) ;	   

	g.translate( -rect.x , -rect.y ) ;
	g.setClip(null);


     }
   }
   /**
     * And the mouse actions.
     */
   public class MouseActions extends MouseAdapter implements MouseMotionListener {

       public void mouseDragged( MouseEvent event ){

	  if( _regularMode )return ;

	  _currentPosition = event.getPoint() ;
	  _shadowPanel.repaint();

       }
       public void mouseMoved( MouseEvent event ){
       }
       public void mousePressed( MouseEvent event ){


	  Point p = event.getPoint() ;

	  if( _shadowList.findInListAndTop( p ) ){

	     ComponentShadow shadow = _shadowList.getLast() ;

             if( shadow.isDestroy(p) ){

		_shadowList.removeLast() ;
		Component component = shadow.getComponent() ;
		_basicPanel.remove( component ) ;

	     }else{

	        _wasClicked   = true ;

                _wasClickedAt = _currentPosition = p ;

		_isResizing   = shadow.isResizing( p  ) ;

	     }

	     _shadowPanel.repaint() ;
	  }else{
             switchNextPanel() ;
          }

       }
       public void mouseReleased( MouseEvent event ){

	  _regularMode = true ;
	  _shadowPanel.repaint() ;

      }

   }
   /**
     * The abstraction of the list of components.
     */
   public class ShadowList {
   
      private LinkedList<ComponentShadow> _shadowList = new LinkedList<ComponentShadow>() ;
      
      public void add( ComponentShadow shadow ){
         _shadowList.add(shadow) ;
      }
      private boolean findInListAndTop( Point p ){

         //for( ComponentShadow shadow : _shadowList.descendingIterator() ){

	 for( int i = 0, n = _shadowList.size() ; i < n ; i ++ ){

	    ComponentShadow shadow = _shadowList.get(n-i-1) ;

	    if( shadow.contains(p) ){
	       _shadowList.remove(shadow) ;
	       _shadowList.add(shadow) ;
	       return true ;
	    }
	 }
         return false ;

      }
      private void removeLast(){
         _shadowList.removeLast() ;
      }
      private ComponentShadow getLast(){ return _shadowList.getLast() ; }
      private int size() { return _shadowList.size() ; }
      public Iterator<ComponentShadow> iterator(){
         return _shadowList.iterator() ;
      }
   }
   /**
     * The companion of the components.
     */
   public class ComponentShadow {

      private Rectangle  _rectangle = new Rectangle() ;
      private String     _name      = null ;
      private Component  _component = null ;
      public ComponentShadow( String name , Component component ){
      
         Dimension ps = (_component = component).getPreferredSize() ;
	 
	 ps.width  = Math.max( ps.width , 200 ) ;
	 ps.height = Math.max( ps.height , 50 ) ;
	 
	 _rectangle = new Rectangle( 0 , 0 , ps.width , ps.height ) ;
	 _name = name ;
      }
      public void synchronize(){
         _component.setLocation( _rectangle.x , _rectangle.y );
	 _component.setSize( _rectangle.width , _rectangle.height );
      }
      public String getName(){ return _name ; }
      public Component getComponent(){ return _component ; }
      public boolean contains( Point p ){
	 return _rectangle.contains(p) ;
      }
      public boolean isResizing( Point p ){
         return 
	    ( p.x > ( _rectangle.x + _rectangle.width  - 20 ) ) &&
	    ( p.y > ( _rectangle.y + _rectangle.height - 20 ) ) ;
      }
      public boolean isDestroy( Point p ){
         return 
	    ( p.x < ( _rectangle.x + 20 ) ) &&
	    ( p.y < ( _rectangle.y + 20 ) ) ;
      }
      public Dimension getSize(){
	 return new Dimension( _rectangle.width , _rectangle.height ) ;
      }
      public void drawComponent( Graphics g ){

	 Rectangle rect = _rectangle ;
 	 g.setColor( Color.yellow);
         g.fillRoundRect( 0 , 0 , rect.width-1 , 20 , 8 , 8 ) ;

	 g.setColor( Color.red);
         g.drawRoundRect( 0 , 0 , rect.width-1 , 20 , 8 , 8 ) ;

         g.drawRoundRect( 0 , 0 , rect.width-1 , rect.height-1 , 8 , 8 ) ;
	 g.drawLine( rect.width-10 , rect.height-1 , rect.width-1, rect.height-10 ) ;
	 g.drawLine( rect.width-15 , rect.height-1 , rect.width-1, rect.height-15 ) ;
	 g.drawLine( rect.width-20 , rect.height-1 , rect.width-1, rect.height-20 ) ;

	 g.drawLine( 0 , 20 , rect.width - 1 , 20 ) ;
	 g.drawLine( 2 , 17 , 17 , 2 ) ;
	 g.drawLine( 2 , 2 , 17 , 17 ) ;

	 g.drawOval( 22 , 3 , 15 , 15 ) ;

	 g.drawString( _name ,  10 , rect.height/2 ) ;
      }
      public Rectangle getRectangle(){ return _rectangle ; }
      public String toString(){ return _name +" "+_rectangle; }
   }
   public class ShadowLayout implements LayoutManager2 {
   
      
      public ShadowLayout(){
      }
      public void addLayoutComponent( String name , Component comp ){
	 //System.out.println("Component added : "+name+" "+comp);
      }
      public void addLayoutComponent(  Component comp , Object constraint ){
	 //System.out.println("Component added : "+comp+" "+constraint);
      }
      public void layoutContainer( Container container ){
      
	 for( Iterator<ComponentShadow> shadowIt = _shadowList.iterator() ;
	      shadowIt.hasNext() ; ){

	    shadowIt.next().synchronize();

	 }

      }
      public Dimension minimumLayoutSize( Container container ){
	return new Dimension(100,100);
      }
      public Dimension maximumLayoutSize( Container container ){
	return new Dimension(100,100);
      }
      public Dimension preferredLayoutSize( Container container ){
	return new Dimension(100,100) ;
      }
      public void invalidateLayout( Container container ){
     }
      public float getLayoutAlignmentX( Container container ){
         return (float)0.5 ;
      }
      public float getLayoutAlignmentY( Container container ){
         return (float)0.5 ;
      }
      public void removeLayoutComponent( Component comp ){
	 System.out.println("Component remove : "+comp);
      }
       }


}

