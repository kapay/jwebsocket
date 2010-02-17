//	---------------------------------------------------------------------------
//	jWebSocket - Main Class: Command line IF, Logger Init and Args-Parser
//	Copyright (c) 2010 jwebsocket.org
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
package org.jwebsocket.server;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.jwebsocket.server.impl.JWebSocketJSONServer;
import org.jwebsocket.server.impl.netty.JWebSocketBaseServer;

/**
 * This is the main entry point of the jWebSocketServer that initializes all the
 * resources based on configuration and start the server and connectors.
 * 
 * @author Puran Singh
 * @author Alexander Schulze
 * @version $Id$
 * 
 */
public final class JWebSocketMainServer {

	/** root logger definition */
	public static Logger rootLogger = Logger.getRootLogger();
	private static Logger log = Logger.getLogger(JWebSocketMainServer.class);

	/**
	 * initialize the log level based on the command line params.
	 * 
	 * @param aLogLevel the log level value
	 */
	public static void initLogs(String aLogLevel) {
		PatternLayout layout = new PatternLayout();
		// layout.setConversionPattern("%d{HH:mm:ss,SSS} %p - %C{1}: %m%n");
		layout.setConversionPattern("%d{HH:mm:ss,SSS} %-5p - %C{1}: %m%n");
		// delete all potentially already existing appenders
		rootLogger.removeAllAppenders();

		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		rootLogger.addAppender(consoleAppender);
		rootLogger.setLevel(Level.toLevel(aLogLevel));
	}

	/**
	 * main entry point for the jWebSocket server
	 * @param args the command line arguments.
	 */
	public static void main(String... args) {
		String prot = "json"; // [plain/json]
		String loglevel = "debug";
		int port = JWebSocketBaseServer.DEFAULT_PORT;
		int sessionTimeout = JWebSocketBaseServer.DEFAULT_TIMEOUT;

		// parse optional command line arguments
		int i = 0;
		while (i < args.length) {
			if (i + 1 < args.length) {
				if (args[i].equalsIgnoreCase("prot")) {
					prot = args[i + 1].toLowerCase();
				} else if (args[i].equalsIgnoreCase("sessiontimeout")) {
					try {
						sessionTimeout = Integer.parseInt(args[i + 1]
								.toLowerCase());
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
		initLogs(loglevel);

		// start the server at given port
		startServer(port, prot, sessionTimeout);
	}

	/**
	 * Start the server based on passed in arguments
	 * 
	 * @param port the port number
	 * @param protocol the protocol for the connector
	 * @param timeout the timeout value in millisecond
	 */
	private static void startServer(int port, String protocol, int timeout) {
		JWebSocketBaseServer jwss = null;

		System.out.println("jWebSocket Ver. "
				+ JWebSocketBaseServer.VERSION_STR);
		System.out.println("Listening on port " + port + ", using " + protocol
				+ " format, " + "session timeout: " + timeout
				+ ", log-level: "
				+ rootLogger.getLevel().toString().toLowerCase());
		if (protocol.equalsIgnoreCase("json")) {
			jwss = new JWebSocketJSONServer(port, timeout);
		} else if (protocol.equalsIgnoreCase("plain")) {
			// ignore for now
		} else if (protocol.equalsIgnoreCase("user")) {
			// ignore for now
		} else {
			System.out.println("Invalid argument.");
			System.out
					.println("java -jar jWebSocket.jar prot=[plain|json|user] port=["
							+ JWebSocketBaseServer.MIN_PORT
							+ "-"
							+ JWebSocketBaseServer.MAX_PORT + "]");
		}

		if (jwss != null) {
			log.debug("jWebSocket server starting...");
			jwss.start();
			log.debug("jWebSocket server running...");
			while (jwss.isAlive()) {
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
