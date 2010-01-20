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
package org.jwebsocket.core.server;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.List;
import java.util.Map.Entry;

import org.apache.mina.common.IoBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.filter.codec.demux.MessageEncoder;
import org.jwebsocket.core.api.Response;
import org.jwebsocket.core.impl.HttpResponse;
import org.jwebsocket.core.impl.WebSocketProtocol;
import org.jwebsocket.core.impl.WebSocketResponse;


/**
 * @author <a href="http://blog.purans.net"> Puran Singh</a> &lt;<a
 *         href="mailto:puran@programmer.net"> puran@programmer.net </a>>
 * @version $Id$
 * 
 */
public class WebSocketProtocolEncoder implements MessageEncoder<Response> {

	private static final byte[] CRLF = new byte[] { 0x0D, 0x0A };

	public WebSocketProtocolEncoder() {
	}

	public void encode(IoSession session, Response message,
			ProtocolEncoderOutput out) throws Exception {
		IoBuffer buf = IoBuffer.allocate(256);
		// Enable auto-expand for easier encoding
		buf.setAutoExpand(true);

		if (message.isWebSocketHandShakeResponse()
				&& message.getResponseCode() == HttpResponse.HTTP_WEBSOCKET_HANDSHAKE_RESPONSE) {
			buf.put(message.getBody());
		} else if (!message.isWebSocketHandShakeResponse()
				&& message.getResponseCode() == WebSocketResponse.WEBSOCKET_RESPONSE) {
			//TODO: separate this logic from this class,
			//because as per the standard the data framing specification may 
			//change in the future.
			buf.put(WebSocketProtocol.DATA_FRAME_START);
			buf.put(message.getBody("UTF-8").getBytes("UTF-8"));
			buf.put(WebSocketProtocol.DATA_FRAME_END);
		} else {
			try {
				// output all headers except the content length
				CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
				buf.putString("HTTP/1.1 ", encoder);
				buf.putString(String.valueOf(message.getResponseCode()),
						encoder);
				switch (message.getResponseCode()) {
				case HttpResponse.HTTP_STATUS_SUCCESS:
					buf.putString(" OK", encoder);
					break;
				case HttpResponse.HTTP_STATUS_NOT_FOUND:
					buf.putString(" Not Found", encoder);
					break;
				}
				buf.put(CRLF);
				for (Entry<String, List<String>> entry : message.getHeaders()
						.entrySet()) {
					String value = entry.getValue().get(0);
					buf.putString(entry.getKey(), encoder);
					buf.putString(": ", encoder);
					if (entry.getKey().equals("Content-Type")) {
						value = "text/html utf-8";
					}
					buf.putString(value, encoder);
					buf.put(CRLF);
				}
				// now the content length is the body length
				buf.putString("Content-Length: ", encoder);
				buf.putString(String.valueOf(message.getBodyLength()), encoder);
				buf.put(CRLF);
				buf.put(CRLF);
				// add body
				buf.put(IoBuffer.wrap(message.getBody()));
			} catch (CharacterCodingException ex) {
				ex.printStackTrace();
			}
		}
		buf.flip();
		out.write(buf);
	}
}
