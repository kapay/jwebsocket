package org.jwebsocket.tcp.nio;

import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.async.IOFuture;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.connectors.BaseConnector;
import org.jwebsocket.kit.RawPacket;
import org.jwebsocket.kit.WebSocketProtocolHandler;
import org.jwebsocket.logging.Logging;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class NioTcpConnector extends BaseConnector {
	private static Logger mLog = Logging.getLogger(NioTcpConnector.class);
	private InetAddress remoteAddress;
	private int remotePort;
	private boolean afterHandshake;
	private byte[] packetBuffer;
	private int payloadLength = -1;
	private int bufferPosition = -1;
	private int packetType = -1;

	public NioTcpConnector(NioTcpEngine engine, InetAddress remoteAddress, int remotePort) {
		super(engine);

		this.remoteAddress = remoteAddress;
		this.remotePort = remotePort;
		afterHandshake = false;
	}

	@Override
	public void sendPacket(WebSocketPacket packet) {
		sendPacketAsync(packet); // nio engine works asynchronously by default
	}

	@Override
	public IOFuture sendPacketAsync(WebSocketPacket packet) {
		byte[] protocolPacket;
		if (isHixieDraft()) {
			protocolPacket = new byte[packet.getByteArray().length + 2];
			protocolPacket[0] = 0x00;
			System.arraycopy(packet.getByteArray(), 0, protocolPacket, 1, packet.getByteArray().length);
			protocolPacket[protocolPacket.length - 1] = (byte) 0xFF;
		} else {
			protocolPacket = WebSocketProtocolHandler.toProtocolPacket(packet);
		}

		DataFuture future = new DataFuture(this, ByteBuffer.wrap(protocolPacket));
		((NioTcpEngine) getEngine()).send(getId(), future);
		return future;
	}

	@Override
	public String getId() {
		return String.valueOf(hashCode());
	}

	@Override
	public String generateUID() {
		return remoteAddress.getHostAddress() + '@' + remotePort;
	}

	@Override
	public InetAddress getRemoteHost() {
		return remoteAddress;
	}

	@Override
	public int getRemotePort() {
		return remotePort;
	}

	public void handshakeValidated() {
		afterHandshake = true;
	}

	public boolean isAfterHandshake() {
		return afterHandshake;
	}

	public boolean isPacketBufferEmpty() {
		return packetBuffer == null;
	}
	
	public void extendPacketBuffer(byte[] newData, int start, int count) {
		if(payloadLength == -1) {
			// packet buffer grows with new data
			if(packetBuffer == null) {
				packetBuffer = new byte[count];
				if(count > 0) {
					System.arraycopy(newData, start, packetBuffer, 0, count);
				}
			} else {
				byte[] newBuffer = new byte[packetBuffer.length + count];
				System.arraycopy(packetBuffer, 0, newBuffer, 0, packetBuffer.length);
				System.arraycopy(newData, start, newBuffer, packetBuffer.length, count);
				packetBuffer = newBuffer;
			}
		} else {
			// packet buffer was already created with the correct length
		    System.arraycopy(newData, start, packetBuffer, bufferPosition, count);
			bufferPosition += count;
		}
	}

	public byte[] getPacketBuffer() {
		return packetBuffer;
	}

	public void flushPacketBuffer() {
		byte[] copy = new byte[packetBuffer.length];
		System.arraycopy(packetBuffer, 0, copy, 0, packetBuffer.length);

		RawPacket lPacket = new RawPacket(copy);
		if(packetType != -1) {
			lPacket.setFrameType(packetType);
		}
		try {
			getEngine().processPacket(this, lPacket);
		} catch (Exception e) {
			mLog.error(e.getClass().getSimpleName()
					+ " in processPacket of connector "
					+ getClass().getSimpleName(), e);
		}

		packetBuffer = null;
		payloadLength = -1;
		packetType = -1;
	}

	public void setPayloadLength(int length) {
		payloadLength = length;
		packetBuffer = new byte[length];
		bufferPosition = 0;
	}

	public boolean isPacketBufferFull() {
		return bufferPosition >= payloadLength;
	}

	public void setPacketType(int packetType) {
		this.packetType = packetType;
	}

	private boolean isHixieDraft() {
		return JWebSocketCommonConstants.WS_DRAFT_DEFAULT.equals(getHeader().getDraft());
	}	
}
