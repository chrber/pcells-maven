// $Id: JFlushStatusPanel2.java,v 1.5 2007/02/15 08:22:28 cvs Exp $
//
package org.dcache.gui.pluggins.flush ;
//

import diskCacheV111.hsmControl.flush.FlushControlCellInfo;
import org.pcells.services.connection.DomainConnection;
import org.pcells.services.connection.DomainConnectionListener;
import org.pcells.services.gui.CellBorder;
import org.pcells.services.gui.CellGuiSkinHelper;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Map;

public class      JFlushStatusPanel2
       extends    CellGuiSkinHelper.CellPanel
       implements ActionListener, DomainConnectionListener   {
                  
   private DomainConnection     _connection = null ;
   private FlushControlCellInfo _info       = null ;
   private JFlushControlPanel.Controller _controller = null ;
   
   private JLabel _nameLabel    = new JLabel( "Name"    , SwingConstants.RIGHT ) ;
   private JLabel _classLabel   = new JLabel( "Class"   , SwingConstants.RIGHT ) ;
   private JLabel _driverLabel  = new JLabel( "Driver Name"  , SwingConstants.RIGHT ) ;
   private JLabel _controlLabel = new JLabel( "Control Type" , SwingConstants.RIGHT ) ;
   private JLabel _statusLabel  = new JLabel( "Status"       , SwingConstants.RIGHT ) ;
   private JLabel _updateLabel  = new JLabel( "Interval"     , SwingConstants.RIGHT ) ;
   private JLabel _groupLabel   = new JLabel( "Pool Group"   , SwingConstants.RIGHT ) ;
   private JLabel _switchLabel  = new JLabel( "Switch to"    , SwingConstants.RIGHT ) ;
   private JLabel _actionLabel  = new JLabel( "Driver action", SwingConstants.RIGHT ) ;
   
   private JLabel _nameText    = new JLabel( "Cell Name"    , JLabel.CENTER ) ;
   private JLabel _classText   = new JLabel( "Cell Class"   , JLabel.CENTER ) ;
   private JTextField _driverText  = new JTextField( "Driver Name"  ) ;
   private JLabel _controlText = new JLabel( "Control Type" , JLabel.CENTER ) ;
   private JLabel _statusText  = new JLabel( "Status"       , JLabel.CENTER ) ;
   private JLabel _updateText  = new JLabel( "Interval"     , JLabel.CENTER ) ;
   private JLabel _groupText   = new JLabel( "Pool Group"   , JLabel.CENTER ) ;
   
   private String [] _controlModes = { "Central" , "Local" } ;
   
   private JButton   _updateDriverButton   = new CellGuiSkinHelper.CellButton( "Update");
   private JButton   _resetDriverButton    = new CellGuiSkinHelper.CellButton( "Reset");
   private JButton   _updateIntervalButton = new CellGuiSkinHelper.CellButton( "Update Interval");
   private JButton   _updateControlLocal   = new CellGuiSkinHelper.CellButton( "Local");
   private JButton   _updateControlCentral = new CellGuiSkinHelper.CellButton( "Central");
   private JComboBox _updateControlledBox  = new JComboBox( _controlModes ) ;
   
   private DriverDetailedInfoPanel _driverInfoPanel = new DriverDetailedInfoPanel()  ;
   
   private class CellInfoPanel2 extends CellGuiSkinHelper.CellPanel {
       private CellInfoPanel2(){
//          setBorder( new CellBorder( "Status Information" , 20 ) ) ;
          setBorder(
               BorderFactory.createTitledBorder(
                   BorderFactory.createLineBorder( CellGuiSkinHelper.getForegroundColor() , 1 ) , "Status Information" )
          ) ;

          setLayout( new GridBagLayout() ) ;         

          GridBagConstraints c = new GridBagConstraints()  ;
          
          c.gridheight = 1 ;
          c.insets     = new Insets(8,8,8,8) ;
          c.fill       = GridBagConstraints.BOTH ;

          c.gridwidth  = 1 ; c.gridx = 0 ; c.gridy = 0 ;
          add( _nameLabel , c ) ; 
          c.gridwidth  = 1 ; c.gridx = 0 ; c.gridy = 1 ;
          add( _classLabel , c ) ;
          c.gridwidth  = 1 ; c.gridx = 0 ; c.gridy = 2 ;
          add( _statusLabel , c ) ; 
          c.gridwidth  = 1 ; c.gridx = 0 ; c.gridy = 3 ;
          add( _controlLabel , c ) ;
          c.gridwidth  = 1 ; c.gridx = 0 ; c.gridy = 4 ;
          add( _switchLabel , c ) ;
          c.gridwidth  = 1 ; c.gridx = 0 ; c.gridy = 5 ;
          add( _groupLabel , c ) ; 
          c.gridwidth  = 1 ; c.gridx = 0 ; c.gridy = 6 ;
          add( _driverLabel , c ) ;
          c.gridwidth  = 1 ; c.gridx = 0 ; c.gridy = 7 ;
          add( _actionLabel , c ) ;
           
          int width = 1 ; //GridBagConstraints.REMAINDER ;
          c.fill    = GridBagConstraints.BOTH ;
          c.weightx = 1.0 ;

          c.gridwidth  = width ; c.gridx = 1 ; c.gridy = 0 ;
          add( _nameText , c ) ; 
          c.gridwidth  = width ; c.gridx = 1 ; c.gridy = 1 ;
          add( _classText , c ) ;
           
          JPanel panel = new CellGuiSkinHelper.CellPanel( new GridLayout(0,2) ) ;
          panel.add( _updateControlLocal ) ;
          panel.add( _updateControlCentral ) ;
                    
          c.gridwidth  = width ; c.gridx = 1 ; c.gridy = 2 ;
          add( _statusText , c ) ; 
          c.gridwidth  = width ; c.gridx = 1 ; c.gridy = 3 ;
          add( _controlText , c ) ; 
          c.gridwidth  = width ; c.gridx = 1 ; c.gridy = 4 ;
          add( panel , c ) ; 
          
          panel = new CellGuiSkinHelper.CellPanel( new GridLayout(0,2) ) ;
          panel.add( _updateDriverButton ) ;
          panel.add( _resetDriverButton ) ;
          
          c.gridwidth  = width ; c.gridx = 1 ; c.gridy = 5 ;
          add( _groupText , c ) ; 
          c.gridwidth  = width ; c.gridx = 1 ; c.gridy = 6 ;
          add( _driverText , c ) ; 
          c.gridwidth  = width ; c.gridx = 1 ; c.gridy = 7 ;
          add( panel , c ) ; 
      }
   }
   private class CellInfoPanel extends CellGuiSkinHelper.CellPanel {
       private CellInfoPanel(){
          setBorder( 
               BorderFactory.createTitledBorder(
                   BorderFactory.createLineBorder( CellGuiSkinHelper.getForegroundColor() , 1 ) , "Cell Information" ) 
          ) ;
         // setBorder( new CellBorder( "Cell Information" , 30 ) ) ;
          setLayout( new GridBagLayout() ) ;
          

          GridBagConstraints c = new GridBagConstraints()  ;
          
          c.gridheight = 1 ;
          c.insets     = new Insets(8,8,8,8) ;
          c.fill       = GridBagConstraints.BOTH ;

          c.gridwidth  = 1 ; c.gridx = 0 ; c.gridy = 0 ;
          add( _nameLabel , c ) ; 
          c.gridwidth  = 1 ; c.gridx = 0 ; c.gridy = 1 ;
          add( _classLabel , c ) ;
           
          int width = 1 ; //GridBagConstraints.REMAINDER ;
          c.fill    = GridBagConstraints.BOTH ;
          c.weightx = 1.0 ;

          c.gridwidth  = width ; c.gridx = 1 ; c.gridy = 0 ;
          add( _nameText , c ) ; 
          c.gridwidth  = width ; c.gridx = 1 ; c.gridy = 1 ;
          add( _classText , c ) ; 
      }
   }
   private class DriverInfoPanel extends CellGuiSkinHelper.CellPanel {
       private DriverInfoPanel(){
          setBorder( new CellBorder( "Driver Information" , 30 ) ) ;
          setLayout( new GridBagLayout() ) ;
          

          GridBagConstraints c = new GridBagConstraints()  ;
          
          c.gridheight = 1 ;
          c.insets     = new Insets(8,8,8,8) ;
          c.fill       = GridBagConstraints.BOTH ;

          c.gridwidth  = 1 ; c.gridx = 0 ; c.gridy = 0 ;
          add( _groupLabel , c ) ; 
          c.gridwidth  = 1 ; c.gridx = 0 ; c.gridy = 1 ;
          add( _driverLabel , c ) ;
          c.gridwidth  = 1 ; c.gridx = 0 ; c.gridy = 2 ;
          add( _actionLabel , c ) ;
           
          int width = 1 ; //GridBagConstraints.REMAINDER ;
          c.fill    = GridBagConstraints.BOTH ;
          c.weightx = 1.0 ;

          
          JPanel panel = new JPanel( new GridLayout(0,2) ) ;
          panel.add( _updateDriverButton ) ;
          panel.add( _resetDriverButton ) ;
          
          c.gridwidth  = width ; c.gridx = 1 ; c.gridy = 0 ;
          add( _groupText , c ) ; 
          c.gridwidth  = width ; c.gridx = 1 ; c.gridy = 1 ;
          add( _driverText , c ) ; 
          c.gridwidth  = width ; c.gridx = 1 ; c.gridy = 2 ;
          add( panel , c ) ; 
          
      }
   }
   private class StatusInfoPanel extends CellGuiSkinHelper.CellPanel {
       private StatusInfoPanel(){
//          setBorder( new CellBorder( "Status Information" , 30 ) ) ;
          setBorder(
               BorderFactory.createTitledBorder(
                   BorderFactory.createLineBorder( CellGuiSkinHelper.getForegroundColor() , 1 ) , "Status Information" )
          ) ;

          setLayout( new GridBagLayout() ) ;
          

          GridBagConstraints c = new GridBagConstraints()  ;
          
          c.gridheight = 1 ;
          c.insets     = new Insets(8,8,8,8) ;
          c.fill       = GridBagConstraints.BOTH ;

          c.gridwidth  = 1 ; c.gridx = 0 ; c.gridy = 0 ;
          add( _statusLabel , c ) ; 
          c.gridwidth  = 1 ; c.gridx = 0 ; c.gridy = 1 ;
          add( _controlLabel , c ) ;
          c.gridwidth  = 1 ; c.gridx = 0 ; c.gridy = 2 ;
          add( _switchLabel , c ) ;
           
          int width = 1 ; //GridBagConstraints.REMAINDER ;
          c.fill    = GridBagConstraints.BOTH ;
          c.weightx = 1.0 ;

          
          JPanel panel = new JPanel( new GridLayout(0,2) ) ;
          panel.add( _updateControlLocal ) ;
          panel.add( _updateControlCentral ) ;
          
          c.gridwidth  = width ; c.gridx = 1 ; c.gridy = 0 ;
          add( _statusText , c ) ; 
          c.gridwidth  = width ; c.gridx = 1 ; c.gridy = 1 ;
          add( _controlText , c ) ; 
          c.gridwidth  = width ; c.gridx = 1 ; c.gridy = 2 ;
          add( panel , c ) ; 
          
      }
   }
   private class DriverDetailedInfoPanel 
           extends CellGuiSkinHelper.CellPanel 
           implements ActionListener, DomainConnectionListener   {
   
       private JPanel     _centerPanel  = new CellGuiSkinHelper.CellPanel( new GridBagLayout() ) ;
       private JTextField _keyText      = new JTextField() ;
       private JTextField _valueText    = new JTextField(20) ;
       private JButton    _actionButton = new CellGuiSkinHelper.CellButton("Update") ;
       
       private DriverDetailedInfoPanel(){
       
//          setBorder( new CellBorder( "Driver Properties" , 20 ) ) ;
          setBorder(
               BorderFactory.createTitledBorder(
                   BorderFactory.createLineBorder( CellGuiSkinHelper.getForegroundColor() , 1 ) , "Driver Properties" )
          ) ;

          
          setLayout( new BorderLayout(4,4) ) ;
          
          _centerPanel.add( new JLabel("No Details for this driver yet" , JLabel.CENTER ) ) ; 
                    
          _actionButton.addActionListener(this);
          _valueText.addActionListener(this);
          
          add( _centerPanel  , "Center" ) ;
          
          driverStatusArrived(null);
          
      }
      public void actionPerformed( ActionEvent event ){
      
          String message = null ;
          
          if( ( _keyText.getText().length()   == 0 ) || 
              ( _valueText.getText().length() == 0 )    ){
              
              message = "driver properties" ;
              
          }else{
          
              message = "driver properties -"+_keyText.getText()+"="+_valueText.getText() ;
              
          }
              
          
          sendToDriver( message ) ;
          
          _keyText.setText("");
          _valueText.setText("");
      
      }
      private void sendToDriver( String message ){
         try{
            _connection.sendObject( _controller.getFlushControllerName() ,
                                    message ,
                                    this ,
                                    26 );
         }catch(Exception ee ){
	    System.out.println("Exception send "+ee ) ;
         }   
      }
      public void domainAnswerArrived( Object obj , int subid ){
         System.out.println( "Answer ("+subid+") : "+obj.toString() ) ;
         try{
            if( subid == 26 ){
               _controller.askForStatus() ;
            }

        }catch(Exception ee ){
            ee.printStackTrace();
        }
      }
      private void driverStatusArrived( FlushControlCellInfo info ){
      
          _centerPanel.removeAll() ;

          long age = 0 ;
          Map  map = null ;
          
          if( info != null ){
             age = info.getDriverPropertiesAge() ;
             map = info.getDriverProperties() ;
          }
          
          
          GridBagConstraints c = new GridBagConstraints()  ;
          c.gridheight = 1 ;
          c.insets     = new Insets(8,8,8,8) ;
          c.fill       = GridBagConstraints.BOTH ;

          int row = 0 ;
          
          c.weightx = 1.0 ; c.gridwidth  = 3 ; c.gridx = 0 ; c.gridy = row++ ;
          _centerPanel.add( new JLabel( "Properties (Age "+(age/1000L)+" seconds)" , JLabel.CENTER ) , c ); 

          c.weightx = 0.0 ; c.gridwidth  = 1 ; c.gridx = 0 ; c.gridy = row ;
          _centerPanel.add( new JLabel( "Property" , JLabel.CENTER ) , c ) ;
          c.weightx = 1.0 ; c.gridwidth  = 2 ; c.gridx = 1 ; c.gridy = row++ ;
          _centerPanel.add( new JLabel( "Value" , JLabel.CENTER )  , c ) ;
          
          if( info != null ){
             Iterator i = map.entrySet().iterator() ; 
             for(  ; i.hasNext() ; row ++ ){
                 Map.Entry entry = (Map.Entry)i.next() ;
                 String key = entry.getKey().toString()  ;
                 Object value = entry.getValue() ;
                 if( value == null )continue ;

                 JLabel keyLabel = new JLabel( key , JLabel.RIGHT ) ;             
                 c.weightx = 0.0 ; c.gridwidth  = 1 ; c.gridx = 0 ; c.gridy = row ;
                 _centerPanel.add( keyLabel , c ) ; 

                 JLabel valueLabel = new JLabel( value.toString() , JLabel.LEFT ) ;
                 valueLabel.setFont( valueLabel.getFont().deriveFont( Font.PLAIN ) ) ;
                 c.weightx = 1.0 ; c.gridwidth  = 1 ; c.gridx = 1 ; c.gridy = row ;
                 _centerPanel.add( valueLabel , c ) ; 
             }
          }
          
          c.weightx = 0.0 ; c.gridwidth  = 1 ; c.gridx = 0 ; c.gridy = row ;
          _centerPanel.add(  _keyText , c ) ;
          c.weightx = 1.0 ; c.gridwidth  = 1 ; c.gridx = 1 ; c.gridy = row ;
          _centerPanel.add(  _valueText , c ) ;
          c.weightx = 0.0 ; c.gridwidth  = 1 ; c.gridx = 2 ; c.gridy = row ;
          _centerPanel.add( _actionButton , c ) ;
          
          _centerPanel.doLayout() ;
          _centerPanel.repaint() ;
          
          doLayout() ;
          repaint() ;
      }
   }
   /*
    *   Master Layout
    */
   public JFlushStatusPanel2( JFlushControlPanel.Controller controller , 
                              DomainConnection connection , 
                              java.util.prefs.Preferences pref){
   
      _connection = connection ;
      _controller = controller ;
      
      
      setLayout(new GridLayout(0,2,2,2)) ;
      
      _driverText.setEnabled(false);
      _driverText.setHorizontalAlignment( JTextField.CENTER ) ;
      _updateDriverButton.setEnabled(false);
      _resetDriverButton.setEnabled(false);
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
      
      
       add( new CellInfoPanel2() ) ;
    //   add( new DriverInfoPanel() ) ;
    //   add( new StatusInfoPanel() ) ;
       add( _driverInfoPanel ) ;

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
      _driverInfoPanel.driverStatusArrived( null ) ;
   
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
      
      _driverInfoPanel.driverStatusArrived( info ) ;
      
   }

}
