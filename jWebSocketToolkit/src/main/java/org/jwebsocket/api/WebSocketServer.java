//	---------------------------------------------------------------------------
//	jWebSocket - Server API
//	Copyright (c) 2010 Alexander Schulze, Innotrade GmbH
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
package org.jwebsocket.api;

import org.jwebsocket.kit.BroadcastOptions;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.WebSocketException;

/**
 * Specifies the API of the jWebSocket server core and its capabilities. Each 
 * server can be bound to one or multiple engines. Each engine can drive or
 * more servers above.
 * The servers usually are not supposed to directly implement any business
 * logic - except for very small or special non token based applications.
 * For applications it is recommended to implement them in plug-ins based on
 * the token server.
 * @author aschulze
 */
public interface WebSocketServer {

	/**
	 * Starts the server and all underlying engines.
	 * @throws WebSocketException
	 */
	void startServer() throws WebSocketException;

	/**
	 * States if at least one of the engines is still running.
	 * @return Boolean state if at least one of the underlying engines is still running.
	 */
	boolean isAlive();

	/**
	 * Stops the server and all underlying engines.
	 * @throws WebSocketException
	 */
	void stopServer() throws WebSocketException;

	/**
	 * Adds a new engine to the server.
	 * @param aEngine
	 */
	void addEngine(WebSocketEngine aEngine);

	/**
	 * Removes a already bound engine from the server.
	 * @param aEngine
	 */
	void removeEngine(WebSocketEngine aEngine);

	/**
	 * Is called from the underlying engine when the engine is started.
	 * @param aEngine
	 */
	void engineStarted(WebSocketEngine aEngine);

	/**
	 * Is called from the underlying engine when the engine is stopped.
	 * @param aEngine
	 */
	void engineStopped(WebSocketEngine aEngine);

	/**
	 * Notifies the application that a client connector has been started.
	 * @param aConnector
	 */
	void connectorStarted(WebSocketConnector aConnector);

	/**
	 * Notifies the application that a client connector has been stopped.
	 * @param aConnector
	 */
	void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason);

	/**
	 * Is called when the underlying engine received a packet from a connector.
	 * @param aEngine
	 * @param aConnector
	 * @param aDataPacket
	 */
	void processPacket(WebSocketEngine aEngine, WebSocketConnector aConnector, WebSocketPaket aDataPacket);

	/**
	 * Sends a packet to a certain connector.
	 * @param aConnector 
	 * @param aDataPacket
	 */
	void sendPacket(WebSocketConnector aConnector, WebSocketPaket aDataPacket);

	/**
	 * Broadcasts a datapacket to all connectors.
	 * @param aDataPacket
	 */
	void broadcastPacket(WebSocketConnector aSource, WebSocketPaket aDataPacket, 
			BroadcastOptions aBroadcastOptions );

	/**
	 * Returns the unique id of the connector.
	 * @return
	 */
	String getId();

}