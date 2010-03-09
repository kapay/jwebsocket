//	---------------------------------------------------------------------------
//	jWebSocket - WebSocket Token Server (abstract)
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
package org.jwebsocket.server;

import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketPaket;
import org.jwebsocket.plugins.PlugIn;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.plugins.BasePlugInChain;

/**
 *
 * @author aschulze
 */
public class CustomServer extends BaseServer {

	private static Logger log = Logger.getLogger(CustomServer.class);
	private BasePlugInChain plugInChain = null;

	/**
	 *
	 *
	 * @param aId
	 */
	public CustomServer(String aId) {
		super(aId);
		plugInChain = new BasePlugInChain(this);
	}

	@Override
	public void processPacket(WebSocketEngine aEngine, WebSocketConnector aConnector, WebSocketPaket aDataPacket) {
		log.debug("Processing data packet '" + aDataPacket.getUTF8() + "'...");
		// TODO: process the packet in a meaningful way - don't just broadcast to all!
		// broadcastPacket(aDataPacket);
	}

	/**
	 * removes a plugin from the plugin chain of the server.
	 * @param aPlugIn
	 */
	public void removePlugIn(PlugIn aPlugIn) {
		plugInChain.removePlugIn(aPlugIn);
	}

	@Override
	public void engineStarted(WebSocketEngine aEngine) {
		log.debug("Processing engine started...");
		plugInChain.engineStarted(aEngine);
	}

	@Override
	public void engineStopped(WebSocketEngine aEngine) {
		log.debug("Processing engine stopped...");
		plugInChain.engineStopped(aEngine);
	}

	@Override
	public void connectorStarted(WebSocketConnector aConnector) {
		log.debug("Processing connector started...");
		// notify plugins that a connector has started,
		// i.e. a client was sconnected.
		plugInChain.connectorStarted(aConnector);
	}

	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
		log.debug("Processing connector stopped...");
		// notify plugins that a connector has stopped,
		// i.e. a client was disconnected.
		plugInChain.connectorStopped(aConnector, aCloseReason);
	}

	/**
	 * @return the plugInChain
	 */
	public BasePlugInChain getPlugInChain() {
		return plugInChain;
	}
}
