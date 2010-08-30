//	---------------------------------------------------------------------------
//	jWebSocket - Copyright (c) 2010 Innotrade GmbH
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
package org.jwebsocket.client.java;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.jwebsocket.api.WebSocketClient;
import org.jwebsocket.api.WebSocketClientEvent;
import org.jwebsocket.api.WebSocketClientListener;
import org.jwebsocket.api.WebSocketPacket;

import org.jwebsocket.api.WebSocketStatus;
import org.jwebsocket.client.token.WebSocketClientTokenEvent;
import org.jwebsocket.kit.RawPacket;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.kit.WebSocketHandshake;

/**
 * Base {@code WebSocket} implementation based on
 * http://weberknecht.googlecode.com by Roderick Baier. This uses thread model
 * for handling WebSocket connection which is defined by the <tt>WebSocket</tt>
 * protocol specification. {@linkplain http://www.whatwg.org/specs/web-socket-protocol/} 
 * {@linkplain http://www.w3.org/TR/websockets/}
 * 
 * @author Roderick Baier
 * @author agali
 * @author puran
 * @version $Id:$
 */
public class BaseWebSocket implements WebSocketClient {

    /** WebSocket connection url */
    private URI mURL = null;
    /** list of the listeners registered */
    private List<WebSocketClientListener> mListeners = new FastList<WebSocketClientListener>();
    /** flag for connection test */
    private volatile boolean mConnected = false;
    private boolean mIsBinaryData = false;
    /** TCP socket */
    private Socket mSocket = null;
    /** IO streams */
    private InputStream mInput = null;
    private PrintStream mOutput = null;
    /** Data receiver */
    private WebSocketReceiver mReceiver = null;
    private WebSocketHandshake mHandshake = null;
    /** represents the WebSocket status */
    private WebSocketStatus mStatus = WebSocketStatus.CLOSED;

    /**
     * Base constructor
     */
    public BaseWebSocket() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void open(String aURIString) throws WebSocketException {
        URI lURI = null;
        try {
            lURI = new URI(aURIString);
        } catch (URISyntaxException ex) {
            throw new WebSocketException("Error parsing WebSocket URL:" + aURIString, ex);
        }
        this.mURL = lURI;
        mHandshake = new WebSocketHandshake(mURL);
        try {
            mSocket = createSocket();
            mInput = mSocket.getInputStream();
            mOutput = new PrintStream(mSocket.getOutputStream());

            mOutput.write(mHandshake.getHandshake());

            boolean handshakeComplete = false;
            boolean header = true;
            int len = 1000;
            byte[] buffer = new byte[len];
            int pos = 0;
            ArrayList<String> handshakeLines = new ArrayList<String>();

            byte[] serverResponse = new byte[16];

            while (!handshakeComplete) {
                mStatus = WebSocketStatus.CONNECTING;
                int b = mInput.read();
                buffer[pos] = (byte) b;
                pos += 1;

                if (!header) {
                    serverResponse[pos - 1] = (byte) b;
                    if (pos == 16) {
                        handshakeComplete = true;
                    }
                } else if (buffer[pos - 1] == 0x0A && buffer[pos - 2] == 0x0D) {
                    String line = new String(buffer, "UTF-8");
                    if (line.trim().equals("")) {
                        header = false;
                    } else {
                        handshakeLines.add(line.trim());
                    }

                    buffer = new byte[len];
                    pos = 0;
                }
            }

            mHandshake.verifyServerStatusLine(handshakeLines.get(0));
            mHandshake.verifyServerResponse(serverResponse);

            handshakeLines.remove(0);

            Map<String, String> headers = new FastMap<String, String>();
            for (String line : handshakeLines) {
                String[] keyValue = line.split(": ", 2);
                headers.put(keyValue[0], keyValue[1]);
            }
            mHandshake.verifyServerHandshakeHeaders(headers);

            mReceiver = new WebSocketReceiver(mInput);

            // TODO: Add event parameter
            // notifyOpened(null);

            mReceiver.start();
            mConnected = true;
            mStatus = WebSocketStatus.OPEN;
        } catch (WebSocketException wse) {
            throw wse;
        } catch (IOException ioe) {
            throw new WebSocketException("error while connecting: " + ioe.getMessage(), ioe);
        }
    }

