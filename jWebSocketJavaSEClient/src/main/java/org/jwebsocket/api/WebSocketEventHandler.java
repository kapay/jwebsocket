//	---------------------------------------------------------------------------
//	jWebSocket - WebSocket CGI Client
//	Copyright (c) 2010 jWebSocket.org, Alexander Schulze, Innotrade GmbH
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
package org.jwebsocket.api;

import org.jwebsocket.api.data.WebSocketMessage;

/**
 * Referenced http://weberknecht.googlecode.com
 * @author agali
 */
public interface WebSocketEventHandler {

	public void onOpen(WebSocket client);
	
	public void onMessage(WebSocket client, WebSocketMessage message);
	
	public void onClose(WebSocket client);
	
	public void onException(WebSocket client, Throwable clause);

}
