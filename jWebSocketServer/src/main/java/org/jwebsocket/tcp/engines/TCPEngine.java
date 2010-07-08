//	---------------------------------------------------------------------------
//	jWebSocket - WebSocket TCP Engine
//	Copyright (c) 2010 Alexander Schulze, Innotrade GmbH
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
package org.jwebsocket.tcp.engines;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import javolution.util.FastMap;

import org.apache.log4j.Logger;
import org.jwebsocket.api.EngineConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.config.JWebSocketConstants;
import org.jwebsocket.engines.BaseEngine;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.tcp.connectors.TCPConnector;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.RequestHeader;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.kit.WebSocketHandshake;

/**
 * Implementation of the jWebSocket TCP engine. The TCP engine provide a Java
 * Socket implementation of the WebSocket protocol. It contains the handshake
 * @author aschulze
 */
public class TCPEngine extends BaseEngine {

	private static Logger log = Logging.getLogger(TCPEngine.class);
	private ServerSocket serverSocket = null;
	private int listenerPort = JWebSocketConstants.DEFAULT_PORT;
	private int sessionTimeout = JWebSocketConstants.DEFAULT_TIMEOUT;
	private boolean isRunning = false;
	Thread engineThread = null;

	public TCPEngine(EngineConfiguration configuration) {
		super(configuration);
		listenerPort = configuration.getPort();
		sessionTimeout = configuration.getTimeout();
	}

	@Override
	public void startEngine()
			throws WebSocketException {
		if (log.isDebugEnabled()) {
			log.debug("Starting TCP engine '"
					+ getId()
					+ "' at port " + listenerPort
					+ " with default timeout "
					+ (sessionTimeout > 0 ? sessionTimeout + "ms" : "infinite")
					+ "...");
		}
		try {
			serverSocket = new ServerSocket(listenerPort);
			/*
			serverSocket = new ServerSocket(listenerPort); // listenerPort
			serverSocket.setReuseAddress(true);
			InetSocketAddress lISA = new InetSocketAddress(listenerPort);
			serverSocket.bind(lISA);
			 */
			setSessionTimeout(sessionTimeout);

			EngineListener listener = new EngineListener(this);
			engineThread = new Thread(listener);
			engineThread.start();

		} catch (IOException ex) {
			throw new WebSocketException(ex.getMessage());
		}

		// TODO: results in firing started event twice! make more clean!
		// super.startEngine();
		if (log.isInfoEnabled()) {
			log.info("TCP engine '"
					+ getId() + "' started' at port "
					+ listenerPort + " with default timeout "
					+ (sessionTimeout > 0 ? sessionTimeout + "ms" : "infinite")
					+ "...");
		}
	}

	@Override
	public void stopEngine(CloseReason aCloseReason)
			throws WebSocketException {
		if (log.isDebugEnabled()) {
			log.debug("Stopping TCP engine '" + getId() + "' at port " + listenerPort + "...");
		}

		// inherited method stops all connectors
		super.stopEngine(aCloseReason);

		// resetting "isRunning" causes engine listener to terminate
		isRunning = false;
		long lStarted = new Date().getTime();

		try {
			// when done, close server socket
			// closing the server socket should lead to an IOExeption
			// at accept in the listener thread which terminates the listener
			if (serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
				if (log.isInfoEnabled()) {
					log.info("TCP engine '" + getId() + "' stopped at port " + listenerPort + " (closed=" + serverSocket.isClosed() + ").");
				}
				serverSocket = null;
			} else {
				log.warn("Stopping TCP engine '" + getId() + "': no server socket or server socket closed.");
			}
		} catch (Exception ex) {
			log.error(ex.getClass().getSimpleName() + " on stopping TCP engine '" + getId() + "': " + ex.getMessage());
		}

		if (engineThread != null) {
			try {
				engineThread.join(10000);
			} catch (Exception ex) {
				log.error(ex.getClass().getSimpleName() + ": " + ex.getMessage());
			}
			if (log.isDebugEnabled()) {
				long lDuration = new Date().getTime() - lStarted;
				if (engineThread.isAlive()) {
					log.warn("TCP engine '" + getId() + "' did not stopped after " + lDuration + "ms.");
				} else {
					log.debug("TCP engine '" + getId() + "' stopped after " + lDuration + "ms.");
				}
			}
		}
	}

