// $Id: DomainConnectionAdapter.java,v 1.2 2006-11-19 09:14:19 patrick Exp $
//
package org.pcells.services.connection ;

import dmg.cells.applets.login.DomainObjectFrame;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 */
public class DomainConnectionAdapter implements DomainConnection {

     private final static Logger _logger = org.slf4j.LoggerFactory.getLogger(DomainConnectionAdapter.class);

     private Map<DomainObjectFrame, DomainConnectionListener>   _packetHash = new HashMap<>() ;
     private final Object    _ioLock     = new Object() ;
     private int       _ioCounter  = 100 ;
     private List<DomainEventListener> _listener   = new ArrayList<>() ;
     private boolean   _connected;

     private InputStream  _inputStream;
     private OutputStream _outputStream;
     private Reader       _reader;
     private Writer       _writer;
     protected ObjectOutputStream _objOut;
     protected ObjectInputStream  _objIn;


     @Override
     public String getAuthenticatedUser(){ return "Unknown" ; }
     public void setIoStreams( InputStream in , OutputStream out){
        setIoStreams( in , out, null , null ) ;
     }
     public void setIoStreams( InputStream in , OutputStream out ,
                               Reader reader , Writer writer    ){

       _inputStream  = in ;
       _outputStream = out ;
       _reader       = reader ;
       _writer       = writer ;

     }
     public void go() throws Exception {
        _logger.debug("runConnection started");
        runConnection() ;
         _logger.debug("runConnection OK");

        informListenersOpened() ;

         _logger.debug("runReceiver starting");
        try{
           runReceiver() ;
        }finally{
            _logger.debug("runReceiver finished");
           informListenersClosed() ;
        }


     }
     public void close()throws IOException {
        _objOut.close() ;
     }
     private static class MyFilter extends FilterInputStream {
        public MyFilter( InputStream in ){
          super(in);
        }
        @Override
        public int read() throws IOException {
          int r = super.read() ;
          return r ;
        }
        @Override
        public int read( byte [] data , int offset , int len ) throws IOException {
           int r = super.read( data , offset ,1 );
           return r;
        }
        @Override
        public int read( byte [] data ) throws IOException {

           byte [] x = new byte[1];
           int r = super.read( x );
           data[0] = x[0];
           return r;
        }
     }
     private void runConnection() throws IOException {

         InputStream inputstream = new MyFilter( _inputStream );
         BufferedReader reader = new BufferedReader(
                            _reader == null ?
                             new InputStreamReader(inputstream) :
                            _reader , 1 ) ;

         PrintWriter writer = new PrintWriter( _writer == null ?
                                               new OutputStreamWriter(_outputStream) :
                                               _writer ) ;

         writer.println( "$BINARY$" ) ;
         writer.flush() ;
         String  check;
         do{

            check = reader.readLine()   ;
            //System.out.println(" >>"+check+"<<");

         }while( ! check.equals( "$BINARY$" )  ) ;
         _logger.debug("opening object streams");
         _objOut = new ObjectOutputStream( _outputStream ) ;
         _logger.debug("opening input object streams");
         _objIn  = new ObjectInputStream( inputstream)  ;

     }
     protected void runReceiver() throws Exception {

        Object            obj;
        DomainObjectFrame frame;
        DomainConnectionListener listener;

        while( true ){

           if( ( obj = _objIn.readObject()  ) == null ) {
               break;
           }
           if( ! ( obj instanceof DomainObjectFrame ) ) {
               continue;
           }

           synchronized( _ioLock ){

              frame    = (DomainObjectFrame) obj ;
              _logger.debug("Frame with ID {} received", frame.getId());
              listener = _packetHash.remove( frame ) ;
              if( listener == null ){
                 _logger.debug("Message without receiver : "+frame );
                 continue ;
              }
           }
           try{
               listener.domainAnswerArrived( frame.getPayload() , frame.getSubId() ) ;
           }catch(Exception eee ){
               _logger.error("Problem in domainAnswerArrived : {}", eee );
           }
        }
     }
     @Override
     public int sendObject( Serializable obj ,
                            DomainConnectionListener listener ,
                            int id
                                                 ) throws IOException {

         synchronized( _ioLock ){

             if( ! _connected ) {
                 throw new IOException("Not connected");
             }

             DomainObjectFrame frame =
                     new DomainObjectFrame( obj , ++_ioCounter , id ) ;
             _logger.debug("Frame with ID {} sent to destination {}", frame.getId(), frame.getDestination());
             _packetHash.put(frame, listener) ;
             _objOut.writeObject(frame) ;
             _objOut.reset() ;
             _objOut.flush();
             return _ioCounter ;
         }
     }
     @Override
     public int sendObject( String destination ,
                            Serializable obj ,
                            DomainConnectionListener listener ,
                            int id
                                                 ) throws IOException {
         _logger.debug("Sending : "+obj );
         synchronized( _ioLock ){
             if( ! _connected ) {
                 throw new IOException("Not connected");
             }
             DomainObjectFrame frame =
                     new DomainObjectFrame( destination , obj , ++_ioCounter , id ) ;
             _logger.debug("Frame with ID {} sent to destination {}", frame.getId(), frame.getDestination());
             _packetHash.put(frame, listener) ;
             _objOut.writeObject(frame);
             _objOut.reset() ;
             _objOut.flush();
             return _ioCounter ;
         }
     }
     @Override
     public void addDomainEventListener( DomainEventListener listener ){
        synchronized( _ioLock ){
          _listener.add(listener) ;
          if( _connected ){
              try{  listener.connectionOpened( this ) ;
              }catch( Throwable t ){
                 _logger.error("Problem while adding EventListener: {}", t);
              }
          }
        }
     }
     @Override
     public void removeDomainEventListener( DomainEventListener listener ){
        synchronized( _ioLock ){
          _listener.remove(listener);
        }
     }
     protected void informListenersOpened(){
        List<DomainEventListener> array = new ArrayList<>( _listener );
        synchronized( _ioLock ){
           _connected = true ;
           for ( DomainEventListener listener: array ){

               try{  listener.connectionOpened( this ) ;
               }catch( Throwable t ){
                  _logger.error("Problem while checking listeners (listener opening): {}", t);
               }
           }
        }
     }
     protected void informListenersClosed(){
         List<DomainEventListener> array = new ArrayList<>( _listener );
        synchronized( _ioLock ){
           _connected = false ;
           for ( DomainEventListener listener: array ){
               try{  listener.connectionClosed( this ) ;
               }catch( Throwable t ){
                   _logger.error("Problem while checking listeners (listener closed): {}", t);
               }
           }
        }
     }

}
