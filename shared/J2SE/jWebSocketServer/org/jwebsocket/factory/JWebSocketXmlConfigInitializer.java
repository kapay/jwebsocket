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

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.jwebsocket.api.EngineConfiguration;
import org.jwebsocket.api.FilterConfiguration;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.ServerConfiguration;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketFilter;
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
 * @author puran
 * @version $Id: JWebSocketXmlConfigInitializer.java 424 2010-05-01 19:11:04Z mailtopuran $
 */
public class JWebSocketXmlConfigInitializer extends AbstractJWebSocketInitializer {

	private final JWebSocketJarClassLoader mClassLoader = new JWebSocketJarClassLoader();

	/**
	 * private constructor
	 */
	public JWebSocketXmlConfigInitializer(JWebSocketConfig aConfig) {
		super(aConfig);
	}

	/**
	 * Returns the initializer object
	 *
	 * @param aConfig the jWebSocket config
	 * @return the initializer object
	 */
	public static JWebSocketXmlConfigInitializer getInitializer(JWebSocketConfig aConfig) {
		return new JWebSocketXmlConfigInitializer(aConfig);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public WebSocketEngine initializeEngine() {
		WebSocketEngine newEngine = null;
		EngineConfig engineConfig = jWebSocketConfig.getEngines().get(0);
		String jarFilePath = "-";
		try {
			// try to load engine from classpath first,could be located in server bundle
			Class<WebSocketEngine> lEngineClass = loadEngineFromClassPath(engineConfig.getName());
			// in case of a class not found exception we DO NOT want to show the
			// exception but subsequently load the class from the jar file
			if (lEngineClass == null) {
				if (mLog.isDebugEnabled()) {
					mLog.debug("Loading from the jar file '" + engineConfig.getName() + "'  ");
				}
				jarFilePath = JWebSocketConfig.getLibraryFolderPath(engineConfig.getJar());
				// jarFilePath may be null if .jar is included in server bundle
				if (jarFilePath != null) {
					if (mLog.isDebugEnabled()) {
						mLog.debug("Loading engine '" + engineConfig.getName() + "' from '" + jarFilePath + "'...");
					}
					mClassLoader.addFile(jarFilePath);
					lEngineClass = (Class<WebSocketEngine>) mClassLoader.loadClass(engineConfig.getName());
				}
			}
			// if class found try to create an instance
			if (lEngineClass != null) {
				Constructor<WebSocketEngine> ctor = lEngineClass.getDeclaredConstructor(EngineConfiguration.class);
				if (ctor != null) {
					ctor.setAccessible(true);
					newEngine = ctor.newInstance(new Object[]{engineConfig});
				} else {
					newEngine = lEngineClass.newInstance();
					newEngine.setEngineConfiguration(engineConfig);
				}
				if (mLog.isDebugEnabled()) {
					mLog.debug("Engine '" + engineConfig.getId() + "' successfully instantiated.");
				}
			} else {
				mLog.error("jWebSocket engine class " + engineConfig.getName() + " could not be loaded.");
			}
		} catch (Exception es) {
			mLog.error("Error initializing engine based on given configuration. Make sure that you are using correct jar file or "
					+ "engine class is in the classpath", es);
		}
		return newEngine;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<WebSocketServer> initializeServers() {
		List<WebSocketServer> lServers = new FastList<WebSocketServer>();
		List<ServerConfig> lServerConfigs = jWebSocketConfig.getServers();
		for (ServerConfig lServerConfig : lServerConfigs) {
			WebSocketServer lServer = null;
			String lJarFilePath = "-";
			try {
				Class<WebSocketServer> lServerClass = loadServerFromClasspath(lServerConfig.getName());
				// if not in classpath...try to load server from given .jar file
				if (lServerClass == null) {
					if (mLog.isDebugEnabled()) {
						mLog.debug("Load server from the jar file '" + lServerConfig.getName());
					}
					lJarFilePath = JWebSocketConfig.getLibraryFolderPath(lServerConfig.getJar());
					// jarFilePath may be null if .jar is included in server bundle
					if (lJarFilePath != null) {
						if (mLog.isDebugEnabled()) {
							mLog.debug("Loading server '" + lServerConfig.getName() + "' from '" + lJarFilePath + "'...");
						}
						mClassLoader.addFile(lJarFilePath);
						lServerClass = (Class<WebSocketServer>) mClassLoader.loadClass(lServerConfig.getName());
					}
				}
				// if class found try to create an instance
				if (lServerClass != null) {
					Constructor<WebSocketServer> ctor = lServerClass.getDeclaredConstructor(ServerConfiguration.class);
					if (ctor != null) {
						ctor.setAccessible(true);
						lServer = ctor.newInstance(new Object[]{lServerConfig});
					} else {
						lServer = lServerClass.newInstance();
						lServer.setServerConfiguration(lServerConfig);
					}
					if (mLog.isDebugEnabled()) {
						mLog.debug("Server '" + lServerConfig.getId() + "' successfully instantiated.");
					}
					// add the initialized server to the list
					lServers.add(lServer);
				} else {
					mLog.error("jWebSocket server class " + lServerConfig.getName() + " could not be loaded.");
				}
			} catch (Exception es) {
				mLog.error("Error initializing server based on given configuration. Make sure that you are using correct jar file or "
						+ "server class is in the classpath", es);
			}
		}
		return lServers;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, List<WebSocketPlugIn>> initializePlugins() {
		Map<String, List<WebSocketPlugIn>> lPluginMap = new FastMap<String, List<WebSocketPlugIn>>();
		// populate the plugin FastMap with server id and empty list
		for (ServerConfig lServerConfig : jWebSocketConfig.getServers()) {
			lPluginMap.put(lServerConfig.getId(), new FastList<WebSocketPlugIn>());
		}
		// now initialize the plugins
		for (PluginConfig pluginConfig : jWebSocketConfig.getPlugins()) {
			try {
				Class<WebSocketPlugIn> lPluginClass = loadPluginFromClasspath(pluginConfig.getName());
				// if not in classpath..try to load plug-in from given .jar file
				if (lPluginClass == null) {
					if (mLog.isDebugEnabled()) {
						mLog.debug("Plug-in '" + pluginConfig.getName() + "' trying to load from file...");
					}
					String jarFilePath = JWebSocketConfig.getLibraryFolderPath(pluginConfig.getJar());
					// jarFilePath may be null if .jar is included in server bundle
					if (jarFilePath != null) {
						mClassLoader.addFile(jarFilePath);
						if (mLog.isDebugEnabled()) {
							mLog.debug("Loading plug-in '" + pluginConfig.getName() + "' from '" + jarFilePath + "'...");
						}
						lPluginClass = (Class<WebSocketPlugIn>) mClassLoader.loadClass(pluginConfig.getName());
					}
				}
				// if class found try to create an instance
				if (lPluginClass != null) {
					WebSocketPlugIn lPlugIn = null;
					Constructor<WebSocketPlugIn> pluginConstructor = lPluginClass.getConstructor(PluginConfiguration.class);
					if (pluginConstructor != null) {
						pluginConstructor.setAccessible(true);
						lPlugIn = pluginConstructor.newInstance(pluginConfig);
					} else {
						lPlugIn = lPluginClass.newInstance();
						lPlugIn.setPluginConfiguration(pluginConfig);
					}
					lPlugIn.addAllSettings(pluginConfig.getSettings());
					if (mLog.isDebugEnabled()) {
						mLog.debug("Plug-in '" + pluginConfig.getId() + "' successfully instantiated.");
					}
					// now add the plugin to plugin map based on server ids
					for (String lServerId : pluginConfig.getServers()) {
						List<WebSocketPlugIn> lPlugIns = lPluginMap.get(lServerId);
						if (lPlugIns != null) {
							lPlugIns.add((WebSocketPlugIn) lPlugIn);
						}
					}
				}

			} catch (Exception ex) {
				mLog.error("Couldn't instantiate the plugin.", ex);
			}
		}
		return lPluginMap;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, List<WebSocketFilter>> initializeFilters() {
		Map<String, List<WebSocketFilter>> lFilterMap = new FastMap<String, List<WebSocketFilter>>();

		// populate the filter FastMap with server id and empty list
		for (ServerConfig lServerConfig : jWebSocketConfig.getServers()) {
			lFilterMap.put(lServerConfig.getId(), new FastList<WebSocketFilter>());
		}
		// now initialize the filter
		for (FilterConfig lFilterConfig : jWebSocketConfig.getFilters()) {
			try {
				Class<WebSocketFilter> lFilterClass = loadFilterFromClasspath(lFilterConfig.getName());
				// try to load filter from classpath first, could be located in server bundle
				lFilterClass = loadFilterFromClasspath(lFilterConfig.getName());
				if (lFilterClass == null) {
					String jarFilePath = JWebSocketConfig.getLibraryFolderPath(lFilterConfig.getJar());
					// jarFilePath may be null if .jar is included in server bundle
					if (jarFilePath != null) {
						mClassLoader.addFile(jarFilePath);
						if (mLog.isDebugEnabled()) {
							mLog.debug("Loading filter '" + lFilterConfig.getName() + "' from '" + jarFilePath + "'...");
						}
						lFilterClass = (Class<WebSocketFilter>) mClassLoader.loadClass(lFilterConfig.getName());
					}
				}
				if (lFilterClass != null) {
					Constructor<WebSocketFilter> lConstr = lFilterClass.getDeclaredConstructor(FilterConfiguration.class);
					lConstr.setAccessible(true);
					WebSocketFilter lFilter = lConstr.newInstance(new Object[]{lFilterConfig});
					if (mLog.isDebugEnabled()) {
						mLog.debug("Filter '" + lFilterConfig.getName() + "' successfully instantiated.");
					}
					// now add the filter to filter FastMap based on server ids
					for (String lServerId : lFilterConfig.getServers()) {
						List<WebSocketFilter> lFilters = lFilterMap.get(lServerId);
						if (lFilters != null) {
							lFilters.add(lFilter);
						}
					}
				}

			} catch (Exception e) {
				mLog.error("Error instantiating filters", e);
			}
		}
		return lFilterMap;
	}
}
