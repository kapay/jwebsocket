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
package org.jwebsocket.core.impl;

import org.apache.mina.common.IoSession;
import org.jwebsocket.api.WebSocket;
import org.jwebsocket.api.WebSocketException;
import org.jwebsocket.core.api.WebSocketCallback;
import org.jwebsocket.core.server.StartWebSocketServer;


/**
 * standard implementation of a {@code WebSocket} interface
 * 
 * @author <a href="http://blog.purans.net"> Puran Singh</a> &lt;<a
 *         href="mailto:puran@programmer.net"> puran@programmer.net </a>>
 * @version $Id$
 * 
 */
public final class StandardWebSocket implements WebSocket {

	public static final int CONNECTING = 0;
	public static final int OPEN = 1;
	public static final int CLOSED = 2;

	private final IoSession session;
	private final WebSocketCallback callback;

	private int readyState = CLOSED;

	/**
	 * private default constructor
	 * 
	 * @param status
	 *            the status of the web socket connection
	 */
	private StandardWebSocket(int readyState) {
		this.readyState = readyState;
		this.session = null;
		this.callback = null;
	}

	/**
	 * constructor to create web socket object based on session and web socket
	 * callback
	 * 
	 * @param session
	 *            the io session object
	 * @param callback
	 *            callback for handling callback operations in handler
	 */
	private StandardWebSocket(IoSession session, WebSocketCallback callback,
			int readyState) {
		this.session = session;
		this.callback = callback;
		this.readyState = readyState;
	}

	/**
	 * static factory method that returns the websocket which is closed or not
	 * yet open;
	 * 
	 * @return the closed status web socket object
	 */
	public static WebSocket getClosedWebSocket() {
		return new StandardWebSocket(CLOSED);
	}

	/**
	 * static method that returns the websocket which is in a connecting process
	 * 
	 * @return the connecting web socket object
	 */
	public static WebSocket getConnectingWebSocket() {
		return new StandardWebSocket(CONNECTING);
	}

	/**
	 * Constructor that constructs a websocket object with status open.
	 * 
	 * @param session
	 *            the io session object associated with the web socket
	 *            connection
	 * @param callback
	 *            the callback for delegating calls to the server handler
	 * @return the web socket object
	 */
	public static WebSocket getOpenedWebSocket(IoSession session,
			WebSocketCallback callback) {
		return new StandardWebSocket(session, callback, OPEN);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws WebSocketException {
		if (readyState == CLOSED) {
			throw new WebSocketException(
					"WebSocket is already closed, cannot close closed socket");
		} else {
			readyState = CLOSED;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getPort() {
		String port = StartWebSocketServer.getConfiguration().getConfiguration(
				StartWebSocketServer.PORT);
		return Integer.parseInt(port);
	}

	@Override
	public boolean isOpen() {
		if (readyState == CLOSED) {
			return false;
		}
		return (session.isConnected() && readyState == OPEN);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void send(Object message) throws WebSocketException {
		if (session != null && readyState == OPEN) {
			callback.send(message);
		} else {
			throw new WebSocketException(
					"WebSocket is closed, cannot send any message. Make sure that web sockect is open");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getReadyState() {
		return readyState;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean containsAttribute(Object key) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getAttribute(Object key) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getAttributeValue(Object key, Object defaultValue) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object removeAttribute(Object key) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean removeAttribute(Object key, Object value) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object setAttribute(Object key, Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object setAttribute(Object key) {
		// TODO Auto-generated method stub
		return null;
	}

}
