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

import java.util.List;
import java.util.Map;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.plugins.jdbc.JDBCTools;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.util.Tools;

/**
 *
 * @author aschulze
 */
public class LoggingPlugIn extends TokenPlugIn {

	private static Logger mLog = Logging.getLogger(LoggingPlugIn.class);
	// if namespace changed update client plug-in accordingly!
	private static final String NS_LOGGING =
			JWebSocketServerConstants.NS_BASE + ".plugins.logging";
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
			} else if (lType.equals("logEvent")) {
				logEvent(aConnector, aToken);
			} else if (lType.equals("subscribe")) {
				logEvent(aConnector, aToken);
			} else if (lType.equals("unsubscribe")) {
				logEvent(aConnector, aToken);
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

	private void registerListener(WebSocketConnector aConnector) {
	}

	private void unregisterListener(WebSocketConnector aConnector) {
	}

	private void subscribe(WebSocketConnector aConnector, Token aToken) {
	}

	private void unsubscribe(WebSocketConnector aConnector, Token aToken) {
	}

	private void logEvent(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		Token lResponse = lServer.createResponse(aToken);

		TokenPlugIn lJDBCPlugIn = (TokenPlugIn) lServer.getPlugInById("jws.jdbc");
		if (lJDBCPlugIn == null) {
			// send response to requester
			lResponse = lServer.createErrorToken(aToken, -1, "JDBC plug-in not loaded.");
			lServer.sendToken(aConnector, lResponse);
			return;
		}

		String lTable = aToken.getString("table");
		List lFields = aToken.getList("fields");
		List lValues = aToken.getList("values");
		String lPrimaryKey = aToken.getString("primaryKey");
		String lSequence = aToken.getString("sequence");

		Token lExecToken = TokenFactory.createToken(
				lJDBCPlugIn.getNamespace(), "exec");

		Integer lValue = null;
		if (lPrimaryKey != null && lSequence != null) {
			Token lGetNextSeqToken = TokenFactory.createToken(
				lJDBCPlugIn.getNamespace(), "getNextSeqVal");
			lGetNextSeqToken.setString("sequence", lSequence);
			Token lNextSeqVal = lJDBCPlugIn.invoke(aConnector, lGetNextSeqToken);
			
			lValue = lNextSeqVal.getInteger("value");
			if( lValue == null ) {
				// take over error message
				lResponse.setInteger("code", lNextSeqVal.getInteger("code"));
				lResponse.setString("msg", lNextSeqVal.getString("msg"));
				lServer.sendToken(aConnector, lResponse);
				return;
			}
			lFields.add(lPrimaryKey);
			lValues.add(lValue);
		}
		
		String lFieldsStr = JDBCTools.fieldListToString(lFields);
		String lValuesStr = JDBCTools.valueListToString(lValues);

		Map<String, String> lVars = new FastMap<String, String>();
		lVars.put("ip", aConnector.getRemoteHost().getHostAddress());
		lValuesStr = Tools.expandVars(lValuesStr, lVars, Tools.EXPAND_CASE_SENSITIVE);
		
		lExecToken.setString("sql",
				"insert into "
				+ lTable
				+ " (" + lFieldsStr + ")"
				+ " values "
				+ " (" + lValuesStr + ")");
		
		Token lExecResp = lJDBCPlugIn.invoke(aConnector, lExecToken);
		lResponse.setInteger("code", lExecResp.getInteger("code"));
		lResponse.setInteger("msg", lExecResp.getInteger("msg"));
		lResponse.setInteger("rowsAffected", lExecResp.getInteger("rowsAffected"));
		lResponse.setInteger("key", lValue);
		lServer.sendToken(aConnector, lResponse);
	}
}
