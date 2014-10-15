// $Id: PnfsManagerPanel.java,v 1.1 2008/11/09 08:23:58 cvs Exp $
//
package org.dcache.gui.pluggins.pnfs;
//
import org.pcells.services.connection.DomainConnection;
import org.pcells.services.connection.DomainConnectionListener;
import org.pcells.services.gui.CellGuiSkinHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;


public class      PnfsManagerPanel
        extends    CellGuiSkinHelper.CellPanel
        implements ActionListener, DomainConnectionListener{

    private DomainConnection _connection      = null ;
    private Preferences      _preferences     = null ;

    private boolean _debug = true ;

    private static final int REQ_PNFSMGR_INFO = 12123 ;

    private PnfsManagerInfoDisplay _counterDisplay = new PnfsManagerInfoDisplay();
    private PnfsDetailPanel        _detailPanel    = new PnfsDetailPanel() ;

    private JPanel     _switchPanel  = null ;
    private CardLayout _switchLayout = null ;


    public PnfsManagerPanel( DomainConnection connection , Preferences preferences ){

        _connection  = connection ;
        _preferences = preferences ;

        BorderLayout l = new BorderLayout(10,10) ;
        setLayout(l) ;

        setBorder( BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder( Color.blue , 1 ) ,
                "Pnfs Manager" ) ) ;

        add( new Controller( )  , "North" ) ;

        _switchPanel = new JPanel( _switchLayout = new CardLayout() ) ;


        JPanel inner = new JPanel( new GridBagLayout() ) ;
        inner.add( _counterDisplay ) ;

        _switchPanel.add(   inner , "main" ) ;
        _switchPanel.add(   _detailPanel    , "detail" ) ;

        add( _switchPanel , "Center" ) ;

    }
    public void domainAnswerArrived( Object obj , int subid ){

        if( subid == REQ_PNFSMGR_INFO ){

            if( ( obj == null ) ){
                //
                // This is a timeout
                //
                //setWaiting(false);
                displayErrorMessage("Request to PnfsManager timed out" );

            }else if( obj instanceof dmg.cells.nucleus.NoRouteToCellException ){
                //
                // The SrmSpaceManager is not reachable.
                //
                setWaiting(false);
                displayErrorMessage("PnfsManager not present !" );

            }else if( obj instanceof String ){
                System.out.println(obj.toString());
                try{

                    PnfsManagerInfo pnfsManagerInfo = PnfsManagerInfo.decodePnfsManagerInfo( (String)obj ) ;

                    _counterDisplay.setPnfsManagerInfo( pnfsManagerInfo );
                    _detailPanel.setMap( pnfsManagerInfo._statisticsMap ) ;

                }catch(PnfsManagerDecodingException pee ){
                    pee.printStackTrace();
                    setWaiting(false);
                    displayErrorMessage("Problem in decoding message from PnfsManager at line "+pee.getLine() );
                }catch(Exception ee ){
                    ee.printStackTrace();
                    setWaiting(false);
                    displayErrorMessage("Problem in decoding message from PnfsManager" );
                }
            }
        }
    }
    private void askForPnfsInfo(){
        try{
            System.out.println("Asking for pnfs info");
            _connection.sendObject(
                    "PnfsManager" , "info" , this , REQ_PNFSMGR_INFO
            ) ;

        }catch(Exception e ){
            displayErrorMessage("Couldn't send query to server.\n"+e.getMessage() );
        }
    }
    private void displayErrorMessage(String errorMessage ){
        JOptionPane.showMessageDialog(
                PnfsManagerPanel.this, errorMessage ,
                "Server Problem",JOptionPane.ERROR_MESSAGE);
    }
    private void setWaiting( boolean waiting ){

    }
    public void actionPerformed( ActionEvent event ){

    }

    private class Controller
            extends JPanel
            implements ActionListener  {

        private JButton   _updateTree   = new JButton("Update") ;
        private JButton   _displayMode  = new JButton("Toggle Mode") ;
        private JComboBox _autoUpdate   = null ;
        private boolean   _weAreWaiting = false ;

        private Timer _autoUpdateTimer  = new Timer( 100 , this ) ;

        private class AutoObject {
            private String name ;
            private long   interval ;
            private AutoObject( String name , long interval ){
                this.name = name ; this.interval = interval ;
            }
            public String toString(){return name ; }
            public long getInterval(){ return interval ; }
        }
        private AutoObject [] _autoLabels = {
                new AutoObject("Manual",0L) ,
                new AutoObject("2 Sec",2L),
                new AutoObject("10 Sec",10L),
                new AutoObject("30 Sec",30L),
                new AutoObject("1 min" ,60L),
        };

        private Controller(){

            setLayout( new FlowLayout(FlowLayout.CENTER,10,10) ) ;
            setBorder( BorderFactory.createEtchedBorder( ) ) ;

            _autoUpdate = new JComboBox(_autoLabels);

            _autoUpdate.addActionListener(this) ;
            _updateTree.addActionListener(this);
            _displayMode.addActionListener(this);

            add( _autoUpdate ) ;
            add( _updateTree ) ;
            add( _displayMode ) ;


        }
        public void actionPerformed( ActionEvent event ){
            Object source = event.getSource() ;

            if( source == _updateTree ){

                askForPnfsInfo() ;

            }else if( source == _autoUpdate ){

                AutoObject auto = (AutoObject)_autoUpdate.getSelectedItem() ;
                if( auto == null )return ;
                if( auto.interval == 0 ){
                    _autoUpdateTimer.stop() ;
                    _updateTree.setEnabled(true);
                }else{
                    _autoUpdateTimer.stop() ;
                    _autoUpdateTimer.setDelay( (int) (auto.interval*1000L ) ) ;
                    _autoUpdateTimer.start() ;
                    _updateTree.setEnabled(false);
                }

            }else if( source == _displayMode ){

                _switchLayout.next( _switchPanel ) ;
                System.out.println("Switching ...");

            }else if( source == _autoUpdateTimer ){

                _updateTree.setEnabled(false);
                System.out.println("Ping : "+System.currentTimeMillis());
                askForPnfsInfo() ;

            }
        }
    }

}
