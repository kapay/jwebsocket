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

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jwebsocket.token.Token;

/**
 * methods to handle JsonArray objects
 * @author Quentin Ambard
 */
public class JSONArrayHandler {

	/**
	 * Return a List from a JSONArray. The type of the List is defined by the class
	 * Only accepts primitives & wrappers + JsonArray + JsonObject (converted as Token) !
	 * @param aJsonArray JSONArray to be converted as a List
	 * @param aClass Class of the object inside the List.
	 * @return
	 * @throws JSONException
	 */
	public static List JSONArrayToList(JSONArray aJsonArray, Type aType) throws JSONException {
//		if (aType  ==== List) {
//			throw new JSONException("Only List are supported");
//		}
		List lList;
		if (aType instanceof ParameterizedType) {
			lList = new ArrayList();
			ParameterizedType lParameterizedType = (ParameterizedType) aType;
			Type[] parameterArgTypes = lParameterizedType.getActualTypeArguments();
			Class parameterArgClass;
			if (parameterArgTypes[0] instanceof ParameterizedType) {
				parameterArgClass = JSONArray.class;
			} else {
				parameterArgClass = (Class) parameterArgTypes[0];
			}

			try {
				String nameUpperCase;
				if (parameterArgClass == Integer.class) {
					nameUpperCase = "getInt";
				} else {
					nameUpperCase = parameterArgClass.getName().substring(parameterArgClass.getName().lastIndexOf(".") + 1, parameterArgClass.getName().length());
					nameUpperCase = "get" + nameUpperCase.substring(0, 1).toUpperCase() + nameUpperCase.substring(1);
				}
				nameUpperCase = nameUpperCase + "Strict";

				Method lMetod = JSONArrayHandler.class.getMethod(nameUpperCase, JSONArray.class, int.class);
				for (int i = 0; i < aJsonArray.length(); i++) {
					Object o = lMetod.invoke(null, aJsonArray, i);
					if (parameterArgClass == JSONObject.class) {
						o = new Token((JSONObject) o);
					} //if we have an array in an array...
					else if (parameterArgClass == JSONArray.class) {
						JSONArrayToList((JSONArray) o, ((ParameterizedType) parameterArgTypes[0]));
					}
					lList.add(o);
				}
			} catch (Exception e) {
				throw new JSONException("");
			}
		} //if not ParameterizedType, get a list of Object
		else {
			lList = JSONArrayToList(aJsonArray);
		}
		return lList;
	}

	/**
	 * Return a List of Object from a JSONArray
	 * @param aJsonArray
	 * @return
	 * @throws JSONException
	 */
	public static List<?> JSONArrayToList(JSONArray aJsonArray) throws JSONException {
		List<Object> lList = new ArrayList<Object>();
		for (int i = 0; i < aJsonArray.length(); i++) {
			try {
				lList.add(aJsonArray.getDouble(i));
			} catch (JSONException e) {
				try {
					lList.add(aJsonArray.getInt(i));
				} catch (JSONException e1) {
					try {
						lList.add(aJsonArray.getString(i));
					} catch (JSONException e2) {
						try {
							lList.add(aJsonArray.getBoolean(i));
						} catch (JSONException e3) {
							try {
								lList.add(aJsonArray.getJSONObject(i));
							} catch (JSONException e4) {
								lList.add(aJsonArray.getJSONArray(i));
							}
						}
					}
				}
			}
		}
		return lList;
	}

	/**
	 * Get the boolean value associated with an index.
	 *
	 * @param index The index must be between 0 and length() - 1.
	 * @return      The truth.
	 * @throws JSONException If there is no value for the index or if the
	 *  value is not convertable to boolean.
	 */
	public static boolean getBooleanStrict(JSONArray jsonArray, int index) throws JSONException {
		Object o = jsonArray.get(index);
		if (o.equals(Boolean.FALSE)) {
			return false;
		} else if (o.equals(Boolean.TRUE)) {
			return true;
		}
		throw new JSONException("JSONArray[" + index + "] is not a Boolean.");
	}

	/**
	 * Get the double value associated with an index.
	 *
	 * @param index The index must be between 0 and length() - 1.
	 * @return      The value.
	 * @throws   JSONException If the key is not found or if the value is not a Number
	 */
	public static double getDoubleStrict(JSONArray jsonArray, int index) throws JSONException {
		Object o = jsonArray.get(index);
		try {
			return ((Number) o).doubleValue();
		} catch (Exception e) {
			throw new JSONException("JSONArray[" + index
					+ "] is not a number.");
		}
	}

	/**
	 * Get the int value associated with an index.
	 *
	 * @param index The index must be between 0 and length() - 1.
	 * @return      The value.
	 * @throws   JSONException If the key is not found or if the value is not a Number
	 *  if the value cannot be converted to a number.
	 */
	public static int getIntStrict(JSONArray jsonArray, int index) throws JSONException {
		Object o = jsonArray.get(index);
		return o instanceof Number
				? ((Number) o).intValue() : (int) getDoubleStrict(jsonArray, index);
	}

	/**
	 * Get the string associated with an index.
	 * @param index The index must be between 0 and length() - 1.
	 * @return      A string value.
	 * @throws JSONException If there is no value for the index.
	 */
	public static String getStringStrict(JSONArray jsonArray, int index) throws JSONException {
		Object o = jsonArray.get(index);
		if (o instanceof String) {
			return o.toString();
		}
		throw new JSONException("JSONArray[" + index
				+ "] is not a string.");
	}

	/**
	 * Same as JSONObjec.getJSONArray (index)
	 * @param jsonArray
	 * @param index
	 * @return
	 * @throws JSONException
	 */
	public static JSONArray getJSONArrayStrict(JSONArray jsonArray, int index) throws JSONException {
		return jsonArray.getJSONArray(index);
	}

	/**
	 *  Same as JSONObject.getJSONObject (index)
	 * @param jsonArray
	 * @param index
	 * @return
	 * @throws JSONException
	 */
	public static JSONObject getJSONObjectStrict(JSONArray jsonArray, int index) throws JSONException {
		return jsonArray.getJSONObject(index);
	}
}
