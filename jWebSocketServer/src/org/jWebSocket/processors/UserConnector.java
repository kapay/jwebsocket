//	---------------------------------------------------------------------------
//	jWebSocket - Template for User Specific Token Connector
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
package org.jWebSocket.processors;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import org.apache.log4j.Logger;
import org.jWebSocket.connectors.BaseConnector;
import org.jWebSocket.server.BaseServer;
import org.jWebSocket.kit.Header;

/**
 *
 * @author aschulze
 */
public class UserConnector extends BaseConnector {

	// of course, logging is optional for this class
	// it has been introduced for demonstration purposes only
	private static Logger log = Logger.getLogger(UserConnector.class);

	/**
	 *
	 * @param aServerSocket
	 * @param aClientSocket
	 */
	public UserConnector(BaseServer aServerSocket, Socket aClientSocket, Header aHeader) {
		super(aServerSocket, aClientSocket, aHeader);
	}

	@Override
	public void clientThreadStarted() {
		// this is method supposed to be overwriten by application
		log.debug("Client thread started.");
	}

	@Override
	public void handshakeSent() {
		// this is method supposed to be overwriten by application
		log.debug("Handshake sent.");
	}

	@Override
	public void timeoutExceeded() {
		// this is method supposed to be overwriten by application
		log.debug("Timeout exceeded.");
	}

	@Override
	public void clientClosed() {
		// this is method supposed to be overwriten by application
		log.debug("Client closed.");
	}

	@Override
	public void dataReceived(Object aObject) {
		String lLine = (String)aObject;
		log.debug("Received stream: '" + lLine + "'");
		try {
			// here you can parse the incoming string
			// in a way your business logic requires it

			// for demonstration purposes the server
			// here simply echos the same string back
			send("[Echo] " + lLine);

		} catch (UnsupportedEncodingException ex) {
			log.error("UnsupportedEncodingException: " + ex.getMessage());
		} catch (IOException ex) {
			log.error("IOException: " + ex.getMessage());
		}
	}

	@Override
	public void clientThreadStopped() {
		// this is method supposed to be overwriten by application
		log.debug("Client thread stopped.");
	}

}
