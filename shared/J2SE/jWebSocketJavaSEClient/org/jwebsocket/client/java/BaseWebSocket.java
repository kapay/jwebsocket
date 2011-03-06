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
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import javolution.util.FastList;
import javolution.util.FastMap;
import org.jwebsocket.api.WebSocketClient;
import org.jwebsocket.api.WebSocketClientEvent;
import org.jwebsocket.api.WebSocketClientListener;
import org.jwebsocket.api.WebSocketPacket;

import org.jwebsocket.api.WebSocketStatus;
import org.jwebsocket.client.token.WebSocketClientTokenEvent;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.kit.RawPacket;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.kit.WebSocketHandshake;
import org.jwebsocket.kit.WebSocketProtocolHandler;

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
 * @version $Id:$
 */
public class BaseWebSocket implements WebSocketClient {

	/**
	 * WebSocket connection url
	 */
	private URI mURL = null;
	/**
	 * list of the listeners registered
	 */
	private List<WebSocketClientListener> mListeners = new FastList<WebSocketClientListener>();
	/**
	 * flag for connection test
	 */
	private volatile boolean mConnected = false;
	/**
	 * TCP socket
	 */
	private Socket mSocket = null;
	/**
	 * IO streams
	 */
	private InputStream mInput = null;
	private PrintStream mOutput = null;
	/**
	 * Data receiver
	 */
	private WebSocketReceiver mReceiver = null;
	/**
	 * represents the WebSocket status
	 */
	private WebSocketStatus mStatus = WebSocketStatus.CLOSED;
	private List<SubProtocol> mSubprotocols;
	private SubProtocol mNegotiatedSubprotocol;
	private String mDraft = JWebSocketCommonConstants.WS_DRAFT_DEFAULT;

	/**
	 * Base constructor
	 */
	public BaseWebSocket() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void open(String aURIString) throws WebSocketException {
		URI lURI = null;
		try {
			lURI = new URI(aURIString);
		} catch (URISyntaxException lEx) {
			throw new WebSocketException("Error parsing WebSocket URL:" + aURIString, lEx);
		}
		this.mURL = lURI;
		String lSubProtocol = makeSubprotocolHeader();
		WebSocketHandshake lHandshake = new WebSocketHandshake(mURL, lSubProtocol, mDraft);
		try {
			mSocket = createSocket();
			mInput = mSocket.getInputStream();
			mOutput = new PrintStream(mSocket.getOutputStream());

			mOutput.write(lHandshake.getHandshake());

			boolean lHandshakeComplete = false;
			boolean lHeader = true;
			int len = 1000;
			byte[] lBuffer = new byte[len];
			int lPos = 0;
			ArrayList<String> lHandshakeLines = new ArrayList<String>();

			byte[] lServerResponse = new byte[16];

			while (!lHandshakeComplete) {
				mStatus = WebSocketStatus.CONNECTING;
				int lB = mInput.read();
				lBuffer[lPos] = (byte) lB;
				lPos += 1;

				if (!lHeader) {
					lServerResponse[lPos - 1] = (byte) lB;
					if (lPos == 16) {
						lHandshakeComplete = true;
					}
				} else if (lBuffer[lPos - 1] == 0x0A && lBuffer[lPos - 2] == 0x0D) {
					String line = new String(lBuffer, "UTF-8");
					if (line.trim().equals("")) {
						lHeader = false;
					} else {
						lHandshakeLines.add(line.trim());
					}

					lBuffer = new byte[len];
					lPos = 0;
				}
			}

			lHandshake.verifyServerStatusLine(lHandshakeLines.get(0));
			lHandshake.verifyServerResponse(lServerResponse);

			lHandshakeLines.remove(0);

			Map<String, String> lHeaders = new FastMap<String, String>();
			for (String lLine : lHandshakeLines) {
				String[] lKeyVal = lLine.split(": ", 2);
				lHeaders.put(lKeyVal[0], lKeyVal[1]);
			}
			lHandshake.verifyServerHandshakeHeaders(lHeaders);

			// set negotiated sub protocol
			if (lHeaders.containsKey("Sec-WebSocket-Protocol")) {
				String llHeader = lHeaders.get("Sec-WebSocket-Protocol");
				if (llHeader.indexOf('/') == -1) {
					mNegotiatedSubprotocol = new SubProtocol(
							llHeader, JWebSocketCommonConstants.WS_FORMAT_DEFAULT);
				} else {
					String[] lSplit = llHeader.split("/");
					mNegotiatedSubprotocol = new SubProtocol(lSplit[0], lSplit[1]);
				}
			} else {
				// just default to 'jwebsocket.org/json'
				mNegotiatedSubprotocol = new SubProtocol(
						JWebSocketCommonConstants.WS_SUBPROTOCOL_DEFAULT,
						JWebSocketCommonConstants.WS_FORMAT_DEFAULT);
			}

			mReceiver = new WebSocketReceiver(mInput);

			// TODO: Add event parameter
			notifyOpened(null);

			mReceiver.start();
			mConnected = true;
			mStatus = WebSocketStatus.OPEN;
		} catch (IOException lIOEx) {
			throw new WebSocketException(
					"Error while connecting: "
					+ lIOEx.getMessage(), lIOEx);
		}
	}

