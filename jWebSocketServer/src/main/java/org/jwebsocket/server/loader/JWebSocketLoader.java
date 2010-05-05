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
package org.jwebsocket.server.loader;

import static org.jwebsocket.config.JWebSocketConstants.JWEBSOCKET_HOME;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketInitializer;
import org.jwebsocket.api.WebSocketPlugIn;
import org.jwebsocket.api.WebSocketServer;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.config.xml.JWebSocketConfigHandler;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.kit.WebSocketRuntimeException;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.security.SecurityFactory;

/**
 * An object that does the process of loading configuration, intialization of
 * the jWebSocket server system.
 * 
 * @author puran
 * @version $Id: JWebSocketLoader.java 345 2010-04-10 20:03:48Z fivefeetfurther$
 */
public final class JWebSocketLoader {

	private static Logger log = Logging.getLogger(JWebSocketLoader.class);
	private JWebSocketConfigHandler configHandler = new JWebSocketConfigHandler();

	/**
	 * Initialize the JWebSocket server system
	 * 
	 * @return the initializer object
	 * @throws WebSocketException
	 *             if there's an exception while initialization
	 */
	public final WebSocketInitializer initialize() throws WebSocketException {
		String configPath = getConfigurationPath();
		// load configuration
		JWebSocketConfig config = loadConfiguration(configPath);
		// initialize security
		SecurityFactory.initFromConfig(config);

		WebSocketInitializer initializer = getInitializer(config);
		if (initializer == null) {
			initializer = JWebSocketXmlConfigInitializer.getInitializer(config);
		}
		return initializer;
	}

	/**
	 * Load the engine for the JWebSocket server system.
	 * 
	 * @param initializer
	 *            the initalizer object
	 * @return the loaded engine
	 * @throws WebSocketException
	 *             if exception while loading engine
	 */
	public final WebSocketEngine loadEngine(WebSocketInitializer initializer)
			throws WebSocketException {
		// initialize and start the engine
		if (log.isInfoEnabled()) {
			log.info("Initializing Engine..");
		}
		WebSocketEngine engine = initializer.intializeEngine();
		if (log.isInfoEnabled()) {
			log.info("Engine Initialized");
		}
		return engine;
	}

	/**
	 * Load the different servers on top of loaded engine
	 * 
	 * @param initializer
	 *            the initializer object
	 * @param engine
	 *            the loaded engine on which to run servers
	 * @return the list of loaded and running servers
	 * @throws WebSocketException
	 *             if there's any exception
	 */
	public final List<WebSocketServer> loadServers(
			WebSocketInitializer initializer, WebSocketEngine engine)
			throws WebSocketException {
		// initialize and start the server
		if (log.isInfoEnabled()) {
			log.info("Initializing Servers..");
		}
		List<WebSocketServer> servers = initializer.initializeServers();
		Map<String, List<WebSocketPlugIn>> pluginMap = initializer.initializePlugins();
		for (WebSocketServer server : servers) {
			server.addEngine(engine);
			List<WebSocketPlugIn> plugins = pluginMap.get(server.getId());
			if (log.isInfoEnabled()) {
				log.info("Initializing Server Plugins..");
			}
			for (WebSocketPlugIn plugin : plugins) {
				server.getPlugInChain().addPlugIn(plugin);
			}
		}
		if (log.isInfoEnabled()) {
			log.info("Servers Initialized with plugins");
		}
		return servers;
	}

	/**
	 * Returns the appropriate {@code WebSocketInitializer} implementation
	 * 
	 * @param config
	 *            the config object
	 * @return the {@code WebSocketInitializer} object
	 */
	private WebSocketInitializer getInitializer(JWebSocketConfig config) {
		WebSocketInitializer initializer = null;
		if ("dev".equals(config.getInstallation())) {
			initializer = instantiateInitializer(config.getInitializer());
		} else if ("prod".equals(config.getInstallation())) {
			initializer = JWebSocketXmlConfigInitializer.getInitializer(config);
		} else {
			// ignore
		}
		return initializer;
	}

