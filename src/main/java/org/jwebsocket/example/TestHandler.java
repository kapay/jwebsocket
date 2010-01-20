/*
 *  Copyright (c) 2009 Puran Singh(mailtopuran@gmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jwebsocket.example;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import org.jwebsocket.api.WebSocket;
import org.jwebsocket.api.WebSocketException;
import org.jwebsocket.api.WebSocketHandler;

/**
 * @author <a href="http://blog.purans.net"> Puran Singh</a> &lt;<a
 *         href="mailto:puran@programmer.net"> puran@programmer.net </a>>
 * @version $Id$
 * 
 */
public class TestHandler implements WebSocketHandler {

	private Map<String, WebSocket> chatMembers = new HashMap<String, WebSocket>();

	@Override
	public void init(Properties properties) {
		System.out.print("Initialized");
	}

	@Override
	public void onClose(WebSocket socket) {
		System.out.print(" Handler Closed:" + socket.getId());
		String user = socket.getAttribute("user").toString();
		System.out.println(user + " left the chat room");
		chatMembers.remove(user);
		try {
			broadcast(user + " left the chat room");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onException(WebSocket socket, Throwable cause) {
		try {
			socket.close();
		} catch (WebSocketException e) {
			e.printStackTrace();
		}
	}

	private void broadcast(String message) throws WebSocketException {
		Set<Entry<String, WebSocket>> sessions = chatMembers.entrySet();

		for (Entry<String, WebSocket> session : sessions) {
			WebSocket webSocket = session.getValue();
			webSocket.send(message);
		}
	}

	@Override
	public void onMessage(WebSocket socket, Object message) {
		if (message == null || message.toString().length() == 0) {
			// ignore blank message
			return;
		}
		String messageStr = message.toString();
		String[] messageArray = messageStr.split("[:]");

		String user = messageArray[0].trim();
		String msg = messageArray[1].trim();
		String serverMessage = "";
		try {
			if (msg.equalsIgnoreCase("join")) {
				socket.setAttribute("user", user);
				System.out.println(user + " joined the chat room");
				chatMembers.put(user, socket);
				serverMessage = user + " joined the chat room";
			} else {
				serverMessage = user + " says:" + msg;
			}
			broadcast(serverMessage);
		} catch (WebSocketException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onOpen(WebSocket socket) {
		System.out.print("Handler Opened");
	}

}