	@Override
	public void send(byte[] aData) throws WebSocketException {
		if (isHixieDraft()) {
			sendInternal(aData);
		} else {
			WebSocketPacket lPacket = new RawPacket(aData);
			lPacket.setFrameType(WebSocketProtocolHandler.toRawPacketType(mNegotiatedSubprotocol.mFormat));
			sendInternal(WebSocketProtocolHandler.toProtocolPacket(lPacket));
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
	 */
	@Override
	public void send(WebSocketPacket aDataPacket) throws WebSocketException {
		if (isHixieDraft()) {
			sendInternal(aDataPacket.getByteArray());
		} else {
			if (isBinaryFormat() && (aDataPacket.getFrameType() != RawPacket.FRAMETYPE_BINARY)) {
				// we negotiated binary format with the server
				throw new WebSocketException(
						"Only binary packets are allowed for this connection");
			}

			sendInternal(WebSocketProtocolHandler.toProtocolPacket(aDataPacket));
		}
	}

	private void sendInternal(byte[] aData) throws WebSocketException {
		if (!mConnected) {
			throw new WebSocketException("error while sending binary data: not connected");
		}
		try {
			if (isHixieDraft()) {
				if (isBinaryFormat()) {
					mOutput.write(0x80);
					// TODO: what if frame is longer than 255 characters (8bit?) Refer to IETF spec!
					mOutput.write(aData.length);
					mOutput.write(aData);
				} else {
					mOutput.write(0x00);
					mOutput.write(aData);
					mOutput.write(0xff);
				}
			} else {
				mOutput.write(aData);
			}
			mOutput.flush();
		} catch (IOException lEx) {
			throw new WebSocketException("error while sending socket data: ", lEx);
		}
	}

	public void handleReceiverError() {
		try {
			if (mConnected) {
				mStatus = WebSocketStatus.CLOSING;
				close();
			}
		} catch (WebSocketException lWSE) {
			// TODO: don't use printStackTrace
			// wse.printStackTrace();
		}
	}

	@Override
	public synchronized void close() throws WebSocketException {
		if (!mConnected) {
			return;
		}
		sendCloseHandshake();
		if (mReceiver.isRunning()) {
			mReceiver.stopit();
		}
		try {
			// input.close();
			// output.close();
			mSocket.shutdownInput();
			mSocket.shutdownOutput();
			mSocket.close();
			mStatus = WebSocketStatus.CLOSED;
		} catch (IOException lIOEx) {
			throw new WebSocketException("error while closing websocket connection: ", lIOEx);
		}
		// TODO: add event
		notifyClosed(null);
	}

	private void sendCloseHandshake() throws WebSocketException {
		if (!mConnected) {
			throw new WebSocketException("error while sending close handshake: not connected");
		}
		try {
			if (isHixieDraft()) {
				mOutput.write(0xff00);
				// TODO: check if final CR/LF is required/valid!
				mOutput.write("\r\n".getBytes());
				// TODO: shouldn't we put a flush here?
			} else {
				WebSocketPacket lPacket = new RawPacket("BYE");
				lPacket.setFrameType(RawPacket.FRAMETYPE_CLOSE);
				send(lPacket);
			}
		} catch (IOException lIOEx) {
			throw new WebSocketException("error while sending close handshake", lIOEx);
		}
		mConnected = false;
	}

	private Socket createSocket() throws WebSocketException {
		String lScheme = mURL.getScheme();
		String lHost = mURL.getHost();
		int lPort = mURL.getPort();

		mSocket = null;

		if (lScheme != null && lScheme.equals("ws")) {
			if (lPort == -1) {
				lPort = 80;
			}
			try {
				mSocket = new Socket(lHost, lPort);
			} catch (UnknownHostException lUHEx) {
				throw new WebSocketException("unknown host: " + lHost, lUHEx);
			} catch (IOException lIOEx) {
				throw new WebSocketException("error while creating socket to " + mURL, lIOEx);
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

							public java.security.cert.X509Certificate[] getAcceptedIssuers() {
								return null;
							}

							public void checkClientTrusted(
									java.security.cert.X509Certificate[] aCerts, String aAuthType) {
							}

							public void checkServerTrusted(
									java.security.cert.X509Certificate[] aCerts, String aAuthType) {
							}
						}
					};
					// Use this trustmanager to not reject unsigned certificates
					SSLContext lSSLContext = SSLContext.getInstance("SSL");
					lSSLContext.init(null, lTrustManager, new java.security.SecureRandom());
					mSocket = (SSLSocket) lSSLContext.getSocketFactory().createSocket(lHost, lPort);
				} catch (NoSuchAlgorithmException lNSAEx) {
					throw new RuntimeException("Unable to initialise SSL context", lNSAEx);
				} catch (KeyManagementException lKMEx) {
					throw new RuntimeException("Unable to initialise SSL context", lKMEx);
				}
			} catch (UnknownHostException lUHEx) {
				throw new WebSocketException("unknown host: " + lHost, lUHEx);
			} catch (IOException lIOEx) {
				throw new WebSocketException("error while creating secure socket to " + mURL, lIOEx);
			} catch (Exception lEx) {
				throw new WebSocketException(lEx.getClass().getSimpleName() + " while creating secure socket to " + mURL, lEx);
			}
		} else {
			throw new WebSocketException("unsupported protocol: " + lScheme);
		}

