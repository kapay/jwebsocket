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
package org.jwebsocket.server.impl;

import org.apache.log4j.Logger;
import org.jwebsocket.server.JWebSocketBaseServer;
import org.jwebsocket.server.api.ConnectorContext;
import org.jwebsocket.server.api.JWebSocketConnector;

/**
 * @author Alexander Schulze
 * @author Puran Singh
 * @version $Id: JWebSocketTokenServer.java 58 2010-02-15 19:21:56Z mailtopuran$
 * 
 */
public class JWebSocketTokenServer extends JWebSocketBaseServer {
	private static Logger log = Logger.getLogger(JWebSocketTokenServer.class);

	public JWebSocketTokenServer(int port, int timeout) {
		super(port, timeout);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JWebSocketBaseConnector createJWebSocketConnector(
			ConnectorContext context) {
		return new JWebSocketJSONConnector(context);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void instantiateServer(int aPort, int aSessionTimeout) {
		log.info("Instantiating jWebSocket server...");
		try {
			super.instantiateServer(aPort, aSessionTimeout);
		} catch (Exception ex) {
			log.info("Exception on instantiating jWebSocket server socket: "
					+ ex.getMessage());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void serverStarted() {
		log.info("jWebSocket server started.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void serverStopped() {
		log.info("jWebSocket server stopped.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clientStarted(JWebSocketConnector client) {
		log.info("Connected new client.");
	}

}
