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
package org.jwebsocket.core.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.mina.common.CloseFuture;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.IoSessionConfig;
import org.jwebsocket.api.WebSocket;
import org.jwebsocket.api.WebSocketException;
import org.jwebsocket.api.WebSocketHandler;
import org.jwebsocket.core.api.Headers;
import org.jwebsocket.core.api.ResourceHandler;
import org.jwebsocket.core.api.Response;
import org.jwebsocket.core.api.WebSocketCallback;
import org.jwebsocket.core.impl.HttpRequest;
import org.jwebsocket.core.impl.HttpResponse;
import org.jwebsocket.core.impl.StandardWebSocket;
import org.jwebsocket.core.impl.WebSocketProtocol;
import org.jwebsocket.core.impl.WebSocketRequest;
import org.jwebsocket.core.impl.WebSocketResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This handler is the main engine that drives the WebSocket server, it recieves
 * the message and triggers the events on the {@code WebSocketHandler} object.
 * 
 * <p>
 * It handles the two cases:
 * <ul>
 * <li>If the request message recieved is {@code HttpRequest} in that case it
 * checks if the HTTP request is the handshake request with HTTP Upgrade value
 * 'WebSocket' to open the web socket connection or HTTP GET request for the
 * resource file. If it's a HTTP upgrade for web socket connection it sends the
 * handshake response and if it's a normal GET request for resource it serves
 * the resource file from the root directory defined.</li>
 * <li>
 * If the request message received is {@code WebSocketRequest}</li>
 * </p>
 * 
 * In case of {@code sessionClosed()} and {@code exceptionCaught()} it creates a new 
 * {@code WebSocket} object with CLOSING status to block any of the I/O operations
 * and triggers {@code onClose()} event of the {@code WebSocketHandler} with the 
 * CLOSING {@code WebSocket} object.
 * 
 * @author <a href="http://blog.purans.net"> Puran Singh</a> &lt;<a
 *         href="mailto:puran@programmer.net"> puran@programmer.net </a>>
 * @version $Id: WebSocketServerHandler.java 29 2010-01-20 07:38:01Z mailtopuran
 *          $
 * 
 */
public class WebSocketServerHandler extends IoHandlerAdapter {

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	private ResourceHandler resourceHandler = null;

	private static WebSocketHandler handler = null;

	/**
	 * initialize the WebSocket with unknown status
	 */
	private static StandardWebSocket webSocket = StandardWebSocket
			.getWebSocket();
	
	
	private final WebSocketCallback CLOSING_CALLBACK = new WebSocketCallback() {
		@Override
		public void send(Object object) throws WebSocketException {
			throw new WebSocketException("Connection is closing, cannot send message");
		}

		@Override
		public void cleanupSession(IoSession session) {
			closeSession(session);
		}
	};

	@SuppressWarnings("unchecked")
	/**
	 * constructor that initializes the {@code WebSocketHandler} implementation
	 * @param handlerName the name of the WebSocket handler 
	 * @param docRoot the root folder for static resources 
	 */
	public WebSocketServerHandler(String handlerName, String docRoot) {
		try {
			Class<WebSocketHandler> clazz = (Class<WebSocketHandler>) Class
					.forName(handlerName);

			resourceHandler = new StaticResourceHandler(docRoot);

			handler = clazz.newInstance();
			handler.init(StartWebSocketServer.getHandlers().getProperties(
					handlerName));
		} catch (ClassNotFoundException e) {
			LOGGER.error("Error instantiating handler", e);
			handler.onException(webSocket, e);
		} catch (InstantiationException e) {
			LOGGER.error("Error instantiating handler", e);
			handler.onException(webSocket, e);
		} catch (IllegalAccessException e) {
			LOGGER.error("Error instantiating handler", e);
			handler.onException(webSocket, e);
		}
	}

	/**
	 * private method that is invoked when HTTP message is received.
	 * 
	 * @param session
	 *            the io session object
	 * @param request
	 *            the http request object
	 * @return the response object
	 * @throws WebSocketException
	 */
	private Response httpMessageReceived(IoSession session, HttpRequest request)
			throws WebSocketException {
		Response response = null;
		Headers headers = request.getHeaders();
		if (headers.getFirstHeaderValue(HttpRequest.REQUEST_METHOD).equals(
				HttpRequest.GET)) {
			if (headers.contains(HttpRequest.UPGRADE)
					&& headers.getFirstHeaderValue(HttpRequest.UPGRADE).equals(
							HttpRequest.WEBSOCKET)) {
				// change the websocket connection to connecting status
				webSocket = StandardWebSocket.getConnectingWebSocket();

				response = HttpResponse.buildWebSocketHandshakeResponse();
				String host = headers.getFirstHeaderValue(HttpRequest.HOST);
				String origin = headers.getFirstHeaderValue(HttpRequest.ORIGIN);
				String context = headers
						.getFirstHeaderValue(HttpRequest.CONTEXT);
				String location = "ws://" + host + "/" + context;
				try {
					response.appendBody(WebSocketProtocol.INITIAL_RESPONSE);
					response.appendBody(origin.getBytes("ASCII"));
					response.appendBody(WebSocketProtocol.LOCATION);
					response.appendBody(location.getBytes("ASCII"));
					// TODO: need to handler subprotocol in handshake response
					// here.
					response.appendBody(WebSocketProtocol.HANDSHAKE_TERMINATOR);
				} catch (UnsupportedEncodingException e) {
					LOGGER.error(
							"IO Exception while writing handshake response", e);
					throw new WebSocketException(
							"IO Exception while writing handshake response", e);
				}
			} else if (!headers.getFirstHeaderValue(HttpRequest.CONTEXT)
					.equals("favicon.ico")) {
				String resource = headers
						.getFirstHeaderValue(HttpRequest.CONTEXT);
				response = resourceHandler.getResourceResponse(resource);
			}
		} else {
			// POST
		}
		return response;
	}

