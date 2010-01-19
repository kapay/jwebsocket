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

import org.apache.mina.common.IoBuffer;

/**
 * @author <a href="http://blog.purans.net">
 *         Puran Singh</a>
 *         &lt;<a href="mailto:puran@programmer.net">
 *          puran@programmer.net
 *         </a>>
 * @version $Id$
 *
 */
public interface RequestDecoder<T> {
	
	/**
	 * Decode the {@code Request}  
	 * @param in the io buffer 
	 * @return the decoded request 
	 */
	T decode(IoBuffer in);
	
	/**
	 * This method checks if the request is ready to decode and returns 
	 * {@code true} if it is ready {@code false} otherwise.
	 * 
	 * @param in the io buffer with request data
	 * @return true or false if the request is ready to decode or not
	 * @throws Exception if exception
	 */
	boolean messageComplete(IoBuffer in) throws Exception;
}
