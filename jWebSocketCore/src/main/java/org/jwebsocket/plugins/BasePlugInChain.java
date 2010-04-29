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

import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.api.WebSocketPlugIn;
import org.jwebsocket.api.WebSocketPlugInChain;
import java.util.List;

import javolution.util.FastList;

import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketPaket;
import org.jwebsocket.api.WebSocketServer;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.logging.Logging;

/**
 * Implements the basic chain of plug-ins which is triggered by a server
 * when data packets are received. Each data packet is pushed through the chain
 * and can be processed by the plug-ins.
 * @author aschulze
 */
public class BasePlugInChain implements WebSocketPlugInChain {

	private static Logger log = Logging.getLogger(BasePlugInChain.class);
	private FastList<WebSocketPlugIn> plugins = new FastList<WebSocketPlugIn>();
	private WebSocketServer server = null;

	/**
	 *
	 * @param aServer
	 */
	public BasePlugInChain(WebSocketServer aServer) {
		server = aServer;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void engineStarted(WebSocketEngine aEngine) {
		if (log.isDebugEnabled()) {
			log.debug("Notifying plug-ins that engine started...");
		}
		try {
			for (WebSocketPlugIn plugIn : getPlugIns()) {
				try {
					plugIn.engineStarted(aEngine);
				} catch (Exception ex) {
					log.error("Engine started (1): " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
				}
			}
		} catch (Exception ex) {
			log.error("Engine started (2): " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void engineStopped(WebSocketEngine aEngine) {
		if (log.isDebugEnabled()) {
			log.debug("Notifying plug-ins that engine stopped...");
		}
		try {
			for (WebSocketPlugIn plugIn : getPlugIns()) {
				try {
					plugIn.engineStopped(aEngine);
				} catch (Exception ex) {
					log.error("Engine stopped (1): " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
				}
			}
		} catch (Exception ex) {
			log.error("Engine stopped (2): " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
		}
	}

	/**
	 * @param aConnector
	 */
	@Override
	public void connectorStarted(WebSocketConnector aConnector) {
		if (log.isDebugEnabled()) {
			log.debug("Notifying plug-ins that connector started...");
		}
		try {
			for (WebSocketPlugIn plugIn : getPlugIns()) {
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
	@Override
	public PlugInResponse processPacket(WebSocketConnector aConnector, WebSocketPaket aDataPacket) {
		if (log.isDebugEnabled()) {
			log.debug("Processing packet for plug-ins...");
		}
		PlugInResponse lPluginResponse = new PlugInResponse();
		for (WebSocketPlugIn plugIn : getPlugIns()) {
			try {
				/*
				if (log.isDebugEnabled()) {
				log.debug("Processing packet for plug-in " + plugIn + "...");
				}
				 */
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
	 * @param aCloseReason
	 */
	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
		if (log.isDebugEnabled()) {
			log.debug("Notifying plug-ins that connector stopped (" + aCloseReason.name() + ")...");
		}
		for (WebSocketPlugIn plugIn : getPlugIns()) {
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
	@Override
	public List<WebSocketPlugIn> getPlugIns() {
		return plugins;
	}

	/**
	 *
	 * @param aPlugIn
	 */
	@Override
	public void addPlugIn(WebSocketPlugIn aPlugIn) {
		plugins.add(aPlugIn);
		aPlugIn.setPlugInChain(this);
	}

	/**
	 *
	 * @param aPlugIn
	 */
	@Override
	public void removePlugIn(WebSocketPlugIn aPlugIn) {
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
