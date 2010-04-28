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
package org.jwebsocket.plugins;

import org.jwebsocket.api.PlugInResponse;
import org.jwebsocket.api.PlugIn;
import org.jwebsocket.api.PlugInChain;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketPaket;
import org.jwebsocket.api.CloseReason;

/**
 *
 * @author aschulze
 */
public abstract class BasePlugIn implements PlugIn {

	private PlugInChain plugInChain = null;

	@Override
	public abstract void engineStarted(WebSocketEngine aEngine);

	@Override
	public abstract void engineStopped(WebSocketEngine aEngine);

	/**
	 *
	 * @param aConnector
	 */
	@Override
	public abstract void connectorStarted(WebSocketConnector aConnector);

	/**
	 *
	 * @param aResponse 
	 * @param aConnector
	 * @param aDataPacket
	 */
	@Override
	public abstract void processPacket(PlugInResponse aResponse, WebSocketConnector aConnector, WebSocketPaket aDataPacket);

	/**
	 *
	 * @param aConnector
	 * @param aCloseReason
	 */
	@Override
	public abstract void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason);

	/**
	 *
	 * @param aPlugInChain
	 */
	@Override
	public void setPlugInChain(PlugInChain aPlugInChain) {
		plugInChain = aPlugInChain;
	}

	/**
	 * @return the plugInChain
	 */
	@Override
	public PlugInChain getPlugInChain() {
		return plugInChain;
	}
}
