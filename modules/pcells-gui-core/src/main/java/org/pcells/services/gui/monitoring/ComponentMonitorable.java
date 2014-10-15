// $Id: ComponentMonitorable.java,v 1.1 2007/03/11 15:25:21 cvs Exp $
//
package org.pcells.services.gui.monitoring ;
//

import java.awt.event.ActionListener;
 
   public interface ComponentMonitorable {
       public interface ComponentMonitorValue {
          public String getName() ;
          public String getUnit() ;
          public float  getValue() ;
          public long   getTimestamp() ;
       }
       public final int RESULT_IDLE     =  -2 ;
       public final int RESULT_ACTIVE   =  -1 ;
       public final int RESULT_OK       =  0 ;
       public final int RESULT_WARNING  =  1 ;
       public final int RESULT_CRITICAL =  2 ;
       public final int RESULT_FATAL    =  3 ;
       public void    setEventListener( ActionListener listener ) ;
       public String  getShortDescription() ;
       public String  getActionMessage() ;
       public int     getResultCode()   ;
       public String  getResultDetails() ;
       public void    start() throws IllegalStateException ;
       public void    stop() ;
       public void    reset() ;
       public boolean isStillActive() ;
       public String  getName() ;
       public ComponentMonitorValue [] getValueSet() ;
   }
