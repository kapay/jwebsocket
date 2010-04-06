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

import java.util.Collections;
import java.util.Map;
import javolution.util.FastMap;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketPaket;
import org.jwebsocket.api.WebSocketServer;
import org.jwebsocket.connectors.BaseConnector;
import org.jwebsocket.kit.BroadcastOptions;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.WebSocketException;

/**
 * The implementation of the basic websocket server. A server is the central
 * instance which either processes incoming data from the engines directly or
 * routes it to the chain of plug-ins. Each server maintains a map of underlying
 * engines. An application can instantiate multiple servers to process different
 * kinds of data packets.
 * @author aschulze
 */
public class BaseServer implements WebSocketServer {

	private FastMap<String, WebSocketEngine> engines = null;
	private String id = null;

	/**
	 * Create a new instance of the Base Server. Each BaseServer maintains a
	 * map of all its underlying engines. Each Server has an Id whioch can be
	 * used to easily address a certain server.
	 * @param aId Id for the new server.
	 */
	public BaseServer(String aId) {
		id = aId;
		engines = new FastMap<String, WebSocketEngine>();
	}

	@Override
	/**
	 * {@inheritDoc }
	 */
	public void addEngine(WebSocketEngine aEngine) {
		engines.put(aEngine.getId(), aEngine);
		aEngine.addServer(this);
	}

	@Override
	/**
	 * {@inheritDoc }
	 */
	public void removeEngine(WebSocketEngine aEngine) {
		engines.remove(aEngine.getId());
		aEngine.removeServer(this);
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void startServer()
			throws WebSocketException {
		// this method is supposed to be overwritten by descending classes.
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public boolean isAlive() {
		// this method is supposed to be overwritten by descending classes.
		return false;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void stopServer()
			throws WebSocketException {
		// this method is supposed to be overwritten by descending classes.
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void engineStarted(WebSocketEngine aEngine) {
		// this method is supposed to be overwritten by descending classes.
		// e.g. to notify the overlying appplications or plug-ins
		// about the engineStarted event
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void engineStopped(WebSocketEngine aEngine) {
		// this method is supposed to be overwritten by descending classes.
		// e.g. to notify the overlying appplications or plug-ins
		// about the engineStopped event
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void connectorStarted(WebSocketConnector aConnector) {
		// this method is supposed to be overwritten by descending classes.
		// e.g. to notify the overlying appplications or plug-ins
		// about the connectorStarted event
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
		// this method is supposed to be overwritten by descending classes.
		// e.g. to notify the overlying appplications or plug-ins
		// about the connectorStopped event
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void processPacket(WebSocketEngine aEngine, WebSocketConnector aConnector, WebSocketPaket aDataPacket) {
		// this method is supposed to be overwritten by descending classes.
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void sendPacket(WebSocketConnector aConnector, WebSocketPaket aDataPacket) {
		// send a data packet to the passed connector
		aConnector.sendPacket(aDataPacket);
	}

	/**
	 * {@inheritDoc }
	 */
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
	 * returns the map of all underlying engines. Each engine has its own unique
	 * id which is used as key in the map.
	 * @return map with the underlying engines.
	 */
	public Map<String, WebSocketEngine> getEngines() {
		return (engines != null ? Collections.unmodifiableMap(engines) : null);
	}

	/**
	 * returns all connectors of the passed engine as a map. Each connector has
	 * its own unique id which is used as key in the connectors map.
	 * @param aEngine
	 * @return the engines
	 */
	public Map<String, WebSocketConnector> getConnectors(WebSocketEngine aEngine) {
		return Collections.unmodifiableMap(aEngine.getConnectors());
	}

	/**
	 * returns all connectors of all engines connected to the server. Each
	 * connector has its own unique id which is used as key in the connectors
	 * map.
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
	 * The search criteria is passed as a map with key/value pairs. The key
	 * represents the name of the shared custom variable for the connector and
	 * the value the value for that variable. If multiple key/value pairs are
	 * passed they are combined by a logical 'and'.
	 * Each connector has its own unique id which is used as key in the
	 * connectors map.
	 * @param aFilter Map of key/values pairs as search criteria.
	 * @return map with the selected connector or empty map if no connector matches the search criteria.
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
	 * Returns the connector identified by it's connector-id or <tt>null</tt>
	 * if no connector with that id could be found. This method iterates
	 * through all embedded engines.
	 * @param aId id of the connector to be returned.
	 * @return WebSocketConnector with the given id or <tt>null</tt> if not found.
	 */
	public WebSocketConnector getConnector(String aId) {
		for (WebSocketEngine lEngine : engines.values()) {
			WebSocketConnector lConnector = lEngine.getConnectors().get(aId);
			if (lConnector != null) {
				return lConnector;
			}
		}
		return null;
	}

	/**
	 * Returns the connector identified by it's connector-id or <tt>null</tt> if
	 * no connector with that id could be found. Only the connectors of the
	 * engine identified by the passed engine are considered. If not engine
	 * with that id could be found <tt>null</tt> is returned.
	 * @param aEngine id of the engine of the connector.
	 * @param aId id of the connector to be returned
	 * @return WebSocketConnector with the given id or <tt>null</tt> if not found.
	 */
	public WebSocketConnector getConnector(String aEngine, String aId) {
		WebSocketEngine lEngine = engines.get(aEngine);
		if (lEngine != null) {
			return lEngine.getConnectors().get(aId);
		}
		return null;
	}

	/**
	 * Returns the connector identified by it's connector-id or <tt>null</tt> if
	 * no connector with that id could be found. Only the connectors of the
	 * passed engine are considered. If no engine is passed <tt>null</tt> is
	 * returned.
	 * @param aEngine reference to the engine of the connector.
	 * @param aId id of the connector to be returned
	 * @return WebSocketConnector with the given id or <tt>null</tt> if not found.
	 */
	public WebSocketConnector getConnector(WebSocketEngine aEngine, String aId) {
		if (aEngine != null) {
			return aEngine.getConnectors().get(aId);
		}
		return null;
	}

	/**
	 * Returns the unique id of the server. Once set by the constructor the id
	 * cannot be changed anymore by the application.
	 * @return Id of this server instance.
	 */
	@Override
	public String getId() {
		return id;

	}

}
