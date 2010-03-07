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
package org.jwebsocket.plugins;

import java.util.List;

import javolution.util.FastList;

import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketPaket;
import org.jwebsocket.api.WebSocketServer;
import org.jwebsocket.kit.CloseReason;

/**
 *
 * @author aschulze
 */
public class BasePlugInChain implements PlugInChain {

	private static Logger log = Logger.getLogger(BasePlugInChain.class);
	private FastList<PlugIn> plugins = new FastList<PlugIn>();
	private WebSocketServer server = null;

	/**
	 *
	 * @param aServer
	 */
	public BasePlugInChain(WebSocketServer aServer) {
		server = aServer;
	}

	public void engineStarted(WebSocketEngine aEngine) {
	}

	public void engineStopped(WebSocketEngine aEngine) {
	}

	/**
	 *
	 * @param aConnector
	 */
	public void connectorStarted(WebSocketConnector aConnector) {
		log.debug("Notifying plug-ins that connector started...");
		try {
			for (PlugIn plugIn : getPlugIns()) {
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
	public PlugInResponse processPacket(PlugInResponse aResponse, WebSocketConnector aConnector, WebSocketPaket aDataPacket) {
		log.debug("Processing packet for plug-ins...");
		PlugInResponse lPluginResponse = new PlugInResponse();
		for (PlugIn plugIn : getPlugIns()) {
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
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
		log.debug("Notifying plug-ins that connector stopped (" + aCloseReason.name() + ")...");
		for (PlugIn plugIn : getPlugIns()) {
			try {
				// log.debug("Notifying plug-in " + plugIn + " that connector stopped...");
				plugIn.connectorStopped(aConnector, aCloseReason);
			} catch (Exception ex) {
				log.error("Connector stopped: " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
			}
		}
	}

	/**
	 *
	 * @return
	 */
	public List<PlugIn> getPlugIns() {
		return plugins;
	}

	/**
	 *
	 * @param aPlugIn
	 */
	public void addPlugIn(PlugIn aPlugIn) {
		plugins.add(aPlugIn);
		aPlugIn.setPlugInChain(this);
	}

	/**
	 *
	 * @param aPlugIn
	 */
	public void removePlugIn(PlugIn aPlugIn) {
		plugins.remove(aPlugIn);
		aPlugIn.setPlugInChain(null);
	}

	/**
	 * @return the server
	 */
	public WebSocketServer getServer() {
		return server;
	}
}