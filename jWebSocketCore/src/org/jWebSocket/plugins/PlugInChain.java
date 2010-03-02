//	---------------------------------------------------------------------------
//	jWebSocket - Plug in chain for incoming requests (per server)
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

import java.util.List;
import javolution.util.FastList;
import org.apache.log4j.Logger;
import org.jWebSocket.api.IDataPacket;
import org.jWebSocket.api.IWebSocketConnector;
import org.jWebSocket.api.IWebSocketEngine;
import org.jWebSocket.api.IWebSocketServer;

/**
 *
 * @author aschulze
 */
public class PlugInChain implements IPlugInChain {

	private static Logger log = Logger.getLogger(PlugInChain.class);
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
		log.debug("Notifying plug-ins that connector started...");
		try {
			for (IPlugIn plugIn : getPlugIns()) {
				try {
					// log.debug("Notifying plug-in " + plugIn + " that connector started...");
					plugIn.connectorStarted(aConnector);
				} catch (Exception ex) {
					log.error("Connector started (1): " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
				}
			}
		} catch (Exception ex) {
			log.error("Connector started (2): " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
		}
	}

	/**
	 *
	 * @param aConnector
	 * @return
	 */
	public PlugInResponse processPacket(PlugInResponse aResponse, IWebSocketConnector aConnector, IDataPacket aDataPacket) {
		log.debug("Processing packet for plug-ins...");
		PlugInResponse lPluginResponse = new PlugInResponse();
		for (IPlugIn plugIn : getPlugIns()) {
			try {
				// log.debug("Processing packet for plug-in " + plugIn + "...");
				plugIn.processPacket(lPluginResponse, aConnector, aDataPacket);
			} catch (Exception ex) {
				log.error("Processing packet: " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
			}
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
		log.debug("Notifying plug-ins that connector stopped...");
		for (IPlugIn plugIn : getPlugIns()) {
			try {
				// log.debug("Notifying plug-in " + plugIn + " that connector stopped...");
				plugIn.connectorStopped(aConnector);
			} catch (Exception ex) {
				log.error("Connector stopped: " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
			}
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
