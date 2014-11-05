// $Id: MonitoringPanelV1.java,v 1.4 2007/04/29 15:15:58 cvs Exp $
//
package org.pcells.services.gui.monitoring ;
//

import org.pcells.services.connection.DomainConnection;
import org.pcells.services.connection.DomainConnectionListener;
import org.pcells.services.connection.DomainEventListener;
import org.pcells.services.gui.CellGuiSkinHelper;
import org.pcells.services.gui.util.AnimatedLabel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.prefs.Preferences;

public class      MonitoringPanelV1
       extends    CellGuiSkinHelper.CellPanel
       implements DomainConnectionListener,
                  DomainEventListener,
                  ActionListener{
                  
   private DomainConnection  _connection   = null ; 
   private Preferences       _preferences  = null ;
   private JButton           _goButton      = new JButton("GO");
   private JTextField        _text          = new JTextField("");
   private MessageWindow     _messageWindow = null ;
   private JComponent        _ourMaster     = this ;
   private MonitorComponentPanel _monitorComponents = null ;
   /*
    *   Master Layout
    */
   public MonitoringPanelV1( DomainConnection connection , java.util.prefs.Preferences pref){
   
      _connection  = connection ;
      _preferences = pref ;
      
      BorderLayout l = new BorderLayout() ;
      l.setVgap(10) ;
      l.setHgap(10);
      setLayout(l) ;
      
      //setBorder( new CellBorder("Pool Controller", 25 ) ) ;
      
      JPanel animated = new AnimatedLabel( "Checking Module"  ) ;
      animated.setPreferredSize( new Dimension( 0 , 32 ) ) ;
      CellGuiSkinHelper.setComponentProperties(animated);
      //
      // load the configuration file
      //
     ComponentMonitorable [] componentList = getComponentList("MonitoringPanel.plugins") ;

      
      add( animated , "North"  ) ;
      add( _monitorComponents = new MonitorComponentPanel( componentList ) , "Center" ) ;
      add( _goButton , "South" ) ;
      
      _text.addActionListener(this);
      _goButton.addActionListener(this);
      CellGuiSkinHelper.setComponentProperties(_text);
      CellGuiSkinHelper.setComponentProperties(_goButton);
      CellGuiSkinHelper.setComponentProperties(_monitorComponents);
      
   }
   private ComponentMonitorable [] getComponentList( String propertyFileName ){

      //int componentCount = 0 ;
      //ComponentMonitorable [] list = new ComponentMonitorable[2] ;
      //list[componentCount++] = new PnfsManagerMonitor( "Pnfs Monitor" , "Check PnfsManager" , _connection ) ;
      //list[componentCount++] = new SrmMonitor( "SRM Monitor" , "Checks 'info' time" , _connection ) ;
      
      Class [] argClasses = {
          java.lang.String.class ,
          java.lang.String.class ,
          java.lang.String.class ,
          org.pcells.services.connection.DomainConnection.class     
      };
      ArrayList result = new ArrayList() ;
      try{
      
          ClassLoader    loader = this.getClass().getClassLoader() ;
          java.util.List list   = loadLocalFile( propertyFileName ) ;
          
          for( Iterator i = list.iterator() ; i.hasNext() ; ){
             String [] args = (String [])i.next() ;
             if( args.length < 3 ){
                 System.err.println("MonitoringPanel : wrong syntax in "+propertyFileName ); 
                 continue ;
             }
             String name          = args[0] ;
             String className     = args[1] ;
             String description   = args[2] ;
             String argString     = args.length > 3 ? args[3] : "" ;
             try{
                Object [] argObjects = new Object[]{ name , description , argString , _connection } ;
                
                Class       cn = loader.loadClass( className ) ;
                Constructor cc = cn.getConstructor( argClasses ) ;
                
                ComponentMonitorable monitorable = (ComponentMonitorable)cc.newInstance( argObjects ) ;
                
                result.add( monitorable ) ;
                
             }catch(Exception eee ){
                System.err.println("Failed to generate : "+name+" : "+className+" : "+eee);
                continue ;
             }
          }
      }catch( Exception ee ){
         ee.printStackTrace();
         System.err.println("Couldn't load property file : "+ee ) ;
      }
      return  (ComponentMonitorable [])result.toArray( new ComponentMonitorable [result.size()] ) ;
   }
   private java.util.List loadLocalFile( String filename ) throws FileNotFoundException , IOException, java.net.MalformedURLException {

       URL fileURL = getClass().getResource("/plugin-files/" + filename);
       java.util.List map = new ArrayList();

       BufferedReader br = null;
       try {
           br = new BufferedReader(new InputStreamReader(fileURL.openStream()));
           while (true) {
               String line = br.readLine();
               if (line == null) break;
               //System.err.println("DEBUG : readline : "+line ) ;
               line = line.trim();
               if ((line.length() == 0) || (line.startsWith("#"))) continue;
               String[] list = line.split(":");
               map.add(list);
           }
       } finally {
           try {
               br.close();
           } catch (Exception ee) {
           }
       }
       return map;
   }
   public class MessageWindow extends JFrame  implements ActionListener  {
       private JTextArea _textField     = new JTextArea() ;
       private JButton   _disposeButton = new JButton("Dispose Window") ;
       public class WindowActions extends WindowAdapter {
           public void windowDeactivated( WindowEvent event ){
              setVisible(false);
           }
       }
       public class WindowMouseListener extends MouseAdapter {
           public void mouseClicked( MouseEvent event ){
             setVisible(false);
           }
       }
       public MessageWindow( String title ){
        
          super( title );
          
          Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

          int windowWidth  = 300 ; 
          int windowHeight = 200 ;

          _textField.setEditable(false);
          
          JPanel centerPanel = new JPanel(new BorderLayout());
          
          centerPanel.setBorder( new CellGuiSkinHelper.CellBorder("Detailed Results" ,  20  ) ) ;
          centerPanel.add( new JScrollPane( _textField ) , "Center"  ) ;
          getContentPane().add( centerPanel , "Center" ) ;



          pack() ;

          Point p = _ourMaster.getLocation() ;
          
          setLocation( p.x , p.y  );
          setSize( windowWidth , windowHeight );
          addWindowListener( new WindowActions() );
          
          WindowMouseListener mouseListener = new WindowMouseListener() ;
          addMouseListener(mouseListener);
          _textField.addMouseListener(mouseListener);
          _disposeButton.addMouseListener(mouseListener) ;

          
       }
       public void actionPerformed( ActionEvent event ){
           setVisible(false);
       }
       public void setText( String text , Point point ){
           _textField.setText(text);
           Dimension d = _textField.getPreferredSize() ;

           System.out.println("Prefered size : "+d+" location : "+point);

          // setLocation( point.x , point.y ) ;
          // setSize( 200 , 100 ) ;
           setVisible(true);         
       }
   }
   public class MonitorComponentPanel extends CellGuiSkinHelper.CellPanel implements ActionListener {
   
       private HashMap   _componentMap  = new HashMap() ;
       private ArrayList _componentList = new ArrayList() ;
       private int       _currentlyActive = -1 ;
       private javax.swing.Timer _timer = new javax.swing.Timer( 1000, this);
       
       public void go(){
           if( _currentlyActive > -1 )return ;
           _currentlyActive = 0 ;
           resetList() ;
           ComponentEntry entry = (ComponentEntry)_componentList.get(0) ;
           if( ! entry._check.isSelected() )return ;
           entry._component.start() ;
           entry._name.setForeground(Color.red);
       }
       private class ComponentEntry implements MouseListener{
          private ComponentMonitorable _component ;
          private JLabel    _status       = null ;
          private JLabel    _description  = null ;
          private JLabel    _name         = null ;
          private JCheckBox _check        = null ;
          
          private ComponentEntry( ComponentMonitorable com ){
             _component = com ;
             _name         = new JLabel( _component.getName() , JLabel.CENTER ) ;
             _description  = new JLabel( _component.getShortDescription() , JLabel.LEFT ) ;
             _status       = new JLabel( _component.getActionMessage() , JLabel.CENTER ) ;
             _check        = new JCheckBox() ;
             _check.setSelected(true);
             _status.addMouseListener( this ) ;
             
             CellGuiSkinHelper.setComponentProperties(_name);
             CellGuiSkinHelper.setComponentProperties(_description);
             CellGuiSkinHelper.setComponentProperties(_status);
             CellGuiSkinHelper.setComponentProperties(_check);
          }
          public void reset(){ _component.reset() ; }
          public void updateText(){
              _status.setText( _component.getActionMessage() ) ;
              switch( _component.getResultCode() ){
                 case ComponentMonitorable.RESULT_IDLE :
                   _status.setForeground( Color.black ) ;
                 break ;
                 case ComponentMonitorable.RESULT_ACTIVE :
                   _status.setForeground( Color.blue ) ;
                 break ;
                 case ComponentMonitorable.RESULT_FATAL :
                   _status.setForeground( Color.red ) ;
                 break ;
                 case ComponentMonitorable.RESULT_WARNING :
                   _status.setForeground( Color.orange ) ;
                 break ;
                 case ComponentMonitorable.RESULT_OK :
                   _status.setForeground( Color.green ) ;
                 break ;
                 case ComponentMonitorable.RESULT_CRITICAL :
                   _status.setForeground( Color.red ) ;
                 break ;
              }
              _status.setToolTipText( _component.getResultDetails() ) ;
          }
          public void mouseClicked( MouseEvent event ){
             System.out.println("Mouse Clicked");
             Point point = event.getPoint() ;
             SwingUtilities.convertPointToScreen( point , _status ) ;

             showInWindow(_component.getResultDetails() , point );
          }
          public void mouseEntered( MouseEvent event ){}
          public void mouseExited( MouseEvent event ){}
          public void mousePressed( MouseEvent event ){}
          public void mouseReleased( MouseEvent event ){}
       }
       public MonitorComponentPanel( ComponentMonitorable [] components ){
       
          setBorder(
               BorderFactory.createTitledBorder(
                   BorderFactory.createLineBorder( CellGuiSkinHelper.getForegroundColor() , 1 ) , "Check Compoments" )
          ) ;
           
          GridBagLayout     lo = new GridBagLayout() ;
          GridBagConstraints c = new GridBagConstraints()  ;
          setLayout( lo ) ;

         c.gridheight = 1 ;
         c.insets     = new Insets(4,4,4,4) ;

         c.fill       = GridBagConstraints.HORIZONTAL ;
         c.weightx    = 1.0 ;
          
          for( int i = 0 ; i < components.length ; i++ ){
              ComponentEntry entry = new ComponentEntry( components[i] ) ;
              _componentMap.put( components[i].getName() , entry ) ;
              _componentList.add( entry ) ;

              c.gridwidth  = 1 ; c.gridx = 0 ; c.gridy = i ;c.weightx    = 0.0 ;
              add( entry._name , c ) ;
              c.gridwidth  = 1 ; c.gridx = 1 ; c.gridy = i ;c.weightx    = 0.0 ;
              add( entry._check , c ) ;
              c.gridwidth  = 1 ; c.gridx = 2 ; c.gridy = i ;c.weightx    = 0.0 ;
              add( entry._description , c  ) ;
              c.gridwidth  = 1 ; c.gridx = 3 ; c.gridy = i ;c.weightx    = 1.0 ;
              add( entry._status , c ) ;
             
          
          }
          _timer.start();
       }
       private void showInWindow( String message , Point point ){
          if( _messageWindow == null ){
             Component comp = this ;
             for( int i = 0 ; i < 100 ; i++ ){
                 Component c = comp.getParent() ;
                 if( c == null )break ;
                 //System.err.println("!!! Component : "+c);
                 comp = c ;
             }
             if( _messageWindow == null )_messageWindow = new MessageWindow( "Result Message Window" );
          }

          _messageWindow.setText(message, point );
       }
       public void actionPerformed( ActionEvent event ){
          Object source = event.getSource() ;
          if( source == _timer ){
             if( _componentList == null )return ;
             if( _currentlyActive < 0 )return ;
             ComponentEntry entry = (ComponentEntry)_componentList.get(_currentlyActive);
             if( ! entry._component.isStillActive() ){
                entry.updateText() ;
                entry._name.setForeground(Color.black);
                _currentlyActive++ ;
                if( _currentlyActive >= _componentList.size() ){
                   _currentlyActive = -1 ;
                }else{
                   entry = (ComponentEntry)_componentList.get(_currentlyActive);
                   if( ! entry._check.isSelected() )return ;
                   entry._component.start() ;
                   entry._name.setForeground(Color.red);
                   entry.updateText() ;
                }
             }else{
                entry.updateText() ;
             }
          }
       } 
       private void updateList(){
          if( _componentList == null )return ;
          try{
             for( Iterator i = _componentList.iterator() ; i.hasNext() ; ){
                 ComponentEntry entry = (ComponentEntry)i.next() ;
                 entry.updateText() ;
             }
          }catch(Exception ee ){
            
          }
       }
      private void resetList(){
          if( _componentList == null )return ;
          try{
             for( Iterator i = _componentList.iterator() ; i.hasNext() ; ){
                 ComponentEntry entry = (ComponentEntry)i.next() ;
                 entry.reset() ;
                 entry.updateText() ;
             }
          }catch(Exception ee ){
            
          }
       }
     
   }
   public void domainAnswerArrived( Object obj , int subid ){
       System.out.println( "Answer ("+subid+") : "+obj.toString() ) ;
   }
   public void actionPerformed( ActionEvent event ){
      Object source = event.getSource() ;
      if( source == _goButton ){
          _monitorComponents.go() ;
      }
   } 
   public void connectionOpened( DomainConnection connection ){
      System.out.println("Connection opened");
      //_controller.askForPoolDecision() ;
   }
   public void connectionClosed( DomainConnection connection ){
      System.out.println("Connection closed" ) ;
   }
   public void connectionOutOfBand( DomainConnection connection, Object obj ){
      System.out.println("Connection connectionOutOfBand "+obj ) ;
   }
}
