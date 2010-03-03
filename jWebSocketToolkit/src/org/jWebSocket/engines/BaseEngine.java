/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jWebSocket.engines;

import java.util.Collections;
import java.util.List;
import javolution.util.FastList;
import org.jWebSocket.api.IDataPacket;
import org.jWebSocket.api.WebSocketConnector;
import org.jWebSocket.api.WebSocketEngine;
import org.jWebSocket.api.WebSocketServer;
import org.jWebSocket.config.Config;
import org.jWebSocket.kit.WebSocketException;

/**
 *
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

	public void processPacket(WebSocketConnector aConnector, IDataPacket aDataPacket) {
	}

	public void sendPacket(WebSocketConnector aConnector, IDataPacket aDataPacket) {
		aConnector.sendPacket(aDataPacket);
	}

	public void broadcastPacket(IDataPacket aDataPacket) {
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
