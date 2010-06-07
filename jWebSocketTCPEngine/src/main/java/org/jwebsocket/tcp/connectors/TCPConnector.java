/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.tcp.connectors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketPaket;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.connectors.BaseConnector;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.RawPacket;
import org.jwebsocket.logging.Logging;

/**
 * Implementation of the jWebSocket TCP socket connector.
 * @author aschulze
 */
public class TCPConnector extends BaseConnector {

	private static Logger log = Logging.getLogger(TCPConnector.class);
	private BufferedReader br = null;
	private PrintStream os = null;
	private Socket clientSocket = null;
	private boolean isRunning = false;
	private CloseReason closeReason = CloseReason.TIMEOUT;

	/**
	 * creates a new TCP connector for the passed engine using the passed 
	 * client socket. Usually connectors are instantiated by their engine
	 * only, not by the application.
	 * @param aEngine
	 * @param aClientSocket
	 */
	public TCPConnector(WebSocketEngine aEngine, Socket aClientSocket) {
		super(aEngine);
		clientSocket = aClientSocket;
		try {
			br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
			os = new PrintStream(clientSocket.getOutputStream(), true, "UTF-8");
		} catch (Exception ex) {
			log.error(ex.getClass().getSimpleName()
					+ " instantiating " + getClass().getSimpleName()
					+ ": " + ex.getMessage());
		}
	}

	@Override
	public void startConnector() {
		int lPort = -1;
		int lTimeout = -1;
		try {
			lPort = clientSocket.getPort();
			lTimeout = clientSocket.getSoTimeout();
		} catch (Exception ex) {
		}
		if (log.isDebugEnabled()) {
			log.debug("Starting TCP connector on port "
					+ lPort + " with timeout "
					+ (lTimeout > 0 ? lTimeout + "ms" : "infinite")
					+ "");
		}
		ClientProcessor clientProc = new ClientProcessor(this);
		Thread clientThread = new Thread(clientProc);
		clientThread.start();
		if (log.isInfoEnabled()) {
			log.info("Started TCP connector on port "
					+ lPort + " with timeout "
					+ (lTimeout > 0 ? lTimeout + "ms" : "infinite")
					+ "");
		}
	}

	@Override
	public void stopConnector(CloseReason aCloseReason) {
		if (log.isDebugEnabled()) {
			log.debug("Stopping TCP connector (" + aCloseReason.name() + ")...");
		}
		int lPort = clientSocket.getPort();
		closeReason = aCloseReason;
		isRunning = false;
		// TODO: Do we need to wait here? At least optionally?
		if (log.isInfoEnabled()) {
			log.info("Stopped TCP connector ("
					+ aCloseReason.name() + ") on port " + lPort + ".");
		}
	}

	@Override
	public void processPacket(WebSocketPaket aDataPacket) {
		// forward the data packet to the engine
		getEngine().processPacket(this, aDataPacket);
	}

	@Override
	public void sendPacket(WebSocketPaket aDataPacket) {
		try {
			if (aDataPacket.getFrameType() == RawPacket.FRAMETYPE_BINARY) {
				// each packet is enclosed in 0xFF<length><data>
				// TODO: for future use! Not yet released!
				os.write(0xFF);
				byte[] lBA = aDataPacket.getByteArray();
				// TODO: implement multi byte length!
				os.write(lBA.length);
				os.write(lBA);
			} else {
				// each packet is enclosed in 0x00<data>0xFF
				os.write(0x00);
				os.write(aDataPacket.getByteArray());
				os.write(0xFF);
			}
			os.flush();
		} catch (IOException ex) {
			log.error(ex.getClass().getSimpleName()
					+ " sending data packet: " + ex.getMessage());
		}
	}

	private class ClientProcessor implements Runnable {

		private WebSocketConnector connector = null;

		/**
		 * Creates the new socket listener thread for this connector.
		 * @param aConnector
		 */
		public ClientProcessor(WebSocketConnector aConnector) {
			connector = aConnector;
		}

		@Override
		public void run() {
			String line;
			WebSocketEngine engine = getEngine();

			try {
				// start client listener loop
				isRunning = true;

				// call connectorStarted method of engine
				engine.connectorStarted(connector);

				while (isRunning) {
					// try to read line within timeout
					try {
						// TODO: optimize protocol packet handling!
						line = br.readLine();
						// if line is null the end of the stream is reached
						// this means the connection has been closed
						// by the client
						if (line == null) {
							// stream has been closed (by client)
							closeReason = CloseReason.CLIENT;
						} else {
							// cut off potential starting 0x00 and 0xff characters
							byte[] ba = line.getBytes();

							int i = 0;
							while (i < ba.length && ba[i] != 0) {
								i++;
							}
							if (i < ba.length) {
								i++;
								line = new String(ba, i, ba.length - i, "UTF-8");
							} else {
								line = null;
								// no data means stream has been closed (by client)
								closeReason = CloseReason.CLIENT;
							}
						}

					} catch (SocketTimeoutException ex) {
						log.error("(timeout) "
								+ ex.getClass().getSimpleName()
								+ ": " + ex.getMessage());
						closeReason = CloseReason.TIMEOUT;
						line = null;
					} catch (Exception ex) {
						log.error("(other) "
								+ ex.getClass().getSimpleName()
								+ ": " + ex.getMessage());
						closeReason = CloseReason.SERVER;
						line = null;
					}

					if (line != null) {
						RawPacket dataPacket = new RawPacket(line);
						// ensure that potential exceptions in a plug in
						// do not abort the connector
						try {
							engine.processPacket(connector, dataPacket);
						} catch (Exception ex) {
							log.error(ex.getClass().getSimpleName()
									+ " in processPacket of connector "
									+ connector.getClass().getSimpleName()
									+ ": " + ex.getMessage());
							line = null;
						}
					} else {
						isRunning = false;
					}
				}

				// call client stopped method of engine
				// (e.g. to release client from streams)
				engine.connectorStopped(connector, closeReason);

				br.close();
				os.close();
				clientSocket.close();

			} catch (Exception ex) {
				// ignore this exception for now
				log.error("(close) "
						+ ex.getClass().getSimpleName()
						+ ": " + ex.getMessage());
			}
		}
	}

	@Override
	public String generateUID() {
		String lUID = clientSocket.getInetAddress().getHostAddress()
				+ "@" + clientSocket.getPort();
		return lUID;
	}

	@Override
	public int getRemotePort() {
		return clientSocket.getPort();
	}

	@Override
	public InetAddress getRemoteHost() {
		return clientSocket.getInetAddress();
	}

	@Override
	public String toString() {
		// TODO: weird results like... '0:0:0:0:0:0:0:1:61130'... on JDK 1.6u19 Windows 7 64bit
		String lRes = getRemoteHost().getHostAddress() + ":" + getRemotePort();
		// TODO: don't hard code. At least use JWebSocketConstants field here.
		String lUsername = getString("org.jWebSocket.plugins.system.username");
		if (lUsername != null) {
			lRes += " (" + lUsername + ")";
		}
		return lRes;
	}
}
