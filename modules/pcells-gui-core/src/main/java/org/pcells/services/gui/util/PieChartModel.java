package org.pcells.services.gui.util ;

import javax.swing.ListModel;

import java.awt.Color;


public interface PieChartModel extends ListModel {

    public interface PieChartItem {
        public Color getColor() ;
        public long  getLongValue() ;
    }
    
}
