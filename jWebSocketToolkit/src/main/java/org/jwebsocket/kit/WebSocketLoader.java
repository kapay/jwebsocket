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
package org.jwebsocket.kit;

import org.jwebsocket.config.JWebSocketConfig;

/**
 * An object that does the process of loading configuration, intialization of the
 * jWebSocket server system.  
 * @author puran
 * @version $Id$
 */
public final class WebSocketLoader {
	
	/**
	 * Load all the configurations based on jWebSocket.xml file at the given 
	 * <tt>configFilePath</tt> location.
	 * @param configFilePath
	 * @return the web socket config object with all the configuration
	 * @throws WebSocketException if there's any while loading configuration
	 */
	public JWebSocketConfig loadConfiguration(String configFilePath) throws WebSocketException {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Initialize the loaded components of the jWebSocket server system using the  
	 * configuration
	 * @param configuration the jWebSocket configuration
	 * @return {@code true} if intialization was completely successful,{@code false}
	 * otherwise.
	 * @throws WebSocketException if exception occurs during intialization
	 */
	public boolean initialize(JWebSocketConfig configuration) throws WebSocketException {
		throw new UnsupportedOperationException();	
	}
}
