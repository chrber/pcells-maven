
package org.pcells.services.gui ;

import org.pcells.util.CellGuiClassLoader;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import java.awt.AWTEventMulticaster;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.prefs.Preferences;


public class JMLSetupPanel extends CellGuiSkinHelper.CellPanel implements ActionListener {

    private AddressPanel _addressPanel = null ;
    private ModulePanel  _modulePanel  = null ;
    private PlugPanel    _plugPanel    = null ;
    private Ssh2KeyPanel _keyPanel    = null ;
    private JTabbedPane  _tabbed       = new JTabbedPane() ;
    private JButton   _apply = new CellGuiSkinHelper.CellButton("Apply");
    private JButton   _reset = new CellGuiSkinHelper.CellButton("Reset");
    private JButton   _quit  = new CellGuiSkinHelper.CellButton("Quit");
    private JLabel    _label = new JLabel("",JLabel.CENTER);
    private VirtualButton   _virtualOK = new VirtualButton() ;
    public JMLSetupPanel( String name , Preferences pref ){

        setLayout( new BorderLayout(10,10) ) ;

        _addressPanel = new AddressPanel(  name , pref.node("Addresses") ) ;
        _modulePanel  = new ModulePanel(   name , pref.node("Modules") ) ;
        _plugPanel    = new PlugPanel(     name , pref.node("Pluggins") ) ;
        _keyPanel    = new Ssh2KeyPanel(   name , pref.node("SSH2_KEYS") ) ;

        CellGuiSkinHelper.setComponentProperties( _tabbed ) ;

        _tabbed.addTab( "Addresses" , _addressPanel ) ;
        _tabbed.addTab( "Modules"   , _modulePanel  ) ;
        _tabbed.addTab( "Pluggins"  , _plugPanel    ) ;
        _tabbed.addTab( "SSH2 Keys" , _keyPanel     ) ;

        add( _tabbed , "Center" ) ;

        JPanel lastLine    = new CellGuiSkinHelper.CellPanel( new BorderLayout( 5,5 ) ) ;
        JPanel leftButtons = new CellGuiSkinHelper.CellPanel( new GridLayout(0,2) ) ;

        leftButtons.add( _apply ) ;
        leftButtons.add( _reset ) ;

        lastLine.add( leftButtons , "West" ) ;
        lastLine.add(_label , "Center" ) ;
        lastLine.add( _quit , "East" ) ;


        add( lastLine , "South" ) ;

        _apply.addActionListener(this);
        _reset.addActionListener(this);
        _quit.addActionListener(this);


        _label.setForeground( Color.red ) ;
    }
    //   public void addActionListener( ActionListener listener ){
//      _virtualOK.addActionListener(listener);
//   }
    public void actionPerformed( ActionEvent event ){
        _label.setText("");
        Object source = event.getSource() ;
        Component selected = _tabbed.getSelectedComponent() ;
        if( ! ( selected instanceof Applyable) ){
            _label.setText("PANIC : Internal error 147");
            return ;
        }
        Applyable applyable = (Applyable)selected ;
        if( source == _apply ){
            try{
                applyable.apply() ;
            }catch(Exception ee ){
                _label.setText(ee.getMessage());
            }
        }else if( source == _reset ){
            try{
                applyable.reset() ;
            }catch(Exception ee ){
                _label.setText(ee.getMessage());
            }
        }else if( source == _quit ){
            processEvent( new ActionEvent( this ,0,"quit") ) ;
        }
    }
    private class VirtualButton extends JButton {
        private void go( Object source ){
            fireActionPerformed( new ActionEvent( source ,0,"quit") );
        }
    }
    private interface Applyable {
        public void reset() throws Exception  ;
        public void apply() throws Exception  ;
    }
    private class AddressPanel
            extends CellGuiSkinHelper.CellPanel
            implements Applyable, ActionListener {

        private JLabel _hostLabel   = new JLabel( "Hostname" , JLabel.RIGHT ) ;
        private JLabel _portLabel   = new JLabel( "Portnumber" , JLabel.RIGHT ) ;
        private JLabel _loginLabel  = new JLabel( "Login Name" , JLabel.RIGHT ) ;
        private JLabel _protocolLabel = new JLabel( "Protocol" , JLabel.RIGHT ) ;
        private JTextField _host    = new JTextField(20) ;
        private JTextField _port    = new JTextField(20) ;
        private JTextField _login   = new JTextField(20) ;
        private JRadioButton  _raw     = new JRadioButton("Raw" , false ) ;
        private JRadioButton  _ssh1    = new JRadioButton("Ssh 1" , true ) ;
        private JRadioButton  _ssh2    = new JRadioButton("Ssh 2" , false ) ;
        private ButtonGroup   _protocol = new ButtonGroup() ;
        private String        _protocolName = "ssh1" ;
        private Preferences _preferences = null ;

        public Insets getInsets(){ return new Insets(20,20,20,20) ; }
        public void reset()  throws Exception {
            loadPreferences();
        }
        public void apply()  throws Exception {
            String login = _login.getText() ;
            if( login.equals("") )
                throw new IllegalArgumentException("LoginName not specified");
            String host = _host.getText() ;
            if( host.equals("") )
                throw new IllegalArgumentException("Hostname not specified");
            String port = _port.getText() ;
            if( port.equals("") )
                throw new IllegalArgumentException("Portnumber not specified");
            int portnumber = 0 ;
            try{
                portnumber = Integer.parseInt(port);
            }catch(Exception eee ){
                throw new
                        IllegalArgumentException("Port Number not an integer");
            }
            if( ( portnumber < 0 ) || ( portnumber > 0x10000 ) )
                throw new
                        IllegalArgumentException("Port number not in range (0...5000)");

            storePreferences() ;
        }
        private void loadPreferences() throws Exception {
            _host.setText( _preferences.get("hostname","localhost") );
            _port.setText( _preferences.get("portnumber","22223") ) ;
            _login.setText( _preferences.get("loginname","admin") ) ;
            String protocol = _preferences.get("protocol" , "ssh1" ) ;
            selectProtocol( protocol ) ;
        }
        private void selectProtocol( String protocol ){
            _protocolName = protocol ;
            if( protocol.equals("raw") ){
                _raw.setSelected(true);
            }else if( protocol.equals("ssh1") ){
                _ssh1.setSelected(true);
            }else if( protocol.equals("ssh2") ){
                _ssh2.setSelected(true);
            }else{
                _ssh1.setSelected(true);
                _protocolName = "ssh1" ;
            }
        }
        private void storePreferences() throws Exception {
            _preferences.put( "hostname" , _host.getText() ) ;
            _preferences.put( "portnumber" , _port.getText() ) ;
            _preferences.put( "loginname"  , _login.getText() ) ;
            _preferences.put( "protocol"    , _protocolName ) ;

            _preferences.sync();
        }
        private AddressPanel( String name , Preferences pref ){

            _preferences = pref ;

            setLayout( new GridBagLayout() ) ;

            CellGuiSkinHelper.setComponentProperties( _raw ) ;
            CellGuiSkinHelper.setComponentProperties( _ssh1 ) ;
            CellGuiSkinHelper.setComponentProperties( _ssh2 ) ;
            CellGuiSkinHelper.setComponentProperties( _hostLabel ) ;
            CellGuiSkinHelper.setComponentProperties( _portLabel ) ;
            CellGuiSkinHelper.setComponentProperties( _protocolLabel ) ;
            CellGuiSkinHelper.setComponentProperties( _loginLabel ) ;
            setBorder(
                    BorderFactory.createTitledBorder(" Cell Login Addresses ")
            );
            GridBagConstraints c = new GridBagConstraints()  ;
            c.gridheight = 1 ;
            c.insets     = new Insets(4,4,4,4) ;

            c.gridwidth  = 1 ; c.gridx = 0 ; c.gridy = 0 ; c.weightx  =  0.0 ;
            c.fill       = GridBagConstraints.NONE ;
            add( _loginLabel , c ) ;
            c.gridwidth  = 3 ; c.gridx = 1 ; c.gridy = 0 ; c.weightx  =  1.0 ;
            c.fill       = GridBagConstraints.HORIZONTAL ;
            add( _login , c ) ;

            c.gridwidth  = 1 ; c.gridx = 0 ; c.gridy = 1 ;
            c.fill       = GridBagConstraints.NONE ;
            add( _hostLabel , c ) ;
            c.gridwidth  = 3 ; c.gridx = 1 ; c.gridy = 1 ;
            c.fill       = GridBagConstraints.HORIZONTAL ;
            add( _host , c ) ;

            c.gridwidth  = 1 ; c.gridx = 0 ; c.gridy = 2 ;
            c.fill       = GridBagConstraints.NONE ;
            add( _portLabel , c ) ;
            c.gridwidth  = 3 ; c.gridx = 1 ; c.gridy = 2 ;
            c.fill       = GridBagConstraints.HORIZONTAL ;
            add( _port , c ) ;

            c.gridwidth  = 1 ; c.gridx = 0 ; c.gridy = 3 ;
            add( _protocolLabel , c ) ;
            c.gridwidth  = 1 ; c.gridx = 1 ; c.gridy = 3 ;
            add( _raw , c ) ;
            c.gridwidth  = 1 ; c.gridx = 2 ; c.gridy = 3 ;
            add( _ssh1 , c ) ;
            c.gridwidth  = 1 ; c.gridx = 3 ; c.gridy = 3 ;
            add( _ssh2 , c ) ;

            _protocol.add( _raw ) ;
            _protocol.add( _ssh1 ) ;
            _protocol.add( _ssh2 ) ;

            _raw.addActionListener(this);
            _ssh1.addActionListener(this);
            _ssh2.addActionListener(this);
            _raw.setActionCommand("raw");
            _ssh1.setActionCommand("ssh1");
            _ssh2.setActionCommand("ssh2");

/*
         _lm.addActionListener(
            new ActionListener(){
                public void actionPerformed( ActionEvent event ){
                   boolean selected = _lm.isSelected() ;
                   _schema.setEnabled( selected ) ;
                   _schemaLabel.setEnabled( selected ) ;
                }
            }
         ) ;
*/
            try{
                loadPreferences() ;
            }catch(Exception ee){
                System.out.println("Error loading preferences : "+ee) ;
            }
        }
        public void actionPerformed( ActionEvent event ){
            _protocolName = event.getActionCommand() ;
        }
    }

