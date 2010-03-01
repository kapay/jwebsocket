/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jWebSocket.connectors;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import org.jWebSocket.api.IDataPacket;
import org.jWebSocket.api.IWebSocketConnector;
import org.jWebSocket.api.IWebSocketEngine;
import org.jWebSocket.kit.DataPacket;
import org.jWebSocket.kit.Header;

/**
 *
 * @author aschulze
 */
public class TCPConnector extends BaseConnector {

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
		ClientProcessor clientProc = new ClientProcessor(this);
		Thread clientThread = new Thread(clientProc);
		clientThread.start();
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

				// allow descending classes to handle handshakeSent event
				// handshakeSent();

				// start client listener loop
				isRunning = true;
				while (isRunning) {
					// try to read line within timeout
					try {
						line = br.readLine();
						if (line == null) {
							// System.out.println("line is null");
							// stream has been closed
							engine.connectorStopped(connector); // due to timeout
						} else {
							// cut off potential starting 0x00 and 0xff characters
							byte[] ba = line.getBytes();
							/*
							// System.out.println("Got chars: " + line.charAt(0) + ", " + line.charAt(1) + ", " + line.charAt(2));
							if( ba.length >= 3 ) {
							System.out.println("Got 3+ bytes: " + ba[0] + ", " + ba[1] + ", " + ba[2] );
							} else if( ba.length >= 2 ) {
							System.out.println("Got 2+ bytes: " + ba[0] + ", " + ba[1] );
							} else if( ba.length >= 1 ) {
							System.out.println("Got 1+ bytes: " + ba[0]);
							}
							 */
							// if no content or only one byte different from 0...
							if (ba.length <= 0 | (ba.length == 1 && ba[0] != 0)) {
								line = null;
								// if byte 0 at pos. 0 is found may be an empty string was sent
							} else if (ba.length >= 1 && ba[0] == 0 && line.length() >= 1) {
								line = line.substring(1);
								// if byte 0 at pos. 1 the rest must be the user data
							} else if (ba.length >= 2 && ba[1] == 0 && line.length() >= 2) {
								line = line.substring(2);
							}
						}
					} catch (SocketTimeoutException ex) {
						// engine.connectorStopped(connector); // due to timeout
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
				System.out.println("BaseConnector: " + ex.getClass().getName() + ": " + ex.getMessage());
			}
		}
	}
}
