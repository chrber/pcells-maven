 

package org.pcells.services.gui.util ;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Random;

public class JTest2 extends JFrame {

    public class Actions implements ActionListener , ItemListener {
          private Random _random = new Random();
          
          public void actionPerformed( ActionEvent event ){
             long free = _random.nextInt( 10000000) ;
             long precious = _random.nextInt(10000000) ;
             long removable = _random.nextInt(10000000) ;
             long total = free + precious + removable + _random.nextInt(10000000) ;
//             _poolSpacePanel.setSpaces( total , precious,free,removable) ;
          }
          public void itemStateChanged( ItemEvent event ){
             System.out.println("Item State changed "+event.getStateChange()+" -> "+event.getItem()+" : "+event);       
          }
    
    }
    public class OurPieChartItem implements PieChartModel.PieChartItem {
       private String _title = null ;
       private Color  _color = null ;
       private long   _value = 0 ;
       public OurPieChartItem( String title , Color color , long value ){
          _title = title ;
          _color = color ;
          _value = value ;
       }
       public long getLongValue(){ return _value ; }
       public Color getColor(){ return _color ; }
       public String toString(){ return _title ; }
    }
    public DefaultPieChartModel _model = new DefaultPieChartModel() ;
//    public PoolSpacePanel       _poolSpacePanel = new PoolSpacePanel() ;
    public JTest2( String [] args ) throws Exception {
          
       getContentPane().setLayout( new BorderLayout() ) ;
       
       JPieChart p = new JPieChart();
       p.setBorder( BorderFactory.createTitledBorder( BorderFactory.createLineBorder( Color.blue , 30 ) , "Hallo" ) ) ;
       p.setModel(_model) ;
       _model.addElement( new OurPieChartItem("Precious" , Color.red , 1000L ) ) ;
       _model.addElement( new OurPieChartItem("Green" , Color.green , 2000L ) ) ;
       _model.addElement( new OurPieChartItem("Yellow" , Color.yellow , 3000L ) ) ;
       
       JPanel north = new JPanel( new BorderLayout(4,4) ) ;
       north.setBackground( Color.cyan);
       
//       _poolSpacePanel.setBackground(Color.black);
       
//       north.add( _poolSpacePanel , "West" ) ;
       getContentPane().add( north , "North" ) ;
       
       
       JButton button = new JButton("Click") ;
       button.addActionListener(new Actions());
       
       getContentPane().add( button , "South" ) ;
       pack();
       setSize(new Dimension(900,500));
       setVisible(true); 
       
       javax.swing.Timer timer = new javax.swing.Timer( 2000, new Actions() ) ;
       timer.start() ; 

    }
    
    public static void main( String [] args )throws Exception {
          new JTest2(args);
    }









}
