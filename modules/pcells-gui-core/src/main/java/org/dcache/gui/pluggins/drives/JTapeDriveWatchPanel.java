package org.dcache.gui.pluggins.drives;

import org.dcache.gui.pluggins.JSwitchUpdatePanel;
import org.pcells.services.connection.DomainConnection;
import org.pcells.services.connection.DomainConnectionListener;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;

public class  JTapeDriveWatchPanel 
       extends JSwitchUpdatePanel
       implements ActionListener, 
                  DomainConnectionListener  {

	private DomainConnection	_connection   	= null;
	private Preferences		_preferences  	= null;
	private JDriveStatusTable 	_table		= null;
	private JTapeDriveDisplay _display	= null;

	public JTapeDriveWatchPanel(DomainConnection connection, Preferences preferences) {
		_connection  = connection;
		_preferences = preferences;

		_display = new JTapeDriveDisplay("Tape Drives");
		_table = new JDriveStatusTable();
		
		addCard(_display);
		addCard(new JScrollPane(_table));
		
		addActionListener(this);
		
		setMinimumUpdateTime(1);
		
	}
	
	public void actionPerformed(ActionEvent e) {
		if ("update".equalsIgnoreCase(e.getActionCommand())) {
		      	
				try{
		          _connection.sendObject( "pvl" ,
		                                  "ls drive" , 
		                                  this ,
		                                  1000 );
//		          setEnabled(false);
		       }catch(Exception ee){
//		          setEnabled(true);
		          ee.printStackTrace() ;
		       }
			
		}
	}

        private DriveStatusInfo [] scanDriveInfoString( String driveInfoString ){
        
            StringTokenizer st = new StringTokenizer( driveInfoString , "\n" ) ;
            ArrayList list = new ArrayList() ;
            
            while( st.hasMoreTokens() ){
               String line = st.nextToken().trim() ;
               if( line.length() == 0 )continue ;
               try{
                   StringTokenizer lt = new StringTokenizer( line ) ;
                   
                   String driveName        = lt.nextToken() ;
                   String operationalState = null ;
                   int pos = 0 ;
                   
                   //
                   //  workaround for formating bug.
                   //
                   String [] opModes = { "enabled" , "disabled" } ;
                   for( int i = 0 , n = opModes.length ; i < n ; i++ ){
                       if( ( pos = driveName.indexOf(opModes[i]) ) > -1 ){
                           driveName = driveName.substring(0,pos) ;
                           operationalState = opModes[i] ;
                       }
                   }
                   //
                   // end of workaround
                   //
                   if( operationalState == null )operationalState = lt.nextToken() ;

                   String driveState = lt.nextToken() ;
                   String pvrName    = lt.nextToken() ;
                   String owner      = lt.nextToken() ;
                   String action     = lt.nextToken() ;
                    
                   list.add( new DriveStatusInfo(driveName,operationalState,driveState,pvrName,owner,action) ) ;

               }catch(Exception ee){
                   System.err.println("Syntax error in : "+line);
                   continue ;
               }
            }
            return (DriveStatusInfo [] )list.toArray( new DriveStatusInfo[list.size()] ) ;
        }
	public void domainAnswerArrived(Object obj, int id) {
        
		System.out.println("the original string: "+obj.toString());
		if( obj instanceof Exception ){
                   System.err.println("Non String message arrived : "+obj.toString());
                   return ;

                }
		ArrayList drives = new ArrayList();

                DriveStatusInfo [] infos = scanDriveInfoString( obj.toString() ) ;
                for( int i = 0 , n = infos.length ; i < n ; i++ ){
                   DriveStatusInfo info = (DriveStatusInfo)infos[i] ;
                   String [] row = new String[6] ;
                   row[0] = info.getName() ;
                   row[1] = info.getMode() ;
                   row[2] = info.getTapeName() ;
                   row[3] = info.getPvrName() ;
                   row[4] = info.getOwnerName() ;
                   row[5] = info.getAction() ;
                   drives.add( row ) ;
                }
		_table.updateTable(drives);
                _display.setDriveList(infos);
		

	}

	public void setEnabled(boolean enabled) {
//		_testbutton.setEnabled(enabled);
	}
	
	public static void main(String[] args) {

		String s = "stk-0       enabled     empty       stk     -       none\n"
				+ "stk-1       enabled     empty       stk     -       none\n"
				+ "st-drive-0  enabled     empty       stacker -       none\n"
				+ "st-drive-1  enabled     empty       stacker -       none\n"
				+ "easy-drive-0enabled     empty       easy    -       none\n"
				+ "easy-drive-1enabled     empty       easy    -       none  ";

		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JTapeDriveWatchPanel panel = new JTapeDriveWatchPanel(null,null);
		f.add(panel);
		f.pack();
		f.setVisible(true);

		panel.domainAnswerArrived(s, 0);

	}
}
