// $Id: SrmMonitor.java,v 1.6 2007/04/29 11:38:39 cvs Exp $
//
package org.dcache.gui.pluggins.monitoring ;
//
 
import dmg.cells.nucleus.NoRouteToCellException;
import dmg.cells.services.login.LoginBrokerInfo;
import org.pcells.services.connection.DomainConnection;
import org.pcells.services.connection.DomainConnectionListener;
import org.pcells.services.gui.monitoring.ComponentMonitorAdapter;
import org.pcells.services.gui.monitoring.ComponentMonitorable;

public class SrmMonitor 
       extends ComponentMonitorAdapter 
       implements DomainConnectionListener {

   private DomainConnection   _connection  = null ; 
   private StringBuffer        _details     = new StringBuffer() ;
   
   public SrmMonitor( String name , String description , String argsString , DomainConnection connection ){
      super( name , description , argsString ) ;

      _connection  = connection ;

   }
   public void domainAnswerArrived( Object obj , int subid ){
   
       System.out.println( "Answer ("+subid+") : "+obj.toString() ) ;
       
       if( subid == 100 )handleLoginBrokerReply( obj , subid ) ;
       if( subid == 101 )handleSrmInfoReply( obj , subid ) ;
       
       
   }
   private String _srmAddress = null ;
   private long   _requestSrmInfoTimer = 0L ;
   private long requestTook(){
      return ( System.currentTimeMillis() - _requestSrmInfoTimer ) / 1000L ;
   }
   public void handleLoginBrokerReply( Object obj , int subid ){
       if( obj == null ){
          setResult( ComponentMonitorable.RESULT_FATAL , 
          "Got timeout, sending 'info' to srm-LoginBroker ("+requestTook()+")") ;
       }else if( obj instanceof NoRouteToCellException ){
          setResult( ComponentMonitorable.RESULT_FATAL , 
          "srm-LoginBroker seems not to be present") ;
       }else if( obj instanceof Exception ){
          setResult( ComponentMonitorable.RESULT_FATAL , 
          "Exception while sending 'info' to srm-LoginBroker : " + ((Exception)obj).getMessage()) ;
       }else if( obj instanceof LoginBrokerInfo [] ){
          LoginBrokerInfo [] info = (LoginBrokerInfo [])obj ;
          if( info.length == 0 ){
              setResult( ComponentMonitorable.RESULT_FATAL , 
              "No SRM registered with SRM loginBroker") ;
          }else{
            _srmAddress = info[0].getCellName()+"@"+info[0].getDomainName() ;
            askForSrmInfo() ;
          }
       }else{
            setResult( ComponentMonitorable.RESULT_FATAL , 
            "Strange reply from SRM loginBroker : "+obj.getClass().getName()) ;
       }
   
   }
   public void handleSrmInfoReply( Object obj , int subid ){
       if( obj == null ){
          setResult( ComponentMonitorable.RESULT_FATAL , 
          "Got timeout, sending 'info' to {SRM} ("+requestTook()+")" ) ;
       }else if( obj instanceof NoRouteToCellException ){
          setResult( ComponentMonitorable.RESULT_FATAL , 
          "SRM seems not to be present") ;
       }else if( obj instanceof Exception ){
          setResult( ComponentMonitorable.RESULT_FATAL , 
          "Got exception from 'info' to {SRM} : " + ((Exception)obj).getMessage()) ;
       }else if( obj instanceof String ){
          setResult( ComponentMonitorable.RESULT_OK , 
          "Answer took : "+(System.currentTimeMillis()-_requestSrmInfoTimer)+" millis");
          try{
             dCacheStringDecoder.SrmLsInfo info = dCacheStringDecoder.SrmLsInfo.decode( (String)obj ) ;
             
          }catch(Exception eee  ){
             setResult( ComponentMonitorable.RESULT_FATAL , 
             "Couldn't decode SRM info message", obj.toString())  ;
          }
       }else{
          setResult( ComponentMonitorable.RESULT_FATAL , 
          "Strange reply from SRM loginBroker : "+obj.getClass().getName()) ;
       }
   
   }
   public void start() throws IllegalStateException {
      synchronized( this ){
      
          setActive(true);

          askForLoginManagerInfo();
      }
   }
   private void askForLoginManagerInfo(){
      setAction("Asking LoginManager for SRM location");
      try{
          _connection.sendObject( "LoginBroker" ,
                                  "ls -binary" ,
                                  this ,
                                  100
                                  );
              
          _requestSrmInfoTimer = System.currentTimeMillis(); 
      }catch(Exception ee ){
          System.err.println("Exception in sending info to PnfsManager :  "+ee ) ;
          setResult( ComponentMonitorable.RESULT_FATAL , "Exception while sending 'info' to PnfsManager" ) ;
      }
   
   }
   private void askForSrmInfo(){
      setAction("Asking SRM for info");
       try{
          _connection.sendObject( _srmAddress ,
                                  "info" ,
                                  this ,
                                  101
                                  );
          _requestSrmInfoTimer = System.currentTimeMillis(); 
      }catch(Exception ee ){
          System.err.println("Exception in sending info to PnfsManager :  "+ee ) ;
          setResult( ComponentMonitorable.RESULT_FATAL , "Exception while sending 'info' to PnfsManager" ) ;
      }
   
   }
   private void askForSrmLs(){
      setAction("Asking SRM for 'ls'");
       try{
          _connection.sendObject( _srmAddress ,
                                  "ls" ,
                                  this ,
                                  101
                                  );
          _requestSrmInfoTimer = System.currentTimeMillis(); 
      }catch(Exception ee ){
          System.err.println("Exception in sending info to PnfsManager :  "+ee ) ;
          setResult( ComponentMonitorable.RESULT_FATAL , "Exception while sending 'info' to PnfsManager" ) ;
      }
   
   }

}
