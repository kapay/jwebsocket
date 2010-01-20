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
 * Standard implementation of a {@code WebSocket} interface to represent 
 * the single socket connection it also maintains session specific attributes
 * through MINA's {@code IoSession} object internally.
 * 
 * This class cannot be overridden
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

	/** two more status for internal operation */
	public static final int UNKNOWN = -1;
	public static final int CLOSING = 3;

	private final IoSession session;
	private final WebSocketCallback callback;

	private volatile int readyState;

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
	 * Returns the new WebSocket connection with UNKNOWN status
	 * 
	 * @return the websocket connection
	 */
	public static StandardWebSocket getWebSocket() {
		return new StandardWebSocket(null, null, UNKNOWN);
	}

	/**
	 * static method that returns the websocket which is in a connecting process
	 * 
	 * @return the connecting web socket object
	 */
	public static StandardWebSocket getConnectingWebSocket() {
		return new StandardWebSocket(null, null, CONNECTING);
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
	public static StandardWebSocket getOpenedWebSocket(IoSession session,
			WebSocketCallback callback) {
		return new StandardWebSocket(session, callback, OPEN);
	}

	/**
	 * static method that returns the closing websocket which is in a connecting
	 * process
	 * 
	 * @param session
	 *            the session object
	 * @param callback
	 *            the callback object for closing connection
	 * @return the connecting web socket object
	 */
	public static StandardWebSocket getClosingWebSocket(IoSession session,
			WebSocketCallback callback) {
		return new StandardWebSocket(session, callback, CLOSING);
	}

	/**
	 * static factory method that returns the websocket which is closed or not
	 * yet open;
	 * 
	 * @param session
	 *            the closed session object
	 * @return the closed status web socket object
	 */
	public static StandardWebSocket getClosedWebSocket(IoSession session) {
		return new StandardWebSocket(session, null, CLOSED);
	}

	/**
	 * Returns {@code true} if the session is unknown to the 
	 * {@code WebSocketHandler} implementations
	 * 
	 * @return true or false if it is known/unknown to the application handlers
	 */
	public boolean isUnknownToHandlers(IoSession session) {
		if (this.session == null || session == null) {
			return true;
		}

		if (this.session.getId() != session.getId()) {
			return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc} 
	 * TODO: fix this to make id unique for each sockect
	 * connection
	 */
	@Override
	public long getId() {
		return session.getId();
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
		//if socket is either closing or open then clean up
		if (this.callback != null && this.session != null) {
			this.callback.cleanupSession(this.session);
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
		if (readyState == CLOSED || readyState == UNKNOWN
				|| readyState == CLOSING || readyState == CONNECTING) {
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
		return session.containsAttribute(key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getAttribute(Object key) {
		return session.getAttribute(key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getAttributeValue(Object key, Object defaultValue) {
		return session.getAttribute(key, defaultValue);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object removeAttribute(Object key) {
		return session.removeAttribute(key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean removeAttribute(Object key, Object value) {
		return session.removeAttribute(key, value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object setAttribute(Object key, Object value) {
		return session.setAttribute(key, value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object setAttribute(Object key) {
		return session.setAttribute(key);
	}

}
