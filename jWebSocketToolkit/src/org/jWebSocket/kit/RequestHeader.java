//	---------------------------------------------------------------------------
//	jWebSocket - RequestHeader Object
//	Copyright (c) 2010 Alexander Schulze, Innotrade GmbH
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
package org.jWebSocket.kit;

import java.util.HashMap;

/**
 * Holds the header of the initial WebSocket request from the client
 * to the server. The RequestHeader internally maintains a HashMap to store
 * key/values pairs.
 * @author aschulze
 */
public class RequestHeader {

	private HashMap<String, Object> args = new HashMap<String, Object>();

	/**
	 * Puts a new object value to the request header.
	 * @param aKey
	 * @param aValue
	 */
	public void put(String aKey, Object aValue) {
		args.put(aKey, aValue);
	}

	/**
	 * Returns the object value for the given key or {@code null} if the
	 * key does not exist in the header.
	 * @param aKey
	 * @return object value for the given key or {@code null}.
	 */
	public Object get(String aKey) {
		return args.get(aKey);
	}

	/**
	 * Returns the string value for the given key or {@code null} if the
	 * key does not exist in the header.
	 * @param aKey
	 * @return String value for the given key or {@code null}.
	 */
	public String getString(String aKey) {
		return (String) args.get(aKey);
	}

	/**
	 * Returns a HashMap of the optional URL arguments passed by the client.
	 * @return HashMap of the optional URL arguments.
	 */
	public HashMap getArgs() {
		return (HashMap) args.get("args");
	}

	/**
	 * Returns the sub protocol passed by the client or a default value 
	 * if no sub protocol has been passed either in the header or in the
	 * URL arguments.
	 * @param aDefault
	 * @return Sub protocol passed by the client or default value.
	 */
	public String getSubProtocol(String aDefault) {
		HashMap lArgs = getArgs();
		String lSubProt = null;
		if (lArgs != null) {
			lSubProt = (String) lArgs.get("prot");
		}
		return (lSubProt != null ? lSubProt : aDefault);
	}

	/**
	 * Returns the session timeout passed by the client or a default value
	 * if no session timeout has been passed either in the header or in the
	 * URL arguments.
	 * @param aDefault
	 * @return Session timeout passed by the client or default value.
	 */
	public Integer getTimeout(Integer aDefault) {
		HashMap lArgs = getArgs();
		Integer lTimeout = null;
		if (lArgs != null) {
			try {
				lTimeout = Integer.parseInt((String) (lArgs.get("timeout")));
			} catch (Exception ex) {
			}
		}
		return (lTimeout != null ? lTimeout : aDefault);
	}
}
