// $Id: DiagramFormat.java,v 1.2 2008/08/04 19:02:56 cvs Exp $
//
package org.dcache.gui.pluggins.costs ;
//
import java.text.* ;

public class DiagramFormat {

   private class Scale {
      int top = 0 , scale = 2 ;
      private Scale( int top , int scale ){
        this.top = top ; this.scale = scale ;
      }
      public String toString(){
        return "Top="+top+";scale="+scale ;
      }
   }
   private Scale [] _scale = {

     new Scale( 15 , 2 ) ,
     new Scale( 21 , 4 ) ,
     new Scale( 34 , 5 ) ,
     new Scale(  69 , 10 ) ,
     new Scale( 100 , 20 ) 
   };
   private double _interval = 0.0 ;
   private double _value    = 0.0 ;

   private DecimalFormat _normalFormat = new DecimalFormat("####.####");
   private DecimalFormat _expFormat    = new DecimalFormat("####0.##E0");

   public DiagramFormat( double x ){

       _value        = x ;

       double ex     = Math.log10(x);
       double exceil = Math.ceil(ex) ;
       double base   = Math.pow( 10.0 , exceil ) ;

       int m = (int)Math.round(100.0*x/base) ;

       Scale ourScale = null ;
       for( int i= 0 ; i < _scale.length ; i++ ){
          if( m <= ( ourScale = _scale[i]).top )break ;
       }

       _interval = ((double)ourScale.scale)/100.0*base ;
   }
   public int getIntervalCount(){ return (int)( _value/_interval ) ; }
   public double getValueAt(int i ){
       return (double)i * _interval ;
   }
   public String getValueLabelAt( int i ){
      double value = getValueAt(i) ;
      if( ( value < 0.0001 ) || ( value > 1000 ) )
         return _expFormat.format(getValueAt(i));
      else
         return _normalFormat.format(getValueAt(i));
   }

    public static void main( String [] xi ){
    
       double x = Double.parseDouble(xi[0]) ;
       
       System.out.println("Double : "+x);
       
       DiagramFormat df = new DiagramFormat( x ) ;
       int count = df.getIntervalCount() ;
       for( int i = 0 ; i < (count+1) ; i++ ){
          System.out.println("Result : "+df.getValueAt(i)+ " >"+df.getValueLabelAt(i)+"<");
       }
              
       
    }


}

