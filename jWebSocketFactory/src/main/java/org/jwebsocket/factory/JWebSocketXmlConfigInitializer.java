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
package org.jwebsocket.factory;

import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;
import static org.jwebsocket.config.JWebSocketConstants.JWEBSOCKET_HOME;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketFilter;
import org.jwebsocket.api.WebSocketInitializer;
import org.jwebsocket.api.WebSocketPlugIn;
import org.jwebsocket.api.WebSocketServer;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.config.xml.EngineConfig;
import org.jwebsocket.config.xml.FilterConfig;
import org.jwebsocket.config.xml.PluginConfig;
import org.jwebsocket.config.xml.ServerConfig;
import org.jwebsocket.kit.WebSocketRuntimeException;

/**
 * Intialize the engine, servers and plugins based on jWebSocket.xml
 * configuration
 * 
 * @author puran
 * @version $Id: JWebSocketXmlConfigInitializer.java 424 2010-05-01 19:11:04Z
 *          mailtopuran $
 * 
 */
public final class JWebSocketXmlConfigInitializer implements
		WebSocketInitializer {

	private static Logger log = Logging.getLogger(JWebSocketXmlConfigInitializer.class);
	private final JWebSocketJarClassLoader classLoader = new JWebSocketJarClassLoader();
	private JWebSocketConfig config;

	/**
	 * private constructor
	 */
	private JWebSocketXmlConfigInitializer(JWebSocketConfig aConfig) {
		this.config = aConfig;
	}

	/**
	 * Returns the initializer object
	 * 
	 * @param config
	 *            the jWebSocket config
	 * @return the initializer object
	 */
	public static JWebSocketXmlConfigInitializer getInitializer(
			JWebSocketConfig config) {
		return new JWebSocketXmlConfigInitializer(config);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public WebSocketEngine intializeEngine() {
		WebSocketEngine newEngine = null;
		EngineConfig engine = config.getEngines().get(0);
		String jarFilePath = "-";
		try {
			jarFilePath = getLibraryFolderPath(engine.getJar());
			classLoader.addFile(jarFilePath);

			if (log.isDebugEnabled()) {
				log.debug("Loading engine '" + engine.getName() + "' from '" + jarFilePath + "'...");
			}
			Class engineClass = null;
			try {
				engineClass = Class.forName(engine.getName());
			} catch (Exception ex) {
				if (log.isDebugEnabled()) {
					log.debug(ex.getClass().getSimpleName()
							+ ": Engine not yet in classpath, hence trying to load from file...");
				}
			}
			if (engineClass == null) {
				engineClass = (Class<WebSocketEngine>) classLoader.loadClass(engine.getName());
			}
			Constructor<WebSocketEngine> ctor =
					engineClass.getDeclaredConstructor(String.class, Integer.class,
					Integer.class);
			ctor.setAccessible(true);
			newEngine = ctor.newInstance(new Object[]{engine.getId(),
						engine.getPort(), engine.getTimeout()});
			if (log.isDebugEnabled()) {
				log.debug("Engine successfully instantiated.");
			}
		} catch (MalformedURLException e) {
			log.error(
					"Couldn't Load the Jar file for engine, make sure jar file exists or name is correct",
					e);
		} catch (ClassNotFoundException e) {
			log.error(
					"Engine class '" + engine.getName() + "'@'" + jarFilePath + "' not found", e);
		} catch (InstantiationException e) {
			log.error(
					"Engine class could not be instantiated", e);
		} catch (IllegalAccessException e) {
			log.error(
					"Illegal Access Exception while intializing engine", e);
		} catch (NoSuchMethodException e) {
			log.error(
					"No Constructor found with given 3 arguments", e);
		} catch (InvocationTargetException e) {
			log.error(
					"Exception invoking engine object", e);
		}

		return newEngine;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<WebSocketServer> initializeServers() {
		List<WebSocketServer> servers = new ArrayList<WebSocketServer>();
		List<ServerConfig> serverConfigs = config.getServers();
		for (ServerConfig serverConfig : serverConfigs) {
			WebSocketServer server = null;
			String jarFilePath = "-";
			try {
				jarFilePath = getLibraryFolderPath(serverConfig.getJar());
				classLoader.addFile(jarFilePath);

				if (log.isDebugEnabled()) {
					log.debug("Loading server '" + serverConfig.getName() + "' from '" + jarFilePath + "'...");
				}
				Class serverClass = null;
				try {
					serverClass = Class.forName(serverConfig.getName());
				} catch (Exception ex) {
					if (log.isDebugEnabled()) {
						log.debug(ex.getClass().getSimpleName()
								+ ": Server not yet in classpath, hence trying to load from file...");
					}
				}
				if (serverClass == null) {
					serverClass = (Class<WebSocketServer>) classLoader.loadClass(serverConfig.getName());
				}
				Constructor<WebSocketServer> ctor = serverClass.getDeclaredConstructor(String.class);
				ctor.setAccessible(true);
				server = ctor.newInstance(new Object[]{serverConfig.getId()});
				if (log.isDebugEnabled()) {
					log.debug("Server successfully instantiated.");
				}
				// add the initialized server to the list
				servers.add(server);
			} catch (MalformedURLException e) {
				log.error(
						"Couldn't Load the Jar file for server, make sure jar file '" + jarFilePath + "' exists and name is correct.",
						e);
			} catch (ClassNotFoundException e) {
				log.error(
						"Server class '" + serverConfig.getName() + "'@'" + jarFilePath + "' not found.", e);
			} catch (InstantiationException e) {
				log.error(
						"Server class '" + serverConfig.getName() + "' could not be instantiated.", e);
			} catch (IllegalAccessException e) {
				log.error(
						"Illegal Access Exception while intializing server '" + serverConfig.getName() + "'.", e);
			} catch (NoSuchMethodException e) {
				log.error(
						"No constructor found with given 1 arguments", e);
			} catch (InvocationTargetException e) {
				log.error(
						"Exception invoking server object.", e);
			}
		}
		return servers;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, List<WebSocketPlugIn>> initializePlugins() {
		Map<String, List<WebSocketPlugIn>> pluginMap = new HashMap<String, List<WebSocketPlugIn>>();

		// populate the plugin map with server id and empty list
		for (ServerConfig serverConfig : config.getServers()) {
			pluginMap.put(serverConfig.getId(),
					new ArrayList<WebSocketPlugIn>());
		}
		// now initialize the pluin
		for (PluginConfig pluginConfig : config.getPlugins()) {
			try {
				String jarFilePath = getLibraryFolderPath(pluginConfig.getJar());
				classLoader.addFile(jarFilePath);

				if (log.isDebugEnabled()) {
					log.debug("Loading plug-in '" + pluginConfig.getName() + "' from '" + jarFilePath + "'...");
				}
				Class pluginClass = null;
				try {
					pluginClass = Class.forName(pluginConfig.getName());
				} catch (Exception ex) {
					if (log.isDebugEnabled()) {
						log.debug(ex.getClass().getSimpleName()
								+ ": Plug-in not yet in classpath, hence trying to load from file...");
					}
				}
				if (pluginClass == null) {
					pluginClass = (Class<WebSocketPlugIn>) classLoader.loadClass(pluginConfig.getName());
				}
				WebSocketPlugIn plugin = (WebSocketPlugIn) pluginClass.newInstance();
				if (log.isDebugEnabled()) {
					log.debug("Plug-in successfully instantiated.");
				}

				// now add the plugin to plugin map based on server ids
				for (String serverId : pluginConfig.getServers()) {
					pluginMap.get(serverId).add(plugin);
				}

			} catch (MalformedURLException e) {
				log.error(
						"Couldn't load the jar file for plugin, make sure the jar file exists and the name is correct.", e);
			} catch (ClassNotFoundException e) {
				log.error(
						"Plugin class '" + pluginConfig.getName() + "' not found.", e);
			} catch (InstantiationException e) {
				log.error(
						"Plugin class '" + pluginConfig.getName() + "' could not be instantiated.", e);
			} catch (IllegalAccessException e) {
				log.error(
						"Illegal Access Exception while intializing plugin.", e);
			}
		}
		return pluginMap;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, List<WebSocketFilter>> initializeFilters() {
		Map<String, List<WebSocketFilter>> filterMap = new HashMap<String, List<WebSocketFilter>>();

		// populate the filter map with server id and empty list
		for (ServerConfig serverConfig : config.getServers()) {
			filterMap.put(serverConfig.getId(),
					new ArrayList<WebSocketFilter>());
		}
		// now initialize the filter
		for (FilterConfig filterConfig : config.getFilters()) {
			try {
				String jarFilePath = getLibraryFolderPath(filterConfig.getJar());
				classLoader.addFile(jarFilePath);

				if (log.isDebugEnabled()) {
					log.debug("Loading filter '" + filterConfig.getName() + "' from '" + jarFilePath + "'...");
				}
				Class filterClass = null;
				try {
					filterClass = Class.forName(filterConfig.getName());
				} catch (Exception ex) {
					if (log.isDebugEnabled()) {
						log.debug(ex.getClass().getSimpleName()
								+ ": Filter not yet in classpath, hence trying to load from file...");
					}
				}
				if (filterClass == null) {
					filterClass = (Class<WebSocketFilter>) classLoader.loadClass(filterConfig.getName());
				}

				Constructor<WebSocketFilter> ctor = filterClass.getDeclaredConstructor(String.class);
				ctor.setAccessible(true);
				WebSocketFilter filter = ctor.newInstance(new Object[]{filterConfig.getId()});

				if (log.isDebugEnabled()) {
					log.debug("Filter successfully instantiated.");
				}
				// now add the filter to filter map based on server ids
				for (String serverId : filterConfig.getServers()) {
					filterMap.get(serverId).add(filter);
				}

			} catch (MalformedURLException e) {
				log.error(
						"Couldn't Load the jar file for filter, make sure jar file exists and name is correct.",
						e);
			} catch (ClassNotFoundException e) {
				log.error(
						"Filter class not found.", e);
			} catch (InstantiationException e) {
				log.error(
						"Filter class could not be instantiated.", e);
			} catch (IllegalAccessException e) {
				log.error(
						"Illegal Access Exception while intializing filter.", e);
			} catch (NoSuchMethodException e) {
				log.error(
						"No Constructor found with given 3 argument.s", e);
			} catch (InvocationTargetException e) {
				log.error(
						"Exception invoking filter objec.t", e);
			}
		}
		return filterMap;
	}

	/**
	 * private method that checks the path of the jWebSocket.xml file
	 * 
	 * @return the path to jWebSocket.xml
	 */
	private String getLibraryFolderPath(String fileName) {
		// try to obtain JWEBSOCKET_HOME environment variable
		String lWebSocketHome = System.getenv(JWEBSOCKET_HOME);
		String lFileSep = System.getProperty("file.separator");
		String lWebSocketXML = "";
		if (lWebSocketHome != null) {
			// append trailing slash if needed
			if (!lWebSocketHome.endsWith(lFileSep)) {
				lWebSocketHome += lFileSep;
			}
			// jars has to be located in %JWEBSOCKET_HOME%/libs (or some other
			// folder defined in config file)
			lWebSocketXML = lWebSocketHome + "libs" + lFileSep + fileName;
		} else {
			throw new WebSocketRuntimeException(
					"JWEBSOCKET_HOME variable not set");
		}
		return lWebSocketXML;
	}
}
