//	---------------------------------------------------------------------------
//	jWebSocket - Abstract WebSocket Client
//	Copyright (c) 2010 jWebSocket.org, Alexander Schulze, Innotrade GmbH
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

import javolution.util.FastList;

import org.jwebsocket.api.JWebSocketClient;
import org.jwebsocket.api.WebSocket;
import org.jwebsocket.api.WebSocketClientEvent;
import org.jwebsocket.api.WebSocketClientListener;
import org.jwebsocket.api.WebSocketEventHandler;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.kit.WebSocketException;

/**
 * Abstract implementation of {@code JWebSocketClient} interface to provide the
 * <tt>jWebSocket</tt> protocol specific features.
 * 
 * @author aschulze
 * @author puran
 * @version $Id:$
 */
public abstract class AbstractJWebSocketClient implements JWebSocketClient {

    /** default maximum data frame size processed */
    public static final int MAX_FRAMESIZE = 16384;

    /** max data frame size */
    private int maxFrameSize = MAX_FRAMESIZE;

    /** list of the listeners registered */
    private List<WebSocketClientListener> listeners = new FastList<WebSocketClientListener>();

    /** WebSocket Connection object */
    private WebSocket webSocket = new BaseWebSocket();

    /** event handler that receives all the WebSocket events */
    private WebSocketEventHandler eventHandler = null;

    public AbstractJWebSocketClient() {
        eventHandler = new BaseWebSocketEventHandler(this, webSocket);
    }

    /**
     * {@inheritDoc} Use the {@code WebSocket} implementation to delegate the
     * jWebSocket connection request
     */
    @Override
    public void open(String uriString) throws WebSocketException {
        URI uri = null;
        try {
            uri = new URI(uriString);
        } catch (URISyntaxException e) {
            throw new WebSocketException("Error parsing WebSocket URL:" + uriString, e);
        }
        webSocket.setEventHandler(eventHandler);
        webSocket.open(uri);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addListener(WebSocketClientListener aListener) {
        listeners.add(aListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeListener(WebSocketClientListener aListener) {
        listeners.remove(aListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<WebSocketClientListener> getListeners() {
        return Collections.unmodifiableList(listeners);
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws WebSocketException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isConnected() {
        return webSocket.isConnected();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void received(String aData, String aEncoding) throws WebSocketException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void received(byte[] aData) throws WebSocketException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(String aData, String aEncoding) throws WebSocketException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(byte[] aData) throws WebSocketException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(WebSocketPacket dataPacket) throws WebSocketException {
    }

    /**
     * @return the max data frame size
     */
    public int getMaxFrameSize() {
        return maxFrameSize;
    }
}
