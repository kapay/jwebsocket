//	---------------------------------------------------------------------------
//	jWebSocket - Java WebSocket Client API
//	Copyright (c) 2010 jWebSocket.org, Alexander Schulze, Innotrade GmbH
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
//	more details.
//	You should have received a copy of the GNU General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/>.
//	---------------------------------------------------------------------------
package org.jWebSocket.client;

import org.jwebsocket.api.WebSocketPaket;

/**
 *
 * @author aschulze
 */
public interface WebSocketClient {

	/**
	 * Starts the WebSocket client.
	 */
	void startClient();

	/**
	 * Stops the WebSocket client.
	 */
	void stopClient();
	
	/**
	 * Notifies the application that the client has started.
	 */
	void clientStarted();
	
	/**
	 * Notifies the application that the client has stopped.
	 */
	void clientStopped();
	
	/**
	 * Allows the application to send a data packet to the server.
	 *
	 * @param aRawPacket
	 */
	void sendPacket(WebSocketPaket aRawPacket);
	
	/**
	 * Notifies the application that a data packet has been received from
	 * the server.
	 *
	 * @param aRawPacket
	 */
	void processPacket(WebSocketPaket aRawPacket);

}
