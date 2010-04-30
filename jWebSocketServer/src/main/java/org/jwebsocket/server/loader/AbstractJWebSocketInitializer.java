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
package org.jwebsocket.server.loader;

import java.util.List;
import java.util.Map;

import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketInitializer;
import org.jwebsocket.api.WebSocketPlugIn;
import org.jwebsocket.api.WebSocketServer;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.config.xml.EngineConfig;
import org.jwebsocket.tcp.engines.TCPEngine;

/**
 * @author puran
 * @version $Id:$
 *
 */
public abstract class AbstractJWebSocketInitializer implements WebSocketInitializer {

	private JWebSocketConfig config;

	public AbstractJWebSocketInitializer(JWebSocketConfig theConfig) {
		this.config = theConfig;
	}

	/**
	 * Initialize the engine to be started based on configuration.
	 * 
	 * @return the initialized engine ready to start
	 */
	public WebSocketEngine intializeEngine() {
		WebSocketEngine newEngine = null;
		EngineConfig engine = config.getEngines().get(0);
		try {
			// newEngine = new NettyEngine(engine.getId(), engine.getPort(),
			// engine.getTimeout());
			newEngine = new TCPEngine(engine.getId(), engine.getPort(), engine
					.getTimeout());
		} catch (Exception e) {
			System.out.println("Error instantating engine: " + e.getMessage());
			System.exit(0);
		}
		return newEngine;
	}

	/**
	 * Initializes all the servers configured via jWebSocket configuration
	 * 
	 * @return the list of initialized servers
	 */
	public List<WebSocketServer> initializeServers() {
		List<WebSocketServer> customServers = initializeCustomServers();
		throw new UnsupportedOperationException("Not supported yet");
	}

	/**
	 * Initialize the list of plugins defined in via jWebSocket configuration
	 * 
	 * @return the map of server id and the list of plugins for each server
	 */
	public Map<String, List<WebSocketPlugIn>> initializePlugins() {
		Map<String, List<WebSocketPlugIn>> customPlugins = initializeCustomPlugins();
		throw new UnsupportedOperationException("Not supported yet");
	}
	
    /**
     * Allow subclass of this class to initialize custom plugins.
     * @return
     */
	public abstract Map<String, List<WebSocketPlugIn>> initializeCustomPlugins();

	/**
	 * Allow the subclass of this class to initialize custom servers
	 * @return the list of custom servers
	 */
	public abstract List<WebSocketServer> initializeCustomServers();

}
