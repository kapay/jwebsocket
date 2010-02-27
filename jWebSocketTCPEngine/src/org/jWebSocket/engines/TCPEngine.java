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
package org.jWebSocket.engines;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.jWebSocket.api.IDataPacket;
import org.jWebSocket.api.IWebSocketConnector;
import org.jWebSocket.api.IWebSocketEngine;
import org.jWebSocket.config.Config;
import org.jWebSocket.connectors.TCPConnector;
import org.jWebSocket.kit.Header;
import org.jWebSocket.kit.WebSocketException;

/**
 *
 * @author aschulze
 */
public class TCPEngine extends BaseEngine {

	private ServerSocket serverSocket = null;
	private boolean isRunning = false;

	public TCPEngine(int aPort, int aSessionTimeout)
		throws WebSocketException {
		startEngine(aPort, aSessionTimeout);
	}

	/**
	 *
	 * @param aPort
	 * @param aSessionTimeout
	 * @throws WebSocketException
	 */
	public void startEngine(int aPort, int aSessionTimeout)
		throws WebSocketException {
		try {
			serverSocket = new ServerSocket(aPort);
			setSessionTimeout(aSessionTimeout);

			ServerProcessor serverProc = new ServerProcessor(this);
			Thread serverThread = new Thread(serverProc);
			serverThread.start();

		} catch (IOException ex) {
			throw new WebSocketException(ex.getMessage());
		}

		super.startEngine();
	}

	@Override
	public void stopEngine()
		throws WebSocketException {
		// inherited method stops all connectors
		super.stopEngine();
		isRunning = false;
		try {
			// when done, close server socket
			serverSocket.close();
		} catch (Exception ex) {
			// log.info("Exception on sleep " + ex.getMessage());
		}
	}

	/**
	 *
	 * @param aClientSocket
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	public static Header processHandshake(Socket aClientSocket)
		throws UnsupportedEncodingException, IOException {

		Header header = new Header();

		String host = null;
		String origin = null;
		String location = null;
		Map args = new HashMap<String, String>();
		String path = "";
		String searchString = "";

		BufferedReader br = null;
		PrintStream os = null;

		br = new BufferedReader(new InputStreamReader(aClientSocket.getInputStream(), "UTF-8"));
		os = new PrintStream(aClientSocket.getOutputStream(), true, "UTF-8");

		// allow descending classes to handle clientThreadStarted event
		// clientThreadStarted();

		// read complete header first...
		String line = br.readLine();
		String req = "";
		while (line != null && line.length() > 0) {
			req += line + "\n";
			line = br.readLine();
		}
		// uncomment for debug purposes
		// System.out.println("BaseServer: Received Header...\n");
		// System.out.println(req);

		// now parse header for correct handshake....
		// get host....
		int pos = req.indexOf("Host:");
		pos += 6;
		host = req.substring(pos);
		pos = host.indexOf("\n");
		host = host.substring(0, pos);
		// get origin....
		pos = req.indexOf("Origin:");
		pos += 8;
		origin = req.substring(pos);
		pos = origin.indexOf("\n");
		origin = origin.substring(0, pos);
		// get path....
		pos = req.indexOf("GET");
		pos += 4;
		path = req.substring(pos);
		pos = path.indexOf("HTTP");
		path = path.substring(0, pos - 1);

		// isolate search string
		pos = path.indexOf(Config.PATHARG_SEPARATOR);
		if (pos >= 0) {
			searchString = path.substring(pos + 1);
			if (searchString.length() > 0) {
				String[] lArgs = searchString.split(Config.ARGARG_SEPARATOR);
				for (int i = 0; i < lArgs.length; i++) {
					String[] lKeyValuePair = lArgs[i].split(Config.KEYVAL_SEPARATOR, 2);
					if (lKeyValuePair.length == 2) {
						args.put(lKeyValuePair[0], lKeyValuePair[1]);
						// uncomment for debug purposes
						// System.out.println("BaseServer: arg" + i + ": " + lKeyValuePair[0] + "=" + lKeyValuePair[1]);
					}
				}
			}
		}

		// create location based on ws:// + host + path
		location = "ws://" + host + path;

		// uncomment for debug purposes
		// System.out.println("BaseServer: Parsed Header...\n");
		// System.out.println("BaseServer: host: " + host);
		// System.out.println("BaseServer: origin: " + origin);
		// System.out.println("BaseServer: location: " + location);
		// System.out.println("BaseServer: path: " + path);
		// System.out.println("BaseServer: searchString: " + searchString);

		// now that we have parsed the header send handshake...
		String res =
			"HTTP/1.1 101 Web Socket Protocol Handshake\r\n"
			+ "Upgrade: WebSocket\r\n"
			+ "Connection: Upgrade\r\n"
			+ "WebSocket-Origin: " + origin + "\r\n"
			+ "WebSocket-Location: " + location + "\r\n"
			+ "\r\n";
		// uncomment for debug purposes
		// System.out.println("BaseServer: Sent handshake \n" + res);

		byte[] ba = res.getBytes("UTF-8");
		os.write(ba);
		os.flush();
		// uncomment for debug purposes
		// System.out.println("Handshake flushed.");

		header.put("args", args);

		header.put("origin", origin);
		header.put("location", location);
		header.put("path", path);
		header.put("searchString", searchString);
		header.put("host", host);

		return header;
	}

	public void processPacket(IWebSocketConnector aConnector, IDataPacket aDataPacket) {
		getServer().processPacket(this, aConnector, aDataPacket);
	}

	public boolean isAlive() {
		return true;
	}

	public class ServerProcessor implements Runnable {

		private IWebSocketEngine engine = null;

		public ServerProcessor(IWebSocketEngine aEngine) {
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
					Socket clientSocket = serverSocket.accept();

					try {
						// process handshake to parse header data
						Header header = processHandshake(clientSocket);

						// set socket timeout to given amount of milliseconds
						// check min and max timeout ranges
						int lSessionTimeout = header.getTimeout(Config.DEFAULT_TIMEOUT);
						if (lSessionTimeout > Config.MAX_TIMEOUT) {
							lSessionTimeout = Config.MAX_TIMEOUT;
						} else if (lSessionTimeout < Config.MIN_TIMEOUT) {
							lSessionTimeout = Config.MIN_TIMEOUT;
						}
						clientSocket.setSoTimeout(lSessionTimeout);

						// create connector and pass header
						IWebSocketConnector connector = new TCPConnector(engine, clientSocket, header);
						getConnectors().add(connector);
						connector.startConnector();

						// allow descendant classes to handle connector started event
						connectorStarted(connector);

					} catch (UnsupportedEncodingException ex) {
						// ignore this exception for now
						System.out.println("BaseServer: UnsupportedEncodingException: " + ex.getMessage());
					} catch (IOException ex) {
						// ignore this exception for now
						System.out.println("BaseServer: IOException: " + ex.getMessage());
					}
				} catch (Exception ex) {
					isRunning = false;
					// log.info("Exception on starting ServerSocket " + ex.getMessage());
				}
			}

			// allow descendant classes to handle server stop event
			engineStopped();
		}
	}

	/**
	 *
	 * @param aRemotePort
	 * @return
	 */
	public IWebSocketConnector getConnectorByRemotePort(int aRemotePort) {
		Iterator lIterator = getConnectors().iterator();
		while (lIterator.hasNext()) {
			TCPConnector lConnector = (TCPConnector) lIterator.next();
			if (lConnector.getClientSocket().getPort() == aRemotePort) {
				return lConnector;
			}
		}
		return null;
	}
}
