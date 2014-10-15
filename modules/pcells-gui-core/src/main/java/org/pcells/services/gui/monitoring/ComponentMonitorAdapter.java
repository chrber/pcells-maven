// $Id: ComponentMonitorAdapter.java,v 1.2 2007/03/16 05:45:04 cvs Exp $
//
package org.pcells.services.gui.monitoring ;
//

import java.awt.event.ActionListener;


public class ComponentMonitorAdapter implements ComponentMonitorable {

       private String  _name = "" ;
       private String  _shortDescription = "" ;
       private int     _resultCode       = ComponentMonitorable.RESULT_ACTIVE ;
       private String  _actionMessage    = "IDLE" ;
       private boolean _isActive         = false ;
       private long    _shouldEnd        = 0L ;
       private String  _details          = null ;
       
       private class MonitorValue implements ComponentMonitorable.ComponentMonitorValue {
          public String getName(){ return "Unknown" ; }
          public String getUnit(){ return "Unit" ; }
          public float  getValue(){ return (float)0.0 ; }
          public long   getTimestamp(){ return 0L ; }
       }
       public ComponentMonitorAdapter( String name , String description , String argsString ){
          _name = name ;
          _shortDescription = description;
          
          argumentInterpreter( argsString ) ;
       }
       public void argumentInterpreter( String arguments ){
       
       }
       public void    setEventListener( ActionListener listener ){
       
       }
       public long getDefaultReplyTimeout(){
          return 10000L ;
       }
       public void  setDetails( String details ){ _details = details ; }
       public String  getShortDescription(){
          return _shortDescription;
       }
       public String  getActionMessage(){
          synchronized( this ){
             return _actionMessage ;
          }
       }
       public int getResultCode(){
          return _resultCode ;
       }
       public void setResult( int code , String message , String details ){
          setResult( code , message ) ;
          _details = details ;
       }
       public void setResult( int code , String message ){
          synchronized( this ){
             _resultCode    = code ;
             _actionMessage = message ;
             _isActive      = false ;
             _details       = null ;
          }

       }       
       public String  getResultDetails(){
          return _details == null ? _actionMessage : _details ;
       }
       public void    start() throws IllegalStateException {
          
       }
       public void setActive( boolean active ) throws IllegalStateException {
          if( active ){
             synchronized( this ){
                if( _isActive ) 
                   throw new 
                   IllegalStateException("Still Active" ) ;
                _isActive      = true ;
                _actionMessage = "Starting" ;
                _resultCode    = ComponentMonitorable.RESULT_ACTIVE ;
             }
          }else{
             _isActive = false ;
          }
       }
       public void    stop(){
       
       }
       public void reset(){
         synchronized( this ){
             _actionMessage = "IDLE" ;
             _resultCode    = ComponentMonitorable.RESULT_IDLE ;
             _isActive      = false ;
             _details       = null ;
         }
       }
       public void setAction( String action ){
         synchronized( this ){
            _actionMessage = action ;
         }
       }
       public boolean isStillActive(){ 
           synchronized( this ){ 
           
              return _isActive ;

           }  
       }
       public String  getName(){ return _name ; }
       
       public ComponentMonitorable.ComponentMonitorValue [] getValueSet(){
          return new MonitorValue[0] ;
       }


}

