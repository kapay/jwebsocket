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

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.jwebsocket.server.api.ConnectorContext;

/**
 * @author Puran Singh
 * @version $Id$
 * 
 */
public class JWebSocketBaseConnector {
	
	private ConnectorContext context;

	public JWebSocketBaseConnector() {
	}
	
	public JWebSocketBaseConnector (ConnectorContext context) {
		this.context = context;
	}

	public void start() {
		
	}
	
	/**
     * sends the handshake back to the browser client
     * according to the WebSocket spec.
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    protected void sendHandshake() {
    }
    
	/**
	 * 
	 * @param aData
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	public void sendString(String aData) {
	}

	/**
    *
    */
	public void clientThreadStarted() {
		// method may be overwriten on demand
	}

	/**
	 * 
	 * @param aHeader
	 */
	public void headerReceived(String aHeader) {
		// method may be overwriten on demand
	}

	/**
    *
    */
	public void headerParsed() {
		// method may be overwriten on demand
	}

	/**
    *
    */
	public void handshakeSent() {
		// method may be overwriten on demand
	}

	/**
    *
    */
	public void timeoutExceeded() {
		// method may be overwriten on demand
	}

	/**
    *
    */
	public void clientClosed() {
		// method may be overwriten on demand
	}

	/**
	 * 
	 * @param line
	 */
	public void dataReceived(String line) {
		// method may be overwriten on demand
	}

	/**
    *
    */
	public void clientThreadStopped() {
		// method may be overwriten on demand
	}

	/**
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void terminate() {
	}

	/**
	 * @return the origin
	 */
	public String getOrigin() {
		return null;
	}

	/**
	 * @return the location
	 */
	public String getLocation() {
		return null;
	}

}
