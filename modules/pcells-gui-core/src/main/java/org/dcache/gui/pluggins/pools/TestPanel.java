
// $Id: TestPanel.java,v 1.1 2008/08/04 19:00:25 cvs Exp $ 

package org.dcache.gui.pluggins.pools;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class TestPanel extends JPanel implements MouseListener {
   private int _count = 0 ;
   public TestPanel(){
     setLayout(new BorderLayout());
     addMouseListener(this);
   }
   public void paintComponent( Graphics g ){
      int x = 0 ;
  synchronized(this){
        x= _count ++ ;
  }
      Dimension d = getSize() ;
  g.setColor(Color.blue);
      g.drawLine( 0 , x , d.width-1 , x ) ;
  System.out.println("Printing at "+x);
   }
   public void mousePressed( MouseEvent event ){
   }
   public void mouseReleased( MouseEvent event ){
   }
   public void mouseClicked( MouseEvent event ){
      repaint();
   }
   public void mouseExited( MouseEvent event ){
   }
   public void mouseEntered( MouseEvent event ){
   }
}
