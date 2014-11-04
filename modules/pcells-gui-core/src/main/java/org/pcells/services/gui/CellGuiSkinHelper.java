// $Id: CellGuiSkinHelper.java,v 1.5 2007/02/15 08:18:12 cvs Exp $
//
package org.pcells.services.gui ;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.HashMap;

public class CellGuiSkinHelper  {

   private final static Logger _logger = LoggerFactory.getLogger(CellGuiSkinHelper.class);

//   static Color __blue   = new Color(190,40,40);
//   static Color __black  = new Color(94, 105, 176);
   static Color __blue   = new Color(0x11,0x11,0x11);
   static Color __black  = new Color(0xaa,0xaa,0xaa);
   static Color __gray   = new Color(180,180,230) ;
   
   static Color __backgroundColor = Color.orange ;
   static Color __foregroundColor = Color.blue ;
   
   static boolean __transparent   = false ;
   static boolean __skinmode      = false ;
   
   static private int __type = 1 ;

   private static Object [] softSkin = 
         {
           new Color( 0xee , 0xff , 0xcc ) ,
           new Color( 0x44 , 0x44 , 0xff ) ,
           new Color( 0x88 , 0x88 , 0xff ) ,
           new Color( 0xee , 0xff , 0xcc ) 
        } ;
   private static Object [] softSkin1 = 
         {
           new Color( 150 , 220,  230 ) ,
           new Color( 220 ,  70,  20  ) ,
           new Color( 220 ,  70,  20 ) ,
           new Color( 150 , 220,  230 ) 
        } ;
   private static Object [] softSkin2 = 
         {
           new Color(  30 , 20 , 140 ) ,
           new Color( 230 , 250 , 0  ) ,
           new Color( 230 , 250 , 0  ) ,
           new Color(  30 , 20 , 140 ) ,
        } ;
   private static Object [] softSkin3 = 
         {
           new Color( 230 , 250 , 0  ) ,
           new Color(  30 , 20 , 140 ) ,
           new Color(  30 , 20 , 140 ) ,
           new Color( 230 , 250 , 0  ) ,
        } ;
   private static Object [] softSkin4 = 
         {
           new Color( 230 , 250 , 0  ) ,
           new Color(  30 , 20 , 140 ) ,
           new Color( 120 , 180, 60   ) ,
           new Color( 200 , 200 , 120  ) ,
        } ;
        
   private static HashMap __skinMap = new HashMap() ;
   static {
   
        __skinMap.put( "soft" , softSkin ) ;
        __skinMap.put( "7ties" , softSkin1 ) ;
        __skinMap.put( "dark" , softSkin2 ) ;
        __skinMap.put( "funky" , softSkin3 ) ;
        __skinMap.put( "debug" , softSkin4 ) ;
        
        loadProperties() ;
   }
   public static void loadProperties(){
   
        String type = System.getProperty("skin") ;
        if( type != null ){
            Object [] skin = (Object[])__skinMap.get(type) ;
            if( skin != null ){
                __backgroundColor  = (Color)skin[0] ;
                __foregroundColor  = (Color)skin[1] ;
                __black            = (Color)skin[2] ;
                __blue             = (Color)skin[3] ;
                __transparent      = false ;
                __skinmode         = true ;
                return ;
            }
        
        }
        type = System.getProperty("skin.type") ;
	if( type != null ){
	  try{
          __type = Integer.parseInt(type);
      } catch (Exception ee) {
          _logger.error("Problem during SkinType determination: "+ ee);
      }
    }
        String trans = System.getProperty("skin.trans") ;
        __transparent = ( trans != null ) && trans.equals("true") ;
        trans = System.getProperty("skin.mode") ;
        __skinmode = ( trans != null ) && trans.equals("true") ;
        __black = getColorByString( System.getProperty("skin.border.top")    , __black ) ;
        __blue  = getColorByString( System.getProperty("skin.border.bottom") , __blue ) ;
        __backgroundColor = getColorByString( System.getProperty("skin.background")    , Color.blue ) ;
        __foregroundColor = getColorByString( System.getProperty("skin.foreground") , Color.orange ) ;
  /*      
        System.out.println("Skin mode = "+__skinmode ) ;
        System.out.println("Transparent = "+__transparent ) ;
        System.out.println("Background = "+__backgroundColor ) ;
        System.out.println("Foreground = "+__foregroundColor ) ;
        System.out.println("Border.top = "+__black ) ;
        System.out.println("Background = "+__blue ) ;
  */
   }
   private static  Color getColorByString( String colorString , Color defColor ) {
   
       if( ( colorString == null ) || ( colorString.trim().length() == 0 ) )return defColor ;
       
       try{
          long x = Long.parseLong(colorString,16) ;

          long a = ( x  ) & 0xffffff ;
          int r = (int) ( ( a >> 16 ) & 0xff ) ;
          int g = (int) ( ( a >>  8 ) & 0xff ) ;
          int b = (int) ( a & 0xff ) ;

          return  new Color( r , g  , b ) ;
       }catch(Exception ee ){
          return defColor ;
       }
   }
   public static void setNiceBorder( boolean border ){
      if( border ){
         __type |= 1 ;
      }else{
         __type = __type / 10 * 10 ;
      }
   }
   public static void setComponentProperties( JComponent component ){
      if( ! __skinmode )return ;
      component.setOpaque( ! __transparent ) ;
      component.setForeground( __foregroundColor ) ;
      component.setBackground( __backgroundColor ) ;
   }
   public static boolean isNiceBorder(){ return ( __type % 10 ) != 0 ; }
   public static void setSkinType( int type ){ __type = type ; }
   public static void setSkin( boolean type ){ __skinmode = type ; }
   public static boolean isSkin(  ){ return __skinmode; }
   public static void setTopColor( Color top ){ __blue = top ; }
   public static void setBottomColor( Color bottom ){ __black = bottom ; }
   public static void setForegroundColor( Color top ){ __foregroundColor = top ; }
   public static void setBackgroundColor( Color bottom ){ __backgroundColor = bottom ; }
   public static Color getForegroundColor(){ return __foregroundColor ; }
   public static Color getBackgroundColor(){ return __backgroundColor ; }
   public static Color getBottomColor(){ return __black ; }
   public static Color getTopColor(){ return __blue ; }
   public static void paintComponentBackground( Graphics gin , JComponent component ){
      
      int type = ( __type / 10 ) % 10 ;
      
    //  if( type == 0 )return ;
      
      Graphics2D g = (Graphics2D) gin ;
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                         RenderingHints.VALUE_ANTIALIAS_ON);

