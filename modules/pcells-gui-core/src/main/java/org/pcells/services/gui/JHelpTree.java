
// $Id: JHelpTree.java,v 1.2 2006/12/23 18:08:22 cvs Exp $

package org.pcells.services.gui ;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.*;

public class JHelpTree extends JTree {


   private static class IndexEntry {
      private int    _level = 0 ;
      private String _name  = null ;
      private String _resource = null ;
      private IndexEntry( String name , String resource , int level ){
         _level = level; _name = name  ; _resource = resource ;
      }
      private IndexEntry( String line ){
        scanLine(line) ;
      }
      public String toString(){ return "Name="+_name+";resource="+_resource+";level="+_level ;}
      private final static int IN_LEVEL = 1 ;
      private final static int GET_NAME = 2 ;
      private final static int GET_RESOURCE = 3 ;
      private void scanLine( String line ){
         int state = IN_LEVEL ;
         StringBuffer name = null , resource = null ;
         for( int i = 0 , n = line.length() ; i < n ; i++ ){
            char c = line.charAt(i);
            switch( state ){
               case IN_LEVEL :
                 if( Character.isWhitespace(c) )continue ;
                 if( c == '+' )_level++ ;
                 else {
                    state = GET_NAME ;
                    name = new StringBuffer() ;
                    name.append(c);
                 }
               break ;
               case GET_NAME :
                  if( c == ':' ){
                     resource = new StringBuffer() ;
                     state = GET_RESOURCE ;
                  }else{
                     name.append(c);
                  }
               break ;
               
               case GET_RESOURCE :
                 if( Character.isWhitespace(c) )continue ;
                 resource.append(c);                  
               break ;
            
            }
         }
         if( ( name == null ) || ( name.length() == 0 ))throw new IllegalArgumentException("Not a name");
         _name = name.toString() ;
         _resource = resource == null ? null : resource.toString() ;
      }
   }

   public class TreeEntry implements TreeNode {
      private String    _name   = null ;
      private TreeEntry _parent = null ;
      private String    _resource = null ;
      private Vector    _children = null ;
      public TreeEntry( String name ){
          _name = name ;
      }
      public void setResource( String resource ){ 
         _resource  = resource ;
      }
      public String getResource(){ return _resource  ;}
      public void addChild( TreeEntry entry ){
         entry.setParent(this) ;
         if( _children == null )_children = new Vector() ;
         _children.add(entry);
      }
      private void setParent( TreeEntry parent ){ _parent = parent ; }
      public Enumeration children(){ return _children == null ? null : _children.elements(); }
      public boolean getAllowsChildren(){ return _children != null ; }
      public TreeNode getChildAt( int index ){ return _children == null ? null : (TreeNode) _children.elementAt(index);  }
      public int getChildCount(){ return _children == null ? 0 : _children.size();  }
      public int getIndex( TreeNode node ){ return _children == null ? 0 : _children.indexOf(node) ; }
      public TreeNode getParent(){ return _parent ; }
      public boolean isLeaf(){return _children == null  ;}
      public String toString(){ return _name ; }
   
   }
   public static IndexEntry [] __entries = {
      new IndexEntry("Basics:generic.html"),
      new IndexEntry("Modules:generic.html"),
      new IndexEntry("+Commander:firstHelp.html"),
      new IndexEntry("+Queues (Restore and Transfer):firstHelp.html"),
      new IndexEntry("++Restore:oterh.html") ,
      new IndexEntry("++Transfer:oterh.html") ,
      new IndexEntry("+Cost Module:oterh.html") ,
      new IndexEntry("+Pool Commander:oterh.html") 
   };
   private TreeEntry _root = new TreeEntry("dCache Help") ;
   
   public JHelpTree(){
      super( new Hashtable() ) ;
//      addTreeSelectionListener(this);
      setRootVisible(false);
   }
   public void loadIndex( URL url ) throws IOException {
       Reader br = new InputStreamReader( url.openStream() );
       try{
          loadIndex( br ) ;
       }finally{
          try{ br.close() ; }catch(IOException ioe ){}
       }
   
   }
   public void loadIndex( Reader reader ) throws IOException {
       String    line = null ; 
       ArrayList list = new ArrayList() ; 
       BufferedReader br = new BufferedReader( reader ) ;       
       for( int i = 0 ; ( line = br.readLine() ) != null ; i++ ){
          line = line.trim() ;
          if( ( line.length() == 0 )   ||
              ( line.startsWith("#")  )   )continue ;
          try{
             list.add( new IndexEntry(line) ) ;
          }catch(IllegalArgumentException iae ){
             System.err.println("Problem in index reader (line "+i+") : "+iae);
          }
       }
       setTreeChain( (IndexEntry []) list.toArray( new IndexEntry[0] ) ) ;

   }
   private void setTreeChain( IndexEntry [] entries ){
      Stack    stack = new Stack() ;
      TreeEntry currentContainer = null , lastChild = null ;
      stack.push(currentContainer = _root);
      int level = -1 ;
      for( int i= 0 , n = entries.length ; i<n;i++ ){
      
         IndexEntry e = entries[i] ;
         TreeEntry te = new TreeEntry( e._name ) ;
         if( e._resource  != null )te.setResource(e._resource);
         
         int diff = e._level - level ;
         
//         System.out.println("Adding ("+diff+") "+e);
         if( diff == 1 ){
//            System.out.println("add to parent");
            currentContainer.addChild( lastChild = te ) ;
         }else if( diff > 1 ){
//            System.out.println("new level");
            stack.push(currentContainer) ;
            currentContainer = lastChild ;
            currentContainer.addChild( lastChild = te ) ;
            level++ ;
         }else{
            diff = - diff  + 1 ;
//            System.out.println("Back levels "+diff);
            for( int j = 0 ; j < diff ; j++ ){
//              lastChild = currentContainer ;
              currentContainer = (TreeEntry)stack.pop() ;
              level-- ;
            }
            currentContainer.addChild( lastChild = te ) ;
         }
      }
      setModel( new DefaultTreeModel( _root ) ) ;
      listChildren(_root);
   }
   private Stack _nodeStack = new Stack() ;
   private void listChildren( TreeNode node ){
       _nodeStack.push(node);
       TreeNode [] x = (TreeNode[])_nodeStack.toArray( new TreeNode[0] ) ;
       expandPath( new TreePath( x ) ) ;
       Enumeration c = node.children() ;
       while( c.hasMoreElements() ){
           TreeNode n = (TreeNode)c.nextElement() ;
           //System.err.println(""+n);
           if( n.getChildCount() > 0 )listChildren(n);
       }
       _nodeStack.pop();
   }
    public static void main(String s[]) throws Exception  {
    
        if( s.length < 1 ){
           System.err.println("Usage : ... <indexFile>") ;
           System.exit(4);
        }
        WindowListener l = new WindowAdapter() {
            public void windowClosing(WindowEvent e) {System.exit(0);}
        };

       JFrame f = new JFrame("JHelpTree - Demo");
       f.addWindowListener(l);
       JHelpTree help = new JHelpTree() ;
       help.loadIndex(  new URL( s[0] ) ) ;
       
       f.getContentPane().add("Center", help );
       f.pack();
       f.setSize(new Dimension(900,500));
       f.setVisible(true);
    }
}
