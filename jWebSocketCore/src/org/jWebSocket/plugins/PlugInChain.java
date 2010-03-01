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

import java.util.Iterator;
import java.util.List;
import javolution.util.FastList;
import org.jWebSocket.api.IDataPacket;
import org.jWebSocket.api.IWebSocketConnector;
import org.jWebSocket.api.IWebSocketEngine;
import org.jWebSocket.api.IWebSocketServer;

/**
 *
 * @author aschulze
 */
public class PlugInChain implements IPlugInChain {

	private FastList<IPlugIn> plugins = new FastList<IPlugIn>();
	private IWebSocketServer server = null;

	/**
	 *
	 * @param aServer
	 */
	public PlugInChain(IWebSocketServer aServer) {
		server = aServer;
	}

	public void engineStarted(IWebSocketEngine aEngine) {
	}

	public void engineStopped(IWebSocketEngine aEngine) {
	}

	/**
	 *
	 * @param aConnector
	 */
	public void connectorStarted(IWebSocketConnector aConnector) {
		for (int i = 0; i < plugins.size(); i++) {
			plugins.get(i).connectorStarted(aConnector);
		}
	}

	/**
	 *
	 * @param aConnector
	 * @return
	 */
	public PlugInResponse processPacket(PlugInResponse aResponse, IWebSocketConnector aConnector, IDataPacket aDataPacket) {
		PlugInResponse lPluginResponse = new PlugInResponse();
		for (Iterator<IPlugIn> i = plugins.iterator(); i.hasNext();) {
			i.next().processPacket(lPluginResponse, aConnector, aDataPacket);
			if (lPluginResponse.isChainAborted()) {
				break;
			}
		}
		return lPluginResponse;
	}

	/**
	 *
	 * @param aConnector
	 */
	public void connectorStopped(IWebSocketConnector aConnector) {
		for (int i = 0; i < plugins.size(); i++) {
			plugins.get(i).connectorStopped(aConnector);
		}
	}

	/**
	 *
	 * @return
	 */
	public List<IPlugIn> getPlugIns() {
		return plugins;
	}


	/**
	 *
	 * @param aPlugIn
	 */
	public void addPlugIn(IPlugIn aPlugIn) {
		plugins.add(aPlugIn);
		aPlugIn.setPlugInChain(this);
	}

	/**
	 *
	 * @param aPlugIn
	 */
	public void removePlugIn(IPlugIn aPlugIn) {
		plugins.remove(aPlugIn);
		aPlugIn.setPlugInChain(null);
	}

	/**
	 * @return the server
	 */
	public IWebSocketServer getServer() {
		return server;
	}


}
