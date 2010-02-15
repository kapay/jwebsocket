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

import org.jwebsocket.server.JWebSocketBaseServer;
import org.jwebsocket.server.api.ConnectorContext;

/**
 * @author alex
 * @author puran
 * @version $Id$
 *
 */
public class JWebSocketTokenServer extends JWebSocketBaseServer {
	
	public JWebSocketTokenServer(int port, int timeout) {
		super(port, timeout);
	}
	
	@Override
	public JWebSocketBaseConnector createJWebSocketClient(ConnectorContext context) {
		return new JWebSocketJSONConnector(context);
	}
}
