//	---------------------------------------------------------------------------
//	jWebSocket - Copyright (c) 2010 jwebsocket.org
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
package org.jwebsocket.console;

import java.util.ArrayList;
import org.apache.log4j.Logger;
import org.jwebsocket.api.EngineConfiguration;
import org.jwebsocket.api.ServerConfiguration;
import org.jwebsocket.api.WebSocketServerListener;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.config.LoggingConfig;
import org.jwebsocket.config.xml.EngineConfig;
import org.jwebsocket.config.xml.ServerConfig;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugInChain;
import org.jwebsocket.plugins.flashbridge.FlashBridgePlugIn;
import org.jwebsocket.plugins.system.SystemPlugIn;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.tcp.engines.TCPEngine;

/**
 * Example of a pure programmatic embedded jWebSocket server.
 * @author aschulze
 */
public class JWebSocketSubSystemSample {

	private static Logger log = null;
	private TokenServer server = null;
	private TCPEngine engine = null;

	/**
	 */
	public JWebSocketSubSystemSample() {
		// the following 3 lines may not be removed due to GNU LGPL 3.0 license!
		System.out.println("jWebSocket Ver. " + JWebSocketServerConstants.VERSION_STR + " (" + System.getProperty("sun.arch.data.model") + "bit)");
		System.out.println(JWebSocketCommonConstants.COPYRIGHT);
		System.out.println(JWebSocketCommonConstants.LICENSE);

		// initialize the logging system
		LoggingConfig loggingConfig = new LoggingConfig(
				"console", // target
				"%d{yyyy-MM-dd HH:mm:ss,SSS} %-5p - %C{1}: %m%n", // pattern
				"debug", // level
				"jWebSocket.log", // file name, if logging to file only
				4096 // bufferSize, if logging to file only
				);
		Logging.initLogs(loggingConfig, new String[]{"%JWEBSOCKET_HOME%/logs", "%CATALINA_HOME%/logs"});
		log = Logging.getLogger(JWebSocketEmbedded.class);

		// initialize the engine
		ArrayList lDomains = new ArrayList();
		lDomains.add("http://jwebsocket.org");
		EngineConfiguration engineConfig = new EngineConfig(
				"tcp0", // id
				"org.jwebsocket.tcp.engines.TCPEngine", // name
				"-", // jar, needs to be in classpath, i.e. embedded in .jar'/manifest
				8787, // port
				120000, // default session timeout
				16384, // max framesize
				lDomains // list of accepted domains
				);
		engine = new TCPEngine(engineConfig);

		// if engine could be instantiated properly...
		if (engine != null) {
			// initialize the server
			ServerConfiguration serverConfig = new ServerConfig(
					"ts0", // id
					"org.jwebsocket.server.TokenServer", // name
					"-" // jar, needs to be in classpath, i.e. embedded in .jar'/manifest
					);
			server = new TokenServer(serverConfig);

			// link server and engine
			engine.addServer(server);
			server.addEngine(engine);

			// add desired plug-ins
			TokenPlugInChain plugInChain = server.getPlugInChain();
			// the system plug-in is essential to process authentication
			// send and broadcast
			plugInChain.addPlugIn(new SystemPlugIn());
			// the FlashBrigde plug-in is strongly recommended to also support
			// non websocket compliant browsers
			plugInChain.addPlugIn(new FlashBridgePlugIn());



		}
	}

	public void start() {
		// start the jWebsocket Server
		try {
			engine.startEngine();
		} catch (WebSocketException ex) {
			log.error("Exception on starting jWebSocket engine: " + ex.getMessage());
		}
		try {
			server.startServer();
		} catch (WebSocketException ex) {
			log.error("Exception on starting jWebSocket server: " + ex.getMessage());
		}
	}

	public void run() {
		// wait until engine has terminated (e.g. by "shutdown" command)
		while (engine.isAlive()) {
			try {
				Thread.sleep(250);
			} catch (InterruptedException ex) {
				// no handling required here
			}
		}
	}

	public void stop() {
		try {
			// terminate all instances
			engine.stopEngine(CloseReason.SERVER);
		} catch (WebSocketException ex) {
			log.error("Exception on stopping jWebSocket subsystem: " + ex.getMessage());
		}
	}

	/**
	 * adds a new listener to the server of the jWebSocket subsystem.
	 * @param aListener
	 */
	public void addListener(WebSocketServerListener aListener) {
		if (aListener != null && server != null) {
			// add listener to the server
			server.addListener(aListener);
		}
	}

	/**
	 * removes a listener from the server of the jWebSocket subsystem.
	 * @param aListener
	 */
	public void removeListener(WebSocketServerListener aListener) {
		if (aListener != null && server != null) {
			// remove listener from the server
			server.removeListener(aListener);
		}
	}
}
