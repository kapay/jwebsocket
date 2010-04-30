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

import static org.jwebsocket.config.JWebSocketConstants.DEFAULT_INSTALLATION;
import static org.jwebsocket.config.JWebSocketConstants.DEFAULT_PROTOCOL;

import java.util.Collections;
import java.util.List;

import org.jwebsocket.config.xml.EngineConfig;
import org.jwebsocket.config.xml.PluginConfig;
import org.jwebsocket.config.xml.RightConfig;
import org.jwebsocket.config.xml.RoleConfig;
import org.jwebsocket.config.xml.ServerConfig;
import org.jwebsocket.config.xml.UserConfig;
import org.jwebsocket.kit.WebSocketRuntimeException;

/**
 * Represents the jWebSocket configuration. This class is immutable and should
 * not be overridden.
 * 
 * @author puran
 * @version $Id: JWebSocketConfig.java 345 2010-04-10 20:03:48Z fivefeetfurther$
 * 
 */
public final class JWebSocketConfig implements Config {

	private final String installation;
	private final String protocol;
	private final String jWebSocketHome;
	private final String libraryFolder;
	private final String initializer;
	
	/**
	 * @return the installation
	 */
	public String getInstallation() {
		if (installation == null || installation.length() == 0) {
			return DEFAULT_INSTALLATION;
		}
		return installation;
	}

	/**
	 * @return the protocol
	 */
	public String getProtocol() {
		if (protocol == null || protocol.length() == 0) {
			return DEFAULT_PROTOCOL;
		}
		return protocol;
	}
	/**
	 * @return the jWebSocketHome
	 */
	public String getjWebSocketHome() {
		return jWebSocketHome;
	}

	/**
	 * @return the libraryFolder
	 */
	public String getLibraryFolder() {
		return libraryFolder;
	}
	/**
	 * @return the initializer
	 */
	public String getInitializer() {
		return initializer;
	}

	/**
	 * @return the config
	 */
	public static JWebSocketConfig getConfig() {
		return config;
	}

	private final List<EngineConfig> engines;
	private final List<ServerConfig> servers;
	private final List<UserConfig> users;
	private final List<PluginConfig> plugins;

	private final List<RightConfig> globalRights;
	private final List<RoleConfig> globalRoles;

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
		installation = builder.installation;
		protocol = builder.protocol;
		jWebSocketHome = builder.jWebSocketHome;
		libraryFolder = builder.libraryFolder;
		initializer = builder.initializer;
		engines = builder.engines;
		servers = builder.servers;
		users = builder.users;
		plugins = builder.plugins;
		globalRights = builder.globalRights;
		globalRoles = builder.globalRoles;
		// validate the config
		validate();
	}

	/**
	 * Config builder class.
	 * 
	 * @author puran
	 * @version $Id$
	 * 
	 */
	public static class Builder {
		private  String installation;
		private  String protocol;
		private  String jWebSocketHome;
		private  String libraryFolder;
		private  String initializer;
		
		private List<EngineConfig> engines;
		private List<ServerConfig> servers;
		private List<UserConfig> users;
		private List<PluginConfig> plugins;

		private List<RightConfig> globalRights;
		private List<RoleConfig> globalRoles;
		
		public Builder addInstallation(String theInstallation) {
			installation = theInstallation;
			return this;
		}
		
		public Builder addProtocol(String theProtocol) {
			protocol = theProtocol;
			return this;
		}
		
		public Builder addJWebSocketHome(String theJWebSocketHome) {
			jWebSocketHome = theJWebSocketHome;
			return this;
		}
		
		public Builder addInitializer(String theInitializer) {
			initializer = theInitializer;
			return this;
		}
		
		public Builder addLibraryFolder(String theLibraryFolder) {
			libraryFolder = theLibraryFolder;
			return this;
		}

		public Builder addEngines(List<EngineConfig> theEngines) {
			engines = theEngines;
			return this;
		}

		public Builder addServers(List<ServerConfig> theServers) {
			servers = theServers;
			return this;
		}

		public Builder addPlugins(List<PluginConfig> thePlugins) {
			plugins = thePlugins;
			return this;
		}

		public Builder addGlobalRights(List<RightConfig> theRights) {
			globalRights = theRights;
			return this;
		}

		public Builder addGlobalRoles(List<RoleConfig> theRoles) {
			globalRoles = theRoles;
			return this;
		}

		public Builder addUsers(List<UserConfig> theUsers) {
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
	public List<EngineConfig> getEngines() {
		return Collections.unmodifiableList(engines);
	}

	/**
	 * @return the servers
	 */
	public List<ServerConfig> getServers() {
		return Collections.unmodifiableList(servers);
	}

	/**
	 * @return the users
	 */
	public List<UserConfig> getUsers() {
		return Collections.unmodifiableList(users);
	}

	/**
	 * @return the plugins
	 */
	public List<PluginConfig> getPlugins() {
		return Collections.unmodifiableList(plugins);
	}

	/**
	 * @return the globalRights
	 */
	public List<RightConfig> getGlobalRights() {
		return Collections.unmodifiableList(globalRights);
	}

	/**
	 * @return the globalRoles
	 */
	public List<RoleConfig> getGlobalRoles() {
		return Collections.unmodifiableList(globalRoles);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void validate() {
		if ((engines == null || engines.isEmpty())
				|| (servers == null || servers.isEmpty())
				|| (users == null || users.isEmpty())
				|| (plugins == null || plugins.isEmpty())
				|| (globalRights == null || globalRights.isEmpty())
				|| (globalRoles == null || globalRoles.isEmpty())) {
			throw new WebSocketRuntimeException(
					"Missing one of the server configuration, please check your configuration file");
		}
	}
}
