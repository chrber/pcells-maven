// $Id: SelectionCommander.java,v 1.6 2006/12/23 18:06:01 cvs Exp $
//
package org.pcells.services.gui ;
//

import dmg.cells.network.CellDomainNode;
import dmg.cells.nucleus.CellInfo;
import org.pcells.services.connection.DomainConnection;
import org.pcells.services.connection.DomainConnectionListener;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

public class      SelectionCommander 
       extends    CellGuiSkinHelper.CellPanel 
       implements ActionListener, 
                  DomainConnectionListener,
                  ListSelectionListener,
                  TreeWillExpandListener,
                  TreeSelectionListener {

   private DomainConnection _connection   = null ;
   private EasyCommander    _commander    = null ;
   private TitleList        _domainList   = null ;
   private TitleList        _cellList     = null ;
   private JButton          _updateCells  = null ;
   private JButton          _toggleViews  = null ;
   private HashMap          _domainMap    = null ;
   private TreeRoot         _treeRoot     = new TreeRoot() ;
   private JTree            _tree         = null ;  
   private CardLayout       _cards        = null ;
   private JPanel           _cardsPanel   = null ;
   private int              _toggleMode   = 0 ;
   
   public SelectionCommander( DomainConnection connection ){
      _connection = connection ;
      
      setLayout( new BorderLayout( 10 , 10 ) ) ;
      
      
      GridLayout l = new GridLayout(2,0);
      l.setVgap(10) ;
      l.setHgap(10);
      
      _domainList  = new TitleList("Domains") ;
      _cellList    = new TitleList("Cells");
      _updateCells = new CellGuiSkinHelper.CellButton("Update Cells");
      _toggleViews = new CellGuiSkinHelper.CellButton("Toggle Views");
      
      _domainList._list.addListSelectionListener(this);
      _cellList._list.addListSelectionListener(this);
      _updateCells.addActionListener(this);
      _toggleViews.addActionListener(this);
      
      _tree  = new JTree( _treeRoot ) ;
      _tree.addTreeWillExpandListener(this);
      _tree.addTreeSelectionListener(this);
      _tree.setCellRenderer( new CustomBasicRenderer2() ) ;
//      _tree.setCellRenderer( new CustomBasicRenderer() ) ;

      _cards      = new CardLayout() ;
      _cardsPanel = new CellGuiSkinHelper.CellPanel(_cards) ;
      
      
      JSplitPane listPanel = new JSplitPane(
                               JSplitPane.VERTICAL_SPLIT ,
                               _domainList ,
                               _cellList  ) ;
                               

      CellGuiSkinHelper.setComponentProperties( listPanel ) ;
      
      listPanel.setDividerLocation(0.5);

      JScrollPane treeScroll = new JScrollPane( _tree ) ;
      CellGuiSkinHelper.setComponentProperties( treeScroll ) ;
      
      _cardsPanel.add( listPanel , "lists" ) ;
      _cardsPanel.add( treeScroll , "tree" ) ;
      
      _cards.show( _cardsPanel , "tree" ) ;
      
      JPanel leftPanel = new CellGuiSkinHelper.CellPanel( new BorderLayout(4,4) ) ;
      leftPanel.add(_cardsPanel , "Center" ) ;

      l = new GridLayout(2,0);
      l.setVgap(4) ;
      l.setHgap(4);
      
      JPanel leftButtonPanel = new CellGuiSkinHelper.CellPanel( l ) ;
      
      leftButtonPanel.add(_updateCells) ;
      leftButtonPanel.add(_toggleViews) ;
      
      leftPanel.add( leftButtonPanel , "South" ) ;
      
      _commander = new EasyCommander( _connection ) ;
      
      
      JSplitPane split = new JSplitPane(
                               JSplitPane.HORIZONTAL_SPLIT ,
                               leftPanel ,
                               _commander  ) ;
      
      CellGuiSkinHelper.setComponentProperties( split ) ;
                              
      add( split , "Center" ) ;
      split.resetToPreferredSizes();
   }
   private class CustomBasicRenderer2 extends DefaultTreeCellRenderer {
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
//          System.out.println("Rendering : "+c);
          if( c instanceof JLabel ){
             JLabel label = (JLabel)c ;
             Icon icon = label.getIcon() ;
             if( value instanceof CellFrame ){
                String name = value.toString() ;
                label.setForeground( 
                         name.equals("System") ?
                         Color.red :
                         Color.green    ) ;
             }else if( value instanceof Frame ){
                Frame f = (Frame)value ;
                label.setForeground( f.cellinfos == null ? Color.red : Color.green) ;
             }else{
//                System.out.println("Rendering for "+value);
             }
             if( selected ){
                 label.setBackground(Color.yellow); 
//                 System.err.println("Settin background to Yellow for "+label ) ;
             }        
          }else{
                System.out.println("x Rendering for "+c);
          }
              
          return c ;
      }
   }
   private class CustomBasicRenderer extends DefaultTreeCellRenderer {

     private DomainIcon _domainIcon = new DomainIcon(false) ;
     private DomainIcon _loadedIcon = new DomainIcon(true) ;
     private CellIcon   _cellIcon   = new CellIcon(14,14) ;
     private CellIcon   _systemIcon = new CellIcon(14,14,Color.red);
     private class CellIcon implements Icon {
        private int _height = 0 ;
        private int _width  = 0 ;
        private Color _color  = new Color( 0 , 0 , 255 ) ;
        private CellIcon( int width , int height ){
           _height = height ;
           _width  = width ;
        }
        private CellIcon( int width , int height , Color color ){
           _height = height ;
           _width  = width ;
           if( color != null )_color  = color ;
        }
        public void paintIcon( Component c , Graphics gin , int xi , int yi ){
           Graphics2D g = (Graphics2D) gin ;
           g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                              RenderingHints.VALUE_ANTIALIAS_ON);
           
           g.setColor(_color);
           g.fillOval(xi+2,yi+2,_width-4,_height-4);
        }
        public int getIconWidth(){ return _width ; }
        public int getIconHeight(){ return _height ; }
     }
     private class DomainIcon implements Icon {
        private int _height = 0 ;
        private int _width  = 0 ;
        private Color _color  = new Color( 0 , 0 , 255 ) ;
        private boolean _loaded = false ;
        private DomainIcon( boolean loaded ){
          _loaded = loaded ;
        }
        public void paintIcon( Component c , Graphics gin , int xi , int yi ){
           Graphics2D g = (Graphics2D) gin ;
           g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                              RenderingHints.VALUE_ANTIALIAS_ON);
           
           g.setColor(_color);
           g.fillOval(xi+2,yi+2,10,10);
           g.setColor(_loaded?Color.green:Color.red);
           g.fillOval(xi+2+3,yi+2+3,4,4);
        }
        public int getIconWidth(){ return 14 ; }
        public int getIconHeight(){ return 14 ; }
     }
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
//          System.out.println("Rendering : "+c);
          if( c instanceof JLabel ){
             JLabel label = (JLabel)c ;
             Icon icon = label.getIcon() ;
             if( value instanceof CellFrame ){
                String name = value.toString() ;
                label.setIcon( 
                         name.equals("System") ?
                         _systemIcon :
                         _cellIcon    ) ;
             }else if( value instanceof Frame ){
                Frame f = (Frame)value ;
                label.setIcon( f.cellinfos == null ? _domainIcon : _loadedIcon) ;
             }else{
//                System.out.println("Rendering for "+value);
             }         
          }else{
                System.out.println("x Rendering for "+c);
          }
              
          return c ;
      }
   }
   public Insets getInsets(){ return new Insets(10,10,10,10) ; }
   private class TitleList extends CellGuiSkinHelper.CellPanel {
   
      private JScrollPane  _scroll = null ;
      private TitledBorder _border = null ;
      private DefaultListModel _model = new DefaultListModel();
      private JList            _list  = new JList(_model) ;
      
      private TitleList(String title ){

         setLayout( new BorderLayout(10,10)) ;
         
         _list.setPrototypeCellValue("it-dcache0-0Domain");
         _list.setSelectionMode( ListSelectionModel.SINGLE_INTERVAL_SELECTION ) ;
         _scroll = new JScrollPane( _list ) ;
         add( _scroll , "Center" ) ;
         
         _border = BorderFactory.createTitledBorder(title) ;
         setBorder( _border ) ;
      }
      public void setBorderTitle( String title ){
         _border.setTitle( title ) ;
         repaint();
      }
      public void setEnabled( boolean enabled ){
         _list.setEnabled(enabled);
      }
   }
   private class CellFrame extends ListEntry {
      private String address = null ;
      private CellInfo info  = null ;
      private CellFrame( Frame domain , String name , String address , CellInfo info ){
         super(domain,true);
         this.name    = name ;
         this.address = address ;
         this.info    = info ;
      }
   }
   private class Frame extends ListEntry {
      private CellDomainNode node      = null ;
      private ArrayList      cellinfos = null ;
      public Frame( TreeNode root , CellDomainNode node ){
          super(root,true);
          this.node = node ;
          this.name = node.getName();
      }
      public Enumeration children(){ 
          return cellinfos == null ? null : new Vector(cellinfos).elements() ; 
      }
      public boolean getAllowsChildren(){ return true ; }
      public TreeNode getChildAt( int index ){ return (TreeNode)cellinfos.get(index) ; }
      public int getChildCount(){ return cellinfos == null ? 0 : cellinfos.size() ; }
      public int getIndex( TreeNode node ){ return cellinfos.indexOf(node) ; }
      public TreeNode getParent(){ return parent ; }
      public boolean isLeaf(){ return false ; }
   }
   private class TreeRoot extends ListEntry {
      private Vector vector = new Vector() ;
      private TreeRoot(){
         super(null,true);
      }
      public void setVector( Vector vector ){
         this.vector = vector ;
      }
      public Enumeration children(){ return vector.elements() ;  }
      public boolean getAllowsChildren(){ return true ; }
      public TreeNode getChildAt( int index ){ return (TreeNode)vector.get(index) ; }
      public int getChildCount(){ 
//         System.out.println("Root : asking for count : "+vector.size());
         return vector.size() ; 
      }
      public int getIndex( TreeNode node ){ return vector.indexOf(node) ; }
      public boolean isLeaf(){ 
//         System.out.println("Root : isLeaf");
         return false ; 
      }
   }
   private class ListEntry implements Comparable, TreeNode {
   
      protected String name           = "Realm" ;
      private boolean  allowsChildren = false ;
      protected TreeNode parent         = null ;
      
      public ListEntry( TreeNode parent , boolean allowsChildren ){
        this.allowsChildren = allowsChildren ; 
        this.parent = parent ;
      }
      public String toString(){ return name ; }
      public int hashCode(){ return name.hashCode() ; }
      public int compareTo( Object other ){
         return name.compareTo(other.toString());
      }
      public boolean equals( Object other ){
//         System.out.println("Comparing : "+name+" "+other);
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
   public void setDomain( String domainName ){
      if( _domainMap == null )return ;
      Frame domain = (Frame)_domainMap.get(domainName);
      if( domain == null )return ;
//      System.out.println("Selected : "+domain+" model "+_domainList._model.size());
      _domainList._list.setSelectedValue(domain,true);
   }
   public void setTopology( java.util.List list ){
      HashMap map = new HashMap() ;
      for( Iterator i = list.iterator() ; i.hasNext() ; ){
         CellDomainNode node = (CellDomainNode)i.next() ;
         map.put( node.getName() , new Frame(_treeRoot,node) ) ;
      }
      _domainList._model.removeAllElements();
      _cellList._model.removeAllElements();
      
      TreeSet set = new TreeSet( map.values() ) ;
      for( Iterator i = set.iterator() ; i.hasNext() ; ){
         _domainList._model.addElement(i.next());
      }
      Vector vector = new Vector( set );
      _treeRoot.setVector(vector);
      _domainMap = map ;
      _tree.treeDidChange();
      ((DefaultTreeModel)_tree.getModel()).nodeChanged(  _treeRoot ) ;
      ((DefaultTreeModel)_tree.getModel()).nodeStructureChanged(  _treeRoot ) ;
      
   }
   public void actionPerformed( ActionEvent event ){
      Object source = event.getSource() ;
      if( source == _updateCells ){
         Frame domain = (Frame)_domainList._list.getSelectedValue();
         if( domain == null )return ;
         updateCellListFor( domain ) ;  
      }else if( source ==  _toggleViews ){
         if( _toggleMode == 0 ){
           _cards.show( _cardsPanel , "lists" ) ;
         }else{
           _cards.show( _cardsPanel , "tree" ) ;
         }
         _toggleMode = ( _toggleMode + 1 ) % 2 ;
      }
    }
   private boolean _useShortAddresses = true ;
   public void setEnabled(boolean enabled ){
      _updateCells.setEnabled(enabled);
      _domainList.setEnabled(enabled);
      _cellList.setEnabled(enabled);
      _tree.setEnabled(enabled);
   }
   public void domainAnswerArrived( Object obj , int id ){
     try{
      if( id == CELLINFOS ){
         if( obj instanceof CellInfo []  ){
             Frame domain = (Frame)_domainList._list.getSelectedValue();
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
             loadCells(domain);
             ((DefaultTreeModel)_tree.getModel()).reload(  domain ) ;
         }
         setEnabled(true);
      }
    }catch(Exception e){
      e.printStackTrace();
    }
   }
   private void loadCells( Frame domain ){
      _cellList.setBorderTitle(domain.getName());
      _cellList._model.removeAllElements();
      ArrayList list = domain.cellinfos ;
      CellFrame system = null ;
      for( Iterator i = list.iterator() ; i.hasNext() ; ){
         CellFrame cf = (CellFrame)i.next() ;
         if( cf.getName().equals("System") )system = cf ;
         _cellList._model.addElement(cf);
      }
      if( system != null )_cellList._list.setSelectedValue(system,true);
   }
   private static final int CELLINFOS = 1001 ;
   private void updateCellListFor( Frame frame ){
      setEnabled(false);
      try{
         _connection.sendObject( frame.node.getAddress() ,
                                 "getcellinfos" , 
                                 this ,
                                 CELLINFOS );
      }catch(Exception ee){
         _updateCells.setEnabled(true);
         _domainList.setEnabled(true);
         ee.printStackTrace() ;
      }
   }
   public void valueChanged( ListSelectionEvent event ){
      System.err.println("Value changed (List) : "+event);
      if( event.getValueIsAdjusting() )return ;
      Object source = event.getSource() ;
      if( source == _domainList._list ){
         Frame domain = (Frame)_domainList._list.getSelectedValue();
         if( domain == null )return ;
         if( domain.cellinfos != null )loadCells(domain);
         else updateCellListFor( domain ) ;    
      }else if( source == _cellList._list ){
         CellFrame cell = (CellFrame)_cellList._list.getSelectedValue();
         if( cell == null )return ;
          _commander.setDestination( cell.address ) ;
      }
   }
   public void treeWillCollapse( TreeExpansionEvent event ){
      System.err.println("Collapse : "+event.getPath());
   }
   public void treeWillExpand(  TreeExpansionEvent event ){
      System.err.println("Will Expand : "+event.getPath());


         JOptionPane.showMessageDialog(
                 this ,
                
                 "Waiting"  ,
                 "Destination Cell Not Found" ,
                 JOptionPane.ERROR_MESSAGE ) ;

//      try{ Thread.sleep(5000L) ; }catch(Exception ee ){}
      TreePath path = event.getPath();
      if( path.getPathCount() < 2 )return ;
      Frame domain = (Frame)path.getPathComponent(1);
      if( domain == null )return ;
      _domainList._list.setSelectedValue(domain,true);      
      
   }
   public void valueChanged( TreeSelectionEvent event ){
      System.err.println("Value changed (Tree) : "+event);
      TreePath path = event.getPath();
      int count = path.getPathCount() ;
      if( count < 2 )return ;

      
      Frame domain = (Frame)path.getPathComponent(1);
      if( domain == null )return ;
      _domainList._list.setSelectedValue(domain,true); 
      
      if( count < 3 )return ;
      
      CellFrame cell = (CellFrame)path.getPathComponent(2) ;
      if( cell == null )return ;
      _cellList._list.setSelectedValue(cell,true); 
           
   }
}
