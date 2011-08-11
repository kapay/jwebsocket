//	---------------------------------------------------------------------------
//	jWebSocket - WebSocket Protocol Handler
//	Copyright (c) 2010 Innotrade GmbH, jWebSocket.org
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
package org.jwebsocket.kit;

import org.jwebsocket.api.WebSocketPacket;
import java.util.List;

/**
 * Utility class for packetizing WebSocketPacket into web socket protocol packet or packets (with fragmentation) and
 * vice versa.
 * <p/>
 * <p>
 * Web socket protocol packet specification
 * (see: http://tools.ietf.org/html/draft-ietf-hybi-thewebsocketprotocol-03):
 * </p>
 * <pre>
 *  0                   1                   2                   3
 *  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 * +-+-+-+-+-------+-+-------------+-------------------------------+
 * |M|R|R|R| opcode|R| Payload len |    Extended payload length    |
 * |O|S|S|S|  (4)  |S|     (7)     |             (16/63)           |
 * |R|V|V|V|       |V|             |   (if payload len==126/127)   |
 * |E|1|2|3|       |4|             |                               |
 * +-+-+-+-+-------+-+-------------+ - - - - - - - - - - - - - - - +
 * |     Extended payload length continued, if payload len == 127  |
 * + - - - - - - - - - - - - - - - +-------------------------------+
 * |                               |         Extension data        |
 * +-------------------------------+ - - - - - - - - - - - - - - - +
 * :                                                               :
 * +---------------------------------------------------------------+
 * :                       Application data                        :
 * +---------------------------------------------------------------+
 * </pre>
 *
 * @author jang
 * @author aschulze
 */
public class WebSocketProtocolHandler {
	// web socket protocol packet types

	/**
	 * converts an abstract data packet into a protocol specific frame
	 * according to the correct version.
	 * @param aDataPacket
	 * @return
	 */
	public static byte[] toProtocolPacket(int aVersion, WebSocketPacket aDataPacket) {

		byte[] lBuff = new byte[2]; // resulting packet will have at least 2 bytes
		WebSocketFrameType lFrameType = aDataPacket.getFrameType();
		int lTargetType = frameTypeToOpcode(aVersion, lFrameType);
		if (lTargetType == -1) {
			throw new WebSocketRuntimeException("Cannot construct a packet with unknown packet type: " + lFrameType);
		}

		// 0x80 means it's the final frame, the RSV bits are not yet set
		lBuff[0] = (byte) (lTargetType | 0x80);

		int lPayloadLen = aDataPacket.getByteArray().length;

		// Here, the spec allows payload length with up to 64-bit integer
		// in size (that is long data type in java):
		// ----
		//   The length of the payload: if 0-125, that is the payload length.
		//   If 126, the following 2 bytes interpreted as a 16 bit unsigned
		//   integer are the payload length.  If 127, the following 8 bytes
		//   interpreted as a 64-bit unsigned integer (the high bit must be 0)
		//   are the payload length.
		// ----
		// However, arrays in java may only have Integer.MAX_VALUE(32-bit) elements.
		// Therefore, we never set target payload length greater than signed 32-bit number
		// (Integer.MAX_VALUE).
		if (lPayloadLen < 126) {
			lBuff[1] = (byte) (lPayloadLen); // just write the payload length
		} else if (lPayloadLen >= 126 && lPayloadLen < 0xFFFF) {
			// first write 126 (meaning, there will follow two bytes for actual length)
			lBuff[1] = (byte) (126 /*<< 1*/);
			int lSize = lBuff.length;
			lBuff = copyOf(lBuff, lSize + 2);
			lBuff[lSize] = (byte) ((lPayloadLen >>> 8) & 0xFF);
			lBuff[lSize + 1] = (byte) (lPayloadLen & 0xFF);
		} else if (lPayloadLen >= 0xFFFF) {
			// first write 127 (meaning, there will follow eight bytes for actual length)
			lBuff[1] = (byte) (127 << 1);
			long len = (long) lPayloadLen;
			int lSize = lBuff.length;
			lBuff = copyOf(lBuff, lSize + 8);
			lBuff[lSize] = (byte) (len >>> 56);
			lBuff[lSize + 1] = (byte) (len >>> 48);
			lBuff[lSize + 2] = (byte) (len >>> 40);
			lBuff[lSize + 3] = (byte) (len >>> 32);
			lBuff[lSize + 4] = (byte) (len >>> 24);
			lBuff[lSize + 5] = (byte) (len >>> 16);
			lBuff[lSize + 6] = (byte) (len >>> 8);
			lBuff[lSize + 7] = (byte) len;
		}

		int lSize = lBuff.length;
		lBuff = copyOf(lBuff, lSize + aDataPacket.getByteArray().length);
		System.arraycopy(aDataPacket.getByteArray(), 0, lBuff, lSize, aDataPacket.getByteArray().length);
		return lBuff;
	}

