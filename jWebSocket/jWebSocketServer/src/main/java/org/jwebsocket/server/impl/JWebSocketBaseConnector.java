//	---------------------------------------------------------------------------
//	jWebSocket -Copyright (c) 2010 jwebsocket.org
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

import org.jwebsocket.server.api.ConnectorContext;
import org.jwebsocket.server.api.JWebSocketConnector;

/**
 * Abstract implementation of {@code JWebSocketConnector} interface
 * 
 * @author Puran Singh
 * @version $Id: JWebSocketBaseConnector.java 58 2010-02-15 19:21:56Z
 *          mailtopuran $
 * 
 */
public abstract class JWebSocketBaseConnector implements JWebSocketConnector {

	private ConnectorContext context;

	private volatile boolean connectorStarted = false;

	public JWebSocketBaseConnector() {
	}

	public JWebSocketBaseConnector(ConnectorContext context) {
		this.context = context;
	}

	public void start() {
		connectorStarted = true;
	}

	/**
	 * {@inheritDoc}
	 */
	public void sendString(String aData) {
		context.sendString(aData);
	}

	/**
	 * {@inheritDoc}
	 */
	public void onHandShakeResponse() {

	}

	/**
	 * {@inheritDoc}
	 */
	public void clientThreadStarted() {
	}

	/**
	 * {@inheritDoc}
	 */
	public void headerReceived(String aHeader) {
		// method may be overwriten on demand
	}

	/**
	 * {@inheritDoc}
	 */
	public void headerParsed() {
		// method may be overwriten on demand
	}

	/**
	 * {@inheritDoc}
	 */
	public void handshakeSent() {
		// method may be overwriten on demand
	}

	/**
	 * {@inheritDoc}
	 */
	public void timeoutExceeded() {
		// method may be overwriten on demand
	}

	/**
	 * {@inheritDoc}
	 */
	public void clientClosed() {
		// method may be overwriten on demand
	}

	/**
	 * {@inheritDoc}
	 */
	public void dataReceived(String line) {
		// method may be overwriten on demand
	}

	/**
	 * {@inheritDoc}
	 */
	public void clientThreadStopped() {
		// method may be overwriten on demand
	}

	/**
	 * {@inheritDoc}
	 */
	public void terminate() {
		connectorStarted = false;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean isAlive() {
		return connectorStarted;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getOrigin() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getLocation() {
		return null;
	}

	/**
	 * Returns the context associated with the connector client
	 * @return the context object
	 */
	protected ConnectorContext getContext() {
		return context;
	}
}
