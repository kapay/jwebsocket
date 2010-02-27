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
import org.jWebSocket.api.IDataPacket;

/**
 *
 * @author aschulze
 */
public class Token extends HashMap<String, Object> {

	/**
	 *
	 */
	public Token() {
	}

	public Token packetToToken(IDataPacket aDataPacket) {
		return null;
	}

	public IDataPacket tokenToPacket(Token aToken) {
		return null;
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
		return (String) get(aArg);
	}

	/**
	 *
	 * @param aArg
	 * @param aDefault
	 * @return
	 */
	public Integer getInteger(String aArg, Integer aDefault) {
		Object lObj = get(aArg);
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
		return (String) get("type");
	}

	/**
	 *
	 * @param aType
	 */
	public void setType(String aType) {
		put("type", aType);
	}

	/**
	 *
	 * @return
	 */
	public String getNS() {
		return (String) get("ns");
	}

	/**
	 *
	 * @param aType
	 */
	public void setNS(String aType) {
		put("ns", aType);
	}
}
