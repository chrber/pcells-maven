// $Id: JCommander.java,v 1.3 2006/11/12 16:38:18 cvs Exp $
//
package org.pcells.services.gui ;
//
import org.pcells.services.connection.DomainConnection;
import org.pcells.services.connection.DomainConnectionListener;
import org.pcells.services.connection.DomainEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class      JCommander
        extends    CellGuiSkinHelper.CellPanel
        implements DomainConnectionListener,
        DomainEventListener {

    private Logger _logger;

    private DomainConnection _connection = null ;
    private Font         _bigFont      = new Font( "Times" , Font.BOLD , 26 ) ;
    private JTextField   _commandField = new JTextField() ;
    private JTextField   _destination  = new JTextField() ;
    private DisplayPanel _displayPanel = null ;

    public JCommander( DomainConnection connection ) {

        _logger = LoggerFactory.getLogger(JCommander.class);
        _connection = connection;
        _connection.addDomainEventListener(this);

        BorderLayout l = new BorderLayout();
        l.setVgap(10);
        l.setHgap(10);
        setLayout(l);

        _logger.debug("Background color of Commander : " + getBackground());
        JLabel label = new JLabel("Commander", JLabel.CENTER);
        label.setFont(_bigFont);

//      setBorder( new CellBorder( "Cell Commander" , 40 ) ) ;
        add(label, "North");

        add(_displayPanel = new DisplayPanel(), "Center");

        add(createSouth(), "South");

        _commandField.addActionListener(

                new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        String text = _commandField.getText();
                        _commandField.setText("");
                        try {
                            String destination = _destination.getText();
                            if (destination.equals("")) {
                                _connection.sendObject(text, new OurListener(), 4);
                            } else {
                                _logger.debug("Sending to " + destination);
                                _connection.sendObject(destination, text, new OurListener(), 4);
                            }
                        } catch (Exception ee) {
                            _logger.error("Error in sending : " + ee);
                        }
                    }
                }
        );
    }
    //  public Insets getInsets(){ return new Insets(40,40,40,40) ; }
    private JPanel createSouth(){
        GridBagLayout     lo = new GridBagLayout() ;
        GridBagConstraints c = new GridBagConstraints()  ;
        JPanel panel = new JPanel( lo ) ;

        c.gridheight = 1 ;
        c.insets     = new Insets(4,4,4,4) ;

        c.gridwidth  = 1 ; c.gridx = 0 ; c.gridy = 0 ;
        panel.add( new JLabel("DUMMY") , c ) ;
        c.gridwidth  = 1 ; c.gridx = 1 ; c.gridy = 0 ;
        panel.add( new JLabel("Destination") , c ) ;

        c.weightx = 1.0 ;
        c.weighty = 0.0 ;
        c.gridwidth  = 1 ; c.gridx = 2 ; c.gridy = 0 ;
        c.fill = GridBagConstraints.HORIZONTAL ;
        panel.add( _destination , c ) ;
        c.gridwidth  = 3 ; c.gridx = 0 ; c.gridy = 1 ;
        panel.add( _commandField , c ) ;

        JPanel jp = new JPanel( new BorderLayout() ) ;
        jp.add( panel , "Center" ) ;
        return jp ;
    }
    private class TitleList extends JPanel {

        private JList        _list   = new JList() ;
        private JScrollPane  _scroll = null ;
        private TitledBorder _border = null ;

        private TitleList(String title ){

            BorderLayout l = new BorderLayout() ;
            l.setVgap(10) ;
            l.setHgap(10);
            setLayout(l) ;

            _list.setPrototypeCellValue("it-dcache0-0Domain");

            _scroll = new JScrollPane( _list ) ;
            add( _scroll , "Center" ) ;

            _border = BorderFactory.createTitledBorder(title) ;
            setBorder( _border ) ;
         /*
         setBorder(

            BorderFactory.createCompoundBorder(
                  _border ,
                  BorderFactory.createEmptyBorder(4,4,4,4)
            )

         ) ;
         */
        }
    }
    private class ListPanel extends JPanel {
        private TitleList _domains = new TitleList("Domains") ;
        private TitleList _cells   = new TitleList("Cells");
        private ListPanel(){
            GridLayout l = new GridLayout(2,0) ;
            l.setVgap(10) ;
            l.setHgap(10);
            setLayout(l) ;
            add(_domains);
            add(_cells);
        }
    }
    private class DisplayPanel extends JPanel implements ActionListener {

        private JTextArea    _displayArea  = new JTextArea() ;
        private JScrollPane  _scrollPane   = null ;
        private JButton      _clearButton  = new JButton("Clear") ;
        private JRadioButton _switchButton = new JRadioButton("Show Cells/Domain");
        private ListPanel    _listPanel    = new ListPanel() ;
        private boolean      _switchMode   = false ;

        private DisplayPanel(){

            BorderLayout l = new BorderLayout() ;
            l.setVgap(10) ;
            l.setHgap(10);
            setLayout(l) ;
            _displayArea.setEditable(false);

            _scrollPane = new JScrollPane( _displayArea ) ;
            add( _scrollPane   , "Center" ) ;

            _clearButton.addActionListener(this);
            _switchButton.addActionListener(this);

            JPanel south = new JPanel() ;
            l = new BorderLayout() ;
            l.setVgap(10) ;
            l.setHgap(10);

            _listPanel = new ListPanel() ;

            south.setLayout(l) ;

            south.add( _switchButton , "West" ) ;
            south.add( _clearButton  , "East" ) ;

            add( south , "South" ) ;

            setBorder(

                    BorderFactory.createCompoundBorder(
                            BorderFactory.createTitledBorder("Blackboard") ,
                            BorderFactory.createEmptyBorder(8,8,8,8)
                    )

            ) ;
        }
        public void actionPerformed( ActionEvent event ){
            Object source = event.getSource() ;
            if( source == _clearButton ){
                _displayArea.setText("");
            }else if( source == _switchButton ){
                boolean en = _switchButton.isSelected() ;
                if( en && ! _switchMode ){
                    add( _listPanel , "West" ) ;
                    _switchMode = true ;
                    validate() ;
                    repaint() ;
                }else if( _switchMode && ! en ){
                    remove( _listPanel  ) ;
                    _switchMode = false ;
                    validate() ;
                    repaint() ;
                }
            }
        }
        private void append( String text ){
            _displayArea.append(text);
            SwingUtilities.invokeLater(

                    new Runnable(){
                        public void run(){
                            Rectangle rect = _displayArea.getBounds() ;
                            rect.y = rect.height - 30 ;
                            _scrollPane.getViewport().scrollRectToVisible( rect ) ;
                        }
                    }
            ) ;
        }
    }

    private void append( String text ){ _displayPanel.append( text ) ; }

    private class OurListener implements DomainConnectionListener {
        public void domainAnswerArrived( Object obj , int subid ){
//         _logger.debug( "Answer ("+subid+") : "+obj.toString() ) ;
            append("Class : "+obj.getClass().getName()+"\n");
            if( obj instanceof Object [] ){
                Object [] array = (Object [])obj ;
                for( int i = 0 , n = array.length ; i < n ; i++ ){
                    append(array[i].toString()+"\n");
                }
            }else{
                append(obj.toString()+"\n");
            }
        }
    }
    public void connectionOpened( DomainConnection connection ){
        _logger.debug("Connection opened");
    }
    public void connectionClosed( DomainConnection connection ){
        _logger.debug("Connection closed" ) ;
    }
    public void connectionOutOfBand( DomainConnection connection, Object obj ){
        _logger.debug("Connection connectionOutOfBand "+obj ) ;
    }
    public void domainAnswerArrived( Object obj , int subid ){
        _logger.debug( "Answer ("+subid+") : "+obj.toString() ) ;
    }
}
