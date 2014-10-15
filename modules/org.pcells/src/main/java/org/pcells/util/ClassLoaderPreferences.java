// $Id: ClassLoaderPreferences.java,v 1.3 2007/02/25 22:42:03 cvs Exp $
//
package  org.pcells.util ;
//

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.prefs.Preferences;


   public class ClassLoaderPreferences {
    
       private HashMap _jarSetPerConnection = new HashMap() ;
       private Preferences _baseRoot   = null ;  
       private boolean     _isModified = false ;
      
       public class Connection {
          private String  _name = null ;
          private HashMap _jars = new HashMap() ;
          private Connection( String name ){
             _name = name ;
          }
          public void add( String name , String version ){
             _isModified = true ;
             _jars.put( name , version ) ;
          }
          public boolean isSelected( String name , String version ){
             for( Iterator it = _jars.entrySet().iterator() ; it.hasNext() ; ){
                Map.Entry entry = (Map.Entry)it.next() ;
                if( entry.getKey().toString().equals(name) && 
                    entry.getValue().toString().equals(version) )return true ; 
             }
             return false ;
          }
          private void remove( String name ){
             _isModified = false ;
             _jars.remove(name) ;
          }
          public void clear(){ _jars = new HashMap() ; }
          public String getName(){ return _name ; }
          private boolean hasChanged(){ return _isModified ; }
          public Iterator jars(){ return _jars.keySet().iterator() ; }
          public Iterator jarSet(){ return _jars.entrySet().iterator() ; }
          private String get( String name ){ return (String)_jars.get(name) ; }
       } 
       ClassLoaderPreferences(  Preferences ourRoot ){
         _baseRoot = ourRoot ;
         reloadPreferences() ;
       }
       public Iterator connections(){ return new ArrayList( _jarSetPerConnection.keySet() ).iterator() ; }
       public void reloadPreferences(){
         loadClassLoaderPreferences( _baseRoot ) ;
       }
       public void save(){
          storeClassLoaderPreferences();
       }
       private void storeClassLoaderPreferences(){
          
          for( Iterator it = _jarSetPerConnection.values().iterator() ; it.hasNext() ; ){
          
             Connection connection = (Connection)it.next() ;
             
             if( ! connection.hasChanged() )continue ;
             
             String      connectionName = connection.getName() ;
             Preferences connectionPref = _baseRoot.node( connectionName ) ;
             Preferences jarList        = connectionPref.node("jars") ;
             
             try{
                jarList.clear() ;

                for( Iterator l = connection.jars() ; l.hasNext() ; ){
 
                   String name = (String)l.next() ;
                   jarList.put( name , connection.get(name) ) ;

                }
                jarList.sync() ;
             }catch(Exception ee ){
                 System.err.println("Problem in storing class loader infos for "+connectionName+" "+ee);
             }
          }
       
       }
       private void loadClassLoaderPreferences( Preferences ourRoot ){
          try{
             _jarSetPerConnection.clear();
             String [] children = ourRoot.childrenNames() ;
             for( int i = 0 , n = children.length ; i < n ; i++ ){
             
                String      connectionName = children[i] ;
                Preferences connectionPref = _baseRoot.node( connectionName ) ;
                Preferences jarList        = connectionPref.node("jars") ;
                
                Connection connection = new Connection(connectionName);
                
                String [] jars = jarList.keys() ;
                for( int j = 0 , m = jars.length ; j < m ; j++ ){
                   String jarName  = jars[j] ;
                   String version  = jarList.get(jarName,"0.0.0") ;
                   connection.add( jarName , version ) ;
                }
                _jarSetPerConnection.put( connectionName , connection ) ;
             }
          }catch(Exception ee ){
              ee.printStackTrace();
              System.err.println("Problem reading 'sessions'"+ee) ;
          }
      }
      public Connection getConnection( String name ){
         reloadPreferences() ;
         return (Connection)_jarSetPerConnection.get(name);
      }
    
    }
