 // $Id: JRestoreDisplay.java,v 1.3 2007/02/23 12:01:04 cvs Exp $
//
package org.dcache.gui.pluggins;
//

 import diskCacheV111.vehicles.RestoreHandlerInfo;
 import org.pcells.services.connection.DomainConnection;
 import org.pcells.services.connection.DomainConnectionListener;

 import javax.swing.JScrollPane;

 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;

public class      JRestoreDisplay 
       extends    JSwitchUpdatePanel 
       implements ActionListener, 
                  DomainConnectionListener  {

   private DomainConnection      _connection   = null ;
   private JHistogramDisplay     _display      = null ;
   private RestoreHandlerInfo [] _currentInfo  = null ;
   private String                _selectString = null ;
   private JRestoreQueueTable    _table        = null ;
    
   public JRestoreDisplay( DomainConnection connection ){
      _connection = connection ;
      
      _table   = new JRestoreQueueTable(connection) ;     
      _display = new JHistogramDisplay("Restore Queue") ;
      
      addCard( _display );
      addCard( new JScrollPane(_table) );
 
      addActionListener(this);

      setMinimumUpdateTime( 1 ) ; // seconds      
   }
   private void getRestoreInfo(){
      try{
         _connection.sendObject( "PoolManager" ,
                                 "xrc ls" , 
                                 this ,
                                 1000 );
         setEnabled(false);
      }catch(Exception ee){
         setEnabled(true);
         ee.printStackTrace() ;
      }
   }
   private void prepareHistogram( RestoreHandlerInfo [] info ,
                                  int binCount  ){
                                  
      if( info == null )return ;
      int [] values = new int[info.length] ;
      int [] flags  = new int[info.length] ;
      long now = System.currentTimeMillis();
      int  minimum = 0 ;
      for( int i = 0 , n = info.length ; i < n ; i++ ){
         RestoreHandlerInfo rhi = (RestoreHandlerInfo)info[i];
         values[i] = (int)((now - rhi.getStartTime() ) / 1000L );
         minimum = Math.min( minimum , values[i] ) ;
         flags[i]  = rhi.getErrorCode() != 0 ? 3 : 1 ;
      }
      if( minimum < 0 )                
         for( int i = 0 , n = info.length ; i < n ; i++ )values[i] -= minimum ;
      _display.prepareHistogram( values , flags , 2 , binCount ) ; 
 
   }
   private void doSelection( String selection ){
       _selectString = selection ;
       selectAndUpdate() ;
   }
   private synchronized void selectAndUpdate(){
       if( _currentInfo == null )return ;
       RestoreHandlerInfo [] info = null ;
       if( (_selectString == null ) || ( _selectString.equals("") ) ){
          info = _currentInfo ;
       }else{
          ArrayList list = new ArrayList() ;
          for( int i = 0 ; i < _currentInfo.length ; i++ )
            if( _currentInfo[i].toString().indexOf(_selectString) > -1 )list.add(_currentInfo[i]);
          info = (RestoreHandlerInfo [])list.toArray( new RestoreHandlerInfo[0] ) ;
       }
       _table.setRestoreQueue( info ) ;
       prepareHistogram( info , 60 ) ;
   }
   public void domainAnswerArrived( Object obj , int id ){
      setEnabled(true);
      if( obj instanceof RestoreHandlerInfo []  ){
          _currentInfo = (RestoreHandlerInfo[])obj ;
	  selectAndUpdate() ;
      }
   }
   public void actionPerformed( ActionEvent event ){
      Object source = event.getSource() ;
      String command = event.getActionCommand() ;
      if( command.equals("update") ){
      
          getRestoreInfo() ;  
           
      }else if( command.equals( "modechanged" ) ){
      
          _table.setInteractiveEnabled( ! isAutoUpdate() );
          
      }else if( command.equals( "select" ) ){
      
          JSwitchUpdatePanel.TextSelectionEvent e = (JSwitchUpdatePanel.TextSelectionEvent)event ;
          doSelection(e.getSelectedText());
          
      }
    }
   
}