    @Override
    public void send(byte[] aData) throws WebSocketException {
        if (!mConnected) {
            throw new WebSocketException("error while sending binary data: not connected");
        }
        try {
            if (mIsBinaryData) {
                mOutput.write(0x80);
                // TODO: what if frame is longer than 255 characters (8bit?) Refer to IETF spec!
                mOutput.write(aData.length);
                mOutput.write(aData);
            } else {
                mOutput.write(0x00);
                mOutput.write(aData);
                mOutput.write(0xff);
            }
            mOutput.flush();
        } catch (IOException ex) {
            throw new WebSocketException("error while sending binary data: ", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(String aData, String aEncoding) throws WebSocketException {
        byte[] data;
        try {
            data = aData.getBytes(aEncoding);
            send(data);
        } catch (UnsupportedEncodingException e) {
            throw new WebSocketException("Encoding exception while sending the data:" + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(WebSocketPacket dataPacket) throws WebSocketException {
        send(dataPacket.getByteArray());
    }

    public void handleReceiverError() {
        try {
            if (mConnected) {
                mStatus = WebSocketStatus.CLOSING;
                close();
            }
        } catch (WebSocketException wse) {
            // TODO: don't use printStackTrace
            // wse.printStackTrace();
        }
    }

    @Override
    public synchronized void close() throws WebSocketException {
        if (!mConnected) {
            return;
        }
        sendCloseHandshake();
        if (mReceiver.isRunning()) {
            mReceiver.stopit();
        }
        try {
            // input.close();
            // output.close();
            mSocket.shutdownInput();
            mSocket.shutdownOutput();
            mSocket.close();
            mStatus = WebSocketStatus.CLOSED;
        } catch (IOException ioe) {
            throw new WebSocketException("error while closing websocket connection: ", ioe);
        }
        // TODO: add event
        notifyClosed(null);
    }

    private void sendCloseHandshake() throws WebSocketException {
        if (!mConnected) {
            throw new WebSocketException("error while sending close handshake: not connected");
        }
        try {
            mOutput.write(0xff00);
            // TODO: check if final CR/LF is required/valid!
            mOutput.write("\r\n".getBytes());
            // TODO: shouldn't we put a flush here?
        } catch (IOException ioe) {
            throw new WebSocketException("error while sending close handshake", ioe);
        }
        mConnected = false;
    }

    private Socket createSocket() throws WebSocketException {
        String scheme = mURL.getScheme();
        String host = mURL.getHost();
        int port = mURL.getPort();

        mSocket = null;

        if (scheme != null && scheme.equals("ws")) {
            if (port == -1) {
                port = 80;
            }
            try {
                mSocket = new Socket(host, port);
            } catch (UnknownHostException uhe) {
                throw new WebSocketException("unknown host: " + host, uhe);
            } catch (IOException ioe) {
                throw new WebSocketException("error while creating socket to " + mURL, ioe);
            }
        } else if (scheme != null && scheme.equals("wss")) {
            if (port == -1) {
                port = 443;
            }
            try {
                SocketFactory factory = SSLSocketFactory.getDefault();
                mSocket = factory.createSocket(host, port);
            } catch (UnknownHostException uhe) {
                throw new WebSocketException("unknown host: " + host, uhe);
            } catch (IOException ioe) {
                throw new WebSocketException("error while creating secure socket to " + mURL, ioe);
            }
        } else {
            throw new WebSocketException("unsupported protocol: " + scheme);
        }

        return mSocket;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean isConnected() {
        if (mConnected && mStatus.equals(WebSocketStatus.OPEN)) {
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc }
     */
    public WebSocketStatus getConnectionStatus() {
        return mStatus;
    }

    /**
     * @return the client socket
     */
    public Socket getConnectionSocket() {
        return mSocket;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addListener(WebSocketClientListener aListener) {
        mListeners.add(aListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeListener(WebSocketClientListener aListener) {
        mListeners.remove(aListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<WebSocketClientListener> getListeners() {
        return Collections.unmodifiableList(mListeners);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyOpened(WebSocketClientEvent aEvent) {
        for (WebSocketClientListener lListener : getListeners()) {
            lListener.processOpened(aEvent);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyPacket(WebSocketClientEvent aEvent, WebSocketPacket aPacket) {
        for (WebSocketClientListener lListener : getListeners()) {
            lListener.processPacket(aEvent, aPacket);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyClosed(WebSocketClientEvent aEvent) {
        for (WebSocketClientListener lListener : getListeners()) {
            lListener.processClosed(aEvent);
        }
    }

    class WebSocketReceiver extends Thread {

        private InputStream mIS = null;
        private volatile boolean mStop = false;

        public WebSocketReceiver(InputStream input) {
            this.mIS = input;
        }

        @Override
        public void run() {
            boolean lFrameStart = false;
            ByteArrayOutputStream lOS = new ByteArrayOutputStream();
            notifyOpened(null);
            while (!mStop) {
                try {
                    int b = mIS.read();
                    // TODO support binary frames
                    if (b == 0x00) {
                        lFrameStart = true;
                    } else if (b == 0xff && lFrameStart == true) {
                        lFrameStart = false;

                        WebSocketClientEvent lWSCE = new WebSocketClientTokenEvent();
                        RawPacket lPacket = new RawPacket(lOS.toByteArray());

                        lOS.reset();
                        notifyPacket(lWSCE, lPacket);
                    } else if (lFrameStart == true) {
                        // messageBytes.add((byte) b);
                        lOS.write(b);
                    } else if (b == -1) {
                        handleError();
                    }
                } catch (IOException ioe) {
                    handleError();
                }
            }
        }

        public void stopit() {
            mStop = true;
        }

        public boolean isRunning() {
            return !mStop;
        }

        private void handleError() {
            stopit();
        }
    }
}
