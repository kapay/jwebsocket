//	---------------------------------------------------------------------------
//	jWebSocket - FlashBridge Plug-In
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
package org.jwebsocket.plugins.flashbridge;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;

/**
 * This plug-in processes the policy-file-request from the browser side
 * flash plug-in. This makes jWebSocket cross-browser-compatible.
 * @author aschulze
 */
public class FlashBridgePlugIn extends TokenPlugIn {

	private static Logger log = Logging.getLogger(FlashBridgePlugIn.class);
	private ServerSocket serverSocket = null;
	private int listenerPort = 843;
	private boolean isRunning = false;
	private int engineInstanceCount = 0;
	private Thread bridgeThread = null;
	private int TIMEOUT = 2500;

	/**
	 *
	 */
	public FlashBridgePlugIn() {
		if (log.isDebugEnabled()) {
			log.debug("Starting FlashBridge...");
		}
		try {
			serverSocket = new ServerSocket(listenerPort);

			BridgeListener listener = new BridgeListener(this);
			bridgeThread = new Thread(listener);
			bridgeThread.start();
		} catch (IOException ex) {
		}
		if (log.isInfoEnabled()) {
			log.info("FlashBridge started.");
		}
	}

	private class BridgeListener implements Runnable {
		private final FlashBridgePlugIn aPlugIn;

		/**
		 * creates the server socket listener for new
		 * incoming socket connections.
		 * @param aPlugIn
		 */
		public BridgeListener(FlashBridgePlugIn aPlugIn) {
			this.aPlugIn = aPlugIn;
			// plugin = aPlugIn;
		}

		@Override
		public void run() {

			isRunning = true;
			while (isRunning) {
				try {
					// accept is blocking so here is no need
					// to put any sleeps into the loop
					//if (log.isDebugEnabled()) {
					//	log.debug("Waiting on flash policy-file-request...");
					//}
					Socket clientSocket = serverSocket.accept();
					//if (log.isDebugEnabled()) {
					//	log.debug("Client connected...");
					//}
					try {
						clientSocket.setSoTimeout(TIMEOUT);
						InputStreamReader isr = new InputStreamReader(clientSocket.getInputStream(), "UTF-8");
						PrintStream os = new PrintStream(clientSocket.getOutputStream(), true, "UTF-8");

						char[] ca = new char[1024];
						String lLine = "";
						int lLen;
						// TODO: implement timeout if anyone plays on port 843!
						do {
							lLen = isr.read(ca);
							if (lLen > 0) {
								lLine += new String(ca, 0, lLen);
							} else {
								Thread.sleep(10);
							}
						} while (lLen >= 0 && lLine.indexOf("<policy-file-request/>") < 0);
						log.debug("Answering on flash policy-file-request (" + lLine + ")...");
						os.print(
							"<cross-domain-policy>"
							+ "<allow-access-from domain=\"*\" to-ports=\"*\" />"
							+ "</cross-domain-policy>\n");
					} catch (UnsupportedEncodingException ex) {
						log.error("(encoding) " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
					} catch (IOException ex) {
						log.error("(io) " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
					} catch (Exception ex) {
						log.error("(other) " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
					}

					clientSocket.close();
					if (log.isDebugEnabled()) {
						log.debug("Client disconnected...");
					}
				} catch (Exception ex) {
					isRunning = false;
					log.error("(accept) " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
				}
			}
		}
	}

	@Override
	public void engineStarted(WebSocketEngine aEngine) {
		if (log.isDebugEnabled()) {
			log.debug("Engine (" + aEngine.getId() + ") started.");
		}
		// every time an engine starts increment counter
		engineInstanceCount++;
	}

	@Override
	public void engineStopped(WebSocketEngine aEngine) {
		if (log.isDebugEnabled()) {
			log.debug("Engine (" + aEngine.getId() + ") stopped.");
		}
		// every time an engine starts decrement counter
		engineInstanceCount--;
		// when last engine stopped also stop the FlashBridge
		if (engineInstanceCount <= 0) {
			super.engineStopped(aEngine);

			isRunning = false;
			long lStarted = new Date().getTime();

			try {
				// when done, close server socket
				// closing the server socket should lead to an exception
				// at accept in the listener thread which terminates the listener
				if (log.isDebugEnabled()) {
					log.debug("Closing FlashBridge server socket...");
				}
				serverSocket.close();
				if (log.isDebugEnabled()) {
					log.debug("Closed FlashBridge server socket.");
				}
			} catch (Exception ex) {
				log.error("(accept) " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
			}

			try {
				bridgeThread.join(10000);
			} catch (Exception ex) {
				log.error(ex.getClass().getSimpleName() + ": " + ex.getMessage());
			}
			if (log.isDebugEnabled()) {
				long lDuration = new Date().getTime() - lStarted;
				if (bridgeThread.isAlive()) {
					log.warn("FlashBridge did not stopped after " + lDuration + "ms.");
				} else {
					log.debug("FlashBridge stopped after " + lDuration + "ms.");
				}
			}


		}
	}
}