		return mSocket;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public boolean isConnected() {
		return mConnected && mStatus.equals(WebSocketStatus.OPEN);
	}

	/**
	 * {@inheritDoc }
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
	public void notifyClosed(WebSocketClientEvent aEvent) {
		for (WebSocketClientListener lListener : getListeners()) {
			lListener.processClosed(aEvent);
		}
	}

	@Override
	public void addSubProtocol(String aProtocolName, String aProtocolFormat) {
		if (mSubprotocols == null) {
			mSubprotocols = new ArrayList<SubProtocol>(5);
		}

		mSubprotocols.add(new SubProtocol(aProtocolName, aProtocolFormat));
	}

	@Override
	public String getNegotiatedProtocolName() {
		return mNegotiatedSubprotocol == null ? null : mNegotiatedSubprotocol.mName;
	}

	@Override
	public String getNegotiatedProtocolFormat() {
		return mNegotiatedSubprotocol == null ? null : mNegotiatedSubprotocol.mFormat;
	}

	@Override
	public void setDraft(String aDraft) {
		this.mDraft = aDraft;
	}

	/**
	 * Make a subprotocol string for Sec-WebSocket-Protocol header.
	 * The result is something like this:
	 * <pre>
	 * chat.example.com/json v2.chat.example.com/xml audio.chat.example.com/binary
	 * </pre>
	 *
	 * @return subprotocol list in one string
	 */
	private String makeSubprotocolHeader() {
		if (mSubprotocols == null || mSubprotocols.size() < 1) {
			//return JWebSocketCommonConstants.WS_SUBPROTOCOL_DEFAULT + '/' + JWebSocketCommonConstants.WS_FORMAT_DEFAULT;
			return null;
		} else {
			StringBuilder lBuff = new StringBuilder();
			for (SubProtocol lProt : mSubprotocols) {
				lBuff.append(lProt.toString()).append(' ');
			}
			return lBuff.toString().trim();
		}
	}

	private boolean isHixieDraft() {
		return JWebSocketCommonConstants.WS_DRAFT_DEFAULT.equals(mDraft);
	}

	private boolean isBinaryFormat() {
		return mNegotiatedSubprotocol != null
				&& JWebSocketCommonConstants.WS_FORMAT_BINARY.equals(mNegotiatedSubprotocol.mFormat);
	}

