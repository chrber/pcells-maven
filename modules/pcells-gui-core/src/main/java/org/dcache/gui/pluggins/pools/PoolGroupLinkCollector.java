// $Id: PoolGroupLinkCollector.java,v 1.3 2008/08/04 19:57:51 cvs Exp $
//
package org.dcache.gui.pluggins.pools;
//
import diskCacheV111.vehicles.CostModulePoolInfoTable;
import org.pcells.services.connection.DomainConnection;
import org.pcells.services.connection.DomainConnectionListener;
import org.pcells.services.connection.DomainEventListener;
import org.pcells.services.gui.ActionEventDistributer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

public class      PoolGroupLinkCollector
        implements DomainConnectionListener,
        DomainEventListener {

    private Logger _logger;

    private DomainConnection _connection = null ;

    private boolean _collectLinks      = true ;
    private boolean _collectLinkGroups = true ;
    private boolean _collectPoolCost   = false ;
    private boolean _poolCostOnly      = false ;
    private boolean _isGroupList       = true ;
    private boolean _debug             = true ;

    private static final int WAITING_FOR_POOL_GROUPS = 10 ;
    private static final int WAITING_FOR_POOLS       = 11 ;
    private static final int WAITING_FOR_ALL_POOLS   = 12 ;
    private static final int WAITING_FOR_LINK_LIST   = 13 ;
    private static final int WAITING_FOR_LINKS       = 14 ;
    private static final int WAITING_FOR_LINK_GROUPS = 15 ;
    private static final int WAITING_FOR_POOL_COST   = 16 ;

    private int  _collectionState = 0 ;
    private Map  _poolGroupHash   = new HashMap() ;
    private Map  _linkHash        = new HashMap() ;
    private Map  _linkGroupHash   = new HashMap() ;
    private int  _selectedPool    = -1 ;
    private int  _linkListPosition = -1 ;
    private Map  _outPoolGroupMap  = new HashMap();
    private ArrayList              _linkList   = null ;
    private List         _poolGroups = null ;
    private ActionEventDistributer _listeners  = new ActionEventDistributer() ;
    private CostModulePoolInfoTable _costTable = null ;
    public class LinkEntry {
        private String name = null ;
        private int readPref = 0 , writePref = 0 , cachePref = 0 , p2pPref = 0 ;
        private Object [] pools  = null ;
        private Object [] poolGroups = null ;
        private String [] resolved   = null ;
        private String    linkGroup  = null ;
        private String    section    = null ;

        private LinkEntry( String name ){ this.name = name ; }
        public String toString(){
            return name ;
        }
        public String getName(){ return name ; }
        public Object [] getPools(){ return pools ; }
        public Object [] getPoolGroups(){ return poolGroups ; }
        public String [] getResolvedPools(){ return resolved ;}
        public String    getLinkGroup(){ return linkGroup ; }
        public String getSection(){ return section ; }

    }
    public PoolGroupLinkCollector( DomainConnection connection ){
        this( connection , true , false );
    }
    public PoolGroupLinkCollector( DomainConnection connection , boolean includeLinks ){
        this( connection , includeLinks , false );
    }
    public PoolGroupLinkCollector( DomainConnection connection ,
                                   boolean includeLinks ,
                                   boolean includeLinkGroups){
        this( connection , includeLinks , includeLinkGroups , false ) ;
    }
    public PoolGroupLinkCollector( DomainConnection connection ,
                                   boolean includeLinks ,
                                   boolean includeLinkGroups,
                                   boolean includePoolCost ) {
        _logger = LoggerFactory.getLogger(PoolGroupLinkCollector.class);
        _connection = connection;
        _collectLinks = includeLinks;
        _collectLinkGroups = includeLinkGroups;
        _collectPoolCost = includePoolCost;

        connection.addDomainEventListener(this);
    }
    public void addActionListener( ActionListener listener ){
        _listeners.addActionListener( listener ) ;
    }
    public class DataArrivedEvent extends ActionEvent {
        private Object _errorObject = null ;
        private boolean _isError    = false ;
        private DataArrivedEvent( Object source ){
            this( source , null ) ;
        }
        private DataArrivedEvent( Object source , Object errorObject ){
            super( source , 10000 , "dataArrived" ) ;
            _errorObject = errorObject ;
            _isError     = errorObject != null ;
        }
        public boolean isError(){ return _isError ; }
        public Object getErrorObject(){ return _errorObject ; }
        public String toString(){
            return "DataArrivedEvent : "+( _isError ? _errorObject.toString() : "" ) ;
        }
    }
    private void fireAction(){
        _listeners.fireEvent( new DataArrivedEvent(this) ) ;
    }
    private void fireAction( Object errorObject ){
        _listeners.fireEvent( new DataArrivedEvent(this,errorObject) ) ;
    }

    public synchronized void collectPoolCost() throws IllegalStateException{

        if( _collectionState != 0 )
            throw new
                    IllegalStateException("Scan still running");

        _poolCostOnly = true ;

        progressReset() ;

        _collectionState = WAITING_FOR_POOL_COST ;

        askForPoolCost() ;
    }
    public synchronized void collectData() throws IllegalStateException{

        if( _collectionState != 0 )
            throw new
                    IllegalStateException("Scan still running");

        _poolCostOnly = false;

        if( _collectPoolCost ){

            progressReset() ;

            _collectionState = WAITING_FOR_POOL_COST ;

            askForPoolCost();

        }else if( _collectLinkGroups ){

            progressReset() ;

            _collectionState = WAITING_FOR_LINK_GROUPS ;

            askForLinkGroups();

        }else{

            progressReset() ;

            _collectionState = WAITING_FOR_POOL_GROUPS ;

            askForPoolGroups();

        }
    }
    private double _progressCurrent  = 0.0 ;
    private int    _progressSequence = -1 ;
    private int    _progressMax      = 0 ;
    private void progressReset(){
        _progressSequence = -1 ;
    }
    private void progressMax( int maximum ){
        _progressSequence ++ ;
        _progressMax = maximum ;
    }
    private void progress( int current ) {
        _progressCurrent = (double)
                (  0.5 * (float)_progressSequence +
                        (float)current / (float)_progressMax * 0.5 ) ;
    }
    public double getProgress(){ return _progressCurrent ; }
    private synchronized void collectionDone(){

        _collectionState = 0 ;
        _outPoolGroupMap = new HashMap( _poolGroupHash ) ;
        fireAction();
    }
    private void collectionFailed( Object obj ){
        _collectionState = 0 ;
        _logger.error("Problem in collection PoolInfos : "+obj ) ;
        fireAction(obj);
    }

    private void askForPoolCost(){
        if(_debug)_logger.debug("Asking for pool cost") ;
        try{
            _connection.sendObject( "PoolManager" ,
                    "xcm ls -l" ,
                    this ,
                    0 ) ;
        }catch(Exception ee){
            collectionFailed("Exception : "+ee);
        }
    }
    private void askForLinkGroups(){
        _logger.debug("Asking for link groups") ;
        try{
            _connection.sendObject( "PoolManager" ,
                    "psu ls -l linkGroup" ,
                    this ,
                    0 ) ;
        }catch(Exception ee){
            collectionFailed("Exception : "+ee);
        }
    }
    private void askForPoolsOfGroup( String poolGroup ){
        _logger.debug("Asking for pools of "+poolGroup) ;
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
        _logger.debug("Asking for all pool groups") ;
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
    private Map scanLinkGroupOutput(String in ) throws IllegalArgumentException {

        StringTokenizer st  = new StringTokenizer( in , "\n" ) ;
        HashMap         map = new HashMap() ;

        while( st.hasMoreTokens() ){
            String line = st.nextToken() ;

            if( ( line.length() < 2 ) || ( line.charAt(0) == ' ' ) )continue ;

            StringTokenizer sti = new StringTokenizer( line ) ;

            String    linkGroupName = null ;
            ArrayList list          = new ArrayList() ;

            for( int i = 0 ; sti.hasMoreTokens() ; i++ ){
                String str = sti.nextToken() ;
                if( str.equals("]") )break ;
                if(_debug)_logger.debug("Token : >"+str+"<");
                switch(i){
                    case 0 : linkGroupName = str ; break ;
                    case 1 :
                        if( ( str.length() != 1 ) || ( ! str.equals(":") ) )
                            throw new IllegalArgumentException("Syntax error : no ':' in linkGroup def") ;
                        break ;
                    case 2 :
                        if( ( str.length() > 0 ) && ( str.equals("[EMPTY]") ) )break;
                        if( ( str.length() != 1 ) || ( ! str.equals("[") ) )
                            throw new IllegalArgumentException("Syntax error : no '[' in linkGroup def") ;
                        break ;
                    default :
                        list.add(str) ;

                }
            }
            map.put( linkGroupName , list ) ;
        }
        return map ;
    }
    public void domainAnswerArrived( Object obj , int subid ){

        _logger.debug( "Answer ("+subid+") : "+obj.getClass().getName()+" "+obj.toString() ) ;

        if( _collectionState == WAITING_FOR_POOL_COST ){

            if( ! ( obj instanceof CostModulePoolInfoTable ) ){ collectionFailed(obj) ; return ; }

            try{

                _costTable = (CostModulePoolInfoTable) obj  ;
                _logger.debug("PoolGroupLinkCollector CostModulePoolInfoTable : "+_costTable);
            }catch(Exception ee ){
                ee.printStackTrace();
                collectionFailed(ee.getMessage()) ;
                return ;
            }

            if( _poolCostOnly ){
                collectionDone();
                return ;
            }
            progressReset() ;

            _collectionState = WAITING_FOR_LINK_GROUPS ;

            askForLinkGroups();


        }else if( _collectionState == WAITING_FOR_LINK_GROUPS ){

            if( ! ( obj instanceof String ) ){ collectionFailed(obj) ; return ; }

            try{

                _linkGroupHash = scanLinkGroupOutput( (String) obj ) ;
                _logger.debug("PoolGroupLinkCollector LinkGroupMap : "+_linkGroupHash);
            }catch(Exception ee ){
                ee.printStackTrace();
                collectionFailed(ee.getMessage()) ;
                return ;
            }

            progressReset() ;

            _collectionState = WAITING_FOR_POOL_GROUPS ;

            askForPoolGroups();


        }else if( _collectionState == WAITING_FOR_POOL_GROUPS ){

            if( ! ( obj instanceof Object [] ) ){ collectionFailed(obj) ; return ; }
            Object [] array = (Object [])obj ;

            _logger.debug(""+array.length+" Pool Groups arrived" ) ;
            progressMax(array.length);

            _poolGroups = new ArrayList() ;

            for( int i= 0 , n = array.length ; i < n ; i++ )
                _poolGroups.add( array[i].toString() ) ;

            _selectedPool    = -1 ;
            _collectionState = WAITING_FOR_ALL_POOLS ;

            askForPoolsOfGroup("*") ;

        }else if( _collectionState == WAITING_FOR_ALL_POOLS ){

            if( ! ( obj instanceof Object [] ) ){ collectionFailed(obj) ; return ; }

            Object [] array = (Object [])obj ;
            _logger.debug(""+array.length+" Pools arrived of *" ) ;

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
                        array[0].toString()+" (should be "+expectedPoolGroup+")");
                return ;
            }
            array = (Object [])array[1] ;
            _logger.debug(""+array.length+" Pools arrived of "+expectedPoolGroup ) ;
            ArrayList list  = new ArrayList() ;

            for(int i=0,n=array.length;i<n;i++)list.add(array[i].toString());

            _poolGroupHash.put(expectedPoolGroup,list);

            _selectedPool++ ;

            progress(_selectedPool);

            if( _selectedPool >= _poolGroups.size() ){

                //set.addAll( _poolGroups ) ;

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

            progressMax(array.length);

            ArrayList list  = new ArrayList() ;

            for(int i=0,n=array.length;i<n;i++)list.add( new LinkEntry(array[i].toString()));

            _logger.debug("Link list arrived : "+list) ;

            if( list.size() == 0 ){
                collectionDone() ;
                return ;
            }

            _linkList         = list ;
            _linkListPosition = 0 ;

            askForLink( _linkList.get(_linkListPosition).toString() ) ;

            _collectionState = WAITING_FOR_LINKS ;

            return ;

        }else if( _collectionState == WAITING_FOR_LINKS ){

            if( ! ( obj instanceof Object [] ) ){ collectionFailed(obj) ; return ; }

            Object [] res = (Object  [] )obj ;

            if( res.length < 9 ){
                _logger.error("Not enought slots in reply on 'psux ls link <link>'");
                collectionFailed(obj) ;
                return ;
            }

            LinkEntry entry = (LinkEntry)_linkList.get(_linkListPosition) ;
            entry.readPref  = ((Integer)res[1]).intValue() ;
            entry.cachePref = ((Integer)res[2]).intValue() ;
            entry.writePref = ((Integer)res[3]).intValue() ;
            entry.p2pPref   = ((Integer)res[7]).intValue() ;
            entry.pools      = res[5] == null ? new String[0] : (Object [])res[5] ;
            entry.poolGroups = res[6] == null ? new String[0] : (Object [])res[6] ;
            entry.section    = (String)res[8] ;
            _logger.debug("Got link entry : "+entry ) ;

            _linkListPosition ++ ;
            progress(_linkListPosition);
            if( _linkListPosition >= _linkList.size() ){
                finishLinkCollection() ;
                collectionDone() ;

                return ;
            }
            askForLink( _linkList.get(_linkListPosition).toString() ) ;
        }
    }
    private synchronized void finishLinkCollection(){

        _linkHash = new HashMap() ;

        for( Iterator it = _linkList.iterator() ; it.hasNext() ; ){

            LinkEntry entry = (LinkEntry)it.next() ;
            //_logger.debug("Link name : "+entry.name);
            ArrayList pools = new ArrayList() ;

            for( int i = 0 ; i < entry.pools.length ; i++ ){
                //_logger.debug("    "+entry.pools[i]);
                pools.add(entry.pools[i]);
            }
            for( int i = 0 ; i < entry.poolGroups.length ; i++ ){
                String poolGroupName = entry.poolGroups[i].toString() ;
                List list = (List)_poolGroupHash.get(poolGroupName);
                if( list != null )pools.addAll(list);
                //_logger.debug("        group : "+poolGroupName+" "+list);
            }
            //_logger.debug("       resolved : "+pools);
            entry.resolved = (String[])pools.toArray( new String[pools.size()] ) ;
            _linkHash.put( entry.name , entry ) ;
        }
    }
    public synchronized Map getPoolGroupMap() {
        return _outPoolGroupMap ;
    }
    public synchronized Map getLinkMap() {
        return _linkHash ;
    }
    public synchronized Map getLinkGroupMap(){
        return _linkGroupHash;
    }
    public synchronized CostModulePoolInfoTable getCostModulePoolInfoTable(){
        return _costTable ;
    }
    public void connectionOpened( DomainConnection connection ){
        _logger.debug("PoolGroupLinkCollector : Connection opened");
        //collectData();
    }
    public void connectionClosed( DomainConnection connection ){
        _logger.debug("PoolGroupLinkCollector : Connection closed" ) ;
    }
    public void connectionOutOfBand( DomainConnection connection, Object obj ){
        _logger.debug("PoolGroupLinkCollector : Connection connectionOutOfBand "+obj ) ;
    }


}
