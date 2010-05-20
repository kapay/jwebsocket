//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket Sample Plug-In
//	Copyright (c) 2010 jWebSocket.org, Alexander Schulze, Innotrade GmbH
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
package org.jwebsocket.plugins.sample;

import java.util.Date;
import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.config.JWebSocketConstants;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.Token;

/**
 *
 * @author aschulze
 */
public class SamplePlugIn extends TokenPlugIn {

	private static Logger log = Logging.getLogger(SamplePlugIn.class);
	// if namespace changed update client plug-in accordingly!
	private static String NS_SAMPLE = JWebSocketConstants.NS_BASE + ".plugins.samples";
	private static String SAMPLE_VAR = NS_SAMPLE + ".started";

	/**
	 *
	 */
	public SamplePlugIn() {
		if (log.isDebugEnabled()) {
			log.debug("Instantiating sample plug-in...");
		}
		// specify default name space for sample plugin
		this.setNamespace(NS_SAMPLE);
	}

	@Override
	public void connectorStarted(WebSocketConnector aConnector) {
		// this method is called every time when a client
		// connected to the server
		aConnector.setVar(SAMPLE_VAR, new Date().toString());
	}

	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
		// this method is called every time when a client
		// disconnected from the server
	}

	@Override
	public void engineStarted(WebSocketEngine aEngine) {
		// this method is called when the engine has started
		super.engineStarted(aEngine);
	}

	@Override
	public void engineStopped(WebSocketEngine aEngine) {
		// this method is called when the engine has stopped
		super.engineStopped(aEngine);
	}

	@Override
	public void processToken(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {

		// get the type of the token
		// the type can be associated with a "command"
		String lType = aToken.getType();

		// get the namespace of the token
		// each plug-in should have its own unique namespace
		String lNS = aToken.getNS();

		// check if token has a type and a matching namespace
		if (lType != null && lNS != null && lNS.equals(getNamespace())) {

			TokenServer lServer = getServer();

			// get the server time
			if (lType.equals("requestServerTime")) {
				// create the response token
				// this includes the unique token-id
				Token lResponse = lServer.createResponse(aToken);

				// add the "time" and "started" field
				lResponse.put("time", new Date().toString());
				lResponse.put("started", aConnector.getVar(SAMPLE_VAR));

				// send the response token back to the client
				lServer.sendToken(aConnector, lResponse);

			} else if (lType.equals("processComplexObject")) {
				// simply echo the complex object
				lServer.sendToken(aConnector, aToken);

			}
		}
	}
}
