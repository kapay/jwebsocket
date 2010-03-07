//	---------------------------------------------------------------------------
//	jWebSocket - Base Engine Implementation
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
package org.jwebsocket.engines;

import java.util.Collections;
import java.util.List;
import javolution.util.FastList;
import org.jwebsocket.api.WebSocketPaket;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketServer;
import org.jwebsocket.config.Config;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.WebSocketException;

/**
 * Provides the basic implementation of the jWebSocket engines.
 * The {@code BaseEngine} is supposed to be used as ancestor for the
 * engine implementations like e.g. the {@code TCPEngine} or the
 * {@code NettyEngine}.
 * @author aschulze
 */
public class BaseEngine implements WebSocketEngine {

	private final List<WebSocketServer> servers = new FastList<WebSocketServer>();
	private final List<WebSocketConnector> connectors = new FastList<WebSocketConnector>();
	private int sessionTimeout = Config.DEFAULT_TIMEOUT;
	private String id = "";

	public BaseEngine(String aId) {
		id = aId;
	}

	public void startEngine() throws WebSocketException {
		// this method will be overridden by engine implementations.
		// notify server that the engine has started
		engineStarted();
	}

	public void stopEngine(CloseReason aCloseReason) throws WebSocketException {
		try {
			// stop all connectors
			for (WebSocketConnector connector : getConnectors()) {
				connector.stopConnector(aCloseReason);
			}
		} catch (Exception ex) {
			// log.info("Exception on sleep " + ex.getMessage());
		}
		// this method will be overridden by engine implementations.
		// notify server that the engine has started
		engineStopped();
	}

	public void engineStarted() {
		// notify servers that the engine has started
		for (WebSocketServer lServer : servers) {
			lServer.engineStarted(this);
		}
	}

	public void engineStopped() {
		// notify servers that the engine has stopped
		for (WebSocketServer lServer : servers) {
			lServer.engineStopped(this);
		}
	}

	public void connectorStarted(WebSocketConnector aConnector) {
		// notify servers that a connector has started
		for (WebSocketServer lServer : servers) {
			lServer.connectorStarted(aConnector);
		}
	}

	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
		// notify servers that a connector has stopped
		for (WebSocketServer lServer : servers) {
			lServer.connectorStopped(aConnector, aCloseReason);
		}
		// once a connector stopped remove it from the list of connectors
		getConnectors().remove(aConnector);
	}

	public boolean isAlive() {
		return false;
	}

	public void processPacket(WebSocketConnector aConnector, WebSocketPaket aDataPacket) {
		List<WebSocketServer> lServers = getServers();
		for (WebSocketServer lServer : lServers) {
			lServer.processPacket(this, aConnector, aDataPacket);
		}
	}

	public void sendPacket(WebSocketConnector aConnector, WebSocketPaket aDataPacket) {
		aConnector.sendPacket(aDataPacket);
	}

	public void broadcastPacket(WebSocketConnector aSource, WebSocketPaket aDataPacket) {
		for (WebSocketConnector connector : connectors) {
			connector.sendPacket(aDataPacket);
		}
	}

	public void removeConnector(WebSocketConnector aConnector) {
		connectors.remove(aConnector);
	}

	public int getSessionTimeout() {
		return sessionTimeout;
	}

	public void setSessionTimeout(int aSessionTimeout) {
		this.sessionTimeout = aSessionTimeout;
	}

	public List<WebSocketConnector> getConnectors() {
		return connectors;
	}

	public WebSocketConnector getConnectorByRemotePort(int aRemotePort) {
		for (WebSocketConnector lConnector : getConnectors()) {
			if (lConnector.getRemotePort() == aRemotePort) {
				return lConnector;
			}
		}
		return null;
	}

	public List<WebSocketServer> getServers() {
		return Collections.unmodifiableList(servers);
	}

	public void addServer(WebSocketServer aServer) {
		this.servers.add(aServer);
	}

	public void removeServer(WebSocketServer aServer) {
		this.servers.remove(aServer);
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
}
