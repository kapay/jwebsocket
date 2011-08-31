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

import java.util.ArrayList;
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
	private Map<String, String> mListeners = new FastMap<String, String>();
	private Class JDBCTools = null;
	private TokenPlugIn mJDBCPlugIn = null;

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

	private boolean getJDBCPlugIn() {
		TokenServer lServer = getServer();
		mJDBCPlugIn = (TokenPlugIn) lServer.getPlugInById("jws.jdbc");
		try {
			JDBCTools = (Class) Tools.invoke(mJDBCPlugIn, "getJDBCTools");
			// JDBCTools.getClassLoader().loadClass(JDBCTools.getName());
			return true;
		} catch (Exception ex) {
			mLog.error("Logging plug-in requires JDBC-Plug-in.");
		}
		return false;
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

			TokenServer lServer = getServer();
			if (mJDBCPlugIn == null || JDBCTools == null) {
				getJDBCPlugIn();
			}
			if (mJDBCPlugIn == null || JDBCTools == null) {
				// send response to requester
				Token lResponse = lServer.createErrorToken(aToken, -1, "JDBC plug-in not loaded.");
				lServer.sendToken(aConnector, lResponse);
				return;
			}

			// log
			if (lType.equals("log")) {
				log(aConnector, aToken);
			} else if (lType.equals("logEvent")) {
				logEvent(aConnector, aToken);
			} else if (lType.equals("getEvents")) {
				getEvents(aConnector, aToken);
			} else if (lType.equals("subscribe")) {
				// logEvent(aConnector, aToken);
			} else if (lType.equals("unsubscribe")) {
				// logEvent(aConnector, aToken);
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

		String lTable = aToken.getString("table");
		List lFields = aToken.getList("fields");
		List lValues = aToken.getList("values");
		String lPrimaryKey = aToken.getString("primaryKey");
		String lSequence = aToken.getString("sequence");

		Integer lValue = null;
		if (lPrimaryKey != null && lSequence != null) {
			Token lGetNextSeqToken = TokenFactory.createToken(
					mJDBCPlugIn.getNamespace(), "getNextSeqVal");
			lGetNextSeqToken.setString("sequence", lSequence);
			Token lNextSeqVal = mJDBCPlugIn.invoke(aConnector, lGetNextSeqToken);

			lValue = lNextSeqVal.getInteger("value");
			if (lValue == null) {
				// take over error message
				lResponse.setInteger("code", lNextSeqVal.getInteger("code"));
				lResponse.setString("msg", lNextSeqVal.getString("msg"));
				lServer.sendToken(aConnector, lResponse);
				return;
			}
			lFields.add(lPrimaryKey);
			lValues.add(lValue);
		}

 		String lFieldsStr = null;
		String lValuesStr = null;
		/*
		try {
			List lTest = new ArrayList();
			lTest.add("test1");
			lTest.add("test2");
			String lInt = (String) Tools.invokeUnique(JDBCTools, "test", lTest);
			System.out.println("test: "+ lInt);
		} catch (Exception ex) {
			mLog.error(ex.getClass().getSimpleName() + ": Method 'test' could not be invoked: " + ex.getMessage());
		}
		 */
		try {
			lFieldsStr = (String) Tools.invokeUnique(JDBCTools, "fieldListToString", lFields);
		} catch (Exception ex) {
			// TODO: return error here
			if (mLog.isDebugEnabled()) {
				mLog.debug("Method 'fieldListToString' could not be invoked: " + ex.getMessage());
			}
		}
		try {
			lValuesStr = (String) Tools.invokeUnique(JDBCTools, "valueListToString", lValues);
		} catch (Exception ex) {
			// TODO: return error here
			if (mLog.isDebugEnabled()) {
				mLog.debug("Method 'valueListToString' could not be invoked: " + ex.getMessage());
			}
		}
		// JDBCTools.
		// String lFieldsStr = JDBCTools.fieldListToString(lFields);
		// String lValuesStr = JDBCTools.valueListToString(lValues);

		Map<String, String> lVars = new FastMap<String, String>();
		lVars.put("ip", aConnector.getRemoteHost().getHostAddress());
		lValuesStr = Tools.expandVars(lValuesStr, lVars, Tools.EXPAND_CASE_SENSITIVE);

		Token lExecToken = TokenFactory.createToken(
				mJDBCPlugIn.getNamespace(), "updateSQL");
		lExecToken.setString("sql",
				"insert into "
				+ lTable
				+ " (" + lFieldsStr + ")"
				+ " values "
				+ " (" + lValuesStr + ")");

		Token lExecResp = mJDBCPlugIn.invoke(aConnector, lExecToken);
		lResponse.setInteger("code", lExecResp.getInteger("code"));
		lResponse.setString("msg", lExecResp.getString("msg"));
		lResponse.setList("rowsAffected", lExecResp.getList("rowsAffected"));
		lResponse.setInteger("key", lValue);

		lServer.sendToken(aConnector, lResponse);
	}

	private void getEvents(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		Token lResponse = lServer.createResponse(aToken);

		if (mJDBCPlugIn == null) {
			// send response to requester
			lResponse = lServer.createErrorToken(aToken, -1, "JDBC plug-in not loaded.");
			lServer.sendToken(aConnector, lResponse);
			return;
		}

		String lTable = aToken.getString("table");
		Integer lFromKey = aToken.getInteger("fromKey");
		Integer lToKey = aToken.getInteger("toKey");
		String lPrimaryKey = aToken.getString("primaryKey");
		String lSQLString = "select * from " + lTable;
		if (lPrimaryKey != null && (lFromKey != null || lToKey != null)) {
			lSQLString += " where";
			if (lFromKey != null) {
				lSQLString += " " + lPrimaryKey + " >= " + lFromKey;
			}
			if (lFromKey != null && lToKey != null) {
				lSQLString += " and";
			}
			if (lToKey != null) {
				lSQLString += " " + lPrimaryKey + " <= " + lToKey;
			}
		}

		Token lQueryToken = TokenFactory.createToken(
				mJDBCPlugIn.getNamespace(), "querySQL");
		lQueryToken.setString("sql", lSQLString);

		Token lQueryResp = mJDBCPlugIn.invoke(aConnector, lQueryToken);
		lResponse.setList("data", lQueryResp.getList("data"));
		lServer.sendToken(aConnector, lResponse);
	}
}
