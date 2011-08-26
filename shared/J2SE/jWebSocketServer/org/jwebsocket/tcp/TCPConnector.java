//	---------------------------------------------------------------------------
//	jWebSocket - TCP Connector
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
package org.jwebsocket.tcp;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import javax.net.ssl.SSLSocket;

import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.async.IOFuture;
import org.jwebsocket.connectors.BaseConnector;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.RawPacket;
import org.jwebsocket.kit.WebSocketFrameType;
import org.jwebsocket.kit.WebSocketProtocolAbstraction;
import org.jwebsocket.logging.Logging;

/**
 * Implementation of the jWebSocket TCP socket connector.
 *
 * @author aschulze
 * @author jang
 */
public class TCPConnector extends BaseConnector {

	private static Logger mLog = Logging.getLogger(TCPConnector.class);
	private InputStream mIn = null;
	private OutputStream mOut = null;
	private Socket mClientSocket = null;
	public static final String TCP_LOG = "TCP";
	public static final String SSL_LOG = "SSL";
	private String mLogInfo = TCP_LOG;
	private boolean mIsRunning = false;
	private CloseReason mCloseReason = CloseReason.TIMEOUT;

	/**
	 * creates a new TCP connector for the passed engine using the passed client
	 * socket. Usually connectors are instantiated by their engine only, not by
	 * the application.
	 *
	 * @param aEngine
	 * @param aClientSocket
	 */
	public TCPConnector(WebSocketEngine aEngine, Socket aClientSocket) {
		super(aEngine);
		mClientSocket = aClientSocket;
		setSSL(mClientSocket instanceof SSLSocket);
		mLogInfo = isSSL() ? SSL_LOG : TCP_LOG;
		try {
			mIn = mClientSocket.getInputStream();
			// mOut = new PrintStream(mClientSocket.getOutputStream(), true, "UTF-8");
			mOut = mClientSocket.getOutputStream();
		} catch (Exception lEx) {
			mLog.error(lEx.getClass().getSimpleName()
					+ " instantiating "
					+ getClass().getSimpleName() + ": "
					+ lEx.getMessage());
		}
	}

	@Override
	public void startConnector() {
		int lPort = -1;
		int lTimeout = -1;
		try {
			lPort = mClientSocket.getPort();
			lTimeout = mClientSocket.getSoTimeout();
		} catch (Exception lEx) {
		}
		String lNodeStr = getNodeId();
		if (lNodeStr != null) {
			lNodeStr = " (unid: " + lNodeStr + ")";
		} else {
			lNodeStr = "";
		}
		if (mLog.isDebugEnabled()) {
			mLog.debug("Starting " + mLogInfo + " connector" + lNodeStr + " on port "
					+ lPort + " with timeout "
					+ (lTimeout > 0 ? lTimeout + "ms" : "infinite") + "");
		}
		ClientProcessor lClientProc = new ClientProcessor(this);
		Thread lClientThread = new Thread(lClientProc);
		lClientThread.start();
		if (mLog.isInfoEnabled()) {
			mLog.info("Started " + mLogInfo + " connector" + lNodeStr + " on port "
					+ lPort + " with timeout "
					+ (lTimeout > 0 ? lTimeout + "ms" : "infinite") + "");
		}
	}