    private class ModulePanel extends CellGuiSkinHelper.CellPanel
            implements Applyable , ActionListener, KeyListener  {

        private class ModulePanelEntry {
            private String _moduleName   = "" ;
            private String _moduleClass  = "" ;
            private String _moduleLoader = "CLASSPATH" ;
            public boolean isValid(){
                if( _moduleName.equals("") || _moduleClass.equals("") )return false ;
                try{
                    Class x = Class.forName(_moduleClass) ;
                }catch(Exception ee ){
                    return false ;
                }
                return true ;
            }
            public String toString(){ return "("+_moduleName+","+_moduleClass+","+_moduleLoader+")" ; }
            public String getName(){ return _moduleName ; }
            public boolean equals( Object obj ){
                if( ! ( obj instanceof ModulePanelEntry ) )return false ;
                ModulePanelEntry other = (ModulePanelEntry)obj ;
                return _moduleName.equals(other._moduleName) &&
                        _moduleClass.equals(other._moduleClass) &&
                        _moduleLoader.equals(other._moduleLoader);
            }
        }
        private JLabel _nameLabel    = new JLabel( "Module Name" , JLabel.RIGHT ) ;
        private JTextField _name     = new JTextField(10) ;
        private JLabel _classLabel   = new JLabel( "Module Class" , JLabel.RIGHT ) ;
        private JTextField _class    = new JTextField(10) ;
        private JLabel _loaderLabel  = new JLabel( "Module Loader" , JLabel.RIGHT ) ;
        private JTextField _loader   = new JTextField(10) ;
        private JButton    _next     = new JButton("Next");
        private JButton    _previous = new JButton("Previous");
        private JButton    _delete   = new JButton("Delete");
        private JButton    _insert   = new JButton("Insert");
        private Preferences _preferences = null ;
        private ArrayList  _list     = new ArrayList() ;
        private JLabel _positionLabel  = new JLabel( "" , JLabel.CENTER ) ;
        private int _currentPosition = 0 ;
        public Insets getInsets(){ return new Insets(20,20,20,20) ; }
        public void reset()  throws Exception {
            loadList(_list) ;
            _currentPosition = 0 ;
            go();
        }
        public void apply()  throws Exception {
            if( ! updateCurrent() )deleteCurrent() ;
            go();
            saveList(_list) ;
        }
        private void loadList( java.util.List list )throws Exception {
            String [] children = _preferences.childrenNames();
            list.clear() ;
            for( int i = 0 , n = children.length ; i < n ; i++ ){
                Preferences p = _preferences.node(children[i]);
                ModulePanelEntry entry = new ModulePanelEntry() ;
                entry._moduleName   = p.get("name",null) ;
                entry._moduleClass  = p.get("class",null) ;
                entry._moduleLoader = p.get("loader","CLASSPATH") ;
                if( ( entry._moduleName  == null ) ||
                        ( entry._moduleClass == null ) )continue ;
                list.add(entry);
            }
        }
        private void saveList( java.util.List list ) throws Exception {
            String [] children = _preferences.childrenNames();
            for( int i = 0 , n = children.length ; i < n ; i++ )
                _preferences.node(children[i]).removeNode();

            Iterator i = list.iterator() ;
            for( int position = 0 ; i.hasNext() ; position++ ){
                ModulePanelEntry entry = (ModulePanelEntry)i.next() ;
                Preferences p = _preferences.node(""+position);
                p.put( "name" , entry._moduleName ) ;
                p.put( "class" , entry._moduleClass ) ;
                p.put( "loader" , entry._moduleLoader ) ;
            }
            _preferences.sync();
        }
        public ModulePanelEntry getModulePanelEntry(){
            ModulePanelEntry entry = new ModulePanelEntry() ;
            entry._moduleName   = _name.getText() ;
            entry._moduleClass  = _class.getText() ;
            entry._moduleLoader = _loader.getText() ;
            return entry ;
        }
        public void setModulePanelEntry( ModulePanelEntry entry ){
            _name.setText(entry._moduleName) ;
            _class.setText(entry._moduleClass) ;
            _loader.setText(entry._moduleLoader) ;
        }
        private ModulePanel( String name , Preferences pref ){
            _preferences = pref ;
            setLayout( new GridBagLayout() ) ;
            setBorder(
                    BorderFactory.createTitledBorder(" Cell Login Modules ")
            );

            GridBagConstraints c = new GridBagConstraints()  ;
            c.gridheight = 1 ;
            c.insets     = new Insets(4,4,4,4) ;

            c.gridx = 0 ; c.gridy = 0 ; c.weightx  =  0.0 ;
            c.gridwidth  = 1 ;
            c.fill       = GridBagConstraints.NONE ;
            add( _nameLabel , c ) ;

            c.gridx = 1 ; c.gridy = 0 ; c.weightx  =  1.0 ;
            c.gridwidth  = 2 ;
            c.fill       = GridBagConstraints.NONE ;
            add( _name , c ) ;

            c.gridx = 3 ; c.gridy = 0 ; c.weightx  =  0.0 ;
            c.gridwidth  = GridBagConstraints.REMAINDER ;
            c.fill       = GridBagConstraints.HORIZONTAL ;
            add( _positionLabel , c ) ;


            c.gridx = 0 ; c.gridy = 1 ; c.weightx  =  0.0 ;
            c.gridwidth  = 1 ;
            c.fill       = GridBagConstraints.NONE ;
            add( _classLabel , c ) ;

            c.gridx = 1 ; c.gridy = 1 ; c.weightx  =  1.0 ;
            c.gridwidth  = GridBagConstraints.REMAINDER ;
            c.fill       = GridBagConstraints.HORIZONTAL ;
            add( _class , c ) ;

            c.gridx = 0 ; c.gridy = 2 ; c.weightx  =  0.0 ;
            c.gridwidth  = 1 ;
            c.fill       = GridBagConstraints.NONE ;
            add( _loaderLabel , c ) ;

            c.gridx = 1 ; c.gridy = 2 ; c.weightx  =  1.0 ;
            c.gridwidth  = GridBagConstraints.REMAINDER ;
            c.fill       = GridBagConstraints.HORIZONTAL ;
            add( _loader , c ) ;

            c.gridx = 0 ; c.gridy = 3 ; c.weightx  =  1.0 ;
            c.gridwidth  = 1 ;
            c.fill       = GridBagConstraints.HORIZONTAL ;
            add( _previous , c ) ;

            c.gridx = 1 ; c.gridy = 3 ; c.weightx  =  1.0 ;
            c.gridwidth  = 1 ;
            c.fill       = GridBagConstraints.HORIZONTAL ;
            add( _next , c ) ;

            c.gridx = 2 ; c.gridy = 3 ; c.weightx  =  1.0 ;
            c.gridwidth  = 1 ;
            c.fill       = GridBagConstraints.HORIZONTAL ;
            add( _insert , c ) ;

            c.gridx = 3 ; c.gridy = 3 ; c.weightx  =  1.0 ;
            c.gridwidth  = 1 ;
            c.fill       = GridBagConstraints.HORIZONTAL ;
            add( _delete , c ) ;
            _delete.setForeground(Color.red);

            _loader.setText("CLASSPATH");
            _loader.setEnabled(false);

            _loaderLabel.setEnabled(false);


            java.util.List loadedList  = new ArrayList() ;
            java.util.List defaultList = new ArrayList() ;
            try{
                loadList( loadedList ) ;
            }catch(Exception ee ){
                System.err.println("can't load preferences : "+ee);
            }
            loadDefault(defaultList);

            if( mergeLists(  loadedList , defaultList , _list ) ){
                try{
                    saveList( _list ) ;
                }catch(Exception ee ){
                    System.err.println("can't save preferences : "+ee);
                }
            }

            _currentPosition = 0 ;

            _delete.addActionListener(this);
            _insert.addActionListener(this) ;
            _next.addActionListener(this) ;
            _previous.addActionListener(this) ;
//         _name.addKeyListener(this);
//         _class.addKeyListener(this) ;
//         _loader.addKeyListener(this) ;

            go() ;
        }
        private boolean mergeLists( java.util.List loaded , java.util.List def , java.util.List result ){
            Map map = new HashMap() ;
            boolean modified = false ;

            for( Iterator i = def.iterator() ; i.hasNext() ; ){
                ModulePanelEntry entry = (ModulePanelEntry)i.next() ;
                map.put( entry.getName() , entry ) ;
            }
            for( Iterator i = loaded.iterator() ; i.hasNext() ; ){

                ModulePanelEntry entry    = (ModulePanelEntry)i.next() ;
                ModulePanelEntry defentry = (ModulePanelEntry)map.get(entry.getName());

                if( defentry != null ){
                    if( ! entry.equals(defentry) ){
                        modified = true ;
                        System.out.println("Overwriting module "+entry+" by "+defentry);
                    }
                    result.add( defentry ) ;
                    def.remove( defentry ) ;
                }else{
                    result.add( entry ) ;
                    System.out.println("Keeping module "+entry );
                }

            }
            for( Iterator i = def.iterator() ; i.hasNext() ; ){
                ModulePanelEntry entry = (ModulePanelEntry)i.next() ;
                result.add(entry);
                System.out.println("Taking from default "+entry ) ;
                modified = true ;
            }
            return modified ;
        }
        public void keyTyped( KeyEvent event ){
            System.out.println("Modified");
        }
        public void keyPressed( KeyEvent event ){
        }
        public void keyReleased( KeyEvent event ){
        }
        private boolean updateCurrent(){
            ModulePanelEntry e2 = getModulePanelEntry();
            if( ! e2.isValid() )return false ;
            ModulePanelEntry entry = (ModulePanelEntry)_list.get(_currentPosition);
            if( ! e2.equals( entry ) )_list.set(_currentPosition,e2) ;
            return true ;
        }
        private void deleteCurrent(){
            _list.remove(_currentPosition) ;
            _currentPosition-- ;
            _currentPosition = Math.max( _currentPosition , 0 ) ;
        }
        public void actionPerformed( ActionEvent event ){
            Object source = event.getSource() ;
            if( source == _delete ){
                deleteCurrent() ;
                go() ;
            }else if( source == _next ){
                if( updateCurrent() ){
                    _currentPosition++ ;
                }else{
                    deleteCurrent() ;
                }
                go() ;
            }else if( source == _previous ){
                if( updateCurrent() ){
                    _currentPosition-- ;
                }else{
                    deleteCurrent() ;
                }
                go() ;

            }else if( source == _insert ){
                if( updateCurrent() ){
                    ModulePanelEntry entry = new ModulePanelEntry() ;
                    _list.add(_currentPosition+1,entry);
                    _currentPosition = _currentPosition+1 ;
                }else{
                    deleteCurrent() ;
                }
                go();
            }

        }
        private void go(){
            int size = _list.size() ;
            if( _currentPosition >= size )_currentPosition=0;
            ModulePanelEntry entry = (ModulePanelEntry)_list.get(_currentPosition);
            _positionLabel.setText(""+_currentPosition);
            setModulePanelEntry(entry);
            if( size < 1 )return ;
            if( size == 1 ){
                _delete.setEnabled( false ) ;
                _previous.setEnabled(false);
                _next.setEnabled(false) ;
                return ;
            }
            _delete.setEnabled( true ) ;
            if( _currentPosition == 0 ){
                _previous.setEnabled(false);
                _next.setEnabled(true) ;
            }else if( _currentPosition == (size - 1 ) ){
                _previous.setEnabled(true);
                _next.setEnabled(false) ;
            }else{
                _previous.setEnabled(true);
                _next.setEnabled(true) ;
            }

        }
        private String [][] _defaultPluggins = {
                { "Commander"  , "org.pcells.services.gui.XCommander"         , "CLASSPATH" } ,
                { "Restore"    , "org.dcache.gui.JRestoreDisplay"    , "CLASSPATH" } ,
                { "Transfer"   , "org.dcache.gui.JTransferPanel"     , "CLASSPATH" } ,
                { "Pools"      , "org.dcache.gui.JPoolCommandPanel"  , "CLASSPATH" } ,
                { "CostModule" , "org.dcache.gui.JCostPanel"         , "CLASSPATH" } ,
        };
        private void loadDefault( java.util.List list ){
            for( int i = 0 , n = _defaultPluggins.length ; i < n ; i++ ){
                ModulePanelEntry entry = new ModulePanelEntry() ;
                String [] x = _defaultPluggins[i] ;
                entry._moduleName   = x[0] ;
                entry._moduleClass  = x[1] ;
                entry._moduleLoader = x[2] ;
                list.add( entry ) ;
            }
        }
        private void loadDefault2( java.util.List list ){
            ModulePanelEntry entry = new ModulePanelEntry() ;
            entry._moduleName   = "Commander" ;
            entry._moduleClass  = "org.pcells.services.gui.XCommander" ;
            entry._moduleLoader = "CLASSPATH" ;
            list.add( entry ) ;
            entry = new ModulePanelEntry() ;
            entry._moduleName   = "Restore" ;
            entry._moduleClass  = "org.dcache.gui.RestoreDisplay" ;
            entry._moduleLoader = "CLASSPATH" ;
            list.add( entry ) ;
            entry = new ModulePanelEntry() ;
            entry._moduleName   = "CostModule" ;
            entry._moduleClass  = "org.dcache.gui.JCostPanel" ;
            entry._moduleLoader = "CLASSPATH" ;
            list.add( entry ) ;
        }
    }

