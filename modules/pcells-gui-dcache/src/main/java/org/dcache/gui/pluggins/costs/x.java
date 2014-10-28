import java.text.* ;

public class x {
    
    public static class DiagramFormat {

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
       
       public DiagramFormat( double x ){

           _value        = x ;
	   
	   double ex     = Math.log10(x);
	   double exceil = Math.ceil(ex) ;
           double base   = Math.pow( 10.0 , exceil ) ;
       
           int m = (int)Math.round(100.0*x/base) ;
	   
	   //System.out.println("Scale base : "+m);
	   
           Scale ourScale = null ;
           for( int i= 0 ; i < _scale.length ; i++ ){
              if( m <= ( ourScale = _scale[i]).top )break ;
           }
           //System.out.println("Using : "+ourScale) ;
       
           _interval = ((double)ourScale.scale)/100.0*base ;
           //System.out.println("Interval : "+_interval ) ;
           //DecimalFormat df1 = new DecimalFormat("####.####");
	   //System.out.println("Formated : "+df1.format(_interval));
       }
       public int getIntervalCount(){ return (int)( _value/_interval ) ; }
       public double getValueAt(int i ){
           return (double)i * _interval ;
       }
       public String getValueLabelAt( int i ){
          return _normalFormat.format(getValueAt(i));
       }
    }
    public static void main( String [] xi ){
    
       double x = Double.parseDouble(xi[0]) ;
       
       System.out.println("Double : "+x);
       
       DiagramFormat df = new DiagramFormat( x ) ;
       int count = df.getIntervalCount() ;
       for( int i = 0 ; i < (count+1) ; i++ ){
          System.out.println("Result : "+df.getValueAt(i)+ " >"+df.getValueLabelAt(i)+"<");
       }
  //     System.out.println("Exp : "+Math.getExponent(x));
  //     System.out.println("Ceil : "+Math.ceil(x));
  //     System.out.println("Floor : "+Math.floor(x));
  
       //double ex = Math.log10(x);
       //double exceil = Math.ceil(ex) ;
       
         //  double base   = Math.pow( 10.0 , exceil ) ;
       
     //  double result = x / base ;
      // System.out.println("ex : "+ex+" exceil : "+exceil ) ;
      // System.out.println("Result : "+(x/base) + " * 10 topowerof "+exceil ) ;
      // System.out.println("Result : "+Math.round(100.0*x/base) + " * 10 topowerof "+Math.round(exceil) ) ;
       
       
       
    }
}
