/*
 *  Copyright (c) 2009 Puran Singh(mailtopuran@gmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jwebsocket.core.impl;

import java.nio.CharBuffer;

import org.apache.mina.common.IoBuffer;
import org.apache.mina.common.IoSession;
import org.jwebsocket.core.server.WebSocketServerHandler;


/**
 * Defines all the WebSocket protocol constants.
 * 
 * @author <a href="http://blog.purans.net"> Puran Singh</a> &lt;<a
 *         href="mailto:puran@programmer.net"> puran@programmer.net </a>>
 * @version $Id$
 * 
 */
public final class WebSocketProtocol {

	/** data frame start and end */
	public static byte DATA_FRAME_START = 0x00;
	public static byte DATA_FRAME_END = (byte) 0xFF;

	public static final byte[] INITIAL_RESPONSE = new byte[] { 0x48, 0x54,
			0x54, 0x50, 0x2F, 0x31, 0x2E, 0x31, 0x20, 0x31, 0x30, 0x31, 0x20,
			0x57, 0x65, 0x62, 0x20, 0x53, 0x6F, 0x63, 0x6B, 0x65, 0x74, 0x20,
			0x50, 0x72, 0x6F, 0x74, 0x6F, 0x63, 0x6F, 0x6C, 0x20, 0x48, 0x61,
			0x6E, 0x64, 0x73, 0x68, 0x61, 0x6B, 0x65, 0x0D, 0x0A, 0x55, 0x70,
			0x67, 0x72, 0x61, 0x64, 0x65, 0x3A, 0x20, 0x57, 0x65, 0x62, 0x53,
			0x6F, 0x63, 0x6B, 0x65, 0x74, 0x0D, 0x0A, 0x43, 0x6F, 0x6E, 0x6E,
			0x65, 0x63, 0x74, 0x69, 0x6F, 0x6E, 0x3A, 0x20, 0x55, 0x70, 0x67,
			0x72, 0x61, 0x64, 0x65, 0x0D, 0x0A, 0x57, 0x65, 0x62, 0x53, 0x6F,
			0x63, 0x6B, 0x65, 0x74, 0x2D, 0x4F, 0x72, 0x69, 0x67, 0x69, 0x6E,
			0x3A, 0x20 };

	public static final byte[] LOCATION = new byte[] { 0x0D, 0x0A, 0x57, 0x65,
			0x62, 0x53, 0x6F, 0x63, 0x6B, 0x65, 0x74, 0x2D, 0x4C, 0x6F, 0x63,
			0x61, 0x74, 0x69, 0x6F, 0x6E, 0x3A, 0x20 };

	public static final byte[] PROTOCOL = new byte[] { 0x0D, 0x0A, 0x57, 0x65,
			0x62, 0x53, 0x6F, 0x63, 0x6B, 0x65, 0x74, 0x2D, 0x50, 0x72, 0x6F,
			0x74, 0x6F, 0x63, 0x6F, 0x6C, 0x3A, 0x20 };

	public static final byte[] HANDSHAKE_TERMINATOR = new byte[] { 0x0D, 0x0A,
			0x0D, 0x0A };

	/**
	 * Return {@code true} if the buffer contains the data frame from web socket
	 * client. {@code false} otherwise.
	 * 
	 * @param in
	 *            the io buffer
	 * @param session
	 *            the session object
	 * @return true or false
	 * TODO: not sure if this is the good place for this method.
	 */
	public static boolean isDataFrame(IoBuffer in, IoSession session) {
		int last = in.remaining() - 1;
		byte startFrame = in.get(0);
		byte endFrame = in.get(last);

		// if the data frame doesn't start with the byte 0x00 or end with 0xFF
		// byte and if the web socket connection is opened then close the session.
		if (WebSocketServerHandler.getWebSocket().isOpen()
				&& (startFrame != WebSocketProtocol.DATA_FRAME_START || endFrame != WebSocketProtocol.DATA_FRAME_END)) {
			session.close();
		}
		// check if the input contains data frame, return true.
		if (startFrame == WebSocketProtocol.DATA_FRAME_START
				&& endFrame == WebSocketProtocol.DATA_FRAME_END) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * static method that returns the string data of the byte This method
	 * decodes the data frame from the client, It is based on the algorithms
	 * defined by {@linkplain http
	 * ://tools.ietf.org/html/draft-hixie-thewebsocketprotocol-68#section-5.3}
	 * 
	 * @param byteData
	 *            the byte to convert to string
	 * @return the string data
	 * TODO: not sure if this is the good place for this method
	 */
	public static String getStringData(byte[] byteData) {
		int last = byteData.length - 1;
		CharBuffer buffer = CharBuffer.allocate(byteData.length);
		for (int i = 1; i < last; i++) {
			char ch = (char) byteData[i];
			buffer.append(ch);
		}
		String data = String.valueOf(buffer.array());
		return data;
	}
}
