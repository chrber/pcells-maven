// $Id: PoolGenericInfo.java,v 1.3 2007/01/11 08:23:23 cvs Exp $
//
package org.dcache.gui.pluggins.pools;

import java.util.*;

public class PoolGenericInfo {

   private Map _hash = new HashMap() ;
   
   public PoolGenericInfo(){
   }
   public PoolGenericInfo( String infoString ){
   
       StringTokenizer st = new StringTokenizer( infoString , "\n") ;
       
       while( st.hasMoreTokens() ){
       
           String line = st.nextToken() ;
           if( line.indexOf("Diskspace usage") > -1 )break ;
           String [] tokens = line.split(":",2);
           if( tokens.length > 1 )_hash.put( tokens[0].trim() , tokens[1].trim() ) ;
           
       }
   }
   public Map getMap(){
      return _hash ;
   }
   public class FlushClass {
   
       public class FlushEntry {
       
       }
       private String _name = null ;
       
       private long   _experationRest = 0L , _experationDefined = 0L ;
       private long   _pendingRest = 0L    , _pendingDefined = 0L ;
       private long   _sizeRest    = 0L    , _sizeDefined    = 0L ;
       
       private int  _active          = 0 ;
       private int  _activeEntries   = 0 ;
       private int  _deactiveEntries = 0 ;
       
       private boolean _suspended    = false ;
       
       public FlushClass( String name ){
          _name = name ;
       }
       public String getName(){ return _name ; }
       public long getExpirationDefined(){ return _experationDefined ; }
       public long getPendingDefined(){ return _pendingDefined ; }
       public long getSizeDefined(){ return _sizeDefined ; }
       public long getExpirationRest(){ return _experationRest ; }
       public long getPendingRest(){ return _pendingRest ; }
       public long getSizeRest(){ return _sizeRest ; }
       public int getActiveProcesses(){ return _active ; }
       public int getActiveEntries(){ return _activeEntries ; }
       public int getInactiveEntries(){ return _deactiveEntries ; }
       public boolean isSuspended(){ return _suspended ; }
       
       public void setExperation( long rest , long defined ){
          _experationRest = rest ;
          _experationDefined = defined ;
       }
       public void setPending( long rest , long defined ){
          _pendingRest = rest ;
          _pendingDefined = defined ;
       }
       public void setSize( long rest , long defined ){
          _sizeRest = rest ;
          _sizeDefined = defined ;
       }
       public void setActive( int active){
          _active = active ;
       }
       public void addActiveEntry( String activePnfs ){
          _activeEntries ++ ;
       }
       public void addDeactiveEntry( String activePnfs ){
          _deactiveEntries ++ ;
       }
       public void setSuspended( boolean suspended ){
          _suspended = suspended ;
       }
       public String toString(){
          StringBuffer sb = new StringBuffer() ;
          
          sb.append(_name).append("={").
             append("exRest=").append(_experationRest).append(";exDefined=").append(_experationDefined).
             append(";pendingRest=").append(_pendingRest).append(";pendingDefined=").append(_pendingDefined).
             append(";sizeRest=").append(_sizeRest).append(";sizeDefined=").append(_sizeDefined).
             append(";activeProcs=").append(_active).
             append(";activeEntries=").append(_activeEntries).
             append(";deactiveEntries=").append(_deactiveEntries).
             append(";suspended=").append(_suspended) ;
             
          return sb.toString();
       }
   }
   private final static int ST_IDLE = 0 ;
   private final static int ST_HEADER = 1 ;
   private final static int ST_ACTIVE = 2 ;
   private final static int ST_DEACTIVE = 3 ;
   private final static int ST_PRE_DEACTIVE = 4 ;
   
