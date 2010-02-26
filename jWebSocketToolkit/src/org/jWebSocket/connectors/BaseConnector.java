package org.jWebSocket.connectors;

import org.jWebSocket.api.IDataPacket;
import org.jWebSocket.api.IWebSocketConnector;
import org.jWebSocket.api.IWebSocketEngine;

/**
 *
 * @author aschulze
 */
public class BaseConnector implements IWebSocketConnector {

	private IWebSocketEngine engine = null;

	public BaseConnector(IWebSocketEngine aEngine) {
		engine = aEngine;
	}

	public void startConnector() {
		engine.connectorStarted(this);
	}

	public void stopConnector() {
		engine.connectorStopped(this);
	}

	public void processPacket(IDataPacket aDataPacket) {
		engine.processPacket(this, aDataPacket);
	}

	public void sendPacket(IDataPacket aDataPacket) {
	}

	/**
	 * @return the engine
	 */
	public IWebSocketEngine getEngine() {
		return engine;
	}
}
