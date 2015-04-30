// $Id: JCostDisplay.java,v 1.3 2005/05/10 07:22:56 cvs Exp $
//
package org.dcache.gui.pluggins;
//

import org.pcells.services.gui.CellGuiSkinHelper;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class      JCostDisplay 
       extends    CellGuiSkinHelper.CellPanel      {
       
    private java.util.List  _array         =  null ;
    private java.util.List  _selectedArray = null ;
    
    private double     _spaceMax  = 0.0 , _spaceAv = 0.0 ;
    private double     _cpuMax    = 0.0 , _cpuAv   = 0.0 ;
    private double     _space     = 0.0 , _cpu     = 0.0 ;
    private Comparator _spaceComp = new ArrayComparator(2) ;
    private Comparator _cpuComp   = new ArrayComparator(3) ;
    private Comparator _mergeComp = new ArrayComparator(0) ;
    private int        _sorting   = NONE ;
    private HashSet    _selection = null ;
    private HashMap    _poolMapping = null ;
    private boolean    _useMerge  = false ;
    private Object  AntiAlias   = RenderingHints.VALUE_ANTIALIAS_ON;
    private Object  Rendering   = RenderingHints.VALUE_RENDER_SPEED;
    private Font   _defaultFont = new Font( "SansSerif" , Font.ITALIC | Font.BOLD , 12 ) ;
    public static int NONE  =  0 ;
    public static int CPU   =  1 ;
    public static int SPACE =  2 ;
    public static int MERGE =  3 ;

    private String _selectedPoolName = null ;
    
    private DisplayKeyListener _keyListener = new DisplayKeyListener() ;
    public JCostDisplay(){
       setBackground(Color.black);
       addMouseListener(new PoolSelectionOnClick());
       addKeyListener( _keyListener ) ;
    }
    private class DisplayKeyListener extends KeyAdapter {
       private int  _counter = 0 ;
       public void keyPressed( KeyEvent event ){
//           System.out.println(event.toString());
//           System.out.println("KeyCode : "+event.getKeyCode()+" "+_counter+" "+KeyEvent.VK_RIGHT+" "+KeyEvent.VK_RIGHT);
           int code = event.getKeyCode() ;
           
           if( ( code == KeyEvent.VK_LEFT ) || ( code == KeyEvent.VK_DOWN ) )_counter -- ;
           else _counter++ ;
           repaint() ;
       }
    }
    private class PoolSelectionOnClick extends MouseAdapter {
    
        public void mousePressed( MouseEvent event ){
//	    System.out.println("Mouse pressed at : "+event);
            requestFocusInWindow();
	    if( _poolMapping == null )return ;
	    Integer i = new Integer( event.getX() ) ;
	    _selectedPoolName = (String)_poolMapping.get(i);
	    repaint() ;
	}
    }
    public synchronized void setMerge( double space , double cpu ){
       _useMerge = true ;
       _space    = space ;
       _cpu      = cpu ;

       runSelection() ;
       
       prepare() ;
       
       repaint() ;
    }
    public synchronized void setSorting( int sorting ){
       _useMerge = false ;
       _sorting = sorting ;
       
       runSelection() ;
       
       prepare() ;
       
       repaint() ;
    }
    public synchronized void setSelectionList( java.util.List selection ){

       _selection = selection == null ? null : new HashSet( selection ) ;
       
       runSelection() ;
       
       prepare() ;
       
       repaint() ;
    }
    public synchronized void setList( java.util.List array ){
       _array = array.size() == 0 ? null : array ;
       
       runSelection() ;
       
       prepare() ;
       
       repaint() ;
    }
    private void runSelection(){
       if( _selection == null ){
          _selectedArray = _array ;
          return ;
       }
       Iterator in = _array.iterator() ;
       _selectedArray = new ArrayList() ;
       while( in.hasNext() ){
          Object [] x = (Object [])in.next() ;
          if( _selection.contains( x[0].toString() ) )
             _selectedArray.add(x) ;
       }
    }
    private void prepare(){
    
       Object [] result = null ;
       
       if( _selectedArray == null )return ;
       result = (Object [])Collections.max( _selectedArray , _spaceComp ) ;
       _spaceMax = ((Double)result[2]).doubleValue() ;

       result = (Object [])Collections.max( _selectedArray , _cpuComp ) ;
       _cpuMax = ((Double)result[3]).doubleValue() ;
       
       int arraySize  = _selectedArray.size() ; 
       _cpuAv   = 0 ;
       _spaceAv = 0 ;
       for( int i = 0 ; i < arraySize ; i++ ){
       
          Object [] x = (Object [])_selectedArray.get(i) ;
          
          _spaceAv += ((Double)x[2]).doubleValue() ;
          _cpuAv   += ((Double)x[3]).doubleValue() ;
       }
       if( arraySize > 0 ){
          _spaceAv = ( _spaceAv / (double)arraySize ) ;
          _cpuAv   = ( _cpuAv   / (double)arraySize ) ;
       }

       if( _useMerge ){
          Collections.sort( _selectedArray , _mergeComp ) ;
       }else if( _sorting == SPACE ){
          Collections.sort( _selectedArray , _spaceComp ) ;
       }else if( _sorting == CPU ){
          Collections.sort( _selectedArray , _cpuComp ) ;
       }

    }
    public synchronized void paint( Graphics g ){

       Graphics2D g2 = (Graphics2D) g ;

           g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, AntiAlias);
//         g2.setRenderingHint(RenderingHints.KEY_RENDERING, Rendering);

 
       if( _useMerge ){
          paintMerged( g2 ) ;
       }else{
          paintSplit( g2 ) ;
       }
       
    }
    private void paintMerged( Graphics2D g2 ){

       g2.setFont( _defaultFont ) ;
       FontMetrics fm = getFontMetrics( _defaultFont );


       Dimension d = getSize() ;
       int w = d.width ;
       int h = d.height ;
       int y = 0 ;
       g2.setColor(getBackground());
       g2.fillRect( 0 , 0 , w , h ) ;
       g2.setColor(Color.green);
       
       java.util.List array = null ;
       synchronized( this ){
          array = _array ;
          if( array == null )return ;
	  _poolMapping = new HashMap() ;
       }

       int startX = ( w - _array.size() ) / 2 ;
       int startY = h - 10 ;

       g2.drawLine( 0 , startY , w-1 , startY ) ;
       
       double sum = 0.0 ;
       int position = Math.abs( _keyListener._counter % _array.size() ) ;
       
       for( int i = 0 , j = _array.size() ; i < j ; i++ ){
       
          Object [] value = (Object [] )_array.get(i) ;
	  
          double dy = ( ((Double)value[2]).doubleValue() * _space +
	                ((Double)value[3]).doubleValue() * _cpu    ) ;
          sum += dy ; 
	  
	  dy = dy / ( _cpuMax * _cpu + _spaceMax * _space ) ;
	  
          y = (int)(dy * (double)( h  - 20 )) ;
       
          int posX = i + startX ;
          if( i == position ){ g2.setColor(Color.red) ; _selectedPoolName = (String)value[0] ;}
          else{ g2.setColor(Color.green) ; }
          g2.drawLine( posX , startY , posX , startY - y ) ;
	  
	  _poolMapping.put( new Integer(posX) , value[0] ) ;
          
       }
       
       g2.setColor(Color.red);

       
       sum = sum / (double) _array.size() /  ( _cpuMax * _cpu + _spaceMax * _space ) ;
       String numberValue = ""+ ( (float)( sum ) ) ;
       
       y = (int)(  sum  * (double)(  h  - 20 )) ;
       
       g2.drawLine( startX - 10 , startY - y , startX + _array.size() + 10 , startY - y ) ;
       

       g2.drawString( numberValue , 
                      startX + _array.size() + 10 + 5 , 
                      startY - y ) ;
       
       
       drawSelectedPoolName( g2 , 10 , h - 20 ) ;
    
    }
    private void paintSplit( Graphics2D g2 ){
    
       g2.setFont( _defaultFont ) ;
       FontMetrics fm = getFontMetrics( _defaultFont );


       Dimension d = getSize() ;
       int w = d.width ;
       int h = d.height ;
       int y = 0 ;
       g2.setColor(getBackground());
       g2.fillRect( 0 , 0 , w , h ) ;
       g2.setColor(Color.green);
       g2.drawLine( 0 , h/2 , w-1 , h/2 ) ;
       
       java.util.List array = null ;
       synchronized( this ){
          array = _array ;
          if( array == null )return ;
	  _poolMapping = new HashMap() ;
       }
       int startX = ( w - _array.size() ) / 2 ;
       int position = Math.abs( _keyListener._counter % _array.size() ) ;
       for( int i = 0 , j = _array.size() ; i < j ; i++ ){
          Object [] value = (Object [] )_array.get(i) ;
          double dy = ((Double)value[2]).doubleValue() / _spaceMax ;
          
          y = (int)(dy * (double)( h / 2 - 10 )) ;
       
          int posX = i + startX ;
          if( i == position ){ g2.setColor(Color.red) ; _selectedPoolName = (String)value[0] ;}
          else{ g2.setColor(Color.green) ; }
          g2.drawLine( posX , h/2 , posX , h/2 - y ) ;
	  
	  _poolMapping.put( new Integer(posX) , value[0] ) ;
          
       }
       g2.setColor(Color.red);
       
       y = (int)(  _spaceAv / _spaceMax * (double)( h / 2 - 10 )) ;
       g2.drawLine( startX - 10 , h/2 - y , startX + _array.size() + 10 , h/2 - y ) ;
       String numberValue = ""+ ( (float)( _spaceAv ) ) ;

       g2.drawString( numberValue , 
                      startX + _array.size() + 10 + 5 , 
                      h/2 - y ) ;
                      
                      
       if( _spaceMax > 1.0 ){
          y = (int)(  1.0 / _spaceMax * (double)( h / 2 - 10 )) ;
          g2.drawLine( startX - 10 , h/2 - y , startX + _array.size() + 10 , h/2 - y ) ;

          g2.drawString( "1.0" , 
                         startX + _array.size() + 10 + 5 , 
                         h/2 - y ) ;
       }
       numberValue = "Space" ;
       int sw   = fm.stringWidth(numberValue);      
       g2.drawString( numberValue , startX - sw - 15 , h/2 - y ) ; 


       if( _cpuMax == 0.0 )return ;

       g2.setColor(Color.green);
       
       for( int i = 0 , j = _array.size() ; i < j ; i++ ){
          Object [] value = (Object [] )_array.get(i) ;
          double dy = ((Double)value[3]).doubleValue() / _cpuMax ;
          
          y = (int)(dy * (double)( h / 2 - 10 )) ;
       
          g2.setColor( i == position ? Color.red : Color.green);
          g2.drawLine( i + startX , h/2 , i + startX , h/2 + y ) ;
          
       }
       
       g2.setColor(Color.red);
       
       y = (int)(  _cpuAv / _cpuMax * (double)( h / 2 - 10 )) ;
       g2.drawLine( startX - 10 , h/2 + y , startX + _array.size() + 10 , h/2 + y ) ;
       numberValue = ""+ ( (float)( _cpuAv ) ) ;

       g2.drawString( numberValue , 
                      startX + _array.size() + 10 + 5 , 
                      h/2 + y ) ;


       numberValue = "Cpu" ;
       sw   = fm.stringWidth(numberValue);      
       g2.drawString( numberValue , startX - sw - 15 , h/2 + y ) ; 
       
       
       drawSelectedPoolName( g2 , 10 , h/2 - 20 ) ;
    }
    private void drawSelectedPoolName( Graphics2D g2 , int x , int y ){
       //
       //   draw pool name if _selectedPoolName != null 
       //
       String poolName = _selectedPoolName ;
       if( poolName == null )return ;
       
       g2.drawString( poolName , x , y ) ;
    
    } 
    private Double getMerged( Object o ){
         
	 return new Double(
	 
	 ((Double)((Object[])o)[2]).doubleValue() * _space +
	 ((Double)((Object[])o)[3]).doubleValue() * _cpu 
	 
	 ) ;
    }
    public class ArrayComparator implements Comparator {
        private int _row = 0 ;
        public ArrayComparator( int row ){
           _row = row ;
        }
        public int compare( Object o1 , Object o2 ){
	   if( _row == 0 ){
	      return  getMerged(o1).compareTo(getMerged(o2)) ;
	   }else{
              return ((Comparable)((Object[])o1)[_row]).compareTo( 
        	  ((Object[])o2)[_row] ) ;
           }
        }
    }    
}
