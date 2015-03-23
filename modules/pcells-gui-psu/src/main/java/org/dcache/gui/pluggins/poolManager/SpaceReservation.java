// $Id: SpaceReservation.java,v 1.3 2008/07/06 21:28:20 cvs Exp $
//
package org.dcache.gui.pluggins.poolManager ;
//
import java.util.* ;
import java.text.* ;

public class SpaceReservation extends SpaceEntityCore {

   private long   _id = 0L ;
   private long   _linkGroupId = 0L ;
   private String _linkGroupName = null ;
   private String _description   = null ;
   private String _voGroup = null ;
   private String _voRole  = null ;
   private String _state   = null ;
   private String _retpol  = null ;
   private String _acclat  = null ;
   private long   _size    = 0L ;
   private long   _used    = 0L ;
   private long   _allocated = 0L ;
   private long   _created   = 0L ;
   private long   _lifetime  = 0L ;
   private long   _expiration = 0L ;
   private SimpleDateFormat df = new SimpleDateFormat("EEE MMM dd kk:mm:ss zzz yyyy", Locale.US) ;
   
   public SpaceReservation( String code ) throws IllegalArgumentException, ParseException {
      super(code);
      resolveValues() ;
      System.out.println("Reservation={"+toString()+"}");
   }
   public long   getId(){ return _id ; }
   public long   getLinkGroupId(){ return _linkGroupId ; }
   public String getLinkGroupName(){ return _linkGroupName ; }
   public void   setLinkGroupName( String linkGroupName ){ _linkGroupName = linkGroupName ; }
   public String getTokenDescription(){ return _description ; }
   public String getVoGroup(){ return _voGroup ; }
   public String getVoRole(){ return _voRole ; }
   public String getState(){ return _state ; }
   public String getRetentionPolicy(){ return _retpol ; }
   public String getAccessLatency(){ return _acclat ; }
   public long   getSize(){ return _size ; }
   public long   getUsed(){ return _used ; }
   public long   getAllocated(){ return _allocated ; }
   public long   getCreated(){ return _created ; }
   public long   getLifetime(){ return _lifetime ; }
   public long   getExpiration(){ return _expiration ; }
   public String getStorageAttributes(){
      return _acclat.substring(0,1)+_retpol.substring(0,1);
   }
   
   public String toString(){ return getBaseString() ; }
   private void resolveValues() throws IllegalArgumentException, ParseException {
   
      Map map = getValueMap() ;
      //
      //   the reservation id.
      //
      _id = Long.parseLong( getValueOf( "_id" ) ) ;      
      //
      // related link group id.
      //
      _linkGroupId = Long.parseLong( getValueOf("linkGroupId")  ) ;
      _linkGroupName = ""+_linkGroupId ;
      //
      // Timing
      //
      String tmp = getValueOf("expiration") ;
      if( tmp.equals("NEVER") ){
          _expiration = 0L ;
      }else{
          try{
              _expiration  = df.parse( tmp ).getTime() ;
	  }catch(Exception eee){
	      System.err.println("Problem decoding time/date : "+tmp ) ;
	      _expiration = (long)-1 ;
	  }
      }
      tmp = getValueOf("created") ;
      try{
          _created  = df.parse( tmp ).getTime() ;
      }catch(Exception eee){
	  System.err.println("Problem decoding time/date : "+tmp ) ;
	  _created = (long)-1 ;
      }
      try{
         _acclat = getValueOf( "accessLatency" ) ;
	 if( _acclat.length() == 0 )_acclat = "UNKNOWN" ;
      }catch(Exception eee ){
         _acclat = "UNKNOWN" ;
      }
      try{
         _retpol = getValueOf( "retentionPolicy" ) ;
	 if( _retpol.length() == 0 )_retpol = "UNKNOWN" ;
      }catch(Exception eee ){
         _retpol = "UNKNOWN" ;
      }
      // 
      // vo group and role
      //
      _voGroup = getValueOf("voGroup") ;
      _voRole  = getValueOf("voRole");
      _state   = getValueOf("state") ;
      try{
         _description = getValueOf("descr");
      }catch(Exception ee ){
         _description = getValueOf("description");
      }
      //
      //  sizes
      //  
      _allocated = Long.parseLong( getValueOf("allocated") ) ;
      _size      = Long.parseLong( getValueOf("size") ) ;
      _used      = Long.parseLong( getValueOf("used") ) ;
      
      tmp = getValueOf( "lifetime") ;
      int tmpleng = tmp.length() ;
      
      if( ( tmpleng < 3 ) || ( ! tmp.endsWith("ms") ) )
        throw new
	IllegalArgumentException("Syntax error in lifetime : >"+tmp+"<");
        
      
      _lifetime = Long.parseLong( tmp.substring(0,tmpleng-2) ) ;

   }
}
