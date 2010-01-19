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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Defines the request or response headers
 * 
 * @author <a href="http://blog.purans.net">
 *         Puran Singh</a>
 *         &lt;<a href="mailto:puran@programmer.net">
 *          puran@programmer.net
 *         </a>>
 * @version $Id$
 *
 */
public final class Headers {
	
	Map<String, List<String>> headers = new ConcurrentHashMap<String, List<String>>();
	
	/**
	 * default constructor with empty header map
	 */
	public Headers() {
		
	}
	/**
	 * Constructs the {@code Headers} object from the map of headers
	 * @param headers the map of key/value pairs headers
	 */
	public Headers(Map<String, List<String>> headers) {
		this.headers = headers;
	}
	
	/**
	 * Returns the header value of the given key
	 * @param key the key property of the header
	 * @return the value of the given header key
	 */
	public List<String> getHeaderValue(String key) {
		return headers.get(key);
	}
	
	/**
	 * Returns the first header value for the given header key
	 * @param key the header key 
	 * @return the first header value from the list of values
	 */
	public String getFirstHeaderValue(String key) {
		return getHeaderValue(key).get(0);
	}
	
	/**
	 * @return the header entry set
	 */
	public Set<Entry<String, List<String>>> entrySet() {
		return headers.entrySet();
	}
	
	/**
	 * check if the header property exists
	 * @param property the header property
	 * @return {@code true} if exists {@code false} otherwise.
	 */
	public boolean contains(String property) {
		return headers.containsKey(property);
	}
}
