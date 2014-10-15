// $Id: DomainConnectionAdapter.java,v 1.2 2006-11-19 09:14:19 patrick Exp $
//
package org.pcells.services.connection;
//
import dmg.cells.applets.login.DomainObjectFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.Timestamp;
import java.util.*;


/**
 */
public class DomainConnectionAdapter implements DomainConnection {

    private static Logger  _logger = LoggerFactory.getLogger(DomainConnectionAdapter.class);

    private Map<DomainObjectFrame, DomainConnectionListener> _packetHash = new HashMap<DomainObjectFrame, DomainConnectionListener>();
    private final Object _ioLock = new Object();
    private int _ioCounter = 100;
    private List<DomainEventListener> _listener = new ArrayList<DomainEventListener>();
    private boolean _connected = false;
    private InputStream _inputStream = null;
    private OutputStream _outputStream = null;
    private Reader _reader = null;
    private Writer _writer = null;
    protected ObjectOutputStream _objOut = null;
    protected ObjectInputStream _objIn = null;

    public String getAuthenticatedUser() {
        return "Unknown";
    }

    public void setIoStreams(InputStream in, OutputStream out) {
        setIoStreams(in, out, null, null);
    }

    public void setIoStreams(InputStream in, OutputStream out,
            Reader reader, Writer writer) {

        _inputStream = in;
        _outputStream = out;
        _reader = reader;
        _writer = writer;

    }

    public void go() throws Exception {
        _logger.debug("runConnection started");
        try {
            runConnection();

            _logger.debug("runConnection OK");

            informListenersOpened();

            _logger.debug("runReceiver starting");
            runReceiver();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            _logger.debug("runReceiver finished");
            informListenersClosed();
        }
    }

    public void close() throws IOException {
        _objOut.close();
    }

    private void runConnection() throws IOException {

        InputStream inputstream = _inputStream;
        BufferedReader reader = new BufferedReader(
                _reader == null
                ? new InputStreamReader(inputstream)
                : _reader, 1);

        PrintWriter writer = new PrintWriter(_writer == null
                ? new OutputStreamWriter(_outputStream)
                : _writer);

        writer.println("$BINARY$");
        writer.flush();
        _logger.debug("Wrote Binary");
        String check;
        do {
            check = reader.readLine();
            _logger.debug("This was read from the InputStream: "+ check);
        } while (!check.equals("$BINARY$"));
        _objOut = new ObjectOutputStream(_outputStream);
        _objOut.flush();
        Calendar calendar = Calendar.getInstance();
        Date currentTimestamp = new Timestamp(calendar.getTime().getTime());
        _logger.debug(currentTimestamp.toString() + " Flushed ObjectOutputStream Opening object streams.");
        assert inputstream != null;
//            BufferedInputStream bufStream = new BufferedInputStream(teeIn);
        _objIn = new ObjectInputStream(inputstream);
        _logger.debug("Created ObjectStreams.");
    }

    protected void runReceiver() throws Exception {

        Object obj = null;
        DomainObjectFrame frame = null;
        DomainConnectionListener listener = null;

        while (true) {

            try {
                obj = _objIn.readObject();
            } catch (EOFException eofEx) {
                _objIn.close();
                break;
            }

            if (!(obj instanceof DomainObjectFrame)) {
                continue;
            }

            synchronized (_ioLock) {

                frame = (DomainObjectFrame) obj;
                listener = _packetHash.remove(frame);
                if (listener == null) {
                    _logger.error("Message without receiver : " + frame);
                    continue;
                }
            }
            try {
                listener.domainAnswerArrived(frame.getPayload(), frame.getSubId());
            } catch (Exception eee) {
                _logger.error("Problem in domainAnswerArrived : " + eee);
            }
        }
    }

    public int sendObject(Object obj,
            DomainConnectionListener listener,
            int id) throws IOException {

        synchronized (_ioLock) {

            if (!_connected) {
                throw new IOException("Not connected");
            }

            DomainObjectFrame frame =
                    new DomainObjectFrame((Serializable) obj, ++_ioCounter, id);

            _objOut.writeObject(frame);
            _objOut.reset();
            _packetHash.put(frame, listener);
            return _ioCounter;
        }
    }

    public int sendObject(String destination,
            Object obj,
            DomainConnectionListener listener,
            int id) throws IOException {
//         _logger.debug("Sending : "+obj ) ;
        synchronized (_ioLock) {
            if (!_connected) {
                throw new IOException("Not connected");
            }
            DomainObjectFrame frame =
                    new DomainObjectFrame(destination, (Serializable) obj, ++_ioCounter, id);
            _objOut.writeObject(frame);
            _objOut.reset();
            _packetHash.put(frame, listener);
            return _ioCounter;
        }
    }

    public void addDomainEventListener(DomainEventListener listener) {
        synchronized (_ioLock) {
            _listener.add(listener);
            if (_connected) {
                try {
                    listener.connectionOpened(this);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }

    public void removeDomainEventListener(DomainEventListener listener) {
        synchronized (_ioLock) {
            _listener.remove(listener);
        }
    }

    protected void informListenersOpened() {
        List<DomainEventListener> array = new ArrayList<DomainEventListener>(_listener);
        synchronized (_ioLock) {
            _connected = true;
            for (DomainEventListener listener : array) {

                try {
                    listener.connectionOpened(this);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }

    protected void informListenersClosed() {
        List<DomainEventListener> array = new ArrayList<DomainEventListener>(_listener);
        synchronized (_ioLock) {
            _connected = false;
            for (DomainEventListener listener : array) {
                try {
                    listener.connectionClosed(this);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }
}
