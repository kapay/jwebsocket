//	---------------------------------------------------------------------------
//	jWebSocket - Users Class
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
public class Users {

	private static Logger log = Logger.getLogger(Users.class);
	private FastMap<String, User> users = new FastMap<String, User>();

	/**
	 *
	 * @param aLoginName
	 * @return
	 */
	public User getUserByLoginName(String aLoginName) {
		if (aLoginName != null) {
			return users.get(aLoginName);
		} else {
			return null;
		}
	}

	/**
	 * Adds a new user to the map of users.
	 * @param aUser
	 */
	public void addUser(User aUser) {
		if (aUser != null) {
			users.put(aUser.getLoginname(), aUser);
		}
	}

	/**
	 * Removes a certain user from the map of users.
	 * @param aLoginName
	 */
	public void removeUser(String aLoginName) {
		if (aLoginName != null) {
			users.remove(aLoginName);
		}
	}

	/**
	 * Removes a certain user from the map of users.
	 * @param aUser
	 */
	public void removeUser(User aUser) {
		if (aUser != null) {
			users.remove(aUser.getLoginname());
		}
	}
}
