// $Id: LoginBrokerPanel.java,v 1.3 2007/03/16 05:48:49 cvs Exp $
//
package org.pcells.services.gui.login ;
//
import dmg.cells.nucleus.NoRouteToCellException;
import dmg.cells.services.login.LoginBrokerInfo;
import org.pcells.services.connection.DomainConnection;
import org.pcells.services.connection.DomainConnectionListener;
import org.pcells.services.gui.CellGuiSkinHelper;
import org.pcells.services.gui.util.RowObjectTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class      LoginBrokerPanel
        extends    CellGuiSkinHelper.CellPanel
        implements ActionListener , DomainConnectionListener {

    private static Logger _logger = LoggerFactory.getLogger(LoginBrokerPanel.class);

    private DomainConnection _connection    = null ;
    private Preferences      _preferences   = null ;
    private JTable           _table         = new JTable();
    private LoginBrokerModel _model         = null , _longModel = null , _shortModel = null ;
    private JButton          _doorBroker    = new JButton("DoorBroker");
    private JButton          _srmBroker     = new JButton("SrmBroker");
    private JButton          _serviceBroker = new JButton("ServiceBroker");
    private JButton          _updateButton  = new JButton("Update");
    private JTextField       _anyBroker     = new JTextField(10);
    private JLabel           _brokerLabel   = new JLabel("Brokers");
    private JCheckBox        _verifyBox     = new JCheckBox("Long Format");
    private String     _currentDestination  = "?";
    private static Pattern __loginInfoPattern = null ;
    private static String  __loginPattern   = "[ ]*Logins/max[ ]+:[ ]+([0-9]+)\\([0-9]+\\)\\/([0-9]+)[ ]*";
    private LoginBrokerInfoRow [] _infoList = null ;
    private JPanel         _topLeftPanel    = new JPanel( new FlowLayout( FlowLayout.TRAILING, 4 , 4) ) ;

    private String [] _headerLong = {
            "Cell","Domain",
            "P-Family" , "P-Version" , "P-Engine" ,
            "Host" , "Port" , "Load" , "Children" , "Max Children" , "Update"
    } ;
    private int [] _mapLong = { 0 , 1 , 2, 3 ,4 , 5, 6, 7, 8, 9, 10 } ;

    private String [] _headerShort = {
            "Cell","Domain",
            "P-Family" , "P-Version" ,
            "Host" , "Port" , "Load" , "Update"
    } ;
    private int [] _mapShort = { 0 , 1 , 2, 3 , 5, 6, 7, 10 } ;

    static {
        __loginInfoPattern = Pattern.compile(__loginPattern);
    }
    //
    private void setCurrentModel( LoginBrokerModel model ){
        _model = model ;
        _table.setModel(_model);
    }
    public LoginBrokerPanel( DomainConnection connection , Preferences pref ){
        setLayout( new BorderLayout(4,4) ) ;

        setBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder( Color.blue , 1 ) , "Service Brokers" ) ) ;

        _connection  = connection ;
        _preferences = pref ;


        _longModel  = new LoginBrokerModel( _headerLong , _mapLong ) ;
        _shortModel = new LoginBrokerModel( _headerShort , _mapShort ) ;

        _model = _shortModel ;

        _table.setSelectionMode( ListSelectionModel.SINGLE_SELECTION);
        _table.setModel( _model ) ;
        CellGuiSkinHelper.setComponentProperties( _table ) ;

        JTableHeader header = _table.getTableHeader() ;
        header.addMouseListener(_longModel);
        header.addMouseListener(_shortModel);
