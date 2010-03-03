//	---------------------------------------------------------------------------
//	jWebSocket - Request RequestHeader Object
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
 *
 * @author aschulze
 */
public class RequestHeader {

	private HashMap<String, Object> args = new HashMap<String, Object>();

	/**
	 *
	 * @param aKey
	 * @param aValue
	 */
	public void put(String aKey, Object aValue) {
		args.put(aKey, aValue);
	}

	/**
	 *
	 * @param aKey
	 * @return
	 */
	public Object get(String aKey) {
		return args.get(aKey);
	}

	/**
	 *
	 * @param aKey
	 * @return
	 */
	public String getString(String aKey) {
		return (String) args.get(aKey);
	}

	public HashMap getArgs() {
		return (HashMap) args.get("args");
	}

	public String getSubProtocol(String aDefault) {
		HashMap lArgs = getArgs();
		String lSubProt = null;
		if (lArgs != null) {
			lSubProt = (String) lArgs.get("prot");
		}
		return (lSubProt != null ? lSubProt : aDefault);
	}

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
