//	---------------------------------------------------------------------------
//	jWebSocket - Context Listener for Web Applications
//	Copyright (c) 2010 jWebSocket.org, Alexander Schulze, Innotrade GmbH
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
package org.jwebsocket.appserver;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.config.JWebSocketConstants;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.api.WebSocketPlugInChain;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.flashbridge.FlashBridgePlugIn;
import org.jwebsocket.plugins.rpc.RPCPlugIn;
import org.jwebsocket.plugins.streaming.MonitorStream;
import org.jwebsocket.plugins.streaming.StreamingPlugIn;
import org.jwebsocket.plugins.streaming.TimeStream;
import org.jwebsocket.plugins.system.SystemPlugIn;
import org.jwebsocket.security.SecurityFactory;
import org.jwebsocket.server.CustomServer;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.tcp.engines.TCPEngine;

/**
 * Web application lifecycle listener.
 * @author aschulze
 */
public class ContextListener implements ServletContextListener {

	TokenServer tokenServer = null;
	CustomServer customServer = null;
	private static Logger log = null;
	WebSocketEngine engine = null;

	/**
	 * initializes the web application on startup.
	 * @param sce
	 */
	@Override
	public void contextInitialized(ServletContextEvent sce) {

		Logging.initLogs("debug", Logging.CONSOLE /*ROLLING_FILE*/);
		log = Logging.getLogger(ContextListener.class);
		if (log.isDebugEnabled()) {
			log.debug("Initialising Context...");
		}

		// init the security settings
		SecurityFactory.init();

		// create the low-level engine
		try {
			// TODO: find solutions for hardcoded engine id, refer to RPCPlugIn!
			engine = new TCPEngine("tcp0", JWebSocketConstants.DEFAULT_PORT, JWebSocketConstants.DEFAULT_TIMEOUT);
			// engine = new NettyEngine("tcp0", JWebSocketConstants.DEFAULT_PORT, JWebSocketConstants.DEFAULT_TIMEOUT);
			engine.startEngine();
		} catch (Exception ex) {
			log.error("Error instantating engine: " + ex.getMessage());
			return;
		}

		StreamingPlugIn streamingPlugIn = null;

		// create the token server (based on the TCP engine)
		tokenServer = null;
		try {
			// instantiate the Token server and bind engine to it
			tokenServer = new TokenServer("ts0");
			// the token server already instantiates a plug-in chain
			WebSocketPlugInChain plugInChain = tokenServer.getPlugInChain();
			// let the server support the engine
			tokenServer.addEngine(engine);
			// add the SystemPlugIn listener (for the jWebSocket default functionality)
			plugInChain.addPlugIn(new SystemPlugIn());
			// add the RPCPlugIn plug-in
			plugInChain.addPlugIn(new RPCPlugIn());
			// add the streaming plug-in (e.g. for the time stream demo)
			plugInChain.addPlugIn(streamingPlugIn = new StreamingPlugIn());
			// add the flash/bridge plug-in (to drive browser that don't yet support web sockets)
			plugInChain.addPlugIn(new FlashBridgePlugIn());

			if (log.isDebugEnabled()) {
				log.debug("Starting token server...");
			}
			tokenServer.startServer();
		} catch (Exception ex) {
			log.error("Error instantiating TokenServer: " + ex.getMessage());
		}

		// initialize streaming sub system...
		if (streamingPlugIn != null) {
			// create the stream for the time stream demo
			TimeStream lTimeStream = new TimeStream("timeStream", tokenServer);
			streamingPlugIn.addStream(lTimeStream);
			// create the stream for the monitor stream demo
			MonitorStream lMonitorStream = new MonitorStream("monitorStream", tokenServer);
			streamingPlugIn.addStream(lMonitorStream);
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
			if (log.isDebugEnabled()) {
				log.debug("Starting custom server...");
			}
			customServer.startServer();
		} catch (Exception ex) {
			log.error("Error instantating CustomServer: " + ex.getMessage());
		}

		WebSocketComm.setServer(tokenServer);
	}

	/**
	 * cleans up the web application on termination.
	 * @param sce
	 */
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
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

		Logging.exitLogs();
	}
}
