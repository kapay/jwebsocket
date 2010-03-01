/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jWebSocket.connectors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import org.apache.log4j.Logger;
import org.jWebSocket.api.IDataPacket;
import org.jWebSocket.api.IWebSocketConnector;
import org.jWebSocket.api.IWebSocketEngine;
import org.jWebSocket.kit.DataPacket;

/**
 *
 * @author aschulze
 */
public class TCPConnector extends BaseConnector {

	private static Logger log = Logger.getLogger(TCPConnector.class);
	private BufferedReader br = null;
	private PrintStream os = null;
	private Socket clientSocket = null;
	private boolean isRunning = false;

	public TCPConnector(IWebSocketEngine aEngine, Socket aClientSocket) {
		super(aEngine);
		clientSocket = aClientSocket;
	}

	@Override
	public void startConnector() {
		log.debug("Starting TCP connector...");
		ClientProcessor clientProc = new ClientProcessor(this);
		Thread clientThread = new Thread(clientProc);
		clientThread.start();
		log.info("Started TCP engine on port " + clientSocket.getPort() + ".");
	}

	@Override
	public void stopConnector() {
		isRunning = false;
	}

	@Override
	public void processPacket(IDataPacket aDataPacket) {
		// pass the data packet to the engine
		getEngine().processPacket(this, aDataPacket);
	}

	@Override
	public void sendPacket(IDataPacket aDataPacket) {
		os.write(0);
		try {
			os.write(aDataPacket.getByteArray());
		} catch (IOException ex) {
			log.error(ex.getClass().getName() + ": " + ex.getMessage());
		}
		os.write(255);
		os.flush();
	}

	public Socket getClientSocket() {
		return null;
	}

	public class ClientProcessor implements Runnable {

		IWebSocketConnector connector = null;

		public ClientProcessor(IWebSocketConnector aConnector) {
			connector = aConnector;
		}

		@Override
		public void run() {
			String line;
			IWebSocketEngine engine = getEngine();

			try {
				br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
				os = new PrintStream(clientSocket.getOutputStream(), true, "UTF-8");

				// start client listener loop
				isRunning = true;
				while (isRunning) {
					// try to read line within timeout
					try {
						// TODO: optimize protocol packet handling!
						line = br.readLine();
						if (line == null) {
							// System.out.println("line is null");
							// stream has been closed
							engine.connectorStopped(connector); // due to timeout
						} else {
							// cut off potential starting 0x00 and 0xff characters
							byte[] ba = line.getBytes();

							int i = 0;
							while (i < ba.length && ba[i] != 0) {
								i++;
							}
							if( i < ba.length ) {
								i++;
								line = new String(ba, i, ba.length - i, "UTF-8");
							} else {
								line = null;
							}
						}
					} catch (SocketTimeoutException ex) {
						log.error(ex.getClass().getName() + ": " + ex.getMessage());
						line = null;
					}
					if (line != null) {
						DataPacket dataPacket = new DataPacket(line);
						engine.processPacket(connector, dataPacket);
					} else {
						isRunning = false;
					}
				}

				// call client stopped method of server
				// (e.g. to release client from streams)
				engine.connectorStopped(connector);

				br.close();
				os.close();
				clientSocket.close();

			} catch (Exception ex) {
				// ignore this exception for now
				log.error(ex.getClass().getName() + ": " + ex.getMessage());
			}
		}
	}

	@Override
	public String generateUID() {
		String lUID = clientSocket.getInetAddress().getHostAddress() + "@" + clientSocket.getPort();
		return lUID;
	}

}
