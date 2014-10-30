package org.dcache.gui.pluggins.drives;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.pcells.services.gui.util.*;
import org.pcells.services.gui.CellGuiSkinHelper;

public class JTapeDriveDisplay	extends    CellGuiSkinHelper.CellPanel {
	
	private DriveListDisplay  _display       = null ;
	private boolean  _isExtended    = false ;
	private JPanel   _displayPanel  = null ;
	private JFrame   _extendedFrame = null ;
	private String   _title         = "Tape Drives" ;
	
	public JTapeDriveDisplay(String title) {

            _title = title == null ? _title : title ;
            setLayout( new BorderLayout( 10 , 10 ) ) ;
		
	    add( _display = new DriveListDisplay() , "Center" ) ;
	   
	    _display.addMouseListener( 
	    	new MouseAdapter() {
	    		public void mouseClicked( MouseEvent event ){
	    			if( event.getClickCount() > 1 ){
	    				if( _isExtended )return ;
	    				_isExtended = true ;
	    				switchToExternal();
	    			}
	    		}
	    	});
	}
      public void setDriveList( DriveStatusInfo [] info ){
         _display.setDriveList(info) ;
      }
        private void switchToExternal(){
            remove( _display ) ;
            doLayout() ;
            repaint();
            
            if( DrawBoardFrame.__sharedStoryBoard != null ){
               DrawBoardFrame.__sharedStoryBoard.addToDrawboard( _display , _title ) ;
            }else if( _extendedFrame == null ){
		_extendedFrame = new JFrame(_title) ;
		_extendedFrame.getContentPane().setLayout( new BorderLayout(10,10) ) ;

		_displayPanel = new CellGuiSkinHelper.CellPanel( new BorderLayout(10,10) ) ;
                _displayPanel.setBorder( new CellGuiSkinHelper.CellBorder(_title,25) ) ;
		_displayPanel.add( _display , "Center" ) ;
		_displayPanel.doLayout() ;
		_display.update() ;

		_extendedFrame.getContentPane().add( _displayPanel , "Center" ) ;
		_extendedFrame.pack() ;
		_extendedFrame.addWindowListener(
		   new WindowAdapter(){
		     public void windowClosing(WindowEvent e) {
		        _isExtended = false ;
		        _extendedFrame.setVisible(false) ;
		        _displayPanel.remove( _display ) ;
		        add( _display , "Center" ) ;
		        doLayout() ;
		        repaint();
		        _display.update() ;
		     }
		   }
		);
		_extendedFrame.setLocation(100,100);
		_extendedFrame.setSize(600,400);
                _extendedFrame.setVisible(true);
	     }else{
		 _displayPanel.add( _display , "Center" ) ;
		 _displayPanel.doLayout() ;
		 _display.update() ;
		 _extendedFrame.getContentPane().doLayout();
        	 _extendedFrame.setVisible(true);
	     }
	  }
          
       
	
}
