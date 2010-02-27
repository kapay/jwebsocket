//	---------------------------------------------------------------------------
//	jWebSocket - Copyright (c) 2010 jwebsocket.org
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

import java.util.List;
import org.jWebSocket.kit.Header;
import org.jWebSocket.kit.WebSocketException;

/**
 * @author Puran Singh
 * @author Alexander Schulze
 * @version $Id: JWebSocketServer.java 63 2010-02-17 04:08:28Z mailtopuran $
 *
 */
public interface IWebSocketEngine {

	/**
	 * starts the engine.
	 */
	void startEngine() throws WebSocketException;

	/**
	 * stops the engine.
	 */
	void stopEngine() throws WebSocketException;

	/**
	 * notifies the server that the engine is started for the first time.
	 */
	void engineStarted();

	/**
	 * notifies the server that the engine has stopped.
	 */
	public void engineStopped();

	/**
	 * notifies the server that a client connector has been  started.
	 *
	 * @param aConnector
	 */
	void connectorStarted(IWebSocketConnector aConnector);

	/**
	 * notifies the server that a client connector has been  stopped.
	 *
	 * @param aConnector
	 */
	void connectorStopped(IWebSocketConnector aConnector);

	/**
	 * Returns the list of connector clients connected to this server
	 * @return the connector clients
	 */
	List<IWebSocketConnector> getConnectors();

	/**
	 * Returns {@ocde true} if the server is running {@code false} otherwise
	 * @return true or false based on the server status
	 */
	boolean isAlive();

	/**
	 * processes incoming data from a certain connector
	 *
	 *
	 * @param aConnector
	 * @param aData
	 */
	void processPacket(IWebSocketConnector aConnector, IDataPacket aDataPacket);

	/**
	 * sends a data packet to a certain connector.
	 *
	 * @param aConnector
	 * @param aData
	 */
	void sendPacket(IWebSocketConnector aConnector, IDataPacket aDataPacket);

	/**
	 * broadcasts a data packet to all connectors.
	 *
	 * @param aData
	 */
	void broadcastPacket(IDataPacket aDataPacket);

	/**
	 * removes a certain connector from the engine.
	 *
	 * @param aConnector
	 */
	void removeConnector(IWebSocketConnector aConnector);
}


