// $Id: BasicHistogramModel.java,v 1.1 2008/08/04 18:38:59 cvs Exp $

package org.pcells.services.gui.util.histogram ;




public interface BasicHistogramModel  {

   public Object getParameterAt( int i ) ;
   public int getParameterCount() ;
   
   public String   getNameAt( int i ) ;
   public float [] getDataAt( int i ) ;
   public int getDataCount() ;

   public void addHistogramListener(HistogramListener l) ;
   public void removeHistogramListener(HistogramListener l) ;


}