//      header.setDefaultRenderer(_model.getRenderer());

        add( new JScrollPane( _table ) , "Center" ) ;

        _srmBroker.addActionListener(this);
        _doorBroker.addActionListener(this);
        _serviceBroker.addActionListener(this);
        _anyBroker.addActionListener(this);
        _updateButton.addActionListener(this);
        _verifyBox.setSelected(false);

        CellGuiSkinHelper.setComponentProperties( _srmBroker ) ;
        CellGuiSkinHelper.setComponentProperties( _doorBroker ) ;
        CellGuiSkinHelper.setComponentProperties( _serviceBroker ) ;
        CellGuiSkinHelper.setComponentProperties( _anyBroker ) ;
        CellGuiSkinHelper.setComponentProperties( _updateButton ) ;
        CellGuiSkinHelper.setComponentProperties( _verifyBox ) ;

        JPanel topPanel = new JPanel( new BorderLayout(4,4) ) ;
        CellGuiSkinHelper.setComponentProperties( topPanel ) ;
        CellGuiSkinHelper.setComponentProperties( _topLeftPanel ) ;

        _topLeftPanel.add( _verifyBox);
        _topLeftPanel.add( _doorBroker);
        _topLeftPanel.add( _srmBroker);
        _topLeftPanel.add( _serviceBroker);
        topPanel.add( _topLeftPanel , "West" ) ;

        JPanel topRightPanel = new JPanel( new GridBagLayout() ) ;
        CellGuiSkinHelper.setComponentProperties( topRightPanel ) ;
        GridBagConstraints c = new GridBagConstraints()  ;
        c.fill  = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0 ;
        c.ipadx   =  10 ;

        topRightPanel.add( _anyBroker , c ) ;

        topPanel.add( topRightPanel  );

        add( topPanel , "North");


    }
    public void setDestination( String destination ){
        _topLeftPanel.remove( _doorBroker);
        _topLeftPanel.remove( _srmBroker);
        _topLeftPanel.remove( _serviceBroker);
        _topLeftPanel.add( _updateButton ) ;
        _anyBroker.setText( destination ) ;
        _anyBroker.setEnabled(false);
        _currentDestination = destination ;
    }
    public void actionPerformed( ActionEvent event ){
        Object source = event.getSource() ;
        String destination = null ;
        if( source ==  _doorBroker ){
            destination = "LoginBroker" ;
        }else if( source ==  _srmBroker ){
            destination = "srm-LoginBroker" ;
        }else if( source ==  _serviceBroker ){
            destination = "service-LoginBroker" ;
        }else if( source ==  _anyBroker ){
            destination = _anyBroker.getText().trim() ;
        }else if( source == _updateButton ){
            destination = _currentDestination ;
        }
        _logger.debug("LoginBrokerPanel destination: {} source of event: {}", destination, source);
        if( ( destination == null ) || ( destination.length() == 0 ) )return ;
        _anyBroker.setText( destination ) ;
        String command = "ls -l -binary" ;

        try{
            _logger.debug("Send message to {}, command: {}", destination, command);
            _connection.sendObject( destination , command , this , 1000 ) ;
            _logger.debug("Message has been sent.");
            _currentDestination = destination ;
            setCurrentModel( _verifyBox.isSelected() ? _longModel : _shortModel ) ;
        }catch(Exception ee ){
            _logger.error("Problem sending object: {} to {}", ee, destination);
            JOptionPane.showMessageDialog(
                    this,
                    destination+" reported "+ee.getMessage()  ,
                    "Problem in "+destination ,
                    JOptionPane.ERROR_MESSAGE ) ;

        }
    }
    public void domainAnswerArrived( Object obj , int id ){
        _logger.debug("Domain answer arrived. {} and ID: {}", obj.getClass(), id);
        if( id == 1000 ){
            String destination = _currentDestination ;
            if( obj == null ){
                JOptionPane.showMessageDialog(
                        this,
                        "Request to "+destination+" timed out"  ,
                        "Problem in "+destination ,
                        JOptionPane.ERROR_MESSAGE ) ;
                return ;
            }else if( obj instanceof NoRouteToCellException ){
                _logger.error("Could not find {}", destination);
                JOptionPane.showMessageDialog(
                        this,
                        destination+" not found"  ,
                        "Problem in "+destination ,
                        JOptionPane.ERROR_MESSAGE ) ;
                return ;
            }else if( ! ( obj instanceof LoginBrokerInfo [] ) ){
                JOptionPane.showMessageDialog(
                        this,
                        destination+" returned illegal class "+obj.getClass().getName()  ,
                        "Problem in "+destination ,
                        JOptionPane.ERROR_MESSAGE ) ;
                return ;
            }
            loginBrokerInfoArrived( (LoginBrokerInfo [])obj ) ;
        }else if( id >= 2000 ){

            int pos = id - 2000 ;
            if( obj instanceof String ){

                doorInfoArrived( (String)obj , pos ) ;

            }
        }
    }
    private void doorInfoArrived( String infoString , int pos ){
        LoginBrokerInfoRow [] rows = _infoList ;
        if( ( rows == null ) || ( pos >= rows.length ) ){
            _logger.error("doorInfoArrived with illegal position : "+pos+" max "+rows.length);
            return ;
        }
        StringTokenizer st = new StringTokenizer( infoString ,"\n" ) ;
        int children = -1 , maxChildren = 0 ;
        while( st.hasMoreTokens() ){
            String line = st.nextToken();
            Matcher m = __loginInfoPattern.matcher(line);
            if( ! m.matches() || ( m.groupCount() < 2 ))continue ;
            children    = Integer.parseInt( m.group(1) ) ;
            maxChildren = Integer.parseInt( m.group(2) ) ;
            break ;
        }
        _logger.debug(" arrived : "+pos+" "+children+" max "+maxChildren);
        _infoList[pos].setChildren( children , maxChildren ) ;
        _model.fire();

    }
    private void loginBrokerInfoArrived( LoginBrokerInfo [] info ){
        _logger.debug("LoginBroker info arrived: {}", info);
        if( _verifyBox.isSelected() ){
            _infoList = new LoginBrokerInfoRow[info.length] ;
            _model.clear() ;
            for( int i = 0 ; i < info.length ; i++ ){
                _infoList[i] = new LoginBrokerInfoRow(info[i]) ;
                _model.add( _infoList[i] ) ;
                String destination = _infoList[i].getServiceAddress() ;
                try{
                    _connection.sendObject( destination , "info" , this , 2000+i );
                    //_connection.sendObject( destination , "ls children -binary" , this , 2000+i );
                }catch(Exception ee ){
                    _logger.error("Could't send 'ls children' to "+destination+" : "+ee);
                    _infoList[i].setChildren(-1,0);
                }
            }
            _model.fire();
        }else{
            _model.clear() ;
            for( int i = 0 ; i < info.length ; i++ ){
                _model.add( new LoginBrokerInfoRow(info[i]) ) ;
            }
            _model.fire();
        }
    }
    public class LoginBrokerModel extends RowObjectTableModel {
        private int [] _map = null ;
        public LoginBrokerModel( String [] header , int [] map  ){
            super(header);
            _map = map ;
        }
        public LoginBrokerInfo getLoginBrokerAt( int pos ){
            return (LoginBrokerInfo)getRowAt(pos) ;
        }
        public int [] getMap(){ return _map ; }
    }
    public class LoginBrokerInfoRow implements RowObjectTableModel.SimpleTableRow {
        private Object []       _values      = null ;
        private LoginBrokerInfo _info        = null ;
        private int             _children    = 0 ;
        private int             _maxChildren = 0 ;

        private LoginBrokerInfoRow( LoginBrokerInfo info ){
            _info = info ;
            init() ;
        }
        private void setChildren( int children , int maxChildren ){
            _children    = children ;
            _maxChildren = maxChildren;
            _values[8] = new Integer( _children ) ;
            _values[9] = new Integer( _maxChildren ) ;
        }
        private String getServiceAddress(){ return _info.getCellName()+"@"+_info.getDomainName() ; }
        private void init(){
            _values = new Object[11] ;
            int pos = 0 ;
            _values[0] = _info.getCellName() ;
            _values[1] = _info.getDomainName() ;
            _values[2] = _info.getProtocolFamily() ;
            _values[3] = _info.getProtocolVersion() ;
            _values[4] = _info.getProtocolEngine() ;
            String [] hosts = _info.getHosts();
            StringBuffer sb = new StringBuffer();
            for( int i = 0 ; i < hosts.length ; i++ ){
                sb.append(hosts[i]).append(";");
            }
            _values[5] = sb.toString();
            _values[6] = new Integer( _info.getPort() ) ;
            _values[7] = new Float( (float)_info.getLoad() ) ;
            _values[8] = new Integer( _children ) ;
            _values[9] = new Integer( _maxChildren ) ;
            _values[10] = new Integer( (int)(_info.getUpdateTime()/1000L) ) ;
        }
        public Object getValueAtColumn( int column ){
            LoginBrokerModel lpm = (LoginBrokerModel)_table.getModel() ;
            return column < _values.length ? _values[lpm.getMap()[column]] : null ;
        }
        public Component renderCell(Component component , Object value , boolean isSelected ,
                                    boolean isFocussed , int row , int column ){

            if( value == null )return null ;
            ((JLabel)component).setHorizontalAlignment( JLabel.CENTER ) ;
            return component ;
        }

    }
    public static void main( String [] args )throws Exception {

        Pattern p = Pattern.compile(args[0]) ;
        Matcher m = p.matcher(args[1]);
        _logger.debug("Matches : "+m.matches());
        int count = m.groupCount() ;
        for( int i = 0 ; i <= count ; i++ ){
            _logger.debug("Group "+i+" : "+m.group(i));
        }
    }
}
