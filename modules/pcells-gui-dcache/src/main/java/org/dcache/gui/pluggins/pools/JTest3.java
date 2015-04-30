 

package org.dcache.gui.pluggins.pools ;

import org.pcells.services.gui.util.ShadowedPanel;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.LayoutManager2;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.LinkedList;


/**
  *  Always look on the bright side of life
  *  always look on the light side of life,
  *  if life seems jolly rotten,
  *  there's something you've forgotten,
  *  and that's to laught and smile and dance and sing,
  *  when you're feeling in the dumps
  *  don't be a silly chumps,
  *  just purse your lips and whistle, that's the thing
  *  and always look on the bright side of life
  *  always look on the right side of life
  *  for life is quite absurd,
  *  and death's the final word
  *  you must always face the curtain with a bow,
  *  forget about your sin, give the audience a grin
  *  enjoy it, it's your last change anyhow,
  *  so always look on the bright side of death,
  *  just before you draw your terminal breath,
  *  life's a piece of shit, when you look at it,
  *  life's a laugh and death's a joke, that's true,
  *  you'll see it's all a show,
  *  keep'em laughing as you go.
  *  just remember that the last laugh is on you,
  *  and always look on the bright side of life,
  *  ....
  */
public class JTest3 extends JFrame {


       public class ComponentShadow {
       
          private Rectangle _rectangle = new Rectangle() ;
	  private String    _name      = null ;
	  
	  public ComponentShadow( String name , Rectangle rec ){
	     _rectangle = rec ;
	     _name = name ;
	  }
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

	     g.drawString("My Name",  10 , rect.height/2 ) ;	  
	  }
          public Rectangle getRectangle(){ return _rectangle ; }

       }

    public class MouseTest extends JPanel {
    
       private Rectangle [] _rectsb = {
       
         new Rectangle(10,10,100, 100) ,
         new Rectangle(200,10,100, 100) ,
         new Rectangle(400,10,100, 100) ,
	 
	 
       };
       
       private LinkedList<ComponentShadow> _shadowList = new LinkedList<ComponentShadow>() ;
       
       private boolean   _regularMode = true ;
       private boolean   _wasClicked  = false ;
       private boolean   _isResizing  = false ;

       private Point     _currentPosition = null ;
       private Point     _wasClickedAt    = new Point(0,0);
       
       public ComponentShadow [] getShadows(){
         return _shadowList.toArray( new ComponentShadow[0] ) ;
       }
       private boolean findPointInList( Point p ){
       
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
       public class MouseActions extends MouseAdapter implements MouseMotionListener {
           public void mouseDragged( MouseEvent event ){
	   
	      if( _regularMode )return ;
	      
	      _currentPosition = event.getPoint() ;
	      repaint();
	      
	   }
	   public void mouseMoved( MouseEvent event ){
	   }
	   public void mousePressed( MouseEvent event ){
	   
	      //System.out.println("Mouse pressed at : "+event);
	      
	      Point p = event.getPoint() ;
	      
	      if( findPointInList( p ) ){
	      
		 ComponentShadow shadow = _shadowList.getLast() ;
		 
                 if( shadow.isDestroy(p) ){
		 
		    _shadowList.removeLast() ;
		     
		 }else{
		 
	            _wasClicked   = true ;

                    _wasClickedAt = _currentPosition = p ;


		    _isResizing   = shadow.isResizing( p  ) ;
		 
		 }
		     
	         repaint() ;
	      }
	   }
	   public void mouseReleased( MouseEvent event ){
	      //System.out.println("Mouse released at : "+event);
	      _regularMode = true ;
	      repaint() ;
	   }
       }
       private MouseActions _actions = new MouseActions() ;
       private Point        _point   = new Point( 100 , 100 ) ;
       private Point        _newPoint = new Point(0,0);
       public MouseTest(){
       
          addMouseListener(new MouseActions());
	  
	  addMouseMotionListener( _actions ) ;
	  
	  for( int i = 0 ; i < _rectsb.length ; i++ ){
	     _shadowList.add( new ComponentShadow( "My Name "+i , _rectsb[i] ) ) ;
	  }
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
	     for( ComponentShadow shadow : _shadowList ){
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
                g.setXORMode( Color.black ) ;
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
             g.setXORMode( Color.black ) ;
	     
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
    public class OurLayoutManager implements LayoutManager2 {
       private ComponentShadow [] _shadow = null ;
       public OurLayoutManager( ComponentShadow [] shadow ){
	  _shadow = shadow ;
       }
       public void addLayoutComponent( String name , Component comp ){
	  //System.out.println("Component added : "+name+" "+comp);
       }
       public void addLayoutComponent(  Component comp , Object constraint ){
	  //System.out.println("Component added : "+comp+" "+constraint);
       }
       public void layoutContainer( Container container ){
	  Component [] c = container.getComponents() ;
	  //System.out.println("NEW LAYOUT : "+c.length);
	  int mn = Math.min( c.length , _shadow.length ) ;
          for( int i = 0 ; i < mn ; i++ ){
	     Rectangle rect = _shadow[i].getRectangle() ;
             c[i].setLocation( rect.x , rect.y );
	     c[i].setSize( rect.width , rect.height );
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
	 // System.out.println("invalidateLayout : "+container);	  
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
    public class LayoutTest extends JPanel {
       public LayoutTest( LayoutManager lm  ){
           setLayout( lm ) ;
	   add( new Button("Click") ) ;
	   add( new Button("Clack") ) ;
	   add( new Button("Clum") ) ;
       }
    }
     
    public class OurContainerListener implements ContainerListener {
       public void componentAdded( ContainerEvent event ){
          System.out.println("componentEvent : "+event);
       }
       public void componentRemoved( ContainerEvent event ){
          System.out.println("componentEvent : "+event);
       }
    }
    public JTest3( String [] args ) throws Exception {
          
       getContentPane().setLayout( new GridLayout(1,0) ) ;
       
       
       JTabbedPane p1 = new JTabbedPane() ;
       
       MouseTest mouseTest = new MouseTest() ;
       OurLayoutManager lm = new OurLayoutManager( mouseTest.getShadows() ) ;
       
       p1.add( "Mouse" , mouseTest ) ;
       p1.add( "Layout" , new LayoutTest(lm) ) ;
       
       
       ShadowedPanel p2 = new ShadowedPanel() ;
       p2.addContainerListener( new OurContainerListener() ) ;
       p2.addOverlayComponent( new JButton("hallo") , "Hallo1" ) ;
       p2.addOverlayComponent( new JButton("hallo2") , "Hallo2" ) ;
       p2.addOverlayComponent( new JButton("hallo3") , "Hallo3" ) ;
       p2.addOverlayComponent( new JButton("hallo4") , "Hallo4" ) ;
       
       
       //getContentPane().add( p1  ) ;
       getContentPane().add( p2  ) ;
       //getContentPane().add( new TestPanel()  ) ;
       setSize(new Dimension(900,500));
       setVisible(true); 
       

    }
    
    public static void main( String [] args )throws Exception {
          new JTest3(args);
    }









}
