// $Id: PoolMoverPanel.java,v 1.1 2007/02/15 08:20:35 cvs Exp $
//
package org.dcache.gui.pluggins.pools;
//

import diskCacheV111.poolManager.PoolManagerCellInfo;
import diskCacheV111.vehicles.IoJobInfo;
import org.dcache.gui.pluggins.JHistogramDisplay;
import org.pcells.services.connection.DomainConnection;
import org.pcells.services.connection.DomainConnectionListener;
import org.pcells.services.gui.CellGuiSkinHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.prefs.Preferences;

public class      PoolMoverPanel
       extends    CellGuiSkinHelper.CellPanel 
       implements ActionListener, DomainConnectionListener {
                  
   private DomainConnection _connection    = null ;
   private Preferences      _preferences   = null ;
   private JButton          _goButton      = new JButton("go");
   private JHistogramDisplay _histogram    = new JHistogramDisplay("Movers");
   private Class _ioJobInfoClass = null ;
   public PoolMoverPanel( DomainConnection connection , Preferences preferences ){
   
      _connection  = connection ;
      _preferences = preferences ;
      
      BorderLayout l = new BorderLayout(10,10) ;
      setLayout(l) ;
      
      setBorder( new CellGuiSkinHelper.CellBorder("Movers" , 25 ) ) ;
      _goButton.addActionListener(this);
      add(_histogram,"Center");
      add(_goButton,"South");
      
      //_ioJobInfoClass = this.getClass().getClassLoader().loadClass("diskCacheV111.vehicles.IoJobInfo");
   }
   public void actionPerformed( ActionEvent event ){
      calibrate();
   }   
   private void calibrate(){
      try{
           _connection.sendObject( "PoolManager" ,
                                   "xgetcellinfo" ,
                                   this ,
                                   100 ) ;
      }catch(Exception ee ){
         ee.printStackTrace();
      }
   }
   private Object _lock = new Object() ;
   private int _waitingFor = 0 ;
   private ArrayList _waitList = null ;
   
      public void domainAnswerArrived( Object obj , int subid ){
         if( subid == 100 ){
            if( obj == null ){
            }else if( obj instanceof Exception ){
            }else if( obj instanceof PoolManagerCellInfo ){
               PoolManagerCellInfo poolManagerCellInfo = (PoolManagerCellInfo)obj ;
               String [] poolList = (String[]) poolManagerCellInfo.getPoolCells().toArray();
               synchronized( _lock ){
                     _waitingFor = poolList.length ;
                     _waitList = new ArrayList() ;
               }
               for( int i = 0 ; i < poolList.length ; i++ ){
                  try{
                      _connection.sendObject( poolList[i] ,
                                   "mover ls -binary" ,
                                   this ,
                                   101 ) ;
                                   
                      _goButton.setEnabled(false);
                  }catch(Exception ee ){
                       ee.printStackTrace();
                  }
               }
            }else{
            }         
         }else if( subid == 101 ){
           synchronized( _lock ){
              _waitingFor -- ;
              if( obj == null ){
                 _waitList.add(new Exception("Timeout"));
              }else{
                 _waitList.add(obj);
              }
              if( _waitingFor <= 0 ){
                  int i = 0 ;
                  long start = System.currentTimeMillis();
                  ArrayList results = new ArrayList() ;
                 for( Iterator ii = _waitList.iterator() ; ii.hasNext() ; i++ ){
                      Object o = ii.next() ;
                      System.err.println("Arrived : "+o.getClass()+ " : loader : "+o.getClass().getClassLoader());             
                      if( ! ( o instanceof IoJobInfo [] ) )continue ;
                      IoJobInfo [] info = (IoJobInfo [])o;
                      for( int j = 0 ; j < info.length ; j++ ){
                         results.add( new Integer( (int)(start - info[j].getStartTime() ) ) )  ;
                      }
                  }
                  int [] values = new int[results.size()] ;
                  i = 0 ;
                  for( Iterator iii = results.iterator() ; iii.hasNext() ; i++ ){
                      values[i] = ((Integer)iii.next()).intValue()/1000 ;
                  }
                  _histogram.prepareHistogram( values , null , 0 , 50 ) ;
                  _goButton.setEnabled(true);
              }
           }
         }
         
      }
   
}
