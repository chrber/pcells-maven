// $Id: HistogramEvent.java,v 1.1 2008/08/04 18:38:59 cvs Exp $

package org.pcells.services.gui.util.histogram ;

import java.util.EventObject;


public class HistogramEvent extends EventObject {
   
   private int _type = 0 ;
   public static final int PARAMETERS_CHANGED = 1 ;
   public static final int STRUCTURE_CHANGED  = 2 ;
   public static final int CONTENTS_CHANGED   = 3 ;
   public HistogramEvent( Object source , int type ){
      super( source ) ;
      _type = type ;
   }
}
