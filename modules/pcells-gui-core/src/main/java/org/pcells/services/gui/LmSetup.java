// $Id: LmSetup.java,v 1.2 2004/06/21 22:30:27 cvs Exp $
//
package org.pcells.services.gui ;
//

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.BevelBorder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;



/**
 */
public class LmSetup extends JFrame {

    private JLabel      _fileLabel = new JLabel(" Current File : ") ;
    private File        _file      = null ;
    private JMenuBar    _bar       = new JMenuBar() ;
    private MovingPigs  _draw      = null ;
    private JTextField  _text      = null ;
    private Container   _pane      = null ;
    private boolean     _textActive = false ;
    public LmSetup(String title ) {
        super( title ) ;
        
        _draw  = new MovingPigs() ;
        _draw.setBorder( new BevelBorder(BevelBorder.LOWERED));

        _text = new JTextField() ;
        _text.setOpaque(true) ;
        _text.setBackground( new Color( 2 , 88 , 130 ) ) ;
        _text.setForeground( new Color( 255,255,255) ) ;
        _text.setBorder( new BevelBorder(BevelBorder.LOWERED) ) ;
        _text.setFont( new Font( "Courier" , Font.ITALIC , 24 ) ) ;
        _text.addActionListener( 
           new ActionListener(){
              public void actionPerformed( ActionEvent event ){
                  String string = _text.getText() ;   
                  _draw.command(string) ;
                  _text.setText("");
              }
           }
        ) ;

        WindowListener l = new WindowAdapter() {
            public void windowClosing(WindowEvent e) { _draw.shutdown() ; }
        };
        addWindowListener(l);

        
        _pane = getContentPane() ;
        BorderLayout bl = new BorderLayout() ;
        bl.setVgap(10) ;
        bl.setHgap(10) ;
        _pane.setLayout( bl ) ;
        
        _pane.add("Center", _draw );
        
        _fileLabel.setBorder( new BevelBorder(BevelBorder.LOWERED) ) ;
        _pane.add("North" , _fileLabel ) ;
//        _pane.add("South" , _text ) ;
        
        _bar.add( new FileActionListener() ) ;
        
        _bar.add( _draw.getEditMenu() ) ;
        
        JMenu menu = new JMenu("Help") ;
        
        menu.add( new HelpListener() ) ;
        menu.add( new CommandMenu() ) ;
        
        _bar.add( menu ) ;
        
         
        setJMenuBar( _bar ) ;
        
    }
    private LmSetupHelp _helpMenu = null ;
    WindowListener l = new WindowAdapter() {
            public void windowClosing(WindowEvent e) {System.exit(0);}
    };
    private class LMFileFilter extends javax.swing.filechooser.FileFilter {
       public String getDescription(){ return "LocationManager (*.lm)" ; }
       public boolean accept( File file ){
         return file.isDirectory() || file.getName().endsWith(".lm") ;
       }
    }
    private class CommandMenu extends JMenuItem implements ActionListener {
        private CommandMenu(){
           super("Commander") ;
           addActionListener(this) ;
           setAccelerator( 
               KeyStroke.getKeyStroke(KeyEvent.VK_C,InputEvent.CTRL_MASK,false) ) ;
        }
        public void actionPerformed( ActionEvent event ){
          if( _textActive ){
             _pane.remove( _text ) ;
             _textActive = false ;
          }else{
             _textActive = true ;
             _pane.add("South" , _text ) ;
          }
          _pane.doLayout() ;
        }
    }
    private class HelpListener extends JMenuItem implements ActionListener {
        private HelpListener(){ 
           super( "Help") ; 
           addActionListener(this);
        }
        public void actionPerformed( ActionEvent event ){
           if( _helpMenu == null ){
              _helpMenu = new LmSetupHelp("LocationManager Help");
              _helpMenu.pack();
              Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
              _helpMenu.setLocation(200,200);
              _helpMenu.setSize(600,400);
              _helpMenu.addWindowListener(
                 new WindowAdapter(){
                   public void windowClosing(WindowEvent e) {
                      _helpMenu.setVisible(false) ;
                   }
                 }
              );
            }
            _helpMenu.setVisible(true);
        }
    }
    private class FileActionListener extends JMenu implements ActionListener {
        private JMenuItem  _new    = new JMenuItem( "New..." ) ;
        private JMenuItem  _open   = new JMenuItem( "Open ..." ) ;
        private JMenuItem  _save   = new JMenuItem( "Save" ) ;
        private JMenuItem  _saveAs = new JMenuItem( "Save As ..." ) ;
        private JMenuItem  _revert = new JMenuItem( "Revert to saved" ) ;
        private JMenuItem  _exit   = new JMenuItem( "Exit" ) ;
        private JFileChooser _chooser = new JFileChooser() ;
        private File         _directory = null ;
        private FileActionListener(){
           super("File");
           
           _new.setAccelerator( 
               KeyStroke.getKeyStroke(KeyEvent.VK_N,InputEvent.CTRL_MASK,false) ) ;
           _open.setAccelerator( 
               KeyStroke.getKeyStroke(KeyEvent.VK_O,InputEvent.CTRL_MASK,false) ) ;
           _save.setAccelerator( 
               KeyStroke.getKeyStroke(KeyEvent.VK_S,InputEvent.CTRL_MASK,false) ) ;
           _saveAs.setAccelerator( 
               KeyStroke.getKeyStroke(KeyEvent.VK_W,InputEvent.CTRL_MASK,false) ) ;
           _revert.setAccelerator( 
               KeyStroke.getKeyStroke(KeyEvent.VK_R,InputEvent.CTRL_MASK,false) ) ;
           add( _new ) ;
           addSeparator() ;
           add( _open ) ;
           add( _save ) ;
           add( _saveAs ) ;
           add( _revert ) ;
           addSeparator() ;
           add( _exit ) ;
           _new.addActionListener(this) ;
           _open.addActionListener(this);
           _save.addActionListener(this);
           _saveAs.addActionListener(this);
           _revert.addActionListener(this);
           _exit.addActionListener(this);
           _save.setEnabled(false);
           _revert.setEnabled(false);
           _chooser.setFileFilter( new LMFileFilter() ) ;
           _chooser.setCurrentDirectory(_directory) ;
           
        }
        public void actionPerformed( ActionEvent event ){
           if( event.getSource() == _new ){
              newAction(event) ;
           }else if( event.getSource() == _open ){
              openAction(event) ;
           }else if( event.getSource() == _save ){
              saveAction(event) ;
           }else if( event.getSource() == _saveAs ){
              saveActionAs(event) ;
           }else if( event.getSource() == _revert ){
              revertAction(event) ;
           }else if( event.getSource() == _exit ){
              _draw.shutdown() ;
           }
        }
        private void setProcessing(){
           _fileLabel.setForeground( Color.red ) ;
           _fileLabel.setText( " Processing File : "+_file.getName() ) ;
        }
        private void setOk(){
           _fileLabel.setForeground( Color.blue ) ;
           _fileLabel.setText( " Current File : "+
                  (_file==null?"<none>":_file.getName()) ) ;
           _save.setEnabled(true);
           _revert.setEnabled(true);
        }
        private void setProblem(String problem){
           _fileLabel.setForeground( Color.red ) ;
           _fileLabel.setText( " "+problem+" : "+
                  _file==null?"":_file.getName() ) ;
        }
        private void newAction( ActionEvent event ){
           _draw.clear() ;
           _file = null ;
           setOk() ;
           _save.setEnabled(false);
           _revert.setEnabled(false);
        }
        private void saveActionAs( ActionEvent event ){
           if( ( _file = getFile() ) == null )return ;
           saveAction(event);
           setOk() ;
        }
        private void saveAction( ActionEvent event ){
           if( _file == null )return ;
           try{
               PrintWriter pw = new PrintWriter( new FileWriter(_file));
               try{
                  _draw.writeSetup( pw ) ;
               }finally{
                   try{ pw.close() ; }catch(Exception eee){}
               }
           }catch(Exception ee){
           
           }
        }
        private void openAction( ActionEvent event ){
           if( ( _file = getFile() ) == null )return ;
           setProcessing() ;
           _bar.setEnabled(false) ;
           new Thread(
              new Runnable(){
                public void run(){
                    if( runInterpreter( _file ) != 0 ){
                        setProblem("Illegal File Format");
                    }else{
                        setOk() ;
                    }
                    _bar.setEnabled(true) ;
                }
              }
           ).start() ;
           
        }
        private void revertAction( ActionEvent event ){
           if( _file == null )return ;
           setProcessing() ;
           _bar.setEnabled(false) ;
           new Thread(
              new Runnable(){
                public void run(){
                    if( runInterpreter( _file ) != 0 ){
                        setProblem("Illegal File Format");
                    }else{
                        setOk() ;
                    }
                    _bar.setEnabled(true) ;
                }
              }
           ).start() ;
           
        }
        private int runInterpreter( File file ){
           try{
               BufferedReader br = new BufferedReader(
                                         new FileReader( file ) ) ;
               _draw.clear() ;                     
               try{
                  String line = null ;
                  while( ( line = br.readLine() ) != null ){
                     _draw.command( line ) ;
                     Thread.currentThread().sleep(200) ;
                  }
               }finally{
                   try{ br.close() ; }catch(Exception ee){}
               }
           }catch(Exception ee){
              return -1 ;
           }
           return 0 ;
        }
        private File getFile(){
           _chooser.setCurrentDirectory( _directory ) ;
           int result = _chooser.showOpenDialog(LmSetup.this) ;
           if( result == 0 ){
           
              File file  = _chooser.getSelectedFile() ;
              _directory = _chooser.getCurrentDirectory() ;
              return file ;
           }
           return null ;
        }
        
    }
    
    public static void main(String argv[]) {
        LmSetup f = new LmSetup("LocationManager Setup Tool");

        f.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int w = 200;
        int h = 200;
        f.setLocation(100,100);
        f.setSize(600,400);
        f.setVisible(true);
    }
}
