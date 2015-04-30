// $Id: JMultiLogin.java,v 1.21 2008/06/30 07:22:44 cvs Exp $
//
package org.pcells.services.gui ;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.Appender;
import org.pcells.services.gui.util.DrawBoardFrame;
import org.pcells.util.AppPropertyProvider;
import org.pcells.util.CellGuiClassLoader;
import org.pcells.util.ClassLoaderPreferences;
import org.pcells.util.Logable;
import org.slf4j.LoggerFactory;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

//

/**
 */
public class JMultiLogin extends JFrame implements ActionListener, MenuListener {

    private org.slf4j.Logger _logger = LoggerFactory.getLogger(VersionUpdate.class);

    public static CellGuiClassLoader __classLoader = CellGuiClassLoader.__classLoader  ;
    private static Font              __bigFont     = new Font( "Times" , Font.BOLD | Font.ITALIC , 26 ) ;

    private Preferences           _userRoot = Preferences.userRoot() ;
    private Preferences           _ourRoot  = _userRoot.node("CellLogin") ;
    private JMenuItem        _newMenuItem   = new JMenuItem("New ... ") ;
    private JMenuItem        _closeMenuItem = new JMenuItem("Close") ;
    private JMenuItem       _removeMenuItem = new JMenuItem("Remove") ;
    private JMenuItem        _exitMenuItem  = new JMenuItem("EXIT") ;
    private HelpMenuItem _aboutMenuItem     = null ;
    private JMenuItem _propertyMenuItem     = null ;
    private JMenuItem _showConsoleMenuItem  = null ;
    private JMenuItem _classLoaderMenuItem  = null ;
    private JMenuItem _drawBoardMenuItem    = null ;
    private JMenuItem _skinMenuItem         = null ;
    private JMenu     _openMenu    = null ;
    private JMenu  _destroyMenu    = null ;
    private JMenu     _fileMenu    = new JMenu("Session");
    private JMenu     _windowsMenu = new JMenu("Windows");
    private JMenu     _extraMenu   = new JMenu("Specials");
    private JMenu     _helpMenu    = new JMenu("Help");
    private JMenu     _newVersion  = new JMenu("(New version available)");
    private NameDialog _nameDialog = new NameDialog(this);
    private CardLayout _cardLayout = null ;
    private Container  _container  = null ;
    private String []  _argvector  = null ;
    private HashMap    _components = new HashMap() ;
    private VersionUpdate _version = null ;
    private JMenuBar      _menuBar = new JMenuBar() ;
    private Component    _myParent = this ;
    static Color blue   = new Color(94, 105, 176);
    static Color start  = new Color(255-94, 255-105, 255-176);
    static Color myGray = new Color( 50 , 50 , 50 ) ;


    private HelloWindow _helloWindow = null ;

    public class HelloWindowComponentListener extends ComponentAdapter {
        public void componentHidden( ComponentEvent event ){
            JMultiLogin.this.setVisible(true);
        }
    }
    public JMultiLogin( String title , String [] argvector ) {

        super( title ) ;

        _helloWindow = new HelloWindow( this ) ;
        _helloWindow.addComponentListener( new HelloWindowComponentListener() ) ;

        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(Exception cnf ){
            _logger.error("Native look and feel class not found or can't be initialized");
        }
        //
        // Java 1.4 Temporary BUG FIX
        //
        UIManager.put("TabbedPane.tabsOpaque"   , Boolean.FALSE);
        UIManager.put("TabbedPane.contentOpaque", Boolean.FALSE);
        /*
         * load preferences
         */
        try{
            String [] keys = _ourRoot.keys() ;
            for( int i = 0 , n = keys.length ; i < n ; i++ ){
                String prop = _ourRoot.get(keys[i],null) ;
                if( ( prop != null ) &&
                        ( System.getProperty(keys[i]) == null ) )

                    System.setProperty( keys[i] , prop ) ;
            }
        }catch(Exception ee ){
            _logger.error("Problems loading preferences : "+ee ) ;
        }
        /*
         */
        _propertyMenuItem     = new PropertyMenuItem("Properties") ;
        _showConsoleMenuItem  = new ConsoleMenuItem("Console") ;
        _classLoaderMenuItem  = new ClassLoaderMenuItem("ClassLoader") ;
        _drawBoardMenuItem    = new DrawBoardMenuItem("Story Board") ;
        _skinMenuItem         = new SkinMenuItem("Skin") ;
        /*
         *    Prepare the flush window
         *
         */
        ArrayList jarList = new ArrayList() ;
        jarList.add("Loading Jars : ");
        String offset = "pcells-gui-";
        for( Iterator it = __classLoader.jars() ; it.hasNext() ; ){
            CellGuiClassLoader.GuiJarEntry e = (CellGuiClassLoader.GuiJarEntry)it.next() ;
            String name = e.getName() ;
            if( name.startsWith(offset) ){
                jarList.add( "    "+name.substring(offset.length()) ) ;
            }else{
                jarList.add( "    "+name ) ;
            }
        }
        String [] stringList = (String[])jarList.toArray( new String[0] ) ;

        _helloWindow.setStringList( stringList ) ;

        _argvector  = argvector ;

        _container  = getContentPane() ;
        _cardLayout = new CardLayout() ;
        _container.setLayout( _cardLayout ) ;

        _version = new VersionUpdate() ;

        _helloWindow.setVersion( _version ) ;

        generateAboutText() ;

        _menuBar.add(_fileMenu);
        _menuBar.add(_windowsMenu);
        _menuBar.add(_extraMenu);

        try{
            _menuBar.setHelpMenu(_helpMenu);
        }catch(Throwable error ){
            _menuBar.add(_helpMenu) ;
        }

        fillHelpMenu( _helpMenu ) ;

        _windowsMenu.addMenuListener(this);
        _newMenuItem.addActionListener(this) ;
        _closeMenuItem.addActionListener(this) ;
        _removeMenuItem.addActionListener(this) ;
        _exitMenuItem.addActionListener(this) ;

        _openMenu = new JMenu("Open ...");
        _destroyMenu = new JMenu("Destroy ...");

        _aboutMenuItem=new HelpMenuItem("A b o u t ...",_aboutText) ;
        _aboutMenuItem.setFrameSize( 500 , 200 ) ;

        _fileMenu.add(_aboutMenuItem);
        _fileMenu.addSeparator();
        _fileMenu.add(_newMenuItem);
        _fileMenu.add(_openMenu);
        _fileMenu.addSeparator();
        _fileMenu.add(_closeMenuItem);
        _fileMenu.add(_removeMenuItem);
        _fileMenu.add(_destroyMenu);
        _fileMenu.add(_exitMenuItem);
        _closeMenuItem.setEnabled(false);
        _removeMenuItem.setEnabled(false);

        _extraMenu.add(_showConsoleMenuItem);
        _extraMenu.add(_skinMenuItem);
        _extraMenu.add(_propertyMenuItem);
        _extraMenu.add( _classLoaderMenuItem ) ;
        _extraMenu.add( _drawBoardMenuItem ) ;

        setJMenuBar(_menuBar) ;

        _container.add( new DrawPanel() , "drawing" )  ;

        prepareOpenMenu() ;

        JFrame f = this ;
        f.pack();
        f.addWindowListener(
                new WindowAdapter() {
                    public void windowClosing( WindowEvent event ){
                        System.exit(0);
                    }

                }
        ) ;

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int w = 200;
        int h = 200;
        f.setLocation(100,100);
        f.setSize(800,600);
        f.toFront();
        f.requestFocus();

    }
    private class OpenMenuListener implements ActionListener {
        private boolean _isOpen = true ;
        private OpenMenuListener( boolean isOpen ){
            _isOpen = isOpen ;
        }
        public void actionPerformed( ActionEvent event ){
            if( _isOpen ){
                JMenuItem item = (JMenuItem)event.getSource() ;
                try{

                    addNewMonoLogin( event.getActionCommand() ) ;
                    item.setEnabled(false);

                }catch(Exception ee ){
                    JOptionPane.showMessageDialog(
                            _myParent ,
                            ee.getMessage(),
                            "Problem creating new Login" ,
                            JOptionPane.ERROR_MESSAGE ) ;
                    switchDisplay(null);
                }
            }else{
                JMenuItem item  = (JMenuItem)event.getSource() ;
                String itemName = event.getActionCommand() ;
                _logger.error("@@@ Destroy : "+itemName);
                undefineMonoLoginEntry( itemName );
                prepareOpenMenu();

            }
        }
    }
    private ActionListener _openMenuItemListener    = new OpenMenuListener(true) ;
    private ActionListener _destroyMenuItemListener = new OpenMenuListener(false) ;

