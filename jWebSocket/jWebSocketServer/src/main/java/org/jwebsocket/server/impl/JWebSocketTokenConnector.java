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

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jwebsocket.server.api.ConnectorContext;

/**
 * @author puran
 * @version $Id$
 * 
 */
public class JWebSocketTokenConnector extends JWebSocketBaseConnector {

	private static Logger log = Logger
			.getLogger(JWebSocketTokenConnector.class);
	public int counter = 0;
	private String pool = null;
	private String sessionId = null;
	private String username = null;

	/**
	 * 
	 * @param aServerSocket
	 * @param aClientSocket
	 */
	public JWebSocketTokenConnector(ConnectorContext context) {
		super(context);
	}

	/**
	 *
	 */
	@Override
	public void sendHandshake() {
		log.debug("Sending handshake...");
		try {
			super.sendHandshake();
		} catch (Exception ex) {
			log.error("Exception on handshake " + ex.getMessage());
		}
	}

	/**
	 * 
	 * @param aData
	 */
	@Override
	public void sendString(String aData) {
		log.debug("Sending packet (" + aData.length() + " bytes): " + aData);
		try {
			super.sendString(aData);
		} catch (Exception ex) {
			log.error("Exception on send packet " + ex.getMessage());
		}
	}

	/**
	 *
	 */
	@Override
	public void clientThreadStarted() {
	}

	/**
	 * 
	 * @param aHeader
	 */
	@Override
	public void headerReceived(String aHeader) {
		log.debug("Header received: " + aHeader.replace("\n", "\\n"));
	}

	/**
	 * 
	 */
	@Override
	public void headerParsed() {
		log.debug("header parsed: origin: " + getOrigin() + ", location: "
				+ getLocation());
	}

	/**
	 *
	 */
	@Override
	public void handshakeSent() {

	}

	@Override
	public void timeoutExceeded() {
	}

	@Override
	public void clientClosed() {
	}

	@Override
	public void dataReceived(String line) {
	}

	@Override
	public void clientThreadStopped() {
	}
	
	protected HashMap<String, Object> parseToken(String aData) {
		return null;
	}
	
	private boolean isLoggedIn() {
		return getUsername() != null;
	}

	public String getSessionId() {
		return (sessionId != null ? sessionId : "-");
	}

	public String getUsername() {
		return (username != null ? username : "-");
	}

	public String getPool() {
		return pool;
	}

	public void sendToken(String aToken, Map aArgs) {
		// this is just the basic class
		// overwrite this method to send token in a special format
	}

	public void sendResponse(Map aArgs) {
	}

	private void echo(HashMap aArgs) {
	}

	public void getClients(HashMap aArgs) {
	}

	private void sendGoodBye(HashMap aArgs) {
	}

	private void close(HashMap aArgs) {
	}

	@Override
	public void terminate() {
	}

}
