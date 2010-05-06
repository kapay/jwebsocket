//	---------------------------------------------------------------------------
//	jWebSocket - Java WebSocket Client - TCP Socket implementation
//	Copyright (c) 2010 jWebSocket.org, Alexander Schulze, Innotrade GmbH
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
package org.jWebSocket.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import org.jwebsocket.api.WebSocketPaket;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.RawPacket;

/**
 *
 * @author aschulze
 */
public class TCPClient implements WebSocketClient {

	private Socket socket = null;
	private String host = null;
	private Integer port = null;
	private boolean isRunning = false;
	private BufferedReader br = null;
	private PrintStream os = null;
	private CloseReason closeReason = null;

	/**
	 *
	 * @param aHost
	 * @param aPort
	 */
	public TCPClient(String aHost, int aPort) {
		host = aHost;
		port = aPort;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void startClient() {
		try {
			socket = new Socket(host, port);
			SocketAddress lSocketAddr = socket.getRemoteSocketAddress();
			socket.connect(lSocketAddr);
			// clientStarted();

			br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
			os = new PrintStream(socket.getOutputStream(), true, "UTF-8");

			ClientProcessor clientProc = new ClientProcessor(this);
			Thread clientThread = new Thread(clientProc);
			clientThread.start();

		} catch (IOException ex) {
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void stopClient() {
//		try {
			isRunning = false;
			/* wait with timeout until client has stopped */
/*
			socket.close();
			clientStopped();
 */
			br = null;
			os = null;
//		} catch (IOException ex) {
//		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clientStarted() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clientStopped() {
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param aDataPacket
	 */
	@Override
	public void sendPacket(WebSocketPaket aDataPacket) {
		try {
			socket.getOutputStream().write(aDataPacket.getByteArray());
		} catch (IOException ex) {
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param aDataPacket
	 */
	@Override
	public void processPacket(WebSocketPaket aDataPacket) {

	}

	private class ClientProcessor implements Runnable {

		private WebSocketClient client = null;

		/**
		 * Creates the new socket listener thread for this connector.
		 * @param aClient
		 */
		public ClientProcessor(WebSocketClient aClient) {
			client = aClient;
		}

		@Override
		public void run() {
			String line;
			try {
				// start client listener loop
				isRunning = true;
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
						// log.error("(timeout) " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
						closeReason = CloseReason.TIMEOUT;
						line = null;
					} catch (Exception ex) {
						// log.error("(other) " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
						closeReason = CloseReason.SERVER;
						line = null;
					}

					if (line != null) {
						RawPacket dataPacket = new RawPacket(line);
						// ensure that potential exceptions in a plug in
						// do not abort the connector
						try {
							processPacket(dataPacket);
						} catch (Exception ex) {
							// log.error(ex.getClass().getSimpleName() + " in processPacket of connector " + client.getClass().getSimpleName() + ": " + ex.getMessage());
							line = null;
						}
					} else {
						isRunning = false;
					}
				}

				br.close();
				os.close();
				socket.close();
				br = null;
				os = null;
			} catch (Exception ex) {
				// ignore this exception for now
				// log.error("(close) " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
			}
		}
	}
}