    private void prepareOpenMenu(){
        _openMenu.removeAll() ;
        _destroyMenu.removeAll() ;
        try{
            String [] children = _ourRoot.childrenNames() ;

            for( int i = 0 , n = children.length ; i < n ; i++ ){

                String name = children[i] ;

                JMenuItem item = new JMenuItem(name);
                item.addActionListener(_openMenuItemListener);
                if( _components.get(name) != null )item.setEnabled(false);
                _openMenu.add(item);

                item = new JMenuItem(name);
                item.addActionListener(_destroyMenuItemListener);
                if( _components.get(name) != null )item.setEnabled(false);
                _destroyMenu.add(item);

            }
        }catch(Exception ee ){
            _logger.error("Problem reading 'sessions'"+ee);
        }
    }
    private void addToWindowsMenu( String name ){
        JMenuItem menuItem = new JMenuItem(name);
        menuItem.addActionListener(this) ;
        _windowsMenu.add( menuItem ) ;
        prepareOpenMenu();
    }
    private void removeFromWindowsMenu(String name ){
        for( int i= 0 , n = _windowsMenu.getItemCount() ; i < n ; i++ ){
            JMenuItem   item = _windowsMenu.getItem(i) ;
            if( item.getText().equals(name) ){
                _windowsMenu.remove(item) ;
                break ;
            }
        }
    }
    private void defineNewMonoLoginEntry( String name ){
        try{
            _ourRoot.node(name);
            _ourRoot.sync();
        }catch(Exception ee ){
            _logger.error("Problem storing new node : "+ee);
        }
    }
    private void undefineMonoLoginEntry( String name ){
        try{
            _ourRoot.node(name).removeNode();
            _ourRoot.sync();
        }catch(Exception ee ){
            _logger.error("Problem removing node : "+name+" : "+ee);
        }
    }
    private void addNewMonoLogin( String name ) throws Exception {

        JPanel panel = createNewMonoLoginPanel( name )  ;

        _container.add( panel , name)  ;
        _components.put( name , panel ) ;

        switchDisplay( name ) ;

        addToWindowsMenu(name);

        validate();
        repaint();
    }
    private JPanel createNewMonoLoginPanel( String name ) throws Exception {

        try{

            ClassLoader loader = __classLoader.getClassLoaderOf(name);

            Class login =  loader.loadClass( "org.pcells.services.gui.JMonoLogin" ) ;
            Constructor c = login.getConstructor( new Class [] { java.lang.String.class , java.util.prefs.Preferences.class } ) ;

            return (JPanel)c.newInstance( new Object [] { name , _ourRoot.node(name) } ) ;

        }catch(Exception eee ){
            _logger.error("createNewMonoLoginPanel : Can't create new MonoLogin due to " + eee);
            eee.printStackTrace();
            throw eee ;
        }

    }
    public void actionPerformed( ActionEvent event ){

        Object source = event.getSource() ;

        if( source == _newMenuItem ){

            Point location = getLocation() ;
            _nameDialog.setLocation( location.x + 30 , location.y + 30 ) ;
            _nameDialog.show() ;

            String result = _nameDialog.getResult() ;
            if( result == null ){
                _logger.error("'new dialog' was canceled");
            }else{
                defineNewMonoLoginEntry(result);
                _cardLayout.show( _container , "drawing");
                prepareOpenMenu();
            }

        }else if( source == _exitMenuItem ){

            System.exit(0);

        }else if( source == _closeMenuItem ){

            String itemName = event.getActionCommand() ;
            JPanel panel = (JPanel)_components.get(itemName);
            if( ( panel == null ) || ! ( panel instanceof JMonoLogin ) )return ;
            ((JMonoLogin)panel).close() ;

        }else if( source == _removeMenuItem ){

            String itemName = event.getActionCommand() ;
            JPanel panel = (JPanel)_components.get(itemName);

            switchDisplay( null ) ;

            _components.remove( itemName ) ;
            removeFromWindowsMenu( itemName ) ;

            prepareOpenMenu();

            if( panel == null )return ;
            _container.remove( panel )  ;


        }else if( source == _windowsMenu ){

//          _logger.debug("Action : "+event.getActionCommand());

        }else if( source instanceof JMenuItem ){

            JMenuItem item = (JMenuItem)source ;
            switchDisplay( item.getText() );

        }
    }
    private void switchDisplay( String name ){
        boolean setEnabled    = true ;
        String  containerName = "drawing" ;
        if( name == null ){
            setEnabled = false ;
            name = "" ;
        }else{
            containerName = name ;
        }
        _cardLayout.show(_container, containerName );
        _closeMenuItem.setText("Close "+name);
        _closeMenuItem.setActionCommand(name);
        _closeMenuItem.setEnabled(setEnabled);
        _removeMenuItem.setText("Remove "+name);
        _removeMenuItem.setActionCommand(name);
        _removeMenuItem.setEnabled(setEnabled);

    }
    public void menuCanceled( MenuEvent event ){

    }
    public void menuSelected( MenuEvent event ){
//          _logger.debug("Action : "+event);
    }
    public void menuDeselected( MenuEvent event ){

    }
    ////////////////////////////////////////////////////////////////////////////////////////////
    //
    //   Create the menu item
    //
    private void fillHelpMenu( JMenu helpMenu ){

        StringWriter sw = new StringWriter() ;
        PrintWriter  pw = new PrintWriter( sw ) ;

        URL url = getClass().getResource("/docs/help/index.tree");
        if( url == null )return ;

        try{
            BufferedReader br = new BufferedReader( new InputStreamReader( url.openStream() ) ) ;
            String line = null ;
            try{
                while( ( line = br.readLine() ) != null ){
                    if( line.trim().equals("*") ){

                        for( Iterator it = __classLoader.jars() ; it.hasNext() ; ){
                            CellGuiClassLoader.GuiJarEntry e = (CellGuiClassLoader.GuiJarEntry)it.next() ;
                            URL subUrl = e.getHelpIndexUrl() ;
                            if( subUrl == null )continue ;
                            try{
                                BufferedReader subbr = new BufferedReader( new InputStreamReader( subUrl.openStream() ) ) ;
                                try{
                                    while( ( line = subbr.readLine() ) != null ){
                                        pw.println(line);
                                    }
                                }finally{
                                    try{
                                        subbr.close();
                                    } catch (Exception ae) {
                                        System.out.println("Problem during closing BufferedReader: "+ ae);
                                    }
                                }
                            }catch(Exception eee ){
                                System.out.println("Problem reading subUrl : "+subUrl+" : "+eee);
                                continue ;
                            }
                        }

                    }else{
                        pw.println(line);
                    }
                }

            }finally{
                try{
                    br.close();
                } catch (Exception ee) {
                    System.out.println("HelpMenu: Problem during closing the BufferedReader: " + ee);
                }
            }
        }catch(Exception ae ){
            _logger.error("Problem in reading main index : "+ae);
        }
        //_logger.error("Intermediate help menue : \n"+sw.toString());
        _helpMenu.add( new HelpMenuItem("Generic Help", new StringReader( sw.toString() ) ) ) ;

    }
    ////////////////////////////////////////////////////////////////////////////////////////////
    //
    //   DRAW PANEL
    //
    public class DrawPanel extends CellGuiSkinHelper.CellPanel {
        public Icon _icon = null ;
        public DrawPanel(){
            URL imageUrl = getClass().getResource("/images/cells-logo.jpg");
            if( imageUrl != null ){
                ImageIcon iicon = new ImageIcon(imageUrl) ;
                Image im    = iicon.getImage() ;
                im = im.getScaledInstance( 200 , -1 , Image.SCALE_SMOOTH ) ;
                _icon = (Icon)new ImageIcon(im);
            }
        }
        public void paintComponent( Graphics gin ){
            Graphics2D g = (Graphics2D) gin ;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            Dimension d = getSize() ;

            g.setColor( Color.lightGray ) ;
//         g.setPaint( new GradientPaint(0,0,Color.white,d.width,d.height,blue) ) ;
//         g.setPaint( new GradientPaint(0,0,start,d.width,d.height,blue) ) ;

            g.fillRect( 0 , 0 , d.width , d.height );
            Icon icon = null ;
            Icon cellsInside = null ;
            if( _icon == null ){
                icon = new CellIcon( d.width , d.height ) ;
            }else{
                icon = _icon ;
                cellsInside = new CellIcon( d.width , d.height ) ;
            }
            int width = icon.getIconWidth() ;
            int height = icon.getIconHeight() ;

            g.setColor( Color.white ) ;

            int x = width + 20 ;
            x = Math.max( x , height + 20 ) ;
            int xCenter = ( d.width-x) / 2 ;
            int yCenter = ( d.height-x) /2 ;

            g.fillOval(  xCenter  , yCenter , x , x ) ;
            int xd = x + x / 3 ;
            //
            // center bubble
            //
            //for( int xp = xCenter + xd ; xp < d.width ; xp += xd )
            //  g.fillOval(  xp  , yCenter , x , x ) ;
            //
            // left and right bubbles.
            //
            //for( int xp = xCenter - xd ; xp > -xd ; xp -= xd )
            //g.fillOval(  xp  , yCenter , x , x ) ;

            icon.paintIcon( this , g , ( d.width-width) / 2  , ( d.height-height)/2 );

            g.setColor( myGray ) ;
            g.setFont( __bigFont ) ;
            g.drawString( "p-Cells "+_version.getThisVersion() , 20 , d.height - 20 ) ;
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////
    //
    //   NAME DIALOG
    //
    private class NameDialog extends JDialog implements ActionListener {
        private JButton _cancelButton = new CellGuiSkinHelper.CellButton("Cancel");
        private JButton _okButton     = new CellGuiSkinHelper.CellButton("OK");
        private JTextField _inputText = new JTextField(30) ;
        private JLabel     _label     = new JLabel("New Session Name");
        private String     _result    = null ;

        private NameDialog( JFrame frame ){
            super( frame , "New Session Name" , true ) ;

            Container    c = getContentPane() ;
            BorderLayout l = new BorderLayout(10,10);

            c.setLayout( l ) ;

            JPanel panel = new CellGuiSkinHelper.CellPanel( new BorderLayout(10,10) ) ;

            panel.setBorder( new CellBorder("New Session Name" , 20 ) ) ;

            panel.add(_inputText,"Center");

            _cancelButton.addActionListener(this);
            _okButton.addActionListener(this);
            _inputText.addActionListener(this);

            JPanel buttons = new CellGuiSkinHelper.CellPanel(new BorderLayout());
            buttons.add(_okButton,"West");
            buttons.add(_cancelButton,"East");
            panel.add(buttons,"South");

            c.add( panel , "Center");
            pack();
            addWindowListener(
                    new WindowAdapter() {
                        public void windowClosing( WindowEvent event ){
                            _result = null ;
                            setVisible(false);
                            _inputText.setText("");
                        }
                        public void windowOpened( WindowEvent event ){
                            _okButton.requestFocus();
                            _inputText.setText("");
                        }

                    }
            ) ;
        }
        public String getResult(){ return _result ; }
        public void actionPerformed( ActionEvent event ){
            Object source = event.getSource() ;
            if( source == _cancelButton ){
                _result = null ;
            }else if( ( source == _okButton ) || ( source == _inputText ) ){
                String text = _inputText.getText() ;
                if( ( text == null ) || ( text.equals("") ) ){
                    _result = null ;
                }else{
                    _result = text ;
                }
            }
            setVisible(false);
            _inputText.setText("");
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////
    //
    //   VERSION SYSTEM
    //
    private void removeAllModules(){
        Preferences root = _ourRoot ;
        try{
            String  []  c = root.childrenNames()  ;
            for( int i = 0 , n = c.length ; i < n ; i++ ){
                Preferences inst = root.node(c[i]) ;
                if( inst.nodeExists("Modules") ){
                    inst.node("Modules").removeNode() ;
                    inst.flush() ;
                }
            }
        }catch(Exception ee ){
            _logger.error("Problems in cleaning up modules : "+ee ) ;
        }
        return ;
    }
    class VersionUpdate implements Runnable {
        private long    _updateTime          = (long)( 10 * 60 * 1000 ) ;
        private String  _ourVersionLocation  = "/docs/help/version" ;
        private int  [] _currentVersion       = { 0 , 0 } ;
        // This seems to be the place where the actual version is set
        private String  _currentVersionString = new AppPropertyProvider().getVersionString();
        private String  _serverVersionString  = null ;
        private String  _versionUrlString     = "http://www.dcache.org/downloads/gui/version.txt";
        private VersionUpdate(){
            loadVersion();
            int [] preferenceVersion = getVersionFromPreferences() ;
            if( compare( _currentVersion , preferenceVersion ) > 0 ){
                _logger.error("Found old version in preferences current version: {} preference version: {}", _currentVersion, preferenceVersion);
                removeAllModules() ;
                setVersionToPreferences( _currentVersion ) ;
            }
            new Thread(this).start();
        }
        public String getThisVersion(){
            return _currentVersionString ;
        }
        public String getServerVersion(){
            return _serverVersionString ;
        }
        public void run(){
            while( ! Thread.currentThread().isInterrupted() ){
                try{
                    try{
                        if( queryUpdate() )break ;
                    }catch(Exception eee ){
                        _logger.error("updateThread reported : " + eee);
                    }
                    Thread.currentThread().sleep(_updateTime);
                }catch(InterruptedException ee ){
                    _logger.error("updateThread interrupted");
                    break ;
                }
            }
            _logger.debug("updateThread finished");
        }
        private int [] getVersionFromPreferences(){
            String versionString = _ourRoot.get("version",null);
            if( versionString == null )return new int[3] ;
            try{
                return stringToVersion(versionString);
            }catch(Exception ee ){
                _logger.error("Couldn't get serious version from pref"+ee);
            }
            return new int[3] ;
        }
        private void setVersionToPreferences( int [] version ){
            _ourRoot.put("version",versionToString(version));
            try{ _ourRoot.flush() ; }
            catch(BackingStoreException ee){
                _logger.error("Could store version : "+ee);
            }
        }
        private boolean queryUpdate() throws Exception {
            _logger.debug("Query version from server : "+_versionUrlString);
            URL url = new URL(_versionUrlString);
            if( url == null )return false ;
            int [] serverVersion = new int[0];
            try{
                BufferedReader br = new BufferedReader(
                        new InputStreamReader( url.openStream() ) ) ;
                String line = null ;
                try{
                    if( ( line = br.readLine() ) != null ){
                        StringTokenizer st = new StringTokenizer(line,".");
                        serverVersion = stringToVersion(line);
                        String serverVersionString = versionToString(serverVersion) ;
                        _logger.debug("serverVersion : "+serverVersionString);
                        if( ( _serverVersionString == null ) || ! _serverVersionString.equals(serverVersionString) ){
                            _serverVersionString = serverVersionString ;
                            generateAboutText() ;
                            _aboutMenuItem.setHelpText(_aboutText);
                        }
                    }
                }finally{
                    try{
                        br.close();
                    } catch (Exception ee) {
                        System.out.println("Problem during closing BufferedReader: "+ ee);
                    }
                }
            }catch(Exception ee ){
                _logger.error("Problem reading version "+ee);
                return false;
            }
            if( compare( serverVersion , _currentVersion ) > 0 ){
                _logger.debug("Need new version");
                _fileMenu.setForeground(Color.red);
                _newVersion.setForeground(Color.red);
                _menuBar.add( _newVersion) ;

            }else{
                _fileMenu.setForeground(Color.black);
            }
            repaint();
            return false;
        }
        private int compare( int [] a , int [] b ){
            int l = Math.min( a.length , b.length ) ;
            if( l <= 0 )
                throw new
                        IllegalArgumentException("Some arguments don't contain data");
            for( int i = 0 ; i < l ; i++ ){
                if( a[i] > b[i] )return 1 ;
                else if( a[i] < b[i] )return -1 ;
            }
            return a.length > b.length ? 1 :
                    a.length < b.length ? -1 : 0 ;
        }
        private String versionToString( int [] version ){
            StringBuffer sb = new StringBuffer() ;
            if( version.length > 0 )sb.append(version[0]);
            for( int i = 1 ; i < version.length ; i++ )
                sb.append(".").append(version[i]);
            return sb.toString() ;
        }
        private int [] stringToVersion( String version ){
            StringTokenizer st = new StringTokenizer(version,".");
            int  [] currentVersion = new int[st.countTokens()];
            for( int i = 0 ; ( i < currentVersion.length ) && st.hasMoreTokens() ; i++ ){
                currentVersion[i] = Integer.parseInt(st.nextToken());
            }
            return currentVersion ;
        }
        private void loadVersion(){
            URL url = getClass().getResource(_ourVersionLocation);
            if( url == null )return ;
            try{
                BufferedReader br = new BufferedReader(
                        new InputStreamReader( url.openStream() ) ) ;
                String line = null ;
                try{
                    _currentVersion = new int[0];
                    if( ( line = br.readLine() ) != null ){
                        _currentVersion = stringToVersion( line ) ;
                        _currentVersionString = versionToString(_currentVersion);
                        _logger.debug("currentVersion : "+_currentVersionString);
                    }
                    if( ( line = br.readLine() ) != null ){
                        _versionUrlString = line ;
                    }
                    return ;
                }finally{
                    try{
                        br.close();
                    } catch (Exception ee){
                        System.out.println("Problem during closing BufferedReader: "+ ee);
                    }
                }
            }catch(Exception ee ){
                _logger.error("Problem reading version "+ee);
                return ;
            }

        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////
    //
    //   HELPER FUNCTIONS
    //
    private String _aboutText = null ;
    private void generateAboutText(){
        StringBuffer sb = new StringBuffer() ;
        sb.append(_aboutText1);
        sb.append("<tr><td>Author</td><td>:</td><td>Patrick Fuhrmann</td></tr>\n");
        sb.append("<tr><td>Version</td><td>:</td><td>").
                append(_version.getThisVersion()).
                append("</td></tr>\n");
        String serverVersion = _version.getServerVersion() ;
        if( serverVersion != null )
            sb.append("<tr><td>dCache.ORG Version</td><td>:</td><td>").
                    append(serverVersion).
                    append("</td></tr>\n");
        sb.append(_aboutText2);
        _aboutText = sb.toString();
    }
    private String _aboutText1 =
            "<html><head><title>Cell Login About Text</title></head>\n"+
                    "<body>\n"+
                    "<center><h1>About Cell Login</h1></center>\n"+
                    "<blockquote><hr>\n"+
                    "<center><table border=0 cellspacing=4 cellpadding=4>\n" ;
    private String _aboutText2 =
            "</table></center>\n"+
                    "<br><hr></blockquote>\n"+
                    "<h3>DISCLAIMER OF WARRANTY</h3>\n"+
                    "UNLESS SPECIFIED IN THIS AGREEMENT,\n"+
                    "ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND\n"+
                    "WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,\n"+
                    "FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT ARE\n"+
                    "DISCLAIMED, EXCEPT TO THE EXTENT THAT THESE DISCLAIMERS ARE HELD\n"+
                    "TO BE LEGALLY INVALID.\n"+

                    "<h3>LIMITATION OF LIABILITY</h3>\n"+
                    "TO THE EXTENT NOT PROHIBITED BY LAW,\n"+
                    "IN NO EVENT WILL dCache.org OR ITS LICENSORS BE LIABLE FOR ANY LOST\n"+
                    "REVENUE, PROFIT OR DATA, OR FOR SPECIAL, INDIRECT,\n"+
                    "CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED\n"+
                    "REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF OR RELATED\n"+
                    "TO THE USE OF OR INABILITY TO USE SOFTWARE, EVEN IF dCache.org HAS BEEN\n"+
                    "ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.  In no event will\n"+
                    "dCache.org's liability to you, whether in contract, tort (including\n"+
                    "negligence), or otherwise, exceed the amount paid by you for\n"+
                    "Software under this Agreement.  The foregoing limitations will\n"+
                    "apply even if the above stated warranty fails of its essential\n"+
                    "purpose.   Some states do not allow the exclusion of incidental\n"+
                    "or consequential damages, so some of the terms above may not be\n"+
                    "applicable to you.\n"+
                    "</body></html>\n";

    private class HelpMenuItem extends JMenuItem implements ActionListener {

        private JFrame _helpMenu = null ;
        private URL    _url      = null ;
        private String _text     = null ;
        private String _title    = null ;
        private int    _width    = 800 ;
        private int    _height   = 400 ;
        private Reader _reader   = null ;
        private HelpMenuItem(String title , URL url ){
            super(title);
            _url   = url ;
            _title = title ;
            addActionListener(this);
        }
        private HelpMenuItem(String title , Reader reader ){
            super(title);
            _reader = reader ;
            _title  = title ;
            addActionListener(this);
        }
        private HelpMenuItem(String title , String text ){
            super(title);
            _text  = text ;
            _title = title ;
            addActionListener(this);
        }
        private void setHelpText( String text ){
            _text     = text ;
            _helpMenu = null ;
        }
        private void setFrameSize( int width , int height ){
            _width  = width ;
            _height = height ;
        }
        public void actionPerformed( ActionEvent event ){
            if( _helpMenu == null ){
//              _logger.debug("Generating help menu "+event );
                try{
                    _helpMenu = _url != null ?
                            (JFrame) new JSelectionHelpFrame(_title,_url) :
                            _reader != null ?
                                    (JFrame) new JSelectionHelpFrame(_title,_reader) :
                                    (JFrame) new JHelpFrame(_title,_text) ;

                    _helpMenu.pack();
                    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                    _helpMenu.setLocation(200,200);
                    _helpMenu.setSize(_width,_height);
                    _helpMenu.addWindowListener(
                            new WindowAdapter(){
                                public void windowClosing(WindowEvent e) {
                                    _helpMenu.setVisible(false) ;
                                }
                            }
                    );
                }catch(Exception ee ){
                    return ;
                }
            }
            _helpMenu.setVisible(true);
            _helpMenu.setExtendedState(JFrame.NORMAL);
        }
    }
    //-----------------------------------------------------------------------------
    //
    //     SKIN MENU ITEM
    //
    private class SkinMenuItem
            extends JMenu
            implements ActionListener{

        private JCheckBox _skinBorder = new JCheckBox("Border",CellGuiSkinHelper.isNiceBorder()) ;
        private JCheckBox _skinMode   = new JCheckBox("Skin",CellGuiSkinHelper.isSkin()) ;
        private JMenuItem _backgroundColorItem   = new JMenuItem("Background") ;
        private JMenuItem _foregroundColorItem   = new JMenuItem("Foreground") ;
        private JMenuItem _borderTopColorItem    = new JMenuItem("Boder.Top") ;
        private JMenuItem _borderBottomColorItem = new JMenuItem("Border.Bottom") ;
        private SkinMenuItem( String name ){
            super(name);
            add( new JLabel( "   Skin behaviour   ", JLabel.CENTER ) ) ;
            addSeparator();
            _skinBorder.addActionListener(this);
            _skinMode.addActionListener(this);
            _backgroundColorItem.addActionListener(this);
            _foregroundColorItem.addActionListener(this);
            _borderTopColorItem.addActionListener(this);
            _borderBottomColorItem.addActionListener(this);
            add(_skinBorder);
            add(_skinMode);
            add(_backgroundColorItem) ;
            add(_foregroundColorItem) ;
            add(_borderTopColorItem) ;
            add(_borderBottomColorItem) ;
        }
        public void actionPerformed( ActionEvent event ){
            Object source = event.getSource() ;
            if( source == _skinBorder ){
                _logger.debug(" Skin border " + _skinBorder.isSelected() ) ;
                CellGuiSkinHelper.setNiceBorder(_skinBorder.isSelected());
            }else if( source == _skinMode ){
                _logger.debug(" Skin Mode " + _skinBorder.isSelected() ) ;
                CellGuiSkinHelper.setSkin(_skinMode.isSelected());
            }else if( source == _borderBottomColorItem ){
                Color color = JColorChooser.showDialog(this,"Choose Border Bottom",Color.white);
                if( color == null )return ;
                CellGuiSkinHelper.setBottomColor(color);
                _logger.debug("Result : "+color);
            }else if( source == _borderTopColorItem ){
                Color color = JColorChooser.showDialog(this,"Choose Border Top",Color.white);
                if( color == null )return ;
                CellGuiSkinHelper.setTopColor(color);
                _logger.debug("Result : "+color);
            }else if( source == _backgroundColorItem ){
                Color color = JColorChooser.showDialog(this,"Choose Background",Color.white);
                if( color == null )return ;
                CellGuiSkinHelper.setBackgroundColor(color);
                _logger.debug("Result : "+color);
            }else if( source == _foregroundColorItem ){
                Color color = JColorChooser.showDialog(this,"Choose Foreground",Color.black);
                if( color == null )return ;
                CellGuiSkinHelper.setForegroundColor(color);
                _logger.debug("Result : "+color);
            }
        }

    }
    //-----------------------------------------------------------------------------
    //
    //     PROPERTY MENU ITEM
    //
    private class PropertyMenuItem
            extends JMenuItem
            implements ActionListener {

        private String _title = null ;
        private PropertyMenuItem(String title ){
            super(title);
            _title = title ;
            addActionListener(this);
        }
        public void actionPerformed( ActionEvent event ){
            String defaultString = "<key>[=<value>]" ;
            String response =
                    JOptionPane.showInputDialog(
                            JMultiLogin.this,
                            "Set/modify/remove property" ,
                            defaultString ) ;
            _logger.debug("Selection : "+response);
            if( response == null )return ;
            response = response.trim() ;
            if( response.equals("") || response.equals(defaultString) )return ;
            int pos = response.indexOf('=') ;
            if( pos < 0 ){
                _ourRoot.remove(response);
                try{
                    _ourRoot.sync();
                } catch (Exception ee) {
                    System.out.println("Problem during sync with persistent store.");
                }
                System.getProperties().remove(response);
                return ;
            }
            if( ( pos <= 0 ) || ( pos == (response.length() - 1 ) ) ){
                JOptionPane.showMessageDialog(JMultiLogin.this,
                        "Syntax Error : please use <key>=<value> or <key>",
                        "Systax Error",
                        JOptionPane.ERROR_MESSAGE);
                return ;
            }
            String key = response.substring(0,pos);
            String value = response.substring(pos+1);
            System.setProperty(key,value);
            _ourRoot.put(key,value);
            try{
                _ourRoot.sync();
            } catch (Exception ee ) {
                System.out.println("Problem during sync with persistent store.");
            }
            CellGuiSkinHelper.loadProperties() ;

            Iterator it = System.getProperties().entrySet().iterator() ;
            while( it.hasNext() ){
                Map.Entry e = (Map.Entry)it.next() ;
                _logger.debug(e.getKey() + " -> " + e.getValue()) ;
            }

        }

    }
    //-----------------------------------------------------------------------------
    //
    //     CLASSLOADER MENU ITEM
    //
    private class      ClassLoaderMenuItem
            extends    JMenuItem
            implements ActionListener {

        private String           _title = null ;
        private ClassLoaderFrame _frame = new ClassLoaderFrame("ClassLoader Setup") ;

        private ClassLoaderMenuItem(String title ){
            super(title);
            _title = title ;
            addActionListener(this);
        }
        public void actionPerformed( ActionEvent event ){
            if( _frame == null )return ;
            _frame.reloadPreferences() ;
            _frame.setVisible(true);
            _frame.setExtendedState(JFrame.NORMAL);
        }

    }
    private class ClassLoaderFrame extends JFrame implements ActionListener {
        private ClassLoaderSelectionPanel _master      = null;
        // private ClassLoaderPreferences    _preferences = new ClassLoaderPreferences( _ourRoot ) ;
        private ClassLoaderPreferences    _preferences = __classLoader.getPreferences() ;
        private ClassLoaderFrame(String title ){
            super(title);

            _master = new ClassLoaderSelectionPanel() ;

            JPanel content = new JPanel( new GridBagLayout() ) ;

            GridBagConstraints c = new GridBagConstraints()  ;
            c.gridheight = 1 ;
            c.insets     = new Insets(4,4,4,4) ;

            c.gridwidth  = 1 ; c.gridx = 0 ; c.gridy = 0 ; c.weightx  =  0.0 ;
            c.fill       = GridBagConstraints.NONE ;

            content.add( _master , c ) ;
            getContentPane().add( content , "Center" );
            pack();
            setLocation(200,200);
            //   setSize( 300 , 300 ) ;

            addWindowListener(
                    new WindowAdapter(){
                        public void windowClosing(WindowEvent e) {
                            setVisible(false) ;
                        }
                        public void windowActivated( WindowEvent e ){
                            //_logger.error("Window activated");
                        }
                    }
            );
        }
        private void reloadPreferences(){
            _preferences.reloadPreferences() ;
            _master.loadPreferences() ;
        }
        public void actionPerformed( ActionEvent event ){
            Object source = event.getSource() ;
        }
        private class ClassLoaderSelectionPanel extends JPanel implements ActionListener {

            private JComboBox _box          = new JComboBox() ;
            private JButton   _updateButton = new JButton("Save");
            private JButton   _resetButton  = new JButton("Reset to Defaults") ;
            private JButton   _clearButton  = new JButton("Clear All") ;
            private JarPanel  _jarPanel     = null ;

            private class JarPanel extends JPanel {
                private CellGuiClassLoader _loader = null ;
                private HashMap _map = new HashMap() ;
                private JarPanel( CellGuiClassLoader loader ){
                    _loader = loader ;
                    setLayout( new GridLayout( 0, 1 ) ) ;
                    initialize() ;
                }
                private void initialize(){
                    for( Iterator jars = _loader.jars() ; jars.hasNext() ; ){
                        CellGuiClassLoader.GuiJarEntry entry = (CellGuiClassLoader.GuiJarEntry)jars.next() ;
                        String name = entry.getName() ;
                        CellGuiClassLoader.Version version = entry.getVersion() ;
                        String versionString = version == null ? "" : version.toString() ;
                        versionString = versionString == null ? "" : versionString ;
                        String distinguestedName = name +" "+versionString ;
                        JCheckBox box = new JCheckBox( distinguestedName , entry.isDefault()  )  ;
                        Object [] value = { name , versionString , box ,  new Boolean( entry.isDefault() ) } ;
                        _map.put( distinguestedName , value ) ;
                        add( box ) ;
                    }
                }
                private void resetToDefault(){
                    for( Iterator it = _map.values().iterator() ; it.hasNext() ; ){
                        Object [] o = (Object [])it.next() ;
                        ((JCheckBox)o[2]).setSelected(  ((Boolean)o[3]).booleanValue() ) ;
                    }
                }
                private void clearAll(){
                    for( Iterator it = _map.values().iterator() ; it.hasNext() ; ){
                        Object [] o = (Object [])it.next() ;
                        ((JCheckBox)o[2]).setSelected( false ) ;
                    }
                }
                private Iterator selected(){
                    ArrayList list = new ArrayList() ;
                    for( Iterator it = _map.values().iterator() ; it.hasNext() ; ){
                        Object [] o = (Object [])it.next() ;
                        if( ((JCheckBox)o[2]).isSelected() )list.add( o ) ;
                    }
                    return list.iterator() ;
                }
                private Iterator all(){
                    ArrayList list = new ArrayList() ;
                    for( Iterator it = _map.values().iterator() ; it.hasNext() ; ){
                        Object [] o = (Object [])it.next() ;
                        list.add( o ) ;
                    }
                    return list.iterator() ;
                }
            }
            private ClassLoaderSelectionPanel(){
                setLayout( new BorderLayout( 5 , 5 ) ) ;
                setBorder(BorderFactory.createTitledBorder(" Module Manager "));
                ClassLoader loader = this.getClass().getClassLoader() ;
                if( loader instanceof CellGuiClassLoader ){

                    CellGuiClassLoader l = (CellGuiClassLoader)loader ;
                    _jarPanel = new JarPanel( l ) ;

                    _box.addActionListener(this);

                    add( _box          , "North" ) ;
                    add( _jarPanel     , "Center" ) ;

                    JPanel  buttons = new JPanel( new GridLayout(0,1) ) ;
                    buttons.add( _updateButton ) ;
                    buttons.add( _resetButton ) ;
                    buttons.add( _clearButton ) ;

                    add( buttons , "South" ) ;

                    _updateButton.addActionListener( this ) ;
                    _resetButton.addActionListener( this ) ;
                    _clearButton.addActionListener( this ) ;
                }else{
                    JLabel label = new JLabel( "GUI is not using a configurable Cell Loader" ) ;
                    label.setForeground(Color.red) ;
                    label.setHorizontalAlignment( JLabel.CENTER ) ;
                    add( label , "Center" ) ;
                }
            }
            public void loadPreferences(){
                _box.removeAllItems() ;
                for( Iterator nn = _preferences.connections() ; nn.hasNext() ; ){
                    String name = nn.next().toString() ;
                    _box.addItem( name ) ;
                }
            }
            public void actionPerformed( ActionEvent event ){
                Object source = event.getSource() ;
                if( source == _box ){
                    if( _box.getSelectedItem() == null )return ;
                    String connectionName = _box.getSelectedItem().toString() ;
                    ClassLoaderPreferences.Connection connection = _preferences.getConnection(connectionName);

                    for( Iterator it = _jarPanel.all() ; it.hasNext() ; ){
                        Object   []  o = (Object [])it.next() ;
                        String name    = o[0].toString() ;
                        String version = o[1].toString() ;
                        JCheckBox check   = (JCheckBox)o[2] ;

                        check.setSelected( connection.isSelected( name , version ) ) ;
                    }

                }else if( source == _updateButton ){
                    String connectionName = _box.getSelectedItem().toString() ;
                    ClassLoaderPreferences.Connection connection = _preferences.getConnection(connectionName);
                    if( connection == null )return ;
                    connection.clear() ;
                    for( Iterator it = _jarPanel.selected() ; it.hasNext() ; ){
                        Object   []  o = (Object [])it.next() ;
                        String name    = o[0].toString() ;
                        String version = o[1].toString() ;
                        connection.add( name , version ) ;
                    }
                    _preferences.save();
                }else if( source == _resetButton ){
                    _jarPanel.resetToDefault() ;
                }else if( source == _clearButton ){
                    _jarPanel.clearAll() ;
                }
            }
        }

    }
    //-----------------------------------------------------------------------------
    //
    //     CONSOLE MENU ITEM
    //
    private class      ConsoleMenuItem
            extends    JMenuItem
            implements ActionListener, Logable {


        private class FrameOutputStream extends OutputStream {
            private static final int STDERR = 1 ;
            private static final int STDOUT = 2 ;
            private int _mode = STDOUT ;
            private FrameOutputStream(int mode ){
                _mode = mode ;
            }
            public void write( int n ){
                if( _mode == STDERR ){
                    _console.appendStderr(""+(char)n);
                }else{
                    _console.appendStdout(""+(char)n);
                }
            }
            public void write( byte [] data , int offset , int size ){
                if( _mode == STDERR ){
                    _console.appendStderr(new String(data,offset,size));
                }else{
                    _console.appendStdout(new String(data,offset,size));
                }
            }
        }
        private class JConsoleFrame extends JFrame implements ActionListener {
            private JTextArea    _text       = new JTextArea();
            private JScrollPane  _scrollPane = null ;
            private ButtonGroup  _loggingRadioButtons = new ButtonGroup();
            private JRadioButton _showOUT    = new JRadioButton("Show DEBUG output");
            private JRadioButton _showERR    = new JRadioButton("Show ERROR output");
            private JButton      _clear      = new CellGuiSkinHelper.CellButton("Clear Screen");
            private JPanel       _master     = null ;
            private JConsoleFrame(String title ){
                super(title);
                _showERR.setSelected(true);
                _showERR.addActionListener(this);
                _showOUT.addActionListener(this);

                _master = new CellGuiSkinHelper.CellPanel( new BorderLayout(4,4) ) ;
                _master.setBorder( new CellBorder( "Console" , 30 ) ) ;

                _master.add(_scrollPane = new JScrollPane(_text)) ;

                CellGuiSkinHelper.setComponentProperties(_showOUT) ;
                CellGuiSkinHelper.setComponentProperties( _showERR ) ;

                JPanel south = new CellGuiSkinHelper.CellPanel(new FlowLayout()) ;
                _loggingRadioButtons.add(_showERR);
                _loggingRadioButtons.add(_showOUT);
                south.add(_showOUT) ;
                south.add(_showERR);
                south.add(_clear);

                _master.add( south , "South" ) ;

                getContentPane().add( _master , "Center" );
                pack();
                setLocation(200,200);
                setSize(_width,_height);

                _text.setEditable(false);
                addWindowListener(
                        new WindowAdapter(){
                            public void windowClosing(WindowEvent e) {
                                setVisible(false) ;
                            }
                        }
                );
                _clear.addActionListener(this);
            }

            public void setLoggingLevel(Level level) {
                Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
                root.setLevel(level);

                Iterator appenderIterator = root.iteratorForAppenders();
                while (appenderIterator.hasNext()) {
                    Appender next = (Appender)appenderIterator.next();
                    _logger.debug("Appender: {}", next.getName());
                }

            }

            public String getLoggingLevel() {
                Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
                return root.getLevel().toString();
            }

            public void actionPerformed( ActionEvent event ){
                Object source = event.getSource() ;
                _logger.debug("Class ActionListener with source: {}", source);
                if( source == _clear ){
                    _text.setText("");
                }
                if (_showERR.isSelected()) {
                    _logger.debug("Logging Level is: {}", getLoggingLevel());
                    setLoggingLevel(Level.ERROR);
                    _logger.error("Logging level set to {}", getLoggingLevel());
                }
                if (_showOUT.isSelected()) {
                    _logger.error("Logging Level is: {}", getLoggingLevel());
                    setLoggingLevel(Level.DEBUG);
                    _logger.debug("Logging level set to {}", getLoggingLevel());
                }
            }

            private void appendStdout( String text ){
                if(_showOUT.isSelected()) {
                    _logger.debug("Printing DEBUG output: {}", text);
                    append(text);
                }
            }
            private void appendStderr( String text ){
                if(_showERR.isSelected()) {
                    _logger.error("Printing ERROR output: {}", text);
                    append(text);
                }
            }

            private void append( String text ){
                _text.append(text);
                SwingUtilities.invokeLater(

                        new Runnable(){
                            public void run(){
                                Rectangle rect = _text.getBounds() ;
                                rect.y = rect.height - 30 ;
                                _scrollPane.getViewport().scrollRectToVisible( rect ) ;
                            }
                        }
                ) ;
            }
        }

        private JConsoleFrame _console  = null ;
        private String _title    = null ;
        private int    _width    = 600 ;
        private int    _height   = 400 ;
        private PrintStream _printer = null ;
        private ConsoleMenuItem(String title ){
            super(title);
            _title = title ;
            _console = new JConsoleFrame(title);
            addActionListener(this);
            _printer = new PrintStream( new FrameOutputStream(FrameOutputStream.STDERR));
            System.setErr( _printer ) ;
            _printer = new PrintStream( new FrameOutputStream(FrameOutputStream.STDOUT));
            System.setOut( _printer ) ;
        }
        private void setFrameSize( int width , int height ){
            _width  = width ;
            _height = height ;
        }
        public void actionPerformed( ActionEvent event ){
            if( _console == null )return ;
            _console.setVisible(true);
            _console.setExtendedState(JFrame.NORMAL);
        }
        public void log( String message ){
            _console.appendStdout(message);
            _console.appendStdout("\n");
        }
        public void elog( String message ){
            _console.appendStderr(message);
            _console.appendStderr("\n");
        }
        public void plog( String message ){
            elog(message);
        }
    }
    //-----------------------------------------------------------------------------
    //
    //     DRAW BOARD MENU ITEM
    //
    private class      DrawBoardMenuItem
            extends    JMenuItem
            implements ActionListener {


        private DrawBoardFrame _console  = null ;
        private String _title    = null ;
        private int    _width    = 600 ;
        private int    _height   = 400 ;
        private PrintStream _printer = null ;
        private DrawBoardMenuItem(String title ){
            super(title);
            _title = title ;
            _console = new DrawBoardFrame(title);
            addActionListener(this);
        }
        private void setFrameSize( int width , int height ){
            _width  = width ;
            _height = height ;
        }
        public void actionPerformed( ActionEvent event ){
            if( _console == null )return ;
            _console.refreshProperties();
            _console.setVisible(true);
            _console.setExtendedState(JFrame.NORMAL);
        }
    }
    public static void main(String argv[]) throws Exception  {
        JMultiLogin f = new JMultiLogin("Cell Login" , argv );
    }
}