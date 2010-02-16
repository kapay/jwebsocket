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
package org.jwebsocket.server.api;

import java.util.List;

/**
 * @author Puran Singh
 * @version $Id:$
 * 
 */
public interface JWebSocketServer {
	/**
	 * Notifies that the server is started for the first time
	 */
	 void serverStarted();

	/**
	 * Notifies server is stopped
	 */
	public void serverStopped();

	/**
	 * Notifies when client connector is started
	 * @param client the started client
	 */
	void clientStarted(JWebSocketConnector client);
	
	/**
	 * Method that creates the connector client for the given context
	 * @param context the connector context object
	 * @return the connector client object
	 * 
	 * Here are the few implementation of this method that will return:
	 * {@code JWebSocketJSONServer} will return {@code JWebSocketJSONConnector}
	 * {@code JWebSocketXMLServer} will return {@code JWebSocketXMLConnector}
	 * {@code JWebSocketCSVServer} will return {@code JWebSocketCSVConnector}
	 */
	JWebSocketConnector createJWebSocketConnector(ConnectorContext context);
	
	/**
	 * Returns the list of connector clients connected to this server 
	 * @return the connector clients
	 */
	List<JWebSocketConnector> getClients();
}
