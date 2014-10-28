// $Id: PoolMonitor.java,v 1.1 2007/03/16 05:43:36 cvs Exp $
//
package org.dcache.gui.pluggins.monitoring ;
//
 
import diskCacheV111.poolManager.PoolManagerCellInfo;
import diskCacheV111.pools.PoolCellInfo;
import dmg.cells.nucleus.NoRouteToCellException;
import org.pcells.services.connection.DomainConnection;
import org.pcells.services.connection.DomainConnectionListener;
import org.pcells.services.gui.monitoring.ComponentMonitorAdapter;
import org.pcells.services.gui.monitoring.ComponentMonitorable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PoolMonitor 
       extends ComponentMonitorAdapter 
       implements DomainConnectionListener,
                  ActionListener {

   private DomainConnection   _connection  = null ; 
    

   public PoolMonitor( String name , String description , String argString ,  DomainConnection connection ){
      super( name , description , argString ) ;

      _connection  = connection ;

   }
   public void actionPerformed( ActionEvent event ){
   }
   public void domainAnswerArrived( Object obj , int subid ){
       System.out.println( "Answer ("+subid+") : "+obj.toString() ) ;
       
       if( subid == 100 )handlePoolManagerReply( obj , subid ) ;
       else if( subid >= POOL_REQUERST_OFFSET )handlePoolReply( obj , subid ) ;
   }
   private void handlePoolManagerReply( Object obj , int subid ){
   
       if( obj == null ){
          setResult( ComponentMonitorable.RESULT_FATAL , 
                     "Request to PoolManager timed out" ) ;
       }else if( obj instanceof NoRouteToCellException ){
          setResult( ComponentMonitorable.RESULT_FATAL , 
                     "PoolManager not present",
                     obj.toString()
                   ) ;
       }else if( obj instanceof Exception ){
          setResult( ComponentMonitorable.RESULT_FATAL , 
                     "Got exception from PoolManager",
                     obj.toString()
                   ) ;
       }else if( obj instanceof PoolManagerCellInfo ){
           analysePoolManagerReply( ( PoolManagerCellInfo) obj ) ;
       }else if( obj instanceof Object [] ){
           analysePoolManagerReply( ( Object []) obj ) ;
       }else{
          setResult( ComponentMonitorable.RESULT_FATAL , 
                     "Got illegal class from PoolManager",
                     obj.toString()
                   ) ;
       }
       
   }
   private String [] _requestedPoolNames      = null ;
   private Object [] _requestedPoolReplies    = null ;
   private int       _requestedPoolReplyCount = 0 ;
   private int       _requestCount            = 0 ;
   private static final int POOL_REQUERST_OFFSET = 2000 ;
   
   private void handlePoolReply( Object obj , int subid ){
   
      if( _requestedPoolReplies == null )return ;
      _requestedPoolReplyCount -- ;
      System.out.println("Pool Reply : "+_requestedPoolReplyCount+" : "+obj);
      setAction("Reply count "+_requestedPoolReplyCount+" out of "+_requestCount);
      
      if( obj == null ){
          // timeout, nothing to do
          return ;
      }
      int pos = subid - POOL_REQUERST_OFFSET ;
      if(  pos >= _requestedPoolReplies.length ){
          _requestedPoolReplies = null ;
          setResult( ComponentMonitorable.RESULT_FATAL , 
                     "Internal Failure",
                     "Returned message from pool was not expected"
                   ) ;
          return ;
      }
      _requestedPoolReplies[pos] = obj ;
      
      if( _requestedPoolReplyCount <= 0 )analysePoolReplies() ;
      
   }
   private void analysePoolReplies(){

      int ok = 0 ;
      int timeouts = 0 ;
      int not_exists = 0 ;
      int problem = 0 ;
      for( int i = 0 ; i < _requestedPoolReplies.length ; i++ ){
         if( _requestedPoolReplies[i] == null )timeouts ++ ;
         else if( _requestedPoolReplies[i] instanceof NoRouteToCellException )not_exists ++ ;
         else if( _requestedPoolReplies[i] instanceof PoolCellInfo )ok ++ ;
         else problem ++ ;
      }
      StringBuffer sb = new StringBuffer() ;
      sb.append("Pool Scan Results\n").
         append("  Pools Ok : ").append(ok).append("\n").
         append("  Pools Missing : ").append(not_exists).append("\n").
         append("  Pools Timed Out : ").append(timeouts).append("\n").
         append("  Pools Reported Problem : ").append(problem).append("\n") ;
      setResult( ComponentMonitorable.RESULT_OK , 
                     ""+ok+" ok; "+(timeouts+not_exists+problem)+" bad;" ,
                     sb.toString()
                   ) ;      
   
   }
   private void analysePoolManagerReply( PoolManagerCellInfo info ){
      String [] poolNames = (String[]) info.getPoolCells().toArray();
      
      if( ( poolNames == null ) || ( poolNames.length == 0 ) ){
          setResult( ComponentMonitorable.RESULT_FATAL , 
                     "PoolManager : No Pools configured",
                     "PoolManager misconfiguration"
                   ) ;      
      }
      _requestedPoolReplies = new Object[_requestCount=_requestedPoolReplyCount = poolNames.length] ;

      try{
         for( int i = 0 ; i < poolNames.length ; i++ ){
         
            String poolName = poolNames[i] ;
            
            try{
               _connection.sendObject( poolName , "info" , this , POOL_REQUERST_OFFSET+i ) ;
            }catch(Exception ee ){
               _requestedPoolReplies[i] = ee ;
               _requestedPoolReplyCount -- ;
            }
            Thread.currentThread().sleep(100L);
         }
      }catch( Exception ee ){
          _requestedPoolReplies = null ;
          setResult( ComponentMonitorable.RESULT_FATAL , 
                     "Fatal Error in sending messages to pool(s)",
                     ee.toString()
                   ) ;      
      }
   }
   private void analysePoolManagerReply( Object [] poolNames ){
         
      if( ( poolNames == null ) || ( poolNames.length == 0 ) ){
          setResult( ComponentMonitorable.RESULT_FATAL , 
                     "PoolManager : No Pools configured",
                     "PoolManager misconfiguration"
                   ) ;      
      }
      setAction( "PoolManager replied with "+poolNames.length+" pools");
      _requestedPoolReplies = new Object[_requestCount=_requestedPoolReplyCount = poolNames.length] ;

      try{
         for( int i = 0 ; i < poolNames.length ; i++ ){
         
            String poolName = poolNames[i].toString() ;
            
            try{
               _connection.sendObject( poolName , "xgetcellinfo" , this , POOL_REQUERST_OFFSET+i ) ;
               setAction("Sending "+i+" out of "+poolNames.length);
            }catch(Exception ee ){
               _requestedPoolReplies[i] = ee ;
               _requestedPoolReplyCount -- ;
            }
            Thread.currentThread().sleep(100L);
         }
      }catch( Exception ee ){
          _requestedPoolReplies = null ;
          setResult( ComponentMonitorable.RESULT_FATAL , 
                     "Fatal Error in sending messages to pool(s)",
                     ee.toString()
                   ) ;      
      }
   }
   public void    start() throws IllegalStateException {
      synchronized( this ){
      
          setActive(true);
          askPoolManagerForPools();
      }
   }
   private void askPoolManagerForPools(){
      setAction("Asking PoolManager for pool list");
      try{
          _connection.sendObject( "PoolManager" ,
                                  "psux ls pool" ,
                                  this ,
                                  100
                                  );
              
      }catch(Exception ee ){
          System.err.println("Exception in sending info to PnfsManager :  "+ee ) ;
          setResult( ComponentMonitorable.RESULT_FATAL , 
          "Exception while sending 'xgetcellinfo' to PoolManager" ) ;
      }
   
   }

}
