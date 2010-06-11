//	---------------------------------------------------------------------------
//	jWebSocket - WebSocket TCP Engine
//	Copyright (c) 2010 Alexander Schulze, Innotrade GmbH
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
//	more details.
//	You should have received a copy of the GNU General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/>.
//	---------------------------------------------------------------------------
package org.jwebsocket.tcp.engines;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.config.JWebSocketConstants;
import org.jwebsocket.engines.BaseEngine;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.tcp.connectors.TCPConnector;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.RequestHeader;
import org.jwebsocket.kit.WebSocketException;

/**
 * Implementation of the jWebSocket TCP engine. The TCP engine provide a Java
 * Socket implementation of the WebSocket protocol. It contains the handshake
 * @author aschulze
 */
public class TCPEngine extends BaseEngine {

	private static Logger log = Logging.getLogger(TCPEngine.class);
	private ServerSocket serverSocket = null;
	private int listenerPort = 8787;
	private int sessionTimeout = 120000;
	private boolean isRunning = false;
	Thread engineThread = null;

	/**
	 * Constructor of the TCP engine. The port and the default session timeout
	 * have to be passed. The session timout passed here is used only when no
	 * explicit timeout per connection is specified.
	 * @param aId
	 * @param aPort TCP port the engine listens on.
	 * @param aSessionTimeout The default server side session time out.
	 * @throws WebSocketException
	 */
	public TCPEngine(String aId, Integer aPort, Integer aSessionTimeout)
			throws WebSocketException {
		super(aId);
		listenerPort = aPort;
		sessionTimeout = aSessionTimeout;
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
					+ " with default timeout "
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
			serverSocket.close();
			if (log.isInfoEnabled()) {
				log.info("TCP engine '" + getId() + "' stopped at port " + listenerPort + " (closed=" + serverSocket.isClosed() + ").");
			}
			serverSocket = null;
		} catch (Exception ex) {
			log.error("Stopping TCP engine '" + getId() + "':" + ex.getMessage());
		}

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

	private long calcSecKeyNum(String aKey) {
		StringBuffer lSB = new StringBuffer();
		int lSpaces = 0;
		for (int i = 0; i < aKey.length(); i++) {
			char lC = aKey.charAt(i);
			if (lC == ' ') {
				lSpaces++;
			} else if (lC >= '0' && lC <= '9') {
				lSB.append(lC);
			}
		}
		long lRes = -1;
		if (lSpaces > 0) {
			try {
				lRes = Long.parseLong(lSB.toString()) / lSpaces;
				log.debug("Key: " + aKey + ", Numbers: " + lSB.toString() + ", Spaces: " + lSpaces + ", Result: " + lRes);
			} catch (NumberFormatException ex) {
				// use default result
			}
		}
		return lRes;
	}

