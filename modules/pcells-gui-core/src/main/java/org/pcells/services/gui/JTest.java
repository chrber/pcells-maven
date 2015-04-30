
package org.pcells.services.gui ;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.URL;

public class JTest extends JPanel implements TreeSelectionListener{

   public void valueChanged( TreeSelectionEvent event ){
      TreePath path = event.getPath();
      int count = path.getPathCount() ;
      if( count < 1 )return ;
      
      for( int i = 0 ; i < count ; i++ ){
         JHelpTree.TreeEntry e = (JHelpTree.TreeEntry)path.getPathComponent(i);
         System.out.println("  "+i+"  "+e+" ; resource : "+e.getResource() );
      }
           
   }

       public JTest(Frame frame , String [] args ) throws IOException{
          JHelpTree help = new JHelpTree( ) ;
          help.addTreeSelectionListener(this);
          help.loadIndex(  new URL( args[0] ) ) ;
          add( help ,"Center" ) ;
          /*
          JHistoryComboBox h = new JHistoryComboBox();
          h.setMaxItemCount(5);
          h.addTopItem("PnfsManager");
          h.addTopItem("PoolManager");
          add(h ) ;
          Window w = new Window(frame);
          w.add( new JButton("Hallo") ) ;
          w.pack();
          w.setSize(new Dimension(900,500));
          w.setVisible(true);
          */
          
       }
/*
       public void paintComponent( Graphics gin ){
           Graphics2D g = (Graphics2D) gin ;
           g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                              RenderingHints.VALUE_ANTIALIAS_ON);
           
          Dimension d = getSize();                   
          Icon icon = new CellIcon( d.width , d.height ) ;
          icon.paintIcon( this , g , 0 , 0 );


       }
*/

    public static void main(String s[]) throws Exception  {
        if( s.length < 1 ){
           System.err.println("Usage : ... <indexUrl>") ;
           System.exit(4);
        }
        System.out.println("Starting ...");
        WindowListener l = new WindowAdapter() {
            public void windowClosing(WindowEvent e) {System.exit(0);}
 //           public void windowDeiconified(WindowEvent e) { demo.surf.start(); }
 //           public void windowIconified(WindowEvent e) { demo.surf.stop(); }
        };



       JFrame f = new JFrame("Java2D Demo - MemoryMonitor");
       f.addWindowListener(l);
//       f.getContentPane().add("Center", new JTest() );
       /*
       Preferences userRoot = Preferences.userRoot() ;
       JMLSetupPanel x = new JMLSetupPanel("Demo",userRoot.node("JTest"))  ;
       x.addActionListener(
          new ActionListener(){
            public void actionPerformed(ActionEvent event ){
               System.out.println(event.toString());
               System.out.println("Source : "+event.getSource().toString());
            } 
          }
       );
       */
       
//       JMonoLogin x = new JMonoLogin("Demo",userRoot.node("JTest"))  ;
       
       f.getContentPane().add( new JTest(f,s),  "Center" );
       f.pack();
       f.setSize(new Dimension(900,500));
       f.setVisible(true);
    }
}
