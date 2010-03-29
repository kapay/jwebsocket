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

	public User getUserByLoginName(String aLoginName) {
		return users.get(aLoginName);
	}

}
