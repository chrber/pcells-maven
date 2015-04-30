// $Id: CellSelection.java,v 1.2 2004/06/21 22:30:27 cvs Exp $
//
package org.pcells.services.gui ;
//

import org.pcells.services.connection.DomainConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class CellSelection 
       extends JPanel 
       implements ActionListener      {

   private final static Logger _logger = LoggerFactory.getLogger(CellSelection.class);
   private DomainConnection _connection = null ;
   private Font _bigFont = new Font( "Times" , Font.BOLD , 26 ) ;
   
   private JButton _updateCells = new JButton("Update Cells") ;
   private JButton _updateDomain = new JButton("Scan");

   private TitleList _domains = new TitleList("Domains") ;
   private TitleList _cells   = new TitleList("Cells");
 
   private JPanel    _listPanel   = null ;
   private JPanel    _framePanel  = null ;
   private JPanel    _buttonPanel = null ;
   
   private JPanel   _cellIconPanel = null ;
   public CellSelection( DomainConnection connection ){
      _connection = connection ;
      setLayout( new BorderLayout(10,10) ) ;
      
      _framePanel = new JPanel( new BorderLayout(4,4) ) ;
      add(_framePanel,"West");
      
//      _framePanel = this ;
      GridLayout l = new GridLayout(0,2);
      l.setVgap(10) ;
      l.setHgap(10);
      _buttonPanel = new JPanel(l);
      _buttonPanel.add(_updateCells);
      _buttonPanel.add(_updateDomain);

      l = new GridLayout(2,0);
      l.setVgap(10) ;
      l.setHgap(10);
      
      _listPanel = new JPanel(l);
      
      _listPanel.add(_domains) ;
      _listPanel.add(_cells) ;
      
      _framePanel.add( _listPanel   , "Center" ) ;
      _framePanel.add( _buttonPanel , "South" ) ;
      
      
      _updateCells.addActionListener(this);
      _updateDomain.addActionListener(this);
      
      Icon icon = new CellIcon(300,300) ;
      _cellIconPanel = new IconDisplayPanel(icon);
   }
   private void displayTimer(){
      _updateCells.setEnabled(false);
      _updateDomain.setEnabled(false);
      _framePanel.removeAll() ;
      _framePanel.add( _cellIconPanel , "Center" ) ;
      _framePanel.add( _buttonPanel , "South" ) ;
      validate();
      repaint() ;
   }
   private void displayLists(){
      _updateCells.setEnabled(true);
      _updateDomain.setEnabled(true);
      _framePanel.removeAll() ;
      _framePanel.add( _listPanel   , "Center" ) ;
      _framePanel.add( _buttonPanel , "South" ) ;
      validate();
      repaint() ;
   }
   public void actionPerformed( ActionEvent event ){
      Object source = event.getSource() ;
      if( source == _updateDomain ){
         displayTimer() ;
         new Thread( new Runnable(){
             public void run(){
                try{
                   Thread.currentThread().sleep(10000) ;
                }catch(Exception ee ){
                    _logger.error("Problem during thread sleep: "+ ee);
                }
                displayLists();
             }
           }
        ).start();
      }
   }
   private class TitleList extends JPanel {
   
      private JList        _list   = new JList() ;
      private JScrollPane  _scroll = null ;
      private TitledBorder _border = null ;
      
      private TitleList(String title ){

         setLayout( new BorderLayout(10,10)) ;
         
         _list.setPrototypeCellValue("it-dcache0-0Domain");
         
         _scroll = new JScrollPane( _list ) ;
         add( _scroll , "Center" ) ;
         
         _border = BorderFactory.createTitledBorder(title) ;
         setBorder( _border ) ;
      }
      public void setBorderTitle( String title ){
         _border.setTitle( title ) ;
      }
   }
   private class IconDisplayPanel extends JPanel {
       private Icon _icon = null ;
       public IconDisplayPanel( Icon icon ){ 
          _icon = icon ;
       }
//       public Dimension getPreferredSize(){
//         return new Dimension( _icon.getIconWidth() , _icon.getIconHeight() );
//       }
       public void paintComponent( Graphics g ){
          Dimension d = getSize() ;
          int x = ( d.width  - _icon.getIconWidth() ) / 2 ;
          int y = ( d.height - _icon.getIconHeight()) / 2 ;
          _icon.paintIcon( this , g ,0 , 0) ;
       }
   }
   private class CellIcon implements Icon {
      private int _height = 0 ;
      private int _width  = 0 ;
      private CellIcon( int width , int height ){
         _height = height ;
         _width  = width ;
      }
      public void paintIcon( Component c , Graphics gin , int xi , int yi ){
         Graphics2D g = (Graphics2D) gin ;
         Dimension d = c.getSize() ;
         _height = d.height ;
         _width  = d.width ;
         
         g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                            RenderingHints.VALUE_ANTIALIAS_ON);
         g.setColor( c.getBackground() ) ;
         g.fillRect(  xi , yi , _width - 1 , _height - 1 ) ;
         int x = xi + 4 ;
         int y = yi + 4 ;
         int width = _width - 8 ;
         int height = _height - 8 ;
         
         Color col = new Color( 0 , 0 , 255 ) ;
         
         while( width > 0 ){
            g.setColor( col ) ;
            width = width / 2 ; height = height / 2 ;
            g.fillOval( x , y , width , height ) ;
            x = x + width  ; y = y + height   ;
            col = col.brighter() ;
         }
       }
      public int getIconWidth(){ return _height ; }
      public int getIconHeight(){ return _width ; }
   }

}
