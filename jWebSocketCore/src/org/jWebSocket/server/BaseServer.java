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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

	/**
	 *
	 */
	public BaseServer() {
		engines = new FastList<IWebSocketEngine>();
	}

	public void addEngine(IWebSocketEngine aEngine) {
		engines.add(aEngine);
		aEngine.setServer(this);
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
		aConnector.sendPacket(aDataPacket);
	}

	public void broadcastPacket(IDataPacket aDataPacket) {
		for (Iterator<IWebSocketConnector> i = getAllConnectors().iterator(); i.hasNext();) {
			sendPacket(i.next(), aDataPacket);
		}
	}

	/**
	 * @return the engines
	 */
	public List<IWebSocketEngine> getEngines() {
		return (engines != null ? Collections.unmodifiableList(engines) : null);
	}

	/**
	 * returns all connectors of the passed engine.
	 * @param aEngine
	 * @return the engines
	 */
	public List<IWebSocketConnector> getConnectors(IWebSocketEngine aEngine) {
		return Collections.unmodifiableList(aEngine.getConnectors());
	}

	/**
	 * returns all connectors of all engines connected to the server.
	 * @return the engines
	 */
	public List<IWebSocketConnector> getAllConnectors() {
		ArrayList clients = new ArrayList();
		for (Iterator i = engines.iterator(); i.hasNext();) {
			clients.addAll(((IWebSocketEngine) i.next()).getConnectors());
		}
		return Collections.unmodifiableList(clients);
	}

	/**
	 * returns only those connectors that match the passed shared variables.
	 * @param aFilter
	 * @return
	 */
	public List<IWebSocketConnector> selectConnectors(Map<String, Object> aFilter) {
		ArrayList clients = new ArrayList();
		for (IWebSocketEngine lEngine : engines) {
			for (IWebSocketConnector lConnector : lEngine.getConnectors()) {
				boolean lMatch = true;
				for (String lKey : aFilter.keySet()) {
					Object lVarVal = lConnector.getVar(lKey);
					lMatch = (lVarVal != null);
					if (lMatch) {
						Object lFilterVal = aFilter.get(lKey);
						if (lVarVal instanceof String && lFilterVal instanceof String) {
							lMatch = ((String) lVarVal).matches((String) lFilterVal);
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
