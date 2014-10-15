// $Id: PnfsManagerMonitor.java,v 1.6 2007/03/16 06:21:59 cvs Exp $
//
package org.dcache.gui.pluggins.monitoring;
//

import dmg.cells.nucleus.NoRouteToCellException;
import org.pcells.services.connection.DomainConnection;
import org.pcells.services.connection.DomainConnectionListener;
import org.pcells.services.gui.monitoring.ComponentMonitorAdapter;
import org.pcells.services.gui.monitoring.ComponentMonitorable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Pattern;

public class PnfsManagerMonitor 
       extends ComponentMonitorAdapter 
       implements DomainConnectionListener,
                  ActionListener {

   private DomainConnection   _connection  = null ; 
   private int     _transactionCounter = 0 ;
   private float   _avarageQueueSize   = (float)0.0 ;
   private Pattern _queue_pattern      = Pattern.compile("[ ]*Threads \\(([0-9]+)\\) Queue[ ]*");
   private Pattern _thread_pattern     = Pattern.compile("[ ]*\\[([0-9]+)\\] ([0-9]+)[ ]*");
   private int _numberOfThreads = 0 ;
   private int _numberOfRetries = 10 ;
    
   private javax.swing.Timer _timer = new javax.swing.Timer( 1000, this);

   public PnfsManagerMonitor( String name , String description , String argsString , DomainConnection connection ){
      super( name , description , argsString ) ;

      _connection  = connection ;

   }
   public void actionPerformed( ActionEvent event ){
       Object source = event.getSource() ;
       if( source == _timer ){
           askForInfo() ;       
       }
   }
   public void domainAnswerArrived( Object obj , int subid ){
       System.out.println( "Answer ("+subid+") : "+obj.toString() ) ;
       
       if( obj == null ){
          setResult( ComponentMonitorable.RESULT_FATAL , 
                     "Got timeout, sending 'info' to PnfsManager" ) ;
       }else if( obj instanceof NoRouteToCellException ){
          setResult( ComponentMonitorable.RESULT_FATAL , 
                     "PnfsManager not present",
                     obj.toString()
                   ) ;
       }else if( obj instanceof Exception ){
          setResult( ComponentMonitorable.RESULT_FATAL , 
                     "Got exception from PnfsManager",
                     obj.toString()
                   ) ;
       }else if( obj instanceof String ){
          try{
             dCacheStringDecoder.PnfsManagerBasicInfo basicInfo = dCacheStringDecoder.PnfsManagerBasicInfo.decode( (String)obj ) ;
             _avarageQueueSize += basicInfo.getAvarageThreadQueueSize() ;
             _numberOfThreads   = basicInfo.getNumberOfThreads() ;
             
             synchronized( this ){
                 _transactionCounter++ ;
                 if( _transactionCounter < _numberOfRetries ){
                    _timer.setRepeats(false);
                    _timer.start();
                    setAction("Round "+_transactionCounter+" : Delay");
                 }else{
                    float result = _avarageQueueSize/(float)_numberOfRetries ;
                    int code = result < (float)2.0 ? ComponentMonitorable.RESULT_OK : ComponentMonitorable.RESULT_WARNING ;
                    setResult( code , 
                               "Avarage Queue Size : "+result ,
                               "Number of Probes   : "+_numberOfRetries+"\n"+
                               "Number of Threads  : "+_numberOfThreads+"\n"+
                               "Avarage Queue Size : "+result 
                             ) ;
                 }
              }
           }catch(Exception ee ){
              setResult( ComponentMonitorable.RESULT_FATAL , "Problem decoding PnfsManager info message : "+ee.getMessage()) ;
           }
       }else{
       
       }
       
   }
   public void    start() throws IllegalStateException {
      synchronized( this ){
      
          setActive(true);
          _transactionCounter = 0 ;
          _avarageQueueSize   = (float) 0.0; 
          askForInfo();
      }
   }
   private void askForInfo(){
      setAction("Round "+_transactionCounter+" : Sending Query");
      try{
          _connection.sendObject( "PnfsManager" ,
                                  "info" ,
                                  this ,
                                  100
                                  );
              
      }catch(Exception ee ){
          System.err.println("Exception in sending info to PnfsManager :  "+ee ) ;
          setResult( ComponentMonitorable.RESULT_FATAL , "Exception while sending 'info' to PnfsManager" ) ;
      }
   
   }

}
