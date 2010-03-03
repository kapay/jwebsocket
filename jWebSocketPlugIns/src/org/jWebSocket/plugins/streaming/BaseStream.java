//	---------------------------------------------------------------------------
//	jWebSocket - In- and Outbound Stream
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
package org.jWebSocket.plugins.streaming;

import javolution.util.FastList;
import org.apache.log4j.Logger;
import org.jWebSocket.api.WebSocketConnector;
import org.jWebSocket.kit.DataPacket;
import org.jWebSocket.server.BaseServer;

/**
 *
 * @author aschulze
 */
public class BaseStream extends Thread {

	private static Logger log = Logger.getLogger(BaseStream.class);
	private FastList<WebSocketConnector> connectors = new FastList<WebSocketConnector>();
	private boolean isRunning = false;
	private String streamID = null;
	/**
	 *
	 */
	public final FastList<Object> queue = new FastList<Object>();

	/**
	 *
	 *
	 * @param aStreamID
	 */
	public BaseStream(String aStreamID) {
		this.streamID = aStreamID;
	}

	/**
	 *
	 *
	 * @param aConnector
	 */
	public void registerConnector(WebSocketConnector aConnector) {
		connectors.add(aConnector);
	}

	/**
	 *
	 *
	 * @param aConnector
	 * @return
	 */
	public boolean isConnectorRegistered(WebSocketConnector aConnector) {
		return connectors.indexOf(aConnector) >= 0;
	}

	/**
	 *
	 *
	 * @param aConnector
	 */
	public void unregisterConnector(WebSocketConnector aConnector) {
		connectors.remove(aConnector);
	}

	/**
	 *
	 *
	 * @param aServer
	 */
	public void registerAllConnectors(BaseServer aServer) {
		// clients.add(aServer);
	}

	/**
	 *
	 *
	 * @param aServer
	 */
	public void unregisterAllConnectors(BaseServer aServer) {
		// clients.remove(aServer);
	}

	/**
	 * puts a data packet into the stream queue.
	 * @param aObject
	 */
	public void put(Object aObject) {
		synchronized (queue) {
			// add the queue item into the queue
			queue.add(aObject);
			// trigger sender thread
			queue.notify();
		}
	}

	/**
	 *
	 * @param aConnector
	 * @param aObject
	 */
	protected void processConnector(WebSocketConnector aConnector, Object aObject) {
		try {
			aConnector.sendPacket(new DataPacket(aObject.toString()));
		} catch (Exception ex) {
			log.error("Exception: " + ex.getMessage());
		}
	}

	/**
	 * iterates through all registered connectors and 
	 * runs processConnector for each.
	 * @param aObject
	 */
	protected void processItem(Object aObject) {
		for (WebSocketConnector lConnector : connectors) {
			processConnector(lConnector, aObject);
		}
	}

	@Override
	public void run() {
		isRunning = true;
		while (isRunning) {
			synchronized (queue) {
				if (queue.size() > 0) {
					Object lObject = queue.remove(0);
					processItem(lObject);
				} else {
					try {
						queue.wait();
					} catch (InterruptedException ex) {
						log.error("Exception:" + ex.getMessage());
					}
				}
			}
		}
	}

	/**
	 * @return the streamID
	 */
	public String getStreamID() {
		return streamID;
	}
}
