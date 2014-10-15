// $Id: YCommander.java,v 1.7 2007/03/11 16:33:19 cvs Exp $
//
package org.pcells.services.gui ;
//
import org.pcells.services.connection.DomainConnection;
import org.pcells.services.connection.DomainConnectionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class      YCommander
        extends    CellGuiSkinHelper.CellPanel
        implements ActionListener,
        DomainConnectionListener {

    private Logger _logger;

    private DomainConnection _connection    = null ;
    private EasyCommander    _commander     = null ;
    private CellDomainTree   _selection     = null ;
    private CardLayout       _cards         = null ;
    private JPanel           _cardsPanel    = null ;
    private JTopologyPane    _topology      = null ;
    private Preferences      _preferences   = null ;
    private Preferences      _filterPrefs   = null ;
    private Preferences      _plugginPrefs  = null ;
    private PlugginDialog    _plugginDialog = null ;
    private String          _currentAddress = null ;
    //
    // Preferences
    //
    //     CellLogin/<INSTANCE>/Modules/<MODULES_NAME>
    //
    //          .../Filters/[KEY_VALUES]
    //          .../pluggins/
    public YCommander( DomainConnection connection , Preferences pref ) {

        _logger = LoggerFactory.getLogger(YCommander.class);
        _connection = connection;
        _preferences = pref;

        _logger.debug(this.getClass().getName() + " loadeded by : " + this.getClass().getClassLoader().getClass().getName());

        _filterPrefs = pref.node("Filters");
        _plugginPrefs = pref.node("org/dcache/gui/pluggins");

        setLayout(new BorderLayout(4, 4));

        _commander = new EasyCommander(connection);

        _selection = new CellDomainTree(connection, pref);
        _selection.addActionListener(this);

        _topology = new JTopologyPane();
        _topology.setMessage("");
        _topology.addActionListener(this);

        _cards = new CardLayout();
        _cardsPanel = new CellGuiSkinHelper.CellPanel(_cards);

        JPanel left = new CellGuiSkinHelper.CellPanel(new BorderLayout(4, 4));
        left.add(_selection, "Center");

        Dimension d = new Dimension(0, 0);
//      left.setMinimumSize(d) ;
        _cardsPanel.setMinimumSize(d);

        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                left,
                _cardsPanel);


        CellGuiSkinHelper.setComponentProperties(split);
//      CellGuiSkinHelper.setComponentProperties( _selection ) ;
        CellGuiSkinHelper.setComponentProperties(_commander);

        _cardsPanel.add(_commander, MAPPING_COMMANDER);
        _cardsPanel.add(_topology, MAPPING_TOPOLOGY);

        _cards.show(_cardsPanel, MAPPING_TOPOLOGY);

        add(split, "Center");

//      split.resetToPreferredSizes();

        _plugginDialog = new PlugginDialog(null);
        try {
            _plugginDialog.installPreferences();
        } catch (BackingStoreException ee) {
            _logger.error("Can't install Prefereneces");
            ee.printStackTrace();
        }

        left.add(_plugginDialog.getComboBox(), "South");

    }
    public void actionPerformed( ActionEvent event ){
        Object source = event.getSource() ;
        if( source ==  _selection ){

            if( event instanceof CellDomainTree.CellDomainEvent ){

                CellDomainTree.CellDomainEvent e = (CellDomainTree.CellDomainEvent)event ;

                String address = _currentAddress = e.getAddress();
                CellDomainTree.CellFrame.FrameCellInfo info  = e.getCellInfo() ;

                if( address == null ){
                    _cards.show( _cardsPanel , MAPPING_TOPOLOGY ) ;
                }else{
                    if( info != null ){
                        for( Iterator i = _plugginDialog.mappings() ;  i.hasNext() ; ){
                            PlugginEntry entry = (PlugginEntry)i.next() ;
                            if( entry.matches( info ) ){
                                _cards.show( _cardsPanel , entry.getName() );
                                try{
                                    entry.setDestination(address);
                                }catch(Exception ee ){
                                    ee.printStackTrace();
                                }
                                return ;
                            }
                        }
                    }
                    _cards.show( _cardsPanel , MAPPING_COMMANDER ) ;
                    _commander.setDestination(address != null?address:"");
                }

            }else if( event instanceof CellDomainTree.DomainScanEvent ){

                CellDomainTree.DomainScanEvent e = (CellDomainTree.DomainScanEvent)event ;
                if( ! e.isScanDone() ){
                    _topology.setMessage("Scanning ...");
                }else{
                    _topology.setTopology( e.getTopology());
                }

            }

        }else if( source == _topology ){

            JTopologyPane.TopologyEvent t = (JTopologyPane.TopologyEvent)event ;
            MouseEvent mouse = t.getMouseEvent();
            if( mouse.getButton() == MouseEvent.BUTTON3 ){
                try{
                    _selection.doDomainScan();
                }catch(Exception ee ){
                    ee.printStackTrace();
                }
            }else{

                String domainName = t.getDomainName() ;
                if( domainName != null ){
                    _selection.setDomain( domainName ) ;
                }
            }
        }
    }
    public void domainAnswerArrived( Object obj , int id ){

    }

    public class PlugginEntry {
        private String     _name           = null ;
        private Class      _class          = null ;
        private JComponent _pluggin        = null ;
        private Method     _setDestination = null ;
        private Pattern    _nameTemplate   = null ;
        private Pattern    _classTemplate  = null ;

        private PlugginEntry( String plugginName , Class cl , JComponent panel , Method mt ){
            _name           = plugginName ;
            _class          = cl ;
            _pluggin        = panel ;
            _setDestination = mt ;
        }
        public JComponent getPanel(){
            return _pluggin ;
        }
        public void setDestination( String destination ) throws IllegalAccessException , InvocationTargetException{

            Object [] args = { destination } ;

            _setDestination.invoke( _pluggin , args ) ;
        }
        public Class getPanelClass(){ return _class ; }
        public String getName(){ return _name ; }
        public String toString(){ return _name ; }
        public void setNameTemplate( String nameTemplate ) throws PatternSyntaxException {
            _nameTemplate = ( nameTemplate == null ) || ( nameTemplate.length() == 0 ) ? null : Pattern.compile( nameTemplate ) ;
        }
        public void setClassTemplate( String classTemplate ) throws PatternSyntaxException {
            _classTemplate = ( classTemplate == null ) || ( classTemplate.length() == 0 ) ? null : Pattern.compile( classTemplate ) ;
        }
        public boolean matches( CellDomainTree.CellFrame.FrameCellInfo info ){

            if( ( _nameTemplate == null ) && ( _classTemplate == null ) )return false ;

            boolean
                    nameOk = ( _nameTemplate == null ) ||
                    _nameTemplate.matcher( info.getCellName() ).matches() ;

            boolean
                    classOk = ( _classTemplate == null ) ||
                    _classTemplate.matcher( info.getCellClass() ).matches() ;

            return nameOk && classOk ;
        }
    }
    private Map loadLocalFile( String filename ) throws FileNotFoundException , IOException, java.net.MalformedURLException {

        URL fileURL = new URL( JMultiLogin.__classLoader.getBase() +"/"+filename ) ;
        Map map     = new HashMap() ;

        BufferedReader br = new BufferedReader( new InputStreamReader( fileURL.openStream() ) ) ;
        try{
            while( true ){
                String line = br.readLine() ;
                if( line == null )break ;
                //_logger.error("DEBUG : readline : "+line ) ;
                line = line.trim() ;
                if( ( line.length() == 0 ) || ( line.startsWith("#") ) )continue ;
                String [] list = line.split(":") ;
                map.put( list[0] , list ) ;
            }
        }finally{
            try{ br.close() ; }catch(Exception ee){}
        }
        return map ;
    }
    private PlugginEntry tryToLoadPluggin( String plugginName , String className ) throws Exception {

        ClassLoader loader = this.getClass().getClassLoader() ; // JMultiLogin.__classLoader ;
        Preferences  privatePrefs = _plugginPrefs.node(plugginName);

        Class  [][] classArgs  =
                {  { org.pcells.services.connection.DomainConnection.class , java.util.prefs.Preferences.class } ,
                        { org.pcells.services.connection.DomainConnection.class  } ,
                        {}
                } ;

        Object [][] objectArgs =
                {  { _connection  , privatePrefs } ,
                        { _connection  } ,
                        {}
                } ;

        Class  [] setClasses    = { java.lang.String.class } ;

        Exception loadFailed = null ;
        for( int i = 0 ; i < classArgs.length ; i++ ){
            try{
                Class       cn = loader.loadClass( className ) ;
                _logger.debug("ClassName: {}", className);
                Constructor cc = cn.getConstructor( classArgs[i] ) ;
                _logger.debug("Constructor: {}", cc);
                JComponent  cp = (JComponent) cc.newInstance( objectArgs[i] ) ;
                Method      mt = cn.getMethod( "setDestination" , setClasses ) ;
                _logger.debug("Method: {}", mt);

                CellGuiSkinHelper.setComponentProperties( cp ) ;

                return new PlugginEntry( plugginName , cn , cp , mt ) ;

            }catch(Exception ee ){
                _logger.error("tryToLoadPluggin failed for Class: " + className + " with exception: " + ee.getStackTrace());
                loadFailed = ee ;
            }
        }
        throw loadFailed ;
    }
    public void setDestination( String destination ){
        _commander.setDestination(destination);
    }
    private final String MAPPING_LAUNCH    = "Launch Mappings" ;
    private final String MAPPING_COMMANDER = "Easy Commander" ;
    private final String MAPPING_TOPOLOGY  = "Topology" ;
    private final String MAPPING_FILTER    = "Pluggin Filter" ;

    private class PlugginDialog extends JDialog implements ActionListener, ListSelectionListener {

        private JLabel     _nameLabel      = new JLabel("Name") ;
        private JLabel     _classLabel     = new JLabel("Class") ;
        private JLabel     _nameTempLabel  = new JLabel("Name Template") ;
        private JLabel     _classTempLabel = new JLabel("Class Template") ;
        private JTextField _nameField      = new JTextField("") ;
        private JTextField _classField     = new JTextField(40) ;
        private JTextField _nameTempField  = new JTextField(40) ;
        private JTextField _classTempField = new JTextField(40) ;
        private JButton    _addButton      = new JButton("Add/Modify");
        private JButton    _rmButton       = new JButton("Remove");
        private JComboBox  _box            = new JComboBox() ;
        private JTable     _table          = null ;

        private ListSelectionModel _selectionModel = null ;
        private PlugginContainer   _tableModel     = new PlugginContainer() ;

        public class PlugginContainer extends AbstractTableModel {

            private ArrayList _list     = new ArrayList() ;
            private HashMap   _mapping = new HashMap() ;

            public int getColumnCount() { return 4; }
            public int getRowCount() { return _list.size() ;}
            public String getColumnName( int pos ){
                switch( pos ){
                    case 0 : return "Name" ;
                    case 1 : return "Class" ;
                    case 2 : return "Cell Name Pattern" ;
                    case 3 : return "Cell Class Pattern" ;
                }
                return "" ;
            }
            public PlugginEntry get( int row ){ return (PlugginEntry)_list.get(row); }
            public Object getValueAt(int row, int col) {
                PlugginEntry entry = (PlugginEntry)_list.get(row);
                switch( col ){
                    case 0 :
                        return entry.getName() ;
                    case 1 :
                        return entry.getPanelClass().getName() ;
                    case 2 :
                        return entry._nameTemplate == null ? "" : entry._nameTemplate.pattern() ;
                    case 3 :
                        return entry._classTemplate == null ? "" : entry._classTemplate.pattern() ;
                }
                return null ;
            }
            public void add( PlugginEntry entry ){
                _mapping.put( entry.getName() , entry ) ;
                _list.add(entry);
                fireTableDataChanged() ;
            }
            public PlugginEntry remove( String name ){
                PlugginEntry entry = (PlugginEntry)_mapping.remove(name) ;
                if( entry == null )return  null ;
                _list.remove(entry) ;
                fireTableDataChanged();
                return entry ;
            }
            public void remove( PlugginEntry entry ){
                _list.remove(entry);
                _mapping.remove( entry.getName() ) ;
                fireTableDataChanged() ;
            }
            public PlugginEntry get( String name ){
                return (PlugginEntry)_mapping.get(name) ;
            }
            public Iterator mappings(){
                Map r = new TreeMap( _mapping ) ;
                return r.values().iterator() ;
            }
        }
        public PlugginDialog( java.awt.Frame frame ){
            super( frame , "Pluggin Filter" , false ) ;

            _addButton.addActionListener(this) ;
            _rmButton.addActionListener(this) ;
            _nameField.addActionListener(this) ;
            _classField.addActionListener(this) ;
            _nameTempField.addActionListener(this) ;
            _classTempField.addActionListener(this) ;
            _box.addActionListener(this);

            _box.addItem(MAPPING_FILTER);
            _box.addItem(MAPPING_LAUNCH);
            _box.addItem(MAPPING_COMMANDER);
            _box.addItem(MAPPING_TOPOLOGY);

            CellGuiSkinHelper.setComponentProperties( _rmButton ) ;
            CellGuiSkinHelper.setComponentProperties( _addButton ) ;
            CellGuiSkinHelper.setComponentProperties( _box ) ;

            GridBagLayout     lo = new GridBagLayout() ;
            GridBagConstraints c = new GridBagConstraints()  ;
            JPanel panel = new CellGuiSkinHelper.CellPanel( lo ) ;
            panel.setBorder( BorderFactory.createRaisedBevelBorder() ) ;

            c.gridheight = 1 ;
            c.insets     = new Insets(4,4,4,4) ;

            c.fill       = GridBagConstraints.HORIZONTAL ;
            c.weightx    = 1.0 ;

            c.gridwidth  = 1 ; c.gridx = 0 ; c.gridy = 0 ;
            panel.add( _nameLabel , c ) ;
            c.gridwidth  = 1 ; c.gridx = 1 ; c.gridy = 0 ;
            panel.add( _nameField , c ) ;
            c.gridwidth  = 1 ; c.gridx = 2 ; c.gridy = 0 ;
            panel.add( _addButton , c ) ;
            c.gridwidth  = 1 ; c.gridx = 3 ; c.gridy = 0 ;
            panel.add( _rmButton , c ) ;

            c.gridwidth  = 1 ; c.gridx = 0 ; c.gridy = 1 ;
            panel.add( _classLabel , c ) ;
            c.gridwidth  = 3 ; c.gridx = 1 ; c.gridy = 1 ;
            panel.add( _classField , c ) ;

            c.gridwidth  = 1 ; c.gridx = 0 ; c.gridy = 2 ;
            panel.add( _nameTempLabel , c ) ;
            c.gridwidth  = 3 ; c.gridx = 1 ; c.gridy = 2 ;
            panel.add( _nameTempField , c ) ;

            c.gridwidth  = 1 ; c.gridx = 0 ; c.gridy = 3 ;
            panel.add( _classTempLabel , c ) ;
            c.gridwidth  = 3 ; c.gridx = 1 ; c.gridy = 3 ;
            panel.add( _classTempField , c ) ;

            Container surface = getContentPane() ;
            surface.add( panel , "North") ;

            _table = new JTable( _tableModel ) ;
            _table.setPreferredScrollableViewportSize( panel.getPreferredSize() /* new Dimension( 0 , 200  ) */ ) ;
            surface.add( new JScrollPane(_table) , "Center" ) ;

            _selectionModel = _table.getSelectionModel() ;
            _selectionModel.setSelectionMode( ListSelectionModel.SINGLE_SELECTION ) ;
            _selectionModel.addListSelectionListener(this);
            pack();

            //   setResizable(false);
            //   setUndecorated(true);
        }
        public Iterator mappings(){ return _tableModel.mappings() ; }
        private void installPreferences() throws BackingStoreException {

            String []   prefs = _filterPrefs.childrenNames() ;
            try{

                Map predefinedMap =  loadLocalFile( "YCommander.plugins" );
                //
                // remove those from the predefined set which already are in the
                // references list.
                //
                // as no body is modifying this anyway we can load the entire predefined list.
                //
                if( 3 == 4 ){
                    for( int i = 0 ; i < prefs.length ; i++ ){

                        String plugginName  = prefs[i] ;
                        Preferences  child  = _filterPrefs.node(plugginName) ;
                        String plugginClass = child.get( "className"  , null ) ;
                        if( plugginClass == null )continue ;

                        predefinedMap.remove( plugginName ) ;
                    }

                }else{
                    // instead we always us the predefined stuff.

                    _logger.error("REF : Cleaning filter prefs : " + _filterPrefs);
                    prefs = _filterPrefs.childrenNames() ;
                    for( int i = 0 ; i < prefs.length ; i++ )_filterPrefs.node(prefs[i]).removeNode() ;
                    prefs = _filterPrefs.childrenNames() ;
                    _logger.error("REF : Cleaning filter prefs : " + _filterPrefs + " size " + prefs.length);
                }
                //
                // now copy the remaining content of the predefined values
                // to the preferences.
                //
                for( Iterator x = predefinedMap.values().iterator() ; x.hasNext() ; ){
                    String [] values = (String [])x.next() ;
                    if( values.length < 2 )continue ;
                    Preferences child = _filterPrefs.node( values[0] ) ;
                    child.put( "className" , values[1] ) ;
                    if( values.length > 2 )child.put( "namePattern"  , values[2] ) ;
                    if( values.length > 3 )child.put( "classPattern" , values[3] ) ;
                }
                _logger.error("REF : Cleaning filter prefs : " + _filterPrefs + " size " + prefs.length);
                _filterPrefs.sync();
            }catch( Exception ee ){
                ee.printStackTrace();
            }
            //
            // now load the preferences
            //
            prefs = _filterPrefs.childrenNames() ;
            for( int i = 0 ; i < prefs.length ; i++ ){

                String plugginName  = prefs[i] ;
                Preferences  child  = _filterPrefs.node(plugginName) ;
                String plugginClass = child.get( "className"  , null ) ;
                if( plugginClass == null )continue ;

                try{
                    PlugginEntry panel = tryToLoadPluggin( plugginName , plugginClass ) ;

                    String nameTemplate  = child.get( "namePattern" , null ) ;
                    String classTemplate = child.get( "classPattern", null ) ;

                    try{
                        panel.setNameTemplate(nameTemplate) ;
                    }catch(Exception ee ){
                        _logger.error("Illegal Pattern : " + nameTemplate);
                    }
                    try{
                        panel.setClassTemplate(classTemplate) ;
                    }catch(Exception ee ){
                        _logger.error("Illegal Pattern : " + classTemplate);
                    }
                    _cardsPanel.add( panel.getPanel()  , panel.getName() ) ;
                    _box.addItem(panel);
                    _tableModel.add(panel);
                }catch(Exception ee ){
                    _logger.error("YCommander : couldn't load " + plugginName + " : " + ee);
                }
            }
        }
        public JComboBox getComboBox(){ return _box ; }
        private void clearFilterForm(){
            setFilterForm( (PlugginEntry) null ) ;
        }
        private void setFilterForm( String plugginName ){
            setFilterForm( plugginName == null ? null : _tableModel.get( plugginName ) ) ;
        }
        private void setFilterForm( PlugginEntry plugginObject ){
            _nameField.setForeground(Color.black);
            _classField.setForeground(Color.green);
            _classTempField.setForeground(Color.black) ;
            _nameTempField.setForeground(Color.black) ;
            if( plugginObject != null ){
                _nameField.setText( plugginObject.getName() ) ;
                _classField.setText( plugginObject.getPanelClass().getName() ) ;
                Pattern p = plugginObject._nameTemplate ;
                _nameTempField.setText( p == null ? "" : p.pattern() ) ;
                p = plugginObject._classTemplate ;
                _classTempField.setText( p == null ? "" : p.pattern() ) ;
            }else{
                _classField.setText( "" ) ;
                _nameTempField.setText( "" ) ;
                _classTempField.setText( "" ) ;
            }

        }
        public void valueChanged( ListSelectionEvent event ){
            if( event.getValueIsAdjusting() )return ;
            int pos = _table.getSelectedRow() ;
            if( pos < 0 ){
                clearFilterForm() ;
                _box.setSelectedItem(MAPPING_COMMANDER);
            }else{
                PlugginEntry entry = _tableModel.get(pos) ;
                setFilterForm(entry);
                _box.setSelectedItem(entry);
            }
        }
        public void actionPerformed( ActionEvent event ){

            Object source        = event.getSource() ;
            String plugginName   = _nameField.getText().trim() ;
            String plugginClass  = _classField.getText().trim() ;
            String templateName  = _nameTempField.getText().trim() ;
            String templateClass = _classTempField.getText().trim() ;

            if( ( source == _rmButton ) && ( plugginName.length()  > 0 ) ){

                PlugginEntry plugginObject = _tableModel.remove( plugginName );
                if( plugginObject != null ){
                    _cardsPanel.remove( plugginObject.getPanel() ) ;
                    _box.removeItem( plugginObject ) ;
                    try{
                        Preferences  prefs = _filterPrefs.node(plugginName);
                        prefs.removeNode() ;
                    }catch(BackingStoreException e ){ e.printStackTrace() ;}
                    clearFilterForm() ;
                }

            }else
            if( ( source == _addButton )     || ( source == _classField     ) ||
                    ( source == _nameTempField ) || ( source == _classTempField )    ){

                if( ( plugginName.length()  > 0 ) && ( plugginClass.length() > 0 ) ){

                    Preferences  prefs = _filterPrefs.node(plugginName);
                    try{
                        PlugginEntry panel = tryToLoadPluggin( plugginName , plugginClass ) ;
                        try{
                            if( templateName.length() > 0 )panel.setNameTemplate(templateName) ;
                        }catch( PatternSyntaxException e ){
                            _nameTempField.setForeground(Color.red) ;
                            throw e ;
                        }
                        try{
                            if( templateClass.length() > 0 )panel.setClassTemplate(templateClass) ;
                        }catch( PatternSyntaxException e ){
                            _classTempField.setForeground(Color.red) ;
                            throw e ;
                        }

                        PlugginEntry plugginObject = _tableModel.remove( plugginName );
                        if( plugginObject != null ){
                            _cardsPanel.remove( plugginObject.getPanel() ) ;
                            _box.removeItem( plugginObject ) ;
                            prefs.removeNode() ;

                        }
                        prefs = _filterPrefs.node(plugginName);
                        prefs.put( "className"    , plugginClass ) ;
                        prefs.put( "namePattern"  , templateName  )  ;
                        prefs.put( "classPattern" , templateClass) ;

                        _cardsPanel.add( panel.getPanel()  , panel.getName() ) ;
                        _box.addItem(panel);
                        _tableModel.add(panel);


                        clearFilterForm() ;

                    }catch(Exception ee ){
                        ee.printStackTrace();
                        _classField.setForeground(Color.red) ;
                    }finally{
                        try{ prefs.sync(); }catch(BackingStoreException e ){ e.printStackTrace() ;}
                    }
                }
            }else if( source == _nameField ){

                setFilterForm( plugginName ) ;

            }else if( source == _box ){
                Object item = _box.getSelectedItem() ;
                if( item instanceof String ){
                    String command = (String)item ;
                    if( command.equals( MAPPING_LAUNCH) ){
                        this.show() ;
                    }else if( command.equals( MAPPING_FILTER) ){
                    }else if( command.equals( MAPPING_COMMANDER ) ){
                        _cards.show( _cardsPanel , item.toString() ) ;
                        _commander.setDestination( _currentAddress== null ? "" : _currentAddress);
                    }else{
                        _cards.show( _cardsPanel , item.toString() ) ;
                    }
                }else{
                    PlugginEntry pluggin = (PlugginEntry)item ;
                    _cards.show( _cardsPanel , pluggin.getName() ) ;
                    try{
                        pluggin.setDestination(_currentAddress== null ? "" : _currentAddress);
                    }catch(Exception ee ){
                        ee.printStackTrace();
                    }
                }
            }
        }
        //public Insets getInsets(){
        //   return new Insets(10,10,10,10) ;
        //}
    }

}
