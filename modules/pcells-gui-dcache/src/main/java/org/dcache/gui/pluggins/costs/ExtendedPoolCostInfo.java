// $Id: ExtendedPoolCostInfo.java,v 1.1 2008/08/04 19:02:56 cvs Exp $
//
package org.dcache.gui.pluggins.costs ;
//

import diskCacheV111.pools.PoolCostInfo;

public class ExtendedPoolCostInfo implements Comparable {

   private PoolCostInfo _info = null ;
   private String       _name = null ;
   public long [] []    _infoArray = null ;
   
   public ExtendedPoolCostInfo( String poolName ){
      _name = poolName ;
      
   }
   public int compareTo( Object oinfo ){
      ExtendedPoolCostInfo info = (ExtendedPoolCostInfo)oinfo ;
      return _name.compareTo(info._name);
   }
   public String getName(){
      return _name ;
   }
   public String toString(){
      return _name+" : "+_info ;
   }
   public void setPoolCostInfo( PoolCostInfo info ){
      _info = info ;
      convertPoolCostInfo(info) ;
   }
   public PoolCostInfo getPoolCostInfo(){
      return _info ;
   }
   public long [][] getPoolCostArray(){
      return _infoArray;
   }
   private void convertPoolCostInfo( PoolCostInfo info ){
   
      long [][] array = new long[5][] ;

      PoolCostInfo.PoolQueueInfo queueInfo = _info.getStoreQueue() ;
      long [] queue = new long[3] ;
      queue[0] = queueInfo.getActive() ;
      queue[1] = queueInfo.getMaxActive() ;
      queue[2] = queueInfo.getQueued() ;
      
      array[0] = queue ;
      
      queueInfo = info.getRestoreQueue() ;
      queue = new long[3] ;
      queue[0] = queueInfo.getActive() ;
      queue[1] = queueInfo.getMaxActive() ;
      queue[2] = queueInfo.getQueued() ;
      
      array[1] = queue ;
      
      queueInfo = info.getP2pQueue() ;
      queue = new long[3] ;
      queue[0] = queueInfo.getActive() ;
      queue[1] = queueInfo.getMaxActive() ;
      queue[2] = queueInfo.getQueued() ;
      
      array[2] = queue ;
      
      queueInfo = info.getP2pClientQueue() ;
      queue = new long[3] ;
      queue[0] = queueInfo.getActive() ;
      queue[1] = queueInfo.getMaxActive() ;
      queue[2] = queueInfo.getQueued() ;
      
      array[3] = queue ;
      
      queueInfo = info.getMoverQueue() ;
      queue = new long[3] ;
      queue[0] = queueInfo.getActive() ;
      queue[1] = queueInfo.getMaxActive() ;
      queue[2] = queueInfo.getQueued() ;
      
      array[4] = queue ;
   
      _infoArray = array ;
   }

}
                  
