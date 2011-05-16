//	---------------------------------------------------------------------------
//	jWebSocket - JDBCTools
//	Copyright (c) 2010 Innotrade GmbH (http://jWebSocket.org)
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
//  for more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
package org.jwebsocket.plugins.jdbc;

import java.sql.ResultSetMetaData;
import java.util.List;
import java.util.Map;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;

/**
 *
 * @author aschulze
 */
public class JDBCTools {

	public static String extractSimpleClass(String aClassName) {
		if (aClassName.equals("[B")) {
			return ("Blob");
		}
		int lLastDotPos = aClassName.lastIndexOf('.');
		if (lLastDotPos >= 0) {
			aClassName = aClassName.substring(lLastDotPos + 1);
		}
		return (aClassName);
	}

	public static String getJSONType(String aJavaType, ResultSetMetaData aMetaData) {
		String lResStr = aJavaType.toLowerCase();
		if (lResStr != null) {
			if (lResStr.equals("bigdecimal")
					|| lResStr.equals("long")
					|| lResStr.equals("int")
					|| lResStr.equals("byte")
					|| lResStr.equals("short")
					|| lResStr.equals("float")
					|| lResStr.equals("double")) {
				lResStr = "number";
			}
		}
		return lResStr;
	}

	public static String getJSONType(String aJavaType, SqlRowSetMetaData aMetaData) {
		String lResStr = aJavaType.toLowerCase();
		if (lResStr != null) {
			if (lResStr.equals("bigdecimal")
					|| lResStr.equals("long")
					|| lResStr.equals("int")
					|| lResStr.equals("byte")
					|| lResStr.equals("short")
					|| lResStr.equals("float")
					|| lResStr.equals("double")) {
				lResStr = "number";
			}
		}
		return lResStr;
	}

	public static List<Object> getResultColumns(SqlRowSet aRowSet, int aColCount) {
		// TODO: should work with usual arrays!
		List<Object> lDataRow = new FastList<Object>();
		Object lObj = null;

		try {
			for (int lColIdx = 1; lColIdx <= aColCount; lColIdx++) {
				lObj = aRowSet.getObject(lColIdx);
				lDataRow.add(lObj);
			}
		} catch (Exception lEx) {
			// System.out.println("EXCEPTION in getResultColumns");
		}

		return lDataRow;
	}

	public static Token resultSetToToken(SqlRowSet aRowSet) {
		// instantiate response token
		Token lResponse = TokenFactory.createToken();
		// TODO: should work with usual arrays as well!
		// Object[] lColumns = null;
		int lRowCount = 0;
		int lColCount = 0;
		List<Map> lColumns = new FastList<Map>();
		List lData = new FastList();
		try {
			// TODO: metadata should be optional to save bandwidth!
			// generate the meta data for the response
			SqlRowSetMetaData lMeta = aRowSet.getMetaData();
			lColCount = lMeta.getColumnCount();
			lResponse.setInteger("colcount", lColCount);

			for (int lColIdx = 1; lColIdx <= lColCount; lColIdx++) {
				// get name of colmuns
				String lSimpleClass = JDBCTools.extractSimpleClass(lMeta.getColumnClassName(lColIdx));
				// convert to json type
				String lJSONType = JDBCTools.getJSONType(lSimpleClass, lMeta);

				Map<String, Object> lColHeader = new FastMap<String, Object>();
				lColHeader.put("name", lMeta.getColumnName(lColIdx));
				lColHeader.put("jsontype", lJSONType);
				lColHeader.put("jdbctype", lMeta.getColumnTypeName(lColIdx));

				lColumns.add(lColHeader);
			}

			// generate the result data
			while (aRowSet.next()) {
				lData.add(getResultColumns(aRowSet, lColCount));
				lRowCount++;
			}
		} catch (Exception lEx) {
			// mLog.error(lEx.getClass().getSimpleName() + " on query: " + lEx.getMessage());
		}

		// complete the response token
		lResponse.setInteger("rowcount", lRowCount);
		lResponse.setList("columns", lColumns);
		lResponse.setList("data", lData);

		return lResponse;
	}

	public static String fieldListToString(List aFieldList) {
		StringBuilder lRes = new StringBuilder();
		int lIdx = 0;
		int lCnt = aFieldList.size();
		for (Object lField : aFieldList) {
			lRes.append(lField);
			lIdx++;
			if (lIdx < lCnt) {
				lRes.append(",");
			}
		}
		return lRes.toString();
	}

	public static String valueToString(Object lField) {
		String lRes;
		if (lField instanceof String
				&& !((String) lField).startsWith("TO_DATE")) {
			lRes = "'" + (String) lField + "'";
		} else {
			lRes = lField.toString();
		}
		return lRes;
	}

	public static String valueListToString(List aFieldList) {
		StringBuilder lRes = new StringBuilder();
		int lIdx = 0;
		int lCnt = aFieldList.size();
		for (Object lField : aFieldList) {
			lRes.append(valueToString(lField));
			/*
			if (lField instanceof String
			&& !((String) lField).startsWith("TO_DATE")) {
			lRes.append("'");
			lRes.append(lField);
			lRes.append("'");
			} else {
			lRes.append(lField);
			}
			 */
			lIdx++;
			if (lIdx < lCnt) {
				lRes.append(",");
			}
		}
		return lRes.toString();
	}
}
