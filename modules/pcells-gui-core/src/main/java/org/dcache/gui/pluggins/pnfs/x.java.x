import java.util.regex.* ;

public class x {

  private static final String  _count_pattern = "\\s*\\w*\\s*([0-9]*)\\s*([0-9]*)\\s*([0-9]*)\\s*([0-9]*)\\s*" ;

  public static void main ( String [] args ) throws Exception {


   Pattern p = Pattern.compile( _count_pattern ) ;
   String line = args[0] ;
                       Matcher m = p.matcher( line ) ;
                       if( ! m.matches() )
                       throw new
                          Exception("Found : "+line+"; expected [queue] count");
                       System.out.println("Group Count : "+m.groupCount());
 for( int i  = 0 ; i < 10 ; i ++ ){
   System.out.println(" group : "+i+" >" + m.group(i)+"<" ) ;
}
}

}
