 

package org.pcells.services.gui.util.histogram ;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class JTest3 extends JFrame {

    private int _mode = 0 ;
    private int _counter = 0 ;
    private int _splitIt = 0 ;
    public class Actions implements ActionListener {
          
          public void actionPerformed( ActionEvent event ){
	     Object source = event.getSource() ;
	     if( source == _details ){
		_mode += 1 ;
		if( _counter ++ == 0 )_model.fire() ;
		else _model.fireParameters();
             }else if( source == _split ){
	        _splitIt ++ ;
		_model.fireParameters() ;
	     }
	  }
    
    }
    private float [][] _values = {
    
        new float[]{ (float)4., (float)1. ,(float)1. , (float)1. , (float)1. } ,
        new float[]{ (float)8., (float)2. ,(float)1. , (float)1. , (float)4. } ,
        new float[]{ (float)0., (float)0. ,(float)0. , (float)0. , (float)0. } ,
        new float[]{ (float)10., (float)1.6 ,(float)1. , (float)1. , (float)2. } ,
    } ;
    private class HistogramModel extends AbstractHistogramModel {
       public  Object getParameterAt( int i ){
          switch( i ){
	     case 0 :  // mode
	        return _splitIt ;
	     case 1 :  // index-0
	        return ( _mode & 1 ) != 0 ? 0 : 1 ;
	     case 2 :  // index-1
	        return ( _mode & 1 ) != 0 ? 0 : _values[0].length-1 ;
	  }
	  return 0 ;
       }
       public  int getParameterCount(){
         return 2 ;
       }

       public  float [] getDataAt( int i ){
          return _values[i] ;
       }
       public  int getDataCount(){
         return _values.length ;
       }
       public void fire(){
          fireStructureChanged(this);
       }
       public void fireParameters(){
          fireParametersChanged(this);
       }
       public String getNameAt( int i ){
          return "Hallo"+i ;
       }
    }
    public HistogramModel _model = new HistogramModel() ;
    private SimpleNamedBarHistogram _hist1 = new SimpleNamedBarHistogram() ;
    private SimpleNamedBarHistogram _hist2 = new SimpleNamedBarHistogram() ;
    private Actions _actions = new Actions() ;
    private JButton _details = new JButton("Details") ;
    private JButton _split   = new JButton("Split") ;

    public JTest3( String [] args ) throws Exception {
          
       getContentPane().setLayout( new BorderLayout(4,4) ) ;
       
       _hist1.setModel( _model ) ;
       _hist2.setModel( _model ) ;
       
       JPanel center = new JPanel( new GridLayout(0,2) ) ;
       center.add( _hist1 ) ;
       center.add( _hist2 ) ;
       
       getContentPane().add( center , "Center" ) ;
       
       
       _details.addActionListener( _actions );
       _split.addActionListener( _actions );
       
       JPanel buttonPanel = new JPanel( new FlowLayout() ) ;
       buttonPanel.add( _details ) ;
       buttonPanel.add( _split ) ;
       
       getContentPane().add( buttonPanel , "South" ) ;
       
       pack();
       setSize(new Dimension(900,500));
       setVisible(true); 
       
       javax.swing.Timer timer = new javax.swing.Timer( 2000, _actions ) ;
       //timer.start() ; 

    }
    
    public static void main( String [] args )throws Exception {
          new JTest3(args);
    }









}
