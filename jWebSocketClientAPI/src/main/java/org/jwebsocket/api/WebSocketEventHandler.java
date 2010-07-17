//	---------------------------------------------------------------------------
//	jWebSocket - WebSocket CGI Client
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
package org.jwebsocket.api;
/**
 * Handler for all the <tt>WebSocket</tt> connection events 
 * referenced from http://weberknecht.googlecode.com by 
 * Roderick Baier.
 * 
 * @author Roderick Baier
 * @author agali
 * @version $Id:$
 */
public interface WebSocketEventHandler {

    /**
     * This method is invoked when a new client connects to the server.
     * @param websocket the WebSocket connection object
     */
     void onOpen(WebSocket websocket);

    /**
     * This method is invoked when a data packet is received from the server
     * @param websocket the WebSocket connection object
     * @param aPacket the data packet
     */
     void onMessage(WebSocket websocket, WebSocketMessage message);

    /**
     * This method is invoked when a client is disconnected
     * @param websocket the WebSocket connection object 
     */
     void onClose(WebSocket websocket);
    
     /**
      * Invoked if there's any exception
      * @param websocket the websocket object
      * @param clause the cause of the exception
      */
     void onException(WebSocket websocket, Throwable clause);

}
