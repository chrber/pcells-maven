// $Id: PnfsManagerPanel.java,v 1.1 2008/11/09 08:23:58 cvs Exp $
//
package org.dcache.gui.pluggins.pnfs;
//

import javax.swing.*;
import java.awt.*;


public class PnfsManagerInfoDisplay extends JPanel {

      private JLabel _l_avarageQueueSize = new JLabel("Avarage Queue Size") ;
      private JLabel _l_numberOfQueues   = new JLabel("Number of Queues") ;
      private JLabel _l_numberOfThreadGropus   = new JLabel("Number of Thread Groups") ;
      private JLabel _l_avarageThreadGroupSize = new JLabel("Avarage Thread Group Size") ;
      private JLabel _l_numberOfCacheLocations   = new JLabel("Number of Cache Location Threads") ;
      private JLabel _l_avarageCacheLocationSize = new JLabel("Avarage Cache Location Thread Size") ;
      private JLabel _l_timeElapsed      = new JLabel("Time Elapsed");
      private JLabel _l_totalRequests    = new JLabel("Total Requests");
      private JLabel _l_badRequests      = new JLabel("Failed Requests");
      private JLabel _l_requestRate      = new JLabel("Total Request Rate[sec]");
      private JLabel _avarageQueueSize = new JLabel("0",JLabel.CENTER) ;
      private JLabel _timeElapsed      = new JLabel("0",JLabel.CENTER);
      private JLabel _numberOfQueues   = new JLabel("0",JLabel.CENTER);
      private JLabel _totalRequests    = new JLabel("0",JLabel.CENTER);
      private JLabel _badRequests      = new JLabel("0",JLabel.CENTER);
      private JLabel _requestRate      = new JLabel("0.0",JLabel.CENTER);
      private JLabel _numberOfThreadGroups   = new JLabel("0",JLabel.CENTER) ;
      private JLabel _averageThreadGroupSize = new JLabel("0.0",JLabel.CENTER) ;
      private JLabel _numberOfCacheLocations   = new JLabel("0",JLabel.CENTER) ;
      private JLabel _averageCacheLocationSize = new JLabel("0.0",JLabel.CENTER) ;

      private PnfsManagerInfo _previousInfo = null ;

      public PnfsManagerInfoDisplay(){

          setLayout( new GridLayout(0,2,10,10) );

          setBorder( BorderFactory.createTitledBorder(
                 BorderFactory.createLineBorder( Color.blue , 1 ) ,
                "Pnfs Manager Counter" ) ) ;

          add( _l_timeElapsed ) ;     add( _timeElapsed ) ;
          add( _l_totalRequests ) ;   add( _totalRequests ) ;
          add( _l_badRequests ) ;     add( _badRequests ) ;
          add( _l_requestRate ) ;     add( _requestRate ) ;
          add( _l_numberOfQueues ) ;  add( _numberOfQueues ) ;
          add( _l_avarageQueueSize ) ;add( _avarageQueueSize ) ;
          add( _l_numberOfThreadGropus ) ;   add( _numberOfThreadGroups ) ;
          add( _l_avarageThreadGroupSize ) ; add( _averageThreadGroupSize ) ;
          add( _l_numberOfCacheLocations ) ; add( _numberOfCacheLocations ) ;
          add( _l_avarageCacheLocationSize ) ; add( _averageCacheLocationSize ) ;
      }
      public void setPnfsManagerInfo( PnfsManagerInfo info ){
         if( _previousInfo == null ){
             _previousInfo = info ;
             return ;
         }
         long elapsed     = info._timestamp   - _previousInfo._timestamp ;
         long requestsOk  = info._requestsOk  - _previousInfo._requestsOk ;
         long requestsBad = info._requestsBad - _previousInfo._requestsBad ;
         _previousInfo = info ;
         _numberOfQueues.setText( ""+info._numberOfQueues);
         _avarageQueueSize.setText( ""+(float)( (double)info._numberOfRequests / (double)info._numberOfQueues ) ) ;
         _timeElapsed.setText( ""+((float)elapsed/1000.0)+" sec");
         _totalRequests.setText(""+requestsOk ) ;
         _badRequests.setText(""+requestsBad ) ;
         _requestRate.setText(""+(float)((double)( requestsOk + requestsBad )/(double)elapsed*1000.0));

         _numberOfThreadGroups.setText( ""+info._numberOfThreadGroups ) ;
         if( info._numberOfThreadGroups > 0 )
           _averageThreadGroupSize.setText( ""+(float)( (double)info._numberOfRequestsInThreadGroups / (double)info._numberOfThreadGroups ) ) ;

         _numberOfCacheLocations.setText( ""+info._numberOfCacheLocations ) ;
         if( info._numberOfCacheLocations  > 0 )
          _averageCacheLocationSize.setText( ""+(float)( (double)info._numberOfRequestsInCacheLocations / (double)info._numberOfCacheLocations ) ) ;
      }

}


