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

/**
 * Implementation for WebSocketEventHandler that handles all the <tt>WebSocket</tt>
 * events.
 * @author agali
 * @author puran
 */
public class BaseWebSocketEventHandler implements WebSocketEventHandler {
    
    /** reference to jWebSocket Client object*/
    private JWebSocketClient jWebSocketClient = null;
    
    private BaseWebSocket baseWebSocket = null;
    
    /**
     * Base constructor for the WebSocket event handler that delegates the
     * events to appropriate 
     * @param jWebSocketClient the jWebSocket client object reference
     */
    public BaseWebSocketEventHandler(JWebSocketClient jWebSocketClient, WebSocket webSocket) {
        this.jWebSocketClient = jWebSocketClient;
        this.baseWebSocket = (BaseWebSocket)webSocket;
    }

    @Override
    public void onClose() {
        jWebSocketClient.notifyClosed(new WebSocketCloseEvent());
    }

    @Override
    public void onException(Throwable clause) {
    }

    @Override
    public void onMessage(WebSocketMessage message) {
        WebSocketPacket dataPacket = getPacket(message);
        jWebSocketClient.notifyPacket(new WebSocketPacketEvent(), dataPacket);
    }

    @Override
    public void onOpen() {
        jWebSocketClient.notifyOpened(new WebSocketOpenEvent());
    }
    
    private WebSocketPacket getPacket(WebSocketMessage message) {
        return null;
    }

    
    /**
     * Connection open event for jWebSocket client 
     * @author puran
     * @version Id:$
     */
    class WebSocketOpenEvent implements WebSocketClientEvent {
    }
    
    class WebSocketPacketEvent implements WebSocketClientEvent {
        
    }
    
    class WebSocketCloseEvent implements WebSocketClientEvent {
        
    }
}
