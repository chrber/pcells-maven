// $Id: LoginBrokerMonitor.java,v 1.1 2007/04/29 11:13:22 cvs Exp $
//
package org.pcells.services.gui.login ;
//

import dmg.cells.nucleus.NoRouteToCellException;
import dmg.cells.services.login.LoginBrokerInfo;
import org.pcells.services.connection.DomainConnection;
import org.pcells.services.connection.DomainConnectionListener;
import org.pcells.services.gui.monitoring.ComponentMonitorAdapter;
import org.pcells.services.gui.monitoring.ComponentMonitorable;

import java.util.*;


public class LoginBrokerMonitor 
       extends ComponentMonitorAdapter 
       implements DomainConnectionListener {

   private DomainConnection   _connection              = null ; 
   private long               _loginBrokerRequestStart = 0L ;
   
   public LoginBrokerMonitor( String name , String description , String argString , DomainConnection connection ){
   
      super( name , description , argString ) ;

      _connection  = connection ;

   }
   
   public void domainAnswerArrived( Object obj , int subid ){
       System.out.println( "Answer ("+subid+") : "+obj.toString() ) ;
       
       if( subid == 100 )handleLoginBrokerReply( obj , subid ) ;
       
       
   }
   public void handleLoginBrokerReply( Object obj , int subid ){
       if( obj == null ){
          setResult( ComponentMonitorable.RESULT_FATAL , 
                     "Got timeout, sending 'info' to LoginBroker" ) ;
       }else if( obj instanceof NoRouteToCellException ){
          setResult( ComponentMonitorable.RESULT_FATAL , 
          "LoginBroker not present" ) ;
       }else if( obj instanceof Exception ){
          setResult( ComponentMonitorable.RESULT_FATAL , 
          "Got Exception from LoginBroker : " + ((Exception)obj).getMessage()) ;
       }else if( obj instanceof LoginBrokerInfo [] ){
          LoginBrokerInfo [] info = (LoginBrokerInfo [])obj ;
          if( info.length == 0 ){
              setResult( ComponentMonitorable.RESULT_FATAL , 
              "Nothing registered with LoginBroker") ;
          }else{
              analyseLoginBrokerEntries( info ) ;
          }
       }else{
            setResult( ComponentMonitorable.RESULT_FATAL , 
            "Got illegal reply from LoginBroker : "+obj.getClass().getName()) ;
       }
   
   }
   private void analyseLoginBrokerEntries( LoginBrokerInfo [] info ){
      HashMap map = new HashMap() ;
      for( int i = 0 ; i < info.length ; i++ ){
         String family = info[i].getProtocolFamily() ;
         List list = (List)map.get(family);
         if( list == null )map.put( family , list = new ArrayList() ) ;
         list.add( info[i] ) ;
      }
      //
      // do we have entries at all ?
      //
      if( map.size() == 0 ){
         setResult( ComponentMonitorable.RESULT_FATAL , 
         "No entries in LoginBroker at all") ;
         return ;
      }
      StringBuffer sb = new StringBuffer() ;
      for( Iterator it = map.entrySet().iterator() ; it.hasNext() ; ){
         Map.Entry entry = (Map.Entry)it.next() ;
         List list = (List)entry.getValue() ;
         sb.append(entry.getKey()).append(" : ").append( list.size() ).append("\n");
      }
      setResult( ComponentMonitorable.RESULT_OK , ""+map.size()+" supported protocols" , sb.toString() ) ;
   }
   public void start() throws IllegalStateException {
      synchronized( this ){
      
          setActive(true);

          askForLoginManagerInfo();
      }
   }
   private void askForLoginManagerInfo(){
      setAction("Asking LoginManager for doors");
      try{
          _connection.sendObject( "LoginBroker" ,
                                  "ls -binary" ,
                                  this ,
                                  100
                                  );
          _loginBrokerRequestStart = System.currentTimeMillis() ;              
      }catch(Exception ee ){
          System.err.println("Exception in sending info to PnfsManager :  "+ee ) ;
          setResult( ComponentMonitorable.RESULT_FATAL , "Exception while sending 'info' to PnfsManager" ) ;
      }
   
   }

}
