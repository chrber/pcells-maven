 
package org.dcache.gui.pluggins.poolManager ;


import javax.swing.DefaultListModel;

public class LinearSpaceListModel extends DefaultListModel {

   public void setSpaces( long total ,
                          long tokenUsed , long tokenFree ,
			  long outsideUsed , long outsideFree ){
	
      //System.out.println("Spaces : "+total+" "+tokenUsed+" "+tokenFree+" "+outsideUsed+" "+outsideFree);		  
      clear();
      addElement( total ) ;
      addElement( tokenUsed ) ;
      addElement( tokenFree ) ;
      addElement( outsideUsed ) ;
      addElement( outsideFree ) ;
      
      fireContentsChanged(this,0,4);	  
   }
			  
	
}
