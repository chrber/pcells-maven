// $Id: SpaceLinkGroup.java,v 1.2 2008/07/06 21:28:20 cvs Exp $
//
package org.dcache.gui.pluggins.poolManager;
//

import java.util.ArrayList;
import java.util.Map;

public class SpaceLinkGroup extends SpaceEntityCore {
   
   private boolean _online    = false ;
   private boolean _replica   = false ;
   private boolean _custodial = false ;
   private boolean _nearline  = false ;
   private boolean _output    = false ;
   private long _reserved  = 0L ;
   private long _available = 0L ;
   private long _free      = 0L ;
   private long _total     = 0L ;
   private String _name    = null ;
   private String [] _vos  = null ;
   private long _id        = 0L ;
      
   public SpaceLinkGroup( String code ) throws IllegalArgumentException {
      super(code) ;
      resolveValues() ;
      System.out.println("LinkGroup={"+toString()+"}");
   }
   public void setTotal( long total ){ _total = total ; }
   public long   getId(){ return _id; }
   public String getLinkGroupName(){ return _name ; }
   public long   getLinkGroupId(){ return _id ; }
   public long   getReserved(){ return _reserved ; }
   public long   getAvailable(){ return _available ; }
   public long   getFree(){ return _free ; }
   public long   getTotal(){ return _total; }
   public String getVOs(){
      StringBuffer sb = new StringBuffer() ; 
      for( int i = 0 ; i < _vos.length ; i++ ){
        if( i > 0 )sb.append(",");
        sb.append(_vos[i]) ;
      }
      return sb.toString();
   }
   public String getAllowedString(){ 
      return (_online?"O":"o") +
             (_nearline?"N":"n") +
	     (_replica?"R":"r") +
	     (_custodial?"C":"c") ;
   }
   public String toString(){ 
      StringBuffer sb = new StringBuffer() ;
      sb.append("Id=").append(_name).append("(").append(_id).append(");");
      sb.append("Allowed:")
        .append(_online?"O":"o")
        .append(_nearline?"N":"n")
        .append(_replica?"R":"r")
        .append(_custodial?"C":"c")
        .append(";") ;
      sb.append("res:").append(_reserved).append(";");
      sb.append("av:").append(_available).append(";");
      sb.append("free:").append(_free).append(";");
      sb.append("total:").append(_total).append(";");
      sb.append("VOs=") ;
      for( int i = 0 ; i < _vos.length ; i++ ){
        if( i > 0 )sb.append(",");
        sb.append(_vos[i]) ;
      }
      sb.append(";");
      return sb.toString() ;
   }
   private void resolveValues(){

      Map    map    = getValueMap() ;
      String result = (String)map.get("_id") ;

      if( ( result == null ) || ( result.length() == 0 ) )
        throw new
        IllegalArgumentException("Syntax error : couldn't determine ID") ;

      _id = Long.parseLong( result ) ;

      _online    = (( result = (String)map.get("onlineAllowed") ) != null ) && result.equals("true") ;
      _nearline  = (( result = (String)map.get("nearlineAllowed") ) != null ) && result.equals("true") ;
      _output    = (( result = (String)map.get("outputAllowed") ) != null ) && result.equals("true") ;
      _custodial = (( result = (String)map.get("custodialAllowed") ) != null ) && result.equals("true") ;
      _replica   = (( result = (String)map.get("replicaAllowed") ) != null ) && result.equals("true") ;

      _reserved  = Long.parseLong( (( result = (String)map.get("ReservedSpace") ) != null ) ? result : "0" ) ; 
      _available = Long.parseLong( (( result = (String)map.get("AvailableSpace") ) != null ) ? result : "0" ) ; 
      _free      = Long.parseLong( (( result = (String)map.get("FreeSpace") ) != null ) ? result : "0" ) ; 

      _name = (( result = (String)map.get("Name") ) != null ) ? result : "?UNKNOWN?" ;

      result = (String)map.get("VOs") ;

      _vos = result == null ? new String[0] : splitVOs( result ) ;

   }
   private String [] splitVOs( String voList ){

      StringBuffer sb = new StringBuffer() ;
      ArrayList    al = new ArrayList() ;
      int state = 0 ;
      for( int i = 0 , n = voList.length() ; i < n  ; i++ ){
         char c = voList.charAt(i) ;
         switch( state ){
            case 0 :
               if( c == '{' )state = 1 ;
            break ;
            case 1 :
               if( c == '}' ){
                  al.add( sb.toString() ) ;
                  sb    = new StringBuffer() ;
                  state = 0 ;
               }else{
                  sb.append(c) ;
               }
            break ;
         }

      }
      return (String [])al.toArray( new String[0] ) ;
   }
   
}
