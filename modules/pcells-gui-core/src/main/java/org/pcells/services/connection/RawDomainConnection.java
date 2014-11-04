// $Id: RawDomainConnection.java,v 1.2 2006-11-19 09:14:19 patrick Exp $
//
package org.pcells.services.connection ;
//

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;

/**
 */
public class RawDomainConnection extends DomainConnectionAdapter {

   private final static Logger _logger = LoggerFactory.getLogger(RawDomainConnection.class);
   private String _hostname;
   private int    _portnumber;
   private Socket _socket;

   public RawDomainConnection( String hostname , int portnumber ){
      _hostname   = hostname ;
      _portnumber = portnumber ;
   }
   @Override
   public void go() throws Exception {

      _socket = new Socket( _hostname , _portnumber ) ;
      setIoStreams( _socket.getInputStream() , _socket.getOutputStream() ) ;

      try{
         super.go() ;
      }finally{
         try{
             _socket.close();
         } catch (Exception ee ) {
             _logger.error("Problem during closing socket for RawDomainConnection.");
         }
      }

   }
   private class RunConnection
           implements Runnable, DomainConnectionListener, DomainEventListener {


      public RunConnection(  )
      {
         _logger.debug("class runConnection init");
         addDomainEventListener(this);
         new Thread(this).start() ;
      }
      @Override
      public void run(){
         try{
            go() ;
         }catch(Exception ee ){
            _logger.error("RunConnection got : "+ee);
            ee.printStackTrace();
         }
      }
      @Override
      public void domainAnswerArrived( Object obj , int id ){
          _logger.debug("Answer : "+obj);
          if( id == 54 ){
             try{
                sendObject(  "logoff" , this , 55 ) ;
             }catch(Exception ee ){
                _logger.error("Exception in sendObject"+ee);
             }
          }
      }
      @Override
      public void connectionOpened( DomainConnection connection ){
         _logger.debug("DomainConnection : connectionOpened");
         try{
            sendObject( "System" , "ps -f" , this , 54 ) ;
         }catch(Exception ee ){
            _logger.error("Exception in sendObject"+ee);
         }
      }
      @Override
      public void connectionClosed( DomainConnection connection ){
         _logger.debug("DomainConnection : connectionClosed");
      }
      @Override
      public void connectionOutOfBand( DomainConnection connection ,
                                       Object subject                ){
         _logger.error("DomainConnection : connectionOutOfBand");
      }
   }
   public void test()
   {
      _logger.debug("Starting test");
      new RunConnection() ;
   }
   public static void main( String [] args )
   {
      if( args.length < 2 ){

          System.err.println("Usage : <hostname> <portNumber>");
          System.exit(4);
      }
      String hostname = args[0] ;
      int portnumber  = Integer.parseInt( args[1] ) ;
      System.out.println("Creating new Raw...");
      RawDomainConnection connection = new RawDomainConnection( hostname , portnumber ) ;
      System.out.println("Starting Test");
      connection.test() ;


   }
}
