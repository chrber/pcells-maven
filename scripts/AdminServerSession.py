#!/usr/bin/env jython


from org.pcells.services.connection import *
from java.util.concurrent import Semaphore
from java.lang import Exception
from java.io import File, FileNotFoundException
import sys
import threading
import re
import string
import getopt
from types import *


# This semaphore is used to ensure that connectionOpened() has been called
# in the EventListener before the AdminServerSession constructor returns.
# Note that this a Java semaphore, not a python one.
_ConnectionSema = Semaphore( 1 )


class _myEventListener( DomainEventListener ) :
    """
    This class is required by the Java admin server interface.
    It sets a semaphore when the connection opens/closes so that
    the main session will be notified when the session is active.
    """
    def __init__( self, verbose ) :
        global _ConnectionSema
        self._verbose = verbose
        if verbose :
                print "_myEventListener.__init__: Acquire semaphore."
        _ConnectionSema.acquire() # Connection is initially closed

    def connectionOpened( self, connection ) :
        global _ConnectionSema
        if self._verbose :
            print "Connection opened"
        _ConnectionSema.release()

    def connectionClosed( self, connection ) :
        global _ConnectionSema
        if self._verbose :
            print "Connection closed"
        if verbose :
                print "_myEventListener.connectionClosed: Acquire semaphore."
        _ConnectionSema.acquire()

    def connectionOutOfBand( self, connection, subject ) :
        if self._verbose :
            print "Connection out of band"


class _myDomainConnectionListener( DomainConnectionListener ) :
    """
    This class is required by the Java admin server interface.
    """
    def __init__( self, verbose = 0 ) :
        self._verbose = verbose
        self._answer = None
        # The semaphore is used to signal when a response arrived.
        # Note that this a Java semaphore, not a python one.
        self._sema = Semaphore( 1 )
        self._sema.acquire() # The answer is initially empty...

    def domainAnswerArrived( self, obj, id ) :
        if self._verbose :
            print "answer arrived:\n", obj
        self._answer = ( obj, id )
        self._sema.release()

    def waitForReply( self ) :
        self._sema.acquire()
        if self._answer == None :
            print "waitForReply: received bogus reply from server"
            sys.exit( 1 )
        return self._answer


class _msgThread( threading.Thread ) :
    """
    This implements a thread to run the connection to the admin server.
    It has to run in a separate thread because of the infinite listen
    loop in the java code.
    """
    def __init__( self, func, name ) :
        threading.Thread.__init__( self, name=name )
        self.setDaemon( 1 ) # if main exits, this thread should go away!
        self._func = func

    def run( self ) :
        self._func()

    def join( self, timeout = None ) :
        threading.Thread.join( self, timeout )


class SessionError ( Exception ):
    """
    This exception class is used to wrap any exceptions thrown by the
    underlying Java code. This avoids exposing the user of this module
    to the imports of the Java exceptions.
    """
    def __init__( self, message ) :
        self.msg = message

    def getMessage( self ) :
        return self.msg