	@Override
	public void connectorStarted(WebSocketConnector aConnector) {
		if (log.isDebugEnabled()) {
			log.debug("Detected new connector at port " + aConnector.getRemotePort() + ".");
		}
		super.connectorStarted(aConnector);
	}

	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
		if (log.isDebugEnabled()) {
			log.debug("Detected stopped connector at port " + aConnector.getRemotePort() + ".");
		}
		super.connectorStopped(aConnector, aCloseReason);
	}

	private RequestHeader processHandshake(Socket aClientSocket)
			throws UnsupportedEncodingException, IOException {

		InputStream is = aClientSocket.getInputStream();
		OutputStream os = aClientSocket.getOutputStream();

		byte[] lBuff = new byte[8192];
		int lRead = is.read(lBuff);
		byte[] lResp = new byte[lRead];
		System.arraycopy(lBuff, 0, lResp, 0, lRead);

		FastMap lRespMap = WebSocketHandshake.parseC2SRequest(lResp);
		// maybe the request is a flash policy-file-request
		String lFlashBridgeReq = (String) lRespMap.get("policy-file-request");
		if (lFlashBridgeReq != null) {
			log.warn("TCPEngine returned policy file request ('" + lFlashBridgeReq + "'), check for FlashBridge plug-in.");
		}
		// generate the websocket handshake
		// if policy-file-request is found answer it
		byte[] ba = WebSocketHandshake.generateS2CResponse(lRespMap);
		if (ba == null) {
			if (log.isDebugEnabled()) {
				log.warn("TCPEngine detected illegal handshake.");
			}
			return null;
		}
		os.write(ba);
		os.flush();

		// if we detected a flash policy-file-request return "null"
		// (no websocket header detected)
		if (lFlashBridgeReq != null) {
			log.warn("TCPEngine returned policy file response ('" + new String(ba, "US-ASCII") + "'), check for FlashBridge plug-in.");
			return null;
		}

		RequestHeader header = new RequestHeader();
		FastMap<String, String> args = new FastMap<String, String>();
		String path = (String) lRespMap.get("path");

		// isolate search string
		String searchString = "";
		if (path != null) {
			int pos = path.indexOf(JWebSocketConstants.PATHARG_SEPARATOR);
			if (pos >= 0) {
				searchString = path.substring(pos + 1);
				if (searchString.length() > 0) {
					String[] lArgs = searchString.split(JWebSocketConstants.ARGARG_SEPARATOR);
					for (int i = 0; i < lArgs.length; i++) {
						String[] lKeyValuePair = lArgs[i].split(JWebSocketConstants.KEYVAL_SEPARATOR, 2);
						if (lKeyValuePair.length == 2) {
							args.put(lKeyValuePair[0], lKeyValuePair[1]);
							if (log.isDebugEnabled()) {
								log.debug("arg" + i + ": " + lKeyValuePair[0] + "=" + lKeyValuePair[1]);
							}
						}
					}
				}
			}
		}

		if (log.isDebugEnabled()) {
			log.debug("Handshake flushed.");
		}

		// set default sub protocol if none passed
		if (args.get("prot") == null) {
			args.put("prot", JWebSocketConstants.SUB_PROT_DEFAULT);
		}

		header.put("host", lRespMap.get("host"));
		header.put("origin", lRespMap.get("origin"));
		header.put("location", lRespMap.get("location"));

		header.put("path", lRespMap.get("path"));
		header.put("searchString", searchString);
		header.put("args", args);

		return header;
	}

	@Override
	/*
	 * Returns {@code true} if the TCP engine is running or {@code false} 
	 * otherwise. The alive status represents the state of the TCP engine
	 * listener thread.
	 */
	public boolean isAlive() {
		return (engineThread != null && engineThread.isAlive());
	}

	private class EngineListener implements Runnable {

		private WebSocketEngine engine = null;

		/**
		 * Creates the server socket listener for new
		 * incoming socket connections.
		 * @param aEngine
		 */
		public EngineListener(WebSocketEngine aEngine) {
			engine = aEngine;
		}

		@Override
		public void run() {

			// notify server that engine has started
			engineStarted();

			isRunning = true;
			while (isRunning) {
				try {
					// accept is blocking so here is no need
					// to put any sleeps into this loop
					// if (log.isDebugEnabled()) {
					//	log.debug("Waiting for client...");
					// }
					Socket clientSocket = serverSocket.accept();
					try {
						// process handshake to parse header data
						RequestHeader header = processHandshake(clientSocket);
						if (header != null) {
							// set socket timeout to given amount of milliseconds
							// use tcp engine's timeout as default and
							// check system's min and max timeout ranges
							int lSessionTimeout = header.getTimeout(getSessionTimeout());
							/* min and max range removed since 0.9.0.0602, see config documentation
							if (lSessionTimeout > JWebSocketConstants.MAX_TIMEOUT) {
							lSessionTimeout = JWebSocketConstants.MAX_TIMEOUT;
							} else if (lSessionTimeout < JWebSocketConstants.MIN_TIMEOUT) {
							lSessionTimeout = JWebSocketConstants.MIN_TIMEOUT;
							}
							 */
							if (log.isDebugEnabled()) {
								log.debug("Client accepted on port "
										+ clientSocket.getPort()
										+ " with timeout "
										+ (lSessionTimeout > 0 ? lSessionTimeout + "ms" : "infinite")
										+ "...");
							}
							if (lSessionTimeout > 0) {
								clientSocket.setSoTimeout(lSessionTimeout);
							}
							// create connector and pass header
							// log.debug("Instantiating connector...");
							WebSocketConnector connector = new TCPConnector(engine, clientSocket);
							// log.debug("Setting header to engine...");
							connector.setHeader(header);
							// log.debug("Adding connector to engine...");
							getConnectors().put(connector.getId(), connector);
							if (log.isDebugEnabled()) {
								log.debug("Starting connector...");
							}
							connector.startConnector();
						} else {
							// if header could not be parsed properly
							// immediately disconnect the client.
							clientSocket.close();
						}
					} catch (UnsupportedEncodingException ex) {
						log.error("(encoding) " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
					} catch (IOException ex) {
						log.error("(io) " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
					} catch (Exception ex) {
						log.error("(other) " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
					}
				} catch (Exception ex) {
					isRunning = false;
					log.error("(accept) " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
				}
			}

			// notify server that engine has stopped
			// this closes all connections
			engineStopped();
		}
	}
}
