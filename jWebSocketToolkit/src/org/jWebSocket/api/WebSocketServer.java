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
package org.jWebSocket.api;

import org.jWebSocket.kit.WebSocketException;

/**
 *
 * @author aschulze
 */
public interface WebSocketServer {

	/**
	 * starts the server and all underlying engines.
	 *
	 * @throws WebSocketException
	 */
	void startServer() throws WebSocketException;

	/**
	 * states if at least one of the engines is still running.
	 * @return
	 */
	boolean isAlive();

	/**
	 * stops the server and all underlying engines.
	 *
	 * @throws WebSocketException
	 */
	void stopServer() throws WebSocketException;

	/**
	 * adds a new engine to the server.
	 * @param aEngine
	 */
	void addEngine(WebSocketEngine aEngine);

	/**
	 * removes a already bound engine from the server.
	 * @param aEngine
	 */
	void removeEngine(WebSocketEngine aEngine);

	/**
	 * is called from the underlying engine when the engine is started.
	 * @param aEngine
	 */
	void engineStarted(WebSocketEngine aEngine);

	/**
	 * is called from the underlying engine when the engine is stopped.
	 * @param aEngine
	 */
	void engineStopped(WebSocketEngine aEngine);

	/**
	 * notifies the application that a client connector has been started.
	 * @param aConnector
	 */
	void connectorStarted(WebSocketConnector aConnector);

	/**
	 * notifies the application that a client connector has been stopped.
	 * @param aConnector
	 */
	void connectorStopped(WebSocketConnector aConnector);

	/**
	 * is called when the underlying engine received a packet from a connector.
	 * @param aEngine
	 * @param aConnector
	 * @param aDataPacket
	 */
	void processPacket(WebSocketEngine aEngine, WebSocketConnector aConnector, WebSocketPaket aDataPacket);

	/**
	 * sends a packet to a certain connector.
	 * @param aConnector 
	 * @param aDataPacket
	 */
	void sendPacket(WebSocketConnector aConnector, WebSocketPaket aDataPacket);

	/**
	 * broadcasts a datapacket to all connectors.
	 * @param aDataPacket
	 */
	void broadcastPacket(WebSocketPaket aDataPacket);

}
