//	---------------------------------------------------------------------------
//	jWebSocket
//	Copyright (c) 2011, Innotrade GmbH - jWebSocket.org, Alexander Schulze
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
package org.jwebsocket.plugins.jms;

/**
 * 
 * @author jsmutny
 */
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;

import org.springframework.jms.core.JmsTemplate;

public class JwsJmsTemplate extends JmsTemplate {

	private Connection mConnection;
	private Session mSession;
	private String mDestinationName;

	@Override
	protected Connection createConnection() throws JMSException {
		mConnection = getConnectionFactory().createConnection();
		return mConnection;
	}

	protected Session createSession() throws JMSException {
		mSession = mConnection.createSession(isSessionTransacted(), getSessionAcknowledgeMode());
		return mSession;
	}

	public Connection getConnection() {
		return mConnection;
	}

	public void setConnection(Connection aConnection) {
		this.mConnection = aConnection;
	}

	public Session getSession() {
		return mSession;
	}

	public void setSession(Session aSession) {
		this.mSession = aSession;
	}

	public void setDestinationName(String aDestinationName) {
		this.mDestinationName = aDestinationName;
	}

	public String getDestinationName() {
		return this.mDestinationName;
	}
}
