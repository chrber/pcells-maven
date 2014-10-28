// $Id: PoolCostDataModel.java,v 1.1 2008/08/04 19:02:57 cvs Exp $
//
package org.dcache.gui.pluggins.costs ;
//
import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import org.pcells.services.gui.util.histogram.*;
import org.dcache.gui.pluggins.pools.PoolGroupLinkCollector ;

import diskCacheV111.poolManager.PoolManagerCellInfo ;
import diskCacheV111.vehicles.CostModulePoolInfoTable ;
import diskCacheV111.pools.PoolCostInfo ;


public class PoolCostDataModel extends AbstractHistogramModel {

     private boolean _drawBoardDetails = false ;
     private boolean _drawMoverCosts   = false ;
     private float   _scale            = (float)1.0 ;
     
     private float [][] values = null ;
     private String  [] names  = null ;

     private ArrayList<ExtendedPoolCostInfo> _poolInfoList = new ArrayList<ExtendedPoolCostInfo>() ; ;

     public PoolCostDataModel(Collection<String> poolList){

         for( String poolName : poolList ){

	    _poolInfoList.add( new ExtendedPoolCostInfo(poolName) ) ;
            //System.out.println("Creating ExtendedPoolCostInfo for " +poolName) ;
	 }
         Collections.sort( _poolInfoList ) ;

     }
     public  Object getParameterAt( int i ){
	switch(i){
	   case 0 : 
	   //
	   //  some stearing bits
	   //   bit  0 : split bar : 0 = don't split , 1 = split
	   //
	   return 0 ;
	   
	   case 1 :
	   //
	   // asks for the index0 in the array to be displayed.
	   //
	   return _drawBoardDetails ? 2 : 0 ;
	   
	   
	   case 2 : 
	   //
	   // asks for the index1 in the array to be displayed.
	   //
	   return _drawBoardDetails ?11 : 0 ;
	   
	   case 3 : 
	   //
	   // scale picture.
	   //
	   return _scale ;

	}
	return 0 ;
     }
     public  int getParameterCount(){
	return 4 ;
     }

     public  String getNameAt( int i ){
       System.out.println("getNameAt("+i+") : "+names[i] ) ;
       return names == null ? null : names[i] ;
     }
     public  float [] getDataAt( int i ){
        System.out.println("getDataAt("+i+") : "+values[i]);
	return values == null ? new float[0] : values[i] ;
     }
     public  int getDataCount(){	   
	return values == null ? 0 : values.length ;
     }
     public void fireParameters(){
	fireParametersChanged(this);
     }
     public void fireContentsChanged(){
	fireContentsChanged(this);
     }
     public void fireStructureChanged(){
	fireStructureChanged(this);
     }
     public void setCostDetails( boolean details ){
        _drawBoardDetails = details ;
	fireParametersChanged(this);
     }
     public void setCostByMover( boolean details ){
        _drawMoverCosts = details ;
	fillNewContents() ;
	fireContentsChanged(this);
     }
     public void setScale( float scale ){
     
        _scale = scale ;
	fireParametersChanged(this);
	
     }
     public void updateCosts( Map<String,PoolCostInfo> costMap ){

        for( ExtendedPoolCostInfo info : _poolInfoList ){

	    PoolCostInfo cost = costMap.get( info.getName() ) ;
	    System.out.println("Updateing cost for " + info.getName() + " : "+cost ) ;
	    if( cost != null )info.setPoolCostInfo(cost);


	}
	fillNewContents() ;
        fireContentsChanged() ;
     }
     public void fillNewContents(){
        //
	// here the content of the poolInfoList has been updated.
	// We need to create the new [][] values and [] names.
	//
	int size = _poolInfoList.size() ;
	if( ( values == null ) || ( values.length != size ) ){
	   values = new float[size][] ;
	   names  = new String[size] ;
	}
	int i = 0 ;
	for( ExtendedPoolCostInfo info : _poolInfoList ){
	   names[i]  = info.getName() ;
	   values[i] = calculateCostArray( info , _drawMoverCosts ) ;
	   i++ ;
	}

     }

     private float [] calculateCostArray( ExtendedPoolCostInfo info , boolean countMovers ){
      
	  long [] [] cost = info.getPoolCostArray() ;
	  
	  if( cost == null ){
	     System.err.println("Couldn't find cost for : "+info.getName() ) ;
	     return null ;
	  }
	  float avarage   = (float)0.0 ;
	  float [] result = new float[cost.length*2+2] ;
	  int costCounter = 0 ;

	  for( int i = 0 ; i < cost.length ; i++ ){

             long  [] x = cost[i] ;
	     if( x[1] == 0 )continue ;

             avarage += (float) (x[0]+x[2]) / (float)x[1] ;
	     result[2+i*2]   = (float)x[0]  ;
	     result[2+i*2+1] = (float)x[2]  ;

	     costCounter ++ ;

	  }
	  result[0] = avarage ;
	  
	  // result[1] = info.getSpaceCost....
	  result[1] = (float)0.0 ;
	  
	  if( ! countMovers ){
	      //
	      // create the real cpu cost
	      //
	      result[0] = result[0]/(float)costCounter;
              for( int i = 2 ; i < result.length ; i++ )result[i] = result[i]/(float)costCounter;
	  
	  }
          {
	     StringBuffer sb = new StringBuffer() ;
	     sb.append(info.getName()+ "=" ) ;
	     for( int i = 0 ; i < result.length ; i++ ){
	        sb.append(" ("+i+")="+result[i]+";");
	     }
	     System.out.println(sb.toString());
	  }
	  return result ;

      }

   }


