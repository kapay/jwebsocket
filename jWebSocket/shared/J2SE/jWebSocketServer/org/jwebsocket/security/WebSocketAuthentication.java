//	---------------------------------------------------------------------------
//	jWebSocket - Authenticator Interface
//	Copyright (c) 2010 Alexander Schulze, Innotrade GmbH
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
package org.jwebsocket.security;

/**
 * Preliminary Draft! Do not yet use! RFC!
 * @author aschulze
 */
public interface WebSocketAuthentication {

	/**
	 * loads the list of users from the repository.
	 */
	void loadUsers();

	/**
	 * refreshes the list of users from the repository.
	 */
	void refreshUsers();

	/**
	 * adds a new user to the list of users in the repository
	 */
	void addUser();

	/**
	 * removes a user from the list of users in the repository
	 */
	void removeUser();

	/**
	 * logs in a user with username (loginname) and password
	 * @param aUsername
	 * @param aPassword
	 * @return
	 */
	User login(String aUsername, String aPassword);

	/**
	 * logs out a user
	 * @param aUser
	 * @return
	 */
	User logout(User aUser);

	/**
	 * returns an object which describes the available features of the
	 * Authentication object.
	 * @return
	 */
	Object getCapabilities();
}