    private class PlugPanel extends CellGuiSkinHelper.CellPanel  implements Applyable {

        private JLabel _nameLabel    = new JLabel( "Not yet supported" , JLabel.CENTER ) ;

        private Preferences _preferences = null ;

        public Insets getInsets(){ return new Insets(20,20,20,20) ; }
        public void reset() throws Exception {
            System.out.println("Cancelled");
        }
        public void apply() throws Exception {
        }
        private PlugPanel( String name , Preferences pref ){
            _preferences = pref ;
            setLayout( new GridBagLayout() ) ;
            setBorder(
                    BorderFactory.createTitledBorder(" JCommand Pluggin Setup ")
            );

            GridBagConstraints c = new GridBagConstraints()  ;
            c.gridheight = 1 ;
            c.insets     = new Insets(4,4,4,4) ;

            c.gridx = 0 ; c.gridy = 0 ; c.weightx  =  1.0 ;
            c.gridwidth  = GridBagConstraints.REMAINDER ;
            c.fill       = GridBagConstraints.HORIZONTAL ;

            ClassLoader loader = this.getClass().getClassLoader() ;
            if( loader instanceof CellGuiClassLoader ){
                CellGuiClassLoader ourLoader = (CellGuiClassLoader)loader ;
                add( createJarListPanel(ourLoader) , c ) ;
            }else{
                add( _nameLabel , c ) ;
            }
        }
        private JPanel createJarListPanel( CellGuiClassLoader loader ){
            JPanel panel = new JPanel( new GridLayout( 0,1 ) ) ;
            for( Iterator jars = loader.jars() ; jars.hasNext() ; ){
                CellGuiClassLoader.GuiJarEntry entry = (CellGuiClassLoader.GuiJarEntry)jars.next() ;
                String name = entry.getName() ;
                CellGuiClassLoader.Version version = entry.getVersion() ;
                String versionString = version == null ? "???" : version.toString() ;
                versionString = versionString == null ? "???" : versionString ;
                panel.add( new JLabel( name +" ("+versionString+")" )  ) ;
            }
            return panel ;
        }
    }

