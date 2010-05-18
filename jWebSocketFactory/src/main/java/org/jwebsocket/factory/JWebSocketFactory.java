/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.factory;

import java.util.List;
import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketInitializer;
import org.jwebsocket.api.WebSocketServer;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.logging.Logging;

/**
 *
 * @author aschulze
 */
public class JWebSocketFactory {

	private static Logger log = null; // don't instantiate logger here! first read args!
	private static WebSocketEngine engine = null;

	// TODO: makes servers a map for faster access!
	private static List<WebSocketServer> servers = null;

	public static void start(String aLogLevel, int aLogTarget) {
		// initialize log4j logging engine
		// BEFORE instantiating any jWebSocket classes
		Logging.initLogs(aLogLevel, aLogTarget);
		log = Logging.getLogger(JWebSocketFactory.class);

		if (log.isDebugEnabled()) {
			log.debug("Starting jWebSocket sub system...");
		}

		JWebSocketLoader loader = new JWebSocketLoader();
		try {
			WebSocketInitializer initializer = loader.initialize();
			engine = loader.loadEngine(initializer);
			servers = loader.loadServers(initializer, engine);

			// start the engine
			if (log.isDebugEnabled()) {
				log.debug("Starting engine (" + engine.getId() + ")...");
			}
			engine.startEngine();

			// now start the servers
			if (log.isDebugEnabled()) {
				log.debug("Starting servers...");
			}
			for (WebSocketServer server : servers) {
				server.startServer();
			}
			if (log.isInfoEnabled()) {
				log.info("jWebSocket server startup complete");
			}
			// perform any clean up task for servers or any status related

		} catch (WebSocketException e) {
			if (log.isDebugEnabled()) {
				log.debug("Exception during startup", e);
			}
			if (log.isInfoEnabled()) {
				log.info("jWebSocketServer failed to start");
			}
			// System.out.println("ERROR during JWebSocketServer startup");
			// TODO: return result here, especially to show in console server
			// System.exit(0);
		}

		// log.info("Server(s) successfully terminated.");
	}

	public static void stop() {
		if (log.isDebugEnabled()) {
			log.debug("Stopping jWebSocket sub system...");
		}

		// stop the engine
		if (log.isDebugEnabled()) {
			log.debug("Stopping engine...");
		}
		try {
			// stop engine if previously started successfully
			if (engine != null) {
				engine.stopEngine(CloseReason.SHUTDOWN);
			}
			if (log.isInfoEnabled()) {
				log.info("jWebSocket engine '" + engine.getId() + "' stopped");
			}
		} catch (WebSocketException ex) {
			log.error("Stopping engine: " + ex.getMessage());
		}

		// now stop the servers
		if (log.isDebugEnabled()) {
			log.debug("Stopping servers...");
		}
		for (WebSocketServer server : servers) {
			try {
				server.stopServer();
				if (log.isInfoEnabled()) {
					log.info("jWebSocket server '" + server.getId() + "' stopped");
				}
			} catch (WebSocketException ex) {
			}
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
		if (aId != null) {
			for (WebSocketServer lServer : servers) {
				if (lServer != null && aId.equals(lServer.getId())) {
					return lServer;
				}
			}
		}
		return null;
	}
}
