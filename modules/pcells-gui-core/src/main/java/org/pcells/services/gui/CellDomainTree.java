// $Id: CellDomainTree.java,v 1.2 2007/01/02 07:46:32 cvs Exp $
//
package org.pcells.services.gui ;
//

import dmg.cells.network.CellDomainNode;
import dmg.cells.nucleus.CellInfo;
import org.pcells.services.connection.DomainConnection;
import org.pcells.services.connection.DomainConnectionListener;
import org.pcells.util.Logable;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class      CellDomainTree 
       extends    CellGuiSkinHelper.CellPanel 
       implements ActionListener, 
                  DomainConnectionListener,
                  TreeWillExpandListener,
                  TreeSelectionListener {

   private DomainConnection _connection   = null ;
   private HashMap          _domainMap    = null ;
   private TreeRoot         _treeRoot     = null ;
   private JTree            _tree         = null ;  
   private DeepSpaceScan    _deepScan     = null ;
   private boolean          _useShortAddresses = true ;
   private JComponent       _ourParent    = this ;

   private RealmRoot        _realmRoot    = null ;
   private BookmarkRoot     _bookmarkRoot = null ;
   
   private Preferences      _preferences   = null ;
   private Preferences      _bookmarkPrefs = null ;   
   private Logable          _logger = new OurLogable();


   private static final int CELLINFOS = 1001 ;
      
   public CellDomainTree( DomainConnection connection , Preferences pref ){
      _connection = connection ;
      
      _bookmarkPrefs = ( _preferences = pref ).node("Bookmarks");
      
      setLayout( new BorderLayout( 10 , 10 ) ) ;
      
      
      _treeRoot     = new TreeRoot() ;
      
      _bookmarkRoot = new BookmarkRoot(_treeRoot) ;
      
      _realmRoot    = new RealmRoot(_treeRoot) ;
      
      _treeRoot.add(_bookmarkRoot) ; 
      _treeRoot.add(_realmRoot) ; 
      
      _tree  = new JTree( _treeRoot ) ;
      _tree.addTreeWillExpandListener(this);
      _tree.addTreeSelectionListener(this);
      _tree.addMouseListener(new MouseActions());
      _tree.setRootVisible(false);
      _tree.collapseRow(0);
      _tree.collapseRow(1);
      _tree.setCellRenderer( new CustomBasicRenderer() ) ;
      
      JScrollPane treeScroll = new JScrollPane( _tree ) ;
      CellGuiSkinHelper.setComponentProperties( treeScroll ) ;
                                    
      add( treeScroll ) ;

      CellGuiSkinHelper.setComponentProperties( _tree ) ;
      CellGuiSkinHelper.setComponentProperties( treeScroll ) ;

      _tree.setBackground(Color.white);
      
      addActionListener(this);
      
      _deepScan = new DeepSpaceScan( connection , _logger , true ) ;
      _deepScan.addActionListener(this);
      
 //     _plugginDialog = new PlugginDialog( null ) ;
      
      createPopup() ;
      
      readBookmarks() ;
   }
   public class OurLogable implements Logable {
       public void log( String log ){
          System.out.println(log);
       }
       public void elog( String log ){
          System.err.println(log);
       }
       public void plog( String log ){
          System.err.println(log);
       }
   }
   

   private void readBookmarks(){
      try{
          String [] prefs = _bookmarkPrefs.childrenNames() ;
          for( int i = 0 ; i < prefs.length ; i++ ){
         
             try{
                String        bookmark  = prefs[i] ;
                Preferences      child  = _bookmarkPrefs.node(bookmark) ;
                BookmarkCellFrame frame = new BookmarkCellFrame( _bookmarkRoot , bookmark , child ) ;
                 _bookmarkRoot.add( frame );
                 ((DefaultTreeModel)_tree.getModel()).reload(  _bookmarkRoot ) ;
             }catch(Exception eee ){
                eee.printStackTrace() ;
                
             }
          }
      }catch(Exception ee ){
         ee.printStackTrace() ;
      }
   }
   public class CellFrame extends ListEntry {
      public class FrameCellInfo {
         private String _name       = null ;
         private String _domainName = null ;
         private String _cellClass  = null ;
         private FrameCellInfo( String name , String domainName , String cellClass ){
            _name = name ; _domainName = domainName ; _cellClass = cellClass ;
         }
         private FrameCellInfo(CellInfo info ){
            _name = info.getCellName() ;
            _domainName = info.getDomainName() ;
            _cellClass  = info.getCellClass() ;
         }
         public String getCellClass(){ return _cellClass ; }
         public String getCellName(){ return _name ; }
         public String getDomainName(){ return _domainName ; }
      }
      protected String        address = null ;
      protected FrameCellInfo info    = null ;
      private CellFrame( ListEntry parent , boolean isLeaf  ){
         super(parent,isLeaf);
      }
      private CellFrame( Frame domain , String name , String address , CellInfo info ){
         super(domain,false);
         this.name    = name ;
         this.address = address ;
         this.info    = new FrameCellInfo( info ) ;
      }
      public FrameCellInfo getCellInfo(){ return this.info ; }
   }
   public class BookmarkCellFrame extends CellFrame {
      private String domainName = null ;
      private BookmarkCellFrame( ListEntry parent , CellFrame cellFrame ){
         super(parent,false);
         this.name    = cellFrame.name ;
         this.address = cellFrame.address ;
         this.domainName = ((ListEntry)cellFrame.parent).getName() ;
         this.info    = cellFrame.info ;
      }
      private BookmarkCellFrame( ListEntry parent , String name , Preferences prefs ) throws BackingStoreException {
         super(parent,false);
         this.name    = name ;

         this.address    = prefs.get("address" , "?" ) ;
         this.domainName = prefs.get("domainName" , "?" ) ;

        
         this.info = new FrameCellInfo(
                              name , 
                              prefs.get("cellInfo.domainName" , "?" ) ,
                              prefs.get("cellInfo.cellClass" , "?" )
                         ) ;
                 
      }
     
      public String getDomainName(){ return this.domainName ; }
      public String getAddress(){ return this.address ; }
      public String getCellName(){ return this.name ; }
      
      public void remove( Preferences pref ) throws BackingStoreException {
         pref.node(getName()).removeNode();
      }
      public void store( Preferences pref ) throws BackingStoreException {
          Preferences node = pref.node(getName());
          node.put( "domainName" , getDomainName() ) ;
          node.put( "address" , getAddress() ) ;
          node.put( "cellInfo.cellName"   , info.getCellName() ) ;
          node.put( "cellInfo.domainName" , info.getDomainName() ) ;
          node.put( "cellInfo.cellClass"  , info.getCellClass() ) ;
          node.flush() ;
          node.sync();
      }
   }
   private class Frame extends ListEntry {
      private CellDomainNode node      = null ;
      private ArrayList      cellinfos = null ;
      private boolean        _hasChildren = true ;
      public Frame( TreeNode root , CellDomainNode node ){
          super(root,true);
          this.node = node ;
          this.name = node.getName();
          
          _hasChildren = node.getLinks() != null ;
      }
      public Enumeration children(){ 
          return cellinfos == null ? null : new Vector(cellinfos).elements() ; 
      }
      public boolean getAllowsChildren(){ return _hasChildren ; }
      public TreeNode getChildAt( int index ){ return (TreeNode)cellinfos.get(index) ; }
      public int getChildCount(){ return ( cellinfos == null ) || ( ! _hasChildren ) ? 0 : cellinfos.size() ; }
      public int getIndex( TreeNode node ){ return cellinfos.indexOf(node) ; }
      public TreeNode getParent(){ return parent ; }
      public boolean isLeaf(){ return false ; }
   }
   private class RealmRoot extends ListEntry {
      private Vector vector = new Vector() ;
      private RealmRoot( TreeNode parent ){
         super(parent,true);
         this.name = "Realm" ;
      }
      public void setVector( Vector vector ){
         this.vector = vector ;
      }
      public Enumeration children(){ return vector.elements() ;  }
      public boolean getAllowsChildren(){ return true ; }
      public TreeNode getChildAt( int index ){ return (TreeNode)vector.get(index) ; }
      public int getChildCount(){ 
         return vector.size() ; 
      }
      public int getIndex( TreeNode node ){ return vector.indexOf(node) ; }
      public boolean isLeaf(){ 
         return false ; 
      }
   }
   private class TreeRoot extends ListEntry {
      private Vector vector = new Vector() ;
      private TreeRoot(){
         super(null,true);
         this.name = "ROOT" ;
      }
      public void setVector( Vector vector ){
         this.vector = vector ;
      }
      public Enumeration children(){ return vector.elements() ;  }
      public boolean getAllowsChildren(){ return true ; }
      public TreeNode getChildAt( int index ){ return (TreeNode)vector.get(index) ; }
      public int getChildCount(){ 
         return vector.size() ; 
      }
      public int getIndex( TreeNode node ){ return vector.indexOf(node) ; }
      public boolean isLeaf(){ 
         return false ; 
      }
      public void add( Object element ){
         vector.add(element);
      }
   }
   private class BookmarkRoot extends ListEntry {
      private Vector vector = new Vector() ;
      private BookmarkRoot(TreeNode parent){
         super(parent,true);
         this.name = "Bookmarks" ;
      }
//      public void setVector( Vector vector ){
//         this.vector = vector ;
//      }
      public Enumeration children(){ return vector.elements() ;  }
      public boolean getAllowsChildren(){ return true ; }
      public TreeNode getChildAt( int index ){ return (TreeNode)vector.get(index) ; }
      public int getChildCount(){ 
         return vector.size() ; 
      }
      public int getIndex( TreeNode node ){ return vector.indexOf(node) ; }
      public boolean isLeaf(){ 
         return false ; 
      }
      public void add( ListEntry entry ){
          if( this.vector.contains(entry) )return ;
          entry.parent = this ;
          this.vector.add( entry ) ;
      }
      public void remove( ListEntry entry ){
         this.vector.remove(entry);
      }
   }
   private class ListEntry implements Comparable, TreeNode {
   
      protected String   name           = "Realm" ;
      private   boolean  allowsChildren = false ;
      protected TreeNode parent         = null ;
      
      public ListEntry( TreeNode parent , boolean allowsChildren ){
        this.allowsChildren = allowsChildren ; 
        this.parent         = parent ;
      }
      public String toString(){ return name ; }
      public int hashCode(){ return name.hashCode() ; }
      public int compareTo( Object other ){
         return name.compareTo(other.toString());
      }
      public boolean equals( Object other ){
         if( other == null )return false ;
         return name.equals(other.toString());
      }
      public String getName(){ return name ; }
      public Enumeration children(){ return null ; }
      public boolean getAllowsChildren(){ return false ; }
      public TreeNode getChildAt( int index ){ return null ; }
      public int getChildCount(){ return 0 ; }
      public int getIndex( TreeNode node ){ return 0 ; }
      public TreeNode getParent(){ return parent ; }
      public boolean isLeaf(){ 
          return true ; 
      }
   }
   private class CustomBasicRenderer extends DefaultTreeCellRenderer {
      public Component getTreeCellRendererComponent( 
                 JTree tree,
                 Object value ,
                 boolean selected,
                 boolean expanded,
                 boolean leaf ,
                 int row ,
                 boolean hasFocus ){
                 
       
          Component c = super.getTreeCellRendererComponent(         
                            tree,value,selected,expanded,leaf,row,hasFocus);
          if( c instanceof JLabel ){
          
             JLabel label = (JLabel)c ;
             Icon icon = label.getIcon() ;
             if( value instanceof CellFrame ){
                String name = value.toString() ;
                label.setForeground( 
                         name.equals("System") ?
                         Color.green :
                         Color.black    ) ;
             }else if( value instanceof Frame ){
                Frame f = (Frame)value ;
                //label.setForeground( ( f.cellinfos == null ) || ( f.node.getLinks() == null ) ? Color.red : Color.black ) ;
                label.setForeground( f.node.getLinks() == null  ? Color.red : 
                                     f.cellinfos == null        ? Color.black :
                                     Color.blue ) ;
             }        
          }else{
                System.out.println("x Rendering for "+c);
          }
              
          return c ;
      }
   }
   public void setDomain( String domainName ){
      if( _domainMap == null )return ;
      Frame domain = (Frame)_domainMap.get(domainName);
      if( domain == null )return ;
      Object [] olist = new Object[3];
      olist[0] = _treeRoot ;
      olist[1] = _realmRoot ;
      olist[2] = domain ;
      TreePath path = new TreePath( olist ) ;
      _tree.setSelectionPath(path) ;
   }
   public void setTopology( java.util.List list ){
      HashMap map = new HashMap() ;
      for( Iterator i = list.iterator() ; i.hasNext() ; ){
         CellDomainNode node = (CellDomainNode)i.next() ;
         map.put( node.getName() , new Frame(_realmRoot,node) ) ;
      }
      TreeSet set    = new TreeSet( map.values() ) ;
      Vector  vector = new Vector( set );
      _realmRoot.setVector(vector);
      _domainMap = map ;
      _tree.treeDidChange();
     
      DefaultTreeModel treeModel = (DefaultTreeModel)_tree.getModel() ;
      
      treeModel.nodeChanged(  _realmRoot ) ;
      treeModel.nodeStructureChanged(  _realmRoot ) ;
      
      _tree.expandPath( new TreePath( _realmRoot ) ) ;
      
   }
   public void actionPerformed( ActionEvent event ){
      Object source = event.getSource() ;
      if( source == _deepScan ){
      
         DeepSpaceScan.ScanFinishedEvent d = (DeepSpaceScan.ScanFinishedEvent)event ;
         setTopology( d.getDomainList() ) ;
         setEnabled(true);
         fireDomainScanEvent( d.getDomainList() ) ;

      }else if( source ==  this ){
      //   System.err.println("OUR EVENT : "+event );
      }
   }
   public class MouseActions extends MouseAdapter {
   
      public void mousePressed(MouseEvent e) {
      
         int      selRow  = _tree.getRowForLocation(e.getX(), e.getY());
         TreePath selPath = _tree.getPathForLocation(e.getX(), e.getY());
         
         if( e.isPopupTrigger() ){
             _popup.show( _ourParent , e.getX() , e.getY() ) ;
         }
         if(selRow != -1) {
         }
     }
   
   }
   public void doDomainScan(){
      try{
          setEnabled(false);
          fireDomainScanEvent(null);
          _deepScan.scan() ;
          _tree.clearSelection();
      }catch(Exception ee ){
         JOptionPane.showMessageDialog(
            _ourParent,               
            "Scan reported "+ee.getMessage()  ,
            "Problem in Domain Scan" ,
            JOptionPane.ERROR_MESSAGE ) ;
         setEnabled(true);
          ee.printStackTrace();
      }         
   }
   private class PopupActions implements ActionListener {
      public void actionPerformed( ActionEvent event ){
      
         Object source = event.getSource() ;
         
         if( source == _rescanDomainItem ){
         
             doDomainScan() ;       
               
         }else if( source == _rescanCellItem ){
         
             if( _currentDomain != null )updateCellListFor( _currentDomain ) ;
             
         }else if( source == _bookmarkCellItem ){
         
             if( _currentCell != null ){

                 BookmarkCellFrame frame = new BookmarkCellFrame( _bookmarkRoot , _currentCell) ;
                 try{ frame.store( _bookmarkPrefs ) ; }catch(Exception ee ){ ee.printStackTrace() ; }
                 _bookmarkRoot.add( frame );
                 ((DefaultTreeModel)_tree.getModel()).reload(  _bookmarkRoot ) ;
                 
             }
         }else if( source == _rmBookmarkCellItem ){
         
             if( _currentBookmark != null ){
                 try{ _currentBookmark.remove( _bookmarkPrefs ) ; }catch(Exception ee ){ ee.printStackTrace() ; }
                 _bookmarkRoot.remove( _currentBookmark );
                 ((DefaultTreeModel)_tree.getModel()).reload(  _bookmarkRoot ) ;
                 
             }
         }
      }
   }
   private JPopupMenu   _popup              = null ;
   private JMenuItem    _rescanDomainItem   = null ;
   private JMenuItem    _rescanCellItem     = null ;
   private JMenuItem    _bookmarkCellItem   = null ;
   private JMenuItem    _rmBookmarkCellItem = null ;
   private PopupActions _popupActions       = new PopupActions() ;
   private Frame        _currentDomain      = null ;
   private CellFrame    _currentCell        = null ;
   private BookmarkCellFrame  _currentBookmark    = null ;
   
   private void createPopup(){

        _popup = new JPopupMenu("Setup") ;

        _popup.setBorderPainted(true);
 
        _popup.add( new JLabel("Rescan(s)") ).setForeground(Color.red);
        
        _rescanDomainItem = _popup.add( new JMenuItem("Rescan Realm") ) ;
        _rescanDomainItem.addActionListener( _popupActions ) ;

        _rescanCellItem = _popup.add( new JMenuItem("Rescan Domain") ) ;
        _rescanCellItem.addActionListener( _popupActions ) ;
        _rescanCellItem.setEnabled(false);

        _popup.add( new JSeparator() ) ;
        _popup.add( new JLabel("Bookmarks") ).setForeground(Color.red);
        
        _bookmarkCellItem = _popup.add( new JMenuItem("Add Bookmark") ) ;
        _bookmarkCellItem.addActionListener( _popupActions ) ;
        _bookmarkCellItem.setEnabled(false);

        _rmBookmarkCellItem = _popup.add( new JMenuItem("Remove Bookmark") ) ;
        _rmBookmarkCellItem.addActionListener( _popupActions ) ;
        _rmBookmarkCellItem.setEnabled(false);

   }
   public void setCurrentBookmark( BookmarkCellFrame bookmark, TreePath path ){
     // System.out.println("BOOKMARK : address : "+bookmark.address);
      if( bookmark == null ){
          _rmBookmarkCellItem.setText("Remove Bookmark");
          _rmBookmarkCellItem.setEnabled(false);
          _currentBookmark = null ;
      }else{
          _rmBookmarkCellItem.setEnabled(true);
          _rmBookmarkCellItem.setText("Remove Bookmark "+bookmark.getName());
          _currentBookmark = bookmark ;
      }
   }
   public void setCurrentDomain( Frame domain ){
     
      if( domain == null ){
          _rescanCellItem.setText("Rescan Domain");
          _rescanCellItem.setEnabled(false);
          _currentDomain = null ;
      }else{
          _rescanCellItem.setEnabled(true);
          _rescanCellItem.setText("Rescan Domain "+domain.getName());
          _currentDomain = domain ;
      }
   }
   public void setCurrentCell( CellFrame cell ){
     
      if( cell == null ){
          _bookmarkCellItem.setText("Add Bookmark");
          _bookmarkCellItem.setEnabled(false);
          _currentCell = null ;
      }else{
          _bookmarkCellItem.setEnabled(true);
          _bookmarkCellItem.setText("Add Bookmark "+cell.getName());
          _currentCell = cell ;
      }
   }
   public void setEnabled(boolean enabled ){
      _tree.setEnabled(enabled);
      //_tree.setBackground( enabled ? Color.white : Color.red ) ;
   }
   public void domainAnswerArrived( Object obj , int id ){
      try{
         if( id == CELLINFOS ){
            if( obj instanceof CellInfo []  ){
                Frame domain = _waitingForCellListOfFrame ;
                _waitingForCellListOfFrame = null ;
                if( domain == null )return ;
                
                CellInfo [] info = (CellInfo[])obj ;
                TreeSet set = new TreeSet() ;
                for( int i = 0 , n = info.length ; i < n ; i++ ){
                   String cellName = info[i].getCellName() ;
                   CellFrame cf = new CellFrame(
                         domain , 
                         cellName ,
                         _useShortAddresses ?
                         cellName+"@"+domain.getName() :
                         domain.node.getAddress()+":"+cellName+"@"+domain.getName(),
                         info[i] ) ;
                   set.add(cf);
                }
                domain.cellinfos = new ArrayList(set) ;
               ((DefaultTreeModel)_tree.getModel()).reload(  domain ) ;
               _tree.expandPath( new TreePath(domain) );
            }
            setEnabled(true);
         }
      }catch(Exception e){
          e.printStackTrace();
      }
   }
   private void loadCells( Frame domain ){
      ArrayList list   = domain.cellinfos ;
      CellFrame system = null ;
      for( Iterator i = list.iterator() ; i.hasNext() ; ){
         CellFrame cf = (CellFrame)i.next() ;
         if( cf.getName().equals("System") )system = cf ;
      }
   }
   private Frame _waitingForCellListOfFrame = null ;
   private void updateCellListFor( Frame frame ){
      if( frame.node.getLinks() == null )return ;
      setEnabled(false);
      _tree.clearSelection();
      _waitingForCellListOfFrame = frame ;
      try{
         _connection.sendObject( frame.node.getAddress() ,
                                 "getcellinfos" , 
                                 this ,
                                 CELLINFOS );
      }catch(Exception ee){
         setEnabled(false);
         ee.printStackTrace() ;
      }
   }
   public void treeWillCollapse( TreeExpansionEvent event ){
      //System.err.println("Collapse : "+event.getPath());
   }
   public void treeWillExpand(  TreeExpansionEvent event ) throws ExpandVetoException {
      //System.err.println("Will Expand : "+event.getPath());

      TreePath path = event.getPath();
      int count = path.getPathCount() ;
      if( count < 2 )return ;
      if( count == 2 ){
      
         if( path.getPathComponent(1) != _realmRoot )return ;

         if( ( _domainMap == null ) || ( _domainMap.size() == 0 ) ){
            int res = JOptionPane.YES_OPTION ;
            /*
              JOptionPane.showConfirmDialog(  
                 this , 
                 "Shell the Domain list be updated ?", 
                 "What to do next", 
                 JOptionPane.YES_NO_OPTION);
            */
            if( res == JOptionPane.YES_OPTION )doDomainScan() ;

            throw new 
            ExpandVetoException( event , "No entries found" ) ; 
         }
         return ;
      }
      Frame domain = (Frame)path.getPathComponent(2);
      if( domain == null )return ;
      if( domain.cellinfos == null ){
          updateCellListFor( domain ) ; 
          throw new 
          ExpandVetoException( event , "No entries found" ) ; 
      }
   }
   public void valueChanged( TreeSelectionEvent event ){
   
      TreePath path = event.getPath();
      //System.err.println("Value changed (Tree) : "+path+" "+event.isAddedPath()+" : "+event);
      
      setCurrentDomain(null);
      setCurrentCell(null) ;
      setCurrentBookmark(null,null);
      
      if( ! event.isAddedPath() ){
         fireCellDomainEvent();
         return ;
      }
      
      int count = path.getPathCount() ;

      String cellName   = null ;
      String domainName = null ;
      String address    = null ;
      CellFrame.FrameCellInfo info     = null ;
      if( count > 1 ){
      
         if( path.getPathComponent(1) == _bookmarkRoot ){
         
            if( count > 2 ){
            
                BookmarkCellFrame frame = (BookmarkCellFrame)path.getPathComponent(2);
                setCurrentBookmark( frame , path ) ;
                cellName   = frame.getCellName() ;
                domainName = frame.getDomainName() ;
                address    = frame.getAddress() ;
                info       = frame.getCellInfo() ;
                
            }
         }else{

            if( count > 2 ){

               Frame domain = (Frame)path.getPathComponent(2);
               if( domain != null ){
                  setCurrentDomain( domain );
                  domainName = domain.getName() ;
                  address    = "System@"+domainName ;
               }

               if( count > 3 ){

                  CellFrame cell = (CellFrame)path.getPathComponent(3) ;
                  if( cell != null ){
                     setCurrentCell( cell ) ;
                     cellName = cell.getName() ;
                     address  = cellName + "@" + domainName ;
                     info     = cell.getCellInfo() ;
                  }
          
               }
            }  
         }
         
      }
      fireCellDomainEvent( domainName , cellName , address , info ) ;
           
   }
   /* 
    *       EVENT processing
    */
    public void fireCellDomainEvent(){
      fireCellDomainEvent(null,null,null,null) ;
    }
    public void fireCellDomainEvent( String domain , String cell , String address , CellFrame.FrameCellInfo cellInfo ){
       fireCellDomainEvent( 
           new CellDomainEvent( this , 10000 , "cellDomainEventfired" ,
                                domain , cell, address , cellInfo ) 
                           );
    }
    public void fireDomainScanEvent( java.util.List domainList ){
       fireCellDomainEvent( 
           new DomainScanEvent( this , 10001 , "scanDomainEventfired" ,domainList));
    }
    public void fireCellDomainEvent( final ActionEvent event ){
       SwingUtilities.invokeLater(
           new Runnable(){
              public void run(){
                   processEvent( event ) ;
              }
           }
       ) ;
    }
    public class DomainScanEvent extends ActionEvent {
        private java.util.List _domainList = null ;
        private DomainScanEvent( Object source , int id , String command ,
                                 java.util.List domainList ){
          super( source , id , command ) ;
          _domainList = domainList ;                        
        }
        public boolean isScanDone(){
          return _domainList != null  ;
        }
        public java.util.List getTopology(){ return _domainList  ;}
    }
    public class CellDomainEvent extends ActionEvent {
       private String _domain  = null ;
       private String _cell    = null ;
       private String _address = null ;
       private CellFrame.FrameCellInfo _info  = null ;
       private CellDomainEvent( Object source , int id , String command , 
                                String domain , String cell , String address , CellFrame.FrameCellInfo info  ){
          super( source , id , command ) ;
          _domain  = domain ;
          _cell    = cell ;
          _address = address ;
          _info    = info ;
       }
       public String getDomain(){ return _domain ; }
       public String getCell(){ return _cell ; }
       public String getAddress(){ return _address ; }
       public CellFrame.FrameCellInfo getCellInfo(){ return _info ; }
       public boolean isDomainOnly(){
          return ( _domain != null ) && ( _cell == null ) ;
       }
       public String toString(){
          return "CellDomainEvent (Domain="+_domain+";Cell="+_cell+";Address="+_address+")";
       }
    }
    private ActionListener _actionListener = null;

    public synchronized void addActionListener(ActionListener l) {
       _actionListener = AWTEventMulticaster.add( _actionListener, l);
    }
    public synchronized void removeActionListener(ActionListener l) {
       _actionListener = AWTEventMulticaster.remove( _actionListener, l);
    }
    public synchronized void processEvent( ActionEvent e) {
       if( _actionListener != null)
         _actionListener.actionPerformed( e );
    }
    
}
