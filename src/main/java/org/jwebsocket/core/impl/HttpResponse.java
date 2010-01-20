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
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jwebsocket.core.api.Headers;
import org.jwebsocket.core.api.Response;
import org.jwebsocket.core.server.StartWebSocketServer;


/**
 * HTTP based implementation of the response interface {@code Response}
 * 
 * @author <a href="http://blog.purans.net"> Puran Singh</a> &lt;<a
 *         href="mailto:puran@programmer.net"> puran@programmer.net </a>>
 * @version $Id$
 * 
 */
public class HttpResponse implements Response {
	/** HTTP response codes */
	public static final int HTTP_STATUS_SUCCESS = 200;

	public static final int HTTP_STATUS_NOT_FOUND = 404;
	
	public static final int HTTP_WEBSOCKET_HANDSHAKE_RESPONSE = 600;

	/** HTTP headers */
	private Headers headers = new Headers();

	/** HTTP header map */
	private Map<String, List<String>> headerMap = new HashMap<String, List<String>>();

	/** Storage for body of HTTP response. */
	private final ByteArrayOutputStream body = new ByteArrayOutputStream(1024);

	private int responseCode = HTTP_STATUS_SUCCESS;

	private boolean webSocketHandShakeResponse = false;

	/**
	 * static factory method to build the http response object for given headers
	 * @return the http response object
	 */
	public static HttpResponse buildHttpResponse() {
		return new HttpResponse(false);
	}

	/**
	 * static factory method to construct the web socket handshake response
	 * @return the http response object for handshake
	 */
	public static HttpResponse buildWebSocketHandshakeResponse() {
		return new HttpResponse(true);
	}

	/**
	 * private constructor that creates the http response object. Note this method returns
	 * a {@code HttpResponse} with empty headers since for hand shake operation headers are
	 * written as bytes by the handler.
	 * 
	 * @param webSocketResponse the http response for web socket hand shake 
	 */
	private HttpResponse(boolean webSocketResponse) {
		if (webSocketResponse) {
			this.webSocketHandShakeResponse = true;
			this.responseCode = HTTP_WEBSOCKET_HANDSHAKE_RESPONSE;
		} else {
			headerMap.put("Server", Arrays.asList(new String[]{"HttpServer ("
					+ StartWebSocketServer.VERSION_STRING + ')'}));
			headerMap.put("Cache-Control", Arrays.asList(new String[]{"private"}));
			headerMap.put("Content-Type", Arrays.asList(new String[]{"text/html; charset=iso-8859-1"}));
			headerMap.put("Connection", Arrays.asList(new String[]{"keep-alive"}));
			headerMap.put("Keep-Alive", Arrays.asList(new String[]{"200"}));
			headerMap.put("Date", Arrays.asList(new String[]{new SimpleDateFormat(
			"EEE, dd MMM yyyy HH:mm:ss zzz").format(new Date())}));
			headerMap.put("Last-Modified", Arrays.asList(new String[]{new SimpleDateFormat(
			"EEE, dd MMM yyyy HH:mm:ss zzz").format(new Date())}));
			
			this.headers = new Headers(headerMap);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isWebSocketHandShakeResponse() {
		return webSocketHandShakeResponse;
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

	@Override
	public String getBody(String encoding) {
		try {
			return body.toString("UTF-8");
		} catch (UnsupportedEncodingException e) {
			//in case of exception return default
			return body.toString();
		}
	}

}
