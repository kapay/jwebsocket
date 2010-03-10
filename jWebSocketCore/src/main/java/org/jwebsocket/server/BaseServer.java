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
package org.jwebsocket.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javolution.util.FastMap;

import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketPaket;
import org.jwebsocket.api.WebSocketServer;
import org.jwebsocket.kit.BroadcastOptions;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.WebSocketException;

/**
 *
 * @author aschulze
 */
public class BaseServer implements WebSocketServer {

	private FastMap<String, WebSocketEngine> engines = null;
	private String id = null;

	/**s
	 *
	 *
	 * @param aId
	 */
	public BaseServer(String aId) {
		id = aId;
		engines = new FastMap<String, WebSocketEngine>();
	}

	@Override
	public void addEngine(WebSocketEngine aEngine) {
		engines.put(aEngine.getId(), aEngine);
		aEngine.addServer(this);
	}

	@Override
	public void removeEngine(WebSocketEngine aEngine) {
		engines.remove(aEngine.getId());
		aEngine.removeServer(this);
	}

	@Override
	public void startServer()
		throws WebSocketException {
		/*
		for (WebSocketEngine lEngine : engines) {
		if( !lEngine.isAlive() ) {
		lEngine.startEngine();
		}
		}
		 */
	}

	@Override
	public boolean isAlive() {
		/*
		boolean lIsAlive = false;
		for (WebSocketEngine lEngine : engines) {
		if (!lEngine.isAlive()) {
		lIsAlive = false;
		break;
		}
		}
		return lIsAlive;
		 */
		return false;
	}

	@Override
	public void stopServer()
		throws WebSocketException {
		/*
		for (WebSocketEngine lEngine : engines) {
		if( lEngine.isAlive() ) {
		lEngine.stopEngine();
		}
		}
		 */
	}

	@Override
	public void engineStarted(WebSocketEngine aEngine) {
		// here nothing has to be done.
		// descendand classes may override this method
		// e.g. to notify the overlying appplications or plug-ins
		// about the engineStarted event
	}

	@Override
	public void engineStopped(WebSocketEngine aEngine) {
		// here nothing has to be done.
		// descendand classes may override this method
		// e.g. to notify the overlying appplications or plug-ins
		// about the engineStopped event
	}

	@Override
	public void connectorStarted(WebSocketConnector aConnector) {
		// here nothing has to be done.
		// descendand classes may override this method
		// e.g. to notify the overlying appplications or plug-ins
		// about the connectorStarted event
	}

	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
		// here nothing has to be done.
		// descendand classes may override this method
		// e.g. to notify the overlying appplications or plug-ins
		// about the connectorStopped event
	}

	@Override
	public void processPacket(WebSocketEngine aEngine, WebSocketConnector aConnector, WebSocketPaket aDataPacket) {
	}

	@Override
	public void sendPacket(WebSocketConnector aConnector, WebSocketPaket aDataPacket) {
		aConnector.sendPacket(aDataPacket);
	}

	@Override
	public void broadcastPacket(WebSocketConnector aSource, WebSocketPaket aDataPacket,
		BroadcastOptions aBroadcastOptions) {
		for (WebSocketConnector lConnector : getAllConnectors().values()) {
			if (!aSource.equals(lConnector) || aBroadcastOptions.isSenderIncluded()) {
				sendPacket(lConnector, aDataPacket);
			}
		}
	}

	/**
	 * @return the engines
	 */
	public Map<String, WebSocketEngine> getEngines() {
		return (engines != null ? Collections.unmodifiableMap(engines) : null);
	}

	/**
	 * returns all connectors of the passed engine.
	 * @param aEngine
	 * @return the engines
	 */
	public Map<String, WebSocketConnector> getConnectors(WebSocketEngine aEngine) {
		return Collections.unmodifiableMap(aEngine.getConnectors());
	}

	/**
	 * returns all connectors of all engines connected to the server.
	 * @return the engines
	 */
	public Map<String, WebSocketConnector> getAllConnectors() {
		FastMap<String, WebSocketConnector> lClients = new FastMap<String, WebSocketConnector>();
		for (WebSocketEngine lEngine : engines.values()) {
			lClients.putAll(lEngine.getConnectors());
		}
		return Collections.unmodifiableMap(lClients);
	}

	/**
	 * returns only those connectors that match the passed shared variables.
	 * @param aFilter
	 * @return
	 */
	public Map<String, WebSocketConnector> selectConnectors(Map<String, Object> aFilter) {
		Map<String, WebSocketConnector> lClients = new FastMap<String, WebSocketConnector>();
		for (WebSocketEngine lEngine : engines.values()) {
			for (WebSocketConnector lConnector : lEngine.getConnectors().values()) {
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
					lClients.put(lConnector.getId(), lConnector);
				}
			}
		}
		return Collections.unmodifiableMap(lClients);
	}

	/**
	 * Returns the unique id of the server.
	 * @return the id
	 */
	@Override
	public String getId() {
		return id;
	}
}
