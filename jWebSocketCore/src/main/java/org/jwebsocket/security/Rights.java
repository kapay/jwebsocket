//	---------------------------------------------------------------------------
//	jWebSocket - Right Class
//	Copyright (c) 2010 jWebSocket.org, Alexander Schulze, Innotrade GmbH
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
package org.jwebsocket.security;

import javolution.util.FastMap;
import org.apache.log4j.Logger;

/**
 *
 * @author aschulze
 */
public class Rights {

	private static Logger log = Logger.getLogger(Rights.class);
	private FastMap<String, Right> rights = new FastMap<String, Right>();

	/**
	 * Adds a new right to the map of rights.
	 * @param aRight
	 */
	public void addRight(Right aRight) {
		if (aRight != null) {
			rights.put(aRight.getKey(), aRight);
		}
	}

	/**
	 * 
	 * @param aKey
	 * @return
	 */
	public Right get(String aKey) {
		return rights.get(aKey);
	}

	/**
	 * Removes a certain right from the map of rights.
	 * @param aKey
	 */
	public void removeRight(String aKey) {
		rights.remove(aKey);
	}

	/**
	 * Removes a certain right from the map of rights.
	 * @param aRight
	 */
	public void removeRight(Right aRight) {
		if (aRight != null) {
			rights.remove(aRight.getKey());
		}
	}

	/**
	 *
	 * @param aRight
	 * @return
	 */
	public boolean hasRight(Right aRight) {
		if (aRight != null) {
			return rights.containsKey(aRight.getKey());
		} else {
			return false;
		}
	}

	/**
	 *
	 * @param aRight
	 * @return
	 */
	public boolean hasRight(String aRight) {
		return rights.containsKey(aRight);
	}
}
