// $Id: CellGuiClassLoader.java,v 1.7 2007/02/15 08:18:12 cvs Exp $
//
package  org.pcells.util ;
//
import java.net.* ;
import java.io.* ;
import java.util.jar.* ;
import java.util.*;
import java.util.prefs.*;
import java.util.regex.* ;
import java.lang.reflect.*;

public class CellGuiClassLoader  extends URLClassLoader  implements ModuleClassLoader {

   private String _ourBaseURL   = null ;
   private Map    _validJars    = new HashMap() ;
   private Map    _validModules = new TreeMap() ;
   private Map    _urlHashMap   = new HashMap() ;
   private Preferences _ourRoot = null ;
   private boolean     _debug   = false ;
   
   private ClassLoaderPreferences  _preferences = null  ;
   
   //
   // pcells-gui-jar-name:
   // pcells-gui-jar-version:
   // pcells-gui-module-class-0: org.pcells.services.gui.Maste
   // pcells-gui-module-name-0:   Gui Master
   // pcells-gui-module-version-0:  2.5.6
   // pcells-gui-module-sequence-0:  100
   // pcells-gui-module-provider-0:  dCache.org, Patrick
   // pcells-gui-module-description-0:  Main Cell Gui
   // pcells-gui-module-instance-0:  Master1 Master2
   // pcells-gui-module-about-0:    /docs/GuiMasterAbout.html
   // pcells-gui-module-help-0:     /docs/GuiMasterHelp.html
   //
   public static class Version implements Comparable {
      private String _versionString = "0.0.0" ;
      private Version(){ _versionString = "0.0.0" ; }
      private Version(String versionString){
         if( versionString == null )return ;
         _versionString = versionString.trim() ;
      }
      public String toString(){ return _versionString ; }
      public boolean equals( Object obj ){
         return _versionString.equals(obj.toString());
      }
      public int compareTo( Object obj ){
          return _versionString.compareTo( obj.toString() ) ;
      }
   }
   public class GuiJarModuleEntry  implements Comparable {
      private String          _name = null ;
      private GuiJarEntry _jarEntry = null ;
      private String _className     = null ;
      private int    _position      = 0 ;
      private GuiJarModuleEntry( String name ){ _name = name ; } 
      private void setJarEntry( GuiJarEntry entry ){ _jarEntry = entry ; }
      private void setClassName( String className ){ _className = className ; }
      public String getClassName(){ return _className ; }
      public String getModuleName(){ return _name ; }
      private void setPosition( int position ){ _position = position ; }
      public String toString(){ 
         return _name+";Class="+_className+";Pos="+_position+";Jar="+_jarEntry.getName() ;
      }
      public int compareTo( Object other ){
         GuiJarModuleEntry e = (GuiJarModuleEntry)other ;
         return _position == e._position ? _name.compareTo( e._name ) :
                _position >  e._position ? 1 : -1 ;
      }
        
   }
   public class GuiJarEntry  implements Comparable{
      private String  _name    = null ;
      private Version _version = new Version()  ;
      private URL     _url     = null ;
      private URL     _helpIndexUrl = null ;
      private String  _helpTitle    = null ;
      private boolean _isDefault    = false ;
      private String  _jarFileName  = null ;
      private Map     _modules      = new HashMap() ;
      
