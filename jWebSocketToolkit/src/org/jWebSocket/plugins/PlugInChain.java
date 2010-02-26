//	---------------------------------------------------------------------------
//	jWebSocket - Listener Chain for incoming requests (per server)
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

import javolution.util.FastList;
import org.jWebSocket.api.IDataPacket;
import org.jWebSocket.api.IPlugIn;
import org.jWebSocket.api.IPlugInChain;
import org.jWebSocket.api.IWebSocketConnector;
import org.jWebSocket.api.IWebSocketEngine;

/**
 *
 * @author aschulze
 */
public class PlugInChain extends FastList<IPlugIn> implements IPlugInChain {

	public void engineStarted(IWebSocketEngine aEngine) {
	}

	public void engineStopped(IWebSocketEngine aEngine) {
	}

	/**
	 *
	 * @param aConnector
	 */
	public void connectorStarted(IWebSocketConnector aConnector) {
		for (int i = 0; i < size(); i++) {
			get(i).connectorStarted(aConnector);
		}
	}

	/**
	 *
	 * @param aConnector
	 * @param aObject
	 * @return
	 */
	public PlugInResponse processPacket(PlugInResponse aResponse, IWebSocketConnector aConnector, IDataPacket aDataPacket) {
		PlugInResponse lPluginResponse = new PlugInResponse();
		for (int i = 0; i < size(); i++) {
			get(i).processPacket(lPluginResponse, aConnector, aDataPacket);
			if (lPluginResponse.isChainAborted()) {
				break;
			}
		}
		return null;
	}

	/**
	 *
	 * @param aConnector
	 */
	public void connectorStopped(IWebSocketConnector aConnector) {
		for (int i = 0; i < size(); i++) {
			get(i).connectorStopped(aConnector);
		}
	}
}
