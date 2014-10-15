package org.pcells.services.gui.util ;

import javax.swing.*;
import java.awt.*;


public interface PieChartModel extends ListModel {

    public interface PieChartItem {
        public Color getColor() ;
        public long  getLongValue() ;
    }
    
}
