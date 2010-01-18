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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.common.IoBuffer;

import com.jwebsocket.core.api.Headers;
import com.jwebsocket.core.api.Request;
import com.jwebsocket.core.api.RequestDecoder;

/**
 * @author <a href="http://blog.purans.net"> Puran Singh</a> &lt;<a
 *         href="mailto:puran@programmer.net"> puran@programmer.net </a>>
 * @version $Id$
 * 
 */
public class HttpRequestDecoder implements RequestDecoder<Request> {
	
	private static final byte[] CONTENT_LENGTH = new String("Content-Length:")
	.getBytes();

	private final CharsetDecoder decoder = Charset.defaultCharset()
			.newDecoder();
	
	/**
	 * static factory method to construct http request decoder object
	 * @return the http request decoder object
	 */
	public static HttpRequestDecoder getHttpRequestDecoder() {
		return new HttpRequestDecoder();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Request decode(IoBuffer in) {
		HttpRequest request = null;
		try {
			Map<String, List<String>> headerMap = parseRequest(new StringReader(
					in.getString(decoder)));
			Headers headers = new Headers(headerMap);
			request = new HttpRequest(headers, null);
			
			return request;
		} catch (CharacterCodingException ex) {
			ex.printStackTrace();
		}

		return request;
	}

	/**
	 * private method to parse the request 
	 * @param is the input stream reader
	 * @return the map of key/value pair of the request
	 */
	private Map<String, List<String>> parseRequest(Reader is) {
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		BufferedReader rdr = new BufferedReader(is);

		try {
			// Get request URL.
			String line = rdr.readLine();
			String[] url = line.split(" ");
			if (url.length < 3) {
				return map;
			}

			map.put("URI", Arrays.asList(new String[] { line }));
			map.put("Method", Arrays.asList(new String[] { url[0].toUpperCase() }));
			map.put("Context", Arrays.asList(new String[] { url[1].substring(1) }));
			map.put("Protocol", Arrays.asList(new String[] { url[2] }));
			// Read header
			while ((line = rdr.readLine()) != null && line.length() > 0) {
				String[] tokens = line.split(": ");
				map.put(tokens[0], Arrays.asList(new String[] { tokens[1] }));
			}

			// If method 'POST' then read Content-Length worth of data
			if (url[0].equalsIgnoreCase("POST")) {
				int len = Integer.parseInt(map.get("Content-Length").get(0));
				char[] buf = new char[len];
				if (rdr.read(buf) == len) {
					line = String.copyValueOf(buf);
				}
			} else if (url[0].equalsIgnoreCase("GET")) {
				int idx = url[1].indexOf('?');
				if (idx != -1) {
					map.put("Context",
							Arrays.asList(new String[] { url[1].substring(1, idx) }));
					line = url[1].substring(idx + 1);
				} else {
					line = null;
				}
			}
			if (line != null) {
				String[] match = line.split("\\&");
				for (String element : match) {
					List<String> params = new ArrayList<String>();
					String[] tokens = element.split("=");
					switch (tokens.length) {
					case 0:
						map.put("@".concat(element), Arrays.asList(new String[] {}));
						break;
					case 1:
						map.put("@".concat(tokens[0]), Arrays.asList(new String[] {}));
						break;
					default:
						String name = "@".concat(tokens[0]);
						if (map.containsKey(name)) {
							params = map.get(name);
							List<String> tmp = new ArrayList<String>(params.size() + 1);
							for (int j = 0; j < params.size(); j++) {
								tmp.add(j, params.get(j));
							}
							params = null;
							params = tmp;
						}
						String value = tokens[1].trim();
						params.add(params.size() - 1, value);
						map.put(name, params);
					}
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return map;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean messageComplete(IoBuffer in) throws Exception {
		int last = in.remaining() - 1;
		if (in.remaining() < 4) {
			return false;
		}
		// to speed up things we check if the Http request is a GET or POST
		if (in.get(0) == (byte) 'G' && in.get(1) == (byte) 'E'
				&& in.get(2) == (byte) 'T') {
			// Http GET request therefore the last 4 bytes should be 0x0D 0x0A
			// 0x0D 0x0A
			return in.get(last) == (byte) 0x0A
					&& in.get(last - 1) == (byte) 0x0D
					&& in.get(last - 2) == (byte) 0x0A
					&& in.get(last - 3) == (byte) 0x0D;
		} else if (in.get(0) == (byte) 'P' && in.get(1) == (byte) 'O'
				&& in.get(2) == (byte) 'S' && in.get(3) == (byte) 'T') {
			// Http POST request
			// first the position of the 0x0D 0x0A 0x0D 0x0A bytes
			int eoh = -1;
			for (int i = last; i > 2; i--) {
				if (in.get(i) == (byte) 0x0A && in.get(i - 1) == (byte) 0x0D
						&& in.get(i - 2) == (byte) 0x0A
						&& in.get(i - 3) == (byte) 0x0D) {
					eoh = i + 1;
					break;
				}
			}
			if (eoh == -1) {
				return false;
			}
			for (int i = 0; i < last; i++) {
				boolean found = false;
				for (int j = 0; j < CONTENT_LENGTH.length; j++) {
					if (in.get(i + j) != CONTENT_LENGTH[j]) {
						found = false;
						break;
					}
					found = true;
				}
				if (found) {
					// retrieve value from this position till next 0x0D 0x0A
					StringBuilder contentLength = new StringBuilder();
					for (int j = i + CONTENT_LENGTH.length; j < last; j++) {
						if (in.get(j) == 0x0D) {
							break;
						}
						contentLength.append(new String(
								new byte[] { in.get(j) }));
					}
					// if content-length worth of data has been received then
					// the message is complete
					return Integer.parseInt(contentLength.toString().trim())
							+ eoh == in.remaining();
				}
			}
		}

		// the message is not complete and we need more data
		return false;
	}
}
 