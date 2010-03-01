//	---------------------------------------------------------------------------
//	jWebSocket - Token
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
package org.jWebSocket.token;

import java.util.HashMap;
import java.util.Iterator;
import org.jWebSocket.api.IDataPacket;
import org.jWebSocket.api.IWebSocketConnector;

/**
 *
 * @author aschulze
 */
public class Token {

	private HashMap<String, Object> items = new HashMap<String, Object>();

	/**
	 *
	 */
	public Token() {
	}

	/**
	 *
	 */
	public Token(IWebSocketConnector aConnector, IDataPacket aDataPacket) {
	}

	public void put(String aKey, Object aValue) {
		items.put(aKey, aValue);
	}

	public Object get(String aKey) {
		return items.get(aKey);
	}

	public Iterator getKeys() {
		return items.keySet().iterator();
	}


	/**
	 *
	 * @param aType
	 */
	public Token(String aType) {
		setType(aType);
	}

	/**
	 *
	 * @param aNS
	 * @param aType
	 */
	public Token(String aNS, String aType) {
		setNS(aNS);
		setType(aType);
	}

	/**
	 *
	 * @param aArg
	 * @return
	 */
	public String getString(String aArg) {
		return (String) items.get(aArg);
	}

	/**
	 *
	 * @param aArg
	 * @param aDefault
	 * @return
	 */
	public Integer getInteger(String aArg, Integer aDefault) {
		Object lObj = items.get(aArg);
		Integer lResult = aDefault;
		if (lObj != null) {
			if (lObj instanceof String) {
				try {
					lResult = Integer.parseInt((String) lObj);
				} catch (NumberFormatException ex) {
					// ignore exception here, return default
				}
			}
		}
		return lResult;
	}

	/**
	 *
	 * @return
	 */
	public String getType() {
		return (String) items.get("type");
	}

	/**
	 *
	 * @param aType
	 */
	public void setType(String aType) {
		items.put("type", aType);
	}

	/**
	 *
	 * @return
	 */
	public String getNS() {
		return (String) items.get("ns");
	}

	/**
	 *
	 * @param aType
	 */
	public void setNS(String aType) {
		items.put("ns", aType);
	}
}
