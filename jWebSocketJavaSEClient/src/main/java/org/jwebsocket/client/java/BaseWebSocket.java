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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import org.jwebsocket.api.WebSocket;
import org.jwebsocket.api.WebSocketEventHandler;
import org.jwebsocket.api.WebSocketStatus;
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
public class BaseWebSocket implements WebSocket {

    /** WebSocket connection url */
    private URI url = null;

    /** websocket event handler */
    private WebSocketEventHandler eventHandler = null;

    /** flag for connection test */
    private volatile boolean connected = false;
    
    private boolean isBinaryData = false;

    /** TCP socket */
    private Socket socket = null;

    /** IO streams */
    private InputStream input = null;
    private PrintStream output = null;

    /** Data receiver */
    private WebSocketReceiver receiver = null;
    private WebSocketHandshake handshake = null;

    /** represents the WebSocket status */
    private WebSocketStatus status = WebSocketStatus.CLOSED;

    /**
     * Base constructor
     */
    public BaseWebSocket() {
    }

    /**
     * {@inheritDoc}
     */
    public void setEventHandler(WebSocketEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    /**
     * {@inheritDoc}
     */
    public WebSocketEventHandler getEventHandler() {
        return this.eventHandler;
    }

    /**
     * {@inheritDoc}
     */
    public void open(URI uri) throws WebSocketException {
        this.url = uri;
        handshake = new WebSocketHandshake(url);
        try {
            socket = createSocket();
            input = socket.getInputStream();
            output = new PrintStream(socket.getOutputStream());

            output.write(handshake.getHandshake());

            boolean handshakeComplete = false;
            boolean header = true;
            int len = 1000;
            byte[] buffer = new byte[len];
            int pos = 0;
            ArrayList<String> handshakeLines = new ArrayList<String>();

            byte[] serverResponse = new byte[16];

            while (!handshakeComplete) {
                status = WebSocketStatus.CONNECTING;
                int b = input.read();
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

            handshake.verifyServerStatusLine(handshakeLines.get(0));
            handshake.verifyServerResponse(serverResponse);

            handshakeLines.remove(0);

            HashMap<String, String> headers = new HashMap<String, String>();
            for (String line : handshakeLines) {
                String[] keyValue = line.split(": ", 2);
                headers.put(keyValue[0], keyValue[1]);
            }
            handshake.verifyServerHandshakeHeaders(headers);

            receiver = new WebSocketReceiver(input, eventHandler);
            receiver.start();
            connected = true;
            status = WebSocketStatus.OPEN;
            eventHandler.onOpen();
        } catch (WebSocketException wse) {
            throw wse;
        } catch (IOException ioe) {
            throw new WebSocketException("error while connecting: " + ioe.getMessage(), ioe);
        }
    }

    public void send(String data) throws WebSocketException {
        if (!connected) {
            throw new WebSocketException("error while sending text data: not connected");
        }
        try {
            output.write(0x00);
            output.write(data.getBytes(("UTF-8")));
            output.write(0xff);
            output.write("\r\n".getBytes());
        } catch (UnsupportedEncodingException uee) {
            throw new WebSocketException("error while sending text data: unsupported encoding", uee);
        } catch (IOException ioe) {
            throw new WebSocketException("error while sending text data", ioe);
        }
    }

    public void send(byte[] data) throws WebSocketException {
        if (!connected) {
            throw new WebSocketException("error while sending binary data: not connected");
        }
        try {
            if (isBinaryData) {
                output.write(0x80);
                output.write(data.length);
                output.write(data);
                output.write("\r\n".getBytes());    
            } else {
                output.write(0x00);
                output.write(data);
                output.write(0xff);
                output.write("\r\n".getBytes());
            }
        } catch (IOException ioe) {
            throw new WebSocketException("error while sending binary data: ", ioe);
        }
    }

    public void handleReceiverError() {
        try {
            if (connected) {
                status = WebSocketStatus.CLOSING;
                close();
            }
        } catch (WebSocketException wse) {
            wse.printStackTrace();
        }
    }

    public synchronized void close() throws WebSocketException {
        if (!connected) {
            return;
        }
        sendCloseHandshake();
        if (receiver.isRunning()) {
            receiver.stopit();
        }
        try {
            input.close();
            output.close();
            socket.close();
            status = WebSocketStatus.CLOSED;
        } catch (IOException ioe) {
            throw new WebSocketException("error while closing websocket connection: ", ioe);
        }
        eventHandler.onClose();
    }

    private void sendCloseHandshake() throws WebSocketException {
        if (!connected) {
            throw new WebSocketException("error while sending close handshake: not connected");
        }

        try {
            output.write(0xff00);
            output.write("\r\n".getBytes());
        } catch (IOException ioe) {
            throw new WebSocketException("error while sending close handshake", ioe);
        }

        connected = false;
    }

    private Socket createSocket() throws WebSocketException {
        String scheme = url.getScheme();
        String host = url.getHost();
        int port = url.getPort();

        Socket socket = null;

        if (scheme != null && scheme.equals("ws")) {
            if (port == -1) {
                port = 80;
            }
            try {
                socket = new Socket(host, port);
            } catch (UnknownHostException uhe) {
                throw new WebSocketException("unknown host: " + host, uhe);
            } catch (IOException ioe) {
                throw new WebSocketException("error while creating socket to " + url, ioe);
            }
        } else if (scheme != null && scheme.equals("wss")) {
            if (port == -1) {
                port = 443;
            }
            try {
                SocketFactory factory = SSLSocketFactory.getDefault();
                socket = factory.createSocket(host, port);
            } catch (UnknownHostException uhe) {
                throw new WebSocketException("unknown host: " + host, uhe);
            } catch (IOException ioe) {
                throw new WebSocketException("error while creating secure socket to " + url, ioe);
            }
        } else {
            throw new WebSocketException("unsupported protocol: " + scheme);
        }

        return socket;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean isConnected() {
        if (connected && status.equals(WebSocketStatus.OPEN)) {
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public WebSocketStatus getConnectionStatus() {
        return status;
    }
    /**
     * @return the client socket
     */
    public Socket getConnectionSocket() {
        return socket;
    }
}
