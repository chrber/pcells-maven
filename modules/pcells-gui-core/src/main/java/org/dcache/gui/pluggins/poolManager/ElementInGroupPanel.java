 // $Id: ElementInGroupPanel.java,v 1.1 2007/11/17 10:50:12 cvs Exp $
//
package org.dcache.gui.pluggins.poolManager;
//

 import org.pcells.services.gui.CellGuiSkinHelper;

 import javax.swing.*;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;

 public class ElementInGroupPanel extends CellGuiSkinHelper.CellPanel {

    private OptionList _allElements = new OptionList( "Elements" ) ;
    private OptionList _allGroups   = new OptionList( "Groups" ) ;
    private OptionList _inGroup     = new OptionList( "InGroup" ) ;


    private Controls   _controls    = new Controls() ;

    private JList      _list = _allElements.getJList() ;
    private DefaultListModel _dataModel = new DefaultListModel() ;

    class MyCellRenderer extends DefaultListCellRenderer {

        /* This is the only method defined by ListCellRenderer.  We just
         * reconfigure the Jlabel each time we're called.
         */
        public Component getListCellRendererComponent(
            JList list,
        Object value,   // value to display
        int index,      // cell index
        boolean iss,    // is the cell selected
        boolean chf)    // the list and the cell have the focus
        {
            /* The DefaultListCellRenderer class will take care of
             * the JLabels text property, it's foreground and background
             * colors, and so on.
             */
            super.getListCellRendererComponent(list, value, index, iss, chf);

            /* We additionally set the JLabels icon property here.
             */

            ListEntry entry = (ListEntry)value ;

            if( ! entry._active ){
               setEnabled(false);
            }
            if( entry._highLight ){
               setForeground(Color.red);
            }
        return this;
        }
    }
    private class ListEntry {
       private String _name = null ;
       private boolean _active = true ;
       private boolean _highLight = false ;
       private ListEntry( String name ){
          _name = name ;
       }
       public String toString(){ return _name ; }

    }
    public class MyListSelectionListener implements ListSelectionListener {
       public void valueChanged( ListSelectionEvent event ){
          if( event.getValueIsAdjusting() )return ;
          JList list =(JList)event.getSource() ;
          Object [] values = list.getSelectedValues() ;
          for( int i = 0 ; i < values.length ; i++ ){
               System.out.println("Values : "+values[i].toString());
          }
       }
    }
    public class OurListSelectionModel extends DefaultListSelectionModel{
        public void addSelectionInterval(int index0, int index1){
           System.out.println("Add Interval "+index0+"  "+index1 );
           for( int i = index0 ; i <= index1 ; i++ ){
              if( isSettingOk(i) )super.addSelectionInterval(i,i) ;
           }
        }
        public void setSelectionInterval(int index0, int index1){
           System.out.println("Set Interval "+index0+"  "+index1 );
           super.clearSelection() ;
           for( int i = index0 ; i <= index1 ; i++ ){
              if( isSettingOk(i) )super.addSelectionInterval(i,i) ;
           }
        }
        private boolean isSettingOk( int index ){
           ListEntry entry = (ListEntry) _dataModel.getElementAt(index) ;
           System.out.println("Index ok : "+entry._active );
           return entry._active ;
        }
    }
    public class xxx implements OptionList.ListEntry {
        private String _name = null ;
        private boolean _highlighted = false  , _enabled = true ;
        public xxx(String name , boolean enabled , boolean highlighted ){
           _name = name ;
           _enabled = enabled ;
           _highlighted = highlighted ;
        }
        public String toString(){ return _name ; }
        public boolean isEnabled(){ return _enabled ; }
        public boolean isHighlighted(){ return _highlighted ; }
    }
    public ElementInGroupPanel( String name ){

         GridLayout gl = new GridLayout( 0 , 4) ;
         gl.setHgap(10) ;

         setLayout( gl ) ;

         _list.setModel( _dataModel ) ;
         _list.setCellRenderer( new MyCellRenderer() ) ;
         _list.addListSelectionListener( new MyListSelectionListener() ) ;
         _list.setSelectionModel( new OurListSelectionModel() ) ;

         _dataModel.addElement( new ListEntry("otto") ) ;
         _dataModel.addElement( new ListEntry("fritz") ) ;
         _dataModel.addElement( new ListEntry("whatever") ) ;

         ListEntry e = new ListEntry("else");
         e._active = false ;
         _dataModel.addElement( e ) ;

         e = new ListEntry("high");
         e._highLight = true ;
         _dataModel.addElement( e ) ;

         _dataModel.addElement( new ListEntry("karl") ) ;
         _dataModel.addElement( new ListEntry("emmi") ) ;


         _allGroups.add( new xxx( "hallo" , true , false ) ) ;
         _allGroups.add( new xxx( "otto" , true , true ) ) ;
         _allGroups.add( new xxx( "karl" , false , false ) ) ;
         _allGroups.add( new xxx( "watse" , false , true ) ) ;


         _allGroups.commit() ;

         add( _allElements ) ;
         add( _allGroups ) ;
         add( _controls ) ;
         add( _inGroup ) ;

    }
    private class Controls extends CellGuiSkinHelper.CellPanel implements ActionListener {

        private JTextField _textField   = new JTextField("");
        private JButton    _addButton   = new JButton( "Add" ) ;
        private JButton    _rmButton    = new JButton( "Remove" ) ;
        private JLabel     _arrow       = new JLabel( "===>>>>" ) ;
        private JCheckBox  _enableCheck = new JCheckBox( "enable" ) ;
        private JCheckBox  _highCheck   = new JCheckBox( "high" ) ;
        private Controls(){

            setLayout( new GridBagLayout() );

            setBorder( BorderFactory.createTitledBorder(
                       BorderFactory.createLineBorder( Color.blue , 1 ) ,
                       "Controls" )
                     ) ;


            GridLayout gl = new GridLayout( 10,0) ;
            gl.setHgap(10) ;

            JPanel panel = new JPanel( gl ) ;
            _arrow.setHorizontalAlignment( JLabel.CENTER);
            _arrow.setForeground(Color.red);

            _addButton.addActionListener(this);
            _rmButton.addActionListener(this);
            panel.add( _addButton ) ;
            panel.add( _enableCheck ) ;
            panel.add( _highCheck ) ;
            panel.add( _arrow);
            panel.add( _rmButton ) ;
            panel.add( _textField ) ;

            GridBagConstraints c = new GridBagConstraints()  ;
            add( panel , c ) ;
        }
        public void actionPerformed( ActionEvent event ){
           Object source = event.getSource() ;
           if( source == _addButton ){
              String text = _textField.getText() ;
              if( text.equals("") )return ;
              _allGroups.add( new xxx( text , _enableCheck.isSelected() , _highCheck.isSelected() ) ) ;

           }else if( source == _rmButton ){
              _allGroups.commit() ;
           }
        }
    }

 }
