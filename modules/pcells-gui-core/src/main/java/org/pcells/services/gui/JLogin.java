// $Id: JLogin.java,v 1.2 2004/06/21 22:30:27 cvs Exp $
//
package org.pcells.services.gui ;

import org.pcells.services.connection.DomainConnection;
import org.pcells.services.connection.DomainEventListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.util.StringTokenizer;

import org.dcache.util.Args;

public class JLogin extends JFrame {

    private javax.swing.Timer _timer = null ;
    private JSshLoginPanel   _login  = new JSshLoginPanel("Desy-dCache") ;
    private DomainConnection _domain = _login.getDomainConnection() ;
    private JTabbedPane      _tab    = new JTabbedPane() ;
    
    public JLogin( String title , String [] argvector ) {
    
        super( title ) ;
        setBackground(Color.blue);
        final Container c = getContentPane() ;
        
        Args args = new Args( argvector ) ;
        String tmp  = args.getOpt("host") ;
        
        if( tmp != null )_login.setHostname(tmp) ;
        tmp = args.getOpt("port") ;
        if( tmp != null )_login.setPortnumber(tmp);

        Class  [] classArgs  = { org.pcells.services.connection.DomainConnection.class } ;
        Object [] objectArgs = { _domain } ;
        
        if( args.argc() > 0 ){
           for( int i = 0 , j = args.argc() ; i < j ; i++ ){
              try{
                  StringTokenizer st = new StringTokenizer( args.argv(i) , "=") ;
                  String name      = st.nextToken() ;
                  String className = st.nextToken() ;
                  Class  cn        = Class.forName( className ) ;
                  Constructor cc   = cn.getConstructor( classArgs ) ;
                  
                  JPanel cp = (JPanel) cc.newInstance( objectArgs ) ;
                  
                  _tab.addTab( "   "+name+"   " , cp ) ;
                  
                  
              }catch(Exception e){
                  System.err.println("Can't init "+args.argv(i)+" : "+e);
                  continue ;
              }
           }
        }else{
//           _tab.addTab( "   Commander   " , new JCommander( _domain ) ) ;
//           _tab.addTab( "   Spy   "       , new JSpyPanel( _domain ) ) ;
//           _tab.addTab( "   Realm   "     , new JRealm( _domain ) ) ;
        }
        _tab.setSelectedIndex(0);
        c.setLayout( new GridBagLayout() ) ;
        
        c.add( _login ) ;
        
       _domain.addDomainEventListener(
        
           new DomainEventListener(){
              public void connectionOpened( DomainConnection connection ){
                 System.out.println("Connection opened");
                 c.removeAll();
                 c.setLayout( new BorderLayout() ) ;
                 c.add( _tab , "Center" ) ;
                 c.invalidate() ;
                 c.doLayout() ;
                 repaint();
              }
              public void connectionClosed( DomainConnection connection ){
                 System.out.println("Connection closed" ) ;
                 c.removeAll() ;
                 c.setLayout( new GridBagLayout() ) ;
                 c.add( _login ) ;
                 c.invalidate() ;
                 c.doLayout() ;
                 repaint();
              }
              public void connectionOutOfBand( DomainConnection connection, Object obj ){
                 System.out.println("Connection connectionOutOfBand" ) ;
              }
           }
        );
        
    }
    
    public static void main(String argv[]) throws Exception  {

       PrintStream devnull = new PrintStream( new FileOutputStream( "/dev/null" ) ) ;
//       System.setErr( devnull ) ;
//       System.setOut( devnull ) ;

       JLogin f = new JLogin("Cell Login" , argv );
       
       f.pack();
       Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
       int w = 200;
       int h = 200;
       f.setLocation(100,100);
       f.setSize(600,400);
       f.setVisible(true);
   }
}
