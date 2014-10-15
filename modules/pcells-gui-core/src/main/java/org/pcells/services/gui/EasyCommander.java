// $Id: EasyCommander.java,v 1.7 2006/12/24 06:44:18 cvs Exp $
//
package org.pcells.services.gui ;
//

import dmg.cells.nucleus.CellInfo;
import dmg.cells.nucleus.CellRoute;
import org.pcells.services.connection.DomainConnection;
import org.pcells.services.connection.DomainConnectionListener;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.SimpleDateFormat;
import java.util.*;

public class      EasyCommander 
       extends    CellGuiSkinHelper.CellPanel 
       implements ActionListener, 
                  DomainConnectionListener,
                  MouseListener  {
 
   private SimpleDateFormat  _formatter   = new SimpleDateFormat ("MM.dd HH:mm:ss");                 
   private JTextArea         _displayArea = new JTextArea() ;
   private JScrollPane       _scrollPane  = null ;
   private JButton           _clearButton = new CellGuiSkinHelper.CellButton("Clear") ;
   private DomainConnection  _connection  = null ;
   private JHistoryTextField _command     = new JHistoryTextField() ;
   private JComboBox         _destination = new JHistoryComboBox2() ;
   private JButton           _pinboard    = new CellGuiSkinHelper.CellButton("Processes");
   private JButton           _info        = new CellGuiSkinHelper.CellButton("Routes");
   private JButton           _help        = new CellGuiSkinHelper.CellButton("Help");
   private boolean           _isSystem    = true ;
   private Font              _displayFont = new Font("Monospaced",Font.PLAIN,12);
   private Font              _bigFont     = new Font("Courier",Font.BOLD,25+1);
   private JLabel            _header      = new JLabel("",JLabel.CENTER);
   private ECDataModel       _model       = new ECDataModel() ;
   private JTable            _processTable = null ;
   private JTable            _routeTable  = null ;
   private CardLayout        _cards       = null ;
   private JPanel            _cardsPanel  = null ;
   private ProcessDataModel  _processData = new ProcessDataModel() ;
   private RouteDataModel    _routeData   = new RouteDataModel() ;
   
   public class ECDataModel {
      private ComboBoxModel _comboBoxModel  = null ;
      private Document  _commandDocument    = null ;
      private Document  _displayDocument    = null ;
      private Vector    _vector             = null ;
      private ListCellRenderer  _renderer   = null;
   }
   public ECDataModel getModel(){ return _model ; }
   public void setModel( ECDataModel model ){
     _destination.setModel( model._comboBoxModel ) ;
     _displayArea.setDocument( model._displayDocument ) ;
     _command.setDocument( model._commandDocument ) ;
     _command.setVector( model._vector ) ;
   }
   public class ProcessDataModel extends AbstractTableModel {
   
         private java.util.List _list     = new ArrayList() ;

         public int getColumnCount() { return 6; }
         public int getRowCount() { return _list.size() ;}
         public String getColumnName( int pos ){
            switch( pos ){
               case 0 : return "Name" ;
               case 1 : return "Class" ;
               case 2 : return "T" ;
               case 3 : return "M" ;
               case 4 : return "Created" ;
               case 5 : return "Info" ;
            }
            return "" ;
         }
         public CellInfo get( int row ){ return (CellInfo)_list.get(row); }
         public Object getValueAt(int row, int col) {
            CellInfo entry = (CellInfo)_list.get(row);
            switch( col ){
               case 0 :
                  return entry.getCellName() ;
               case 1 :
                  return dmg.util.Formats.cutClass(entry.getCellClass()) ;
               case 2 :
                  return ""+entry.getThreadCount() ;
               case 3 :
                  return ""+entry.getEventQueueSize() ;
               case 4 :
                  return _formatter.format( entry.getCreationTime() ) ;
               case 5 :
                  return entry.getShortInfo() ;
            }
            return null ;
         }
         public void setCellInfos( CellInfo [] infos ){
            _list = Arrays.asList( infos );
            Collections.sort( _list , _cellCompare ) ;
            fireTableDataChanged() ;
         }   
   }
   public class RouteComparator implements Comparator {
       public int compare( Object a , Object b ){
          CellRoute ra = (CellRoute)a ;
          CellRoute rb = (CellRoute)b ;
          String ca = ra.getRouteTypeName()+
                      ra.getTargetName()+
                      ra.getDomainName()+
                      ra.getCellName() ;
          String cb = rb.getRouteTypeName()+
                      rb.getTargetName()+
                      rb.getDomainName()+
                      rb.getCellName() ;
                      
          return ca.compareTo(cb);
       }
   }
   public class CellComparator implements Comparator {
       public int compare( Object a , Object b ){
          CellInfo ra = (CellInfo)a ;
          CellInfo rb = (CellInfo)b ;
          String ca = ra.getCellName() ;
          String cb = rb.getCellName() ;
                      
          return ca.compareTo(cb);
       }
   }
   private RouteComparator _routeCompare = new RouteComparator() ;
   private CellComparator  _cellCompare  = new CellComparator() ;
   
   public class RouteDataModel extends AbstractTableModel {
   
         private java.util.List _list     = new ArrayList() ;

         public int getColumnCount() { return 4; }
         public int getRowCount() { return _list.size() ;}
         public String getColumnName( int pos ){
            switch( pos ){
               case 0 : return "Cell" ;
               case 1 : return "Domain" ;
               case 2 : return "Target" ;
               case 3 : return "Type" ;
            }
            return "" ;
         }
         public CellRoute get( int row ){ return (CellRoute)_list.get(row); }
         public Object getValueAt(int row, int col) {
            CellRoute entry = (CellRoute)_list.get(row);
            switch( col ){
               case 0 :
                  return entry.getCellName() ;
               case 1 :
                  return entry.getDomainName() ;
               case 2 :
                  return entry.getTargetName() ;
               case 3 :
                  return entry.getRouteTypeName() ;
            }
            return null ;
         }
         public void setRouteInfos( CellRoute [] infos ){
            _list = Arrays.asList( infos );
            Collections.sort( _list , _routeCompare ) ;
            fireTableDataChanged() ;
         }   
   }
   public EasyCommander( DomainConnection connection ){
     _connection = connection ;
     
     setLayout(new BorderLayout(10,10)) ;

     CellGuiSkinHelper.setComponentProperties( _destination ) ;
     CellGuiSkinHelper.setComponentProperties( _header ) ;
     
     _displayArea.setEditable(false);
     _displayArea.setFont( _displayFont ) ;
     _displayArea.addMouseListener(this) ;
     _header.setFont(_bigFont);
     _scrollPane = new JScrollPane( _displayArea ) ;
     
     _processTable = new JTable( _processData ) ;
     _routeTable   = new JTable( _routeData ) ;
     
     _cards      = new CardLayout() ;
     _cardsPanel = new CellGuiSkinHelper.CellPanel(_cards) ;

     _cardsPanel.add( _scrollPane  , "commander" ) ; 
     _cardsPanel.add( new JScrollPane( _processTable ) , "processes" ) ;
     _cardsPanel.add( new JScrollPane( _routeTable )   , "routes" ) ;
     
     _cards.show( _cardsPanel , "commander" ) ;
     
     add( _cardsPanel   , "Center" ) ;

     _clearButton.addActionListener(this);
     _destination.addActionListener(this);
     _pinboard.addActionListener(this);
     _info.addActionListener(this);
     _command.addActionListener(this);
     _help.addActionListener(this);
     
     _model._comboBoxModel   = _destination.getModel() ;
     _model._displayDocument = _displayArea.getDocument() ;
     _model._commandDocument = _command.getDocument() ;
     _model._vector          = _command.getVector() ;
     
     JPanel south = createSouth();

     add( south , "South" ) ;
     add( _header , "North");
     setBorder(

        BorderFactory.createCompoundBorder(
              BorderFactory.createTitledBorder("EasyCommander") ,
              BorderFactory.createEmptyBorder(8,8,8,8)
        )

     ) ;
     
     createPopup();
   }
   private static JFrame  __easyCommanderFrame = null ;
   private static EasyCommander.ECDataModel __easyCommanderDataModel = null ;
   static public EasyCommander getEasyCommander( DomainConnection connection ){
          EasyCommander commander = new EasyCommander(connection);
          if( __easyCommanderDataModel == null ){
                __easyCommanderDataModel = commander.getModel() ;
          }else{
                commander.setModel(__easyCommanderDataModel); 
          }
          return commander ; 
   }
   static public  void showEasyCommander( DomainConnection connection ){
      if( __easyCommanderFrame == null ){
          EasyCommander commander = getEasyCommander(connection);
          __easyCommanderFrame = new JFrame("EasyCommander");
          __easyCommanderFrame.getContentPane().setLayout( new BorderLayout(10,10) ) ;
          __easyCommanderFrame.getContentPane().add(  commander , "Center" );
          __easyCommanderFrame.pack();
          __easyCommanderFrame.setSize(new Dimension(400,300));
//          __easyCommanderFrame.setPosition(new Dimension(400,300));
       }
       __easyCommanderFrame.setVisible(true);  
 
   }
   public void setDestination( String text ){ 
     _destination.addItem(text) ; 
     _destination.setSelectedItem(text);
   }
   public void paintComponent( Graphics gin ){
       Graphics2D g = (Graphics2D) gin ;
       g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                          RenderingHints.VALUE_ANTIALIAS_ON);
      super.paintComponent( g ) ;
   }
   private JPanel createSouth(){
       GridBagLayout     lo = new GridBagLayout() ;
       GridBagConstraints c = new GridBagConstraints()  ;
       JPanel panel = new CellGuiSkinHelper.CellPanel( lo ) ;
       
       c.gridheight = 1 ;
       c.insets     = new Insets(4,4,4,4) ;
       
       c.gridwidth  = 1 ; c.gridx = 0 ; c.gridy = 0 ;
       panel.add( _pinboard , c ) ; 
       c.gridwidth  = 1 ; c.gridx = 1 ; c.gridy = 0 ;
       panel.add( _info , c ) ; 
       c.gridwidth  = 1 ; c.gridx = 2 ; c.gridy = 0 ;
       panel.add( _help , c ) ; 
       c.gridwidth  = 1 ; c.gridx = 3 ; c.gridy = 0 ;

       JLabel l = new JLabel("Cell Name : ") ;
       CellGuiSkinHelper.setComponentProperties( l ) ;
       panel.add( l , c ) ; 
       c.gridwidth  = 1 ; c.gridx = 4 ; c.gridy = 0 ; c.weightx = 1.0 ;
       c.fill = GridBagConstraints.HORIZONTAL ;
       panel.add( _destination , c ) ; 
       c.gridwidth  = 1 ; c.gridx = 5 ; c.gridy = 0 ; c.weightx = 0.0 ;
       panel.add( _clearButton , c ) ; 
       
       
       c.gridwidth  = 1 ; c.gridx = 0 ; c.gridy = 1 ;
       
       l = new JLabel("Command : ") ;
       CellGuiSkinHelper.setComponentProperties( l ) ;
       panel.add( l , c ) ; 
       c.gridwidth  = 5 ; c.gridx = 1 ; c.gridy = 1 ;
       panel.add( _command , c ) ; 
       
       JPanel jp = new CellGuiSkinHelper.CellPanel( new BorderLayout() ) ;
       jp.add( panel , "Center" ) ;
       return jp ;
   }
   public void domainAnswerArrived( Object obj , int id ){
//      append("DEBUG : class arrived : "+obj.getClass().getName());
      if( obj instanceof CellRoute [] ){
      
          CellRoute [] info = (CellRoute [])obj ;
          
          _routeData.setRouteInfos( info ) ;
          
          _cards.show( _cardsPanel , "routes" ) ;

      }else if( obj instanceof CellInfo [] ){
      
          CellInfo [] info = (CellInfo [])obj ;
          
          _processData.setCellInfos( info ) ;
          
          _cards.show( _cardsPanel , "processes" ) ;

      }else if( obj instanceof Object [] ){
         _cards.show( _cardsPanel , "commander" ) ;
         Object [] array = (Object [])obj ;
         for( int i = 0 , n = array.length ; i < n ;i++ ){
            append(array[i].toString());
            append("\n");
         }
      }else{
         _cards.show( _cardsPanel , "commander" ) ;
         append(obj.toString());
         append("\n");
      }
      if(_useOutputDelimiter)
          append(" << --- " + new Date().toString() +" --- >>\n\n");
   }
   public void actionPerformed( ActionEvent event ){
      Object source = event.getSource() ;
      Object selected = _destination.getSelectedItem() ;
      String destination = selected == null ? "" : selected.toString() ;
      if( source == _clearButton ){
         _displayArea.setText("");
      }else if( source == _pinboard ){
        if( _isSystem ){
           sendCommand( "getcellinfos" , destination ) ;
        }else{
           sendCommand( "show pinboard 200" , destination ) ;
        }
      }else if( source == _info ){
        if( _isSystem ){
           sendCommand( "getroutes" , destination ) ;
        }else{
           sendCommand( "info" , destination ) ;
        }
      
      }else if( source == _command ){
         if( _command.equals("") )return ;
         sendCommand( _command.getText() , destination ) ;
         _command.setText("");
      }else if( source == _help ){
         sendCommand( "help" , destination ) ;
      }else if( source == _destination ){
         if( getCellNameOfPath(destination).equals("System") ){
            makeSystem() ;
         }else{
            makeOthers();
         }
         _header.setText(getDestinationOfPath(destination));
      }
   }
   private void sendCommand( String command , String destination ){
     try{
        if( ( destination == null ) || ( destination.equals("") ) )
           _connection.sendObject(command,this,5);
        else
           _connection.sendObject(destination,command,this,5);
     }catch(Exception ee ){
        append(" EXCEPTION WHEN SENDING COMMAND : "+ee );
     }
   }
   private String getCellNameOfPath( String path ){
      int pos = path.lastIndexOf(":");
      String rest = null ;
      if( pos < 0 ){
         rest = path ;
      }else{
         rest = path ;
         if( pos == ( path.length() - 1 ) )return "" ;
         rest = path.substring(pos+1);
      }
      pos = rest.indexOf("@");
      if( pos < 0 )return rest ;
      if( pos ==  0 )return "" ;
      return rest.substring(0,pos);
   }
   private String getDestinationOfPath( String path ){
      int pos = path.lastIndexOf(":");
      String rest = null ;
      if( pos < 0 )return path ;
      if( pos == ( path.length() - 1 ) )return "" ;
      return path.substring(pos+1);
   }
   private void makeSystem(){
      _isSystem = true ;
      _pinboard.setText("Processes");
      _info.setText("Routes");
   }
   private void makeOthers(){
      _isSystem = false ;
      _pinboard.setText("Pinboard");
      _info.setText("Info");
   }
   private void append( String text ){
      _displayArea.append(text);
      SwingUtilities.invokeLater(

         new Runnable(){
            public void run(){
                Rectangle rect = _displayArea.getBounds() ;
                rect.y = rect.height - 30 ;
                _scrollPane.getViewport().scrollRectToVisible( rect ) ;
            }
         }
     ) ;
   }
   public void mouseClicked( MouseEvent event ){
//     if( event.getClickCount() > 1 )_displayArea.setText("");
//       System.out.println("Mouse clicked");
       if( event.getButton() != MouseEvent.BUTTON3 )return ;
       Highlighter hl = _displayArea.getHighlighter();
       Highlighter.Highlight [] lights = hl.getHighlights() ;
       if( lights == null ){
//          System.out.println("No hightlights");
          return ;
       }
       if( lights.length < 1 )return ;
       int min  = lights[0].getStartOffset() ;
       int max  = lights[0].getEndOffset() ;
       String text = _displayArea.getText() ;
       if( text == null )return ;
       text = text.substring(min,max).trim() ;
       if( text.equals("") )return ;
       if( text.indexOf("\n") > -1 )return ;
       setDestination( text) ;

   }
   public void mouseEntered( MouseEvent event ){
   }
   public void mouseExited( MouseEvent event ){
   }
   public void mousePressed( MouseEvent event ){
      if( event.getButton() != MouseEvent.BUTTON2 )return ;
      Point p = event.getPoint() ;
      _popup.show(this,p.x,p.y) ;
   }
   public void mouseReleased( MouseEvent event ){
   }
   private JPopupMenu _popup = null ;
   private boolean    _useOutputDelimiter  = true ;
   private JMenuItem  _outputDelimiterItem = null ;
   private void createPopup(){

        _popup = new JPopupMenu("Setup") ;

//        _popup.setBorderPainted(true);
        
        ActionListener al= new ActionListener(){
              public void actionPerformed(ActionEvent e){
                 if( _useOutputDelimiter ){
                    _useOutputDelimiter = false ;
                    _outputDelimiterItem.setText("Enable Output Delimiter");
                 }else{
                    _outputDelimiterItem.setText("Disable Output Delimiter");
                    _useOutputDelimiter = true ;
                 }
              }
           } ;

        _outputDelimiterItem = _popup.add( new JMenuItem("Disable Output Delimiter") ) ;
        _outputDelimiterItem.addActionListener(al) ;
        
    }

}
