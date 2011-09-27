//	---------------------------------------------------------------------------
//	jWebSocket - Copyright (c) 2010 Innotrade GmbH
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
package org.jwebsocket.client.java;

import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import javolution.util.FastList;
import javolution.util.FastMap;
import org.jwebsocket.api.WebSocketBaseClientEvent;
import org.jwebsocket.api.WebSocketClient;
import org.jwebsocket.api.WebSocketClientEvent;
import org.jwebsocket.api.WebSocketClientListener;
import org.jwebsocket.api.WebSocketPacket;

import org.jwebsocket.api.WebSocketStatus;
import org.jwebsocket.client.token.WebSocketTokenClientEvent;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.kit.Headers;
import org.jwebsocket.kit.RawPacket;
import org.jwebsocket.kit.WebSocketEncoding;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.kit.WebSocketExceptionType;
import org.jwebsocket.kit.WebSocketFrameType;
import org.jwebsocket.kit.WebSocketHandshake;
import org.jwebsocket.kit.WebSocketProtocolAbstraction;
import org.jwebsocket.kit.WebSocketSubProtocol;

/**
 * Base {@code WebSocket} implementation based on
 * http://weberknecht.googlecode.com by Roderick Baier. This uses thread model
 * for handling WebSocket connection which is defined by the <tt>WebSocket</tt>
 * protocol specification. {@linkplain http://www.whatwg.org/specs/web-socket-protocol/}
 * {@linkplain http://www.w3.org/TR/websockets/}
 *
 * @author Roderick Baier
 * @author agali
 * @author puran
 * @author jang
 * @author aschulze
 * @version $Id:$
 */
public class BaseWebSocketClient implements WebSocketClient {

	/**
	 * WebSocket connection URI
	 */
	private URI mURI = null;
	/**
	 * list of the listeners registered
	 */
	private List<WebSocketClientListener> mListeners = new FastList<WebSocketClientListener>();
	/**
	 * TCP socket
	 */
	private Socket mSocket = null;
	/**
	 * IO streams
	 */
	private InputStream mIn = null;
	private OutputStream mOut = null;
	/**
	 * Data receiver
	 */
	private WebSocketReceiver mReceiver = null;
	/**
	 * represents the WebSocket status
	 */
	protected volatile WebSocketStatus mStatus = WebSocketStatus.CLOSED;
	private List<WebSocketSubProtocol> mSubprotocols;
	private WebSocketSubProtocol mNegotiatedSubProtocol;
	/**
	 * 
	 */
	public static String EVENT_OPEN = "open";
	/**
	 * 
	 */
	public static String EVENT_CLOSE = "close";
	/**
	 * 
	 */
	public static String DATA_CLOSE_ERROR = "error";
	/**
	 * 
	 */
	public static String DATA_CLOSE_CLIENT = "client";
	/**
	 * 
	 */
	public static String DATA_CLOSE_SERVER = "server";
	/**
	 * 
	 */
	public static String DATA_CLOSE_SHUTDOWN = "shutdown";
	private int mVersion = JWebSocketCommonConstants.WS_VERSION_DEFAULT;
	private WebSocketEncoding mEncoding = WebSocketEncoding.TEXT;
	private ReliabilityOptions mReliabilityOptions = null;
	private final ScheduledThreadPoolExecutor mExecutor = new ScheduledThreadPoolExecutor(1);
	private final Map<String, Object> mParams = new FastMap<String, Object>();

	/**
	 * Base constructor
	 */
	public BaseWebSocketClient() {
	}

	/**
	 * Constructor including reliability options
	 */
	public BaseWebSocketClient(ReliabilityOptions aReliabilityOptions) {
		mReliabilityOptions = aReliabilityOptions;
	}

	/**
	 * 
	 * @param aKey
	 * @param aDefault
	 * @return
	 */
	public Object getParam(String aKey, Object aDefault) {
		Object lValue = mParams.get(aKey);
		if (null == lValue) {
			lValue = aDefault;
		}
		return lValue;
	}

	/**
	 * 
	 * @param aKey
	 * @return
	 */
	public Object getParam(String aKey) {
		return mParams.get(aKey);
	}

