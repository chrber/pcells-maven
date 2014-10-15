// $Id: PnfsManagerPanel.java,v 1.1 2008/11/09 08:23:58 cvs Exp $
//
package org.dcache.gui.pluggins.pnfs;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PnfsManagerInfo {

   int [] _threads = null ;
   long _requestsOk     = 0 , _requestsBad      = 0 ;
   int  _numberOfQueues = 0 , _numberOfRequests = 0 ;
   int  _numberOfThreadGroups = 0 , _numberOfRequestsInThreadGroups = 0 ;
   int  _numberOfCacheLocations = 0 , _numberOfRequestsInCacheLocations = 0 ;
   long _timestamp = 0L ;
   HashMap _statisticsMap = null ;


   public static void main( String [] args )throws Exception {

      BufferedReader br = new BufferedReader( new InputStreamReader( System.in ) ) ;
      StringBuffer sb= new StringBuffer() ;
      String line = null ;
      while( ( line = br.readLine() ) != null ){
        sb.append(line).append("\n") ;
      }
      PnfsManagerInfo info = PnfsManagerInfo.decodePnfsManagerInfo(sb.toString()) ;
      System.out.println("----------\n"+info.toString());

   }
   public String toString(){
     StringBuffer sb = new StringBuffer() ;
     sb.append( "Requests OK : ").append( _requestsOk ).append("\n");
     sb.append( "Requests Bad  : ").append( _requestsBad ).append("\n");
     sb.append( "Number of Queues  : ").append( _numberOfQueues ).append("\n");
     sb.append( "Number of Requests  : ").append( _numberOfRequests ).append("\n");
     sb.append( "Number of Thread Groups  : ").append( _numberOfThreadGroups ).append("\n");
     sb.append( "Number of Requests in Thread Groups  : ").append( _numberOfRequestsInThreadGroups ).append("\n");
     sb.append( "Number of cache locations  : ").append( _numberOfCacheLocations ).append("\n");
     sb.append( "Number of requests in cache locations  : ").append( _numberOfRequestsInCacheLocations ).append("\n");
     if( _statisticsMap != null ){
        Iterator it = _statisticsMap.entrySet().iterator() ;
        while( it.hasNext() ){
           Map.Entry e = (Map.Entry)it.next() ;
           String name = (String)e.getKey() ;
           long [] values = (long []) e.getValue() ;  
           sb.append(" ").append(name).append(" ").append(values[0]).append(" ").append(values[1]).append("\n");
        }
     }
     return sb.toString();
   }
   public PnfsManagerInfo(){
      touch() ;
   }
   public void touch(){
      _timestamp = System.currentTimeMillis() ;
   }
   public Map getStatisticsMap(){ return _statisticsMap ; }

   private static final String  _queue_pattern = "\\s*\\[([0-9]*)\\]\\s*([0-9]*)\\s*" ;
   private static final String  _count_pattern = "\\s*(\\w*)\\s*([0-9]*)\\s*([0-9]*)\\s*([0-9]*)\\s*" ;

   private static final int ST_IDLE            = 0 ;
   private static final int ST_THREAD_QUEUES   = 1 ;
   private static final int ST_THREAD_GROUPS   = 2 ;
   private static final int ST_CACHE_LOCATIONS = 3 ;
   private static final int ST_STATISTICS      = 4 ;
   private static final int ST_STATISTICS_BASE = 5 ;
   private static final int ST_STATISTICS_FOLD = 6 ;

   public static PnfsManagerInfo decodePnfsManagerInfo( String info ) throws Exception{

        int     state = 0 ;
        Matcher m     = null ;
        Pattern p     = null ;
        long requestCounter  = 0 ;
        long errorCounter    = 0 ;
        int numberOfQueues   = 0 ;
        int numberOfRequests = 0 ;
        int numberOfThreadGroups = 0 ;
        int numberOfRequestsInThreadGroups = 0 ;
        int numberOfCacheLocations = 0 ;
        int numberOfRequestsInCacheLocations = 0 ;
        HashMap statisticsMap = new HashMap() ;
        int statisticsMode = 0 ;

        StringTokenizer st = new StringTokenizer(info,"\n");
        for( int linecount = 0 ;  st.hasMoreTokens()  ; linecount++) {
             String line = st.nextToken() ;
             if( line.trim().length() == 0 )continue ;
             System.out.println("State : ["+state+"] Line : "+line ) ;
             switch( state ){

             case ST_IDLE :
                   if( ( line.indexOf("Threads") >=0 ) && ( line.indexOf("Queue") >=0 ) ){
                       state = ST_THREAD_QUEUES ;
                       p = Pattern.compile(_queue_pattern) ;
                   }
                   break ;
             case ST_THREAD_QUEUES :
                   if( line.indexOf( "Statistics:" ) >= 0 ){
                       state = ST_STATISTICS ;
                       p = Pattern.compile(_count_pattern) ;
                   }else if( line.indexOf("Thread groups") >= 0 ){
                       state = ST_THREAD_GROUPS ;
                       p = Pattern.compile(_queue_pattern) ;
                   }else if( line.indexOf("Cache Location Queues") >=0 ){
                       state =  ST_CACHE_LOCATIONS ;
                       p = Pattern.compile(_queue_pattern) ;
                   }else{
                       m = p.matcher(line) ;
                       if( ! m.matches() )
                       throw new
                               PnfsManagerDecodingException("LINE : "+linecount+";Found : "+line+"; expected [queue] count",linecount);
                       numberOfQueues ++ ;
                       numberOfRequests += Integer.parseInt(m.group(2));
                       System.out.println(" !! "+numberOfQueues + "  " +numberOfRequests ) ;
                   }

             break ;
             case ST_THREAD_GROUPS :
                   if( line.indexOf( "Statistics:" ) >= 0 ){
                       state = ST_STATISTICS ;
                       p = Pattern.compile(_count_pattern) ;
                   }else if( line.indexOf("Cache Location Queues") >= 0 ){
                       state =  ST_CACHE_LOCATIONS ;
                       p = Pattern.compile(_queue_pattern) ;
                   }else{
                       m = p.matcher(line) ;
                       if( ! m.matches() )
                       throw new
                               PnfsManagerDecodingException("LINE : "+linecount+";Found : "+line+"; expected [queue] count",linecount);
                       numberOfThreadGroups ++ ;
                       numberOfRequestsInThreadGroups += Integer.parseInt(m.group(2));
                       System.out.println(" !! "+numberOfThreadGroups + "  " +numberOfRequestsInThreadGroups ) ;
                   }

            break ;
            case ST_CACHE_LOCATIONS :
                   if( line.indexOf( "Statistics:" ) >= 0 ){
                       state = ST_STATISTICS ;
                       p = Pattern.compile(_count_pattern) ;
                   }else{
                       m = p.matcher(line) ;
                       if( ! m.matches() )
                       throw new
                               PnfsManagerDecodingException("LINE : "+linecount+";Found : "+line+"; expected [queue] count",linecount);
                       numberOfCacheLocations ++ ;
                       numberOfRequestsInCacheLocations += Integer.parseInt(m.group(2));
                       System.out.println(" !! "+numberOfCacheLocations + "  " +numberOfRequestsInCacheLocations ) ;
                   }
            break ;
            case ST_STATISTICS :
                   {
                       if( ( line.indexOf( "PnfsManager") >= 0 ) ||
                           ( line.indexOf( "Total" )      >= 0 )    ){
                          statisticsMode ++;
                          break ;
                       }

                       m = p.matcher(line) ;
                       if( ! m.matches() )
                       throw new
                               PnfsManagerDecodingException("LINE : "+linecount+";Found : "+line+"; expected [queue] count",linecount);
                       String CounterName     = m.group(1);
                       long   CounterRequests = Long.parseLong(m.group(2)) ;
                       long   CounterErrors   = Long.parseLong(m.group(3)) ;
                       System.out.println( "DEBUG : "+CounterName+" "+CounterRequests+" "+CounterErrors ) ;
                       requestCounter += CounterRequests;
                       errorCounter   += CounterErrors;
                       long [] x = { CounterRequests , CounterErrors };
                       statisticsMap.put( CounterName , x ) ;

                   }
                break ;
             }
          }
          long [] total = { requestCounter , errorCounter } ;
          statisticsMap.put( "Total" , total ) ;
          System.out.println(" !!! Counters " + requestCounter+ "  "+errorCounter) ;
          PnfsManagerInfo pnfsInfo = new PnfsManagerInfo() ;
          pnfsInfo._numberOfQueues   = numberOfQueues ;
          pnfsInfo._numberOfRequests = numberOfRequests ;
          pnfsInfo._numberOfThreadGroups = numberOfThreadGroups ;
          pnfsInfo._numberOfRequestsInThreadGroups = numberOfRequestsInThreadGroups ;
          pnfsInfo._numberOfCacheLocations = numberOfCacheLocations ;
          pnfsInfo._numberOfRequestsInCacheLocations = numberOfRequestsInCacheLocations ;
          pnfsInfo._requestsOk   = requestCounter ;
          pnfsInfo._requestsBad  = errorCounter ;
          pnfsInfo._statisticsMap = statisticsMap ;
          return pnfsInfo ;
        }


      //
      // result = b - a ;
      //
      public static Map calculateDetailDiff( Map a , Map b ){
         Map result = new HashMap() ;
         for( Iterator i = a.keySet().iterator() ; i.hasNext() ; ){
             String key = (String)i.next() ;
             try{
                long [] valueA = (long [])a.get(key);
                long [] valueB = (long [])b.get(key);
                long [] res = new long[2] ;
                res[0] = valueB[0] - valueA[0] ;
                res[1] = valueB[1] - valueA[1] ;
                result.put( key , res ) ;
             }catch(Exception wrong ){
                System.err.println("Problem in calculating details diff at "+key+" : "+wrong ) ;
             }
         }
         return result ;
      }


}
