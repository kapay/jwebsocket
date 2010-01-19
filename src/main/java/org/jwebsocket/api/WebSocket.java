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
package org.jwebsocket.api;

/**
 * Web Socket server interface used by the web socket handler to handle the
 * request from the client using web socket API. This interface represents the
 * web socket connection of the WebSocket server.
 * 
 * Note that the number of methods expose by this interface might increase in future 
 * to provide more features.
 * 
 * @author <a href="http://blog.purans.net"> Puran Singh</a> &lt;<a
 *         href="mailto:puran@programmer.net"> puran@programmer.net </a>>
 * @version $Id$
 * 
 */
public interface WebSocket {
	/**
	 * Send the message to the client
	 * 
	 * @param message
	 *            the message to send
	 */
	void send(Object message) throws WebSocketException;

	/**
	 * close the web socket connection
	 */
	void close() throws WebSocketException;

	/**
	 * returns {@code true} if the web socket connection is open {@code false}
	 * otherwise. Alternative method to check if the connection is open or closed 
	 * is to invoke {@code getStatus()} method.
	 * 
	 * @return true or false based on open/closed connection
	 */
	boolean isOpen();

	/**
	 * Returns the port at which web socket server is listening.
	 * @return the port number
	 */
	int getPort();
	
	/**
	 * returns the status of the web socket connection
	 * {@code CONNECTING = 0}
	 * {@code OPEN = 1}
	 * {@code CLOSED = 2}
	 * @return the status value
	 */
	int getReadyState();
}
