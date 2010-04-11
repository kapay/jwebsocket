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
package org.jwebsocket.plugins.streaming;

import javolution.util.FastList;

import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.kit.RawPacket;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.server.BaseServer;

/**
 * implements a stream on which connectors can be registered and unregistered.
 * The fundamental streaming capabilities are provided by the <tt>BaseStream</tt>
 * class. The <tt>BaseStream</tt> implements an internal queue to which messages
 * can be posted. The message then are broadcasted to the registered clients.
 * Therefore the <tt>BaseStream</tt> class maintains a list of clients. A certain
 * client can register at or unregister from the stream. Basically streams send
 * their messages only to clients that are registered at a stream.
 * @author aschulze
 */
public class BaseStream {

	private static Logger log = Logging.getLogger(BaseStream.class);
	private FastList<WebSocketConnector> connectors = new FastList<WebSocketConnector>();
	private boolean isRunning = false;
	private String streamID = null;
	private final FastList<Object> queue = new FastList<Object>();
	private Thread queueThread = null;

	/**
	 * creates a new stream with a certain id.
	 * @param aStreamID
	 */
	public BaseStream(String aStreamID) {
		this.streamID = aStreamID;
		QueueProcessor queueProcessor = new QueueProcessor();
		queueThread = new Thread(queueProcessor);
		queueThread.start();
	}

	/**
	 * registers a connector at the stream. After this operation the stream
	 * will send new messages to this client as well.
	 * @param aConnector
	 */
	public void registerConnector(WebSocketConnector aConnector) {
		if (aConnector != null) {
			connectors.add(aConnector);
		}
	}

	/**
	 * checks if a certain connector is registered at the stream.
	 * @param aConnector
	 * @return <tt>true</tt> if the connector is already registered otherwise <tt>false</tt>.
	 */
	public boolean isConnectorRegistered(WebSocketConnector aConnector) {
		return (aConnector != null && connectors.indexOf(aConnector) >= 0);
	}

	/**
	 * unregisters a connector from the stream. After this operation the stream
	 * will no longer new messages to this client.
	 * @param aConnector
	 */
	public void unregisterConnector(WebSocketConnector aConnector) {
		if (aConnector != null) {
			connectors.remove(aConnector);
		}
	}

	/**
	 * registers all connectors of the given server at the stream. After this
	 * operation the stream will send new messages to all clients on the
	 * given server.
	 * @param aServer
	 */
	public void registerAllConnectors(BaseServer aServer) {
		// TODO: to be implemented!
	}

	/**
	 * unregisters all connectors of the given server from the stream. After
	 * this operation the stream will no longer send new messages to any
	 * clients on the given server.
	 * @param aServer
	 */
	public void unregisterAllConnectors(BaseServer aServer) {
		// TODO: to be implemented!
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
	 * sends a message from the queue to a certain connector.
	 * @param aConnector
	 * @param aObject
	 */
	protected void processConnector(WebSocketConnector aConnector, Object aObject) {
		try {
			aConnector.sendPacket(new RawPacket(aObject.toString()));
		} catch (Exception ex) {
			log.error(ex.getClass().getSimpleName() + ": " + ex.getMessage());
		}
	}

	/**
	 * iterates through all registered connectors and runs 
	 * <tt>processConnector</tt> for each.
	 * @param aObject
	 */
	protected void processItem(Object aObject) {
		for (WebSocketConnector lConnector : connectors) {
			processConnector(lConnector, aObject);
		}
	}

	private class QueueProcessor implements Runnable {

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
							log.error(ex.getClass().getSimpleName() + ": " + ex.getMessage());
						}
					}
				}
			}

		}
	}

	/**
	 * returns the id of the stream.
	 * @return the streamID
	 */
	public String getStreamID() {
		return streamID;
	}
}
