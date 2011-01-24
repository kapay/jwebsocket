//	---------------------------------------------------------------------------
//	jWebSocket - Authorization Interface
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
public interface WebSocketAuthorization {

	/**
	 * loads the list of roles from the repository
	 */
	void loadRoles();

	/**
	 * loads the list of rights from the repository
	 */
	void loadRights();

	/**
	 * loads the assignments between users rights and roles from the repository
	 */
	void loadAssignments();

	/**
	 * adds a new right to the list of rights in the repository
	 */
	void addRight();

	/**
	 * removes a right from the list of rights in the repository
	 */
	void removeRight();

	/**
	 * adds a new roles to the list of roles in the repository
	 */
	void addRole();

	/**
	 * removes a role from the list of roles in the repository
	 */
	void removeRole();

	/**
	 * returns an object which describes the available feature of the
	 * Authorization object.
	 * @return
	 */
	Object getCapabilities();
}
