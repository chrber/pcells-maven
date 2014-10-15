// $Id: HistogramListener.java,v 1.1 2008/08/04 18:38:59 cvs Exp $

package org.pcells.services.gui.util.histogram ;

import java.util.EventListener;


public interface HistogramListener extends EventListener {


    public void histogramContentsChanged( HistogramEvent event ) ;
    public void histogramStructureChanged( HistogramEvent event ) ;
    public void histogramParametersChanged( HistogramEvent event ) ;


}
