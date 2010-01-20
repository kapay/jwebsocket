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
package org.jwebsocket.core.api;


/**
 * Interface defining a single response from "websocket" server.
 * 
 * @author <a href="http://blog.purans.net"> Puran Singh</a> &lt;<a
 *         href="mailto:puran@programmer.net"> puran@programmer.net </a>>
 * @version $Id$
 * 
 */
public interface Response {

	/**
	 * Returns the response headers for the given response
	 * @return the response headers
	 */
	Headers getHeaders();

	/**
	 * Returns the response code depending on the nature of the response
	 * See: 
	 * {@code HttpResponse.HTTP_STATUS_SUCCESS}
     * {@code HttpResponse.HTTP_STATUS_NOT_FOUND}
	 * @return the response code
	 */
	int getResponseCode();
	
	/**
	 * sets the response code 
	 * @param code the code value
	 */
	void setResponseCode(int responseCode);
	
	/**
	 * Returns {@code true} if this response is a handshake response during 
	 * initial startup for opening a web socket connection or {@code false} 
	 * for normal response.
	 * 
	 * @return true of false
	 */
	boolean isWebSocketHandShakeResponse();

	/**
	 * Append the byte data to the body of the response 
	 * @param b the byte data to append to the response
	 */
	void appendBody(byte[] b);

	/**
	 * Append string data to the body
	 * @param s the string data
	 */
	void appendBody(String s);

	/**
	 * Get the body of the response
	 * @return the body of the response as byte data 
	 */
	byte[] getBody();

	/**
	 * Returns the size of the body
	 * @return the body length
	 */
	int getBodyLength();
	
	/**
	 * Returns the string value of the response body using specified encoding
	 * @param encoding the encoding standard
	 * @return string body 
	 */
	String getBody(String encoding);
}
