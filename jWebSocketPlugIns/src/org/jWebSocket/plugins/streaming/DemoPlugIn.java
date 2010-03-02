//	---------------------------------------------------------------------------
//	jWebSocket - Demo for a custom specific listener implementation
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

import org.apache.log4j.Logger;
import org.jWebSocket.api.IWebSocketConnector;
import org.jWebSocket.config.Config;
import org.jWebSocket.connectors.BaseConnector;
import org.jWebSocket.plugins.PlugInResponse;
import org.jWebSocket.plugins.TokenPlugIn;
import org.jWebSocket.token.Token;

/**
 * shows how custom specific listeners can be added to an application
 * Here the methods to control the timeStream are implemented
 * @author aschulze
 */
public class DemoPlugIn extends TokenPlugIn {

	private static Logger log = Logger.getLogger(DemoPlugIn.class);
	private TimeStream timeStream = null;
	private String NS_DEMO_DEFAULT = Config.NS_BASE + ".plugins.demo";

	public DemoPlugIn() {
		this.setNamespace(NS_DEMO_DEFAULT);
		timeStream = new TimeStream("timeStream");
		timeStream.start();
	}

	@Override
	public void processToken(PlugInResponse aResponse, IWebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();

		String lStream;
 		if (lType != null && (lNS == null || lNS.equals(getNamespace()))) {

			if (lType.equals("regAtStream")) {
				log.debug("Processing '" + lType + "'...");
				lStream = (String) aToken.get("stream");
				if (!timeStream.isClientRegistered(aConnector)) {
					log.debug("Registering client at stream '" + lStream + "'...");
					timeStream.registerClient(aConnector);
				}
				// else...
				// todo: error handling
			} else if (lType.equals("unregFromStream")) {
				log.debug("Processing '" + lType + "'...");
				lStream = (String) aToken.get("stream");
				if (timeStream.isClientRegistered(aConnector)) {
					log.debug("Unregistering client from stream '" + lStream + "'...");
					timeStream.unregisterClient(aConnector);
				}
				// else...
				// TODO: implement error handling
			}
		}
	}

	@Override
	public void connectorStopped(IWebSocketConnector aConnector) {
		// if a connector terminates, unregister it from stream
		timeStream.unregisterClient(aConnector);
	}
}
