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
import org.jwebsocket.security.SecurityFactory;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.Token;

/**
 *
 * @author aschulze
 */
public class SamplePlugIn extends TokenPlugIn {

	private static Logger log = Logging.getLogger(SamplePlugIn.class);
	// if namespace changed update client plug-in accordingly!
	private static String NS_SAMPLE = JWebSocketConstants.NS_BASE + ".plugins.sample";
	private static String SAMPLE_VAR = NS_SAMPLE + ".started";

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
			// get the server time
			if (lType.equals("getTime")) {
				getTime(aConnector, aToken);
			}
		}
	}

	/**
	 * return the server time
	 * @param aConnector
	 * @param aToken
	 */
	public void getTime(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		if (log.isDebugEnabled()) {
			log.debug("Processing 'getTime'...");
		}

		// check if user is allowed to run 'getTime' command
		if (!SecurityFactory.checkRight(lServer.getUsername(aConnector), NS_SAMPLE + ".getTime")) {
			// if user is not granted to run this command 
			// return an access denied token to the client
			lServer.sendToken(aConnector, lServer.createAccessDenied(aToken));
			return;
		}

		// create the response token
		// this includes the unique token-id
		Token lResponse = lServer.createResponse(aToken);

		// add the time field
		lResponse.put("time", new Date().toString());
		// add the
		lResponse.put("started", aConnector.getVar(SAMPLE_VAR));

		// send the response token back to the client
		lServer.sendToken(aConnector, lResponse);
	}
}
