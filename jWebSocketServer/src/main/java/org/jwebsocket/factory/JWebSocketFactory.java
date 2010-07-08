/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.factory;

import java.util.List;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketFilter;
import org.jwebsocket.api.WebSocketInitializer;
import org.jwebsocket.api.WebSocketPlugIn;
import org.jwebsocket.api.WebSocketServer;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.logging.Logging;

/**
 * Factory to initialize and start the jWebSocket components
 * @author aschulze
 * @version $Id:$
 */
public class JWebSocketFactory {

  // don't instantiate logger here! first read args!
	private static Logger log = null; 
	private static WebSocketEngine engine = null;
	private static List<WebSocketServer> servers = null;

	public static void start() {

		JWebSocketLoader loader = new JWebSocketLoader();
		try {
			WebSocketInitializer initializer = loader.initialize();
			initializer.initializeLogging();

			log = Logging.getLogger(JWebSocketFactory.class);
			if (log.isDebugEnabled()) {
				log.debug("Starting jWebSocket Server Sub System...");
			}
			if (initializer == null) {
				log.error("jWebSocket Server sub system could not be initialized.");
				return;
			}

			engine = initializer.initializeEngine();
			if (engine == null) {
				// the loader already logs an error!
				return;
			}

			// initialize and start the server
			if (log.isDebugEnabled()) {
				log.debug("Initializing servers...");
			}
			servers = initializer.initializeServers();

			FastMap<String, List<WebSocketPlugIn>> pluginFastMap = initializer.initializePlugins();
			if (log.isDebugEnabled()) {
				log.debug("Initializing plugins...");
			}
			for (WebSocketServer server : servers) {
				server.addEngine(engine);
				List<WebSocketPlugIn> plugins = pluginFastMap.get(server.getId());
				for (WebSocketPlugIn plugin : plugins) {
					server.getPlugInChain().addPlugIn(plugin);
				}
			}
			if (log.isInfoEnabled()) {
				log.info("Plugins initialized.");
			}

			FastMap<String, List<WebSocketFilter>> filterFastMap = initializer.initializeFilters();
			if (log.isDebugEnabled()) {
				log.debug("Initializing filters...");
			}
			for (WebSocketServer server : servers) {
				server.addEngine(engine);
				List<WebSocketFilter> filters = filterFastMap.get(server.getId());
				for (WebSocketFilter filter : filters) {
					server.getFilterChain().addFilter(filter);
				}
			}
			if (log.isInfoEnabled()) {
				log.info("Filters initialized.");
			}

			boolean engineStarted = false;

			// first start the engine
			if (log.isDebugEnabled()) {
				log.debug("Starting engine '" + engine.getId() + "'...");
			}

			try {
				engine.startEngine();
				engineStarted = true;
			} catch (Exception ex) {
				log.error("Starting engine '" + engine.getId() + "' failed ("
						+ ex.getClass().getSimpleName() + ": "
						+ ex.getMessage() + ").");
			}

			// do not start any servers if engine could not be started
			if (engineStarted) {
				// now start the servers
				if (log.isDebugEnabled()) {
					log.debug("Starting servers...");
				}
				for (WebSocketServer server : servers) {
					try {
						server.startServer();
					} catch (Exception ex) {
						log.error("Starting server '" + server.getId() + "' failed ("
								+ ex.getClass().getSimpleName() + ": "
								+ ex.getMessage() + ").");
					}
				}

				if (log.isInfoEnabled()) {
					log.info("jWebSocket server startup complete");
				}
			}

		} catch (WebSocketException ex) {
			if (log != null) {
				if (log.isDebugEnabled()) {
					log.debug("Exception during startup", ex);
				}
			} else {
				System.out.println(ex.getClass().getSimpleName() + " during jWebSocket Server startup: " + ex.getMessage());
			}
			if (log != null && log.isInfoEnabled()) {
				log.info("jWebSocketServer failed to start.");
			}
		}
	}

	public static void stop() {
		if (log != null && log.isDebugEnabled()) {
			log.debug("Stopping jWebSocket Sub System...");
		}

		// String lEngineId = "?";
		// stop engine if previously started successfully
		if (engine != null) {
			// now stop the servers
			if (log != null && log.isDebugEnabled()) {
				log.debug("Stopping engine...");
			}
			try {
				engine.stopEngine(CloseReason.SHUTDOWN);
				if (log != null && log.isInfoEnabled()) {
					log.info("jWebSocket engine '" + engine.getId() + "' stopped");
				}
			} catch (WebSocketException ex) {
				if (log != null) {
					log.error("Stopping engine: " + ex.getMessage());
				}
			}
		}

		if (servers != null) {
			// now stop the servers
			if (log != null && log.isDebugEnabled()) {
				log.debug("Stopping servers...");
			}
			for (WebSocketServer server : servers) {
				try {
					server.stopServer();
					if (log != null && log.isInfoEnabled()) {
						log.info("jWebSocket server '" + server.getId() + "' stopped");
					}
				} catch (WebSocketException ex) {
					if (log != null) {
						log.error("Stopping server: " + ex.getMessage());
					}
				}
			}
		}

		if (log != null && log.isInfoEnabled()) {
			log.info("jWebSocket Server Sub System stopped.");
		}
		Logging.exitLogs();
	}

	public static WebSocketEngine getEngine() {
		return engine;
	}

	public static List<WebSocketServer> getServers() {
		return servers;
	}

	/**
	 * Returns the server identified by it's id or <tt>null</tt> if no server
	 * with that id could be found in the factory.
	 * @param aId id of the server to be returned.
	 * @return WebSocketServer with the given id or <tt>null</tt> if not found.
	 */
	public static WebSocketServer getServer(String aId) {
		if (aId != null && servers != null) {
			for (WebSocketServer lServer : servers) {
				if (lServer != null && aId.equals(lServer.getId())) {
					return lServer;
				}
			}
		}
		return null;
	}
}
