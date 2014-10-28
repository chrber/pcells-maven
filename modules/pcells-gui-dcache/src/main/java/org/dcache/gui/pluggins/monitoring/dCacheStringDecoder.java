// $Id: dCacheStringDecoder.java,v 1.4 2007/04/29 11:38:39 cvs Exp $
//
package org.dcache.gui.pluggins.monitoring ;
//
 
import java.util.*;
import java.util.regex.*;


public class dCacheStringDecoder {

   private static String __all_requests  = "#(\\-[0-9]+) created by ([^\\s]+) with credentials : RequestCredential\\[(.*)\\] state = (.*)" ;
   private static String __put_request = "PutReqeest "+__all_requests  ;
   

   public static void main( String [] args ) throws Exception {
       Pattern p = Pattern.compile( __all_requests ) ;
       Matcher m = p.matcher(args[0]);
       System.out.println("Matches : "+m.matches() );
       int c = m.groupCount();
       for( int i = 0 ; i <= c ; i++ ){
          System.out.println(" group "+c+" : "+m.group(i));
       }
   }

 // PutRequest #-2147474022 created by dteam001 with credentials : RequestCredential[/O=GermanGrid/OU=DESY/CN=Andreas Gellrich,nondelegated  ] state = Pending


   /**
    *----------------------------------------------------------------------------------------------------
    *
    *    SRM LS INFO
    */
   public static class SrmLsInfo {
   
       private int _numberOfRequests = 0 ;
       private int _numberOfPutRequests = 0 ;
       private int _numberOfGetRequests = 0 ;
       private int _numberOfCopyRequests = 0 ;
       
       public static SrmLsInfo decode( String infoString ) throws Exception { return new SrmLsInfo(infoString) ; }
       public int getNumberOfRequests(){ return _numberOfRequests ; }
       public int getNumberOfPutRequests(){ return _numberOfPutRequests ; }
       public int getNumberOfGetRequests(){ return _numberOfGetRequests ; }
       public int getNumberOfCopyRequests(){ return _numberOfCopyRequests ; }
       private SrmLsInfo( String infoString ) throws Exception {
          decodeSrmLsInfo(infoString ) ;
       }
       private void decodeSrmLsInfo( String infoString ) throws Exception {

           StringTokenizer st = new StringTokenizer(infoString,"\n");
           int state = 0 ;
           int [] counter = new int[3] ;
           while( st.hasMoreTokens() ){
              String line = st.nextToken().trim() ;
              if( line.length() == 0 )continue ;
              if( line.equals("Get Requests:") ){
                 state = 1 ;
                 continue ;
              }else if( line.equals("Put Requests:") ){
                  state = 2 ;
                 continue ;
              }else if( line.equals("Copy Requests:") ){
                  state = 3 ;
                 continue ;
              }
              if( ( state < 1 ) || ( state > 3 ) )continue ;

              counter[state-1] ++ ;
           }

           _numberOfGetRequests = counter[0] ;
           _numberOfPutRequests = counter[1] ;
           _numberOfCopyRequests = counter[2] ;
           _numberOfRequests = counter[0] + counter[1] + counter[2] ;
           return ;
        }
        
   }
   /**
    *----------------------------------------------------------------------------------------------------
    *
    *    PNFS MANAGER INFO
    */
   private static Pattern _queue_pattern      = Pattern.compile("[ ]*Threads \\(([0-9]+)\\) Queue[ ]*");
   private static Pattern _thread_pattern     = Pattern.compile("[ ]*\\[([0-9]+)\\] ([0-9]+)[ ]*");

   public static class PnfsManagerBasicInfo {
       private int _numberOfThreads = 0 ;
       private float _avarageThreadQueueLength = (float)0.0 ;
       public float getAvarageThreadQueueSize(){ return _avarageThreadQueueLength ; }
       public int getNumberOfThreads(){ return _numberOfThreads ; }
       public static PnfsManagerBasicInfo decode( String infoString ){ return getPnfsManagerBasicInfo( infoString ) ; }
   }
   public static PnfsManagerBasicInfo getPnfsManagerBasicInfo( String infoString ) throws IllegalArgumentException {
       PnfsManagerBasicInfo info = new PnfsManagerBasicInfo() ;
       StringTokenizer st = new StringTokenizer(infoString,"\n");
       info._numberOfThreads = 0 ;
       while( st.hasMoreTokens() ){
          String line = st.nextToken().trim() ;
          if( line.length() == 0 )continue ;
          Matcher m = _queue_pattern.matcher(line) ;
          if( m.matches() ){
             info._numberOfThreads = Integer.parseInt( m.group(1) ) ;
             break ;
          }
       }
       if( info._numberOfThreads == 0 )
         throw new
         IllegalArgumentException("No queue info found") ;
       float sum = 0 ;
       for( int i = 0 ;  i < info._numberOfThreads  ; i++ ){
          String  line = st.nextToken().trim() ;
          Matcher m    = _thread_pattern.matcher(line) ;
          if( ! m.matches() )
             throw new
             IllegalArgumentException("Thread info lines didn't match anywere (not a PnfsManager Info string)");
        
          int n = Integer.parseInt( m.group(2) ) ;
          sum += (float) n ;   
       }
       info._avarageThreadQueueLength = sum / (float)info._numberOfThreads ;
       return info ;
   }
  
  
}
