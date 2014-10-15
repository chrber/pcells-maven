// $Id: Ssh1DomainConnection.java,v 1.5 2007/02/15 08:18:12 cvs Exp $
//
package org.pcells.services.connection ;
//
import dmg.protocols.ssh.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
/**
 */
public class Ssh1DomainConnection
        extends DomainConnectionAdapter
        implements SshClientAuthentication {

    private static Logger _logger;

    private String _hostname   = null ;
    private int    _portnumber = 0 ;
    private Socket _socket     = null ;
    public SshAuthRsa _rsaAuth   = null ;
    public String     _password  = null ;
    public String     _loginName = "Unknown" ;

    public Ssh1DomainConnection( String hostname , int portnumber ) {
        _logger = LoggerFactory.getLogger(Ssh1DomainConnection.class);
        _hostname = hostname;
        _portnumber = portnumber;

        _logger.debug(this.getClass().getName() + " loaded by : " + this.getClass().getClassLoader().getClass().getName());

    }
    public void go() throws Exception {

        _socket = new Socket( _hostname , _portnumber ) ;
        SshStreamEngine engine  = new SshStreamEngine( _socket , this ) ;

        setIoStreams( engine.getInputStream() ,
                engine.getOutputStream(),
                engine.getReader() ,
                engine.getWriter() );

        try{
            super.go() ;
        }finally{
            try{ _socket.close() ; }catch(Exception ee ){}
        }

    }
    public void setLoginName( String name ){
        _loginName = name ;
    }
    public void setPassword( String password ){
        _password = password ;
    }
    public void setIdentityFile( File identityFile ) throws Exception {

        InputStream in  = new FileInputStream(identityFile) ;
        SshRsaKey   key = new SshRsaKey( in ) ;
        try{ in.close() ; }catch(Exception ee ){}

        _rsaAuth = new SshAuthRsa( key ) ;

    }
    ////////////////////////////////////////////////////////////////////////////////////////
    //
    //   Client Authentication interface
    //
    private int _requestCounter = 0 ;
    public boolean isHostKey( InetAddress host , SshRsaKey keyModulus ) {


        //      _logger.debug( "Host key Fingerprint\n   -->"+
        //                      keyModulus.getFingerPrint()+"<--\n"   ) ;

        //     NOTE : this is correctly done in : import dmg.cells.applets.login.SshLoginPanel

        return true ;
    }
    public String getUser( ){
        _requestCounter = 0 ;
        return _loginName ;
    }
    public SshSharedKey  getSharedKey( InetAddress host ){
        return null ;
    }
    public SshAuthMethod getAuthMethod(){

        SshAuthMethod result = null ;
        if( _requestCounter++ == 0 ){
            if( _rsaAuth == null )result = new SshAuthPassword( _password ) ;
            else result = _rsaAuth ;
        }else if( _requestCounter++ <= 2 ){
            result = new SshAuthPassword( _password ) ;
        }else{
            result = null;
        }
//       _logger.debug("getAuthMethod("+_requestCounter+") "+result) ;
        return result ;
    }
    public static void main( String [] args )throws Exception {
//      if( args.length < 2 ){
//
//          _logger.error("Usage : <hostname> <portNumber>");
//          System.exit(4);
//      }
//      String hostname = args[0] ;
//      int portnumber  = Integer.parseInt( args[1] ) ;
        String hostname = "vm-dcache-deploy5.desy.de" ;
        int portnumber  = 22223 ;
        _logger.debug("Creating new Raw...");
        Ssh1DomainConnection connection = new Ssh1DomainConnection( hostname , portnumber ) ;
        _logger.debug("Starting Test");
        connection.test() ;


    }
    private class RunConnection
            implements Runnable, DomainConnectionListener, DomainEventListener {


        public RunConnection(  ) throws Exception {
            _logger.debug("class runConnection init");
            addDomainEventListener(this);
            setLoginName("admin");
//         setIdentityFile( new File("/home/patrick/.ssh/identity" ) );
            setPassword("dickerelch");
            new Thread(this).start() ;
        }
        public void run(){
            try{
                go() ;
            }catch(Exception ee ){
                _logger.debug("RunConnection got : "+ee);
                ee.printStackTrace();
            }
        }
        public void domainAnswerArrived( Object obj , int id ){
            _logger.debug("Answer : "+obj);
            if( id == 54 ){
                try{
                    sendObject(  "logoff" , this , 55 ) ;
                }catch(Exception ee ){
                    _logger.debug("Exception in sendObject"+ee);
                }
            }
        }
        public void connectionOpened( DomainConnection connection ){
            _logger.debug("DomainConnection : connectionOpened");
            try{
                sendObject( "System" , "ps -f" , this , 54 ) ;
            }catch(Exception ee ){
                _logger.debug("Exception in sendObject"+ee);
            }
        }
        public void connectionClosed( DomainConnection connection ){
            _logger.debug("DomainConnection : connectionClosed");
        }
        public void connectionOutOfBand( DomainConnection connection ,
                                         Object subject                ){
            _logger.debug("DomainConnection : connectionOutOfBand");
        }
    }
    public void test() throws Exception {
        _logger.debug("Starting test");
        new RunConnection() ;
    }

}
