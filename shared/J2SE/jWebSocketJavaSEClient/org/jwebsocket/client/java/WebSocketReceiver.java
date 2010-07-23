//  ---------------------------------------------------------------------------
//  jWebSocket - WebSocket Client Interface
//  Copyright (c) 2010 jWebSocket.org, Alexander Schulze, Innotrade GmbH
//  ---------------------------------------------------------------------------
//  This program is free software; you can redistribute it and/or modify it
//  under the terms of the GNU Lesser General Public License as published by the
//  Free Software Foundation; either version 3 of the License, or (at your
//  option) any later version.
//  This program is distributed in the hope that it will be useful, but WITHOUT
//  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//  more details.
//  You should have received a copy of the GNU Lesser General Public License along
//  with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//  ---------------------------------------------------------------------------
package org.jwebsocket.client.java;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.jwebsocket.api.WebSocketEventHandler;
import org.jwebsocket.api.WebSocketMessage;

/**
 * Based on the implementation from http://weberknecht.googlecode.com
 * @author Roderick Baier
 * @version $Id:$
 */
public class WebSocketReceiver extends Thread {

	private InputStream input = null;
	private WebSocketEventHandler eventHandler = null;

	private volatile boolean stop = false;
	
	public WebSocketReceiver(InputStream input, WebSocketEventHandler eventHandler) {
		this.input = input;
		this.eventHandler = eventHandler;
	}

	public void run() {
		boolean frameStart = false;
		List<Byte> messageBytes = new ArrayList<Byte>();

		while (!stop) {
			try {
				int b = input.read();
				// TODO support binary frames
				if (b == 0x00) {
					frameStart = true;
				}
				else if (b == 0xff && frameStart == true) {
					frameStart = false;
					Byte[] message = messageBytes.toArray(new Byte[messageBytes.size()]);
					WebSocketMessage webSocketMessage = new BaseWebSocketMessage(message); 
					eventHandler.onMessage(webSocketMessage);
					messageBytes.clear();
				}
				else if (frameStart == true){
					messageBytes.add((byte)b);
				}
				else if (b == -1) {
					handleError();
				}
			}
			catch (IOException ioe) {
				handleError();
			}
		}
	}
	
	public void stopit() {
		stop = true;
	}
	
	public boolean isRunning() {
		return !stop;
	}
	
	private void handleError() {
		stopit();
	}

}
