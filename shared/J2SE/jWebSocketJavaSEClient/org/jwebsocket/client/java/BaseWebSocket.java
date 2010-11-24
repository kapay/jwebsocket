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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

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
		} catch (URISyntaxException ex) {
			throw new WebSocketException("Error parsing WebSocket URL:" + aURIString, ex);
		}
		this.mURL = lURI;
		String lSubProtocol = makeSubprotocolHeader();
		WebSocketHandshake mHandshake = new WebSocketHandshake(mURL, lSubProtocol, mDraft);
		try {
			mSocket = createSocket();
			mInput = mSocket.getInputStream();
			mOutput = new PrintStream(mSocket.getOutputStream());

			mOutput.write(mHandshake.getHandshake());

			boolean handshakeComplete = false;
			boolean header = true;
			int len = 1000;
			byte[] buffer = new byte[len];
			int pos = 0;
			ArrayList<String> handshakeLines = new ArrayList<String>();

			byte[] serverResponse = new byte[16];

			while (!handshakeComplete) {
				mStatus = WebSocketStatus.CONNECTING;
				int b = mInput.read();
				buffer[pos] = (byte) b;
				pos += 1;

				if (!header) {
					serverResponse[pos - 1] = (byte) b;
					if (pos == 16) {
						handshakeComplete = true;
					}
				} else if (buffer[pos - 1] == 0x0A && buffer[pos - 2] == 0x0D) {
					String line = new String(buffer, "UTF-8");
					if (line.trim().equals("")) {
						header = false;
					} else {
						handshakeLines.add(line.trim());
					}

					buffer = new byte[len];
					pos = 0;
				}
			}

			mHandshake.verifyServerStatusLine(handshakeLines.get(0));
			mHandshake.verifyServerResponse(serverResponse);

			handshakeLines.remove(0);

			Map<String, String> headers = new FastMap<String, String>();
			for (String line : handshakeLines) {
				String[] keyValue = line.split(": ", 2);
				headers.put(keyValue[0], keyValue[1]);
			}
			mHandshake.verifyServerHandshakeHeaders(headers);

			// set negotiated sub protocol
			if (headers.containsKey("Sec-WebSocket-Protocol")) {
				String lHeader = headers.get("Sec-WebSocket-Protocol");
				if (lHeader.indexOf('/') == -1) {
					mNegotiatedSubprotocol = new SubProtocol(lHeader, JWebSocketCommonConstants.WS_FORMAT_DEFAULT);
				} else {
					String[] lSplit = lHeader.split("/");
					mNegotiatedSubprotocol = new SubProtocol(lSplit[0], lSplit[1]);
				}
			} else {
				// just default to 'jwebsocket.org/json'
				mNegotiatedSubprotocol = new SubProtocol(JWebSocketCommonConstants.WS_SUBPROTOCOL_DEFAULT,
						JWebSocketCommonConstants.WS_FORMAT_DEFAULT);
			}

			mReceiver = new WebSocketReceiver(mInput);

			// TODO: Add event parameter
			// notifyOpened(null);

			mReceiver.start();
			mConnected = true;
			mStatus = WebSocketStatus.OPEN;
		} catch (IOException ioe) {
			throw new WebSocketException("error while connecting: " + ioe.getMessage(), ioe);
		}
	}

	@Override
	public void send(byte[] aData) throws WebSocketException {
		if (isHixieDraft()) {
			sendInternal(aData);
		} else {
			WebSocketPacket lPacket = new RawPacket(aData);
			lPacket.setFrameType(WebSocketProtocolHandler.toRawPacketType(mNegotiatedSubprotocol.format));
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
		} catch (UnsupportedEncodingException e) {
			throw new WebSocketException("Encoding exception while sending the data:" + e.getMessage(), e);
		}

		send(lData);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void send(WebSocketPacket dataPacket) throws WebSocketException {
		if (isHixieDraft()) {
			sendInternal(dataPacket.getByteArray());
		} else {
			if (isBinaryFormat() && (dataPacket.getFrameType() != RawPacket.FRAMETYPE_BINARY)) {
				// we negotiated binary format with the server
				throw new WebSocketException("Only binary packets are allowed for this connection");
			}

			sendInternal(WebSocketProtocolHandler.toProtocolPacket(dataPacket));
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
		} catch (IOException ex) {
			throw new WebSocketException("error while sending socket data: ", ex);
		}
	}


	public void handleReceiverError() {
		try {
			if (mConnected) {
				mStatus = WebSocketStatus.CLOSING;
				close();
			}
		} catch (WebSocketException wse) {
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
		} catch (IOException ioe) {
			throw new WebSocketException("error while closing websocket connection: ", ioe);
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
		} catch (IOException ioe) {
			throw new WebSocketException("error while sending close handshake", ioe);
		}
		mConnected = false;
	}

	private Socket createSocket() throws WebSocketException {
		String scheme = mURL.getScheme();
		String host = mURL.getHost();
		int port = mURL.getPort();

		mSocket = null;

		if (scheme != null && scheme.equals("ws")) {
			if (port == -1) {
				port = 80;
			}
			try {
				mSocket = new Socket(host, port);
			} catch (UnknownHostException uhe) {
				throw new WebSocketException("unknown host: " + host, uhe);
			} catch (IOException ioe) {
				throw new WebSocketException("error while creating socket to " + mURL, ioe);
			}
		} else if (scheme != null && scheme.equals("wss")) {
			if (port == -1) {
				port = 443;
			}
			try {
				SocketFactory factory = SSLSocketFactory.getDefault();
				mSocket = factory.createSocket(host, port);
			} catch (UnknownHostException uhe) {
				throw new WebSocketException("unknown host: " + host, uhe);
			} catch (IOException ioe) {
				throw new WebSocketException("error while creating secure socket to " + mURL, ioe);
			}
		} else {
			throw new WebSocketException("unsupported protocol: " + scheme);
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
	public void addSubProtocol(String protocolName, String protocolFormat) {
		if (mSubprotocols == null) {
			mSubprotocols = new ArrayList<SubProtocol>(5);
		}

		mSubprotocols.add(new SubProtocol(protocolName, protocolFormat));
	}

	@Override
	public String getNegotiatedProtocolName() {
		return mNegotiatedSubprotocol == null ? null : mNegotiatedSubprotocol.name;
	}

	@Override
	public String getNegotiatedProtocolFormat() {
		return mNegotiatedSubprotocol == null ? null : mNegotiatedSubprotocol.format;
	}

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
//			return JWebSocketCommonConstants.WS_SUBPROTOCOL_DEFAULT + '/' + JWebSocketCommonConstants.WS_FORMAT_DEFAULT;
			return "jwebsocket.org/json jwebsocket.org/xml chat.example.com/custom"; 
		} else {
			StringBuilder buff = new StringBuilder();
			for (SubProtocol prot : mSubprotocols) {
				buff.append(prot.toString()).append(' ');
			}
			return buff.toString().trim();
		}
	}

	private boolean isHixieDraft() {
		return JWebSocketCommonConstants.WS_DRAFT_DEFAULT.equals(mDraft);
	}

	private boolean isBinaryFormat() {
		return mNegotiatedSubprotocol != null
				&& JWebSocketCommonConstants.WS_FORMAT_BINARY.equals(mNegotiatedSubprotocol.format);
	}

	class SubProtocol {
		String name;
		String format;

		private SubProtocol(String name, String format) {
			this.name = name;
			this.format = format;
		}

		@Override
		public int hashCode() {
			return name.hashCode() * 31 + format.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof SubProtocol) {
				SubProtocol other = (SubProtocol) obj;
				return name.equals(other.name) && format.equals(other.format);
			} else {
				return super.equals(obj);
			}
		}

		@Override
		public String toString() {
			StringBuilder buff = new StringBuilder();
			buff.append(name).append('/').append(format);
			return buff.toString();
		}
	}

	class WebSocketReceiver extends Thread {

		private InputStream mIS = null;
		private volatile boolean mStop = false;

		public WebSocketReceiver(InputStream input) {
			this.mIS = input;
		}

		@Override
		public void run() {
			ByteArrayOutputStream lOS = new ByteArrayOutputStream();
			try {
				if (isHixieDraft()) {
					readHixie();
				} else {
					readHybi();
				}
			} catch (Exception e) {
				handleError();
			}
		}

		private void readHixie() throws IOException {
			boolean lFrameStart = false;
			ByteArrayOutputStream aBuff = new ByteArrayOutputStream();
			while (!mStop) {
				int b = mIS.read();
				// TODO: support binary frames
				if (b == 0x00) {
					lFrameStart = true;
				} else if (b == 0xff && lFrameStart == true) {
					lFrameStart = false;

					WebSocketClientEvent lWSCE = new WebSocketClientTokenEvent();
					RawPacket lPacket = new RawPacket(aBuff.toByteArray());

					aBuff.reset();
					notifyPacket(lWSCE, lPacket);
				} else if (lFrameStart == true) {
					aBuff.write(b);
				} else if (b == -1) {
					handleError();
				}
			}
		}

		private void readHybi() throws WebSocketException, IOException {
			int lPacketType;
			// utilize data input stream, because it has convenient methods for reading
			// signed/unsigned bytes, shorts, ints and longs
			DataInputStream lDis = new DataInputStream(mIS);
			ByteArrayOutputStream aBuff = new ByteArrayOutputStream();

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
							aBuff.write(lDis.read());
						}
					}

					if (!lFragmented) {
						if (lPacketType == RawPacket.FRAMETYPE_PING) {
							// As per spec, we must respond to PING with PONG (maybe
							// this should be handled higher up in the hierarchy?)
							WebSocketPacket lPong = new RawPacket(aBuff.toByteArray());
							lPong.setFrameType(RawPacket.FRAMETYPE_PONG);
							send(lPong);
						} else if (lPacketType == RawPacket.FRAMETYPE_CLOSE) {
							close();
						}

						// Packet was read, pass it forward.
						WebSocketPacket lPacket = new RawPacket(aBuff.toByteArray());
						lPacket.setFrameType(lPacketType);
						WebSocketClientEvent lWSCE = new WebSocketClientTokenEvent();
						notifyPacket(lWSCE, lPacket);
						aBuff.reset();
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