   public long [] getNumbersFromQueueHeader( String line , int expected ) throws IllegalArgumentException {
      StringTokenizer st = new StringTokenizer( line ) ;
      long [] result = null ;
      
      if( st.countTokens() == 1 ){ // active count
         if( expected > 1 )
           throw new
           IllegalArgumentException("More Numbers expeced in header");     
         
         result = new long[1] ;
         result[0] = Long.parseLong( st.nextToken() ) ;
         return result ;
      }else if( st.countTokens() >= 3 ){ // active count
         result = new long[2] ;
         result[0] = Long.parseLong( st.nextToken() ) ;
         st.nextToken() ;
         result[1] = Long.parseLong( st.nextToken() ) ;
         return result ;
      }else{
         throw new
         IllegalArgumentException("Illegal Number of tokens in header");     
      }
   }
   public long [] getActiveCountFromQueueHeader( String line ) throws IllegalArgumentException {
      StringTokenizer st = new StringTokenizer( line ) ;
      long [] result = null ;
      
      if( st.countTokens() == 1 ){ // active count only         
         result = new long[2] ;
         result[0] = Long.parseLong( st.nextToken() ) ;
         result[1] = 0L ;
      }else{  // expecting <count> SUSPENDED
         result = new long[2] ;
         result[0] = Long.parseLong( st.nextToken() ) ;
         String suspended = st.nextToken() ;
         if( ! suspended.equals("SUSPENDED") )
             throw new
             IllegalArgumentException("Illegal Token in 'Active Store' line : "+suspended); 
         result[1] = 1L ;    
      }
      return result ;
   }
   public List  scanFlushInfo( String  flushInfoString ) throws IllegalArgumentException{
   
   //    StringTokenizer st = new StringTokenizer( flushInfoString , "\n" ) ;
       String [] columns = flushInfoString.split("\n");
       int columnCounter = 0 ;
       int     state = ST_IDLE  ;
       String  line  = null ;
       boolean undo  = false ;
       int     headCounter = 0 ;
       List       flushClassList = new ArrayList() ;
       FlushClass flushClass     = null ;
       while( true ){
          if( ! undo ){
            // if( ! st.hasMoreTokens() )break ;
            // line = st.nextToken().trim() ;
            if( columnCounter >= columns.length )break ;
            line = columns[columnCounter++].trim();
          }
//          System.out.println("CURRENT mode : "+state+" : "+line );
          undo = false ;
          switch( state ){
             case ST_IDLE : 
                if( line.length() == 0 )continue ;
                if( line.startsWith("Name :") ){
                   state = ST_HEADER ;
                   undo  = true ;
                   headCounter = 0 ;
//                   System.out.println("Switching from idle to HEAD");
                }else if( line.startsWith("Deactivated Requests" ) ){
                   state = ST_PRE_DEACTIVE ;
//                   System.out.println("Switching from idle to DEACT");
                }
                
             break ;
             case ST_HEADER : 
               headCounter ++ ;
              String [] x = line.split(":",2);
//              System.out.println("HEAD "+headCounter+" : "+x[0]+" <> "+x[1]);
               if( x.length < 2 )
                  throw new
                  IllegalArgumentException("Not a Queue Class Header");
               if( headCounter == 1 ){
//                  System.out.println("Starting scan for : "+x[1]);
               }else if( headCounter == 2 ){
                  if(  flushClass != null ){
                     flushClassList.add( flushClass ) ;
                     flushClass = null ;
                  }
                   flushClass = new FlushClass( x[1] ) ;
               }else if( headCounter == 3 ){
                  long [] results = getNumbersFromQueueHeader(x[1] , 2 );
                  flushClass.setExperation( results[0] , results[1] ) ;
               }else if( headCounter == 4 ){
                  long [] results = getNumbersFromQueueHeader(x[1] , 2 );
                  flushClass.setPending( results[0] , results[1] ) ;
               }else if( headCounter == 5 ){
                  long [] results = getNumbersFromQueueHeader(x[1] , 2 );
                  flushClass.setSize( results[0] , results[1] ) ;          
               }else if( headCounter == 6 ){
                  long [] results = getActiveCountFromQueueHeader(x[1] );
                  flushClass.setActive( (int)results[0] ) ; 
                  flushClass.setSuspended( results[1] == 1 ) ;    
                  state = ST_ACTIVE ; 
                  headCounter = 0 ;
//                  System.out.println("Header sucessfully scanned; switching to ACTIVE "+flushClass);
               }
             break ;
             case ST_ACTIVE :
                 if( line.length() == 0 ){
                    state = ST_IDLE ;
//                    System.out.println("ACTIVE finished : switching to idle : "+flushClass);
                 }else{
                    StringTokenizer st2 = new StringTokenizer(line) ;
                    flushClass.addActiveEntry( st2.nextToken() ) ; 
                 }
             break ;
             case ST_DEACTIVE : 
                 if( line.length() == 0 ){
//                    System.out.println("Desctive switching to idle and adding flushclass");
                    state = ST_IDLE ;
                 }else{
                    StringTokenizer st2 = new StringTokenizer(line) ;
                    flushClass.addDeactiveEntry( st2.nextToken() ) ; 
                 }
             break ;
             case ST_PRE_DEACTIVE : 
                 if( line.length() == 0 ){
//                    System.out.println("pre deactive switching to deactive");
                    state = ST_DEACTIVE ;
                 }else{
                    throw new
                    IllegalArgumentException("No more empty line after 'Deactivated Requests'");
                 }
             break ;
          
          }
       }
       if(  flushClass != null ){
         flushClassList.add( flushClass ) ;
         flushClass = null ;
       }
       return flushClassList  ;
   }
}
