/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.server.loader;

import java.util.List;
import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketInitializer;
import org.jwebsocket.api.WebSocketServer;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.logging.Logging;

/**
 *
 * @author aschulze
 */
public class JWebSocketStartUp {

	private static Logger log = null;
	private static Logger logger = null; // don't instantiate logger here! first read args!

	public static void start(String aLogLevel, int aLogTarget) {

		// initialize log4j logging engine
		// BEFORE instantiating any jWebSocket classes
		Logging.initLogs(aLogLevel, aLogTarget);
		log = Logging.getLogger(JWebSocketStartUp.class);

		JWebSocketLoader loader = new JWebSocketLoader();

		try {
			WebSocketInitializer initializer = loader.initialize();
			WebSocketEngine engine = loader.loadEngine(initializer);
			List<WebSocketServer> servers = loader.loadServers(initializer, engine);

			//start the engine
			if (log.isDebugEnabled()) {
				log.debug("Starting engine (" + engine.getId() + ")...");
			}
			engine.startEngine();

			//now start the servers
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
			if (logger.isDebugEnabled()) {
				logger.debug("Exception during startup", e);
			}
			if (logger.isInfoEnabled()) {
				logger.info("jWebSocketServer failed to start");
			}
			System.out.println("ERROR during JWebSocketServer startup");
			System.exit(0);
		}

		// log.info("Server(s) successfully terminated.");
	}

	public static void stop() {
/*
		try {
			// stop engine if previously started successfully
			if (engine != null) {
				engine.stopEngine(CloseReason.SHUTDOWN);
			}
		} catch (WebSocketException ex) {
			log.error("Stopping engine: " + ex.getMessage());
		}
		try {
			// stop token server if previously started successfully
			if (tokenServer != null) {
				tokenServer.stopServer();
			}
		} catch (WebSocketException ex) {
			log.error("Stopping TokenServer: " + ex.getMessage());
		}

		try {
			// stop custom server if previously started successfully
			if (customServer != null) {
				customServer.stopServer();
			}
		} catch (WebSocketException ex) {
			log.error("Stopping CustomServer: " + ex.getMessage());
		}
*/
		Logging.exitLogs();
	}

}
