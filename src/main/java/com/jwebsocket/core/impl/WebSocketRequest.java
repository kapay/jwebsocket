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
package com.jwebsocket.core.impl;

import com.jwebsocket.core.api.Headers;
import com.jwebsocket.core.api.Request;

/**
 * Implementation of {@code Request} for web socket request message.
 * 
 * @author <a href="http://blog.purans.net"> Puran Singh</a> &lt;<a
 *         href="mailto:puran@programmer.net"> puran@programmer.net </a>>
 * @version $Id$
 * TODO: headers is not used right now for web socket request and response
 * is it the bad design.
 */
public class WebSocketRequest implements Request {

	private Headers headers = new Headers();

	private byte[] body = new byte[1024];

	/**
	 * @param headers the web socket request headers
	 * @param body the body data
	 */
	public WebSocketRequest(Headers headers, byte[] body) {
		this.headers = headers;
		this.body = body;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte[] getBody() {
		return body;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Headers getHeaders() {
		return headers;
	}

}
