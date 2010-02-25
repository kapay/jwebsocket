//	---------------------------------------------------------------------------
//	jWebSocket - WebSocket Basic Server
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
package org.jWebSocket.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javolution.util.FastList;
import org.jWebSocket.config.Config;
import org.jWebSocket.plugins.PlugInChain;
import org.jWebSocket.connectors.BaseConnector;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author aschulze
 */
public class BaseServer extends Thread {

	private int sessionTimeout = Config.DEFAULT_TIMEOUT;
	private ServerSocket serverSocket = null;
	private final List<BaseConnector> clients = new FastList<BaseConnector>();
	private PlugInChain plugInChain = null;
	private boolean isRunning = false;

	private static final String PATHARG_SEPARATOR = ";";
	private static final String ARGARG_SEPARATOR = ",";
	private static final String KEYVAL_SEPARATOR = "=";

	/**
	 *
	 * @param aPort
	 * @param aSessionTimeout
	 * @param aListeners
	 */
	public BaseServer(int aPort, int aSessionTimeout, PlugInChain aListeners) {
		try {
			instantiateServer(aPort, aSessionTimeout);
			this.plugInChain = aListeners;
		} catch (IOException ex) {
			// ignore exception on this level
		}
	}

	/**
	 *
	 * @param aPort
	 * @param aSessionTimeout
	 * @throws IOException
	 */
	protected void instantiateServer(int aPort, int aSessionTimeout) throws IOException {
		serverSocket = new ServerSocket(aPort);
		sessionTimeout = aSessionTimeout;
	}

	/**
	 * This Method needs to be overwritten your descendant classes if you
	 * want to maintain you own client objects.
	 * @param aClientSocket
	 * @param aHeader
	 * @return
	 */
	public BaseConnector createConnector(Socket aClientSocket, Header aHeader) {
		return new BaseConnector(this, aClientSocket, aHeader);
	}

	/**
	 *
	 */
	public void serverStarted() {
		// method may be overwriten on demand
	}

	/**
	 *
	 */
	public void serverStopped() {
		// method may be overwriten on demand
	}

	/**
	 *
	 *
	 * @param aClient
	 */
	public void connectorStarted(BaseConnector aClient) {
		// method may be overwriten on demand

		// notify all listeners that a new client was connected
		if (plugInChain != null) {
			plugInChain.connectorStarted(aClient);
		}
	}

	/**
	 *
	 * @param aClient
	 */
	public void connectorTerminated(BaseConnector aClient) {
		// if connector terminates...
		// notify all listeners that a client has disconnected
		if (plugInChain != null) {
			plugInChain.connectorTerminated(aClient);
		}
		// remove it from list of connectors for this server
		remove(aClient);
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
		pos = path.indexOf(PATHARG_SEPARATOR);
		if (pos >= 0) {
			searchString = path.substring(pos + 1);
			if (searchString.length() > 0) {
				String[] lArgs = searchString.split(ARGARG_SEPARATOR);
				for (int i = 0; i < lArgs.length; i++) {
					String[] lKeyValuePair = lArgs[i].split(KEYVAL_SEPARATOR, 2);
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

	@Override
	public void run() {

		// allow descendant classes to handle server start event
		serverStarted();

		isRunning = true;
		while (isRunning) {
			try {
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
					BaseConnector client = createConnector(clientSocket, header);

					clients.add(client);
					client.start();

					// allow descendant classes to handle connector started event
					connectorStarted(client);

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
		serverStopped();
	}

	/**
	 *
	 *
	 * @param aObject
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	public void broadcast(Object aObject)
			throws UnsupportedEncodingException, IOException {
		for (BaseConnector client : clients) {
			client.send(aObject);
		}
	}

	/**
	 *
	 *
	 * @param aConnector
	 */
	public void remove(BaseConnector aConnector) {
		clients.remove(aConnector);
	}

	/**
	 * terminates this server. First all clients are terminated and then the
	 * socket server is closed.
	 */
	public void terminate() {
		isRunning = false;
		try {
			// terminate all client threads
			for (BaseConnector client : clients) {
				client.terminate();
			}
			// when done, close server socket
			serverSocket.close();
			sleep(500);
		} catch (Exception ex) {
			// log.info("Exception on sleep " + ex.getMessage());
		}
	}

	/**
	 * @return the clients
	 */
	public List<BaseConnector> getClients() {
		return clients;
	}

	/**
	 * @return the plugin-chain
	 */
	public PlugInChain getPlugInChain() {
		return plugInChain;
	}

	/**
	 *
	 * @param aRemotePort
	 * @return
	 */
	public BaseConnector getConnectorByRemotePort(int aRemotePort) {
		Iterator<BaseConnector> lIterator = clients.iterator();
		while (lIterator.hasNext()) {
			BaseConnector lConnector = lIterator.next();
			if (lConnector.getClientSocket().getPort() == aRemotePort) {
				return lConnector;
			}
		}
		return null;
	}
}
