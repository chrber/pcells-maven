 

package org.dcache.gui.pluggins.poolManager;

import javax.swing.*;
import java.awt.*;

public class JTest2 extends JFrame {

    public JTest2( String [] args ) throws Exception {
          
       getContentPane().setLayout( new BorderLayout() ) ;
       
       
       getContentPane().add( new ElementInGroupPanel("hallo") , "Center" ) ;
       pack();
       setSize(new Dimension(900,500));
       setVisible(true); 
       

    }
    
    public static void main( String [] args )throws Exception {
          new JTest2(args);
    }









}
