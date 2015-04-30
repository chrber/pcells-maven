// $Id: SrmSpaceManagerPanel.java,v 1.5 2008/07/08 15:48:22 cvs Exp $
//
package org.dcache.gui.pluggins.poolManager ;
//

import diskCacheV111.pools.PoolCostInfo;
import diskCacheV111.util.CacheException;
import diskCacheV111.vehicles.CostModulePoolInfoTable;
import org.pcells.services.connection.DomainConnection;
import org.pcells.services.connection.DomainConnectionListener;
import org.pcells.services.gui.CellGuiSkinHelper;
import org.pcells.services.gui.EasyCommander;
import org.pcells.services.gui.LoadedPicturePanel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

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
import java.util.Set;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;

import org.dcache.gui.pluggins.pools.PoolGroupLinkCollector;


public class      SrmSpaceManagerPanel
       extends    CellGuiSkinHelper.CellPanel 
       implements ActionListener{
                  
   private DomainConnection _connection      = null ;
   private Preferences      _preferences     = null ;
   private Object           _infoLock        = new Object() ;
   private java.util.List   _reservationList = null ;
   private java.util.Map    _linkGroupMap    = null ;
   private java.util.Map    _linkGroupMapByName = null ;
   private CardLayout       _switchLayout    = null ;
   private JPanel           _switchPanel     = null ;
   private final String     _titlePrefix     = "Space Reservations" ;
   private EasyCommander    _commander       = null ;
   private LoadedPicturePanel _waitingImage  = new LoadedPicturePanel( "/images/sheep004.png" , 200 ) ;
   
   private SpaceLinkGroupTable   _spaceLinkGroupTable   = null ;
   private SpaceReservationPanel _spaceReservationPanel = null ;
   
   private boolean _debug = true ;
   
   public SrmSpaceManagerPanel( DomainConnection connection , Preferences preferences ){
   
      _connection  = connection ;
      _preferences = preferences ;
      
      BorderLayout l = new BorderLayout(10,10) ;
      setLayout(l) ;
      
         setBorder( BorderFactory.createTitledBorder( 
	            BorderFactory.createLineBorder( Color.blue , 2 ) , 
		    "Srm Space Manager" ) ) ;
      //setBorder( new CellGuiSkinHelper.CellBorder("Srm Space Manager" , 25 ) ) ;
 
      add( new ControlPanel( connection , preferences )  , "North" ) ;
      
      _spaceLinkGroupTable   = new SpaceLinkGroupTable( _connection ) ;
      
      _spaceReservationPanel = new SpaceReservationPanel( connection , preferences ) ;
      
      JSplitPane split = new JSplitPane(
                              JSplitPane.VERTICAL_SPLIT ,
			      new JScrollPane(_spaceLinkGroupTable) ,
			      _spaceReservationPanel
                         ) ;      
      
      
      split.setDividerLocation( 100 ) ;
      
      _commander = new EasyCommander( connection ) ;
      _commander.setDestination( "SrmSpaceManager" ) ;
      
      _switchPanel = new JPanel( _switchLayout = new CardLayout() ) ;
      _switchPanel.add( split         , "spaces" ) ;
      _switchPanel.add( _commander    , "commander" ) ;
      _switchPanel.add( _waitingImage , "waiting" ) ;
      
      setCard( "spaces" ) ;
      
      add( _switchPanel , "Center" ) ;
      
      _spaceLinkGroupTable.getSelectionModel().addListSelectionListener( new ListSelectionAction() ) ;
       
   }
   private String _cardName = null ;
   private void setCard( String cardName ){
      _switchLayout.show( _switchPanel , _cardName = cardName ) ;
   }
   private String getCardName(){ return _cardName ; }
   private class SpaceReservationPanel extends JPanel {
   
      private TitledBorder          _titledBorder          = null ;
      private SpaceReservationTable _spaceReservationTable = null ;
      private SpaceLinkGroup        _spaceLinkGroup        = null ;
      private SpaceLinkGroup        _spaceReservationLinkGroup = null ;
      private LinearSpacePanel      _pie                       = null ;
      
      public SpaceReservationPanel(  DomainConnection connection , Preferences preferences ){
      
         setLayout( new BorderLayout(10,10) ) ;
	 
         setBorder( _titledBorder = BorderFactory.createTitledBorder( 
	            BorderFactory.createLineBorder( Color.blue , 1 ) , 
		    "Space Reservations" ) ) ;

         _spaceReservationTable = new SpaceReservationTable( connection ) ;
	 _spaceReservationTable.setSpaceReservationList(new ArrayList());


	 _pie = new LinearSpacePanel();
	 
	 add( _pie , "North") ;
	 
	 _pie.setSpaces( 1L , 0L , 0L , 0L , 0L ) ;
	 
	 add( new JScrollPane(_spaceReservationTable) , "Center" ) ;
         
      
      }
      private void clearAll(){
         _spaceReservationTable.setSpaceReservationList( new ArrayList() ) ;
         _pie.setSpaces(1L,0L,0L,0L,0L);      
      }
      private void setSpaceLinkGroup( SpaceLinkGroup linkGroup ){
      
      
          _spaceReservationLinkGroup = linkGroup ;
	  _titledBorder.setTitle(_titlePrefix) ;
	  
      	  long   id   = linkGroup.getId() ;
	  String name = linkGroup.getLinkGroupName() ;

	  
	  java.util.List reservationList = null ;
	  
	  synchronized( _infoLock ){
	      reservationList = _reservationList ;
	      if( reservationList == null ){
	         System.err.println("No reservation list attached");
		 return ;	  
	      }
	  }
	  
	  _titledBorder.setTitle(_titlePrefix ) ;
	  java.util.List list = new ArrayList() ;
	  
	  long totalTokenSize  = 0L ;
	  long totalTokenUsed  = 0L ;
	  long totalLG       = linkGroup.getTotal() ;
	  
	  for( Iterator it = reservationList.iterator() ; it.hasNext() ; ){
	  
	     SpaceReservation sr = (SpaceReservation)it.next() ;
	     
	     if( sr.getLinkGroupId() == id ){
	        if( sr.getLifetime() < 0L ){
		   totalTokenSize += sr.getSize() ;
		   totalTokenUsed += sr.getUsed() ;
	           list.add( sr ) ;
		}
             }
	  }
	  long totalTokenEmpty = totalTokenSize - totalTokenUsed ;
	  long free = _spaceReservationLinkGroup.getFree() ;
	  long rest = free - totalTokenEmpty ;
	  long usedNotReserved = totalLG - free - totalTokenUsed ;
	  
	  rest = Math.max( rest , 0L ) ;
	  
	  _pie.setSpaces(  _spaceReservationLinkGroup.getTotal(),
	                   totalTokenUsed , 
			   totalTokenEmpty , 
			   usedNotReserved ,
			   rest ) ;
          
          _spaceReservationTable.setSpaceReservationList( list ) ;
      }
   }
   private class ListSelectionAction implements ListSelectionListener {

      public void valueChanged( ListSelectionEvent event ){
      
	  if( event.getValueIsAdjusting() )return ;

	  ListSelectionModel model = (ListSelectionModel)event.getSource() ;

	  int pos = model.getMinSelectionIndex() ;

	  if( pos < 0 )return ;
	  
	  System.err.println("Posistion selected : "+pos );
	  
	  SpaceLinkGroup spaceLinkGroup = _spaceLinkGroupTable.getSpaceLinkGroupAt(pos);
	  
	  
	  _spaceReservationPanel.setSpaceLinkGroup( spaceLinkGroup ) ;
	  

      }
	   
   }
   public void actionPerformed( ActionEvent event ){
   
   }
   private static final int UPDATE_SRM_LS = 1000 ;
   private static final int GET_POOL_COST = 1002 ;
   
   
   private class ControlPanel 
           extends JPanel 
	   implements ActionListener, DomainConnectionListener {
   
      private JButton                 _updateButton     = new JButton("Update Link Group Tree" ) ;
      private JButton                 _commanderButton  = new JButton("Commander" ) ;
      private JButton                 _spacesButton     = new JButton("Update Spaces" ) ;
      
      private PoolGroupLinkCollector  _treeCollector = null ;
      private CostModulePoolInfoTable _costModulePoolInfoTable = null ;
      
      public ControlPanel( DomainConnection connection , Preferences preferences ){
      
         setLayout( new FlowLayout(FlowLayout.CENTER,10,10) ) ;
         setBorder( BorderFactory.createTitledBorder( 
	            BorderFactory.createLineBorder( Color.blue , 1 ) , 
		    "Control" ) ) ;
		
         _treeCollector = new PoolGroupLinkCollector(connection,true,true);
	 _treeCollector.addActionListener(this);
	     
         _updateButton.addActionListener( this ) ;
         _commanderButton.addActionListener( this ) ;
         _spacesButton.addActionListener( this ) ;
	 
	 add( _updateButton ) ;
	 add( _spacesButton ) ;
	 add( _commanderButton ) ;
      }
      private void displayErrorMessage(String errorMessage ){
         JOptionPane.showMessageDialog(
               SrmSpaceManagerPanel.this, errorMessage ,
               "Server Problem",JOptionPane.ERROR_MESSAGE);
      }
      public void actionPerformed( ActionEvent event ){
      
          Object source = event.getSource() ;
	  
	  if( source == _updateButton ){
	       setWaiting(true);
	     try{
	     
	        _connection.sendObject(  
	            "SrmSpaceManager" , "ls -l" , this , UPDATE_SRM_LS
		                      ) ;
	              
	     }catch(Exception e ){
	        setWaiting(false);
                displayErrorMessage("Couldn't send query to server.\n"+e.getMessage() );	        
	     }
	  }else if( source == _commanderButton ){
             setCard( "commander" ) ;
	  }else if( source == _spacesButton ){
             setCard( "spaces" ) ;
	  }else if( source == _treeCollector ){
	  
	     PoolGroupLinkCollector.DataArrivedEvent e = (PoolGroupLinkCollector.DataArrivedEvent)event ;
	     
	     if(_debug)System.out.println(e.toString());
	     	     
	     if( e.isError() ){
	        setWaiting(false);
	        displayErrorMessage( e.getErrorObject().toString() );
		return ;
	     }
	     
	     if(_debug)System.out.println(_treeCollector.getLinkGroupMap());
	     //
	     // now only the pool cost is still missing.
	     //
	     try{
	     
	        _connection.sendObject(  
	            "PoolManager" , "xcm ls -l" , this ,  GET_POOL_COST
		                      ) ;
	     }catch(Exception ee ){
                setWaiting(false);
                displayErrorMessage("Couldn't send GET_POOL_COST query to PoolManager.\n"+ee.getMessage() );	        
	     }
	  }
      }
      private javax.swing.Timer _timer = new javax.swing.Timer( 100 , new TickTack() ) ;
      private boolean _weAreWaiting = false ;
      private class TickTack implements ActionListener {
         public void actionPerformed( ActionEvent event ){
	    if( _weAreWaiting ){
	        _waitingImage.setProgress(_treeCollector.getProgress());
	       
	    }
	 }
      }
      private void setWaiting( boolean isWaiting ){
	 //
	 // determine panel
	 //
         if( isWaiting ){
	    _updateButton.setEnabled(false);
	    //if( getCardName().equals("spaces") )setCard("waiting");
	    setCard("waiting");
	 }else{
	    _updateButton.setEnabled(true);
	    if( getCardName().equals("waiting") )setCard("spaces");
	 }
	 //
	 // and the timer
	 //
	 if( isWaiting & ! _weAreWaiting ){
	    _waitingImage.setMessage("Waiting for Link Group Tree");
	    _waitingImage.setProgress(0.0);
	    _timer.start() ;
	 }else if( ( ! isWaiting ) && _weAreWaiting ){
	    _timer.stop();
	 }
         _weAreWaiting = isWaiting ;
      }
      public void domainAnswerArrived( Object obj , int subid ){
      
         if( subid == UPDATE_SRM_LS ){
	 
	    if( ( obj == null ) ){
	       //
	       // This is a timeout
	       //
	       setWaiting(false);
	       displayErrorMessage("Request to SrmSpaceManager timed out" );
	       
	    }else if( obj instanceof dmg.cells.nucleus.NoRouteToCellException ){
	       //
	       // The SrmSpaceManager is not reachable.
	       //
	       setWaiting(false);
	       displayErrorMessage("SrmSpaceManager not present !" );
        }else if( obj instanceof CacheException){
            setWaiting(false);
            displayErrorMessage("SrmSpaceManager: No Link Groups configured.\n");
	    }else if( ! ( obj instanceof String ) ){
	       //
	       // Something unexpected.
	       //
	       setWaiting(false);
	       displayErrorMessage("Unexpected reply from SrmSpaceManager :\n"+
	                           obj.getClass().getName() );
				   
	    }else{
	       try{
	          //
		  // This is a valid reply from the space manager.
		  //
	          srmSpaceLsArrived( (String) obj ) ;
		  //
		  // Though we still need the PoolManager tree structure.
		  //
		  _treeCollector.collectData() ;
		  //
	       }catch(Exception ee ){
	          
	          displayErrorMessage("Problem in decoding SrmSpaceManager reply :\n"+ee );
                  setWaiting(false);
	          ee.printStackTrace();
		  return ;
	       }
	    }
	 }else if( subid == GET_POOL_COST ){
	    if( ( obj == null ) ){
	       //
	       // This is a timeout
	       //
	       setWaiting(false);
	       displayErrorMessage("Request to PoolManager timed out" );
	       
	    }else if( obj instanceof dmg.cells.nucleus.NoRouteToCellException ){
	       //
	       // The PoolManager is not reachable.
	       //
	       setWaiting(false);
	       displayErrorMessage("PoolManager not present !" );
	       	       
	    }else if( ! ( obj instanceof CostModulePoolInfoTable ) ){
	       //
	       // Something unexpected.
	       //
	       setWaiting(false);
	       displayErrorMessage("Unexpected reply from PoolManager :\n"+
	                           obj.getClass().getName() );
				   
	    }else{
	    
	       _costModulePoolInfoTable = (CostModulePoolInfoTable) obj ;
	          
	       new Thread( new Runnable(){
	                       public void run(){
			           try{
				      calculateInfos() ;
				   }finally{
				       SwingUtilities.invokeLater(
				          new Runnable(){
					      public void run(){
	                                         setWaiting(false);
					      }
					  }
				       ) ;
				   }
			       }
	                   }
			 ).start() ;
	       
	    }
	 
	 }
      }
      private void calculateInfos(){
      
         Map linkGroups = _treeCollector.getLinkGroupMap() ;
	 Map links      = _treeCollector.getLinkMap() ;
	 Map poolGroups = _treeCollector.getPoolGroupMap() ;
	 
	 
         for( Iterator lgs = linkGroups.entrySet().iterator() ; lgs.hasNext() ; ){
	 
	    Map.Entry entry         = (Map.Entry)lgs.next() ;
	    String    linkGroupName = (String)entry.getKey() ;
	    ArrayList linkNameList  = (ArrayList)entry.getValue() ;
            Set       poolNameSet   = new HashSet() ;
	    
	    if(_debug)System.out.println("LinkGroupName : "+linkGroupName);
	    
	    for( Iterator lks = linkNameList.iterator() ; lks.hasNext() ; ){
	    
	       String linkName = (String)lks.next() ;
	       
	       if(_debug)System.out.println("  Link : "+linkName);
	       
	       PoolGroupLinkCollector.LinkEntry le =
	            (PoolGroupLinkCollector.LinkEntry)links.get(linkName);
		    
	       if(_debug)if(le==null)System.out.println("   LinkName "+linkName+" not found");
	       
               String [] poolNames = le.getResolvedPools() ;
	       if( poolNames == null )continue ;
	       
	       for( int i = 0 ; i < poolNames.length ; i++ ){
	           poolNameSet.add(poolNames[i]);
	       } 
	    }
	    long size = 0L ;
	    for( Iterator itt = poolNameSet.iterator() ; itt.hasNext() ; ){
	       String poolName = (String)itt.next() ;
	       PoolCostInfo info = _costModulePoolInfoTable.getPoolCostInfoByName(poolName);
               if( info == null ){
                  System.err.println("No cost info found for : "+poolName);
                  continue ;
               }
	       size += info.getSpaceInfo().getTotalSpace() ;
	    }
	    if( _debug )System.out.println("calculateInfos : "+linkGroupName+" "+size+" : "+poolNameSet);
	    
	    SpaceLinkGroup lg = (SpaceLinkGroup)_linkGroupMapByName.get(linkGroupName) ;
	    if( lg == null ){
	       System.err.println("Couldn't find "+linkGroupName) ;
	       continue ;
	    }
	    lg.setTotal(size);
	 }
	 if(_debug)System.out.println("LinkGroupMap : "+_linkGroupMap);
	 //
	 // set the link group table.
	 _spaceLinkGroupTable.setSpaceLinkGroupMap( _linkGroupMap ) ;
	 //
	 // reset the picture and the reservation table.
	 //
         _spaceReservationPanel.clearAll();
      }
      private void srmSpaceLsArrived( String str ) throws IllegalArgumentException {
      
         if(_debug)System.out.println(str);
	 int errors = 0 ;
	 java.util.List reservationList = new ArrayList() ;
	 java.util.Map  linkGroupMap    = new HashMap() ;
         StringTokenizer st = new StringTokenizer( str , "\n" ) ;
	 int state = 0 ;
	 while( st.hasMoreTokens() ){
	 
	    String line = st.nextToken().trim() ;

	    if( line.length() == 0 )continue ;
	    if( line.startsWith("total number") ){
	       state = 0 ;
	       continue ;
	    }
	    switch( state ){
	       case 0 :
	          if( line.startsWith("Reservations:") ){
		     state = 1 ;
		  }else if( line.startsWith("LinkGroups:") ){
		     state = 2 ;
		  }
	       break ;
	       case 1 : 
	         try{
		     reservationList.add( new SpaceReservation( line ) ) ;
		 }catch(Exception e ){
		     errors ++ ;
		     System.err.println("Problem in scanning reservations of 'srm space ls' : "+e);
		     e.printStackTrace() ;
		 }
	       break ;
	       case 2 : 
	         try{
		     SpaceLinkGroup slg = new SpaceLinkGroup(line);
		     linkGroupMap.put( new Long(slg.getId()) , slg ) ;
		 }catch(Exception e ){
		     errors ++ ;
		     System.err.println("Problem in scanning linkGroups of 'srm space ls' : "+e);
		     e.printStackTrace() ;
		 }
	       break ;
	    }
	 
	 }
	 if( errors > 0 ){
	    displayErrorMessage(
	       "Some errors detected while scanning srm spaces; check console output");
	 }
	 System.out.println("Reservations : "+reservationList.size());
	 System.out.println("Link Groups  : "+linkGroupMap.size());
	 System.out.println("Scan Errors  : "+errors);
	 //
	 // iterate through the reservations, insert the link group name
	 // and add them to the array.
	 //
	 for( Iterator r = reservationList.iterator() ; r.hasNext()  ; ){
	 
	    SpaceReservation spaceReservation = (SpaceReservation)r.next() ;
	    
	    SpaceLinkGroup linkGroup = (SpaceLinkGroup)linkGroupMap.get( new Long(spaceReservation.getLinkGroupId()) ) ;
	    
	    if( linkGroup != null )spaceReservation.setLinkGroupName( linkGroup.getLinkGroupName() ) ;
	    
	 }
	 
	 
         synchronized( _infoLock ){
	 
	    _reservationList = reservationList ;
	    _linkGroupMap    = linkGroupMap ;
	    _linkGroupMapByName = new HashMap() ;
	    
	    for( Iterator iii = _linkGroupMap.values().iterator() ; iii.hasNext() ; ){
	       SpaceLinkGroup slg = (SpaceLinkGroup)iii.next() ;
	       _linkGroupMapByName.put( slg.getLinkGroupName() , slg ) ;
	    }

	 }
      }
   }
   
}
