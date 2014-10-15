// 
// $Id: JCellPanel.java,v 1.1 2006/09/02 08:31:56 cvs Exp $
//
package org.pcells.services.gui ;

import javax.swing.*;
import java.awt.*;

public class JCellPanel extends JPanel {

   public JCellPanel(){}
   public JCellPanel( LayoutManager  layout ){
      super(layout);
   }

   public void paintComponent( Graphics gin ){
      CellGuiSkinHelper.paintComponentBackground(gin,this);
      super.paintComponent(gin);
      
   }

}
