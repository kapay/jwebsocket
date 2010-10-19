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
}