      private GuiJarEntry( String name ){ _name = name ; }
      private void setJarFileName( String jarFileName ){ _jarFileName = jarFileName ; }
      private String getJarFileName(){ return _jarFileName ; }
      private void setVersion( Version version ){ _version = version  ; }
      private void setUrl( URL url ){ _url = url ; }
      private URL getUrl(){ return _url ; }
      public Version getVersion(){ return _version ; }
      public String getName(){ return _name ; }
      private void setHelpTitle( String title ){ _helpTitle = title ; }
      public String getHelpTitle(){ return _helpTitle ; }
      private void setHelpIndexUrl( URL indexUrl ){ _helpIndexUrl = indexUrl ; }
      public URL getHelpIndexUrl(){ return _helpIndexUrl ; }
      public String toString(){
         return _name + ";version="+_version+";url="+_url;
      }
      public void setIsDefault( boolean isDefault ){ _isDefault = isDefault ; }
      public boolean isDefault(){
         return _isDefault ; 
      }
      public int compareTo( Object other ){
         GuiJarEntry e = (GuiJarEntry)other ;
         return _version.compareTo( e._version );
      }
      public void addModule( String moduleName , GuiJarModuleEntry module ){
         _modules.put( moduleName , module ) ;
      }
      public Collection modules(){
         return _modules.values() ;
      }
   }
   public class GuiClassLoader extends URLClassLoader implements ModuleClassLoader {
       private ArrayList _modules = new ArrayList() ;
       private GuiClassLoader( java.util.List list ){ 
           super( new URL[0] , ClassLoader.getSystemClassLoader() ) ; 
           for( Iterator it = list.iterator() ; it.hasNext() ; ){
              GuiJarEntry jarEntry = (GuiJarEntry)it.next() ;
              addURL( jarEntry.getUrl() ) ;
              _modules.addAll( jarEntry.modules() ) ;
           }
       }
       public Iterator modules(){
          return new TreeSet( _modules ).iterator() ;
       }
   }
   public Iterator modules(){
      return new TreeSet(_validModules.values()).iterator() ;
   }
   public Class loadClass( String name ) throws ClassNotFoundException {
      return super.loadClass( name , true ) ;
   }
   public Iterator jars(){
       java.util.List list = new ArrayList() ;
       for( Iterator outer = _validJars.values().iterator() ; outer.hasNext() ; ){
       for( Iterator it = ((java.util.List)outer.next()).iterator() ; it.hasNext() ; ){
          list.add( it.next() ) ;
       }
       }
       return list.iterator() ;
   }
   public java.util.List jarTree(){
       java.util.List list = new ArrayList() ;
       for( Iterator outer = _validJars.values().iterator() ; outer.hasNext() ; ){
          list.add( outer.next() ) ;
       }
       return list ;
   }
   public String getBase( ) 
   {
      return _ourBaseURL ;
   }
   //
   //   JarFileName             JarName        Version 
   // ------------------------------------------------------
   //    pcells-gui-dcache.jar  dcache          1.1.1
   //    dcache.jar             dcache.jar      0.0.0
   //    dcache-mail.jar        dcache-main.jar 0.0.0
   //
   public ClassLoader getClassLoaderOf( String connectionName ) throws NoSuchElementException {
   
       ClassLoaderPreferences.Connection connection = _preferences.getConnection(connectionName);
       
       java.util.List entryList = new ArrayList() ;
       
       for( Iterator it = connection.jarSet() ; it.hasNext() ; ){
       
          Map.Entry entry = (Map.Entry)it.next() ;
          String    name  = entry.getKey().toString() ;
          String version  = entry.getValue().toString();
          
          java.util.List l = (java.util.List)_validJars.get(name) ;
          if( l == null )
            throw new
            NoSuchElementException("Jar not found : "+name) ;
            
          boolean found = false ;
          for( Iterator ix = l.iterator() ; ix.hasNext() ; ){
              GuiJarEntry jarEntry = (GuiJarEntry)ix.next() ;
              if( jarEntry.getVersion().toString().equals(version) ){
                 found = true ;
                 entryList.add(jarEntry);
                 break ;
              }
          }
          if( ! found )
            throw new
            NoSuchElementException("Jar not found : "+name+" version "+version);
             
       }
       //
       // switch back to default
       //
       if( entryList.size() == 0 )return this ;
       //

       return new GuiClassLoader( entryList );
   }
   public void defineClassLoader( String name , GuiJarEntry [] entries ){
      _urlHashMap.put( name ,  entries ) ;
   }
   private String toCanonicalClassName( String classString ){
      return classString.replace('.','/')+".class";
   }
   public ClassLoaderPreferences getPreferences(){
      return _preferences ;
   }
   private Pattern __pattern = Pattern.compile( "\\-[0-9]+");
   
