 

package org.dcache.gui.pluggins.poolManager ;

import org.pcells.services.gui.* ;
import org.pcells.services.gui.util.* ;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.*;
import java.util.prefs.*;
import dmg.cells.applets.login.* ;
import java.lang.reflect.*;
import java.util.Random;
import org.dcache.gui.pluggins.*;

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