	@Override
	public void stopConnector(CloseReason aCloseReason) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Stopping " + mLogInfo
					+ " connector (" + aCloseReason.name() + ")...");
		}
		int lPort = mClientSocket.getPort();
		mCloseReason = aCloseReason;
		mIsRunning = false;

		if (!isHixie()) {
			// Hybi specs demand that client must be notified with CLOSE control message before disconnect
			WebSocketPacket lClose = new RawPacket("BYE");
			lClose.setFrameType(WebSocketFrameType.CLOSE);
			sendPacket(lClose);
		}

		try {
			mIn.close();
			if (mLog.isInfoEnabled()) {
				mLog.info("Stopped " + mLogInfo
						+ " connector (" + aCloseReason.name()
						+ ") on port " + lPort + ".");
			}
		} catch (IOException lEx) {
			if (mLog.isDebugEnabled()) {
				mLog.info(lEx.getClass().getSimpleName()
						+ " while stopping " + mLogInfo
						+ " connector (" + aCloseReason.name()
						+ ") on port " + lPort + ": " + lEx.getMessage());
			}
		}
	}

	@Override
	public void processPacket(WebSocketPacket aDataPacket) {
		// forward the data packet to the engine
		getEngine().processPacket(this, aDataPacket);
	}

	@Override
	public synchronized void sendPacket(WebSocketPacket aDataPacket) {
		try {
			if (isHixie()) {
				sendHixie(aDataPacket);
			} else {
				sendHybi(getVersion(), aDataPacket);
			}
			mOut.flush();
		} catch (IOException lEx) {
			mLog.error(lEx.getClass().getSimpleName()
					+ " sending data packet: " + lEx.getMessage());
		}
	}

	@Override
	public IOFuture sendPacketAsync(WebSocketPacket aDataPacket) {
		throw new UnsupportedOperationException("Underlying connector:"
				+ getClass().getName()
				+ " doesn't support asynchronous send operation");
	}

	private class ClientProcessor implements Runnable {

		private WebSocketConnector mConnector = null;

		/**
		 * Creates the new socket listener thread for this connector.
		 *
		 * @param aConnector
		 */
		public ClientProcessor(WebSocketConnector aConnector) {
			mConnector = aConnector;
		}

		@Override
		public void run() {
			WebSocketEngine lEngine = getEngine();
			ByteArrayOutputStream lBuff = new ByteArrayOutputStream();
			Thread.currentThread().setName("jWebSocket TCP-Connector " + getId());
			try {
				// start client listener loop
				mIsRunning = true;

				// call connectorStarted method of engine
				lEngine.connectorStarted(mConnector);

				if (isHixie()) {
					readHixie(lBuff, lEngine);
				} else {
					readHybi(getVersion(), lBuff, lEngine);
				}

				// call client stopped method of engine
				// (e.g. to release client from streams)
				lEngine.connectorStopped(mConnector, mCloseReason);

				// br.close();
				mIn.close();
				mOut.close();
				mClientSocket.close();

			} catch (Exception lEx) {
				// ignore this exception for now
				mLog.error("(close) " + lEx.getClass().getSimpleName()
						+ ": " + lEx.getMessage());
			}
		}

		private void readHixie(ByteArrayOutputStream aBuff,
				WebSocketEngine aEngine) throws IOException {
			while (mIsRunning) {
				try {
					int lIn = mIn.read();
					// start of frame
					if (lIn == 0x00) {
						aBuff.reset();
						// end of frame
					} else if (lIn == 0xFF) {
						RawPacket lPacket = new RawPacket(aBuff.toByteArray());
						try {
							aEngine.processPacket(mConnector, lPacket);
						} catch (Exception lEx) {
							mLog.error(lEx.getClass().getSimpleName()
									+ " in processPacket of connector "
									+ mConnector.getClass().getSimpleName()
									+ ": " + lEx.getMessage());
						}
						aBuff.reset();
					} else if (lIn < 0) {
						mCloseReason = CloseReason.CLIENT;
						mIsRunning = false;
						// any other byte within or outside a frame
					} else {
						aBuff.write(lIn);
					}
				} catch (SocketTimeoutException lEx) {
					mLog.error("(timeout) " + lEx.getClass().getSimpleName()
							+ ": " + lEx.getMessage());
					mCloseReason = CloseReason.TIMEOUT;
					mIsRunning = false;
				} catch (Exception lEx) {
					mLog.error("(other) " + lEx.getClass().getSimpleName()
							+ ": " + lEx.getMessage());
					mCloseReason = CloseReason.SERVER;
					mIsRunning = false;
				}
			}
		}

		private void readHybi(int aVersion, ByteArrayOutputStream aBuff,
				WebSocketEngine aEngine) throws IOException {
			WebSocketFrameType lFrameType;
			// utilize data input stream, because it has convenient methods for reading
			// signed/unsigned bytes, shorts, ints and longs
			DataInputStream lDis = new DataInputStream(mIn);

			while (mIsRunning) {
				try {
					WebSocketPacket lPacket = WebSocketProtocolAbstraction.protocolToRawPacket(getVersion(), mIn);

					if (WebSocketFrameType.TEXT.equals(lPacket.getFrameType())) {
						if (mLog.isDebugEnabled()) {
							mLog.debug("Processing 'text' frame...");
						}
						aEngine.processPacket(mConnector, lPacket);
					} else if (WebSocketFrameType.PING.equals(lPacket.getFrameType())) {
						if (mLog.isDebugEnabled()) {
							mLog.debug("Processing 'ping' frame...");
						}
						WebSocketPacket lPong = new RawPacket("");
						lPong.setFrameType(WebSocketFrameType.PONG);
						sendPacket(lPong);
					} else if (WebSocketFrameType.CLOSE.equals(lPacket.getFrameType())) {
						if (mLog.isDebugEnabled()) {
							mLog.debug("Processing 'close' frame...");
						}
						mCloseReason = CloseReason.CLIENT;
						mIsRunning = false;
						// As per spec, server must respond to CLOSE with acknowledgment CLOSE (maybe
						// this should be handled higher up in the hierarchy?)
						WebSocketPacket lClose = new RawPacket("");
						lClose.setFrameType(WebSocketFrameType.CLOSE);
						sendPacket(lClose);
					} else {
						if (mLog.isDebugEnabled()) {
							mLog.debug("Processing unknown frame type '" + lPacket.getFrameType() + "'...");
						}
					}
				} catch (SocketTimeoutException lEx) {
					mLog.error("(timeout) " + lEx.getClass().getSimpleName() + ": " + lEx.getMessage());
					mCloseReason = CloseReason.TIMEOUT;
					mIsRunning = false;
				} catch (Exception lEx) {
					mLog.error("(other) " + lEx.getClass().getSimpleName() + ": " + lEx.getMessage());
					mCloseReason = CloseReason.SERVER;
					mIsRunning = false;
				}
			}
		}
	}

	@Override
	public String generateUID() {
		String lUID = mClientSocket.getInetAddress().getHostAddress()
				+ "@" + mClientSocket.getPort();
		return lUID;
	}

	@Override
	public int getRemotePort() {
		return mClientSocket.getPort();
	}

	@Override
	public InetAddress getRemoteHost() {
		return mClientSocket.getInetAddress();
	}

	@Override
	public String toString() {
		// TODO: Show proper IPV6 if used
		String lRes = getId() + " (" + getRemoteHost().getHostAddress()
				+ ":" + getRemotePort();
		String lUsername = getUsername();
		if (lUsername != null) {
			lRes += ", " + lUsername;
		}
		return lRes + ")";
	}

	private void sendHixie(WebSocketPacket aDataPacket) throws IOException {
		if (aDataPacket.getFrameType() == WebSocketFrameType.BINARY) {
			// each packet is enclosed in 0xFF<length><data>
			// TODO: for future use! Not yet finally spec'd in IETF drafts!
			mOut.write(0xFF);
			byte[] lBA = aDataPacket.getByteArray();
			// TODO: implement multi byte length!
			mOut.write(lBA.length);
			mOut.write(lBA);
		} else {
			// each packet is enclosed in 0x00<data>0xFF
			mOut.write(0x00);
			mOut.write(aDataPacket.getByteArray());
			mOut.write(0xFF);
		}
	}

	// TODO: implement fragmentation for packet sending
	private void sendHybi(int aVersion, WebSocketPacket aDataPacket) throws IOException {
		byte[] lPacket = WebSocketProtocolAbstraction.rawToProtocolPacket(aVersion, aDataPacket);
		mOut.write(lPacket);
	}
}
