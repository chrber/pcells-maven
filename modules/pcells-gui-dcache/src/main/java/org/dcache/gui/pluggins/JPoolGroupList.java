// $Id: JPoolGroupList.java,v 1.6 2006/12/23 18:13:16 cvs Exp $
//
package org.dcache.gui.pluggins;
//
import dmg.cells.nucleus.NoRouteToCellException;
import org.pcells.services.connection.DomainConnection;
import org.pcells.services.connection.DomainConnectionListener;
import org.pcells.services.connection.DomainEventListener;
import org.pcells.services.gui.CellGuiSkinHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

public class      JPoolGroupList
        extends    CellGuiSkinHelper.CellPanel
        implements DomainConnectionListener,
        DomainEventListener,
        ActionListener ,
        ListSelectionListener {

    private Logger _logger;

    private DomainConnection _connection = null ;

    private JTitleList   _groupList    = null ;
    private JTitleList   _linkTList    = null ;
    private JButton      _updateButton = new CellGuiSkinHelper.CellButton("Update Groups"); ;
    private JButton      _toggleButton = new CellGuiSkinHelper.CellButton("Switch to Links"); ;
    private JProgressBar _progressBar  = null ;
    private boolean      _collectLinks = true ;
    private CardLayout   _cardLayout   = new CardLayout() ;
    private JPanel       _cardPanel    = new CellGuiSkinHelper.CellPanel( _cardLayout )  ;

    public JPoolGroupList( DomainConnection connection ){
        this( connection , true );
    }
    public JPoolGroupList( DomainConnection connection , boolean includeLinks ) {
        _logger = LoggerFactory.getLogger(JPoolGroupList.class);
        _connection = connection;
        _collectLinks = includeLinks;
        BorderLayout l = new BorderLayout();
        l.setVgap(10);
        l.setHgap(10);
        setLayout(l);

        _groupList = new JTitleList("Pool Groups");
        _groupList.setPrototypeCellValue("it-dcache23-0   ");
        _groupList.addListSelectionListener(this);

        _linkTList = new JTitleList("Links");
        _linkTList.setPrototypeCellValue("it-dcache23-0   ");
        _linkTList.addListSelectionListener(this);

        _cardPanel.add(_groupList, "group");
        _cardPanel.add(_linkTList, "link");

        _updateButton.addActionListener(this);
        _toggleButton.addActionListener(this);

        add(_cardPanel, "Center");
        if (_collectLinks) {
            JPanel south = new CellGuiSkinHelper.CellPanel(new GridLayout(2, 0));
            south.add(_updateButton);
            south.add(_toggleButton);
            add(south, "South");
        } else {
            add(_updateButton, "South");
        }
        connection.addDomainEventListener(this);
    }
    public void setProgressBar( JProgressBar progressBar ){
        _progressBar = progressBar ;
    }
    private boolean _isGroupList = true ;
    public void actionPerformed( ActionEvent event ){
        Object source = event.getSource() ;
        if( source == _toggleButton ){
            if( _isGroupList ){
                _cardLayout.show( _cardPanel , "link" ) ;
                _isGroupList = false ;
                _updateButton.setText("Update Links") ;
                _toggleButton.setText("Switch to Groups");
            }else{
                _cardLayout.show( _cardPanel , "group" ) ;
                _isGroupList = true ;
                _updateButton.setText("Update Groups") ;
                _toggleButton.setText("Switch to Links");
            }
        }else if( source == _updateButton ){
            collectData() ;
        }
    }
    private java.util.ArrayList _listeners = new ArrayList() ;
    public void addActionListener( ActionListener listener ){
        _listeners.add(listener) ;
    }
    private void fireAction(){
        Iterator i = _listeners.iterator() ;
        while( i.hasNext() ){
            ((ActionListener)i.next()).actionPerformed(
                    new ActionEvent(this,0,"ListChanged"));
        }
    }
    private static final int WAITING_FOR_POOL_GROUPS = 10 ;
    private static final int WAITING_FOR_POOLS       = 11 ;
    private static final int WAITING_FOR_ALL_POOLS   = 12 ;
    private static final int WAITING_FOR_LINK_LIST   = 13 ;
    private static final int WAITING_FOR_LINKS       = 14 ;

    private int  _collectionState = 0 ;
    private Map  _poolGroupHash   = null ;
    private Map  _linkHash        = null ;
    private java.util.List _poolGroups      = null ;
    private int  _selectedPool    = -1 ;

    private void collectData(){

        _updateButton.setEnabled(false);
        _toggleButton.setEnabled(false);
        _groupList.setEnabled(false);

        _collectionState = WAITING_FOR_POOL_GROUPS ;

        if( _progressBar != null ){
            _progressBar.setMinimum(0) ;
            _progressBar.setMaximum(1);
            _progressBar.setValue(0);
        }
        askForPoolGroups();
    }
    private void collectionDone(){
        _updateButton.setEnabled(true);
        _toggleButton.setEnabled(true);
        _groupList.setEnabled(true);
    }
    private void collectionFailed( Object obj ){
        _logger.debug("Problem in collection PoolInfos : "+obj ) ;

        if( obj instanceof NoRouteToCellException ){
            JOptionPane.showMessageDialog(
                    this ,
                    ((NoRouteToCellException)obj).getDestinationPath().getDestinationAddress().getCellName()+
                            " not found"  ,
                    "Destination Cell Not Found" ,
                    JOptionPane.ERROR_MESSAGE ) ;
        }else{
            JOptionPane.showMessageDialog( this , obj , "Illegal Return Value" , JOptionPane.ERROR_MESSAGE ) ;
        }
        collectionDone();
    }

    private void askForPoolsOfGroup( String poolGroup ){
        //_logger.debug("Asking for pools of "+poolGroup) ;
        try{
            _connection.sendObject( "PoolManager" ,
                    poolGroup.equals("*") ?
                            "psux ls pool" :
                            "psux ls pgroup "+poolGroup ,
                    this ,
                    0 ) ;
        }catch(Exception ee){
            collectionFailed("Exception : "+ee);
        }
    }
    private void askForPoolGroups(){
        //_logger.debug("Asking for all pool groups") ;
        try{
            _connection.sendObject( "PoolManager" ,
                    "psux ls pgroup" ,
                    this ,
                    0 ) ;
        }catch(Exception ee){
            collectionFailed("Exception : "+ee);
        }
    }
    private void askForLinkList(){
        _logger.debug("Asking for link list") ;
        try{
            _connection.sendObject( "PoolManager" ,
                    "psux ls link" ,
                    this ,
                    0 ) ;
        }catch(Exception ee){
            collectionFailed("Exception : "+ee);
        }
    }
    private void askForLink( String linkName ){
        _logger.debug("Asking for link list") ;
        try{
            _connection.sendObject( "PoolManager" ,
                    "psux ls link "+linkName ,
                    this ,
                    0 ) ;
        }catch(Exception ee){
            collectionFailed("Exception : "+ee);
        }
    }
    public void domainAnswerArrived( Object obj , int subid ){
        _logger.debug( "Answer ("+subid+") : "+obj.getClass().getName()+" "+obj.toString() ) ;
        if( _collectionState == WAITING_FOR_POOL_GROUPS ){

            if( ! ( obj instanceof Object [] ) ){ collectionFailed(obj) ; return ; }
            Object [] array = (Object [])obj ;

//	 _logger.debug(""+array.length+" Pool Groups arrived" ) ;
            if( _progressBar != null )_progressBar.setMaximum(array.length);

            _poolGroups = new ArrayList() ;

            for( int i= 0 , n = array.length ; i < n ; i++ )
                _poolGroups.add( array[i].toString() ) ;

            _selectedPool    = -1 ;
            _collectionState = WAITING_FOR_ALL_POOLS ;

            askForPoolsOfGroup("*") ;

        }else if( _collectionState == WAITING_FOR_ALL_POOLS ){

            if( ! ( obj instanceof Object [] ) ){ collectionFailed(obj) ; return ; }

            Object [] array = (Object [])obj ;
            //_logger.debug(""+array.length+" Pools arrived of *" ) ;

            ArrayList list  = new ArrayList() ;

            for(int i=0,n=array.length;i<n;i++)list.add(array[i].toString());

            _poolGroupHash = new HashMap() ;
            _poolGroupHash.put("*",list);

            if( _poolGroups.size() == 0 ){
                collectionDone() ;
                return ;
            }

            _selectedPool = 0 ;
            _collectionState = WAITING_FOR_POOLS ;

            askForPoolsOfGroup( _poolGroups.get(_selectedPool).toString() ) ;

        }else if( _collectionState == WAITING_FOR_POOLS ){

            if( ! ( obj instanceof Object [] ) ){ collectionFailed(obj) ; return ; }
            Object [] array = (Object [])obj ;

            if( array.length < 3 ){ collectionFailed("Not enough elements in array");return ;}

            String expectedPoolGroup = (String)_poolGroups.get(_selectedPool);

            if( ! array[0].toString().equals(expectedPoolGroup) ){
                collectionFailed("Unexpected poolgroup arrived "+
                        array[0].toString()+" (should be "+array[0]+")");
                return ;
            }
            array = (Object [])array[1] ;
            //_logger.debug(""+array.length+" Pools arrived of "+expectedPoolGroup ) ;
            ArrayList list  = new ArrayList() ;

            for(int i=0,n=array.length;i<n;i++)list.add(array[i].toString());

            _poolGroupHash.put(expectedPoolGroup,list);

            _selectedPool++ ;
            if( _progressBar != null )_progressBar.setValue(_selectedPool);

            if( _selectedPool >= _poolGroups.size() ){

                Set set = _groupList.set() ;
                _groupList.clear();
                set.addAll( _poolGroups ) ;
                _groupList.apply() ;

                if( _collectLinks ){
                    _logger.debug("JPoolGroupList : switching to link, ask for link list");
                    _collectionState = WAITING_FOR_LINK_LIST ;
                    askForLinkList() ;
                }else{
                    collectionDone() ;
                }
                return ;
            }

            askForPoolsOfGroup( _poolGroups.get(_selectedPool).toString() ) ;
        }else if( _collectionState == WAITING_FOR_LINK_LIST ){
            if( ! ( obj instanceof Object [] ) ){ collectionFailed(obj) ; return ; }
            Object [] array = (Object [])obj ;
            if( _progressBar != null )_progressBar.setMaximum(array.length);
            ArrayList list  = new ArrayList() ;
            for(int i=0,n=array.length;i<n;i++)list.add( new LinkEntry(array[i].toString()));
            _logger.debug("Link list arrived : "+list) ;
            if( list.size() == 0 ){
                collectionDone() ;
                return ;
            }
            _linkList = list ;
            _linkListPosition = 0 ;
            askForLink( _linkList.get(_linkListPosition).toString() ) ;
            _collectionState = WAITING_FOR_LINKS ;
            return ;
        }else if( _collectionState == WAITING_FOR_LINKS ){
            if( ! ( obj instanceof Object [] ) ){ collectionFailed(obj) ; return ; }
            Object [] res = (Object  [] )obj ;
            if( res.length < 7 ){
                _logger.error("Not enought slots in reply on 'psux ls link <link>'");
                collectionFailed(obj) ;
                return ;
            }

            LinkEntry entry = (LinkEntry)_linkList.get(_linkListPosition) ;
            entry.readPref  = ((Integer)res[1]).intValue() ;
            entry.cachePref = ((Integer)res[2]).intValue() ;
            entry.writePref = ((Integer)res[3]).intValue() ;
            entry.pools      = res[5] == null ? new String[0] : (Object [])res[5] ;
            entry.poolGroups = res[6] == null ? new String[0] : (Object [])res[6] ;
            _logger.debug("Got link entry : "+entry ) ;
            _linkListPosition ++ ;
            if( _progressBar != null )_progressBar.setValue(_linkListPosition);
            if( _linkListPosition >= _linkList.size() ){
                finishLinkCollection() ;
                collectionDone() ;

                return ;
            }
            askForLink( _linkList.get(_linkListPosition).toString() ) ;
        }
    }
    private void finishLinkCollection(){
        _linkHash = new HashMap() ;
        Set set = _linkTList.set() ;
        _linkTList.clear();
        for( Iterator it = _linkList.iterator() ; it.hasNext() ; ){
            LinkEntry entry = (LinkEntry)it.next() ;
            set.add( entry.name ) ;
            ArrayList pools = new ArrayList() ;
            if( entry.pools != null )
                for( int i = 0 ; i < entry.pools.length ; i++ )
                    pools.add(entry.pools[i]);
            if( entry.poolGroups != null )
                for( int i = 0 ; i < entry.poolGroups.length ; i++ ){
                    String poolGroupName = entry.poolGroups[i].toString() ;
                    java.util.List list = (java.util.List)_poolGroupHash.get(poolGroupName);
                    if( list != null )pools.addAll(list);
                }
            _linkHash.put( entry.name , pools ) ;
        }
        _linkTList.apply() ;
    }
    private class LinkEntry {
        private String name = null ;
        private int readPref = 0 , writePref = 0 , cachePref = 0 ;
        private Object [] pools  = null ;
        private Object [] poolGroups = null ;
        private String [] resolved   = null ;
        private LinkEntry( String name ){ this.name = name ; }
        public String toString(){
            return name ;
           /*
           if( pools == null )return name ;
           return name+"("+pools.length+","+poolGroups.length+")";
           */
        }
    }
    private int       _linkListPosition = -1 ;
    private ArrayList _linkList       = null ;
    private Set       _poolSet        = null ;
    private String [] _poolGroupNames = null ;

    public synchronized Set getPoolSet(){
        return _poolSet ;
    }
    public synchronized String [] getPoolGroupNames(){ return _poolGroupNames ; }
    public void valueChanged( ListSelectionEvent event ){
        if( event.getValueIsAdjusting() == true )return;

        JList jlist = (JList)event.getSource() ;
        Object [] selected = jlist.getSelectedValues() ;

        HashSet set = new HashSet() ;
        java.util.List groupNames = new java.util.ArrayList() ;
        for( int i = 0 , n = selected.length ; i<n ;i++){
            String poolGroup = selected[i].toString();
            groupNames.add( poolGroup ) ;
            java.util.List list =
                    (java.util.List)( jlist == _groupList.getJList() ?
                            _poolGroupHash.get(poolGroup) :
                            _linkHash.get(poolGroup) ) ;
            if( list == null )continue ;

            set.addAll(list);

        }
        synchronized(this){
            _poolSet = set ;
            _poolGroupNames = (String [])groupNames.toArray( new String[0] ) ;
        }
        //_logger.debug("Selected pools : "+set ) ;
        fireAction() ;
    }

    public void connectionOpened( DomainConnection connection ){
        _logger.debug("Connection opened");
        //collectData();
    }
    public void connectionClosed( DomainConnection connection ){
        _logger.debug("Connection closed" ) ;
    }
    public void connectionOutOfBand( DomainConnection connection, Object obj ){
        _logger.debug("Connection connectionOutOfBand "+obj ) ;
    }


}
