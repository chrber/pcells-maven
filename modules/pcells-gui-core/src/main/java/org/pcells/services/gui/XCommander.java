// $Id: XCommander.java,v 1.5 2006/12/23 18:07:09 cvs Exp $
//
package org.pcells.services.gui ;
//

import org.pcells.services.connection.DomainConnection;
import org.pcells.util.Logable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class      XCommander 
       extends    CellGuiSkinHelper.CellPanel 
       implements ActionListener, ItemListener, Logable    {
                  
   private DomainConnection _connection = null ;
   private JConsole      _console       = null ;
   private DeepSpaceScan _deepSpaceScan = null ;
   private CardLayout    _masterCards   = new CardLayout() ;
   private JPanel        _masterBoard   = null ;
   private JTopologyPane _topology      = null ;
   private EasyCommander _commander     = null ;
   private SelectionCommander _scommander = null ;
   private MasterPanel   _masterPanel   = null ;
   private java.util.List _domainList   = null ;
   private String         _currentCard  = null ;
   private String         _previousCard = null ;
   private Logable        _logable      = null ;
   
   private JRadioButton   _showTopology  = new JRadioButton("Topology");
   private JRadioButton   _showCommander = new JRadioButton("Commander");
   private JButton        _scan          = new CellGuiSkinHelper.CellButton("Scan Domains");
   
   private static final String  TOPOLOGY    = "Topology" ;
   private static final String  ECOMMANDER  = "EasyCommander" ;
   private static final String  XCOMMANDER  = "XCommander" ;
   private String [] _selections = { 
          TOPOLOGY , 
          ECOMMANDER , 
          XCOMMANDER 
   } ;
   private JComboBox _selection  = new JComboBox( _selections ) ;
   
   public XCommander( DomainConnection connection ){
      this( connection , null );
   }
   public XCommander( DomainConnection connection , Logable logable ){
      setLayout( new BorderLayout(10,10) );
      _connection = connection ;
      _logable    = logable ;
      _deepSpaceScan = new DeepSpaceScan( connection , this ) ;
      _deepSpaceScan.addActionListener(this);
      
      _topology = new JTopologyPane() ;
      _topology.setMessage("");
      _topology.addActionListener(this);
      
      _showTopology.addActionListener(this);
      _showCommander.addActionListener(this);
      
      _selection.addItemListener( this ) ;
      CellGuiSkinHelper.setComponentProperties( _selection ) ;
      
      _commander  = new EasyCommander( connection ) ;
      _scommander = new SelectionCommander( connection ) ;
      
      _masterPanel = new MasterPanel() ;
      
      _masterBoard = new CellGuiSkinHelper.CellPanel( _masterCards ) ;
      _masterBoard.add( _topology    , "topology" ) ;
      _masterBoard.add( _masterPanel , "master" ) ;
            
      _scan.addActionListener(this);
      
      add( _masterBoard ) ;
    
      showTopology();  
      _previousCard = XCOMMANDER ;
   }
   private int counter = 0 ;
   private synchronized void showCard( String card ){
     counter++;
     log(""+counter+
          "Show card req="+card+
          " cur="+_currentCard+
          " pre="+_previousCard);
      
     try{

        if( card.equals(TOPOLOGY) ){
           _masterCards.show( _masterBoard , "topology" ) ;
        }else{
           _masterCards.show( _masterBoard , "master" ) ;
           _masterPanel.show( card);
        }
        _previousCard = _currentCard ;
        _currentCard  = card ;
     }finally{
        log(""+counter+
             "Show card status "+
             " cur="+_currentCard+
             " pre="+_previousCard);
     }
   }
   private void showPrevious(){
       log("showPrevious : "+
       _selection.getSelectedItem()+" => "+_previousCard);
       _selection.setSelectedItem( _previousCard ) ;
   }
   
   private void showTopology(){
        _selection.setSelectedItem( TOPOLOGY ) ;
   }
   private void showEasyCommander(){
        _selection.setSelectedItem( ECOMMANDER ) ;
   }
   private void showXCommander(){
        _selection.setSelectedItem( XCOMMANDER ) ;
   }
   private class MasterPanel
          extends CellGuiSkinHelper.CellPanel       {
          
        
      private CardLayout    _cards   = new CardLayout() ;
      private JPanel        _board   = null ;
      
      private class SouthPanel extends CellGuiSkinHelper.CellPanel {
         private SouthPanel(){
            super( new FlowLayout() ) ;
/*
            setBorder(

                BorderFactory.createCompoundBorder(
                      BorderFactory.createLineBorder(Color.red) ,
                      BorderFactory.createEmptyBorder(8,8,8,8)
                )

             ) ;
*/
            add( _scan      ) ;
            add( _selection ) ;
//            add( _showTopology ) ;
//            add( _showCommander ) ;
            
//            _showTopology.setSelected(false);
//            _showCommander.setSelected(true);
//            ButtonGroup group = new ButtonGroup();
//            group.add(_showTopology);
//            group.add(_showCommander);
         }
         public Insets getInsets(){ return new Insets(4,4,4,4);}
         
      }
      private MasterPanel(){
         setLayout( new BorderLayout(4,4) ) ;
         
         JPanel south = new SouthPanel() ;
         
         add( south , "South" ) ;
         
         _board = new CellGuiSkinHelper.CellPanel( _cards ) ;
         _board.add( _commander , ECOMMANDER ) ;
         _board.add( _scommander , XCOMMANDER ) ;
         
         add( _board , "Center" ) ;
      } 
      private void show( String board ){ _cards.show( _board , board ) ; }
   }
   public void itemStateChanged( ItemEvent event ){

      if( event.getStateChange() != ItemEvent.SELECTED )return ;
      String card = (String)event.getItem();
      System.out.println("itemStateChanged : "+card);
      showCard(card);
      
   }
   public void actionPerformed( ActionEvent event ){
      Object source = event.getSource() ;
      if( source == _topology ){
        JTopologyPane.TopologyEvent t = (JTopologyPane.TopologyEvent)event ;
        MouseEvent mouse = t.getMouseEvent();
        if( mouse.getButton() == MouseEvent.BUTTON3 ){
           try{
              _deepSpaceScan.scan();
              _topology.setMessage("S c a n n i n g");
              _previousCard = TOPOLOGY ;
           }catch(Exception ee ){
              elog("Got exception while starting scan : "+ee);
           }
        }else{
        
           String destination = t.getDomainName() ;
           if( destination != null ){
              _commander.setDestination("System@"+destination);
              _scommander.setDomain(destination);
           }
           _previousCard = _previousCard == TOPOLOGY ?
                           XCOMMANDER : _previousCard;
           showPrevious() ;
        }
      }else if( source == _showTopology ){
           showCard(TOPOLOGY);
      }else if( source == _showCommander ){
           showCard(XCOMMANDER);
      }else if( source == _scan ){
         try{
            _deepSpaceScan.scan();
            _topology.setMessage("S c a n n i n g");
            showTopology();
         }catch(Exception ee ){
            elog("Got exception while starting scan : "+ee);
         }
      }else if( source == _deepSpaceScan ){
         DeepSpaceScan.ScanFinishedEvent d = (DeepSpaceScan.ScanFinishedEvent)event ;
         _domainList = d.getDomainList() ;
         _topology.setTopology(_domainList);
         _scommander.setTopology(_domainList);
         
         showPrevious();
      }
      
   }
   public void log( String message ){
     if( _logable != null )_logable.log(message);
     else System.out.println(message);
   }
   public void elog( String message ){
     if( _logable != null )_logable.elog(message);
     else System.err.println(message);
   }
   public void plog( String message ){
     elog(message);
   }

}
