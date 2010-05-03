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
package org.jwebsocket.console;

import java.util.List;

import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketInitializer;
import org.jwebsocket.api.WebSocketServer;
import org.jwebsocket.config.JWebSocketConstants;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.server.loader.JWebSocketLoader;

/**
 * @author puran
 * @version $Id$
 *
 */
public class StartJWebSocket {
	private static Logger log = null;
	private static Logger logger = Logging.getLogger(StartJWebSocket.class);

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		//TODO: get this from xml file
		String loglevel = "debug";

		// initialize log4j logging engine
		// BEFORE instantiating any jWebSocket classes
		Logging.initLogs(loglevel, Logging.CONSOLE);
		log = Logging.getLogger(JWebSocket.class);

		// the following 3 lines may not be removed due to GNU GPL 3.0 license!
		System.out.println("jWebSocket Ver. "
				+ JWebSocketConstants.VERSION_STR);
		System.out.println(JWebSocketConstants.COPYRIGHT);
		System.out.println(JWebSocketConstants.LICENSE);

		JWebSocketLoader loader = new JWebSocketLoader();
		
		try {
			WebSocketInitializer initializer = loader.initialize();
			WebSocketEngine engine = loader.loadEngine(initializer);
			List<WebSocketServer> servers = loader.loadServers(initializer, engine);
			
			//start the engine
			if (log.isInfoEnabled()) {
				log.info("Starting Engine...");
			}
			engine.startEngine();
			
			//now start the servers
			if (log.isInfoEnabled()) {
				log.info("Starting Servers...");
			}
			for (WebSocketServer server : servers) {
				server.startServer();
			}
			
			//perform any clean up task for servers or any status related 
			
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
}
