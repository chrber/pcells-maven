// $Id: JSelectionHelpFrame.java,v 1.3 2006/12/23 18:04:07 cvs Exp $
//
package org.pcells.services.gui ;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.Document;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.Reader;
import java.net.URL;


/**
 */
public class JSelectionHelpFrame extends JFrame implements TreeSelectionListener {

    private JSplitPane  _master     = null ; 
    private JScrollPane _scrollPane = null ;
    private JHelpTree   _tree       = null ;
    private JEditorPane _htmlDoc    = null ;
    private Document    _document   = null ;

    public JSelectionHelpFrame( String title , URL url ) throws Exception {
       super(title);
       
       _tree = new JHelpTree() ;
       _tree.loadIndex( url ) ;
       _tree.addTreeSelectionListener(this);
       _htmlDoc = new JEditorPane() ;


       _master = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT , 
                                 _tree ,
                                 _scrollPane = new JScrollPane(_htmlDoc) ) ;
                                  
       _master.setBorder( new CellBorder( "Cell Login Help" , 30 ) ) ;
       _master.setDividerLocation(0.5);
       _htmlDoc.setContentType( "text/html" ) ;
       _htmlDoc.setEditable(false);
       _document = _htmlDoc.getEditorKit().createDefaultDocument() ;
       _htmlDoc.setDocument(_document);
       getContentPane().add( _master );

    }
    public JSelectionHelpFrame( String title , Reader reader ) throws Exception {
       super(title);
       
       _tree = new JHelpTree() ;
       _tree.loadIndex( reader ) ;
       try{ reader.close() ; }catch(Exception ee ){}
       _tree.addTreeSelectionListener(this);
       _htmlDoc = new JEditorPane() ;


       _master = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT , 
                                  new JScrollPane( _tree ),
                                 _scrollPane = new JScrollPane(_htmlDoc) ) ;
                                  
       _master.setBorder( new CellBorder( "Cell Login Help" , 30 ) ) ;
       _master.setDividerLocation(0.5);
       _htmlDoc.setContentType( "text/html" ) ;
       _htmlDoc.setEditable(false);
       _document = _htmlDoc.getEditorKit().createDefaultDocument() ;
       _htmlDoc.setDocument(_document);
       getContentPane().add( _master );

    }
   private String __noresource =
       "<html><body bgcolor=gray><center><h1>pCells help facility for dCache</h1></center></body></html>";
   public void valueChanged( TreeSelectionEvent event ){
      TreePath path = event.getPath();
      int count = path.getPathCount() ;
      if( count < 1 )return ;
      
      JHelpTree.TreeEntry e = (JHelpTree.TreeEntry)path.getPathComponent(count-1);
      String resource = e.getResource() ;
      
      System.out.println("Setting resource : "+resource ) ;

      try{
         if( _document == null ){
            _document = _htmlDoc.getEditorKit().createDefaultDocument() ;
            _htmlDoc.setDocument(_document);
         }
         if( resource == null )
            throw new
            IllegalArgumentException("Resource not specified for : "+e);
            
         if( resource.indexOf(':') < 0 ){
            _htmlDoc.setPage( getClass().getResource(resource) ) ;
         }else{
            _htmlDoc.setPage( resource );
         }
      }catch(Exception io ){
         System.err.println("Problem getting resource for : "+e+"("+io+")");
         _htmlDoc.setText( __noresource ) ;
         _document = null ;
      }  
   }
    public static void main(String s[])throws Exception {
        if( s.length < 1 ){
           System.err.println("Usage : ... <indexUrl>") ;
           System.exit(4);
        }
        WindowListener l = new WindowAdapter() {
            public void windowClosing(WindowEvent e) {System.exit(0);}
        };
        JSelectionHelpFrame f = new JSelectionHelpFrame("LocationManager Help Tool",new URL(s[0]));
        f.addWindowListener(l);

        f.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int w = 200;
        int h = 200;
        f.setLocation(100,100);
        f.setSize(900,400);
        f.setVisible(true);
    }
}
 
