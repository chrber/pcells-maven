// $Id: DeepSpaceScan.java,v 1.5 2008/11/08 15:27:54 cvs Exp $
//
package org.pcells.services.gui ;
//

import dmg.cells.network.CellDomainNode;
import dmg.cells.nucleus.CellDomainInfo;
import dmg.cells.nucleus.CellTunnelInfo;
import org.pcells.services.connection.DomainConnection;
import org.pcells.services.connection.DomainConnectionListener;
import org.pcells.util.Logable;

import java.awt.AWTEventMulticaster;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

public  class      DeepSpaceScan 
        implements DomainConnectionListener {

    private DomainConnection _connection = null ;
    private boolean          _active     = false ;
    private Logable          _logable    = null;
    private Object           _ioLock     = new Object() ;
    private int              _expected   = 0 ;
    private HashMap          _hash       = new HashMap() ;
    private CellDomainNode [] _topoManagerNodes = null ;
    private boolean          _autoScan   = false ;
    private HashMap          _expectedDomains = null ;
    private int              _nextExpected    = 200 ;
    public DeepSpaceScan( DomainConnection connection , boolean forceAutonomousScan ){
       _connection = connection ;
       _autoScan   = forceAutonomousScan ;
    }
    public DeepSpaceScan( DomainConnection connection , Logable log , boolean forceAutonomousScan ){
       _connection = connection ;
       _logable    = log ;
       _autoScan   = forceAutonomousScan ;
    }
    public DeepSpaceScan(  DomainConnection connection , Logable log ){
       this( connection , log , false ) ;
    }
    public void forceAutoScan( boolean autoScan ){
       _autoScan = autoScan ;
    }
    private void say(String message ){
       if( _logable == null )return ;
       _logable.log(message);
    }
    private void esay(String message ){
       if( _logable == null )return ;
       _logable.elog(message);
    }
    public synchronized void  scan() throws IllegalStateException {
       if( _active )
          throw new
          IllegalStateException("DeepSpaceScan still active");

       _active = true ;

       say("Starting deep space scan");
       
       _topoManagerNodes = null ;
       _expected         = 0 ;
       _hash             = new HashMap();
       
       if( _autoScan ){
          autonomousScan() ;
       }else{
          try{
             _connection.sendObject("topo" , "gettopomap" , this , 100 ) ;
          }catch(Exception ee ){
             say("Problem sending initial packet to topo") ;
             autonomousScan() ;
          }
       }
    }
    private void autonomousScan(){
       say("Deep Space Scan  : starting autonomous scan");
       _expectedDomains = new HashMap() ;
       _nextExpected    = 200 ;
       try{
          _connection.sendObject("System" , "id" , this , 101 ) ;
       }catch(Exception ee ){
          failed("Problem sending 'id' request to System") ;
          return ;
       }

    }
    private void handleDomainNameReply( Object obj ){
       //
       // expecting 'id' of System
       //
       if( ! ( obj instanceof String ) ){
          failed("No reasonable answer from 'System' : "+obj.toString());
          return ;
       }
       say("System 'id' : "+obj);
       String domainName = ((String)obj).trim() ;
       addNewEntry( domainName , "System@"+domainName ) ;
    }
    private void handleTopoManangerReply( Object obj ){
       //
       // expecting topo map here
       //
       if( ! ( obj instanceof CellDomainNode [] ) ){
          esay("Couldn't come in contact with TopoCell");
          esay("Starting autonomous deep space scan");

          autonomousScan();
          return ;
       }
       _topoManagerNodes = (CellDomainNode[])obj ;
       for( int i = 0 , n = _topoManagerNodes.length ; i < n ; i++ ){
          say(""+_topoManagerNodes[i].toString());
       }
       ok();
    }
    private void handleCellTunnelInfo( Integer id , Object obj ){
       synchronized( _ioLock ){
          try{
             //
             // get the original Domain Node of this request.
             //
             CellDomainNode node = (CellDomainNode)_expectedDomains.remove(id) ;
             if( node == null ){
                say("PANIC : No request know to id : "+id);
                return ;
             }
             //
             // the actual domain name
             //
             String domainName = node.getName() ;
             //
             // just a double check
             //
             CellDomainNode entry = (CellDomainNode)_hash.get( domainName );
             if( entry == null ){
                 if( entry == null )say("PANIC : entries in expectedDomains and the domain hash don't match for "+domainName);                  
                 return ;            
             }
             if( ! ( obj instanceof CellTunnelInfo [] ) ){
                say("Unexpected answer class arrived for node : "+domainName+" > "+obj.getClass().getName());
                return ;
             }
             CellTunnelInfo [] info = (CellTunnelInfo [] )obj ;
             if(  info == null  ){
                say("PANIC : Zero tunnel info arrived for "+domainName+" (maybe a timeout)");
                return ;
             }
             say("Including tunnel infos for "+domainName);
             entry.setLinks( info ) ;
             for( int i = 0 , n = info.length ; i < n ; i++ ){
	        CellDomainInfo remoteInfo = info[i].getRemoteCellDomainInfo() ;
		String remote = remoteInfo == null ? getTunnelName(info[i]) : remoteInfo.getCellDomainName() ;
                if( _hash.get(remote) != null )continue ;
                addNewEntry( remote , entry.getAddress()+":System@"+remote ) ;
             }
          }finally{
             _expected -- ;
             if( _expected == 0 ){
               ok();
             }
          }
       }
    }
    private String getTunnelName( CellTunnelInfo info ){
       if( info == null )return "Unknown" ;
       String infoString = info.toString() ;
       int pos = infoString.indexOf("L[") ;
       if( pos <= 1 )return "Unknown" ;
       return infoString.substring(0,pos-1);
    }
    private void addNewEntry( String remote , String address ){
      synchronized( _ioLock ){
         _expected ++ ;

         CellDomainNode e = new CellDomainNode( remote , address );
         say("Adding new entry : "+remote+" <"+address+">");
         _hash.put( remote , e ) ;
         
         int next = _nextExpected ++ ;
         
         _expectedDomains.put( new Integer(next) , e ) ;
         
         try{
            _connection.sendObject( e.getAddress() , "getcelltunnelinfos" , this , next ) ;
         }catch(Exception ee ){
            say("Problem sending 'id' request to System") ;
            return ;
         }
      }
    }
    public void domainAnswerArrived( Object obj , int id ){
       if( id == 100 ){
          handleTopoManangerReply(obj);
          return ;
       }else if( id == 101 ){
          handleDomainNameReply(obj);
          return;
       }else{
          handleCellTunnelInfo( new Integer(id) , obj);
          return ;
       }
    }
    private synchronized void ok(){
       _active = false ;
       say("Deep Space Scan finished");  
       final ArrayList list = new ArrayList() ;
       if( _topoManagerNodes != null ){
          for( int i = 0 , n = _topoManagerNodes.length ; i < n ; i++ ){          
             list.add(_topoManagerNodes[i]);
          }
       }else{
          list.addAll( _hash.values() ) ;
       }
       new Thread( 
         new Runnable(){
            public void run(){
               processEvent(
                   new ScanFinishedEvent(DeepSpaceScan.this,0,"finished",list)
               ) ;
            }
         }
       ).start() ;
    }
    private synchronized void failed( String message ){
       say(message) ;
       _active = false ;
       return ;
    }
    public class ScanFinishedEvent extends ActionEvent {
       private java.util.List _list = null ;
       private ScanFinishedEvent( Object source , int id , String command , 
                                  java.util.List list ){
          super( source , id , command ) ;
          _list = list ;
       }
       public java.util.List getDomainList(){
          return _list ;
       }
    }
    private ActionListener _actionListener = null;

    public synchronized void addActionListener(ActionListener l) {
       _actionListener = AWTEventMulticaster.add( _actionListener, l);
    }
    public synchronized void removeActionListener(ActionListener l) {
       _actionListener = AWTEventMulticaster.remove( _actionListener, l);
    }
    public synchronized void processEvent( ActionEvent e) {
       if( _actionListener != null)
         _actionListener.actionPerformed( e );
    }


}
