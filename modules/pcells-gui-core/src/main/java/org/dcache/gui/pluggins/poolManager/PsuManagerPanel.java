// $Id: PsuManagerPanel.java,v 1.1 2007/11/17 10:50:12 cvs Exp $
//
package org.dcache.gui.pluggins.poolManager;
//

import org.pcells.services.connection.DomainConnection;
import org.pcells.services.gui.CellGuiSkinHelper;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

//import diskCacheV111.poolManager.PoolManagerCellInfo ;

public class      PsuManagerPanel
       extends    CellGuiSkinHelper.CellPanel 
       implements ActionListener{
                  
   private DomainConnection _connection    = null ;
   private Preferences      _preferences   = null ;
   
   private ElementInGroupPanel _elementInGroup = null ;

   public PsuManagerPanel( DomainConnection connection , Preferences preferences ){

      _connection  = connection ;
      _preferences = preferences ;

      BorderLayout l = new BorderLayout(10,10) ;
      setLayout(l) ;

      setBorder( new CellGuiSkinHelper.CellBorder("Pool Commander" , 25 ) ) ;

      _elementInGroup = new ElementInGroupPanel("Pools") ;
      
      add( _elementInGroup , "Center" ) ;
       
   }
   public void actionPerformed( ActionEvent event ){
   
   }
   
}
