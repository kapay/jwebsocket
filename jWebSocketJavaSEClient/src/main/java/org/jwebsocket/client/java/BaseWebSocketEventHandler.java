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

import org.jwebsocket.api.JWebSocketClient;
import org.jwebsocket.api.WebSocket;
import org.jwebsocket.api.WebSocketClientEvent;
import org.jwebsocket.api.WebSocketEventHandler;
import org.jwebsocket.api.WebSocketMessage;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.kit.WebSocketException;

/**
 * Implementation for WebSocketEventHandler that handles all the base
 * <tt>WebSocket</tt> events and delegates the event handling along with 
 * event data to <tt>jWebSocket</tt> clients and listeners for specific 
 * event handling
 * @author agali
 * @author puran
 * @version $Id:$
 */
public class BaseWebSocketEventHandler implements WebSocketEventHandler {

    /** reference to jWebSocket Client object */
    private JWebSocketClient jWebSocketClient = null;

    /** reference of WebSocket */
    private BaseWebSocket baseWebSocket = null;

    /**
     * Base constructor for the WebSocket event handler that delegates the
     * events to appropriate
     * 
     * @param jWebSocketClient the jWebSocket client object reference
     */
    public BaseWebSocketEventHandler(JWebSocketClient jWebSocketClient, WebSocket webSocket) {
        this.jWebSocketClient = jWebSocketClient;
        this.baseWebSocket = (BaseWebSocket) webSocket;
    }

    /**
     * {@inheritDoc}
     * Perform the clean up
     */
    @Override
    public void onClose() {
        jWebSocketClient.notifyClosed(new WebSocketCloseEvent());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onException(Throwable clause) {
        clause.printStackTrace();
    }

    @Override
    public void onMessage(WebSocketMessage message) {
        WebSocketPacket dataPacket = new RawClientPacket(message);
        jWebSocketClient.notifyPacket(new WebSocketPacketEvent(), dataPacket);
        try {
            jWebSocketClient.received(dataPacket.getByteArray());
            jWebSocketClient.received(dataPacket.getUTF8(), "UTF-8");
        } catch (WebSocketException e) {
            // log
        }
    }

    @Override
    public void onOpen() {
        jWebSocketClient.notifyOpened(new WebSocketOpenEvent());
    }

    // event classes 
    
    class WebSocketOpenEvent implements WebSocketClientEvent {
    }

    class WebSocketPacketEvent implements WebSocketClientEvent {

    }

    class WebSocketCloseEvent implements WebSocketClientEvent {

    }
}
