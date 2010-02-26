/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jWebSocket.server;

import org.jWebSocket.api.IDataPacket;
import org.jWebSocket.api.IPlugIn;
import org.jWebSocket.api.IWebSocketConnector;
import org.jWebSocket.api.IWebSocketEngine;
import org.jWebSocket.api.IWebSocketServer;
import org.jWebSocket.kit.WebSocketException;
import org.jWebSocket.plugins.PlugInChain;

/**
 *
 * @author aschulze
 */
public class BaseServer implements IWebSocketServer {

	private PlugInChain plugInChain = null;
	private IWebSocketEngine engine = null;

	public BaseServer(IWebSocketEngine aEngine) {
		engine = aEngine;
		plugInChain = new PlugInChain();
	}

	public void startServer()
		throws WebSocketException {
		engine.startEngine();
	}

	public void stopServer()
		throws WebSocketException {
		engine.stopEngine();
	}

	public void engineStarted(IWebSocketEngine aEngine) {
		plugInChain.engineStarted(aEngine);
	}

	public void engineStopped(IWebSocketEngine aEngine) {
		plugInChain.engineStopped(aEngine);
	}

	public void connectorStarted(IWebSocketConnector aConnector) {
		// notify plugins that a connector has started,
		// i.e. a client was sconnected.
		if (plugInChain != null) {
			plugInChain.connectorStarted(aConnector);
		}
	}

	public void connectorStopped(IWebSocketConnector aConnector) {
		// notify plugins that a connector has stopped,
		// i.e. a client was disconnected.
		if (plugInChain != null) {
			plugInChain.connectorStopped(aConnector);
		}
	}

	public void processPacket(IWebSocketEngine aEngine, IWebSocketConnector aConnector, IDataPacket aDataPacket) {
	}

	public void sendPacket(IWebSocketConnector aConnector, IDataPacket aDataPacket) {
	}

	public void broadcastPacket(IDataPacket aDataPacket) {
	}

	public void addPlugIn(IPlugIn aPlugIn) {
		if (plugInChain != null) {
			plugInChain.add(aPlugIn);
		}
	}
}
