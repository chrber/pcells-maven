package org.dcache.gui.pluggins.pools;

public class SplitCheck {

   public static void main( String [] args )throws Exception {
   
//        if( args.length < 2 )System.exit(4);
//       String in = args[1] ;
        String in = " hallo \n \notto\n" ;
        
     //   String [] result = args[0].split( args[1] ) ;
        String [] result = in.split( "\n" ) ;
        for( int i = 0 ; i < result.length ; i++ ){
           System.out.println("   <"+result[i]+">");
        }
   }
}
