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

import org.apache.mina.common.IoBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoderAdapter;
import org.apache.mina.filter.codec.demux.MessageDecoderResult;
import org.jwebsocket.core.api.Request;
import org.jwebsocket.core.api.RequestDecoder;
import org.jwebsocket.core.impl.HttpRequestDecoder;
import org.jwebsocket.core.impl.WebSocketProtocol;
import org.jwebsocket.core.impl.WebSocketRequestDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class decodes the web socket protocol requests.
 * 
 * @author <a href="http://blog.purans.net"> Puran Singh</a> &lt;<a
 *         href="mailto:puran@programmer.net"> puran@programmer.net </a>>
 * @version $Id$
 * 
 */
public class WebSocketProtocolDecoder extends MessageDecoderAdapter {

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	private static RequestDecoder<Request> httpRequestDecoder = HttpRequestDecoder
			.getHttpRequestDecoder();

	private static RequestDecoder<Request> webSocketRequestDecoder = WebSocketRequestDecoder
			.getWebSocketRequestDecoder();

	/**
	 * Get the appropriate request decoder based on the byte data recieved. If
	 * the data recieved is a data frame then it has to be a web socket request
	 * else it is a HTTP handshake request from the client or other HTTP requests.
	 * 
	 * @param in the io buffer
	 * @param session the session object
	 * @return the request decoder object
	 */
	private RequestDecoder<Request> getDecoder(IoBuffer in, IoSession session) {
		if (!WebSocketProtocol.isDataFrame(in, session)) {
			LOGGER.info("HTTP Request Recieved by the decoder");
			return httpRequestDecoder;
		} else {
			LOGGER.info("WebSocket Request Recieved by the decoder");
			return webSocketRequestDecoder;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MessageDecoderResult decodable(IoSession session, IoBuffer in) {
		// Return NEED_DATA if the whole header is not read yet.
		try {
			return getDecoder(in, session).messageComplete(in) ? MessageDecoderResult.OK
					: MessageDecoderResult.NEED_DATA;
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return MessageDecoderResult.NOT_OK;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MessageDecoderResult decode(IoSession session, IoBuffer in,
			ProtocolDecoderOutput out) throws Exception {
		// Try to decode body
		Request request = getDecoder(in, session).decode(in);

		// Return NEED_DATA if the body is not fully read.
		if (request == null) {
			return MessageDecoderResult.NEED_DATA;
		}

		out.write(request);

		return MessageDecoderResult.OK;
	}

}
