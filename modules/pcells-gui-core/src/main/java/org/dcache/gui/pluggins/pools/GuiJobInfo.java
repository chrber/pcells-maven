// $Id: GuiJobInfo.java,v 1.1 2007/02/19 08:59:06 cvs Exp $

package org.dcache.gui.pluggins.pools;

import diskCacheV111.vehicles.IoJobInfo;
import diskCacheV111.vehicles.JobInfo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class GuiJobInfo {
   private static SimpleDateFormat   __df   = new SimpleDateFormat( "EEE MMM d HH:mm:ss z yyyy" ) ;
   private static String __st_jobs_string   = "([0-9]+) ([AW]) ([RMHL]) \\{(.*):([0-9]+)\\} ([0-9A-Fa-f\\.]+)" ;
   private static String __st_string        = "([0-9A-Fa-f\\.]+)[ ]+[0-9]+[ ]+(.*)" ;
   private static Pattern __st_jobs_pattern = null ;
   private static Pattern __st_pattern      = null ;
   
   private long   _jobId      = 0L ;
   private String _clientName = "[unknown]" ;
   private long   _clientId   = 0L ;
   private long   _startTime  = 0L ;
   private long   _submitTime = 0L ;
   private String _status     = "?" ;
   private long   _transferTime     = 0L ;
   private long   _bytesTransferred = 0L ;
   private long   _lastTransferred  = 0L ;
   private String _pnfsId           = "" ;
   
   static {
       __st_jobs_pattern = Pattern.compile( __st_jobs_string ) ;
       __st_pattern      = Pattern.compile( __st_string ) ;
   }
   public static class Container {
       private GuiJobInfo[] _infos = null ;
   }
   public static GuiJobInfo[] newInstanceByString( String stLs , String stJobLs )
          throws IllegalArgumentException, ParseException, IllegalStateException 
   {
      HashMap map = new HashMap() ;
      StringTokenizer st = new StringTokenizer( stLs , "\n" ) ;
      while( st.hasMoreTokens() ){
         String line = st.nextToken() ;
         if( ( line != null ) && ( line.length() != 0 ) ){
         
             try{ 
                Matcher    mm     = __st_pattern.matcher( line ) ;
                System.out.println("stLs line : >"+line+"<>"+mm.pattern()+"< "+mm.matches());
               
                GuiJobInfo info   = new GuiJobInfo();
                info._pnfsId      = mm.group(1);
                info._submitTime  = __df.parse(mm.group(2)).getTime() ;
                map.put( info._pnfsId , info ) ;
             }catch(Exception ee ){
                System.err.println("create GuiJobInfo : Problem scanning : "+line+" : "+ee);
                continue ;
             }
          }
      }
      st = new StringTokenizer( stJobLs , "\n" ) ;
      while( st.hasMoreTokens() ){
         String line = st.nextToken() ;
         if( ( line != null ) && ( line.length() != 0 ) ){
             try{ 
                Matcher    mm     = __st_jobs_pattern.matcher( line ) ;
                System.out.println("stJobLs line : >"+line+"<>"+mm.pattern()+"< "+mm.matches());
                String     pnfsid = mm.group(6);
                GuiJobInfo info   = (GuiJobInfo)map.get( pnfsid ) ;
                if( info == null ){
                   System.err.println("Inconsistent state in creating GuiJobInfo");
                   continue ;
                }
                info._jobId      = Long.parseLong( mm.group(1) ) ;
                info._status     = mm.group(2) ;
                info._clientName = mm.group(4) ;
                info._clientId   = Long.parseLong( mm.group(5) ) ;
             }catch(Exception ee ){
                System.err.println("create GuiJobInfo : Problem scanning : "+line+" : "+ee);
                continue ;
             }
         }
      
      }
      return (GuiJobInfo[])(new ArrayList( map.values() ).toArray( new GuiJobInfo[ map.size() ] ) ) ;
   }
   private IoJobInfo _ioInfo   = null ;
   private JobInfo   _info     = null ;
   private boolean   _isIoInfo = false ;
   
   public GuiJobInfo(){}
   public GuiJobInfo( JobInfo info ){
      _info     = info ;
      _isIoInfo = _info instanceof IoJobInfo ;
      _ioInfo   = (IoJobInfo) ( _isIoInfo ? _info : null ) ;
      
      _jobId      = _info.getJobId() ;
      _clientName = _info.getClientName() ;
      _clientId   = _info.getClientId() ;
      _startTime  = _info.getStartTime() ;
      _submitTime = _info.getSubmitTime() ;
      _status     = _info.getStatus() ;
      _transferTime     = _isIoInfo ? _ioInfo.getTransferTime() : 0L ;
      _bytesTransferred = _isIoInfo ? _ioInfo.getBytesTransferred() : 0L ;
      _lastTransferred  = _isIoInfo ? _ioInfo.getLastTransferred() : 0L ;
   }
   public boolean isIoJobInfo(){ return _isIoInfo ; }
   public long    getJobId(){ return _jobId ; }
   public String  getClientName(){ return _clientName ; }
   public long    getClientId(){ return _clientId ; }
   public long    getStartTime(){ return _startTime ; }
   public long    getSubmitTime(){ return _submitTime ; }
   public String  getStatus(){ return _status ; }
   public long    getTransferTime(){ return _transferTime ; }
   public long    getBytesTransferred(){ return _bytesTransferred ; }
   public long    getLastTransferred(){ return _lastTransferred ; }


}
