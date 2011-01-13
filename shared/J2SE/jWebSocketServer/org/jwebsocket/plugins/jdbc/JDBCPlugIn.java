//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket JDBC Plug-In
//  Copyright (c) 2010 Innotrade GmbH, jWebSocket.org
//	---------------------------------------------------------------------------
//  THIS CODE IS FOR RESEARCH, EVALUATION AND TEST PURPOSES ONLY!
//  THIS CODE MAY BE SUBJECT TO CHANGES WITHOUT ANY NOTIFICATION!
//	THIS CODE IS NOT YET SECURE AND MAY NOT BE USED FOR PRODUCTION ENVIRONMENTS!
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
package org.jwebsocket.plugins.jdbc;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.security.SecurityFactory;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 * 
 * @author aschulze
 */
public class JDBCPlugIn extends TokenPlugIn {

	private static Logger mLog = Logging.getLogger(JDBCPlugIn.class);
	// if namespace changed update client plug-in accordingly!
	private static final String NS_JDBC = JWebSocketServerConstants.NS_BASE + ".plugins.jdbc";

	public JDBCPlugIn() {
		super(null);
	}

	public JDBCPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating JDBC plug-in...");
		}
		// specify default name space for admin plugin
		this.setNamespace(NS_JDBC);
	}

	@Override
	public void processToken(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();

		if (lType != null && getNamespace().equals(lNS)) {
			// select from database
			if (lType.equals("select")) {
				select(aConnector, aToken);
			} else if (lType.equals("update")) {
				// update(aConnector, aToken);
			} else if (lType.equals("delete")) {
				// delete(aConnector, aToken);
			} else if (lType.equals("insert")) {
				// insert(aConnector, aToken);
			} else if (lType.equals("startTA")) {
				// startTA(aConnector, aToken);
			} else if (lType.equals("commit")) {
				// commit(aConnector, aToken);
			} else if (lType.equals("rollback")) {
				// rollback(aConnector, aToken);
			} else if (lType.equals("execSQL")) {
				// execSQL(aConnector, aToken);
			} else if (lType.equals("querySQL")) {
				// querySQL(aConnector, aToken);
			}
		}
	}

	public List<Object> getResultColumns(ResultSet aResultSet, int aColCount) {
		// TODO: should work with usual arrays!
		List<Object> lDataRow = new FastList<Object>();
		Object lObj = null;

		try {
			for (int lColIdx = 1; lColIdx <= aColCount; lColIdx++) {
				lObj = aResultSet.getObject(lColIdx);
				lDataRow.add(lObj);
			}

		} catch (Exception lEx) {
			System.out.println("EXCEPTION in getResultColumns");
		}

		return lDataRow;
	}

	private Token mQuerySQL(String aSQL) {
		// instantiate response token
		Token lResponse = TokenFactory.createToken();
		// TODO: should work with usual arrays as well!
		// Object[] lColumns = null;
		int lRowCount = 0;
		int lColCount = 0;
		List<Map> lColumns = new FastList<Map>();
		List lData = new FastList<Map>();
		try {
			DBQueryResult lRes = DBConnectSingleton.querySQL(DBConnectSingleton.USR_SYSTEM, aSQL);

			// TODO: metadata should be optional to save bandwidth!
			// generate the meta data for the response
			lColCount = lRes.metaData.getColumnCount();
			lResponse.setInteger("colcount", lColCount);

			for (int lColIdx = 1; lColIdx <= lColCount; lColIdx++) {
				// get name of colmuns
				String lSimpleClass = JDBCTools.extractSimpleClass(lRes.metaData.getColumnClassName(lColIdx));
				// convert to json type
				String lRIAType = JDBCTools.getJSONType(lSimpleClass, lRes.metaData);

				Map lColHeader = new FastMap<String, Object>();
				lColHeader.put("name", lRes.metaData.getColumnName(lColIdx));
				lColHeader.put("jsontype", lRIAType);
				lColHeader.put("jdbctype", lRes.metaData.getColumnTypeName(lColIdx));

				lColumns.add(lColHeader);
			}

			// generate the result data
			while (lRes.resultSet.next()) {
				lData.add(getResultColumns(lRes.resultSet, lColCount));
				lRowCount++;
			}
		} catch (Exception lEx) {
			mLog.error(lEx.getClass().getSimpleName() + " on query: " + lEx.getMessage());
		}

		// complete the response token
		lResponse.setInteger("rowcount", lRowCount);
		lResponse.setList("columns", lColumns);
		lResponse.setList("data", lData);

		return lResponse;
	}

	private Token mExecSQL(String aSQL) {
		// instantiate response token
		Token lResponse = TokenFactory.createToken();

		// complete the response token
		lResponse.setInteger("rowsAffected", -1);

		return lResponse;
	}

	/**
	 * shutdown server
	 *
	 * @param aConnector
	 * @param aToken
	 */
	public void select(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'select'...");
		}

		// check if user is allowed to run 'select' command
		if (!SecurityFactory.hasRight(lServer.getUsername(aConnector), NS_JDBC + ".select")) {
			lServer.sendToken(aConnector, lServer.createAccessDenied(aToken));
			// return;
		}

		// obtain required parameters for query
		String lTable = aToken.getString("table");
		String lFields = aToken.getString("fields");
		String lOrder = aToken.getString("order");
		String lWhere = aToken.getString("where");
		String lGroup = aToken.getString("group");
		String lHaving = aToken.getString("having");

		// build SQL string
		String lSQL = 
				"select "
				+ lFields
				+ " from "
				+ lTable;

		// add where condition
		if (lWhere != null && lWhere.length() > 0) {
			lSQL += " where " + lWhere;
		}
		// add order options
		if (lOrder != null && lOrder.length() > 0) {
			lSQL += " order by " + lOrder;
		}

		Token lResponse = mQuerySQL(lSQL);

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}

	public void querySQL(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'querySQL'...");
		}

		// check if user is allowed to run 'select' command
		if (!SecurityFactory.hasRight(lServer.getUsername(aConnector), NS_JDBC + ".querySQL")) {
			lServer.sendToken(aConnector, lServer.createAccessDenied(aToken));
			// return;
		}

		// load SQL string
		String lSQL = aToken.getString("sql");

		Token lResponse = mQuerySQL(lSQL);

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}

	public void execSQL(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'execSQL'...");
		}

		// check if user is allowed to run 'select' command
		if (!SecurityFactory.hasRight(lServer.getUsername(aConnector), NS_JDBC + ".execSQL")) {
			lServer.sendToken(aConnector, lServer.createAccessDenied(aToken));
			// return;
		}

		// load SQL string
		String lSQL = aToken.getString("sql");

		Token lResponse = mExecSQL(lSQL);

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}

}