	/**
	 * 
	 * @param aKey
	 * @param aValue
	 */
	public void setParam(String aKey, Object aValue) {
		mParams.put(aKey, aValue);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @param aURI 
	 */
	@Override
	public void open(String aURI) throws WebSocketException {
		open(JWebSocketCommonConstants.WS_VERSION_DEFAULT, aURI);
	}

	/**
	 * Make a sub protocol string for Sec-WebSocket-Protocol header.
	 * The result is something like this:
	 * <pre>
	 * org.jwebsocket.json org.websocket.text org.jwebsocket.binary
	 * </pre>
	 *
	 * @return sub protocol list in one string
	 */
	private String generateSubProtocolsHeaderValue() {
		if (mSubprotocols == null || mSubprotocols.size() <= 0) {
			return JWebSocketCommonConstants.WS_SUBPROT_DEFAULT;
		} else {
			StringBuilder lBuff = new StringBuilder();
			for (WebSocketSubProtocol lProt : mSubprotocols) {
				lBuff.append(lProt.getSubProt()).append(' ');
			}
			return lBuff.toString().trim();
		}
	}

	/**
	 * 
	 * @param aVersion
	 * @param aURI
	 * @throws WebSocketException
	 */
	public void open(int aVersion, String aURI) {
		String lSubProtocols = generateSubProtocolsHeaderValue();
		open(aVersion, aURI, lSubProtocols);
	}

	/**
	 * 
	 * @param aVersion
	 * @param aURI
	 * @param aSubProtocols 
	 * @throws WebSocketException
	 */
	public void open(int aVersion, String aURI, String aSubProtocols) {
		try {
			mVersion = aVersion;
			mURI = new URI(aURI);
			// the WebSocket Handshake here generates the initial client side Handshake only
			WebSocketHandshake lHandshake = new WebSocketHandshake(mVersion, mURI, aSubProtocols);
			// close current socket if still connected 
			// to avoid open connections on server
			if (mSocket != null && mSocket.isConnected()) {
				mSocket.close();
			}
			mSocket = createSocket();
			mIn = mSocket.getInputStream();
			mOut = mSocket.getOutputStream();

			mOut.write(lHandshake.generateC2SRequest());

			mStatus = WebSocketStatus.CONNECTING;

			Headers lHeaders = new Headers();
			lHeaders.readFromStream(aVersion, mIn);

			// parse negotiated sub protocol
			String lProtocol = lHeaders.getField(Headers.SEC_WEBSOCKET_PROTOCOL);
			if (lProtocol != null) {
				mNegotiatedSubProtocol = new WebSocketSubProtocol(lProtocol, mEncoding);
			} else {
				// just default to 'jwebsocket.org/json' and 'text'
				mNegotiatedSubProtocol = new WebSocketSubProtocol(
						JWebSocketCommonConstants.WS_SUBPROT_DEFAULT,
						JWebSocketCommonConstants.WS_ENCODING_DEFAULT);
			}

			// create new thread to receive the data from the new client
			mReceiver = new WebSocketReceiver(this, mIn);
			// and start the receiver thread for the port
			mReceiver.start();
			// now set official status, may listeners ask for that
			mStatus = WebSocketStatus.OPEN;
			// and finally notify listeners for OnOpen event
			WebSocketClientEvent lEvent =
					new WebSocketBaseClientEvent(this, EVENT_OPEN, "");
			notifyOpened(lEvent);

		} catch (Exception lEx) {
			WebSocketClientEvent lEvent =
					new WebSocketBaseClientEvent(this, EVENT_CLOSE,
					lEx.getClass().getSimpleName() + ": "
					+ lEx.getMessage());
			notifyClosed(lEvent);
			mCheckReconnect(lEvent);
		}
	}

	@Override
	public void send(byte[] aData) throws WebSocketException {
		if (isHixie()) {
			sendInternal(aData);
		} else {
			WebSocketPacket lPacket = new RawPacket(aData);
			lPacket.setFrameType(
					WebSocketProtocolAbstraction.encodingToFrameType(
					mNegotiatedSubProtocol.getEncoding()));
			sendInternal(
					WebSocketProtocolAbstraction.rawToProtocolPacket(
					mVersion, lPacket));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void send(String aData, String aEncoding) throws WebSocketException {
		byte[] lData;
		try {
			lData = aData.getBytes(aEncoding);
		} catch (UnsupportedEncodingException lEx) {
			throw new WebSocketException(
					"Encoding exception while sending the data:"
					+ lEx.getMessage(), lEx);
		}
		send(lData);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @param aDataPacket 
	 */
	@Override
	public void send(WebSocketPacket aDataPacket) throws WebSocketException {
		if (isHixie()) {
			sendInternal(aDataPacket.getByteArray());
		} else {
			sendInternal(WebSocketProtocolAbstraction.rawToProtocolPacket(mVersion, aDataPacket));
		}
	}

	private void sendInternal(byte[] aData) throws WebSocketException {
		if (!mStatus.isWriteble()) {
			throw new WebSocketException("Error while sending binary data: not connected");
		}
		try {
			if (isHixie()) {
				if (WebSocketEncoding.BINARY.equals(mNegotiatedSubProtocol.getEncoding())) {
					mOut.write(0x80);
					// TODO: what if frame is longer than 255 characters (8bit?) Refer to IETF spec!
					mOut.write(aData.length);
					mOut.write(aData);
				} else {
					mOut.write(0x00);
					mOut.write(aData);
					mOut.write(0xff);
				}
			} else {
				mOut.write(aData);
			}
			mOut.flush();
		} catch (IOException lEx) {
			throw new WebSocketException("Error while sending socket data: ", lEx);
		}
	}

	/**
	 * 
	 */
	public void handleReceiverError() {
		try {
			if (mStatus.isClosable()) {
				close();
			}
		} catch (WebSocketException lWSE) {
			// TODO: don't use printStackTrace
			// wse.printStackTrace();
		}
	}

	@Override
	public synchronized void close() throws WebSocketException {
		if (!mStatus.isWriteble()) {
			return;
		}
		String lExMsg = "Error(s) on close:";
		boolean lThrowEx = false;
		try {
			sendCloseHandshake();
		} catch (Exception lEx) {
			lExMsg += " " + lEx.getMessage();
			lThrowEx = true;
		}
		// set status AFTER close frame was sent, otherwise sending
		// close frame leads to an exception.
		mStatus = WebSocketStatus.CLOSING;
		if (mReceiver.isRunning()) {
			mReceiver.stopit();
		}
		try {
			// shutdown methods are not implemented for SSL sockets
			if (!(mSocket instanceof SSLSocket)) {
				mSocket.shutdownInput();
				mSocket.shutdownOutput();
			}
		} catch (IOException lIOEx) {
			lExMsg += " " + lIOEx.getMessage();
			lThrowEx = true;
		}
		try {
			mSocket.close();
		} catch (IOException lIOEx) {
			lExMsg += " " + lIOEx.getMessage();
			lThrowEx = true;
		}
		mStatus = WebSocketStatus.CLOSED;
		if (lThrowEx) {
			throw new WebSocketException(lExMsg);
		}

		WebSocketClientEvent lEvent =
				new WebSocketBaseClientEvent(this, EVENT_CLOSE, "client");
		notifyClosed(lEvent);
	}

	private void sendCloseHandshake() throws WebSocketException {
		if (!mStatus.isClosable()) {
			throw new WebSocketException("Error while sending close handshake: not connected");
		}
		try {
			if (isHixie()) {
				mOut.write(0xff00);
				// TODO: check if final CR/LF is required/valid!
				mOut.write("\r\n".getBytes());
				// TODO: shouldn't we put a flush here?
			} else {
				WebSocketPacket lPacket = new RawPacket(WebSocketFrameType.CLOSE, "BYE");
				send(lPacket);
			}
		} catch (IOException lIOEx) {
			throw new WebSocketException("Error while sending close handshake", lIOEx);
		}
		mStatus = WebSocketStatus.CLOSED;
	}

	private Socket createSocket() throws WebSocketException {
		String lScheme = mURI.getScheme();
		String lHost = mURI.getHost();
		int lPort = mURI.getPort();

		mSocket = null;

		if (lScheme != null && lScheme.equals("ws")) {
			if (lPort == -1) {
				lPort = 80;
			}
			try {
				mSocket = new Socket(lHost, lPort);
			} catch (UnknownHostException lUHEx) {
				throw new WebSocketException("Unknown host: " + lHost,
						WebSocketExceptionType.UNKNOWN_HOST, lUHEx);
			} catch (IOException lIOEx) {
				throw new WebSocketException("Error while creating socket to " + mURI,
						WebSocketExceptionType.UNABLE_TO_CONNECT, lIOEx);
			}
		} else if (lScheme != null && lScheme.equals("wss")) {
			if (lPort == -1) {
				lPort = 443;
			}
			try {
				try {
					// TODO: Make acceptance of unsigned certificates optional!
					// This methodology is used to accept unsigned certficates
					// on the SSL server. Be careful with this in production environments!
					// Create a trust manager to accept unsigned certificates
					TrustManager[] lTrustManager = new TrustManager[]{
						new X509TrustManager() {

							@Override
							public java.security.cert.X509Certificate[] getAcceptedIssuers() {
								return null;
							}

							@Override
							public void checkClientTrusted(
									java.security.cert.X509Certificate[] aCerts, String aAuthType) {
							}

							@Override
							public void checkServerTrusted(
									java.security.cert.X509Certificate[] aCerts, String aAuthType) {
							}
						}
					};
					// Use this trustmanager to not reject unsigned certificates
					SSLContext lSSLContext = SSLContext.getInstance("TLS");
					lSSLContext.init(null, lTrustManager, new java.security.SecureRandom());
					mSocket = (SSLSocket) lSSLContext.getSocketFactory().createSocket(lHost, lPort);
				} catch (NoSuchAlgorithmException lNSAEx) {
					throw new RuntimeException("Unable to initialise SSL context", lNSAEx);
				} catch (KeyManagementException lKMEx) {
					throw new RuntimeException("Unable to initialise SSL context", lKMEx);
				}
			} catch (UnknownHostException lUHEx) {
				throw new WebSocketException("Unknown host: " + lHost,
						WebSocketExceptionType.UNKNOWN_HOST, lUHEx);
			} catch (IOException lIOEx) {
				throw new WebSocketException("Error while creating secure socket to " + mURI,
						WebSocketExceptionType.UNABLE_TO_CONNECT_SSL, lIOEx);
			} catch (Exception lEx) {
				throw new WebSocketException(lEx.getClass().getSimpleName() + " while creating secure socket to " + mURI, lEx);
			}
		} else {
			throw new WebSocketException("Unsupported protocol: " + lScheme,
					WebSocketExceptionType.PROTOCOL_NOT_SUPPORTED);
		}

		return mSocket;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public boolean isConnected() {
		return mStatus.isConnected();
	}

	@Override
	public WebSocketStatus getStatus() {
		return mStatus;
	}

	/**
	 * {@inheritDoc }
	 * 
	 * @return 
	 */
	public WebSocketStatus getConnectionStatus() {
		return mStatus;
	}

	/**
	 * @return the client socket
	 */
	public Socket getConnectionSocket() {
		return mSocket;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addListener(WebSocketClientListener aListener) {
		mListeners.add(aListener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeListener(WebSocketClientListener aListener) {
		mListeners.remove(aListener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<WebSocketClientListener> getListeners() {
		return Collections.unmodifiableList(mListeners);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void notifyOpened(WebSocketClientEvent aEvent) {
		for (WebSocketClientListener lListener : getListeners()) {
			lListener.processOpened(aEvent);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void notifyPacket(WebSocketClientEvent aEvent, WebSocketPacket aPacket) {
		for (WebSocketClientListener lListener : getListeners()) {
			lListener.processPacket(aEvent, aPacket);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void notifyReconnecting(WebSocketClientEvent aEvent) {
		for (WebSocketClientListener lListener : getListeners()) {
			lListener.processReconnecting(aEvent);
		}
	}

	/**
	 * @return the mReliabilityOptions
	 */
	public ReliabilityOptions getReliabilityOptions() {
		return mReliabilityOptions;
	}

	/**
	 * @param mReliabilityOptions the mReliabilityOptions to set
	 */
	public void setReliabilityOptions(ReliabilityOptions mReliabilityOptions) {
		this.mReliabilityOptions = mReliabilityOptions;
	}
	/*
	class ReOpener implements Runnable {
	
	private WebSocketClientEvent mEvent;
	
	public ReOpener(WebSocketClientEvent aEvent) {
	mEvent = aEvent;
	}
	
	@Override
	public void run() {
	notifyReconnecting(mEvent);
	try {
	open(mURI.toString());
	} catch (WebSocketException ex) {
	// TODO: process potential exception here!
	}
	}
	}
	 */

	class ReOpener implements Runnable {

		private WebSocketClientEvent mEvent;

		public ReOpener(WebSocketClientEvent aEvent) {
			mEvent = aEvent;
		}

		@Override
		public void run() {
			notifyReconnecting(mEvent);
			try {
				open(mURI.toString());
				// did we configure reliability options?
				/*
				if (mReliabilityOptions != null
				&& mReliabilityOptions.getReconnectDelay() > 0) {
				mExecutor.schedule(
				new ReOpener(aEvent),
				mReliabilityOptions.getReconnectDelay(),
				TimeUnit.MILLISECONDS);
				}
				 */
			} catch (Exception lEx) {
				WebSocketClientEvent lEvent =
						new WebSocketBaseClientEvent(mEvent.getClient(), EVENT_CLOSE,
						lEx.getClass().getSimpleName() + ": "
						+ lEx.getMessage());
				notifyClosed(lEvent);
			}
		}
	}

	private void mCheckReconnect(WebSocketClientEvent aEvent) {
		// did we configure reliability options?
		if (mReliabilityOptions != null
				&& mReliabilityOptions.getReconnectDelay() > 0) {
			// schedule a re-connect action after the re-connect delay
			mExecutor.schedule(
					new ReOpener(aEvent),
					mReliabilityOptions.getReconnectDelay(),
					TimeUnit.MILLISECONDS);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void notifyClosed(WebSocketClientEvent aEvent) {
		for (WebSocketClientListener lListener : getListeners()) {
			lListener.processClosed(aEvent);
		}
	}

	@Override
	public void addSubProtocol(WebSocketSubProtocol aSubProt) {
		if (mSubprotocols == null) {
			mSubprotocols = new ArrayList<WebSocketSubProtocol>(3);
		}
		mSubprotocols.add(aSubProt);
	}

	@Override
	public String getNegotiatedSubProtocol() {
		return mNegotiatedSubProtocol == null ? null : mNegotiatedSubProtocol.getSubProt();
	}

	@Override
	public WebSocketEncoding getNegotiatedEncoding() {
		return mNegotiatedSubProtocol == null ? null : mNegotiatedSubProtocol.getEncoding();
	}

	@Override
	public void setVersion(int aVersion) {
		this.mVersion = aVersion;
	}

	private boolean isHixie() {
		return WebSocketProtocolAbstraction.isHixieVersion(mVersion);
	}

	class WebSocketReceiver extends Thread {

		private WebSocketClient mClient = null;
		private InputStream mIS = null;
		private volatile boolean mStop = false;

		public WebSocketReceiver(WebSocketClient aClient, InputStream aInput) {
			mClient = aClient;
			mIS = aInput;
		}

		@Override
		public void run() {
			Thread.currentThread().setName("jWebSocket-Client " + getId());
			try {
				if (isHixie()) {
					readHixie();
				} else {
					readHybi();
				}
			} catch (Exception lEx) {
				handleErrorAndClose();
			}
		}

		private void readHixie() throws IOException {
			boolean lFrameStart = false;
			ByteArrayOutputStream lBuff = new ByteArrayOutputStream();
			while (!mStop) {
				int lB = mIS.read();
				// TODO: support binary frames
				if (lB == 0x00) {
					lFrameStart = true;
				} else if (lB == 0xff && lFrameStart == true) {
					lFrameStart = false;

					WebSocketClientEvent lWSCE = new WebSocketTokenClientEvent(mClient, null, null);
					RawPacket lPacket = new RawPacket(lBuff.toByteArray());

					lBuff.reset();
					notifyPacket(lWSCE, lPacket);
				} else if (lFrameStart == true) {
					lBuff.write(lB);
				} else if (lB == -1) {
					handleErrorAndClose();
				}
			}
		}

		private void readHybi() throws WebSocketException, IOException {
			WebSocketClientEvent lWSCE;
			WebSocketFrameType lFrameType;

			while (!mStop) {
				try {
					WebSocketPacket lPacket = WebSocketProtocolAbstraction.protocolToRawPacket(mVersion, mIS);
					lFrameType = (lPacket != null ? lPacket.getFrameType() : WebSocketFrameType.INVALID);
					if (WebSocketFrameType.INVALID == lFrameType
							|| WebSocketFrameType.CLOSE == lFrameType) {
						mStop = true;
						mStatus = WebSocketStatus.CLOSED;
						lWSCE = new WebSocketBaseClientEvent(mClient, EVENT_CLOSE, "error");
						notifyClosed(lWSCE);
						mCheckReconnect(lWSCE);
					} else if (WebSocketFrameType.PING == lFrameType) {
						WebSocketPacket lPong = new RawPacket(
								WebSocketFrameType.PONG, "");
						send(lPong);
					} else if (WebSocketFrameType.PONG == lFrameType) {
						// TODO: need to process connection management here!
					} else {
						lWSCE = new WebSocketTokenClientEvent(mClient, null, null);
						notifyPacket(lWSCE, lPacket);
					}
				} catch (Exception lEx) {
					handleErrorAndClose();
				}
			}
		}

		public void stopit() {
			mStop = true;
		}

		public boolean isRunning() {
			return !mStop;
		}

		private void handleErrorAndClose() {
			if (!mStatus.isClosed()) {
				mStatus = WebSocketStatus.CLOSED;
				WebSocketClientEvent lEvent =
						new WebSocketBaseClientEvent(mClient, EVENT_CLOSE, "error");
				notifyClosed(lEvent);
			}
			stopit();
		}
	}
}
