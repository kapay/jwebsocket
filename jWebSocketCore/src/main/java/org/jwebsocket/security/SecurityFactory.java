/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.security;

import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;

/**
 *
 * @author aschulze
 */
public class SecurityFactory {

	private static Logger log = Logging.getLogger(SecurityFactory.class);
	private static Users users = new Users();

	/**
	 *
	 */
	public static void initDemo() {
		if (log.isDebugEnabled()) {
			log.debug("Initializing demo rights, roles and users...");
		}
		// specify rights
		Right lRPC = new Right("org.jWebSocket.plugins.rpc.rpc", "Allow Remote Procedure Calls (RPC) to server");
		Right lRRPC = new Right("org.jWebSocket.plugins.rpc.rrpc", "Allow Reverse Remote Procedure Calls (RRPC) to other clients");

		// specify roles and assign rights to roles
		Role lGuestRole = new Role("guest", "Guests");
		Role lRegRole = new Role("regUser", "Registered Users", lRPC);
		Role lAdminRole = new Role("admin", "Administrators", lRPC, lRRPC);

		// specify role sets for a simpler assignment to the users
		Roles lGuestRoles = new Roles(lGuestRole);
		Roles lRegRoles = new Roles(lGuestRole, lRegRole);
		Roles lAdminRoles = new Roles(lGuestRole, lRegRole, lAdminRole);

		User lGuestUser = new User("guest", "Guest", "Guest", "guest", lGuestRoles);
		User lRegUser = new User("user", "User", "User", "user", lRegRoles);
		User lAdminUser = new User("admin", "Admin", "Admin", "admin", lAdminRoles);

		users.addUser(lGuestUser);
		users.addUser(lRegUser);
		users.addUser(lAdminUser);

		log.info("Demo rights, roles and users initialized.");
	}

	/**
	 *
	 * @param aLoginname
	 * @param aRight
	 * @return
	 */
	public static boolean checkRight(String aLoginname, String aRight) {
		boolean lHasRight = false;
		User lUser = users.getUserByLoginName(aLoginname);
		if (lUser != null) {
			return lUser.hasRight(aRight);
		}
		return lHasRight;
	}
}
