//	---------------------------------------------------------------------------
//	jWebSocket - Copyright (c) 2010 jwebsocket.org
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
//	more details.
//	You should have received a copy of the GNU General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/>.
//	---------------------------------------------------------------------------
package org.jwebsocket.kit;

import java.io.IOException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.log4j.Logger;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.config.xml.JWebSocketConfigHandler;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.logging.Logging;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.core.io.Resource;

/**
 * An object that does the process of loading configuration, intialization of
 * the jWebSocket server system.
 * 
 * @author puran
 * @version $Id$
 */
public final class WebSocketLoader {

	private static Logger log = Logging.getLogger(WebSocketLoader.class);
	
	private JWebSocketConfigHandler configHandler = new JWebSocketConfigHandler();

	/**
	 * Load all the configurations based on jWebSocket.xml file at the given
	 * <tt>configFilePath</tt> location.
	 * 
	 * @return the web socket config object with all the configuration
	 * @throws WebSocketException
	 *             if there's any while loading configuration
	 */
	public JWebSocketConfig loadConfiguration(final String configFilePath)
			throws WebSocketException {
		FileSystemXmlApplicationContext ctx = new FileSystemXmlApplicationContext();
		Resource resource = ctx.getResource(configFilePath);
		XMLInputFactory factory = XMLInputFactory.newInstance();
		XMLStreamReader streamReader = null;
		JWebSocketConfig config = null;
		try {
			streamReader = factory.createXMLStreamReader(resource
					.getInputStream());
			config = configHandler.processConfig(streamReader);

		} catch (XMLStreamException e) {
			if (log.isDebugEnabled()) {
				log.debug("Exception occurred while creating XML stream", e);
			}
		} catch (IOException e) {
			if (log.isDebugEnabled()) {
				log.debug("I/O Exception occurred while creating XML stream",e);
			}
		}
		return config;
	}

	

	/**
	 * Initialize the loaded components of the jWebSocket server system using
	 * the configuration
	 * 
	 * @param configuration
	 *            the jWebSocket configuration
	 * @return {@code true} if intialization was completely successful,{@code
	 *         false} otherwise.
	 * @throws WebSocketException
	 *             if exception occurs during intialization
	 */
	public boolean initialize(final JWebSocketConfig configuration)
			throws WebSocketException {
		throw new UnsupportedOperationException();
	}
}
