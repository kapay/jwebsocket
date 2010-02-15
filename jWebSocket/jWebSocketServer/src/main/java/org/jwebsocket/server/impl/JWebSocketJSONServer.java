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
package org.jwebsocket.server.impl;


import org.apache.log4j.Logger;
import org.jwebsocket.server.api.ConnectorContext;
/**
 * 
 * @author alex
 * @author puran
 * @version $Id$
 *
 */
public class JWebSocketJSONServer extends JWebSocketTokenServer {
	
	/**
	 * default constructor 
	 * @param port the port number at which the JSON server is running
	 * @param timeout the timeout value for the JSON server
	 */
	public JWebSocketJSONServer(int port, int timeout) {
		super(port, timeout);
	}

	@Override
	public JWebSocketBaseConnector createJWebSocketClient(ConnectorContext context) {
		return new JWebSocketJSONConnector(context);
	}
	
	private static Logger log = Logger.getLogger(JWebSocketTokenServer.class);

	@Override
	protected void instantiateServer(int aPort, int aSessionTimeout) {
		log.info("Instantiating jWebSocket server...");
		try {
			super.instantiateServer(aPort, aSessionTimeout);
		} catch (Exception ex) {
			log.info("Exception on instantiating jWebSocket server socket: " + ex.getMessage());
		}
	}

	@Override
	public void serverStarted() {
		log.info("jWebSocket server started.");
	}

	@Override
	public void serverStopped() {
		log.info("jWebSocket server stopped.");
	}

	@Override
	public void clientStarted(JWebSocketBaseConnector client) {
		log.info("Connected new client.");
	}


}
