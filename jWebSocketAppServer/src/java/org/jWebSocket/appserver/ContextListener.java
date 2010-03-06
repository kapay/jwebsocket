/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jWebSocket.appserver;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.log4j.Logger;
import org.jWebSocket.api.WebSocketEngine;
import org.jWebSocket.config.Config;
import org.jWebSocket.engines.TCPEngine;
import org.jWebSocket.kit.WebSocketException;
import org.jWebSocket.logging.Logging;
import org.jWebSocket.plugins.TokenPlugInChain;
import org.jWebSocket.plugins.rpc.RPCPlugIn;
import org.jWebSocket.plugins.streaming.StreamingPlugIn;
import org.jWebSocket.plugins.system.SystemPlugIn;
import org.jWebSocket.server.TokenServer;
import org.jWebsocket.server.CustomServer;

/**
 * Web application lifecycle listener.
 * @author aschulze
 */
public class ContextListener implements ServletContextListener {

	private TokenServer tokenServer = null;
	private CustomServer customServer = null;
	private static Logger log = null;

	@Override
	public void contextInitialized(ServletContextEvent sce) {

		Logging.initLogs("debug");
		log = Logger.getLogger(ContextListener.class);
		log.debug("Initialising Context...");

		// create the low-level engine
		WebSocketEngine engine = null;
		try {
			engine = new TCPEngine("tcp0", Config.DEFAULT_PORT, Config.DEFAULT_TIMEOUT);
		} catch (Exception ex) {
			log.error("Instantating engine: " + ex.getMessage());
			return;
		}

		// create the token server (based on the TCP engine)
		try {
			// instantiate the Token server and bind engine to it
			tokenServer = new TokenServer("ts0");
			// the token server already instantiates a plug-in chain
			TokenPlugInChain plugInChain = tokenServer.getPlugInChain();
			// let the server support the engine
			tokenServer.addEngine(engine);
			// add the SystemPlugIn listener (for the jWebSocket default functionality)
			plugInChain.addPlugIn(new SystemPlugIn());
			// add the RPCPlugIn plug-in
			plugInChain.addPlugIn(new RPCPlugIn());
			// add the streaming plug-in (e.g. for the time stream demo)
			plugInChain.addPlugIn(new StreamingPlugIn());

			log.info("Starting token server...");
			tokenServer.startServer();
		} catch (Exception ex) {
			log.error("Instantiating TokenServer: " + ex.getMessage());
		}

		// create the custom server (based on the TCP engine as well)
		try {
			// instantiate the custom server and bind engine to it
			customServer = new CustomServer("cs0");
			// the custom server already instantiates a plug-in chain
			// BasePlugInChain plugInChain = customServer.getPlugInChain();
			// let the server support the engine
			customServer.addEngine(engine);
			// add the SystemPlugIn listener (for the jWebSocket default functionality)
			// customServer.addPlugIn(new SystemPlugIn());
			log.info("Starting custom server...");
			customServer.startServer();
		} catch (Exception ex) {
			log.error("Instantating CustomServer: " + ex.getMessage());
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		try {
			tokenServer.stopServer();
		} catch (WebSocketException ex) {
			log.error("Stopping TokenServer: " + ex.getMessage());
		}
		try {
			customServer.stopServer();
		} catch (WebSocketException ex) {
			log.error("Stopping CustomServer: " + ex.getMessage());
		}
	}
}
