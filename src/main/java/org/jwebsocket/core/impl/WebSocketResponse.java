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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.jwebsocket.core.api.Headers;
import org.jwebsocket.core.api.Response;


/**
 * @author <a href="http://blog.purans.net">
 *         Puran Singh</a>
 *         &lt;<a href="mailto:puran@programmer.net">
 *          puran@programmer.net
 *         </a>>
 * @version $Id$
 *
 */
public class WebSocketResponse implements Response {
	
	public static final int WEBSOCKET_RESPONSE = 100;
	
	/**  headers */
	private Headers headers = new Headers();

	/** Storage for body of HTTP response. */
	private final ByteArrayOutputStream body = new ByteArrayOutputStream(1024);

	private int responseCode = WEBSOCKET_RESPONSE;
	
	/**
	 * @return the web socket response
	 */
	public static WebSocketResponse buildWebSocketResponse() {
		return new WebSocketResponse();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getResponseCode() {
		return this.responseCode;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void appendBody(byte[] b) {
		try {
			body.write(b);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void appendBody(String s) {
		try {
			body.write(s.getBytes());
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte[] getBody() {
		return body.toByteArray();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getBodyLength() {
		return body.size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Headers getHeaders() {
		return headers;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isWebSocketHandShakeResponse() {
		return false;
	}

}