	private RequestHeader processHandshake(Socket aClientSocket)
			throws UnsupportedEncodingException, IOException {

		RequestHeader header = new RequestHeader();

		String host = null;
		String origin = null;
		String location = null;
		String secKey1 = null;
		String secKey2 = null;
		byte[] secKey3 = new byte[8];
		boolean isSecure = false;
		Long secNum1 = null;
		Long secNum2 = null;
		byte[] secKeyResponse = new byte[8];

		Map<String, String> args = new HashMap<String, String>();
		String path = "";
		String searchString = "";

		InputStream is = aClientSocket.getInputStream();
		OutputStream os = aClientSocket.getOutputStream();

		byte[] lTest = new byte[8192];
		int lRead = is.read(lTest);

		String req = new String(lTest, 0, lRead, "US-ASCII");
		isSecure = (req.indexOf("Sec-WebSocket") > 0);

		if (isSecure) {
			lRead -= 8;
			for (int i = 0; i < 8; i++) {
				secKey3[i] = lTest[lRead + i];
			}
		}

		if (log.isDebugEnabled()) {
			log.debug("Received "
					+ (isSecure ? "secured" : "unsecured")
					+ " Header (" + req.replace("\r\n", "\\n") + ")");
		}

		// now parse header for correct handshake....
		// get host....
		int pos = req.indexOf("Host:");
		pos += 6;
		host = req.substring(pos);
		pos = host.indexOf("\r\n");
		host = host.substring(0, pos);
		// get origin....
		pos = req.indexOf("Origin:");
		pos += 8;
		origin = req.substring(pos);
		pos = origin.indexOf("\r\n");
		origin = origin.substring(0, pos);
		// get path....
		pos = req.indexOf("GET");
		pos += 4;
		path = req.substring(pos);
		pos = path.indexOf("HTTP");
		path = path.substring(0, pos - 1);

		// the following section implements the sec-key process in WebSocket Draft 76
		/*
		To prove that the handshake was received, the server has to take
		three pieces of information and combine them to form a response.  The
		first two pieces of information come from the |Sec-WebSocket-Key1|
		and |Sec-WebSocket-Key2| fields in the client handshake.

		Sec-WebSocket-Key1: 18x 6]8vM;54 *(5:  {   U1]8  z [  8
		Sec-WebSocket-Key2: 1_ tx7X d  <  nw  334J702) 7]o}` 0

		For each of these fields, the server has to take the digits from the
		value to obtain a number (in this case 1868545188 and 1733470270
		respectively), then divide that number by the number of spaces
		characters in the value (in this case 12 and 10) to obtain a 32-bit
		number (155712099 and 173347027).  These two resulting numbers are
		then used in the server handshake, as described below.
		 */
		pos = req.indexOf("Sec-WebSocket-Key1:");
		if (pos > 0) {
			pos += 20;
			secKey1 = req.substring(pos);
			pos = secKey1.indexOf("\r\n");
			secKey1 = secKey1.substring(0, pos);
			secNum1 = calcSecKeyNum(secKey1);
			log.debug("Sec-WebSocket-Key1:" + secKey1 + " => " + secNum1);
		}
		pos = req.indexOf("Sec-WebSocket-Key2:");
		if (pos > 0) {
			pos += 20;
			secKey2 = req.substring(pos);
			pos = secKey2.indexOf("\r\n");
			secKey2 = secKey2.substring(0, pos);
			secNum2 = calcSecKeyNum(secKey2);
			log.debug("Sec-WebSocket-Key2:" + secKey2 + " => " + secNum2);
		}

		/*
		The third piece of information is given after the fields, in the last
		eight bytes of the handshake, expressed here as they would be seen if
		interpreted as ASCII: Tm[K T2u
		The concatenation of the number obtained from processing the |Sec-
		WebSocket-Key1| field, expressed as a big-endian 32 bit number, the
		number obtained from processing the |Sec-WebSocket-Key2| field, again
		expressed as a big-endian 32 bit number, and finally the eight bytes
		at the end of the handshake, form a 128 bit string whose MD5 sum is
		then used by the server to prove that it read the handshake.
		 */

		if (secNum1 != null && secNum2 != null) {

			log.debug("Sec-WebSocket-Key3:" + new String(secKey3, "UTF-8"));
			BigInteger sec1 = new BigInteger(secNum1.toString());
			BigInteger sec2 = new BigInteger(secNum2.toString());

			// concatene 3 parts secNum1 + secNum2 + secKey
			byte[] l128Bit = new byte[16];
			byte[] lTmp;
			lTmp = sec1.toByteArray();
			for (int i = 0; i < 4; i++) {
				l128Bit[i] = lTmp[i];
			}
			lTmp = sec2.toByteArray();
			for (int i = 0; i < 4; i++) {
				l128Bit[i + 4] = lTmp[i];
			}
			lTmp = secKey3;
			for (int i = 0; i < 8; i++) {
				l128Bit[i + 8] = lTmp[i];
			}
			// build md5 sum of this new 128 byte string
			try {
				MessageDigest md = MessageDigest.getInstance("MD5");
				secKeyResponse = md.digest(l128Bit);
			} catch (Exception ex) {
				log.error("getMD5: " + ex.getMessage());
			}
		}

		// isolate search string
		pos = path.indexOf(JWebSocketConstants.PATHARG_SEPARATOR);
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

		// create location based on ws:// + host + path
		location = "ws://" + host + path;
		if (log.isDebugEnabled()) {
			log.debug("Parsed header ("
					+ "host: " + host + ", "
					+ "origin: " + origin + ", "
					+ "location: " + location + ", "
					+ "path: " + path + ", "
					+ "searchString: " + searchString
					+ ")");
		}
		// now that we have parsed the header send handshake...
		// since 0.9.0.0609 considering Sec-WebSocket-Key processing
		String res =
				"HTTP/1.1 101 Web Socket Protocol Handshake\r\n"
				+ "Upgrade: WebSocket\r\n"
				+ "Connection: Upgrade\r\n"
				+ (isSecure ? "Sec-" : "") + "WebSocket-Origin: " + origin + "\r\n"
				+ (isSecure ? "Sec-" : "") + "WebSocket-Location: " + location + "\r\n"
				+ "\r\n";

		if (log.isDebugEnabled()) {
			log.debug("Sent handshake (" + res.replace("\r\n", "\\n") + ")");
		}

		byte[] ba = res.getBytes("US-ASCII");
		os.write(ba);
		// if Sec-WebSocket-Keys are used send security response first
		if (isSecure) {
			os.write(secKeyResponse);
		}
		os.flush();

		if (log.isDebugEnabled()) {
			log.debug("Handshake flushed.");
		}

		// set default sub protocol if none passed
		if (args.get("prot") == null) {
			args.put("prot", JWebSocketConstants.SUB_PROT_DEFAULT);
		}
		header.put("args", args);
		header.put("origin", origin);
		header.put("location", location);
		header.put("path", path);
		header.put("searchString", searchString);
		header.put("host", host);

		return header;
	}

	@Override
	/*
	 * Returns {@code true} if the TCP engine is running or {@code false} 
	 * otherwise. The alive status represents the state of the TCP engine
	 * listener thread.
	 */
	public boolean isAlive() {
		// TODO: Check isAlive state of TCPEngine
		return true;
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