      Dimension d = component.getSize() ;       
      g.setColor( Color.black ) ;
      g.setPaint(new GradientPaint((float)0,(float)0,__blue,(float)(d.width),(float)(d.height),__black));
      g.fillRect( 0 , 0 , d.width - 1 , d.height - 1 ) ; 
   }
   
   
   
   public static class CellButton extends JButton {

      public CellButton(String string ){ 
	 super(string) ;
         setComponentProperties(this);
      }

//    public void paintComponent( Graphics gin ){
//	 CellGuiSkinHelper.paintComponentBackground(gin,this);
//       super.paintComponent(gin);
//    }
   }

    public static class CellPanel extends JPanel {

       public CellPanel(){
          setComponentProperties(this) ;
       }
       public CellPanel( LayoutManager  layout ){
	  super(layout);
          setComponentProperties(this) ;
       }
//     public void paintComponent( Graphics gin ){
//        System.err.println("Paint component for : "+this);
//	  CellGuiSkinHelper.paintComponentBackground(gin,this);
//	  super.paintComponent(gin);
//     }

    }
   
   
   public static class CellBorder implements Border {

      private int     _dim      = 50 ;
      private int     _diff     = 5 ; 
      private String  _title    = null ;
      private Object  AntiAlias = RenderingHints.VALUE_ANTIALIAS_ON;
      private Object  Rendering = RenderingHints.VALUE_RENDER_SPEED;
      private int     fontType  = Font.ITALIC | Font.BOLD ;
      private Font   _defaultFont = new Font( "SansSerif" , __simpleBorder ? Font.BOLD : fontType , 8 ) ;

      private static boolean  __simpleBorder = false ;
      private static Color    __borderTextColor = Color.black ;


      public CellBorder( String title , int height ){
	 if( __type == 0 )height = 20 ;
	 _title = title ;
	 _dim   = height ;
	 _diff  = _dim / 10 ;
	 _defaultFont = _defaultFont.deriveFont( (float)( _dim - 2 * _diff ) ) ;
      }
      public void paintBorder( Component c ,
                               Graphics g ,
                               int x , int y , int width , int height ){
	 
	  int type = __type % 10 ;
	  switch( type ){
	      case 0 :
	          paintSimpleBorder( c , g , x , y , width , height ) ;
	      break ;
	      default :
	          paintDefaultBorder( c , g , x , y , width , height ) ;
          }
      }

      public void paintSimpleBorder( 
                               Component c ,
                               Graphics g ,
                               int x , int y , int width , int height){

	  Graphics2D g2 = (Graphics2D) g ;
	  g2.setFont( _defaultFont ) ;
	  FontMetrics fm = c.getFontMetrics( _defaultFont );
	  int strH = (int) (fm.getAscent()-fm.getDescent());
	  int sw   = fm.stringWidth(_title);
	  g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, AntiAlias);
	  g2.setColor( __borderTextColor ) ;
	  g2.drawString( _title , width/2 - sw/2 , _dim / 2 + strH/2 ) ;

      }
      public void paintDefaultBorder( 
                               Component c ,
                               Graphics g ,
                               int x , int y , int width , int height ){


	  Graphics2D g2 = (Graphics2D) g ;

	  g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, AntiAlias);

	  //g2.setColor( c.getBackground().darker() ) ;
	  g2.setPaint(new GradientPaint((float)_diff,(float)_diff,__black,(float)(width),(float)(height),__blue));

	  Polygon borderShape = new Polygon() ;
	  borderShape.addPoint( _diff , _diff ) ;
	  borderShape.addPoint( width - 2 * _diff -1 , _diff ) ;
	  borderShape.addPoint( _dim - _diff , _dim - _diff ) ;
	  borderShape.addPoint( _diff , height - 2 * _diff - 1 ) ;

	  g2.fill( borderShape ) ;
	  g2.setColor( g2.getColor().darker() ) ;

	  g2.setFont( _defaultFont ) ;
	  FontMetrics fm = c.getFontMetrics( _defaultFont );
	  int strH = (int) (fm.getAscent()-fm.getDescent());
	  int sw   = fm.stringWidth(_title);

	  g2.setColor( Color.black ) ;
	  g2.drawString( _title , width/2 - sw/2 + 2 , _dim / 2 + strH/2 + 2 ) ;
	  g2.setColor( __gray ) ;
	  g2.drawString( _title , width/2 - sw/2 , _dim / 2 + strH/2 ) ;

      }
      public Insets getBorderInsets( Component c ){
	 return new Insets( _dim , _dim , _dim/2 , _dim/2  ) ;
      }
      public boolean isBorderOpaque(){ return false ; }
   }
}
