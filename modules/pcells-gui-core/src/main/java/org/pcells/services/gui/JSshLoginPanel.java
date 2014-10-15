// $Id: JSshLoginPanel.java,v 1.3 2006/12/23 18:05:10 cvs Exp $
//
package org.pcells.services.gui ;
//

import dmg.cells.applets.login.DomainObjectFrame;
import dmg.protocols.ssh.*;
import org.pcells.services.connection.DomainConnection;
import org.pcells.services.connection.DomainConnectionListener;
import org.pcells.services.connection.DomainEventListener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
/**
 */
public class      JSshLoginPanel 
       extends    JLoginPanel 
       implements SshClientAuthentication {
       
   private SshDomainConnection _connection = new SshDomainConnection() ;
   private ObjectOutputStream _objOut = null ;
   private ObjectInputStream  _objIn  = null ;
   private Socket             _socket = null ;
   public DomainConnection getDomainConnection(){ return _connection ; }
   public JSshLoginPanel(){ this(null);}
   public JSshLoginPanel( String name ){ 
      super(name) ; 
      addActionListener(
         new ActionListener(){
            public void actionPerformed( ActionEvent event ){
                setMessage( "Calling ConnectionEngine");
                new Thread( new ConnectionThread() ).start() ;
            }
         }
      ) ;
   }


   private class ConnectionThread implements Runnable {
      public void run(){
         try{
            runConnectionProtocol() ;
            _connection.informListenersOpened() ;
         }catch( SshAuthenticationException ae){
            setErrorMessage( "Login Failed") ;
            displayLoginPanel() ;
            return ;
         }catch( Exception e){
            e.printStackTrace();
            setErrorMessage( "Login Failed ("+e+")") ;
            displayLoginPanel() ;
            return ;
         }
         setErrorMessage("");
         setMessage("Connected") ;
         try{
            runReceiver() ;
         }catch(Exception ee ){
            ee.printStackTrace();
            setErrorMessage("Connection Broken");
         }finally{
            try{ _socket.close() ;}catch( Exception ce ){}      
            _connection.informListenersClosed() ;
            displayLoginPanel() ;
         }
      }
      private void runConnectionProtocol() throws Exception {
          setMessage( "Connecting" ) ;
          int             port    = Integer.parseInt( getPortnumber() ) ;
                          _socket = new Socket( getHostname() , port ) ;
          SshStreamEngine engine  = new SshStreamEngine( _socket , JSshLoginPanel.this ) ;
          PrintWriter     writer  = new PrintWriter( engine.getWriter() ) ;
          BufferedReader  reader = new BufferedReader( engine.getReader() ) ;
          setMessage( "Requesting Binary Connection" ) ;
          writer.println( "$BINARY$" ) ;
          writer.flush() ;
          String  check  = null ;
          do{
             check = reader.readLine()   ;      
          }while( ! check.equals( "$BINARY$" ) ) ;
          setMessage("Binary acknowledged");
          _objOut = new ObjectOutputStream( engine.getOutputStream() ) ;
          _objIn  = new ObjectInputStream( engine.getInputStream() ) ;

      }
      private void runReceiver() throws Exception {

         Object            obj   = null ;
         DomainObjectFrame frame = null ;
         DomainConnectionListener listener = null ;
         while( true ){
            if( ( obj = _objIn.readObject()  ) == null )break ;
            System.out.println("Received : "+obj ) ;
            if( ! ( obj instanceof DomainObjectFrame ) )continue ;
            synchronized( _connection._ioLock ){
               frame    = (DomainObjectFrame) obj ;
               listener = (DomainConnectionListener)_connection._packetHash.remove( frame ) ;
               if( listener == null ){
                  System.err.println("Message without receiver : "+frame ) ;
                  continue ;
               }
            }
            try{
                System.out.println("Delivering : "+frame.getPayload() ) ;
                listener.domainAnswerArrived( frame.getPayload() , frame.getSubId() ) ;
            }catch(Exception eee ){
                eee.printStackTrace();
                System.out.println( "Problem in domainAnswer Arrived : "+eee ) ;
            }
         }
      }
  }
  //===============================================================================
  //
  //   domain connection interface 
  //  
  public class SshDomainConnection implements DomainConnection {
     private Hashtable _packetHash = new Hashtable() ;
     private Object    _ioLock     = new Object() ; 
     private int       _ioCounter  = 100 ;
     private Vector    _listener   = new Vector() ;
     private boolean   _connected  = false ;
     
     public String getAuthenticatedUser(){ return getLogin() ; }
     
     public int sendObject( Object obj , 
                            DomainConnectionListener listener ,
                            int id 
                                                 ) throws IOException {
         System.out.println("Sending : "+obj ) ;
         synchronized( _ioLock ){
             if( ! _connected )throw new IOException( "Not connected" ) ;
             DomainObjectFrame frame = 
                     new DomainObjectFrame((Serializable) obj, ++_ioCounter , id ) ;
             _objOut.writeObject( frame ) ;
             _objOut.reset() ;
             _packetHash.put( frame , listener ) ;
             return _ioCounter ;
         }
     }
     public int sendObject( String destination ,
                            Object obj , 
                            DomainConnectionListener listener ,
                            int id 
                                                 ) throws IOException {
         System.out.println("Sending : "+obj ) ;
         synchronized( _ioLock ){
             if( ! _connected )throw new IOException( "Not connected" ) ;
             DomainObjectFrame frame = 
                     new DomainObjectFrame( destination , (Serializable) obj, ++_ioCounter , id ) ;
             _objOut.writeObject( frame ) ;
             _objOut.reset() ;
             _packetHash.put( frame , listener ) ;
             return _ioCounter ;
         }
     }
     public void addDomainEventListener( DomainEventListener listener ){
        synchronized( _ioLock ){
          _listener.addElement(listener) ;
          if( _connected ){
              try{  listener.connectionOpened( this ) ;
              }catch( Throwable t ){
                 t.printStackTrace() ;
              }
          }
        }
     }
     public void removeDomainEventListener( DomainEventListener listener ){
        synchronized( _ioLock ){
          _listener.removeElement(listener);
        }
     }
     private void informListenersOpened(){
        synchronized( _ioLock ){
           _connected = true ;
           Vector v = (Vector)_listener.clone() ;
           Enumeration e = v.elements() ;
           while( e.hasMoreElements() ){
               DomainEventListener listener = (DomainEventListener)e.nextElement() ;
               try{  listener.connectionOpened( this ) ;
               }catch( Throwable t ){
                  t.printStackTrace() ;
               }
           }
        }
     }
     private void informListenersClosed(){
        synchronized( _ioLock ){
           _connected = false ;
           Vector v = (Vector)_listener.clone() ;
           Enumeration e = v.elements() ;
           while( e.hasMoreElements() ){
               DomainEventListener listener = (DomainEventListener)e.nextElement() ;
               try{  listener.connectionClosed( this ) ;
               }catch( Throwable t ){
                  t.printStackTrace() ;
               }
           }
        }
     }
  }
  //
  //
  /////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////////////
  //
  //   Client Authentication interface 
  //   
  private int _requestCounter = 0 ;
  public boolean isHostKey( InetAddress host , SshRsaKey keyModulus ) {


//      System.out.println( "Host key Fingerprint\n   -->"+
//                      keyModulus.getFingerPrint()+"<--\n"   ) ;

//     NOTE : this is correctly done in : import dmg.cells.applets.login.SshLoginPanel

      return true ;
  }
  public String getUser( ){
     _requestCounter = 0 ;
     String loginName = getLogin() ;
     System.out.println( "getUser : "+loginName ) ;
     return loginName ;
  }
  public SshSharedKey  getSharedKey( InetAddress host ){ 
     return null ; 
  }

  public SshAuthMethod getAuthMethod(){  
      String password = getPassword() ;
      System.out.println("getAuthMethod("+_requestCounter+") "+password) ;
      return _requestCounter++ > 2 ? 
             null : 
             new SshAuthPassword( password) ;
  }
}
