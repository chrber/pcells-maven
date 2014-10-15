// $Id: JTransferPanel.java,v 1.4 2007/02/23 12:01:04 cvs Exp $
//
package org.dcache.gui.pluggins;
//

import diskCacheV111.util.PnfsId;
import org.pcells.services.connection.DomainConnection;
import org.pcells.services.connection.DomainConnectionListener;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class      JTransferPanel 
       extends JSwitchUpdatePanel
       implements ActionListener,
                  DomainConnectionListener  {

   private DomainConnection     _connection   = null ;
   private JTransferDisplay     _display      = null ;
   private JSimpleTransferTable _table        = null ;
   private TransferInfo []      _transferInfo = null ;

   private TransferInfo [] _transferInfoList = null ;
   private String          _selectString     = null ;

   private class JTransferDisplay extends JHistogramDisplay {
      private JTransferDisplay( DomainConnection connection ){
         super( "Transfer Queue");
      }
      public void setTransferList( TransferInfo [] list ){
          if( list == null )return ;
          int [] values = new int[list.length] ;
          int [] flags  = new int[list.length] ;
          for( int i= 0 ; i < list.length ; i++ ){
              TransferInfo info = list[i] ;
              values[i] = (int)Math.max(0L,info._timer.longValue()/1000L);
              flags[i] =
                 info._mode.startsWith("N") &&
                 info._status.equals("WaitingForDoorTransferOk") ?
                 3 : 1 ;
          }
          prepareHistogram( values , flags , 2 , 60 ) ;
      }
   }

   public JTransferPanel( DomainConnection connection ){
      _connection = connection ;
      _table   = new JSimpleTransferTable(connection) ;
      _display = new JTransferDisplay(connection);

      addActionListener(this);
      addCard( _display ) ;
      addCard( new JScrollPane(_table) ) ;

      setMinimumUpdateTime( 1 ) ; // seconds

   }
   private static final int TRANSFER_REQUEST = 20000 ;
   private void getTransferInfo(){
      setEnabled(false);

      try{
         _connection.sendObject( "TransferObserver" ,
                                 "ls iolist" ,
                                 this ,
                                 TRANSFER_REQUEST );
      }catch(Exception ee){
         setEnabled(false);
         ee.printStackTrace() ;
      }
   }
   class TransferInfo {
       public String  _cellName   = null ;
       public String  _domainName = null ;
       public Integer _serialId   = null ;
       public String  _protocol   = null ;
       public Integer _uid = null ;
       public Integer _pid = null ;
       public PnfsId  _pnfsId = null ;
       public String  _pool   = null ;
       public String  _client = null ;
       public String  _status = null ;
       public Long    _timer  = null ;
       public String  _mode   = null ;
       public Long    _transferTime     = null ;
       public Long    _bytesTransferred = null ;
       public Long    _transferStarted  = null ;
       public Double  _transferSpeed    = null ;
       private String _string           = null ;

       private TransferInfo( String string ) throws Exception {
           _string  = string ;
           StringTokenizer st = new StringTokenizer(string);
           _cellName = st.nextToken() ;
           _domainName = st.nextToken() ;
           _serialId   = new Integer( st.nextToken() ) ;
           _protocol   = st.nextToken() ;
           _uid        = new Integer( st.nextToken() ) ;
           _pid        = new Integer( st.nextToken() ) ;
           _pnfsId     = new PnfsId( st.nextToken() ) ;
           _pool       = st.nextToken() ;
           _client     = st.nextToken() ;
           _status     = st.nextToken() ;
           _timer      = new Long( st.nextToken() ) ;
           _mode       = st.nextToken() ;
           if( st.hasMoreTokens() ){
              _transferTime = new Long( st.nextToken() );
              _bytesTransferred = new Long( st.nextToken() ) ;
              _transferSpeed = new Double( st.nextToken() ) ;
              _transferStarted = new Long( st.nextToken() ) ;
           }

       }
       public String toString(){ return _string ; }

   }
   private TransferInfo [] decodeTransferInfo( String string ){
      ArrayList     list = new ArrayList() ;
      StringTokenizer sb = new StringTokenizer( string , "\n" ) ;
      while( sb.hasMoreTokens() ){

	  String nextToken = sb.nextToken().trim()  ;
	  if( nextToken.length() == 0 )continue ;
	  try{
	      list.add( new TransferInfo( nextToken ) ) ;
	  }catch(Exception e ){
	      System.err.println("Sytax error in token : "+nextToken+" : "+e ) ;
	  }
      }
      return (TransferInfo [] )list.toArray( new TransferInfo[0] ) ;
   }
   public void domainAnswerArrived( Object obj , int id ){
       setEnabled(true);
       if( ( id == TRANSFER_REQUEST ) &&  ( obj instanceof String  ) ){
	  _transferInfoList = decodeTransferInfo( (String) obj ) ;
	  System.out.println("TransferInfoList created with "+_transferInfoList.length+" entries");
          selectAndUpdate() ;
     }
   }
   private void selectAndUpdate( ){
      String sel = _selectString ;
      if( ( sel == null ) || sel.equals("") ){
          try{
              _display.setTransferList(_transferInfoList);
          }catch(Exception ee){
               ee.printStackTrace() ;
          }
          try{
             _table.setTransferList( _transferInfoList ) ;
          }catch(Exception ee){
               ee.printStackTrace() ;
          }

      }else{
          ArrayList list = new ArrayList() ;
          for( int i = 0 , n = _transferInfoList.length ; i < n ; i++ )
             if( _transferInfoList[i].toString().indexOf(sel) > -1 )list.add(_transferInfoList[i]);

          TransferInfo [] selectedList = (TransferInfo [] )list.toArray( new TransferInfo[0] ) ;

          _display.setTransferList(selectedList);
          _table.setTransferList(selectedList) ;
      }
   }
   private void doSelection( String selection ){
       _selectString = selection ;
       selectAndUpdate() ;
   }
   public void actionPerformed( ActionEvent event ){
      Object source = event.getSource() ;
      String command = event.getActionCommand() ;
      if( command.equals("update") ){

          getTransferInfo() ;

      }else if( command.equals( "modechanged" ) ){

          _table.setInteractiveEnabled( ! isAutoUpdate() );

      }else if( command.equals( "select" ) ){
          TextSelectionEvent e = (TextSelectionEvent)event ;
          doSelection(e.getSelectedText());
      }

    }
   
}
