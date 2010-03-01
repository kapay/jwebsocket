//	---------------------------------------------------------------------------
//	jWebSocket - Basic PlugIn Class
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

/**
 *
 * @author aschulze
 */
public interface IPlugIn {

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
	 *
	 * @param aConnector
	 */
	public void connectorStarted(IWebSocketConnector aConnector);

	/**
	 *
	 * @param aResponse
	 * @param aConnector
	 * @param aDataPacket
	 */
	public void processPacket(PlugInResponse aResponse, IWebSocketConnector aConnector, IDataPacket aDataPacket);

	/**
	 *
	 * @param aConnector
	 */
	public void connectorStopped(IWebSocketConnector aConnector);

	/**
	 *
	 * @param aPlugInChain
	 */
	public void setPlugInChain(IPlugInChain aPlugInChain);

	/**
	 * @return the plugInChain
	 */
	public IPlugInChain getPlugInChain();

}
