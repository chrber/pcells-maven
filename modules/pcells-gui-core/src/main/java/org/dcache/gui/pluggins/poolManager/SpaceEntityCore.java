// $Id: SpaceEntityCore.java,v 1.2 2008/06/26 07:19:18 cvs Exp $
//
package org.dcache.gui.pluggins.poolManager;
//

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SpaceEntityCore {

   private String _coreString = null ;
   private Map    _valueMap   = null ;

   protected SpaceEntityCore( String in ) throws IllegalArgumentException {
      _coreString = in.trim() ;
      _valueMap   = scanBasics();
      System.out.println(" Map : "+_valueMap);
   }
   
   protected Map getValueMap(){ return _valueMap ; }
   
   protected String getValueOf( String key ) throws IllegalArgumentException {
   
      String result = (String)_valueMap.get(key) ;
      
      if( result == null )
        throw new
        IllegalArgumentException("Syntax error : key <"+key+"> not present in server reply") ;
	
      return result ;
   }
   
   public String getBaseString(){ return _coreString ; }

   private Map scanBasics() throws IllegalArgumentException {

     int lastBlank = -1 ;
     int stringLen = _coreString.length() ;
     boolean hideQuote = false ;
     HashMap   hash = new HashMap() ;
     ArrayList list = new ArrayList() ;
     for( int i = 0 ; i < stringLen ; i++ ){
	 char c = _coreString.charAt(i) ;
	 if( c == ' ' ){
	    lastBlank = i ;
	 }else if( c == '{' ){
	    hideQuote = true ;
	 }else if( c == '}' ){
	    hideQuote = false ;
	 }else if( c == ':' ){
	    if( hideQuote ||
	        Character.isDigit(_coreString.charAt(i-1)) )continue ;
	    if( ( lastBlank == -1 ) || ( ( lastBlank + 1 ) == i ) )
	       throw new
	       IllegalArgumentException("Syntax error : 'no key found'");
	    int [] x = new int[2] ;
	    x[0] = lastBlank ; x[1] = i ;
	    //System.out.println("x[0]="+x[0]+"; x[1]="+x[1]+":");
	    list.add( x ) ;
	 }
     }
     int len = list.size() ;
     if( len == 0 )
       throw new
       IllegalArgumentException("No arguments found");

     String name = _coreString.substring(0, ((int [])list.get(0))[0] ) ;
     hash.put( "_id" , name ) ;
     for( int i = 0 ; i < len ; i++ ){
	int [] here = (int [])list.get(i) ;
        String key   = _coreString.substring(here[0]+1, here[1]) ;
	String value = null ;
	if( i == ( len - 1 ) ){
	   value = _coreString.substring( here[1]+1, stringLen ) ;
	}else{
	   int [] next = (int [])list.get(i+1) ;
	   value = _coreString.substring( here[1]+1, next[0] ) ;
	}
	//System.out.println("Key="+key+";value="+value+";");
	hash.put( key.trim() , value.trim() ) ;	      
     }
     return hash ;
   }
   
}