	/**
	 * Instantiate the initializer custom initializer class if there's any
	 * configured via xml configuration, otherwise return the default {@code
	 * JWebSocketInitializer} class that initialize all the default engine,
	 * plugins and servers.
	 * 
	 * @param initializerClass
	 *            the class name to instantiate
	 * @return the instantiated initializer object
	 */
	@SuppressWarnings("unchecked")
	private WebSocketInitializer instantiateInitializer(String initializerClass) {
		WebSocketInitializer initializer = null;
		try {
			Class<WebSocketInitializer> clz = (Class<WebSocketInitializer>) Class.forName(initializerClass);
			initializer = clz.newInstance();
		} catch (ClassNotFoundException e) {
			log.error("Error instantiating initializer", e);
		} catch (InstantiationException e) {
			log.error("Error instantiating initializer", e);
		} catch (IllegalAccessException e) {
			log.error("Error instantiating initializer", e);
		}
		if (log.isInfoEnabled()) {
			log.info("Initializer Found:" + initializer);
		}
		return initializer;
	}

	/**
	 * Load all the configurations based on jWebSocket.xml file at the given
	 * <tt>configFilePath</tt> location.
	 * 
	 * @param configFilePath
	 *            the path to jWebSocket.xml file
	 * @return the web socket config object with all the configuration
	 * @throws WebSocketException
	 *             if there's any while loading configuration
	 */
	private JWebSocketConfig loadConfiguration(final String configFilePath)
			throws WebSocketException {
		JWebSocketConfig config = null;
		File lFile = new File(configFilePath);
		String lMsg;
		try {
			FileInputStream fis = new FileInputStream(lFile);
			XMLInputFactory factory = XMLInputFactory.newInstance();
			XMLStreamReader streamReader = null;
			streamReader = factory.createXMLStreamReader(fis);
			config = configHandler.processConfig(streamReader);
		} catch (XMLStreamException ex) {
			lMsg = "Exception occurred while creating XML stream";
			if (log.isDebugEnabled()) {
				log.debug(lMsg);
			}
			throw new WebSocketException(lMsg);
		} catch (FileNotFoundException ex) {
			lMsg = "jWebSocket config not found while creating XML stream";
			if (log.isDebugEnabled()) {
				log.debug(lMsg);
			}
			throw new WebSocketException(lMsg);
		}
		return config;
	}

	/**
	 * private method that checks the path of the jWebSocket.xml file
	 * 
	 * @return the path to jWebSocket.xml
	 */
	private String getConfigurationPath() {

		String lFileSep = System.getProperty("file.separator");

		// first try to get the xml file from the classpath
		String lPath = /*".." + lFileSep + "conf" + lFileSep +*/ "jWebSocket.xml";
		URL lURL = Thread.currentThread().getContextClassLoader().getResource(lPath);
		String lWebSocketXML = null;
		if (log.isDebugEnabled()) {
			log.debug("Trying to load configuration from " + lPath + " in class path (" + lURL + ")...");
		}
		if (lURL != null) {
			lWebSocketXML = lURL.getFile();
			if (lWebSocketXML != null) {
				return lWebSocketXML;
			}
		}

		// try to obtain JWEBSOCKET_HOME environment variable
		String lWebSocketHome = System.getenv(JWEBSOCKET_HOME);
		if (log.isDebugEnabled()) {
			log.debug("Trying to load configuration from %JWEBSOCKET_HOME% (" + lWebSocketHome + ")...");
		}
		if (lWebSocketHome != null) {
			// append trailing slash if needed
			if (!lWebSocketHome.endsWith(lFileSep)) {
				lWebSocketHome += lFileSep;
			}
			// jWebSocket.xml has to be located in %JWEBSOCKET_HOME%/conf
			lWebSocketXML = lWebSocketHome + "conf" + lFileSep
					+ "jWebSocket.xml";
		} else {
			throw new WebSocketRuntimeException(
					"JWEBSOCKET_HOME variable not set");
		}
		return lWebSocketXML;
	}
}
