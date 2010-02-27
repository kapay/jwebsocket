/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jWebSocket.engines;

import java.util.Collections;
import java.util.List;
import javolution.util.FastList;
import org.jWebSocket.api.IDataPacket;
import org.jWebSocket.api.IWebSocketConnector;
import org.jWebSocket.api.IWebSocketEngine;
import org.jWebSocket.api.IWebSocketServer;
import org.jWebSocket.config.Config;
import org.jWebSocket.kit.WebSocketException;

/**
 *
 * @author aschulze
 */
public abstract class BaseEngine implements IWebSocketEngine {

	private IWebSocketServer server = null;
	private final List<IWebSocketConnector> connectors = new FastList<IWebSocketConnector>();
	private int sessionTimeout = Config.DEFAULT_TIMEOUT;

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
			for (IWebSocketConnector connector : getConnectors()) {
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
		// notify server that the engine has started
		if( server != null ) {
			server.engineStarted(this);
		}
	}

	public void engineStopped() {
		// notify server that the engine has stopped
		if( server != null ) {
			server.engineStopped(this);
		}
	}

	public void connectorStarted(IWebSocketConnector aConnector) {
		// notify server that a connector has started
		if( server != null ) {
			server.connectorStarted(aConnector);
		}
	}

	public void connectorStopped(IWebSocketConnector aConnector) {
		// notify server that a connector has stopped
		if( server != null ) {
			server.connectorStopped(aConnector);
		}
	}

	public abstract boolean isAlive();

	public abstract void processPacket(IWebSocketConnector aConnector, IDataPacket aDataPacket);

	public void sendPacket(IWebSocketConnector aConnector, IDataPacket aDataPacket) {
		aConnector.sendPacket(aDataPacket);
	};

	public void broadcastPacket(IDataPacket aDataPacket) {
		for (IWebSocketConnector connector : connectors) {
			connector.sendPacket(aDataPacket);
		}
	}

	public void removeConnector(IWebSocketConnector aConnector) {
		connectors.remove(aConnector);
	}

	/**
	 * @return the sessionTimeout
	 */
	public int getSessionTimeout() {
		return sessionTimeout;
	}

	/**
	 * @param sessionTimeout the sessionTimeout to set
	 */
	public void setSessionTimeout(int aSessionTimeout) {
		this.sessionTimeout = aSessionTimeout;
	}

	/**
	 * @return the connectors
	 */
	public List<IWebSocketConnector> getConnectors() {
		return Collections.unmodifiableList(connectors);
	}

	/**
	 * @return the server
	 */
	public IWebSocketServer getServer() {
		return server;
	}

	/**
	 * @param server the server to set
	 */
	public void setServer(IWebSocketServer server) {
		this.server = server;
	}
}
