package org.pcells.services.gui.util ;

import javax.swing.DefaultListModel;


public class DefaultPieChartModel extends DefaultListModel implements PieChartModel {
    public void addElement( PieChartModel.PieChartItem item ){
       super.addElement( item ) ;
    }
    public void fireContentsChanged(Object source){
       super.fireContentsChanged(this , 0 , 4 ) ;
    }
}