	/**
	 * method invoked when web socket request is recieved
	 * 
	 * @param session
	 *            the io session object
	 * @param message
	 *            the web socket request message
	 * @return response the response object
	 */
	private void webSocketMessageReceived(IoSession session,
			WebSocketRequest message) {
		Object messageData = WebSocketProtocol.getStringData(message.getBody());
		handler.onMessage(webSocket, messageData);
	}

	@Override
	public void messageReceived(IoSession session, Object message)
			throws WebSocketException {
		Response response = null;
		if (message instanceof HttpRequest) {

			response = httpMessageReceived(session, (HttpRequest) message);

			if (response != null) {
				// write the response
				session.write(response);

				// if it's the websocket handshake response,
				if (response.isWebSocketHandShakeResponse()) {
					IoSessionConfig config = session.getConfig();
					config.setUseReadOperation(true);
					webSocket = createNewWebSocket(session);
					if (webSocket != null) {
						handler.onOpen(webSocket);
					}
				}
			}
		} else if (message instanceof WebSocketRequest) {
			webSocketMessageReceived(session, (WebSocketRequest) message);
		} else {
			// ignore
		}
	}

	/**
	 * Creates a new web socket object as soon as the connection is opened
	 * 
	 * @param session
	 *            the session object
	 * @return the new web socket object
	 */
	private StandardWebSocket createNewWebSocket(final IoSession session) {
		WebSocketCallback callback = new WebSocketCallback() {
			@Override
			public void send(Object object) throws WebSocketException {
				Response response = WebSocketResponse.buildWebSocketResponse();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				try {
					ObjectOutputStream oos = new ObjectOutputStream(baos);
					oos.writeObject(object);
				} catch (IOException ioe) {
					LOGGER.error("IO error while write response", ioe);
				}
				byte[] responseBytes = baos.toByteArray();
				response.appendBody(responseBytes);
				session.write(response);
			}

			@Override
			public void cleanupSession(IoSession sessionToClean) {
				closeSession(sessionToClean);
			}
		};

		webSocket = StandardWebSocket.getOpenedWebSocket(session, callback);

		return webSocket;
	}

	/**
	 * closes the session
	 * 
	 * @param session
	 *            the session object
	 */
	public void closeSession(IoSession session) {
		// do the clean up if internal session is not closed
		if (session != null && (session.isClosing() || !session.isConnected())) {
			CloseFuture future = session.close(true);
			try {
				//initialize socket connection object to UNKNOWN
				webSocket = StandardWebSocket.getWebSocket();
				future.await();
				LOGGER.info("Closed session:"+session.getId());
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	@Override
	public void sessionOpened(IoSession session) {
		// set idle time to 60 seconds
		session.getConfig().setIdleTime(IdleStatus.BOTH_IDLE, 3600);
	}

	/**
	 * {@inheritDoc} 
	 */
	@Override
	public void sessionClosed(IoSession session) {
		LOGGER.info("Starting to close session:" + session.getId());
		// if the session is unknown to the handlers close
		// immediately without triggering close event
		if (webSocket.isUnknownToHandlers(session)) {
			//NOTE: Not sure why but MINA is sending new session object
			// with totally different id when the session is closed before sending the
			// session object which was actually closed.
			closeSession(session);
		} else {
			webSocket = StandardWebSocket.getClosingWebSocket(session, CLOSING_CALLBACK);
			handler.onClose(webSocket);

			// check if client handlers didn't close socket then close it.
			//relax.
			if (webSocket.getReadyState() == StandardWebSocket.CLOSING
					|| webSocket.getReadyState() == StandardWebSocket.OPEN) {
				closeSession(session);
			}
		}
	}

	/**
	 * {@inheritDoc} 
	 */
	@Override
	public void sessionIdle(IoSession session, IdleStatus status) {
		closeSession(session);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void exceptionCaught(IoSession session, Throwable cause) {
		webSocket = StandardWebSocket.getClosingWebSocket(session, CLOSING_CALLBACK);
		handler.onException(webSocket, cause);
		// check if client handlers didn't close socket then close it.
		//relax.
		if (webSocket.getReadyState() == StandardWebSocket.CLOSING
				|| webSocket.getReadyState() == StandardWebSocket.OPEN) {
			closeSession(session);
		}
	}

	/**
	 * static method that returns the current web socket handler.
	 * 
	 * @return the web socket handler
	 */
	public static WebSocketHandler getWebSocketHandler() {
		return handler;
	}

	/**
	 * static method to return the websocket object associated with this handler
	 * 
	 * @return the web socket object
	 */
	public static WebSocket getWebSocket() {
		return webSocket;
	}
}
