// $Id: JHelpFrame.java,v 1.2 2004/06/21 22:30:27 cvs Exp $
//
package org.pcells.services.gui ;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.Document;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.StringReader;
import java.net.URL;


/**
 */
public class JHelpFrame extends JFrame {

    private JPanel      _master     = null ; 
    private JScrollPane _scrollPane = null ;

    public JHelpFrame( String title , URL url ) throws Exception {
       super(title);
       _master = new JPanel( new BorderLayout(4,4) ) ;
       _master.setBorder( new CellBorder( "Cell Login Help" , 30 ) ) ;

       
       JEditorPane htmlDoc = new JEditorPane() ;
       _master.add( _scrollPane = new JScrollPane(htmlDoc) ) ;

       htmlDoc.setContentType( "text/html" ) ;
       htmlDoc.setPage(url);
       htmlDoc.setEditable(false);

       getContentPane().add( _master );

    }
    public JHelpFrame( String title , String text ) throws Exception {
       super(title);
       JEditorPane htmlDoc = new JEditorPane() ;
       htmlDoc.setContentType( "text/html" ) ;
       Document doc = htmlDoc.getEditorKit().createDefaultDocument() ;
       htmlDoc.setDocument(doc);
       htmlDoc.setEditable(false);
       StringReader reader = new StringReader(text);
       try{
          htmlDoc.read( reader , doc ) ;
       }catch(Exception ee ){
       
       }
       getContentPane().add( new JScrollPane(htmlDoc));
    }
    public static void main(String argv[]) {
        WindowListener l = new WindowAdapter() {
            public void windowClosing(WindowEvent e) {System.exit(0);}
        };
        LmSetupHelp f = new LmSetupHelp("LocationManager Help Tool");
        f.addWindowListener(l);

        f.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int w = 200;
        int h = 200;
        f.setLocation(100,100);
        f.setSize(600,400);
        f.setVisible(true);
    }
}
