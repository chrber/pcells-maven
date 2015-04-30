// $Id: JFlushStatusPanel.java,v 1.1 2006/03/26 15:13:47 cvs Exp $
//
package org.dcache.gui.pluggins.flush ;
//

import diskCacheV111.hsmControl.flush.FlushControlCellInfo;
import org.pcells.services.connection.DomainConnection;
import org.pcells.services.connection.DomainConnectionListener;
import org.pcells.services.gui.CellBorder;
import org.pcells.services.gui.CellGuiSkinHelper;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class      JFlushStatusPanel
       extends    CellGuiSkinHelper.CellPanel
       implements ActionListener, DomainConnectionListener   {
                  
   private DomainConnection     _connection = null ;
   private FlushControlCellInfo _info       = null ;
   private JFlushControlPanel.Controller _controller = null ;
   
   private JLabel _nameLabel    = new JLabel( "Cell Name"    , SwingConstants.RIGHT ) ;
   private JLabel _classLabel   = new JLabel( "Cell Class"   , SwingConstants.RIGHT ) ;
   private JLabel _driverLabel  = new JLabel( "Driver Name"  , SwingConstants.RIGHT ) ;
   private JLabel _controlLabel = new JLabel( "Control Type" , SwingConstants.RIGHT ) ;
   private JLabel _statusLabel  = new JLabel( "Status"       , SwingConstants.RIGHT ) ;
   private JLabel _updateLabel  = new JLabel( "Interval"     , SwingConstants.RIGHT ) ;
   private JLabel _groupLabel   = new JLabel( "Pool Group"   , SwingConstants.RIGHT ) ;
   
   private JLabel _nameText    = new JLabel( "Cell Name"    , JLabel.CENTER ) ;
   private JLabel _classText   = new JLabel( "Cell Class"   , JLabel.CENTER ) ;
   private JTextField _driverText  = new JTextField( "Driver Name"  ) ;
   private JLabel _controlText = new JLabel( "Control Type" , JLabel.CENTER ) ;
   private JLabel _statusText  = new JLabel( "Status"       , JLabel.CENTER ) ;
   private JLabel _updateText  = new JLabel( "Interval"     , JLabel.CENTER ) ;
   private JLabel _groupText   = new JLabel( "Pool Group"   , JLabel.CENTER ) ;
   
   private String [] _controlModes = { "Central" , "Local" } ;
   
   private JButton   _updateDriverButton   = new JButton( "Update Driver");
   private JButton   _updateIntervalButton = new JButton( "Update Interval");
   private JButton   _updateControlLocal   = new JButton( "Local");
   private JButton   _updateControlCentral = new JButton( "Central");
   private JComboBox _updateControlledBox  = new JComboBox( _controlModes ) ;
   /*
    *   Master Layout
    */
   public JFlushStatusPanel( JFlushControlPanel.Controller controller , 
                             DomainConnection connection , 
                             java.util.prefs.Preferences pref){
   
      _connection = connection ;
      _controller = controller ;
      
      
      setLayout(new GridBagLayout()) ;
      
      //_controlText.setOpaque(true);
      //_controlText.setBackground( Color.green ) ;
      
      _driverText.setEnabled(false);
      _driverText.setHorizontalAlignment( JTextField.CENTER ) ;
      _updateDriverButton.setEnabled(false);
      _updateIntervalButton.setEnabled(false);
      _updateControlledBox.setEnabled(false);
      _updateControlledBox.addActionListener(this);
      
      _updateControlLocal.addActionListener(this);
      _updateControlCentral.addActionListener(this);
      
      _nameText.setFont( _nameText.getFont().deriveFont( Font.PLAIN ) ) ;
      _classText.setFont( _classText.getFont().deriveFont( Font.PLAIN ) ) ;
      _controlText.setFont( _controlText.getFont().deriveFont( Font.PLAIN ) ) ;
      _statusText.setFont( _statusText.getFont().deriveFont( Font.PLAIN ) ) ;
      _updateText.setFont( _updateText.getFont().deriveFont( Font.PLAIN ) ) ;
      _groupText.setFont( _groupText.getFont().deriveFont( Font.PLAIN ) ) ;
      
      
      GridBagLayout      l = new GridBagLayout() ;
      GridBagConstraints c = new GridBagConstraints()  ;

       JPanel panel = new CellGuiSkinHelper.CellPanel( l ); ;
       panel.setBorder( new CellBorder("Parameter Setting", 30 ) ) ;

       panel.setBackground( Color.lightGray );
       c.gridheight = 1 ;
       c.insets     = new Insets(8,8,8,8) ;
       c.fill = GridBagConstraints.BOTH ;
       
       c.gridwidth  = 1 ; c.gridx = 0 ; c.gridy = 0 ;
       panel.add( _nameLabel , c ) ; 
       c.gridwidth  = 1 ; c.gridx = 0 ; c.gridy = 1 ;
       panel.add( _classLabel , c ) ; 
       c.gridwidth  = 1 ; c.gridx = 0 ; c.gridy = 2 ;
       panel.add( _driverLabel , c ) ; 
       c.gridwidth  = 1 ; c.gridx = 0 ; c.gridy = 3 ;
       panel.add( _controlLabel , c ) ; 
       c.gridwidth  = 1 ; c.gridx = 0 ; c.gridy = 4 ;
       panel.add( _statusLabel , c ) ; 
       c.gridwidth  = 1 ; c.gridx = 0 ; c.gridy = 5 ;
       panel.add( _updateLabel , c ) ; 
       c.gridwidth  = 1 ; c.gridx = 0 ; c.gridy = 6 ;
       panel.add( _groupLabel , c ) ; 
       
       int width = 1 ; //GridBagConstraints.REMAINDER ;
       c.fill = GridBagConstraints.BOTH ;
       c.weightx = 1.0 ;
       
       c.gridwidth  = width ; c.gridx = 1 ; c.gridy = 0 ;
       panel.add( _nameText , c ) ; 
       c.gridwidth  = width ; c.gridx = 1 ; c.gridy = 1 ;
       panel.add( _classText , c ) ; 
       c.gridwidth  = width ; c.gridx = 1 ; c.gridy = 2 ;
       panel.add( _driverText , c ) ; 
       c.gridwidth  = width ; c.gridx = 1 ; c.gridy = 3 ;
       panel.add( _controlText , c ) ; 
       c.gridwidth  = width ; c.gridx = 1 ; c.gridy = 4 ;
       panel.add( _statusText , c ) ; 
       c.gridwidth  = width ; c.gridx = 1 ; c.gridy = 5 ;
       panel.add( _updateText , c ) ; 
       c.gridwidth  = width ; c.gridx = 1 ; c.gridy = 6 ;
       panel.add( _groupText , c ) ; 
      
      // c.fill = GridBagConstraints.NONE ;
       c.weightx = 0.0 ;

       c.gridwidth  = 1 ; c.gridx = 2 ; c.gridy = 2 ;
       panel.add( _updateDriverButton , c ) ; 
       
         GridLayout gl = new GridLayout(1,0) ;
	 gl.setVgap(10) ;
	 gl.setHgap(10);
	 JPanel centralLocal = new CellGuiSkinHelper.CellPanel(gl);
         centralLocal.setOpaque(false);
         centralLocal.add( _updateControlLocal ) ;
         centralLocal.add( _updateControlCentral ) ;
         
       c.gridwidth  = 1 ; c.gridx = 2 ; c.gridy = 3 ;
       panel.add( centralLocal , c ) ; 
       
//       panel.add( _updateControlledBox , c ) ; 
       
       
       c.gridwidth  = 1 ; c.gridx = 2 ; c.gridy = 5 ;
       panel.add( _updateIntervalButton , c ) ; 

       add( panel ) ;

       _controller.askForStatus() ;
           
   }
   public void actionPerformed( ActionEvent event ){
      System.out.println("Box action : "+event);
      Object source  = event.getSource() ;
      if( source == _updateControlledBox ){
         String command = (String)_updateControlledBox.getSelectedItem() ;
         if( command.equals( "Central" ) ){
             System.out.println("Send Central");
             setControlTypeCentral(true);
         }else if( command.equals( "Local") ){
             System.err.println("Send Central");
             setControlTypeCentral(false);
         }
      }else if( source == _updateControlLocal ){
             setControlTypeCentral(false);
      }else if( source == _updateControlCentral ){
             setControlTypeCentral(true);
      }
   }
   private void setControlTypeCentral( boolean central ){
      try{
         _connection.sendObject( _controller.getFlushControllerName() ,
                                 "set control "+( central ? "on" : "off" ) ,
                                 this ,
                                 16 );
      }catch(Exception ee ){
	 System.out.println("Exception send "+ee ) ;
      }   
   }
   public void domainAnswerArrived( Object obj , int subid ){
      System.out.println( "Answer ("+subid+") : "+obj.toString() ) ;
      try{
         if( subid == 16 ){
            if( ! ( obj instanceof String ) ){
                System.err.println("Unkown reply : "+obj);
            }
            _controller.askForStatus() ;
         }

     }catch(Exception ee ){
         ee.printStackTrace();
     }
   }
   
   private void clearAll(){
      _nameText.setText( "Unknown" ) ;
      _classText.setText( "Unknown" ) ;
      _driverText.setText( "Unknown" ) ;
      _controlText.setText( "Unknown" ) ;
      _updateControlledBox.setEnabled(false) ;
      _statusText.setText( "Unknown" ) ;
      _updateText.setText( "Unknown" ) ;
      _groupText.setText( "Unknown"  ) ;
   
   }
   public void prepareFlushStatus( FlushControlCellInfo info ){
   
      if( info == null ){ clearAll() ; return ; }
      
      _nameText.setText( info.getCellName()+"@"+info.getDomainName() ) ;
      _classText.setText( info.getCellClass() ) ;
      _driverText.setText( info.getDriverName() ) ;
      
      if( info.getIsControlled() ){
         _controlText.setText("Centrally Managed" ) ;
         _controlText.setForeground( Color.red ) ;      
      }else{
         _controlText.setText("Locally Managed" ) ;
         _controlText.setForeground( Color.black ) ;      
      }
//      String control = info.getIsControlled() ? "Central" : "Local" ;
//      _controlText.setText( control ) ;
//      _updateControlledBox.setSelectedItem( control ) ;

      _updateControlledBox.setEnabled(true) ;
      _statusText.setText( info.getStatus() ) ;
      _updateText.setText( "" + ( info.getUpdateInterval() / 1000L ) + " seconds" ) ;
      
      java.util.List list = info.getPoolGroups() ;
      
      _groupText.setText( ( list != null ) && ( list.size() > 0 ) ? list.get(0).toString() : "NONE" ) ;
      
   }

}
