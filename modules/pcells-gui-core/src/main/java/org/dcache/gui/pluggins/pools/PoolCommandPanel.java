// $Id: PoolCommandPanel.java,v 1.2 2008/07/08 15:52:09 cvs Exp $
//
package org.dcache.gui.pluggins.pools;
//

import org.pcells.services.connection.DomainConnection;
import org.pcells.services.gui.CellGuiSkinHelper;
import org.pcells.services.gui.EasyCommander;
import org.pcells.services.gui.LoadedPicturePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.prefs.Preferences;

public class      PoolCommandPanel
       extends    CellGuiSkinHelper.CellPanel 
       implements ActionListener{
                  
   private DomainConnection _connection    = null ;
   private Preferences      _preferences   = null ;
   private PoolGroupLinkTree _directory    = null ;
   private CardLayout        _switchCard  = new CardLayout();
   private JPanel            _switchPanel = new CellGuiSkinHelper.CellPanel( _switchCard ) ;
   private EasyCommander     _commander   = null ;
   private PoolControlPanel  _poolControl = null ;
   private MultiPoolCommandPanel _multiCommand = null ;

   private WaitPanel          _waitPanel    = new WaitPanel() ;
   private LoadedPicturePanel _waitingImage = new LoadedPicturePanel( "/images/sheep004.png" , 200 ) ;
   private boolean            _sheep        = true ;

   private class WaitPanel extends JPanel {
       private Color  _currentColor = Color.green;
       private double _fraction     = (double) 0.4 ;
       private boolean _waiting     = false ;
       private WaitPanel(){
          setOpaque(true);
       }
       private void setWaiting( double fraction ){
          _fraction     = fraction ;
          _waiting      = true ;
          _currentColor = Color.red ;
          repaint();
       }
       private void setOk(){
          _waiting      = false ;
          _currentColor = Color.green ;
          repaint();
       }
       public void paintComponent( Graphics g ){
           Dimension size = getSize() ;
           g.setColor(_currentColor);
           g.fillRect(0,0,size.width,size.height);

           if( ! _waiting )return ;
           Graphics2D g2 = (Graphics2D) g ;
           g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                               RenderingHints.VALUE_ANTIALIAS_ON);

           int dim = 200 ;
           int x = ( size.width - dim ) / 2 ;
           int y = ( size.height - dim ) / 2 ;
           g.setColor( Color.gray ) ;
           g.fillOval( x + 5, y + 5 , dim , dim ) ;
           g.setColor( Color.green ) ;
           g.fillOval( x  , y  , dim , dim ) ;

           g.setColor( Color.red ) ;
           g.fillArc( x  , y  , dim , dim , 0 , (int) ( _fraction * (double)360.0 ) ) ;

       }
   }
   private void setWaiting( String message , double waiting ){
      if( _sheep ){
         _waitingImage.setMessage(message);
         _waitingImage.setProgress(waiting);
      }else{
         _waitPanel.setWaiting(waiting);
      }
   }
   private void setOk(){
      if( _sheep ){
         _waitingImage.setMessage("Finished");
         _waitingImage.setProgress(1.0);
      }else{
         _waitPanel.setOk();
      }
   }
   public PoolCommandPanel( DomainConnection connection , Preferences preferences ){

      _connection  = connection ;
      _preferences = preferences ;

      BorderLayout l = new BorderLayout(10,10) ;
      setLayout(l) ;

      setBorder( new CellGuiSkinHelper.CellBorder("Pool Commander" , 25 ) ) ;

      _directory = new PoolGroupLinkTree( _connection , _preferences ) ;
      _directory.addActionListener(this);

      _commander = new EasyCommander( connection ) ;

      _multiCommand = new MultiPoolCommandPanel( connection ) ;

      _poolControl  = new PoolControlPanel( connection , preferences ) ;

      //_switchPanel.add( _commander   , "commander" ) ;

      _switchPanel.add( _poolControl    , "pool" ) ;
      _switchPanel.add( _sheep ?_waitingImage : _waitPanel , "wait" ) ;
      _switchPanel.add( _multiCommand   , "multi" ) ;
      _switchPanel.setMinimumSize( new Dimension(0,0) ) ;

      JSplitPane split = new JSplitPane(
                               JSplitPane.HORIZONTAL_SPLIT ,
                               _directory ,
                               _switchPanel  ) ;

      add( split , "Center" ) ;

      split.resetToPreferredSizes();

      _switchCard.show( _switchPanel , "wait" ) ;

   }
   private javax.swing.Timer _timer = null ;
   public void actionPerformed( ActionEvent event ){
      Object source = event.getSource() ;
      if( source == _timer ){
          double progress = _directory.getProgress() ;
          //System.out.println("TIMER : "+progress);
          setWaiting("Waiting for Pool Info",progress) ;
      }else if( source == _directory ){
          System.out.println("actionPerformed : "+event);
          String command = event.getActionCommand() ;
          if( command.equals("updating") ){
              setWaiting("Waiting",0.0) ;
              _switchCard.show( _switchPanel , "wait" ) ;

              _timer = new javax.swing.Timer( 500 , this ) ;
              _timer.start() ;
          }else if( command.equals("finished") ){
              _timer.stop() ;
              setOk() ;
              _switchCard.show( _switchPanel , "wait" ) ;
          }else if( command.equals("node") ){
             PoolGroupLinkTree.PoolGroupLinkTreeEvent e = (PoolGroupLinkTree.PoolGroupLinkTreeEvent)event ;
             Object o = e.getCurrentNode() ;
             if( o == null ){
                setOk() ;
                _switchCard.show( _switchPanel , "wait" ) ;
             }else if( o instanceof PoolGroupLinkTree.LinkEntry ){
                 PoolGroupLinkTree.LinkEntry link = (PoolGroupLinkTree.LinkEntry)o ;
                 setOk() ;
                 PoolGroupLinkTree.PoolGroupEntry poolGroup = (PoolGroupLinkTree.PoolGroupEntry)link.getChildByName("Resolved Pools") ;
                 displayPoolGroup( poolGroup ) ;
             }else if( o instanceof PoolGroupLinkTree.PoolEntry ){
                 PoolGroupLinkTree.PoolEntry pool = (PoolGroupLinkTree.PoolEntry)o ;
                 _poolControl.setDestination( pool.getName() ) ;
                 _switchCard.show( _switchPanel , "pool" ) ;
             }else if( o instanceof PoolGroupLinkTree.PoolGroupEntry ){
                 PoolGroupLinkTree.PoolGroupEntry poolGroup = (PoolGroupLinkTree.PoolGroupEntry)o ;
                 displayPoolGroup( poolGroup ) ;
             }
          }
      }
    }
      private void displayPoolGroup( PoolGroupLinkTree.PoolGroupEntry poolGroup ){
            Enumeration ee = poolGroup.children() ;
            String [] poolNames = new String[poolGroup.getChildCount()];
            for( int i = 0 ; ee.hasMoreElements() ; i++  ){
               poolNames[i] = ee.nextElement().toString() ;
            }
            Arrays.sort( poolNames );
            _multiCommand.setPools( poolNames ) ;
            setOk() ;
            _switchCard.show( _switchPanel , "multi" ) ;
      }
   
}
