//	---------------------------------------------------------------------------
//	jWebSocket - Token Implementation
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
package org.jwebsocket.token;

import java.util.HashMap;
import java.util.Iterator;

/**
 * A token is ...
 * @author aschulze
 */
public class Token {

	private HashMap<String, Object> items = new HashMap<String, Object>();

	/**
	 * Creates a new empty instance of a token.
	 * The token does not contaon any items.
	 */
	public Token() {
	}

	/**
	 * puts a new key/value pair into the token, in other words it adds a
	 * new item to the token.
	 * @param aKey key of the the token item.
	 * @param aValue value of the token item.
	 */
	public void put(String aKey, Object aValue) {
		items.put(aKey, aValue);
	}

	/**
	 *
	 * @param aKey
	 * @return
	 */
	public Object get(String aKey) {
		return items.get(aKey);
	}

	/**
	 *
	 * @param aKey
	 * @return
	 */
	public Object remove(String aKey) {
		return items.remove(aKey);
	}


	/**
	 *
	 * @return
	 */
	public Iterator<String> getKeys() {
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
	 * Returns the name space of the token. If you have the same token type
	 * interpreted by multiple different plug-ins the namespace allows to
	 * uniquely address a certain plug-in. Each plug-in has its own namespace.
	 * @return the namespace.
	 */
	public String getNS() {
		return (String) items.get("ns");
	}

	/**
	 * Sets the name space of the token. If you have the same token type
	 * interpreted by multiple different plug-ins the namespace allows to
	 * uniquely address a certain plug-in. Each plug-in has its own namespace.
	 * @param aNS the namespace to be set for the token.
	 */
	public void setNS(String aNS) {
		items.put("ns", aNS);
	}

	@Override
	public String toString() {
		String lRes = "{";
		for (Iterator<String> i = items.keySet().iterator(); i.hasNext();) {
			String lKey = i.next();
			lRes += lKey + "=" + items.get(lKey) + (i.hasNext() ? "," : "");
		}
		return lRes + "}";
	}
}
