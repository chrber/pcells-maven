 

package org.pcells.services.gui ;

import org.dcache.gui.pluggins.JHistogramDisplay;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class JTest2 extends JFrame {
    public class Actions implements ActionListener , ItemListener {
          public void actionPerformed( ActionEvent event ){
            // System.out.println("Action performed : "+event);
             makeNewPanel();
             JDialog dialog = _optionPane.createDialog( JTest2.this , "Hallo" ) ;
             dialog.show() ;
             Object o = _optionPane.getValue() ;
             System.out.println("Value class="+o.getClass().getName()+" value="+o.toString());
          }
          public void itemStateChanged( ItemEvent event ){
             System.out.println("Item State changed "+event.getStateChange()+" -> "+event.getItem()+" : "+event);       
          }
    
    }
    private JOptionPane _optionPane = null ;
    private JPanel      _optionPanel = null ;
    private String [] _labels = {
        "Id" , "345345" ,
        "Name" , "hallo@waste" ,
        "Pool" , "it-dcache-0"
    } ;
    private class XLabel extends JLabel {
        public XLabel( String text , int pos ){
           super( text , pos ) ;
           setBackground(Color.white) ;
           setOpaque(true);
        }
        public Insets getInsets(){ System.out.println("INsets"); return new Insets(10,10,10,10) ; }
        
    }
    private void makeNewPanel(){
       Color color = Color.white ; /*new Color(220,220,220);*/
       _optionPanel.removeAll() ;
        for( int i = 0 ; i < _labels.length ; i+= 2 ){
           JLabel label = new XLabel(_labels[i],SwingConstants.RIGHT ) ;
           _optionPanel.add( label ) ;
           label = new XLabel(_labels[i+1],SwingConstants.LEFT );
           _optionPanel.add( label ) ;
       }
       _optionPanel.doLayout() ;
   
    }
        
        public PieChartPanel.Section [] Xsections = {
            new PieChartPanel.Section( "Precious" , 1000L , Color.red ) ,
            new PieChartPanel.Section( "Cached"  , 500L , Color.green ) ,
            new PieChartPanel.Section( "Not Used"  , 500L , Color.yellow ) ,
            new PieChartPanel.Section( "Pinned" , 2000L , Color.orange ) ,
            new PieChartPanel.Section( "Otto" , 200L , Color.blue ) 
        };
    public JTest2( String [] args ) throws Exception {
          
       getContentPane().setLayout( new BorderLayout() ) ;
       JHistogramDisplay display = new JHistogramDisplay() ;
       int [] data = { 13,26,21,29,17,16,18,23,24,25,28,13,14,18,13,14,15,6,8,13,4,5,12,3,9,5,14,6,4} ;
       
       display.prepareHistogram( data , null , 0 , 10 ) ;
//       getContentPane().add( display , "Center" ) ;
       
       PieChartPanel p = new PieChartPanel();
       p.drawSection( Xsections ) ;
       
       getContentPane().add( p , "Center" ) ;
       JButton button = new JButton("Click") ;
       button.addActionListener(new Actions());
       
       getContentPane().add( button , "South" ) ;
       pack();
       setSize(new Dimension(900,500));
       setVisible(true);  

       JHistogramDisplay display2 = new JHistogramDisplay() ;
       display2.prepareHistogram( data , null , 0 , 10 ) ;
       _optionPanel = new JPanel( new GridLayout(0,2,5,5) ) ;
       
       _optionPane = new JOptionPane( _optionPanel , JOptionPane.QUESTION_MESSAGE , JOptionPane.OK_CANCEL_OPTION ) ;
       

    }
    
    public static void main( String [] args )throws Exception {
          new JTest2(args);
    }









}