   private String [] splitNameVersion( String name ){
      if( name.endsWith(".jar") )name = name.substring(0,name.length()-4);
      Matcher m = __pattern.matcher(name);
      int     i = 0 ;
      int start = 0 ;
      String [] list = new String[3] ;
      for( ; m.find() && ( i < list.length ) ; i++ ){
         if( i == 0 )start = m.start() ;
         list[i] = name.substring(m.start()+1,m.end()) ;
      }
      for( ; i < list.length ; i++ )list[i] = "0" ;
      if( start == 0 )return new String [] { name , "x.x.x" } ;
      StringBuffer sb = new StringBuffer()  ;
      for( i = 0 ; i < list.length - 1 ; i++ )sb.append(list[i]).append(".");
      sb.append(list[i]);
      return new String [] { name.substring(0,start) , sb.toString() } ;
   }
   public CellGuiClassLoader( String applicationName ) throws Exception {
   
      super( new URL[0] ) ;
      
      _ourRoot     = Preferences.userRoot().node( applicationName ) ;
      _preferences = new ClassLoaderPreferences( _ourRoot ) ;
      
      String cn = toCanonicalClassName(this.getClass().getName()) ;
      URL ourBaseUrl = ClassLoader.getSystemResource( cn ) ;
      if( ourBaseUrl == null )
         throw new
         Exception("Couldn't determine our URL");
       
	   
      if( _debug )System.err.println("Url  : "+ourBaseUrl);	   
      if( _debug )System.err.println("File : "+ourBaseUrl.getFile() ) ;
      if( _debug )System.err.println("Prot : "+ourBaseUrl.getProtocol() ) ;
      if( _debug )System.err.println("Path : "+ourBaseUrl.getPath() ) ;

      if( ! ourBaseUrl.getProtocol().equals("jar") ){
           System.err.println("Error : Not a jar protocol : "+ourBaseUrl);
          // throw new Exception("We have not been loaded by a jar file");
      }
      String path = ourBaseUrl.getPath() ;
      try{      
         int pos = path.indexOf("!");
         if(  pos >= 0 ){
             path = path.substring(0,pos) ;
             //System.out.println("Subpath : "+path);
          }
      }catch(Exception ee ){
          System.err.println("Syntax error in "+ourBaseUrl ) ;
          throw ee ;
      }
      File x = new File(path) ;
      //System.out.println("Dir : "+x.getParent());
   
      //
      // this should be the directory which contains all the drivers.
      //
	   
      URL s = new URL( _ourBaseURL = x.getParent() ) ;
      String jarFileName      = null ;
	   
      //
      // Find all jar files in the pcells subdirectory. Its name is
      //  i) if pcells-gui-jar-name attribute exists : call it that way
      // ii) it its name is dcache(XXX).jar : call it dcache.jar
      // iii) otherwise just call it that way.
      // Get the version : if the attribute pcells-gui-jar-version is set,
      // use this version otherwise 0.0.0.
      //
      BufferedReader br = new BufferedReader( new InputStreamReader( s.openStream() ) ) ; 
      try{
	 while( ( jarFileName = br.readLine() ) != null ){
         
             jarFileName = jarFileName.trim() ;
             
	     if( ! jarFileName.endsWith( ".jar" ) )continue ;
             
	     URL nextJarFile = new URL("jar:"+x.getParent()+"/"+jarFileName+"!/");

	     try{
		 JarURLConnection jarConnection = (JarURLConnection)nextJarFile.openConnection();
		 try{

		     Manifest   manifest     = jarConnection.getManifest();
                     Attributes manifestAttr = manifest.getMainAttributes() ;
                     
                     String jarName = (String)manifestAttr.get( new Attributes.Name("pcells-gui-jar-name") ) ;
                  //   jarName = jarName == null ? ( jarFileName.startsWith("dcache") ? "dcache" : jarFileName  ) : jarName ;
                     Version version = null ;
                     if( jarName == null ){
                         String [] xx = splitNameVersion( jarFileName ) ;
                         jarName = xx[0] ;
                         version = new Version( xx[1] ) ;
                     }else{
                        //jarName = jarName == null ?  jarFileName  : jarName ;
                        version = new Version( (String)manifestAttr.get( new Attributes.Name("pcells-gui-jar-version") ) );
                     }
                     jarName = jarName.trim() ;
                    
                     if( _debug )System.out.println("Jar internal name : "+jarName+" Version : "+version);

                     GuiJarEntry entry = new GuiJarEntry( jarName ) ;
                     entry.setUrl( nextJarFile ) ;
                     entry.setVersion( version ) ;
                     entry.setJarFileName( jarFileName ) ;
                     entry.setIsDefault(false) ;
                                          
                     java.util.List entryList = (java.util.List)_validJars.get( jarName ) ;                  
                     if( entryList == null )_validJars.put( jarName , entryList = new ArrayList() ) ;
                     entryList.add( entry ) ;
                     
                      try{
                         String helpIndex = (String)manifestAttr.get( new Attributes.Name("pcells-gui-jar-help") ) ;
                         String helpName  = null ;
                         if( helpIndex == null ){
                             helpIndex = "docs/help/help.idx" ;
                             helpName  = jarName ;
                         }else{
                             helpIndex = helpIndex.trim();
                             int p = helpIndex.indexOf(':');
                             if( p <= 0 ){
                                 helpName = jarName ;
                             }else{
                                 String tmp = helpIndex ;
                                 helpName  = tmp.substring(0,p) ;
                                 helpIndex = tmp.substring(p+1);
                             }
                         }
                         URLClassLoader loader = new URLClassLoader( new URL[] { nextJarFile } ) ;
                         Enumeration zz = loader.getResources(helpIndex) ;
                         URL helpUrl = null ;
                         for( ; zz.hasMoreElements() ; ){
                              URL u = (URL)zz.nextElement() ;
                              if( u.toString().indexOf(jarName) > -1 ){ helpUrl = u ; break ; }
                         }
                         entry.setHelpIndexUrl( helpUrl ) ;
                         entry.setHelpTitle( helpName ) ;
                         //System.err.println("helpUrl for : "+jarName+" : "+helpUrl+" : "+helpName) ;

                      }catch(Exception ee ){
                         System.err.println("Problem with help text at url ("+nextJarFile+") : "+ee ) ;
                      }
                      if( _debug )System.err.println("JarFile : "+entry ) ;
                 }finally{
		     //
		     // I don't know how to close the connection.
		     // but this should be done here.
		     //
		 }
	     }catch(Exception ei){
		 System.err.println("Problems opening : "+nextJarFile);
		 continue ;
	     }
	  }

       }finally{
	  try{ br.close() ; }catch(Exception ee ){}
       }

       //
       // determine the default :
       //  i) if the version (pcells-gui-jar-versio) was set in the jar file, take the more recent.
       // ii) for cells.jar and dcache.jar take the one with no extention.
       //iii) for all others there is no default.
       //
       for( Iterator outer = _validJars.values().iterator() ; outer.hasNext() ; ){
          java.util.List list = (java.util.List)outer.next() ;
          
          GuiJarEntry ent = (GuiJarEntry)list.get(0) ;
          String name = ent.getJarFileName() ;
          if( _debug )System.err.println("NAME : "+name+ " List len "+list.size() );
          ent = (GuiJarEntry)Collections.max( list ) ;
          ent.setIsDefault(true);
       }
       // 
       // get all the defined modules of all jar files.
       //
       String keyStartsWith = "pcells-gui-module-name-" ;
       for( Iterator outer = _validJars.values().iterator() ; outer.hasNext() ; ){
       for( Iterator it = ((java.util.List)outer.next()).iterator() ; it.hasNext() ; ){
       
           GuiJarEntry ent = (GuiJarEntry)it.next() ;
           URL         url = ent.getUrl() ;
           
           if( ! ent.isDefault() )continue ;
           
           if( _debug )System.err.println("ADDING URL : ("+ent.isDefault()+") (ent="+ent+") url : "+url ) ;           
           addURL( url ) ;
           
           try{
           
              JarURLConnection jarConnection = (JarURLConnection)url.openConnection();

              Manifest   manifest     = jarConnection.getManifest();
              Attributes manifestAttr = manifest.getMainAttributes() ;
                     	       
	      for( Iterator entries = manifestAttr.entrySet().iterator() ; entries.hasNext() ; ){

                  Map.Entry entry   = (Map.Entry)entries.next();
		  Object    keyObj  = entry.getKey() ;
                  if( keyObj == null )continue ;
                  
		  String key = entry.getKey().toString().trim() ;                
                  
		  if( key.startsWith(keyStartsWith) ){
                     try{
                        String moduleName = entry.getValue().toString().trim() ;
                        int    sequence   = Integer.parseInt(key.substring(keyStartsWith.length()));
		        String className  = (String)manifestAttr.get( new Attributes.Name("pcells-gui-module-class-"+sequence)) ;
		        if( ( className == null ) || ( className.trim().length() == 0 ) )
                           throw new
		           Exception("Class name for entry : "+entry.getValue()+" : "+sequence+" not found");
                        
                        String posString = (String)manifestAttr.get( new Attributes.Name("pcells-gui-module-position-"+sequence)) ;
                        int position = 0 ;
                        if( ( posString != null ) && ( ( posString = posString.trim()).length() > 0 ) ){
                           try{
                              position = Integer.parseInt( posString ) ;
                           }catch(Exception eeee ){
                           }
                        }
                        
                        GuiJarModuleEntry e = new GuiJarModuleEntry( moduleName ) ;
                        e.setClassName( className ) ;
                        e.setPosition( position ) ;
                        e.setJarEntry( ent ) ;
                                          
                        if( _debug )System.out.println("GuiJarModuleEntry : "+e ) ;
                        
                        _validModules.put( moduleName , e ) ;
                        ent.addModule( moduleName , e ) ;
                        
                     }catch(Exception ioe ){
		        System.err.println("Syntax Error in manifest : "+key+ " : " +ioe  )  ;
		        continue ;
                     }
                  }
              }
              
           }catch(Exception eee ){
              System.err.println("Problem preparing to load "+url+" : "+eee ) ;
              continue ;
           }
       }
       }
   }
   public static CellGuiClassLoader __classLoader = null ;
   public static void main( String [] args )throws Exception {
   
        String applicationName = "CellLogin" ;
        
        try{
            __classLoader = new CellGuiClassLoader( applicationName ) ;
        }catch(Exception ee  ){
            System.err.println("CellGuiClassLoader failed with : "+ee);
            ee.printStackTrace() ;
        }
       try{

          Class login =  __classLoader.loadClass( "org.pcells.services.gui.JMultiLogin" ) ;

          String [] x = new String[0] ;
          Constructor c = login.getConstructor( new Class [] { java.lang.String.class , x.getClass() } ) ;
          Object panel = c.newInstance( new Object [] { applicationName , args } ) ;

       }catch(Exception eee ){
          System.err.println("Can't create new JMultiLogin");
          eee.printStackTrace() ;
       }

   }

}
