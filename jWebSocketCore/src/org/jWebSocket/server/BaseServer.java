//	---------------------------------------------------------------------------
//	jWebSocket - Basic server (dispatcher)
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
package org.jWebSocket.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javolution.util.FastList;
import org.jWebSocket.api.IDataPacket;
import org.jWebSocket.api.WebSocketConnector;
import org.jWebSocket.api.WebSocketEngine;
import org.jWebSocket.api.WebSocketServer;
import org.jWebSocket.kit.WebSocketException;

/**
 *
 * @author aschulze
 */
public class BaseServer implements WebSocketServer {

	private FastList<WebSocketEngine> engines = null;

	/**
	 *
	 */
	public BaseServer() {
		engines = new FastList<WebSocketEngine>();
	}

	public void addEngine(WebSocketEngine aEngine) {
		engines.add(aEngine);
		aEngine.addServer(this);
	}

	public void removeEngine(WebSocketEngine aEngine) {
		engines.remove(aEngine);
		aEngine.removeServer(this);

	}

	public void startServer()
		throws WebSocketException {
		for (WebSocketEngine lEngine : engines) {
			lEngine.startEngine();
		}
	}

	public boolean isAlive() {
		boolean lIsAlive = true;
		for (WebSocketEngine lEngine : engines) {
			if (!lEngine.isAlive()) {
				lIsAlive = false;
				break;
			}
		}
		return lIsAlive;
	}

	public void stopServer()
		throws WebSocketException {
		for (WebSocketEngine lEngine : engines) {
			lEngine.stopEngine();
		}
	}

	public void engineStarted(WebSocketEngine aEngine) {
	}

	public void engineStopped(WebSocketEngine aEngine) {
	}

	public void connectorStarted(WebSocketConnector aConnector) {
	}

	public void connectorStopped(WebSocketConnector aConnector) {
	}

	public void processPacket(WebSocketEngine aEngine, WebSocketConnector aConnector, IDataPacket aDataPacket) {
	}

	public void sendPacket(WebSocketConnector aConnector, IDataPacket aDataPacket) {
		aConnector.sendPacket(aDataPacket);
	}

	public void broadcastPacket(IDataPacket aDataPacket) {
		for (WebSocketConnector lConnector : getAllConnectors()) {
			sendPacket(lConnector, aDataPacket);
		}
	}

	/**
	 * @return the engines
	 */
	public List<WebSocketEngine> getEngines() {
		return (engines != null ? Collections.unmodifiableList(engines) : null);
	}

	/**
	 * returns all connectors of the passed engine.
	 * @param aEngine
	 * @return the engines
	 */
	public List<WebSocketConnector> getConnectors(WebSocketEngine aEngine) {
		return Collections.unmodifiableList(aEngine.getConnectors());
	}

	/**
	 * returns all connectors of all engines connected to the server.
	 * @return the engines
	 */
	public List<WebSocketConnector> getAllConnectors() {
		ArrayList clients = new ArrayList();
		for (WebSocketEngine lEngine : engines) {
			clients.addAll(lEngine.getConnectors());
		}
		return Collections.unmodifiableList(clients);
	}

	/**
	 * returns only those connectors that match the passed shared variables.
	 * @param aFilter
	 * @return
	 */
	public List<WebSocketConnector> selectConnectors(Map<String, Object> aFilter) {
		ArrayList clients = new ArrayList();
		for (WebSocketEngine lEngine : engines) {
			for (WebSocketConnector lConnector : lEngine.getConnectors()) {
				boolean lMatch = true;
				for (String lKey : aFilter.keySet()) {
					Object lVarVal = lConnector.getVar(lKey);
					lMatch = (lVarVal != null);
					if (lMatch) {
						Object lFilterVal = aFilter.get(lKey);
						if (lVarVal instanceof String && lFilterVal instanceof String) {
							lMatch = ((String) lVarVal).matches((String) lFilterVal);
						} else if (lVarVal instanceof Boolean) {
							lMatch = ((Boolean) lVarVal).equals((Boolean) lFilterVal);
						} else {
							lMatch = lVarVal.equals(lFilterVal);
						}
						if (!lMatch) {
							break;
						}
					}
				}
				if (lMatch) {
					clients.add(lConnector);
				}
			}
		}
		return Collections.unmodifiableList(clients);
	}
}
