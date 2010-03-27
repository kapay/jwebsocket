//	---------------------------------------------------------------------------
//	jWebSocket - Copyright (c) 2010 jwebsocket.org
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
package org.jwebsocket.config;

import java.util.List;

import org.jwebsocket.config.Config;
import org.jwebsocket.config.xml.Engine;
import org.jwebsocket.config.xml.Plugin;
import org.jwebsocket.config.xml.Right;
import org.jwebsocket.config.xml.Role;
import org.jwebsocket.config.xml.Server;
import org.jwebsocket.config.xml.User;
import org.jwebsocket.kit.WebSocketRuntimeException;

/**
 * @author puran
 * @version $Id$
 * 
 */
public final class JWebSocketConfig implements Config {

	private final List<Engine> engines;
	private final List<Server> servers;
	private final List<User> users;
	private final List<Plugin> plugins;

	private final List<Right> globalRights;
	private final List<Role> globalRoles;

	private static JWebSocketConfig config = null;

	/**
	 * private constructor used by the builder
	 */
	private JWebSocketConfig(Builder builder) {
		if (builder.engines == null || builder.servers == null
				|| builder.users == null || builder.globalRights == null
				|| builder.globalRoles == null) {
			throw new WebSocketRuntimeException(
					"Configuration is not loaded completely.");
		}
		engines = builder.engines;
		servers = builder.servers;
		users = builder.users;
		plugins = builder.plugins;
		globalRights = builder.globalRights;
		globalRoles = builder.globalRoles;
	}

	/**
	 * Config builder class.
	 * @author puran
	 * @version $Id$
	 *
	 */
	public static class Builder {
		private List<Engine> engines;
		private List<Server> servers;
		private List<User> users;
		private List<Plugin> plugins;

		private List<Right> globalRights;
		private List<Role> globalRoles;

		public Builder addEngines(List<Engine> theEngines) {
			engines = theEngines;
			return this;
		}
		public Builder addServers(List<Server> theServers) {
			servers = theServers;
			return this;
		}
		public Builder addPlugins(List<Plugin> thePlugins) {
			plugins = thePlugins;
			return this;
		}
		public Builder addGlobalRights(List<Right> theRights) {
			globalRights = theRights;
			return this;
		}

		public Builder addGlobalRoles(List<Role> theRoles) {
			globalRoles = theRoles;
			return this;
		}

		public Builder addUsers(List<User> theUsers) {
			users = theUsers;
			return this;
		}

		public synchronized JWebSocketConfig buildConfig() {
			if (config == null) {
				config = new JWebSocketConfig(this);
			}
			return config;
		}
	}

	/**
	 * @return the engines
	 */
	public List<Engine> getEngines() {
		return engines;
	}

	/**
	 * @return the servers
	 */
	public List<Server> getServers() {
		return servers;
	}

	/**
	 * @return the users
	 */
	public List<User> getUsers() {
		return users;
	}

	/**
	 * @return the plugins
	 */
	public List<Plugin> getPlugins() {
		return plugins;
	}

	/**
	 * @return the globalRights
	 */
	public List<Right> getGlobalRights() {
		return globalRights;
	}

	/**
	 * @return the globalRoles
	 */
	public List<Role> getGlobalRoles() {
		return globalRoles;
	}

}
