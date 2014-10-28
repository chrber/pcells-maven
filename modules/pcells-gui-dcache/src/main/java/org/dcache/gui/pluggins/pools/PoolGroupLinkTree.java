// $Id: PoolGroupLinkTree.java,v 1.2 2007/02/15 08:21:38 cvs Exp $
//
package org.dcache.gui.pluggins.pools ;
//
import org.pcells.services.connection.DomainConnection;
import org.pcells.services.connection.DomainConnectionListener;
import org.pcells.services.gui.ActionEventDistributer;
import org.pcells.services.gui.CellGuiSkinHelper;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.prefs.Preferences;

public class      PoolGroupLinkTree 
       extends    CellGuiSkinHelper.CellPanel 
       implements ActionListener, 
                  DomainConnectionListener,
                  TreeWillExpandListener,
                  TreeSelectionListener {

   private DomainConnection _connection   = null ;
   private JTree            _tree         = null ;  
   private RootContainer    _root         = null ;
   private JButton          _updateButton = new CellGuiSkinHelper.CellButton("Update Tree") ;
   private boolean          _primaryDone  = false ;
   private PoolGroupLinkCollector _collector  = null ;
   private ActionEventDistributer _listeners  = new ActionEventDistributer() ;

   public class PoolGroupLinkTreeEvent extends ActionEvent {
       private TreeNode _currentTreeNode = null ;
       public PoolGroupLinkTreeEvent( Object source , String command , TreeNode node ){
          this( source , command ) ;
          _currentTreeNode = node ;
       }
       public PoolGroupLinkTreeEvent( Object source , String command ){
          super( source , 10000 , command ) ;
       }
       public TreeNode getCurrentNode(){ return _currentTreeNode ; }
       public String toString(){
         return "PoolGroupLinkTreeEvent;command="+getActionCommand()+"/"+
                (  _currentTreeNode == null ? "NONE" : _currentTreeNode.getClass().getName() ) ; 
       }
   }   
   public PoolGroupLinkTree( DomainConnection connection , Preferences pref ){

      setLayout( new BorderLayout( 10 , 10 ) ) ;

      _connection = connection ;

      _root = new RootContainer() ;
      _root.add( new PoolGroupContainer("Pool Groups") ) ;
      _root.add( new LinkContainer("Links") ) ;
      
      _tree  = new JTree(_root) ;
      _tree.addTreeWillExpandListener(this);
      _tree.addTreeSelectionListener(this);
      //_tree.addMouseListener(new MouseActions());
      _tree.setRootVisible(false);

      JScrollPane treeScroll = new JScrollPane( _tree ) ;
      CellGuiSkinHelper.setComponentProperties( treeScroll ) ;
                                    
      add( treeScroll , "Center" ) ;
      add( _updateButton , "South" ) ;

      _collector = new PoolGroupLinkCollector( connection , true ) ;
      
      _collector.addActionListener( this ) ;
      _updateButton.addActionListener(this) ;
   }
   public void setEnabled( boolean enabled ){
      _tree.setEnabled(enabled);
   }
   public void addActionListener( ActionListener listener ){
      _listeners.addActionListener( listener ) ;
   }
   private void fireEvent(String actionCommand ){
       _listeners.fireEvent( new PoolGroupLinkTreeEvent( this , actionCommand ) ) ;
   }
   private void fireEvent( TreeNode node ){
       _listeners.fireEvent( new PoolGroupLinkTreeEvent( this , "node" , node ) ) ;
   }
   private class RootContainer extends TreeContainer {
      private RootContainer(){
         super("ROOT");
      }
   }
   private class PoolGroupContainer extends TreeContainer {
      private PoolGroupContainer( String name ){
         super(name);
      }
   }
   private class PoolContainer extends TreeContainer {
      private PoolContainer( String name ){
         super(name);
      }
   }
   private class LinkContainer extends TreeContainer {
      private LinkContainer( String name ){
          super(name) ;
      }
   }
   public class LinkEntry extends TreeContainer {
      private LinkEntry( String name ){
          super(name) ;
      }
   }
   public class PoolGroupEntry extends TreeContainer {
      private PoolGroupEntry( String name ){
          super(name) ;
      }
   }
   private class TreeContainer extends ListEntry {
      private Vector vector = new Vector() ;
      private Map    map    = new HashMap() ;
      private TreeContainer( String name ){
         super(name);
         this.allowsChildren = true ;
      }
      public Enumeration children(){ return vector.elements() ;  }
      public TreeNode getChildAt( int index ){ return (TreeNode)vector.get(index) ; }
      public int getChildCount(){ return vector.size() ; }
      public int getIndex( TreeNode node ){ return vector.indexOf(node) ; }
      public boolean isLeaf(){ return false ;  }
      public void add( ListEntry entry ){
          if( this.vector.contains(entry) )return ;
          entry.parent = this ;
          this.vector.add( entry ) ;
          this.map.put( entry.name , entry ) ;
      }
      public void remove( ListEntry entry ){
         this.vector.remove(entry);
         this.map.remove(entry.name);
      }
      public void clear(){ this.vector.clear() ; }
      public ListEntry getChildByName( String name ){ return (ListEntry)this.map.get(name) ; }
      public void sort(){
         Collections.sort( this.vector ) ;
      }
   }
   public class PoolEntry extends ListEntry {
       private PoolEntry(String name ){ super(name) ; }
   }
   private class ListEntry implements Comparable, TreeNode {
   
      protected String   name           = "Realm" ;
      protected boolean  allowsChildren = false ;
      protected TreeNode parent         = null ;
      
      public ListEntry( String name ){ this.name = name ; }
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
      public boolean getAllowsChildren(){ return allowsChildren ; }
      public TreeNode getChildAt( int index ){ return null ; }
      public int getChildCount(){ return 0 ; }
      public int getIndex( TreeNode node ){ return 0 ; }
      public TreeNode getParent(){ return parent ; }
      public boolean isLeaf(){ return true ; }
      private void setParent( TreeNode parent ){ this.parent = parent ; }
   }
   public void setGroupLink( Map links , Map groupMap ){
   
       PoolGroupContainer pgContainer = new PoolGroupContainer("Pool Groups");
       
       for( Iterator l = groupMap.entrySet().iterator() ; l.hasNext() ; ){
         
          Map.Entry entry = (Map.Entry)l.next() ;
          String name = (String)entry.getKey();
          
          PoolGroupEntry poolGroup = new PoolGroupEntry( name ) ;
          pgContainer.add( poolGroup ) ;
          java.util.List   list = (java.util.List)entry.getValue() ;
          System.out.println("PoolGroup : "+name ) ;
          for( Iterator i = list.iterator() ; i.hasNext() ; ){
             String poolName = i.next().toString() ;
             poolGroup.add( new PoolEntry( poolName ) );
             System.out.println("    "+poolName);
          }
          poolGroup.sort() ;
       
       }
       pgContainer.sort();
       
       LinkContainer linkContainer = new LinkContainer("Links") ;
              
       LinkEntry linkEntry = null ;
       for( Iterator l = links.values().iterator() ; l.hasNext() ; ){
       
          PoolGroupLinkCollector.LinkEntry link = (PoolGroupLinkCollector.LinkEntry)l.next() ;
          
          linkContainer.add( linkEntry = new LinkEntry(link.getName()) ) ;
          
          System.out.println("Link : "+link.getName() ) ;
          
          Object [] groups = link.getPoolGroups();
          
          //PoolGroupContainer poolGroupContainer = new PoolGroupContainer("Pool Groups");
          //linkEntry.add( poolGroupContainer ) ;
          
          PoolGroupEntry singlePools = new PoolGroupEntry("Orphans") ;
          linkEntry.add( singlePools ) ;
          
          PoolGroupEntry resolvedPools = new PoolGroupEntry("Resolved Pools");
          linkEntry.add( resolvedPools ) ;
          
          System.out.println(" Groups");
          
          PoolGroupEntry poolGroupEntry = null ;
          for( int i = 0 ; i < groups.length ; i++ ){
          
             String poolGroupName = groups[i].toString() ;
             System.out.println("     "+poolGroupName);
             
             linkEntry.add( poolGroupEntry = new PoolGroupEntry( poolGroupName ) ) ;
             
             PoolGroupEntry entry = (PoolGroupEntry)pgContainer.getChildByName( poolGroupName ) ;
             
             for( Enumeration e = entry.children() ; e.hasMoreElements() ; ){
                 PoolEntry poolEntry = (PoolEntry)e.nextElement() ;
                 poolGroupEntry.add( new PoolEntry( poolEntry.getName() ) ) ;
             }
             poolGroupEntry.sort();
          }

          PoolEntry poolEntry = null ;
          Object [] pools = link.getPools();
          System.out.println(" Pools");
          for( int i = 0 ; i < pools.length ; i++ ){
          
             String poolName = pools[i].toString() ;
             
             singlePools.add( poolEntry = new PoolEntry( poolName ) ) ;
             System.out.println("     "+poolName);
          }
          singlePools.sort() ;
          
          pools = link.getResolvedPools();
          System.out.println(" Resolved");
          for( int i = 0 ; i < pools.length ; i++ ){
          
             String poolName = pools[i].toString() ;
             
             resolvedPools.add( poolEntry = new PoolEntry( poolName ) ) ;
             System.out.println("     "+poolName);
          }
          resolvedPools.sort();
       }
       linkContainer.sort(); 
       _root.clear() ;
        _root.add( pgContainer ) ;
        _root.add( linkContainer ) ;
       ((DefaultTreeModel)_tree.getModel()).setRoot( _root ) ;

   }
   public void treeWillExpand(  TreeExpansionEvent event ) throws ExpandVetoException {
      if( _primaryDone )return ;
      runUpdate() ;
      throw new ExpandVetoException( event , "Have to load first");
   }
   public void treeWillCollapse( TreeExpansionEvent event ){
      //System.err.println("Collapse : "+event.getPath());
   }
   public void valueChanged( TreeSelectionEvent event ){
      if( ! event.isAddedPath() ){
         fireEvent( (TreeNode)null);
         return ;
      }
      TreePath path = event.getPath();
      int count = path.getPathCount() ;
      Object obj = path.getLastPathComponent() ;
      if( ( obj instanceof LinkEntry ) ||
          ( obj instanceof PoolGroupEntry ) ||
          ( obj instanceof PoolEntry )         ){
         
           fireEvent((TreeNode)obj);
           
      }else{
         fireEvent( (TreeNode)null);
      }
 
   }
   public double getProgress(){
      return _collector.getProgress() ;
   }
   private void runUpdate(){
      fireEvent("updating");
      try{
          setEnabled(false);           
          _collector.collectData() ;
      }catch(IllegalStateException ise ){
             JOptionPane.showMessageDialog( this , ise , "Collector still in progess" , JOptionPane.ERROR_MESSAGE ) ;
      }catch(Exception e ){
             JOptionPane.showMessageDialog( this , e , "Exception : "+e , JOptionPane.ERROR_MESSAGE ) ;
      }
   
   }
   public void actionPerformed( ActionEvent event ){
      Object source = event.getSource() ;
      if( source == _updateButton ){
         runUpdate() ;
      }else if( source == _collector ){
          setEnabled(true);
          fireEvent("finished");

          PoolGroupLinkCollector.DataArrivedEvent data = (PoolGroupLinkCollector.DataArrivedEvent)event ;
          if( data.isError() ){
             JOptionPane.showMessageDialog( this , data.getErrorObject() , "Problem collecting data" , JOptionPane.ERROR_MESSAGE ) ;
          }else{
             _primaryDone = true ;
             setGroupLink( _collector.getLinkMap() , _collector.getPoolGroupMap() ) ;
          }
      
      }
   }
   public void domainAnswerArrived( Object obj , int id ){
   }
}
