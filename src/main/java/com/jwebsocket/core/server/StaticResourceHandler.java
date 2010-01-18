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
package com.jwebsocket.core.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.core.io.Resource;

import com.jwebsocket.core.api.ResourceHandler;
import com.jwebsocket.core.api.Response;
import com.jwebsocket.core.impl.HttpResponse;

/**
 * @author <a href="http://blog.purans.net">
 *         Puran Singh</a>
 *         &lt;<a href="mailto:puran@programmer.net">
 *          puran@programmer.net
 *         </a>>
 * @version $Id$
 *
 */
public class StaticResourceHandler implements ResourceHandler {
	
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	private String docRoot = null;
	
	/**
	 * constructor 
	 * @param docRoot the path for document root where WebSocket server looks for 
	 * resource files.
	 */
	public StaticResourceHandler(String docRoot) {
		this.docRoot = docRoot;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Response getResourceResponse(String resourceName) {
		Response response = HttpResponse.buildHttpResponse();
		response.setResponseCode(HttpResponse.HTTP_STATUS_SUCCESS);
		FileSystemXmlApplicationContext ctx = new FileSystemXmlApplicationContext();
		
		BufferedReader fileReader = null;
		try {
			String path = "";
			if (docRoot != null) {
				path = docRoot + resourceName + ".html";
			} else {
				path = resourceName + ".html";
			}
			Resource resource = ctx.getResource(path);
			fileReader = new BufferedReader(new InputStreamReader(
					resource.getInputStream()));
			String line = "";
			while ((line = fileReader.readLine()) != null) {
				response.appendBody(line.getBytes("UTF-8"));
				response.appendBody("\r\n".getBytes("UTF-8"));
			}
			fileReader.close();
		} catch (IOException e) {
			LOGGER.error("IO Exception while writing handshake response", e);
		}
		return response;
	}
}
