// $Id: JCostPanel.java,v 1.3 2008/08/04 19:02:57 cvs Exp $
//
package org.dcache.gui.pluggins.costs ;
//

import diskCacheV111.pools.PoolCostInfo;
import diskCacheV111.vehicles.CostModulePoolInfoTable;
import org.pcells.services.connection.DomainConnection;
import org.pcells.services.connection.DomainConnectionListener;
import org.pcells.services.connection.DomainEventListener;
import org.pcells.services.gui.LoadedPicturePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.dcache.gui.pluggins.pools.PoolGroupLinkCollector;

public class      JCostPanel
        extends    JPanel
        implements DomainConnectionListener,
        DomainEventListener{

    private Logger _logger;

    private DomainConnection _connection = null ;

    private Controller   _controller  = null ;
    private final String _sheepImage  =  "/images/sheep004.png" ;
    private CardLayout   _cardSwitch  = new CardLayout() ;
    private JPanel       _switchPanel = new JPanel( _cardSwitch ) ;

    private SectionPanelGroup  _sectionPanelGroup  = new SectionPanelGroup() ;

    private LoadedPicturePanel  _waitingImage  = new LoadedPicturePanel( _sheepImage , 200 ) ;

    private static final int COLLECT_TREE_INFO = 10 ;
    private static final int COLLECT_COST_INFO = 11 ;
    private int _collectInfo = 0 ;
    public JCostPanel( org.pcells.services.connection.DomainConnection connection,
                       java.util.prefs.Preferences prefs ){
        this(connection);
    }
    public JCostPanel( DomainConnection connection ){

        _logger = LoggerFactory.getLogger(JCostPanel.class);

        _connection = connection ;

        BorderLayout l = new BorderLayout(10,10) ;
        setLayout(l) ;

        setBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder( Color.blue , 1 ) ,
                        "Cost Module" ) ) ;

        connection.addDomainEventListener(this);


        _controller  = new Controller() ;

        _switchPanel.add( _sectionPanelGroup   , "main" ) ;
        _switchPanel.add( _waitingImage , "waiting" ) ;

        _sectionPanelGroup.addChangeListener(_controller);

        add( _switchPanel , "Center") ;
        add( _controller  , "North" ) ;

    }
    private class Controller
            extends JPanel
            implements ActionListener, ChangeListener  {

        private JButton _updateTree    = new JButton("Update Tree") ;
        private JButton _updateCost    = new JButton("Update Cost") ;
        private JCheckBox _costByMover  = new JCheckBox("Use simple mover count instead of cost");
        private JCheckBox _costDetails  = new JCheckBox("Cost Details");
        private JComboBox _autoUpdate   = null ;
        private boolean   _weAreWaiting = false ;

        private javax.swing.Timer _timer            = new javax.swing.Timer( 100 , this ) ;
        private javax.swing.Timer _autoUpdateTimer  = new javax.swing.Timer( 100 , this ) ;

        private PoolGroupLinkCollector _treeCollector = null ;

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
                new AutoObject("2 min" ,120L),
                new AutoObject("5 min" ,300L)
        };
        public boolean isCostByMover(){
            return _costByMover.isSelected() ;
        }
        public boolean isCostDetails(){
            return _costDetails.isSelected() ;
        }
        private Controller(){

            setLayout( new FlowLayout(FlowLayout.CENTER,10,10) ) ;
            setBorder( BorderFactory.createEtchedBorder( ) ) ;

            _autoUpdate = new JComboBox(_autoLabels);

            _updateTree.addActionListener(this);
            _updateCost.addActionListener(this);
            _costByMover.addActionListener(this);
            _costDetails.addActionListener(this);
            _autoUpdate.addActionListener(this) ;

            add( _updateTree ) ;
            add( _updateCost ) ;
            // TEMP add( _costByMover ) ;
            // TEMP add( _costDetails ) ;
            add( _autoUpdate ) ;

            _treeCollector = new PoolGroupLinkCollector(_connection,true,true,true);
            _treeCollector.addActionListener(this);


        }
        public void stateChanged( ChangeEvent event ){

            Object source = event.getSource() ;
            if( source == _sectionPanelGroup ){
                //redrawCurrentSection();
            }
        }
        public void actionPerformed( ActionEvent event ){
            Object source = event.getSource() ;
            if( source == _updateTree ){

                _collectInfo = COLLECT_TREE_INFO ;
                try{
                    _treeCollector.collectData() ;
                }catch(IllegalStateException ise ){
                    _logger.error("Could start collector due to : "+ise);
                    return ;
                }
                setWaiting(true,"Waiting for Link(Group) Tree");

            }else if( source == _updateCost ){

                _collectInfo = COLLECT_COST_INFO ;
                try{
                    _treeCollector.collectPoolCost() ;
                }catch(IllegalStateException ise ){
                    _logger.error( "Could start collector due to : "+ise);
                    return ;
                }
                _updateCost.setEnabled(false);
                _updateTree.setEnabled(false);
                //setWaiting(true,"Waiting for Pool Cost");

            }else if( source == _treeCollector ){

                setWaiting(false);

                PoolGroupLinkCollector.DataArrivedEvent data =
                        (PoolGroupLinkCollector.DataArrivedEvent)event ;

                if( data.isError() ){

                    displayErrorMessage( data.getErrorObject().toString() ) ;

                }else if(  _collectInfo == COLLECT_COST_INFO ){

                    _sectionPanelGroup.setCostMap( getCostMap() ) ;

                }else if(  _collectInfo == COLLECT_TREE_INFO ){
                    //
                    // now build the SectionPanelGroup
                    //
                    reloadSectionPanelGroup() ;

                }
            }else if( source == _costByMover ){

                _sectionPanelGroup.setCostByMover( _costByMover.isSelected() ) ;

            }else if( source == _costDetails ){

                _sectionPanelGroup.setCostDetails( _costDetails.isSelected() ) ;

            }else if( source == _autoUpdate ){
                //_logger.debug("Selected : "+_autoUpdate.getSelectedItem().getClass().getName());
                AutoObject auto = (AutoObject)_autoUpdate.getSelectedItem() ;
                if( auto == null )return ;
                if( auto.interval == 0 ){
                    _autoUpdateTimer.stop() ;
                    _updateCost.setEnabled(true);
                    _updateTree.setEnabled(true);
                }else{
                    _autoUpdateTimer.stop() ;
                    _autoUpdateTimer.start() ;
                    _autoUpdateTimer.setDelay( (int) (auto.interval*1000L ) ) ;
                    _updateCost.setEnabled(false);
                    _updateTree.setEnabled(false);
                }
            }else if( source == _timer ){
                if( _weAreWaiting )_waitingImage.setProgress(_treeCollector.getProgress());
            }else if( source == _autoUpdateTimer ){
                _collectInfo = COLLECT_COST_INFO ;
                try{
                    _treeCollector.collectPoolCost() ;
                }catch(IllegalStateException ise ){
                    _logger.error("Could start collector due to : "+ise);
                    return ;
                }
                _updateCost.setEnabled(false);
                _updateTree.setEnabled(false);
            }
        }

        /**
         * Fills the _costMap
         */
        private Map<String,PoolCostInfo>  getCostMap(){

            CostModulePoolInfoTable table = _treeCollector.getCostModulePoolInfoTable() ;
            if( table == null ){
                _logger.error("Problem in publishing cost : cost table not yet present");
                return null ;
            }

            Map<String,PoolCostInfo> map = new HashMap<String,PoolCostInfo>() ;

            for( PoolCostInfo info : table.poolInfos() ){
                map.put( info.getPoolName() , info ) ;
            }

            return map ;
        }
        /**
         * Creates the Section Panels from the result of the 'psu' commands.
         */
        private void reloadSectionPanelGroup(){

            Map map        = _treeCollector.getLinkMap() ;

            Map<String,ArrayList<PoolGroupLinkCollector.LinkEntry>> sectionMap =
                    new HashMap<String,ArrayList<PoolGroupLinkCollector.LinkEntry>>() ;

            //
            // go though all links and groups them by 'section'.
            //
            //  map( sectionName , ArrayOfLinkEntries )
            //
            for( Iterator it = map.values().iterator() ; it.hasNext() ; ){

                PoolGroupLinkCollector.LinkEntry entry =
                        (PoolGroupLinkCollector.LinkEntry)it.next();

                String linkSection = entry.getSection() == null ? "Default" : entry.getSection();

                ArrayList<PoolGroupLinkCollector.LinkEntry> list = sectionMap.get(linkSection) ;

                if( list == null ){

                    sectionMap.put( linkSection,
                            list = new ArrayList<PoolGroupLinkCollector.LinkEntry>()) ;

                }
                list.add(entry) ;

            }
            _sectionPanelGroup.removeAll();

            HashSet<String> allLinkedPools = new HashSet<String>() ;

            for( Iterator it = sectionMap.entrySet().iterator() ; it.hasNext() ; ){

                Map.Entry    entry = (Map.Entry)it.next() ;
                String sectionName = (String)entry.getKey() ;
                ArrayList linkList = (ArrayList)entry.getValue() ;
                HashSet<String> poolList = new HashSet<String>() ;

                String [] pools = null ;
                PoolGroupLinkCollector.LinkEntry linkEntry = null ;

                for( Iterator itt = linkList.iterator(); itt.hasNext() ; ){

                    linkEntry = (PoolGroupLinkCollector.LinkEntry)itt.next() ;

                    if( ( pools = linkEntry.getResolvedPools() ) == null )continue ;

                    for( int i= 0 ; i < pools.length ; i++ ){
                        //
                        // add to pool list of this selection.
                        //
                        poolList.add(pools[i]) ;
                        //
                        // add to pool set of 'all' linked pools.
                        //
                        allLinkedPools.add(pools[i]);
                    }
                }
                _logger.debug(" section : "+sectionName+" "+poolList);
                //
                // create new section panel and add to section panel group.
                //
                _sectionPanelGroup.addTab( sectionName , poolList );
            }
            //
            // shouldn't but may happen.
            //
            Map<String,PoolCostInfo> costMap = getCostMap() ;

            if( costMap == null ){
                _logger.error("No costs available");
                return ;
            }
            //
            // get all (running) pools from the cost matrix.
            //
            HashSet<String> tmpSet = new HashSet<String>( costMap.keySet() ) ;
            _sectionPanelGroup.addTab( "All" , tmpSet );
            //
            // remove the linked ones. The rest is unassigned.
            //
            tmpSet.removeAll( allLinkedPools ) ;
            _sectionPanelGroup.addTab( "Unlinked" , tmpSet );

            _sectionPanelGroup.setCostMap( costMap ) ;

            _sectionPanelGroup.allTabsDone() ;


        }
        private void setWaiting(boolean waiting  ){
            setWaiting( waiting , null ) ;
        }
        private void setWaiting(boolean waiting , String message ){
            if( message != null )_waitingImage.setMessage(message);
            _weAreWaiting = waiting ;
            if( waiting ){
                _timer.start() ;
                _cardSwitch.show( _switchPanel   , "waiting" ) ;
            }else{
                _timer.start() ;
                _cardSwitch.show( _switchPanel   , "main" ) ;
            }
            _updateTree.setEnabled(!waiting);
            _updateCost.setEnabled(!waiting);

        }

    }
    private void displayErrorMessage(String errorMessage ){
        JOptionPane.showMessageDialog(
                JCostPanel.this, errorMessage ,
                "Server Problem",JOptionPane.ERROR_MESSAGE);
    }
    public void connectionOpened( DomainConnection connection ){
        _logger.debug("Connection opened");
    }
    public void connectionClosed( DomainConnection connection ){
        _logger.debug("Connection closed" ) ;
    }
    public void connectionOutOfBand( DomainConnection connection, Object obj ){
        _logger.debug("Connection connectionOutOfBand "+obj ) ;
    }
    public void domainAnswerArrived( Object obj , int subid ){
        _logger.debug( "Answer ("+subid+") : "+obj.toString() ) ;
    }


}
