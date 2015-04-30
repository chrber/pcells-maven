// $Id: JSwitchUpdatePanel.java,v 1.2 2007/02/15 08:24:32 cvs Exp $
//
package org.dcache.gui.pluggins;
//

import org.pcells.services.gui.CellGuiSkinHelper;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import java.awt.AWTEventMulticaster;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;



public class JSwitchUpdatePanel extends CellGuiSkinHelper.CellPanel {

   private JButton    _update       = null ;
   private JButton    _toggle       = null ;

   private CardLayout _switchCard   = new CardLayout();
   private JPanel     _switchPanel  = new CellGuiSkinHelper.CellPanel( _switchCard ) ;
   private boolean    _toggleMode   = false ;
   private JTextField _selectText   = null ;
   private JLabel     _selectLabel  = null ;
   private JButton    _timerButton  = null ;
   private JTextField _timerText    = null ;
   private CardLayout _timerCard    = new CardLayout() ;
   private JPanel     _timerPanel   = new CellGuiSkinHelper.CellPanel( _timerCard ) ;
   private boolean    _timerIsButton = true ;
   private int        _timerInterval = 30 ;
   private int        _minimumUpdateTime = 30 ;
   private ActionListener _internalActionListener = new InternalActionListener() ;   
   private ActionListener _timerActionHandler     = new TimerActionListener() ;

   public JSwitchUpdatePanel(){
       setLayout( new BorderLayout( 10 , 10 ) ) ;

       _update = new CellGuiSkinHelper.CellButton("Update") ;
       _update.addActionListener(_internalActionListener);
       
       _toggle = new CellGuiSkinHelper.CellButton("Toggle Display") ;
       _toggle.addActionListener(_internalActionListener);
       
       _selectLabel   = new JLabel("Selection : ") ;
       _selectLabel.setHorizontalAlignment( JLabel.RIGHT ) ;

       _selectText   = new JTextField("") ;
       _selectText.addActionListener(_internalActionListener);
       _selectText.setHorizontalAlignment( JTextField.LEFT ) ;

       _timerButton = new CellGuiSkinHelper.CellButton( "Stop");
       _timerButton.addActionListener( _timerActionHandler ) ;
       _timerButton.setForeground(Color.red);


       _timerText   = new JTextField("30") ;
       _timerText.addActionListener( _timerActionHandler ) ;
       _timerText.setHorizontalAlignment( JTextField.RIGHT ) ;

       _timerPanel.add( _timerButton , "button" ) ;
       _timerPanel.add( _timerText   , "text"   ) ;
       _timerCard.show( _timerPanel  , "text" ) ;
       _timerIsButton = false ;

       JPanel p = new CellGuiSkinHelper.CellPanel( new BorderLayout(2,2) ) ;
       p.add( _toggle , "West" ) ;
       p.add( _update , "Center");
       JPanel pRigth = new CellGuiSkinHelper.CellPanel( new GridLayout(0,3,5,5) ) ;
       pRigth.add( _timerPanel  ) ;
       pRigth.add( _selectLabel  ) ;
       pRigth.add( _selectText  ) ;
       
       p.add( pRigth , "East" ) ;

       add( p , "South");
       add( _switchPanel , "Center" ) ;

   }
   public void setEnabled( boolean enable ){
      _update.setEnabled(enable);
      _timerText.setEnabled(enable);
      _selectText.setEnabled(enable);
   }
   private int _counter = 0 ;
   public synchronized void addCard( JComponent panel ){
       String x = "panel-"+_counter++ ;
       _switchPanel.add( panel , x) ;
       if( _counter == 1 )_switchCard.show( _switchPanel , x ) ;
   }
   public void setMinimumUpdateTime( int seconds ){
      _minimumUpdateTime = seconds ;
      _timerText.setText( ""+_minimumUpdateTime);
   }
   public boolean isAutoUpdate(){
       return _timerIsButton ;
   }
   private class TimerActionListener 
           implements ActionListener,
	              Runnable       
		            {
      private Thread _worker = null ;
      public void actionPerformed( ActionEvent event ){
         Object source = event.getSource() ;
         System.err.println("Timer action : "+source);
	 if( source == _timerButton ){
	 
             _timerCard.show( _timerPanel , "text" ) ;
	     _timerIsButton = false;	 
	     if( _worker != null )_worker.interrupt() ;
	     _worker = null ;
             fireActionEvent( "modechanged" ) ;
	 }else if( source == _timerText ){
	 
	     try{ 
                  String   text = _timerText.getText() ;
	         _timerInterval = Integer.parseInt(text) ;
		 if( _timerInterval < _minimumUpdateTime )throw new Exception("");
        	 _timerCard.show( _timerPanel , "button" ) ;
		 _timerIsButton = true ;
		 
		 (_worker = new Thread( this , "ticker" )).start() ;
                 fireActionEvent( "modechanged" ) ;
		 
	     }catch(Exception ee){
	        _timerInterval = _minimumUpdateTime;
		_timerText.setText(""+_timerInterval);
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
           while( ! Thread.interrupted() ){
	       _displayCounter = counter ;
	       SwingUtilities.invokeLater(this);
	       counter -- ;
	       if( counter < 0 ){
	          counter = _timerInterval ;
                  fireActionEvent("update");
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
   private class InternalActionListener implements ActionListener {
       public void actionPerformed( ActionEvent event ){
         Object source = event.getSource() ;
         if( source == _update ){
             fireActionEvent("update");
         }else if( source == _toggle ){
             _switchCard.next( _switchPanel ) ;
         }else if( source == _selectText ){
             System.err.println("Select triggered : "+_selectText.getText() ) ;
             fireSelectionEvent( _selectText.getText() ) ;
         }
       }
   }
   public class TextSelectionEvent extends ActionEvent  {
       private String _text = "" ;
       private TextSelectionEvent( Object source , int id , String command , String selection ){
          super( source , id , command ) ;
          _text = selection ;
       }
       public String getSelectedText(){ return _text ; }
   } 
   private ActionListener _actionListener = null;
   public void fireSelectionEvent( String text ){
       processEvent( new TextSelectionEvent( this  , 0 ,  "select" , text ) ); 
   }
   public void fireActionEvent( String command ){
       processEvent( new ActionEvent( this  , 0 ,  command ) ); 
   }
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
