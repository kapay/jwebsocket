//	---------------------------------------------------------------------------
//	jWebSocket - Streaming Plug-In
//	Copyright (c) 2010 jWebSocket.org by Innotrade GmbH Alexander Schulze
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

import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketConstants;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.PlugInResponse;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.token.Token;

/**
 * implements the stream control plug-in to manage the various underlying
 * streams. Streams are instantiated by the application and registered at
 * the streaming plug-in. The streaming plug-in only can control streams
 * but not instantiate new streams.
 * @author aschulze
 */
public class StreamingPlugIn extends TokenPlugIn {

	private static Logger log = Logging.getLogger(StreamingPlugIn.class);
	private String NS_STREAMING_DEFAULT = JWebSocketConstants.NS_BASE + ".plugins.streaming";
	private FastMap<String, BaseStream> streams = new FastMap<String, BaseStream>();

	/**
	 * create a new instance of the streaming plug-in and set the default
	 * namespace for the plug-in.
	 */
	public StreamingPlugIn() {
		// specify default name space for streaming plugin
		this.setNamespace(NS_STREAMING_DEFAULT);
	}

	/**
	 * adds a new stream to the mapo of streams. The stream must not be null
	 * and must have a valid and unqiue id.
	 * @param aStream
	 */
	public void addStream(BaseStream aStream) {
		if (aStream != null && aStream.getStreamID() != null) {
			streams.put(aStream.getStreamID(), aStream);
		}
	}

	@Override
	public void processToken(PlugInResponse aAction, WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();

		if (lType != null && (lNS == null || lNS.equals(getNamespace()))) {
			if (lType.equals("register")) {
				registerConnector(aConnector, aToken);
			} else if (lType.equals("unregister")) {
				unregisterConnector(aConnector, aToken);
			}
		}
	}

	/**
	 * registers a connector at a certain stream.
	 * @param aConnector
	 * @param aToken
	 */
	public void registerConnector(WebSocketConnector aConnector, Token aToken) {
		if (log.isDebugEnabled()) {
			log.debug("Processing register...");
		}

		BaseStream lStream = null;
		String lStreamID = (String) aToken.get("stream");
		if (lStreamID != null) {
			lStream = streams.get(lStreamID);
		}

		if (lStream != null) {
			if (!lStream.isConnectorRegistered(aConnector)) {
				if (log.isDebugEnabled()) {
					log.debug("Registering client at stream '" + lStreamID + "'...");
				}
				lStream.registerConnector(aConnector);
			}
			// else...
			// todo: error handling
		}
		// else...
		// todo: error handling
	}

	/**
	 * registers a connector from a certain stream.
	 * @param aConnector
	 * @param aToken
	 */
	public void unregisterConnector(WebSocketConnector aConnector, Token aToken) {
		if (log.isDebugEnabled()) {
			log.debug("Processing unregister...");
		}

		BaseStream lStream = null;
		String lStreamID = (String) aToken.get("stream");
		if (lStreamID != null) {
			lStream = streams.get(lStreamID);
		}

		if (lStream != null) {
			if (lStream.isConnectorRegistered(aConnector)) {
				if (log.isDebugEnabled()) {
					log.debug("Unregistering client from stream '" + lStreamID + "'...");
				}
				lStream.unregisterConnector(aConnector);
			}
			// else...
			// todo: error handling
		}
		// else...
		// todo: error handling
	}

	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
		// if a connector terminates, unregister it from all streams.
		for (BaseStream lStream : streams.values()) {
			try {
				lStream.unregisterConnector(aConnector);
			} catch (Exception ex) {
				log.error(ex.getClass().getSimpleName() + " on stopping conncector: " + ex.getMessage());
			}
		}
	}
}
