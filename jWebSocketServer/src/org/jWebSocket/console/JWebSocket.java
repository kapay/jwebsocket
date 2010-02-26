//	---------------------------------------------------------------------------
//	jWebSocket - Main Class: Command line IF, Logger Init and Args-Parser
//	Copyright (c) 2010 Alexander Schulze, Innotrade GmbH
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
package org.jWebSocket.console;

import org.apache.log4j.Logger;
import org.jWebSocket.api.IWebSocketEngine;
import org.jWebSocket.config.Config;
import org.jWebSocket.demo.DemoPlugIn;
import org.jWebSocket.engines.TCPEngine;
import org.jWebSocket.kit.WebSocketException;
import org.jWebSocket.logging.Logging;
import org.jWebSocket.plugins.KeepAlivePlugIn;
import org.jWebSocket.plugins.PlugInChain;
import org.jWebSocket.plugins.RPCPlugIn;
import org.jWebSocket.plugins.SystemPlugIn;
import org.jWebSocket.server.BaseServer;
import org.jWebSocket.server.TokenServer;

/**
 * This class is the main class for the jWebSocket server
 * @author aschulze
 */
public class JWebSocket {

	/**
	 *
	 */
	private static Logger log = Logger.getLogger(JWebSocket.class);

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {

		String prot = "token"; // [plain|token]
		String loglevel = "debug";
		int port = Config.DEFAULT_PORT_TOKEN;
		int sessionTimeout = Config.DEFAULT_TIMEOUT;

		// parse optional command line arguments
		int i = 0;
		while (i < args.length) {
			if (i + 1 < args.length) {
				if (args[i].equalsIgnoreCase("prot")) {
					prot = args[i + 1].toLowerCase();
				} else if (args[i].equalsIgnoreCase("sessiontimeout")) {
					try {
						sessionTimeout = Integer.parseInt(args[i + 1].toLowerCase());
					} catch (NumberFormatException ex) {
						// ignore execption here
					}
				} else if (args[i].equalsIgnoreCase("loglevel")) {
					loglevel = args[i + 1].toLowerCase();
				} else if (args[i].equalsIgnoreCase("port")) {
					try {
						port = Integer.parseInt(args[i + 1].toLowerCase());
					} catch (NumberFormatException ex) {
						// ignore execption here
					}
				}
			}
			i += 2;
		}

		// initialize log4j logging engine
		// BEFORE instantiating any jWebSocket classes
		Logging.initLogs(loglevel);

		BaseServer server = null;

		// the following lines may not be removed due to GNU GPL 3.0 license!
		System.out.println("jWebSocket Ver. " + Config.VERSION_STR);
		System.out.println(Config.COPYRIGHT);
		System.out.println(Config.LICENSE);

		System.out.println(
			"Listening on port " + port + ", default (sub)prot " + prot + ", "
			+ "default session timeout: " + sessionTimeout + ", log-level: " + loglevel.toLowerCase());
		if (prot.equalsIgnoreCase("token")) {

			// first of all create the low-level engine
			try {
				IWebSocketEngine engine = new TCPEngine(port, sessionTimeout);
				// instantiate the Token server and bind engine to it
				server = new TokenServer(engine);
				// add the SystemPlugIn listener (for the jWebSocket default functionality)
				server.addPlugIn(new SystemPlugIn());
				// add the keep alive plug-in
				server.addPlugIn(new KeepAlivePlugIn());
				// add the RPCPlugIn plug-in
				server.addPlugIn(new RPCPlugIn());
				// add the demo listener (for the time stream demo)
				server.addPlugIn(new DemoPlugIn());
			} catch (WebSocketException ex) {
				System.out.println("Error: " + ex.getMessage());
			}

		} else if (prot.equalsIgnoreCase("plain")) {
			server = new UserServer(port, sessionTimeout, null);
		} else {
			System.out.println("Invalid argument.");
			System.out.println("java -jar jWebSocket.jar prot=[plain|token] port=["
				+ Config.MIN_IN_PORT + "-" + Config.MAX_IN_PORT + "]");
		}

		if (server != null) {
			log.debug("jWebSocket server starting...");
			server.start();
			log.debug("jWebSocket server running...");
			while (server.isAlive()) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException ex) {
					log.debug(ex.getClass().getName() + " " + ex.getMessage());
				}
			}
			log.info("jWebSocket server successfully terminated.");
		}
	}
}
