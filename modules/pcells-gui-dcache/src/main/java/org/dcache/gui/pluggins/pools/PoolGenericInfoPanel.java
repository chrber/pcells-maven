package org.dcache.gui.pluggins.pools ;

import org.pcells.services.gui.* ;
import org.pcells.services.gui.util.* ;

import diskCacheV111.pools.* ;

import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

public class PoolGenericInfoPanel extends JPanel  {

   private Object [][] _map = {
       { "Base directory" , "Pool Base : "     , null } ,
       { "Version"        , "Code Version : "  , null } ,
       { "Revision"       , "Revision : "      , null } ,
//       { "Recovery"       , "Recovery Mode : " , null } ,
       { "Pool Mode"      , "Pool Mode : "     , null } ,
       { "Detail"         , "Disable Details : " , null } ,
       { "ReplicationMgr"    , "Replication Manager : " , null } ,

       { "StickyFiles"       , "Allow Sticky Files : " , null } ,
       { "ModifyFiles"       , "Allow Modifing Files : " , null } ,
       { "Gap"               , "Gap : " , null } ,
       { "Report remove"     , "Report File Removal : " , null } ,
       { "Clean prec. files" , "Allow cleaning precious files : " , null } ,
       { "Hsm Load Suppr."   , "Suppress HSM load : " , null } ,
       { "Ping Heartbeat"    , "Ping Heartbeat : " , null } ,
       { "LargeFileStore"    , "Large Filestore Mode : " , null } ,
       { "P2P File Mode"     , "Pool 2 Pool File Mode : " , null }
   } ;
   
   private GridPanel _gridPanel = null ;
   public PoolGenericInfoPanel(){
   
       setLayout( new BorderLayout(10,10) ) ;
      
       setBorder( BorderFactory.createTitledBorder( BorderFactory.createLineBorder( Color.blue , 1 ) , "Generic Infos" ) ) ;

       _gridPanel = new GridPanel() ;
       
       add( _gridPanel , "North");
    }
    private class GridPanel extends JPanel {

     //public Insets getInsets(){ return  new Insets(20,20,20,20) ; } 
     private GridPanel(){
     
       GridBagLayout     lo = new GridBagLayout() ;
       GridBagConstraints c = new GridBagConstraints()  ;
       setLayout( lo ) ;
       
       JComponent gridPanel = this ;
       
       CellGuiSkinHelper.setComponentProperties( gridPanel ) ;
       Color back = gridPanel.getBackground() ;
       if( back != null )gridPanel.setBackground(back.brighter());
       
       Insets insets1 = new Insets(1,1,0,0);
       Insets insets2 = new Insets(1,0,0,1);

              
       c.insets     = (Insets)insets1.clone() ;
       c.fill       = GridBagConstraints.HORIZONTAL;
       c.weightx    = 0.0 ;
       c.weighty    = 1.0 ;
       c.gridheight = 1 ;
       c.ipadx = 2 ;
       c.ipady = 2 ;

        int i = 0 ;
        for(  ; i < 6 ; i++ ){
        
           JLabel label = new JLabel( _map[i][1].toString() , JLabel.RIGHT ) ;
           label.setFont( label.getFont().deriveFont( Font.BOLD ) ) ;
           CellGuiSkinHelper.setComponentProperties( label ) ;           
           label.setOpaque(true);
           c.insets     = (Insets)insets1.clone() ;
           c.gridwidth  = 1 ; c.gridx = 0 ; c.gridy = i ; c.weightx    = 0.0 ;
           gridPanel.add( label , c ) ;
           
           label = new JLabel( "Undefined" , JLabel.LEFT ) ;
           label.setFont( label.getFont().deriveFont( Font.PLAIN ) ) ;
           CellGuiSkinHelper.setComponentProperties( label ) ;           
           label.setOpaque(true);
           c.insets     = (Insets)insets2.clone() ;
           c.gridwidth  = 3 ; c.gridx = 1 ; c.gridy = i ; c.weightx    = 1.0 ;
           gridPanel.add( label , c ) ;
           _map[i][2] = label ;
        
        }
        for( ; i < _map.length ; i++ ){
        
           JLabel label = new JLabel( _map[i][1].toString() , JLabel.RIGHT ) ;
           label.setFont( label.getFont().deriveFont( Font.BOLD ) ) ;
           CellGuiSkinHelper.setComponentProperties( label ) ;           
           label.setOpaque(true);
           c.insets     = (Insets)insets1.clone() ;
           c.gridwidth  = 1 ; c.gridx = ( i - 5 ) % 2  * 2 ; c.gridy = ( i - 5 ) / 2 + 5 ; c.weightx    = 0.0 ;
           gridPanel.add( label , c ) ;
           
           label = new JLabel( "Undefined" , JLabel.LEFT ) ;
           label.setFont( label.getFont().deriveFont( Font.PLAIN ) ) ;
           CellGuiSkinHelper.setComponentProperties( label ) ;           
           label.setOpaque(true);
           c.insets     = (Insets)insets2.clone() ;
           c.gridwidth  = 1 ; c.gridx = ( i - 5 ) % 2 * 2 + 1 ; c.gridy = ( i - 5 ) / 2 + 5 ; c.weightx    = 1.0 ;
           gridPanel.add( label , c ) ;
           _map[i][2] = label ;
        
        }

      }

    }
    private void refill( PoolGenericInfo info ){
    
        if( info == null ){
           for( int i = 0 ; i < _map.length ; i++ ){
               JLabel label = (JLabel)_map[i][2] ;
               label.setText( "Undefined" ) ;
           }
           return ;
        }
        Map infoMap = info.getMap() ;
        for( int i = 0 ; i < _map.length ; i++ ){
            String value =  (String)infoMap.get(_map[i][0]) ;
            JLabel label = (JLabel)_map[i][2] ;
            label.setText( value == null ? "Undefined" : value ) ;
        }
       
    }
    public void setGenericInfo( PoolGenericInfo info ){
       refill( info ) ;
//       _gridPanel.repaint() ;
    }
}
