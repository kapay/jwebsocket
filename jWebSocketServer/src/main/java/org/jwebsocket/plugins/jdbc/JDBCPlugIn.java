//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket JDBC Plug-In
//	Copyright (c) 2010 Alexander Schulze, Innotrade GmbH
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
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketServerConstants;
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
public class JDBCPlugIn extends TokenPlugIn {

	private static Logger log = Logging.getLogger(JDBCPlugIn.class);
	// if namespace changed update client plug-in accordingly!
	private static final String NS_JDBC = JWebSocketServerConstants.NS_BASE + ".plugins.jdbc";

	/**
	 *
	 */
	public JDBCPlugIn() {
		if (log.isDebugEnabled()) {
			log.debug("Instantiating jdbc plug-in...");
		}
		// specify default name space for admin plugin
		this.setNamespace(NS_JDBC);
	}

	@Override
	public void processToken(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();

		if (lType != null && (lNS == null || lNS.equals(getNamespace()))) {
			// select from database
			if (lType.equals("select")) {
				select(aConnector, aToken);
			}
		}
	}

	public JSONArray getResultColumns(ResultSet aResultSet, int aColCount) {
		// String blobStr = null;

		// TODO: should work with usual arrays!
		JSONArray lDataRow = new JSONArray();
		Object lObj = null;

		try {
			for (int i = 1; i <= aColCount; i++) {
				lObj = aResultSet.getObject(i);
				lDataRow.put(lObj);
				/*
				if (obj == null) {
				// nothing todo
				} else if (obj instanceof byte[]) {
				blobStr = new String((byte[]) obj);
				lDataRow.append(Tools.stringToJSON(blobStr));

				} else if (obj instanceof java.sql.Date) {
				lDataRow.append(
				Tools.javaSqlDateToString((java.sql.Date) obj));

				} else if (obj instanceof java.sql.Timestamp) {
				lDataRow.append(
				Tools.javaSqlTimestampToString(((java.sql.Timestamp) obj)));

				} else if (obj instanceof String) {
				lDataRow.append(Tools.stringToJSON((String) obj));

				} else {
				lDataRow.append(obj.toString());
				}

				if (i < aColCount) {
				lDataRow.append(Config.SIMPLE_RSP_FLD_SEP);
				}
				 */
			}

		} catch (Exception ex) {
			System.out.println("EXCEPTION in getResultColumns");
		}

		return lDataRow;
	}

	/**
	 * shutdown server
	 * @param aConnector
	 * @param aToken
	 */
	public void select(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		if (log.isDebugEnabled()) {
			log.debug("Processing 'select'...");
		}

		// check if user is allowed to run 'select' command
		if (!SecurityFactory.checkRight(lServer.getUsername(aConnector), NS_JDBC + ".select")) {
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

		// buld SQL string
		String lSQL =
				"select "
				+ lFields
				+ " from "
				+ lTable;
		if (lWhere != null && lWhere.length() > 0) {
			lSQL += " where " + lWhere;
		}
		if (lOrder != null && lOrder.length() > 0) {
			lSQL += " order by " + lOrder;
		}

		// instantiate response token
		Token lResponse = lServer.createResponse(aToken);
		// TODO: should work with usual arrays as well!
		// Object[] lColumns = null;
		int lRowCount = 0;
		int lColCount = 0;
		JSONArray lColumns = new JSONArray();
		JSONArray lData = new JSONArray();
		try {
			DBQueryResult lRes = DBConnectSingleton.querySQL(DBConnectSingleton.USR_SYSTEM, lSQL);

			// TODO: metadata should be optional to save bandwidth!
			// generate the meta data for the response
			lColCount = lRes.metaData.getColumnCount();
			lResponse.put("colcount", lColCount);

			for (int i = 1; i <= lColCount; i++) {
				// get name of colmuns
				String lSimpleClass = JDBCTools.extractSimpleClass(
						lRes.metaData.getColumnClassName(i));
				// convert to json type
				String lRIAType = JDBCTools.getJSONType(lSimpleClass, lRes.metaData);

				// TODO: should work with usual (sub-)tokens as well!
				JSONObject lColHeader = new JSONObject();
				lColHeader.put("name", lRes.metaData.getColumnName(i));
				lColHeader.put("jsontype", lRIAType);
				lColHeader.put("jdbctype", lRes.metaData.getColumnTypeName(i));

				lColumns.put(lColHeader);
				// lColumns[i] = lColHeader;
			}

			// generate the result data
			while (lRes.resultSet.next()) {
				lData.put(getResultColumns(lRes.resultSet, lColCount));
				lRowCount++;
			}
		} catch (Exception ex) {
			log.error(ex.getClass().getSimpleName() + " on query: " + ex.getMessage());
		}

		// complete the response token
		lResponse.put("rowcount", lRowCount);
		lResponse.put("columns", lColumns);
		lResponse.put("data", lData);

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}
}
