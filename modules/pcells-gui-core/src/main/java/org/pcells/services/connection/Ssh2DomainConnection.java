// $Id: Ssh1DomainConnection.java,v 1.5 2007/02/15 08:18:12 cvs Exp $
//
package org.pcells.services.connection;
//
import dmg.protocols.ssh.*;
import org.apache.sshd.ClientChannel;
import org.apache.sshd.ClientSession;
import org.apache.sshd.SshClient;
import org.apache.sshd.client.future.AuthFuture;
import org.apache.sshd.client.future.ConnectFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

/**
 */
public class Ssh2DomainConnection
        extends DomainConnectionAdapter
        implements SshClientAuthentication {

    private static Logger _logger;
    private String _hostname = null;
    private int _portnumber = 0;
    private Socket _socket = null;
    private ClientSession _session = null;
    public SshAuthRsa _rsaAuth = null;
    public String _password = null;
    public String _loginName = "Unknown";
    public String _privateKeyFilePath;
    public String _publicKeyFilePath;
    public String _keyPath;
    public String _algorithm;

    public Ssh2DomainConnection(String hostname, int portnumber) {
        _hostname = hostname;
        _portnumber = portnumber;
        _logger = LoggerFactory.getLogger(Ssh2DomainConnection.class);

        _logger.debug(this.getClass().getName() + " loadeded by : " + this.getClass().getClassLoader().getClass().getName());

    }

    public void go() throws Exception {
        _logger.debug("Running Ssh2 GO");
        SshClient ssh2Client = SshClient.setUpDefaultClient();
        _logger.debug("Initialized Ssh2Client");
        ssh2Client.start();
        try {
            _logger.debug("Ssh2Client started. Creating Session");
            ConnectFuture connectFuture = ssh2Client.connect(_hostname, _portnumber);
            connectFuture.awaitUninterruptibly();
            _logger.debug("Connection Ssh2 successfull?: " + connectFuture.isConnected());
            if (connectFuture.getSession() != null) {
                _session = connectFuture.getSession();
            } else {
                _logger.error("The session does not exist.");
            }
            _logger.debug("Ssh2ClientSession created: " + _session.toString());
            int ret = ClientSession.WAIT_AUTH;
            AuthFuture authFuture = null;
            while ((ret & ClientSession.WAIT_AUTH) != 0) {
                if ( _password.isEmpty() ) {
                    _logger.debug("++++++++++++ Keybaseed Login +++++++++++++++++");
                    _logger.debug("++++++++++++ with User: " + _loginName + "and keyPath: " + get_keyPath() + "and algorithm: " + get_algorithm() + " +++++++++++++++++");
                    KeyPair keyPair = loadKeyPair(_algorithm);
                    _logger.debug("Got key pair: " + keyPair.getPrivate().toString() + " and " + keyPair.getPublic().toString());
                    authFuture = _session.authPublicKey(_loginName, keyPair);
                    ret = _session.waitFor(ClientSession.WAIT_AUTH | ClientSession.CLOSED | ClientSession.AUTHED, 0);
                } else {
                    authFuture = _session.authPassword(_loginName, _password);
                    ret = _session.waitFor(ClientSession.WAIT_AUTH | ClientSession.CLOSED | ClientSession.AUTHED, 0);
                }
            }

            _logger.debug("Ssh2 AuthFuture: " + authFuture);
            if (authFuture != null) {
                if (authFuture.isSuccess()) {
                    ClientChannel channel = _session.createSubsystemChannel("pcells");
                    // Connecting streams of sshClient and DomainConnectionAdapter
                    PipedOutputStream guiOutputStream = new PipedOutputStream();
                    PipedInputStream clientInputStream = new PipedInputStream();
                    guiOutputStream.connect(clientInputStream);
                    PipedOutputStream clientOutputStream = new PipedOutputStream();
                    PipedInputStream guiInputStream = new PipedInputStream();
                    clientOutputStream.connect(guiInputStream);
                    channel.setIn(clientInputStream);
                    channel.setOut(clientOutputStream);
                    channel.setErr(new OutputStream()
                    {
                        @Override
                        public void write(int b) throws IOException
                        {
                            throw new UnsupportedOperationException("Not supported yet.");
                        }
                    });
                    channel.open().await();

                    _objOut = new ObjectOutputStream(guiOutputStream);
                    _objOut.flush();
                    Calendar calendar = Calendar.getInstance();
                    Date currentTimestamp = new Timestamp(calendar.getTime().getTime());
                    _logger.debug(currentTimestamp.toString() + " Flushed ObjectOutputStream Opening object streams.");
                    _objIn = new ObjectInputStream(guiInputStream);

                    try {
                        informListenersOpened();
                        runReceiver();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    } finally {
                        informListenersClosed();
                    }

                    int channelReturn = channel.waitFor(ClientChannel.CLOSED, 0);
                    if (channelReturn == ClientChannel.EXIT_SIGNAL) _session.close(true);
                }
            } else {
            }
        } catch (Exception e) {
            _logger.error("Ssh2 Exception caught: " + e.toString());
        } finally {
            ssh2Client.stop();
        }
    }

    public void setLoginName(String name) {
        _loginName = name;
    }

    public void setPassword(String password) {
        _password = password;
    }

    public void setKeyPairPaths(String privateKeyFile, String publicKeyFile) {
        _privateKeyFilePath = privateKeyFile;
        _publicKeyFilePath = publicKeyFile;
    }

    public void setIdentityFile(File identityFile) throws Exception {

        InputStream in = new FileInputStream(identityFile);
        SshRsaKey key = new SshRsaKey(in);
        try {
            in.close();
        } catch (Exception ee) {
        }

        _rsaAuth = new SshAuthRsa(key);

    }

    ////////////////////////////////////////////////////////////////////////////////////////
    //
    //   Client Authentication interface
    //
    private int _requestCounter = 0;

    public boolean isHostKey(InetAddress host, SshRsaKey keyModulus) {


        //      _logger.debug( "Host key Fingerprint\n   -->"+
        //                      keyModulus.getFingerPrint()+"<--\n"   ) ;

        //     NOTE : this is correctly done in : import dmg.cells.applets.login.SshLoginPanel

        return true;
    }
    public String getUser() {
        _requestCounter = 0;
        return _loginName;
    }

    public SshSharedKey getSharedKey(InetAddress host) {
        return null;
    }

    public SshAuthMethod getAuthMethod() {

        SshAuthMethod result = null;
        if (_requestCounter++ == 0) {
            if (_rsaAuth == null) {
                result = new SshAuthPassword(_password);
            } else {
                result = _rsaAuth;
            }
        } else if (_requestCounter++ <= 2) {
            result = new SshAuthPassword(_password);
        } else {
            result = null;
        }
//       _logger.debug("getAuthMethod("+_requestCounter+") "+result) ;
        return result;
    }

    public String get_privateKeyFilePath() {
        return _privateKeyFilePath;
    }

    public void set_privateKeyFilePath(String _privateKeyFilePath) {
        this._privateKeyFilePath = _privateKeyFilePath;
    }

    public String get_publicKeyFilePath() {
        return _publicKeyFilePath;
    }

    public void set_publicKeyFilePath(String _publicKeyFilePath) {
        this._publicKeyFilePath = _publicKeyFilePath;
    }

    public String get_keyPath() {
        return _keyPath;
    }

    public void set_keyPath(String _keyPath) {
        this._keyPath = _keyPath;
    }

    public String get_algorithm() {
        return _algorithm;
    }

    public void set_algorithm(String _algorithm) {
        this._algorithm = _algorithm;
    }

    public KeyPair loadKeyPair(String algorithm)
            throws IOException, NoSuchAlgorithmException,
            InvalidKeySpecException {
        _logger.debug("Loading key files");

        PublicKey publicKey = null;
        try {
            _logger.debug("Get Public Key: " + get_publicKeyFilePath() + " with algorithm: " + algorithm);
            publicKey = generatePublicKey(get_publicKeyFilePath(), algorithm);
            _logger.debug("Generated Public Key");
        } catch (Exception e) {
            _logger.error("Problem getting public key: ");
            e.printStackTrace();
        }
        PrivateKey privateKey = null;
        try {
            _logger.debug("Get Private Key: " + get_privateKeyFilePath() + " with algorithm: " + algorithm);
            privateKey = generatePrivateKey(get_privateKeyFilePath(), algorithm);
            _logger.debug("Generated Private Key");
        } catch (Exception e) {
            _logger.error("Problem getting private key: ");
            e.printStackTrace();
        }
        return new KeyPair(publicKey, privateKey);
    }

    private byte[] readFromKeyFile (String filename) {
        File f = new File(filename);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        DataInputStream dis = new DataInputStream(fis);
        byte[] keyBytes = new byte[(int)f.length()];
        try {
            dis.readFully(keyBytes);
            dis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        _logger.debug("Read file: " + filename);
        return keyBytes;
    }

    public PublicKey generatePublicKey(String filename, String algorithm)
            throws Exception {
        _logger.debug("Generating Public Key: " + filename + " , " + algorithm);
        byte[] keyBytes = readFromKeyFile(filename);
        X509EncodedKeySpec spec =
                new X509EncodedKeySpec(keyBytes);
        _logger.debug("Get Kefactory for algorithm: " + algorithm);
        KeyFactory kf = KeyFactory.getInstance(algorithm);
        return kf.generatePublic(spec);
    }

    public PrivateKey generatePrivateKey(String filename, String algorithm)
            throws Exception {
        _logger.debug("Generating Private Key: " + filename + " , " + algorithm);
        byte[] keyBytes = readFromKeyFile(filename);

        PKCS8EncodedKeySpec spec =
                new PKCS8EncodedKeySpec(keyBytes);
        _logger.debug("Get Keyfactory for algorithm: " + algorithm);
        KeyFactory kf = KeyFactory.getInstance(algorithm);
        return kf.generatePrivate(spec);
    }

    public static void main(String[] args) throws Exception {
//        if (args.length < 2) {
//
//            System.err.println("Usage : <hostname> <portNumber>");
//            System.exit(4);
//        }
//        String hostname = args[0];
//        int portnumber = Integer.parseInt(args[1]);
        String hostname = "localhost";
        int portnumber = 22224;
        Ssh2DomainConnection connection = new Ssh2DomainConnection(hostname, portnumber);
        try {
            _logger.debug("Pinging host:" + InetAddress.getByName(hostname).isReachable(1000));
            _logger.debug("Host is reachable");
        } catch (Exception e) {
            _logger.error("Host is not reachable");
            e.printStackTrace();
        }

        _logger.debug("Starting Test");
        RunConnection runCon = connection.test();
        new Thread(runCon).start();

    }

    private class RunConnection
            implements Runnable, DomainConnectionListener, DomainEventListener {

//        private Logger _logger = LoggerFactory.getLogger(RunConnection.class);

        public RunConnection() throws Exception {
            _logger.debug("class runConnection init");
            addDomainEventListener(this);
            _logger.debug("Event listener added");
            setLoginName("admin");
            _logger.debug("LoginName set");
//            setIdentityFile(new File("/Users/chris/.ssh/identity"));
            String userHome = System.getProperties().getProperty("user.home");
            String keyPath = userHome+".ssh";
            set_keyPath(keyPath);
            set_privateKeyFilePath(userHome + File.separator + ".ssh" + File.separator + "id_dsa.der");
            set_publicKeyFilePath(userHome + File.separator + ".ssh" + File.separator + "id_dsa.pub.der");
            set_algorithm("DSA");
            setLoginName("admin");
            _logger.debug("Keys set to: " + get_privateKeyFilePath() + " and " + get_publicKeyFilePath());
            setPassword("");
            _logger.debug("Password set");
        }

        @Override
        public void run() {
            try {
                _logger.debug("started Thread run");
                go();
//                connectionOpened(new Ssh2DomainConnection(_hostname, _portnumber));
                _logger.debug("After go() call");
            } catch (Exception ee) {
                _logger.error("RunConnection got : " + ee);
                ee.printStackTrace();
            }
        }

        public void domainAnswerArrived(Object obj, int id) {
            _logger.debug("Answer : " + obj);
            if (id == 54) {
                try {
                    sendObject("logoff", this, 55);
                } catch (Exception ee) {
                    _logger.error("Exception in sendObject" + ee);
                }
            }
        }

        public void connectionOpened(DomainConnection connection) {
            _logger.debug("DomainConnection : connectionOpened");
            try {
                sendObject("System", "ps -f", this, 54);
            } catch (Exception ee) {
                _logger.error("Exception in sendObject" + ee);
            }
        }

        public void connectionClosed(DomainConnection connection) {
            _logger.debug("DomainConnection : connectionClosed");
        }

        public void connectionOutOfBand(DomainConnection connection,
                Object subject) {
            _logger.debug("DomainConnection : connectionOutOfBand");
        }
    }

    public RunConnection test() throws Exception {
        _logger.debug("Starting Test method");
        return new Ssh2DomainConnection.RunConnection();
    }
}
