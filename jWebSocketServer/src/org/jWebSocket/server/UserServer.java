//	---------------------------------------------------------------------------
//	jWebSocket - Template for User Specific WebSocket Server
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
package org.jWebSocket.server;

import org.jWebSocket.plugins.PlugInChain;
import org.jWebSocket.connectors.BaseConnector;
import org.jWebSocket.connectors.UserConnector;
import java.net.Socket;

/**
 *
 * @author aschulze
 */
public class UserServer extends BaseServer {

	/**
	 *
	 * @param aPort
	 */
	public UserServer(int aPort, int aSessionTimeout, PlugInChain aListeners) {
		super(aPort, aSessionTimeout, aListeners);
	}

	@Override
	public BaseConnector createConnector(Socket aClientSocket, Header aHeader) {
		return new UserConnector(this, aClientSocket, aHeader);
	}
}