class AdminServerSession:
    """
    This class initiates a connection to the dCache admin server specified
    by the server/port/login/password arguments. It then allows commands to
    be sent to specific cells of the admin server. The results are returned
    as a Jython object. The type of the object is specific to the admin
    command that was executed.
    When used in conjunction with the runAdminCommands() function below,
    it provides a simple-to-use way to script admin server commands.
    """

    def __init__( self, host, port, login, password, verbose = 0 ) :
    
        self._errorPattern = re.compile( "Command not found :" )
        self._verbose = verbose
        self._hostId = str( host ) + ":" + str( port )
        if verbose :
            print "AdminServerSession: connect to ", login + "@" + self._hostId

        # Define the ListenerObject for all incoming messages
        self._responseListener = _myDomainConnectionListener( verbose )
        # We set up the connection parameters.
        # The SSH v1 connection is provided by dmg.protocols.ssh.
        self._connection = Ssh2DomainConnection( host, port )
        self._connection.setLoginName( login )
        #self._connection.setPrivateKeyPath("/Users/chris/.ssh/id_dsa")
        self._connection.set_algorithm("DSA")
	# If the password argument starts with a /, we assume it is a file
	# containing the password, rather than the actual password.
	if password[0] == "/" :
	    self._connection.setPassword("")
            self._connection.set_privateKeyFilePath(password)
            self._connection.set_publicKeyFilePath(password+".pub")
            # hack:
            self._connection.set_keyPath("/Users/chris/.ssh")
            self._connection.set_publicKeyFilePath("/Users/chris/.ssh/id_dsa.pub.der")
            self._connection.set_privateKeyFilePath("/Users/chris/.ssh/id_dsa.der")
	else :
	    self._connection.setPassword( password )

        self._connection.addDomainEventListener( _myEventListener( verbose ) )

        # Fire up the connection...
        try :
            if verbose :
                print "AdminServerSession: starting connection"
            self._thread = _msgThread( self._connection.go, "AdminSvrSession" )
            if verbose :
                print "AdminServerSession: Running go"
            self._thread.start()
            if verbose :
                print "AdminServerSession: Start thread" 
            # Don't allow sending commands until the connection is up.
            # You should only call this once. A second call will deadlock.
            if verbose :
                print "AdminServerSession: Acquire semaphore."
            _ConnectionSema.acquire()
            if verbose :
                print "AdminServerSession: connected"

        except SshAuthenticationException, e :
            print "authentication failed: ", e.getMessage()
            sys.exit( 1 )

    def sendCommand( self, cellName, command ) :
        """ This sends the specified command to the specified cell.
            The cellName argument should be a string.
            The command argument is typically a string, but it is possible
            that some future admin server cell might do something different.
        """
        try:
            if self._verbose :
                print "sending command '" + str( command ) + "' to cell '", \
                       cellName, "' at host ", self._hostId

            self._connection.sendObject( cellName, command, \
                                         self._responseListener, 1 )
            if self._verbose :
                print "awaiting reply from ", self._hostId

            result = self._responseListener.waitForReply()

            if self._verbose :
                print "received reply from ", self._hostId
            if type( result[0] ) is StringType and \
	       self._errorPattern.match( result[0] ) :
                raise SessionError( "Command '" + str( command ) + \
                                    "' not recognized by server" )
            return result[0]

        except :
            print "sendCommand: got exception:", sys.exc_value
            return None

    def close( self ) :
        # Don't call close() on _connection or it will crash.
        # We just try to stop the connection thread.
        self._thread.join( 0.1 )


def _usage( extraUsage ) :
    """This method prints the usage for the command. The caller can pass
       additional usage information via the commandline. Extra args must
       appear after the arguments required by the AdminSessionServer.
    """
    print "%s [-v] host port login password %s\n" % ( sys.argv[0], extraUsage )
    print "If password begins with a /, it is assumed to be the path"
    print "to an identity file"
    sys.exit( 1 )


def runAdminCommands( func, extraUsage = "", params = {} ) :

    verbose = 0
    try:
        opts, args = getopt.getopt( sys.argv[1:], 'hv', ["help"] )
    except getopt.GetoptError :
        _usage( extraUsage )
    for o, a in opts :
        if o == "-v" :
            verbose = 1
        else :
            _usage( extraUsage )

    if len( args ) < 4 :
        _usage( extraUsage )
    # We name the arguments in the hope of gaining some clarity
    host = args[0]
    # Some arguments and replies from the admin server must be integers
    try:
       port = int( args[1] )
    except:
        print "%s: port argument must be an integer" % sys.argv[0]
        _usage( extraUsage )
    login = args[2]
    password = args[3]
    args.append(params)

    try :
        if verbose:
            print "creating admin session"
        session = AdminServerSession( host, port, login, password, verbose )
        if verbose:
            print "calling command function"
        # Pass the verbose flag and any unused options
        retval = func( session.sendCommand, verbose, opts[verbose:], args[4:] )
        if verbose:
            print "closing session"
        session.close()

    except SessionError, e :
        print "Admin server error:", e.getMessage()
        sys.exit( 1 )
    except Exception, ex:
        print "Unexpected exception:", ex.getMessage()
        sys.exit( 2 )

    return retval
    
def getAdminServerSession( ) :

    verbose = 0
    try:
        opts, args = getopt.getopt( sys.argv[1:], 'hv', ["help"] )
    except getopt.GetoptError :
        _usage( "" )
    for o, a in opts :
        if o == "-v" :
            verbose = 1

    if len( args ) < 4 :
        _usage( extraUsage )
    # We name the arguments in the hope of gaining some clarity
    host = args[0]
    # Some arguments and replies from the admin server must be integers
    try:
       port = int( args[1] )
    except:
        print "%s: port argument must be an integer" % sys.argv[0]
        _usage( extraUsage )
    login = args[2]
    password = args[3]

    try :
        if verbose:
            print "creating admin session"
        session = AdminServerSession( host, port, login, password, verbose )

    except SessionError, e :
        print "Admin server error:", e.getMessage()
        sys.exit( 1 )
    except Exception, ex:
        print "Unexpected exception:", ex.getMessage()
        sys.exit( 2 )

    return session
