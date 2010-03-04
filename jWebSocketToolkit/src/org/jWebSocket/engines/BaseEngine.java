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
package org.jWebSocket.engines;

import java.util.Collections;
import java.util.List;
import javolution.util.FastList;
import org.jWebSocket.api.WebSocketPaket;
import org.jWebSocket.api.WebSocketConnector;
import org.jWebSocket.api.WebSocketEngine;
import org.jWebSocket.api.WebSocketServer;
import org.jWebSocket.config.Config;
import org.jWebSocket.kit.WebSocketException;

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

	/**
	 *
	 */
	public BaseEngine() {
	}

	public void startEngine() throws WebSocketException {
		// this method will be overridden by engine implementations.
		// notify server that the engine has started
		engineStarted();
	}

	public void stopEngine() throws WebSocketException {
		try {
			// stop all connectors
			for (WebSocketConnector connector : getConnectors()) {
				connector.stopConnector();
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

	public void connectorStopped(WebSocketConnector aConnector) {
		// notify servers that a connector has stopped
		for (WebSocketServer lServer : servers) {
			lServer.connectorStopped(aConnector);
		}
	}

	public boolean isAlive() {
		return false;
	}

	public void processPacket(WebSocketConnector aConnector, WebSocketPaket aDataPacket) {
	}

	public void sendPacket(WebSocketConnector aConnector, WebSocketPaket aDataPacket) {
		aConnector.sendPacket(aDataPacket);
	}

	public void broadcastPacket(WebSocketPaket aDataPacket) {
		for (WebSocketConnector connector : connectors) {
			connector.sendPacket(aDataPacket);
		}
	}

	public void removeConnector(WebSocketConnector aConnector) {
		connectors.remove(aConnector);
	}

	/**
	 * @return the sessionTimeout
	 */
	public int getSessionTimeout() {
		return sessionTimeout;
	}

	/**
	 * @param aSessionTimeout
	 */
	public void setSessionTimeout(int aSessionTimeout) {
		this.sessionTimeout = aSessionTimeout;
	}

	/**
	 * @return the connectors
	 */
	public List<WebSocketConnector> getConnectors() {
		return connectors;
	}

	/**
	 * @return the server
	 */
	public List<WebSocketServer> getServers() {
		return Collections.unmodifiableList(servers);
	}

	/**
	 */
	public void addServer(WebSocketServer aServer) {
		this.servers.add(aServer);
	}

	/**
	 */
	public void removeServer(WebSocketServer aServer) {
		this.servers.remove(aServer);
	}
}