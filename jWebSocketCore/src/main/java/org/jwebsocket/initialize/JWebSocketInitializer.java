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
package org.jwebsocket.initialize;

import java.util.List;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketServer;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.plugins.PlugIn;

/**
 * Class that performs initialization
 * @author puran
 * @version $Id$
 */
public final class JWebSocketInitializer {
	
	private JWebSocketConfig config;
	
	/**
	 * private constructor
	 */
	private JWebSocketInitializer(JWebSocketConfig theConfig) {
		this.config = theConfig;
	}
	
	/**
	 * Returns the initializer object 
	 * @param config the jWebSocket config 
	 * @return the initializer object
	 */
	public JWebSocketInitializer getInitializer(JWebSocketConfig config) {
		return new JWebSocketInitializer(config);
	}
	
	/**
	 * Initialize the engine to be started based on configuration.
	 * @return the initialized engine ready to start
	 */
	public WebSocketEngine intializeEngine() {
		throw new UnsupportedOperationException("Not supported yet");
	}
	
	/**
	 * Initializes all the servers configured via jWebSocket configuration
	 * @return the list of initialized servers
	 */
	public List<WebSocketServer> initializeServers() {
		throw new UnsupportedOperationException("Not supported yet");
	}
	
	/**
	 * Initialize the list of plugins defined in via jWebSocket configuration
	 * @return the list of plugins
	 */
	public List<PlugIn> initializePlugins() {
		throw new UnsupportedOperationException("Not supported yet");
	}
}
