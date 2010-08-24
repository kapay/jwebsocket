//	---------------------------------------------------------------------------
//	jWebSocket - RPC PlugIn
//	Copyright (c) 2010 Innotrade GmbH, jWebSocket.org
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
package org.jwebsocket.plugins.rpc.util;

import java.util.List;
import java.util.Map;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.rpc.RPCPlugIn;
import org.jwebsocket.token.Token;

/**
 * map javascript types with java types
 * @author Quentin Ambard
 */
public class TypeConverter {

	public final static String PROTOCOL_TYPE_INT = "int";
	public final static String PROTOCOL_TYPE_BOOLEAN = "boolean";
	public final static String PROTOCOL_TYPE_STRING = "string";
	public final static String PROTOCOL_TYPE_MAP = "map";
	public final static String PROTOCOL_TYPE_DOUBLE = "double";
	public final static String PROTOCOL_TYPE_ARRAY = "array";
	private static Logger mLog = Logging.getLogger(RPCPlugIn.class);
	private final static Map<String, String> mProtocolValue = new FastMap<String, String>();

	static {
		mProtocolValue.put(boolean.class.getName(), PROTOCOL_TYPE_BOOLEAN);
		mProtocolValue.put(Boolean.class.getName(), PROTOCOL_TYPE_BOOLEAN);
		mProtocolValue.put(double.class.getName(), PROTOCOL_TYPE_DOUBLE);
		mProtocolValue.put(Double.class.getName(), PROTOCOL_TYPE_DOUBLE);
		mProtocolValue.put(Integer.class.getName(), PROTOCOL_TYPE_INT);
		mProtocolValue.put(int.class.getName(), PROTOCOL_TYPE_INT);
		mProtocolValue.put(String.class.getName(), PROTOCOL_TYPE_STRING);
		mProtocolValue.put(List.class.getName(), PROTOCOL_TYPE_ARRAY);
		mProtocolValue.put(Token.class.getName(), PROTOCOL_TYPE_MAP);
	}

	public static String getProtocolValue(String aJavaType) {
		return mProtocolValue.get(aJavaType);
	}

	public static String getProtocolValue(Class aJavaClass) {
		return getProtocolValue(aJavaClass.getName());
	}

	public static boolean isValidProtocolType(String aJavaType) {
		return mProtocolValue.containsKey(aJavaType);
	}

	public static boolean isValidProtocolType(Class aJavaClass) {
		return isValidProtocolType(aJavaClass.getName());
	}

	public static boolean matchProtocolTypeToJavaType(String aProtocolType, String aJavaType) {
		if (!mProtocolValue.containsKey(aJavaType)) {
			return false;
		} else {
			return mProtocolValue.get(aJavaType).equals(aProtocolType);
		}
	}

	public static boolean matchProtocolTypeToJavaType(String aProtocolType, Class aJavaType) {
		return matchProtocolTypeToJavaType(aProtocolType, aJavaType.getName());
	}
}
