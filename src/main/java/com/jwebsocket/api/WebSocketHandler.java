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
package com.jwebsocket.api;

import java.util.Properties;

/**
 * Base interface that represents the WebSocket server application and recieves all the 
 * websocket events. This has to be implemented by the main Web Socket application with 
 * the application specfific codes on different WebSocket events.
 * 
 * @author <a href="http://blog.purans.net">
 *         Puran Singh</a>
 *         &lt;<a href="mailto:puran@programmer.net">
 *          puran@programmer.net
 *         </a>>
 * @version $Id$
 *
 */
public interface WebSocketHandler {
	/**
	 * This method allows this handler to perform intialization tasks. 
	 * It is invoked before the web socket connection is opened.
	 * @param properties the key/value pair of the properties defined via
	 * websocket.xml configuration file. 
	 */
	void init(Properties properties);

	/**
	 * Notifies the handler when the web socket connection is open and ready to 
	 * recieve requests from the client.
	 * 
	 * @param socket the new web socket object 
	 */
	void onOpen(WebSocket socket);

	/**
	 * Notifies the handler when the message is recieved in a web socket.
	 * @param socket the websocket object where message is received
	 * @param message the message body
	 */
	void onMessage(WebSocket socket, Object message);

	/**
	 * Notifies the handler when the web socket is closed, either by the client
	 * or due to some exception.
	 * Handler should do all the clean up work in this method.
	 * @param socket the web socket object which is closed
	 */
	void onClose();

	/**
	 * Notifies the handler when there's any exception.
	 * @param socket the closed web socket object
	 * @param cause the cause of the exception
	 */
	void onException(WebSocket socket, Throwable cause);
}
