//	---------------------------------------------------------------------------
//	jWebSocket - StreamingPlugIn Plug-In
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

import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketConstants;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.PlugInResponse;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.token.Token;

/**
 *
 * @author aschulze
 */
public class StreamingPlugIn extends TokenPlugIn {

	private static Logger log = Logging.getLogger(StreamingPlugIn.class);
	private String NS_STREAMING_DEFAULT = JWebSocketConstants.NS_BASE + ".plugins.streaming";
	private TimeStream timeStream = null;

	/**
	 *
	 */
	public StreamingPlugIn() {
		// specify default name space for keep alive plugin
		this.setNamespace(NS_STREAMING_DEFAULT);
	}

	@Override
	public void connectorStarted(WebSocketConnector aConnector) {
		if (timeStream == null) {
			timeStream = new TimeStream("timeStream", getServer());
			timeStream.start();
		}
	}

	@Override
	public void processToken(PlugInResponse aAction, WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();

		String lStream;

		if (lType != null && (lNS == null || lNS.equals(getNamespace()))) {
			if (lType.equals("register")) {
				if (log.isDebugEnabled()) {
					log.debug("Processing '" + lType + "'...");
				}
				lStream = (String) aToken.get("stream");
				if (!timeStream.isConnectorRegistered(aConnector)) {
					if (log.isDebugEnabled()) {
						log.debug("Registering client at stream '" + lStream + "'...");
					}
					timeStream.registerConnector(aConnector);
				}
				// else...
				// todo: error handling
			} else if (lType.equals("unregister")) {
				if (log.isDebugEnabled()) {
					log.debug("Processing '" + lType + "'...");
				}
				lStream = (String) aToken.get("stream");
				if (timeStream.isConnectorRegistered(aConnector)) {
					if (log.isDebugEnabled()) {
						log.debug("Unregistering client from stream '" + lStream + "'...");
					}
					timeStream.unregisterConnector(aConnector);
				}
				// else...
				// TODO: implement error handling
			}
		}
	}

	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
		// if a connector terminates, unregister it from stream
		timeStream.unregisterConnector(aConnector);
	}
}
