//	---------------------------------------------------------------------------
//	jWebSocket - Copyright (c) 2010 Innotrade GmbH
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
package org.jwebsocket.client.java;

import org.jwebsocket.api.WebSocket;
import org.jwebsocket.api.WebSocketEventHandler;
import org.jwebsocket.api.data.WebSocketMessage;
import org.jwebsocket.kit.WebSocketException;

/**
 * Implementation for WebSocketEventHandler that handles all the
 * incoming requests 
 * @author agali
 *
 */
public class BaseWebSocketEventHandler implements WebSocketEventHandler {

	@Override
	public void onClose(WebSocket client) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onException(WebSocket client, Throwable clause) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessage(WebSocket client, WebSocketMessage message) {
		System.out.println("BaseWebSocketEventHandler.onMesssage: " +message.getText());
		String responseToServer = "FROM CLIENT:" + message.getText();
		try {
			client.send(responseToServer);
		} catch (WebSocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void onOpen(WebSocket client) {
		System.out.println("BaseWebSocketEventHandler.onMesssage: " +client.toString());
	}

	
	
}
