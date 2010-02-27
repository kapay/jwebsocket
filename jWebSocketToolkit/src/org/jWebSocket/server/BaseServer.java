/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jWebSocket.server;

import java.util.Iterator;
import javolution.util.FastList;
import org.jWebSocket.api.IDataPacket;
import org.jWebSocket.api.IWebSocketConnector;
import org.jWebSocket.api.IWebSocketEngine;
import org.jWebSocket.api.IWebSocketServer;
import org.jWebSocket.kit.WebSocketException;

/**
 *
 * @author aschulze
 */
public class BaseServer implements IWebSocketServer {

	private FastList<IWebSocketEngine> engines = null;

	public void addEngine(IWebSocketEngine aEngine) {
		engines.add(aEngine);
	}

	public void removeEngine(IWebSocketEngine aEngine) {
		engines.remove(aEngine);
	}

	public void startServer()
			throws WebSocketException {
		for (Iterator<IWebSocketEngine> i = engines.iterator(); i.hasNext();) {
			i.next().startEngine();
		}
	}

	public boolean isAlive() {
		boolean lIsAlive = false;
		for (Iterator<IWebSocketEngine> i = engines.iterator(); !lIsAlive && i.hasNext();) {
			lIsAlive = i.next().isAlive();
		}
		return lIsAlive;
	}

	public void stopServer()
			throws WebSocketException {
		for (Iterator<IWebSocketEngine> i = engines.iterator(); i.hasNext();) {
			i.next().stopEngine();
		}
	}

	public void engineStarted(IWebSocketEngine aEngine) {
	}

	public void engineStopped(IWebSocketEngine aEngine) {
	}

	public void connectorStarted(IWebSocketConnector aConnector) {
	}

	public void connectorStopped(IWebSocketConnector aConnector) {
	}

	public void processPacket(IWebSocketEngine aEngine, IWebSocketConnector aConnector, IDataPacket aDataPacket) {
	}

	public void sendPacket(IWebSocketConnector aConnector, IDataPacket aDataPacket) {
	}

	public void broadcastPacket(IDataPacket aDataPacket) {
	}
}