	class SubProtocol {

		private String mName;
		private String mFormat;

		private SubProtocol(String aName, String aFormat) {
			this.mName = aName;
			this.mFormat = aFormat;
		}

		@Override
		public int hashCode() {
			return mName.hashCode() * 31 + mFormat.hashCode();
		}

		@Override
		public boolean equals(Object aObj) {
			if (aObj instanceof SubProtocol) {
				SubProtocol lOther = (SubProtocol) aObj;
				return mName.equals(lOther.mName) && mFormat.equals(lOther.mFormat);
			} else {
				return super.equals(aObj);
			}
		}

		@Override
		public String toString() {
			StringBuilder lBuff = new StringBuilder();
			lBuff.append(mName).append('/').append(mFormat);
			return lBuff.toString();
		}
	}

	class WebSocketReceiver extends Thread {

		private InputStream mIS = null;
		private volatile boolean mStop = false;

		public WebSocketReceiver(InputStream aInput) {
			this.mIS = aInput;
		}

		@Override
		public void run() {
			try {
				if (isHixieDraft()) {
					readHixie();
				} else {
					readHybi();
				}
			} catch (Exception lEx) {
				handleError();
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

					WebSocketClientEvent lWSCE = new WebSocketClientTokenEvent();
					RawPacket lPacket = new RawPacket(lBuff.toByteArray());

					lBuff.reset();
					notifyPacket(lWSCE, lPacket);
				} else if (lFrameStart == true) {
					lBuff.write(lB);
				} else if (lB == -1) {
					handleError();
				}
			}
		}

		private void readHybi() throws WebSocketException, IOException {
			int lPacketType;
			// utilize data input stream, because it has convenient methods for reading
			// signed/unsigned bytes, shorts, ints and longs
			DataInputStream lDis = new DataInputStream(mIS);
			ByteArrayOutputStream lBuff = new ByteArrayOutputStream();

			while (!mStop) {
				// begin normal packet read
				int lFlags = lDis.read();
				// determine fragmentation
				boolean lFragmented = (0x01 & lFlags) == 0x01;
				// shift 4 bits to skip the first bit and three RSVx bits
				int lType = lFlags >> 4;
				lPacketType = WebSocketProtocolHandler.toRawPacketType(lType);

				if (lPacketType == -1) {
					// Could not determine packet type, ignore the packet.
					// Maybe we need a setting to decide, if such packets should abort the connection?
					handleError();
				} else {
					// Ignore first bit. Payload length is next seven bits, unless its value is greater than 125.
					long lPayloadLen = mIS.read() >> 1;
					if (lPayloadLen == 126) {
						// following two bytes are acutal payload length (16-bit unsigned integer)
						lPayloadLen = lDis.readUnsignedShort();
					} else if (lPayloadLen == 127) {
						// following eight bytes are actual payload length (64-bit unsigned integer)
						lPayloadLen = lDis.readLong();
					}

					if (lPayloadLen > 0) {
						// payload length may be extremely long, so we read in loop rather
						// than construct one byte[] array and fill it with read() method,
						// because java does not allow longs as array size
						while (lPayloadLen-- > 0) {
							lBuff.write(lDis.read());
						}
					}

					if (!lFragmented) {
						if (lPacketType == RawPacket.FRAMETYPE_PING) {
							// As per spec, we must respond to PING with PONG (maybe
							// this should be handled higher up in the hierarchy?)
							WebSocketPacket lPong = new RawPacket(lBuff.toByteArray());
							lPong.setFrameType(RawPacket.FRAMETYPE_PONG);
							send(lPong);
						} else if (lPacketType == RawPacket.FRAMETYPE_CLOSE) {
							close();
						}

						// Packet was read, pass it forward.
						WebSocketPacket lPacket = new RawPacket(lBuff.toByteArray());
						lPacket.setFrameType(lPacketType);
						WebSocketClientEvent lWSCE = new WebSocketClientTokenEvent();
						notifyPacket(lWSCE, lPacket);
						lBuff.reset();
					}
				}
			}
		}

		public void stopit() {
			mStop = true;
		}

		public boolean isRunning() {
			return !mStop;
		}

		private void handleError() {
			stopit();
		}
	}
}
