// $Id: RestoreDisplay.java,v 1.5 2005/05/09 05:51:15 cvs Exp $
//
package org.dcache.gui.pluggins;
//
import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.border.*;
import java.util.*;
import java.io.* ;
import org.pcells.services.connection.DomainConnection ;
import org.pcells.services.connection.DomainConnectionListener ;
import org.pcells.services.connection.DomainEventListener ;
import dmg.cells.network.CellDomainNode ;
import dmg.cells.nucleus.CellTunnelInfo ;
import dmg.cells.nucleus.CellDomainInfo ;   
import dmg.cells.nucleus.CellInfo ;   
import diskCacheV111.vehicles.RestoreHandlerInfo ;   
import org.pcells.services.gui.* ;

public class      RestoreDisplay 
       extends     CellGuiSkinHelper.CellPanel 
       implements ActionListener, 
                  DomainConnectionListener  {

   private DomainConnection   _connection   = null ;
   private JButton            _update       = null ;
   private JButton            _toggle       = null ;
   private JHistogramDisplay  _display      = null ;
   private RestoreHandlerInfo [] _currentInfo = null ;
   private JRestoreQueueTable    _table       = null ;
   
   private CardLayout _switchCard   = new CardLayout();
   private JPanel     _switchPanel  = new JPanel( _switchCard ) ;
   private boolean    _toggleMode   = false ;
   
   private JButton    _timerButton  = null ;
   private JTextField _timerText    = null ;
   private CardLayout _timerCard    = new CardLayout() ;
   private JPanel     _timerPanel   = new JPanel( _timerCard ) ;
   private boolean    _timerIsButton = true ;
   private int        _timerInterval = 30 ;
   private ActionListener _timerActionHandler = null ;
   
    
   public RestoreDisplay( DomainConnection connection ){
      _connection = connection ;
      _table = new JRestoreQueueTable(connection) ;
      
      setLayout( new BorderLayout( 10 , 10 ) ) ;
      
      _update = new CellGuiSkinHelper.CellButton("Update") ;
      _update.addActionListener(this);
      _toggle = new CellGuiSkinHelper.CellButton("Toggle Display") ;
      _toggle.addActionListener(this);
      _toggle.setEnabled(false);
      
      
      _timerButton = new CellGuiSkinHelper.CellButton( "Stop");
      _timerText   = new JTextField("30") ;
      
      _timerPanel.add( _timerButton , "button" ) ;
      _timerPanel.add( _timerText   , "text"   ) ;
      _timerCard.show( _timerPanel , "text" ) ;
      _timerIsButton = false ;
      _timerButton.setForeground(Color.red);
      _timerActionHandler = new TimerActionListener() ;
      _timerButton.addActionListener( _timerActionHandler ) ;
      _timerText.addActionListener( _timerActionHandler ) ;
      _timerText.setHorizontalAlignment( JTextField.RIGHT ) ;
      
      JPanel p = new JPanel( new BorderLayout(2,2) ) ;
      p.add( _toggle , "West" ) ;
      p.add( _update , "Center");
      p.add( _timerPanel , "East" ) ;
      
      add( p , "South");
      _switchPanel.add( _display = new JHistogramDisplay("Restore Queue") , "display" ) ;
      _switchPanel.add( new JScrollPane(_table) , "table" ) ;
      
      add( _switchPanel , "Center" ) ;
      
   }
   private class TimerActionListener 
           implements ActionListener,
	              Runnable       
		            {
      private Thread _worker = null ;
      public void actionPerformed( ActionEvent event ){
         Object source = event.getSource() ;
	 if( source == _timerButton ){
	 
             _timerCard.show( _timerPanel , "text" ) ;
	     _timerIsButton = false;	 
             _update.setEnabled(true);
             _table.setInteractiveEnabled(true);
	     if( _worker != null )_worker.interrupt() ;
	     _worker = null ;
	 }else if( source == _timerText ){
	 
	     try{ 
                  String   text = _timerText.getText() ;
	         _timerInterval = Integer.parseInt(text) ;
		 if( _timerInterval < 30 )throw new Exception("");
        	 _timerCard.show( _timerPanel , "button" ) ;
		 _timerIsButton = true ;
                 _update.setEnabled(false);
		 _table.setInteractiveEnabled(false);
		 
		 (_worker = new Thread( this , "ticker" )).start() ;
		 
	     }catch(Exception ee){
	        _timerInterval = 30 ;
		_timerText.setText("30");
             }
 	     
	 }
      }
      private int _displayCounter = -1 ;
      private Color _storeColor   = Color.black ;
      private String _storeText   = "Update" ;
      public void run(){
         if( Thread.currentThread() == _worker ){
	   int counter = _timerInterval ;
	   System.out.println("Thread started");
	   getRestoreInfo() ;
           while( ! Thread.interrupted() ){
	       _displayCounter = counter ;
	       SwingUtilities.invokeLater(this);
	       counter -- ;
	       if( counter < 0 ){
	          counter = _timerInterval ;
		  getRestoreInfo() ;
	       }
	       try{
	          Thread.sleep(1000L);
	       }catch(InterruptedException e){
	          break ;
	       }
	   }
	   _displayCounter = -1 ;
	   SwingUtilities.invokeLater(this);
	   System.out.println("Thread stopped");
	 }else{
	     if( _displayCounter == _timerInterval ){
	        _storeColor = _update.getForeground() ;
		_storeText  = _update.getText() ;
	        _update.setForeground(Color.red);
	        _update.setText(""+_displayCounter) ;
	     }else if( _displayCounter == -1 ){
	        _update.setText("Update") ;
		_update.setForeground( _storeColor ) ;
		
	     }else{
	        _update.setText(""+_displayCounter) ;
	     }
	 }
      }
   }
   private void getRestoreInfo(){
      _update.setEnabled(false);
      try{
         _connection.sendObject( "PoolManager" ,
                                 "xrc ls" , 
                                 this ,
                                 1000 );
      }catch(Exception ee){
         _update.setEnabled(true);
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
   public void domainAnswerArrived( Object obj , int id ){
      _update.setEnabled(true);
      if( obj instanceof RestoreHandlerInfo []  ){
          RestoreHandlerInfo [] info = (RestoreHandlerInfo[])obj ;
	  _table.setRestoreQueue( info ) ;
          prepareHistogram( info , 40 ) ;
	  _toggle.setEnabled(true);
      }
   }
   public void actionPerformed( ActionEvent event ){
      Object source = event.getSource() ;
      if( source == _update ){
          getRestoreInfo() ;   
      }else if( source == _toggle ){
          if( _toggleMode ){
             _switchCard.show( _switchPanel , "display" ) ;
	     _toggleMode = false ;
	  }else{
             _switchCard.show( _switchPanel , "table" ) ;
	     _toggleMode = true ;
	  }     
      }
    }
   
}
