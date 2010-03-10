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
import java.util.Map;
import javolution.util.FastMap;
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

	private final Map<String, WebSocketServer> servers = new FastMap<String, WebSocketServer>();
	private final Map<String, WebSocketConnector> connectors = new FastMap<String, WebSocketConnector>();
	private int sessionTimeout = Config.DEFAULT_TIMEOUT;
	private String id = "";

	/**
	 *
	 * @param aId
	 */
	public BaseEngine(String aId) {
		id = aId;
	}

	@Override
	public void startEngine() throws WebSocketException {
		// this method will be overridden by engine implementations.
		// notify server that the engine has started
		engineStarted();
	}

	@Override
	public void stopEngine(CloseReason aCloseReason) throws WebSocketException {
		try {
			// stop all connectors
			for (WebSocketConnector connector : getConnectors().values()) {
				connector.stopConnector(aCloseReason);
			}
		} catch (Exception ex) {
			// log.info("Exception on sleep " + ex.getMessage());
		}
		// this method will be overridden by engine implementations.
		// notify server that the engine has started
		engineStopped();
	}

	@Override
	public void engineStarted() {
		// notify servers that the engine has started
		for (WebSocketServer lServer : servers.values()) {
			lServer.engineStarted(this);
		}
	}

	@Override
	public void engineStopped() {
		// notify servers that the engine has stopped
		for (WebSocketServer lServer : servers.values()) {
			lServer.engineStopped(this);
		}
	}

	@Override
	public void connectorStarted(WebSocketConnector aConnector) {
		// notify servers that a connector has started
		for (WebSocketServer lServer : servers.values()) {
			lServer.connectorStarted(aConnector);
		}
	}

	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
		// notify servers that a connector has stopped
		for (WebSocketServer lServer : servers.values()) {
			lServer.connectorStopped(aConnector, aCloseReason);
		}
		// once a connector stopped remove it from the list of connectors
		getConnectors().remove(aConnector.getId());
	}

	@Override
	public boolean isAlive() {
		return false;
	}

	@Override
	public void processPacket(WebSocketConnector aConnector, WebSocketPaket aDataPacket) {
		Map<String, WebSocketServer> lServers = getServers();
		for (WebSocketServer lServer : lServers.values()) {
			lServer.processPacket(this, aConnector, aDataPacket);
		}
	}

	@Override
	public void sendPacket(WebSocketConnector aConnector, WebSocketPaket aDataPacket) {
		aConnector.sendPacket(aDataPacket);
	}

	@Override
	public void broadcastPacket(WebSocketConnector aSource, WebSocketPaket aDataPacket) {
		for (WebSocketConnector connector : connectors.values()) {
			connector.sendPacket(aDataPacket);
		}
	}

	@Override
	public void removeConnector(WebSocketConnector aConnector) {
		connectors.remove(aConnector.getId());
	}

	@Override
	public int getSessionTimeout() {
		return sessionTimeout;
	}

	@Override
	public void setSessionTimeout(int aSessionTimeout) {
		this.sessionTimeout = aSessionTimeout;
	}

	@Override
	public Map<String, WebSocketConnector> getConnectors() {
		return connectors;
	}

	@Override
	public WebSocketConnector getConnectorByRemotePort(int aRemotePort) {
		for (WebSocketConnector lConnector : getConnectors().values()) {
			if (lConnector.getRemotePort() == aRemotePort) {
				return lConnector;
			}
		}
		return null;
	}

	@Override
	public Map<String, WebSocketServer> getServers() {
		return Collections.unmodifiableMap(servers);
	}

	@Override
	public void addServer(WebSocketServer aServer) {
		this.servers.put(aServer.getId(), aServer);
	}

	@Override
	public void removeServer(WebSocketServer aServer) {
		this.servers.remove(aServer.getId());
	}

	/**
	 * @return the id
	 */
	@Override
	public String getId() {
		return id;
	}
}