    private class Ssh2KeyPanel
            extends CellGuiSkinHelper.CellPanel
            implements Applyable {

        private JLabel _privateKeyLabel = new JLabel( "SSH private key" , JLabel.RIGHT ) ;
        private JTextField _privateKeyTextField = new JTextField(20) ;

        private Preferences _preferences = null ;

        public Insets getInsets(){ return new Insets(20,20,20,20) ; }
        public void reset()  throws Exception {
            loadPreferences();
        }
        public void apply()  throws Exception {
            String privateKeyPath = _privateKeyTextField.getText() ;
            if( privateKeyPath.equals("") )
                throw new IllegalArgumentException("Private key path not specified");
            storePreferences() ;
        }
        private void loadPreferences() throws Exception {
            String userHome = System.getProperties().getProperty("user.home");
            _privateKeyTextField.setText(_preferences.get("privateKeyPath", userHome+ "/.ssh" + File.separator + "id_dsa"));
        }

        private void storePreferences() throws Exception {
            _preferences.put("privateKeyPath", _privateKeyTextField.getText()) ;
            _preferences.sync();
        }
        private Ssh2KeyPanel( String name , Preferences pref ){

            _preferences = pref ;

            setLayout( new GridBagLayout() ) ;

            CellGuiSkinHelper.setComponentProperties(_privateKeyLabel) ;
            setBorder(
                    BorderFactory.createTitledBorder(" SSH keys in PEM format")
            );
            GridBagConstraints c = new GridBagConstraints()  ;
            c.gridheight = 1 ;
            c.insets     = new Insets(4,4,4,4) ;

            c.gridwidth  = 1 ; c.gridx = 0 ; c.gridy = 1 ;
            c.fill       = GridBagConstraints.NONE ;
            add(_privateKeyLabel, c ) ;
            c.gridwidth  = 3 ; c.gridx = 1 ; c.gridy = 1 ;
            c.fill       = GridBagConstraints.HORIZONTAL ;
            add(_privateKeyTextField, c ) ;

            try{
                loadPreferences() ;
            }catch(Exception ee){
                System.out.println("Error loading preferences : "+ee) ;
            }
        }
    }


    private ActionListener _actionListener = null;

    public synchronized void addActionListener(ActionListener l) {
        _actionListener = AWTEventMulticaster.add( _actionListener, l);
    }
    public synchronized void removeActionListener(ActionListener l) {
        _actionListener = AWTEventMulticaster.remove( _actionListener, l);
    }
    public void processEvent( ActionEvent e) {
        if( _actionListener != null)
            _actionListener.actionPerformed( e );
    }

}
