/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.appserver;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.config.Config;
import org.jwebsocket.tcp.engines.TCPEngine;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugInChain;
import org.jwebsocket.plugins.rpc.RPCPlugIn;
import org.jwebsocket.plugins.streaming.StreamingPlugIn;
import org.jwebsocket.plugins.system.SystemPlugIn;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.server.CustomServer;

/**
 * Web application lifecycle listener.
 * @author aschulze
 */
public class ContextListener implements ServletContextListener {

	TokenServer tokenServer = null;
	CustomServer customServer = null;
	private static Logger log = null;

	/**
	 *
	 * @param sce
	 */
	@Override
	public void contextInitialized(ServletContextEvent sce) {

		Logging.initLogs("debug");
		log = Logger.getLogger(ContextListener.class);
		log.debug("Initialising Context...");

		// create the low-level engine
		WebSocketEngine engine = null;
		try {
			// TODO: find solutions for hardcoded engine id, refer to RPCPlugIn!
			engine = new TCPEngine("tcp0", Config.DEFAULT_PORT, Config.DEFAULT_TIMEOUT);
			engine.startEngine();
		} catch (Exception ex) {
			System.out.println("Error instantating engine: " + ex.getMessage());
			return;
		}

		// create the token server (based on the TCP engine)
		tokenServer = null;
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

			log.debug("Starting token server...");
			tokenServer.startServer();
		} catch (Exception ex) {
			System.out.println("Error instantiating TokenServer: " + ex.getMessage());
		}

		// create the custom server (based on the TCP engine as well)
		customServer = null;
		try {
			// instantiate the custom server and bind engine to it
			customServer = new CustomServer("cs0");
			// the custom server already instantiates a plug-in chain
			// BasePlugInChain plugInChain = customServer.getPlugInChain();
			// let the server support the engine
			customServer.addEngine(engine);
			// add the SystemPlugIn listener (for the jWebSocket default functionality)
			// customServer.addPlugIn(new SystemPlugIn());
			log.debug("Starting custom server...");
			customServer.startServer();
		} catch (Exception ex) {
			System.out.println("Error instantating CustomServer: " + ex.getMessage());
		}
	}

	/**
	 *
	 * @param sce
	 */
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
