
package org.pcells.services.gui ;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class Test4 extends JFrame {

   public interface Drawable {
      public void draw( Dimension d , Graphics g );
   }
   public class MyDrawable implements Drawable {
      public void draw(Dimension d , Graphics g ){
          g.setColor( Color.orange ) ;
          g.drawRect( 0 , 0 , 20 , 20 ) ;         
      }
   }
   private Point            _currentVector    = new Point(0,0);
   private Point            _currentMouse     = null ;
   private ComponentBase    _currentComponent = null ;
   private boolean          _currentResize    = false ;
   private ArrayList        _myComponents     = new ArrayList() ;
   
   
   public class MovingPanel extends JPanel implements LayoutManager {
   private Mousi            _mousi            = new Mousi() ;
       public MovingPanel(){
          addMouseListener( _mousi );
          addMouseMotionListener( _mousi );
          setLayout(this);
          add(  new JButton("Hallo Otto"),"waste") ;
          add(  new JButton("Hallo Karl"),"Otto") ;
       }
       public void paintComponent( Graphics gin ){
          super.paintComponent(gin);
          Dimension d = getSize() ;
          gin.setColor( Color.gray ) ;

           Graphics2D g = (Graphics2D) gin ;
           g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                              RenderingHints.VALUE_ANTIALIAS_ON);

          if( _currentComponent != null ){

              for( Iterator ii = _myComponents.iterator() ; ii.hasNext()  ; ){
                 ComponentBase x = (ComponentBase)ii.next() ;
                 if( x == _currentComponent )continue ;
                 g.drawRect( x.r.x , x.r.y , x.r.width , x.r.height  ) ;
              }
              g.setXORMode( Color.white ) ;
              g.setColor(Color.green);
              g.fillRect( _currentComponent.r.x , _currentComponent.r.y ,
                          _currentComponent.r.width , _currentComponent.r.height  ) ;

              _currentComponent.draw(d,g);
          
          }else{
              for( Iterator ii = _myComponents.iterator() ; ii.hasNext()  ; ){
                 ComponentBase x = (ComponentBase)ii.next() ;
                 x.draw(d,g);
              }
          }
          g.drawRect( 20 , 20 , d.width - 40 , d.height -40 ) ;
       }
    public void addLayoutComponent(String name, Component comp){
       System.out.println("addLayoutComponent : "+name+"   "+comp);
       _myComponents.add(new ComponentBase( name , 10 , 10 , comp ));
       doLayout();
       repaint();
    }
    public void removeLayoutComponent(Component comp) {
    }
    public Dimension preferredLayoutSize(Container target) {
      synchronized (target.getTreeLock()) {
        int nmembers = target.getComponentCount();
        if( nmembers < 1 )return target.getSize() ;
        Component m = target.getComponent(0);
        return m.getSize() ;
      }
      
    } 
    public Dimension minimumLayoutSize(Container target) {
      synchronized (target.getTreeLock()) {
        return target.getSize() ;
      }
    }
      public void layoutContainer(Container target) {
         synchronized (target.getTreeLock()) {
           //int nmembers = target.getComponentCount();
           //if( nmembers < 1 )return  ;
           //Component m = target.getComponent(0);
           //m.setLocation(  0 , 0 ) ;
           //m.setSize( 100, 100 ) ;
              for( Iterator ii = _myComponents.iterator() ; ii.hasNext()  ; ){
                 ComponentBase base = (ComponentBase)ii.next() ;
                 Component comp = base._component ;
                 System.out.println("Doint layout : "+base._name+" "+base._innerFrame);
                 if( base._innerFrame == null )continue ;
                 
                 comp.setLocation( base._innerFrame.x ,  base._innerFrame.y) ;
                 comp.setSize( base._innerFrame.width ,  base._innerFrame.height ) ;              
              }
              repaint();
           }
      }
   public class Mousi extends MouseAdapter implements MouseMotionListener{
       public void mousePressed( MouseEvent event ){
          _currentComponent = null ;
          for( int i = _myComponents.size() -1 ; i >= 0  ; i-- ){
             ComponentBase x = (ComponentBase)_myComponents.get(i);
             if( x.contains(event.getPoint()) ){
                _currentComponent = x ;
                _currentVector    = _currentComponent.getVector( event.getPoint() ) ;
                _currentResize    = x.containsCorner( event.getPoint() ) ;
                _myComponents.set(i , _myComponents.get(_myComponents.size()-1) ) ;
                _myComponents.set(_myComponents.size()-1,  _currentComponent) ;
                break ;
             }
          }
          repaint() ;
       }
       public void mouseReleased( MouseEvent event ){
         _currentComponent = null ;
         _currentResize    = false ;
          //System.out.println("Mouse released : "+event ) ;
          doLayout();
          repaint();
         
       }
       public void mouseMoved( MouseEvent event ){
          //System.out.println("Mouse moved : "+event ) ;
       }
       public void mouseDragged( MouseEvent event ){
          if( _currentComponent != null ){
            Point p = event.getPoint() ;
            if( _currentResize ){
               _currentComponent.r.width  = p.x - _currentComponent.r.x ;
               _currentComponent.r.height = p.y - _currentComponent.r.y ;
            }else{
               _currentComponent.r.x = _currentVector.x + p.x;
               _currentComponent.r.y = _currentVector.y + p.y ;
            }
          }
          repaint();
       }
   }
   }
   public class ComponentBase {
   
       private String     _name = "MyName" ;
       private Component  _component = null ;
       private Rectangle  r = new Rectangle( 10 , 10 , 100 , 100 ) ;
       private Rectangle  _innerFrame = null ;
       
       public ComponentBase( String name , int x , int y ){
          r.x = x  ; r.y = y ;
          _name = name ;
       }
       public ComponentBase( String name , int x , int y  , Component component ){
          this( name , x , y ) ;
          _component = component ;
       }
       public void draw( Dimension d , Graphics g ){
       
          g.setColor( Color.orange ) ;
          g.drawRect( r.x , r.y , r.width , r.height ) ;
          g.setColor( Color.blue ) ;
          int xFrame = 16 ;
          int yFrame =  4 ;
         
          g.setFont( g.getFont().deriveFont( (float)xFrame ).deriveFont( Font.BOLD ) ) ;
          FontMetrics fm = g.getFontMetrics() ;
          xFrame = fm.getAscent() + fm.getDescent() + 8 ;

          g.fillRect( r.x + 1 , r.y + 1 , r.width - 2 , xFrame ) ;
          g.fillRect( r.x + 1 , r.y + 1 , yFrame  , r.height - 2   ) ;
           
          g.setColor( Color.orange ) ;
          g.drawString(_name, r.x + 5  , r.y + xFrame - 4 - fm.getDescent() ) ;
          
          
          _innerFrame = new Rectangle( r.x + yFrame + 1 , r.y + xFrame + 1 , r.width - yFrame - 2 , r.height - xFrame - 2 ) ;

       }
       public boolean contains( Point p ){
          return  r.contains(p)  ;
       }
       public boolean containsCorner( Point p ){
           return
             ( ( r.x + r.width - 10  ) < p.x ) && ( r.x + r.width  > p.x ) &&
             ( ( r.y + r.height - 10 ) < p.y ) && ( r.y + r.height > p.y ) ;
       }
       public Point getVector( Point p ){
           return new Point( r.x - p.x  , r.y - p.y  );
       }
   }
   public Test4( String [] args ) throws IOException{
   
      super("Test4 ...");

      WindowListener l = new WindowAdapter() {
            public void windowClosing(WindowEvent e) {System.exit(0);}
      };
      addWindowListener(l);
       
      getContentPane().add( new MovingPanel() ,  "Center" );
      pack();
      setSize(new Dimension(900,500));
      setVisible(true);
      
           
   }
    public static void main(String s[]) throws Exception  {
         new Test4(s);
    }
}