	/* TODO: implement fragmentation */
	/**
	 *
	 * @param aSrc
	 * @param aFragmentSize
	 * @return
	 */
	public static List<byte[]> toProtocolPacketFragmented(WebSocketPacket aSrc, int aFragmentSize) {
		throw new UnsupportedOperationException("Fragmentation is currently not supported");
	}

	/**
	 * converts a WebSocket protocol opcode to an abstract jWebSocket frame type
	 * @param aOpCode
	 * @return
	 */
	public static WebSocketFrameType opcodeToFrameType(int aVersion, int aOpcode) {

		WebSocketOpcode lOpcode = new WebSocketOpcode(aVersion);

		if (aOpcode == lOpcode.OPCODE_FRAGMENT) {
			return WebSocketFrameType.FRAGMENT;
		} else if (aOpcode == lOpcode.OPCODE_TEXT) {
			return WebSocketFrameType.TEXT;
		} else if (aOpcode == lOpcode.OPCODE_BINARY) {
			return WebSocketFrameType.BINARY;
		} else if (aOpcode == lOpcode.OPCODE_CLOSE) {
			return WebSocketFrameType.CLOSE;
		} else if (aOpcode == lOpcode.OPCODE_PING) {
			return WebSocketFrameType.PING;
		} else if (aOpcode == lOpcode.OPCODE_PONG) {
			return WebSocketFrameType.PONG;
		} else {
			return WebSocketFrameType.INVALID;
		}
	}

	/**
	 *
	 * @param aJWebSocketFormatConstant
	 * @return
	 */
	/*
	public static int toRawPacketType(String aJWebSocketFormatConstant) {
	return JWebSocketCommonConstants.WSWS_FORMAT_BINARY.equals(aJWebSocketFormatConstant)
	? RawPacket.FRAMETYPE_BINARY
	// treat everything else as utf8 packet type
	: RawPacket.FRAMETYPE_UTF8;
	}
	 */
	/**
	 *
	 * @param aFrameType
	 * @return
	 */
	public static int frameTypeToOpcode(int aVersion, WebSocketFrameType aFrameType) {

		WebSocketOpcode lOpcode = new WebSocketOpcode(aVersion);

		switch (aFrameType) {
			case FRAGMENT:
				return lOpcode.OPCODE_FRAGMENT;
			case TEXT:
				return lOpcode.OPCODE_TEXT;
			case BINARY:
				return lOpcode.OPCODE_BINARY;
			case CLOSE:
				return lOpcode.OPCODE_CLOSE;
			case PING:
				return lOpcode.OPCODE_PING;
			case PONG:
				return lOpcode.OPCODE_PONG;
			default:
				return lOpcode.OPCODE_INVALID;
		}
	}

	private static byte[] copyOf(byte[] aOriginal, int aNewLength) {
		byte[] lCopy = new byte[aNewLength];
		System.arraycopy(aOriginal, 0, lCopy, 0, Math.min(aOriginal.length, aNewLength));
		return lCopy;
	}
}
