//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket Logging Plug-In
//  Copyright (c) 2011 Innotrade GmbH, jWebSocket.org
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
package org.jwebsocket.plugins.logging;

import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketServerConstants;
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
public class LoggingPlugIn extends TokenPlugIn {

	private static Logger mLog = Logging.getLogger(LoggingPlugIn.class);
	// if namespace changed update client plug-in accordingly!
	private static final String NS_LOGGING = JWebSocketServerConstants.NS_BASE + ".plugins.logging";
	private ILogger mLogger = null;
	private static final String DEF_IMPL = "log4j";
	private String mImplementation = DEF_IMPL;

	public LoggingPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating logging plug-in...");
		}
		// specify default name space for admin plugin
		this.setNamespace(NS_LOGGING);
		mGetSettings();
	}

	private void mGetSettings() {
		mImplementation = getString("implementation", DEF_IMPL);
		mLogger = new Log4JLogger();
	}
/*
	@Override
	public void connectorStarted(WebSocketConnector aConnector) {
	}

	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseRease) {
	}
*/
	@Override
	public void processToken(PlugInResponse aResponse,
			WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();

		if (lType != null && getNamespace().equals(lNS)) {
			// log
			if (lType.equals("log")) {
				log(aConnector, aToken);
			}
		}
	}

	private void log(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		// instantiate response token
		Token lResponse = lServer.createResponse(aToken);
		String lInfo = aToken.getString("info");
		String lMessage = aToken.getString("message");
		String lLevel = aToken.getString("level");

		try {
			LogLevel lLogLevel = LogLevel.stringToLevel(lLevel);
			mLogger.log(lLogLevel, lInfo, lMessage);
		} catch (Exception lEx) {
			String lMsg = lEx.getClass().getSimpleName() + ": " + lEx.getMessage();
			mLog.error(lMsg);
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", lMsg);
		}

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}
}
