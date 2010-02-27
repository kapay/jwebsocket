//	---------------------------------------------------------------------------
//	jWebSocket - PlugInChain Interface
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
package org.jWebSocket.plugins;

import org.jWebSocket.api.IDataPacket;
import org.jWebSocket.api.IWebSocketConnector;
import org.jWebSocket.api.IWebSocketEngine;
import org.jWebSocket.plugins.PlugInResponse;

/**
 *
 * @author aschulze
 */
public interface IPlugInChain {

	/**
	 * is called by the server when the engine has been started.
	 * @param aEngine
	 */
	void engineStarted(IWebSocketEngine aEngine);

	/**
	 * is called by the server when the engine has been stopped.
	 * @param aEngine
	 */
	void engineStopped(IWebSocketEngine aEngine);

	/**
	 * is called by the server when a new connector has been started,
	 * i.e. a new client has connected.
	 * @param aConnector
	 */
	void connectorStarted(IWebSocketConnector aConnector);

	/**
	 * is called when a data packet from a client was received
	 * and has to be processed.
	 * @param aResponse
	 * @param aConnector
	 * @param aDataPacket
	 * @return
	 */
	PlugInResponse processPacket(PlugInResponse aResponse, IWebSocketConnector aConnector, IDataPacket aDataPacket);

	/**
	 * is called by the server when a connector has been stopped,
	 * i.e. a client has disconnected.
	 * @param aConnector
	 */
	void connectorStopped(IWebSocketConnector aConnector);
}
