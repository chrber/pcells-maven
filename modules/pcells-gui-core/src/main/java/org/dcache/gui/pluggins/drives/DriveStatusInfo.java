// $Id: DriveStatusInfo.java,v 1.1 2007/02/24 12:11:05 cvs Exp $
//
package org.dcache.gui.pluggins.drives;
//


public class DriveStatusInfo {

   private String _name = null ,
                  _mode = null ,
                  _state = null ,
                  _pvr   = null ,
                  _owner = null ,
                  _action = null ;
   public DriveStatusInfo( 
               String driveName ,
               String driveMode ,
               String driveState ,
               String pvrName ,
               String ownerName ,
               String currentAction ){
               
       _name   = driveName ;
       _mode   = driveMode ;
       _state  = driveState ;
       _pvr    = pvrName ;
       _owner  = ownerName ;
       _action = currentAction ;
   }
   public String getName(){ return _name ; }
   public String getMode(){ return _mode ; }
   public String getTapeName(){ return _state ; }
   public String getPvrName(){ return _pvr ; }
   public String getOwnerName(){ return _owner ; }
   public String getAction(){ return _action ; }
   public String toString(){
      return _name+" "+_mode+" "+_state+" "+_pvr+" "+_owner+" "+_action;
   }
   public boolean isEmpty(){ return ( _state == null ) ||  _state.equals("empty") ; }
   public boolean isAction(){ return ! _action.equals("none" ) ; }
}

