/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.console;

import java.util.ArrayList;
import org.apache.log4j.Logger;
import org.jwebsocket.api.EngineConfiguration;
import org.jwebsocket.api.ServerConfiguration;
import org.jwebsocket.api.WebSocketListener;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.config.JWebSocketConstants;
import org.jwebsocket.config.xml.EngineConfig;
import org.jwebsocket.config.xml.LoggingConfig;
import org.jwebsocket.config.xml.ServerConfig;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.WebSocketEvent;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugInChain;
import org.jwebsocket.plugins.flashbridge.FlashBridgePlugIn;
import org.jwebsocket.plugins.system.SystemPlugIn;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.tcp.engines.TCPEngine;

/**
 *
 * @author aschulze
 */
public class SampleEmbeddedServer implements WebSocketListener {

	private static Logger log = null;

	@Override
	public void processOpened(WebSocketEvent aEvent) {
		log.info("Client " + aEvent.getSessionId() + " connected.");
	}

	@Override
	public void processPacket(WebSocketEvent aEvent, WebSocketPacket aPacket) {
		log.info("Client " + aEvent.getSessionId() + "sent: '" + aPacket.getASCII() + "'.");
		aEvent.getServer().sendPacket(aEvent.getConnector(), aPacket);
	}

	@Override
	public void processClosed(WebSocketEvent aEvent) {
		log.info("Client " + aEvent.getSessionId() + " disconnected.");
	}

	/**
	 * @param args the command line arguments
	 */
	public void runEmbeddedJWebSocketServer() {
		// the following 3 lines may not be removed due to GNU LGPL 3.0 license!
		System.out.println("jWebSocket Ver. " + JWebSocketConstants.VERSION_STR + " (" + System.getProperty("sun.arch.data.model") + "bit)");
		System.out.println(JWebSocketConstants.COPYRIGHT);
		System.out.println(JWebSocketConstants.LICENSE);

		// initialize the logging system
		LoggingConfig loggingConfig = new LoggingConfig(
				"console", // target
				"%d{yyyy-MM-dd HH:mm:ss,SSS} %-5p - %C{1}: %m%n", // pattern
				"debug", // level
				"jWebSocket.log", // file name, if logging to file only
				4096 // bufferSize, if logging to file only
				);
		Logging.initLogs(loggingConfig);
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
		TCPEngine engine = new TCPEngine(engineConfig);

		// if engine could be instantiated properly...
		if (engine != null) {
			// initialize the server
			ServerConfiguration serverConfig = new ServerConfig(
					"ts0", // id
					"org.jwebsocket.server.TokenServer", // name
					"-" // jar, needs to be in classpath, i.e. embedded in .jar'/manifest
					);
			TokenServer server = new TokenServer(serverConfig);

			// link server and engine
			engine.addServer(server);
			server.addEngine(engine);
			// add listener to the server
			server.addListener(this);

			// add desired plug-ins
			TokenPlugInChain plugInChain = server.getPlugInChain();
			// the system plug-in is essential to process authentication
			// send and broadcast
			plugInChain.addPlugIn(new SystemPlugIn());
			// the FlashBrigde plug-in is strongly recommended to also support
			// non websocket compliant browsers
			plugInChain.addPlugIn(new FlashBridgePlugIn());

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

			// wait until engine has terminated (e.g. by "shutdown" command)
			while (engine.isAlive()) {
				try {
					Thread.sleep(250);
				} catch (InterruptedException ex) {
					// no handling required here
				}
			}

			try {
				// terminate all instances
				engine.stopEngine(CloseReason.SERVER);
			} catch (WebSocketException ex) {
				log.error("Exception on stopping jWebSockt subsystem: " + ex.getMessage());
			}
		}

	}
}
