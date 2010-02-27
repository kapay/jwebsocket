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
package org.jWebSocket.server;

import org.apache.log4j.Logger;
import org.jWebSocket.plugins.IPlugIn;
import org.jWebSocket.api.IWebSocketConnector;
import org.jWebSocket.api.IWebSocketEngine;
import org.jWebSocket.plugins.PlugInChain;

/**
 *
 * @author aschulze
 */
public class TokenServer extends BaseServer {

	private static Logger log = Logger.getLogger(TokenServer.class);

	private PlugInChain plugInChain = null;

	/**
	 *
	 * @param aPort
	 * @param aSessionTimeout
	 * @param aListeners
	 */
	public TokenServer() {
		plugInChain = new PlugInChain();
	}

	/**
	 * adds a plugin to the plugin chain of the server.
	 * @param aPlugIn
	 */
	public void addPlugIn(IPlugIn aPlugIn) {
		if (plugInChain != null) {
			plugInChain.add(aPlugIn);
		}
	}

	public void engineStarted(IWebSocketEngine aEngine) {
		plugInChain.engineStarted(aEngine);
	}

	public void engineStopped(IWebSocketEngine aEngine) {
		plugInChain.engineStopped(aEngine);
	}

	public void connectorStarted(IWebSocketConnector aConnector) {
		// notify plugins that a connector has started,
		// i.e. a client was sconnected.
		if (plugInChain != null) {
			plugInChain.connectorStarted(aConnector);
		}
	}

	public void connectorStopped(IWebSocketConnector aConnector) {
		// notify plugins that a connector has stopped,
		// i.e. a client was disconnected.
		if (plugInChain != null) {
			plugInChain.connectorStopped(aConnector);
		}
	}


}
