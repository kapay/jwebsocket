//	---------------------------------------------------------------------------
//	jWebSocket - Copyright (c) 2010 jwebsocket.org
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
package org.jwebsocket.factory;

import java.io.File;
import org.apache.log4j.Logger;
import org.jwebsocket.config.xml.LoggingConfig;
import org.jwebsocket.logging.Logging;
import static org.jwebsocket.config.JWebSocketConstants.JWEBSOCKET_HOME;
import static org.jwebsocket.config.JWebSocketConstants.CATALINA_HOME;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jwebsocket.api.EngineConfiguration;
import org.jwebsocket.api.ServerConfiguration;
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

	// don't initialize logger here! Will be initialized with loaded settings!
	private static Logger log = null; // Logging.getLogger(JWebSocketXmlConfigInitializer.class);
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
	@Override
	public void initializeLogging() {
		LoggingConfig loggingConfig = config.getLoggingConfig();
		// initialize log4j logging engine
		// BEFORE instantiating any jWebSocket classes
		Logging.initLogs(loggingConfig.getLevel(), loggingConfig.getAppender(),
				loggingConfig.getFilename(), loggingConfig.getPattern(),
				loggingConfig.getBufferSize());
		log = Logging.getLogger(JWebSocketXmlConfigInitializer.class);
		if (log.isDebugEnabled()) {
			log.debug("Logging settings"
					+ ": appender: " + loggingConfig.getAppender()
					+ ", filename: " + loggingConfig.getFilename()
					+ ", level: " + loggingConfig.getLevel()
					+ ", buffersize: " + loggingConfig.getBufferSize()
					+ ", pattern: " + loggingConfig.getPattern());
		}
		if (log.isDebugEnabled()) {
			log.debug("Starting jWebSocket Server Sub System...");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public WebSocketEngine initializeEngine() {
		WebSocketEngine newEngine = null;
		EngineConfig engineConfig = config.getEngines().get(0);
		String jarFilePath = "-";
		try {
			Class engineClass = null;

			// try to load engine from classpath first, 
			// could be located in server bundle
			try {
				engineClass = Class.forName(engineConfig.getName());
				if (log.isDebugEnabled()) {
					log.debug("Engine '" + engineConfig.getName() + "' loaded from classpath.");
				}
			} catch (ClassNotFoundException ex) {
				// in case of a class not found exception we DO NOT want to
				// show the exception but subsequently load the class from
				if (log.isDebugEnabled()) {
					log.debug("Engine '" + engineConfig.getName() + "' not yet in classpath, hence trying to load from file...");
				}
			}

			// if not in classpath...
			// try to load engine from given .jar file
			if (engineClass == null) {
				jarFilePath = getLibraryFolderPath(engineConfig.getJar());
				// jarFilePath may be null if .jar is included in server bundle
				if (jarFilePath != null) {
					if (log.isDebugEnabled()) {
						log.debug("Loading engine '" + engineConfig.getName() + "' from '" + jarFilePath + "'...");
					}
					classLoader.addFile(jarFilePath);
					engineClass = (Class<WebSocketEngine>) classLoader.loadClass(engineConfig.getName());
				}
			}

			// if class found
			// try to create an instance
			if (engineClass != null) {
				Constructor<WebSocketEngine> ctor = engineClass.getDeclaredConstructor(EngineConfiguration.class);
				ctor.setAccessible(true);
				newEngine = ctor.newInstance(new Object[]{engineConfig});
				if (log.isDebugEnabled()) {
					log.debug("Engine '" + engineConfig.getId() + "' successfully instantiated.");
				}
			} else {
				log.error("jWebSocket engine class " + engineConfig.getName() + " could not be loaded.");
			}
		} catch (MalformedURLException e) {
			log.error("Couldn't load the jar file for engine, make sure jar file exists or name is correct", e);
		} catch (ClassNotFoundException e) {
			log.error("Engine class '" + engineConfig.getName() + "'@'" + jarFilePath + "' not found", e);
		} catch (InstantiationException e) {
			log.error("Engine class could not be instantiated", e);
		} catch (IllegalAccessException e) {
			log.error("Illegal Access Exception while intializing engine", e);
		} catch (NoSuchMethodException e) {
			log.error("No Constructor found with given 3 arguments", e);
		} catch (InvocationTargetException e) {
			log.error("Exception invoking engine object", e);
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
				Class serverClass = null;

				// try to load server from classpath first,
				// could be located in server bundle
				try {
					serverClass = Class.forName(serverConfig.getName());
					if (log.isDebugEnabled()) {
						log.debug("Server '" + serverConfig.getName() + "' loaded from classpath.");
					}
				} catch (ClassNotFoundException ex) {
					// in case of a class not found exception we DO NOT want to
					// show the exception but subsequently load the class from
					if (log.isDebugEnabled()) {
						log.debug("Server '" + serverConfig.getName() + "' not yet in classpath, hence trying to load from file...");
					}
				}

				// if not in classpath...
				// try to load server from given .jar file
				if (serverClass == null) {
					jarFilePath = getLibraryFolderPath(serverConfig.getJar());
					// jarFilePath may be null if .jar is included in server bundle
					if (jarFilePath != null) {
						if (log.isDebugEnabled()) {
							log.debug("Loading server '" + serverConfig.getName() + "' from '" + jarFilePath + "'...");
						}
						classLoader.addFile(jarFilePath);
						serverClass = (Class<WebSocketServer>) classLoader.loadClass(serverConfig.getName());
					}
				}

				// if class found
				// try to create an instance
				if (serverClass != null) {
					Constructor<WebSocketServer> ctor = serverClass.getDeclaredConstructor(ServerConfiguration.class);
					ctor.setAccessible(true);
					server = ctor.newInstance(new Object[]{serverConfig});
					if (log.isDebugEnabled()) {
						log.debug("Server '" + serverConfig.getId() + "' successfully instantiated.");
					}
					// add the initialized server to the list
					servers.add(server);
				} else {
					log.error("jWebSocket server class " + serverConfig.getName() + " could not be loaded.");
				}
			} catch (MalformedURLException e) {
				log.error(
						"Couldn't load the jar file for server, make sure jar file '" + jarFilePath + "' exists and name is correct.",
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
				Class pluginClass = null;

				// try to load plug-in from classpath first,
				// could be located in server bundle
				try {
					pluginClass = Class.forName(pluginConfig.getName());
					if (log.isDebugEnabled()) {
						log.debug("Plug-in '" + pluginConfig.getName() + "' loaded from classpath.");
					}
				} catch (ClassNotFoundException ex) {
					// in case of a class not found exception we DO NOT want to
					// show the exception but subsequently load the class from
					if (log.isDebugEnabled()) {
						log.debug("Plug-in '" + pluginConfig.getName() + "' not yet in classpath, hence trying to load from file...");
					}
				}

				// if not in classpath...
				// try to load plug-in from given .jar file
				if (pluginClass == null) {
					String jarFilePath = getLibraryFolderPath(pluginConfig.getJar());
					// jarFilePath may be null if .jar is included in server bundle
					if (jarFilePath != null) {
						classLoader.addFile(jarFilePath);
						if (log.isDebugEnabled()) {
							log.debug("Loading plug-in '" + pluginConfig.getName() + "' from '" + jarFilePath + "'...");
						}
						pluginClass = (Class<WebSocketPlugIn>) classLoader.loadClass(pluginConfig.getName());
					}
				}

				// if class found
				// try to create an instance
				if (pluginClass != null) {
					/*
					Constructor<WebSocketPlugIn> ctor = pluginClass.getDeclaredConstructor();
					ctor.setAccessible(true);
					Object lObj = ctor.newInstance(new Object[]{});
					log.debug("lObj.classname = " + lObj.getClass().getName());
					Object plugin = null;
					try {
					plugin = lObj;
					log.info(
					"lObj instanceof WebSocketPlugIn " + ( lObj instanceof WebSocketPlugIn ? "YES" : "NO" )
					);
					} catch (Exception ex) {
					log.error(
					ex.getClass().getSimpleName() + " while instantiating class '" + pluginConfig.getName() + "'.");
					}
					 */
					WebSocketPlugIn plugin = (WebSocketPlugIn) pluginClass.newInstance();

					if (log.isDebugEnabled()) {
						log.debug("Plug-in '" + pluginConfig.getId() + "' successfully instantiated.");
					}

					// now add the plugin to plugin map based on server ids
					for (String serverId : pluginConfig.getServers()) {
						pluginMap.get(serverId).add((WebSocketPlugIn) plugin);
					}
				}

			} catch (MalformedURLException ex) {
				log.error(
						"Couldn't load the jar file for plugin, make sure the jar file exists and the name is correct.", ex);
			} catch (ClassNotFoundException ex) {
				log.error(
						"Plugin class '" + pluginConfig.getName() + "' not found.", ex);
			} catch (InstantiationException ex) {
				log.error(
						"Plugin class '" + pluginConfig.getName() + "' could not be instantiated.", ex);
			} catch (IllegalAccessException ex) {
				log.error(
						"Illegal Access Exception while instantiating plugin.", ex);
			} catch (Exception ex) {
				log.error(
						ex.getClass().getSimpleName() + " while instantiating plugin '" + pluginConfig.getName() + "'.", ex);
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
				Class filterClass = null;

				// try to load filter from classpath first,
				// could be located in server bundle
				try {
					filterClass = Class.forName(filterConfig.getName());
					if (log.isDebugEnabled()) {
						log.debug("Filter '" + filterConfig.getName() + "' loaded from classpath.");
					}
				} catch (ClassNotFoundException ex) {
					// in case of a class not found exception we DO NOT want to
					// show the exception but subsequently load the class from
					if (log.isDebugEnabled()) {
						log.debug("Filter '" + filterConfig.getName() + "' not yet in classpath, hence trying to load from file...");
					}
				}

				// if not in classpath...
				// try to load plug-in from given .jar file
				if (filterClass == null) {
					String jarFilePath = getLibraryFolderPath(filterConfig.getJar());
					// jarFilePath may be null if .jar is included in server bundle
					if (jarFilePath != null) {
						classLoader.addFile(jarFilePath);
						if (log.isDebugEnabled()) {
							log.debug("Loading filter '" + filterConfig.getName() + "' from '" + jarFilePath + "'...");
						}
						filterClass = (Class<WebSocketFilter>) classLoader.loadClass(filterConfig.getName());
					}
				}

				// if class found
				// try to create an instance
				if (filterClass != null) {
					Constructor<WebSocketFilter> ctor = filterClass.getDeclaredConstructor(String.class);
					ctor.setAccessible(true);
					WebSocketFilter filter = ctor.newInstance(new Object[]{filterConfig.getId()});
					if (log.isDebugEnabled()) {
						log.debug("Filter '" + filterConfig.getName() + "' successfully instantiated.");
					}
					// now add the filter to filter map based on server ids
					for (String serverId : filterConfig.getServers()) {
						filterMap.get(serverId).add(filter);
					}
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
		String lWebSocketLib = null;
		String lWebSocketHome = null;
		String lFileSep = null;
		File lFile = null;

		// try to load lib from %JWEBSOCKET_HOME%/libs folder
		lWebSocketHome = System.getenv(JWEBSOCKET_HOME);
		lFileSep = System.getProperty("file.separator");
		if (lWebSocketHome != null) {
			// append trailing slash if needed
			if (!lWebSocketHome.endsWith(lFileSep)) {
				lWebSocketHome += lFileSep;
			}
			// jar can to be located in %JWEBSOCKET_HOME%/libs
			lWebSocketLib = lWebSocketHome + "libs" + lFileSep + fileName;
			lFile = new File(lWebSocketLib);
			if (lFile.exists()) {
				if (log.isDebugEnabled()) {
					log.debug("Loading " + lWebSocketLib + "...");
				}
				return lWebSocketLib;
			} else {
				if (log.isDebugEnabled()) {
					log.debug(fileName + " not found at %" + JWEBSOCKET_HOME + "%/libs.");
				}
			}
		}

		// try to load lib from %CATALINA_HOME%/libs folder
		lWebSocketHome = System.getenv(CATALINA_HOME);
		lFileSep = System.getProperty("file.separator");
		if (lWebSocketHome != null) {
			// append trailing slash if needed
			if (!lWebSocketHome.endsWith(lFileSep)) {
				lWebSocketHome += lFileSep;
			}
			// jars can to be located in %CATALINA_HOME%/lib
			lWebSocketLib = lWebSocketHome + "lib" + lFileSep + fileName;
			lFile = new File(lWebSocketLib);
			if (lFile.exists()) {
				if (log.isDebugEnabled()) {
					log.debug("Loading " + lWebSocketLib + "...");
				}
				return lWebSocketLib;
			} else {
				if (log.isDebugEnabled()) {
					log.debug(fileName + " not found at %" + CATALINA_HOME + "/lib%.");
				}
			}
		}

		return null;
	}
}
