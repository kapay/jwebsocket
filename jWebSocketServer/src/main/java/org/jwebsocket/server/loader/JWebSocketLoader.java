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
package org.jwebsocket.server.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.log4j.Logger;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.config.xml.JWebSocketConfigHandler;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.kit.WebSocketRuntimeException;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.security.SecurityFactory;
import static org.jwebsocket.config.JWebSocketConstants.JWEBSOCKET_HOME;

/**
 * An object that does the process of loading configuration, intialization of
 * the jWebSocket server system.
 * 
 * @author puran
 * @version $Id: JWebSocketLoader.java 345 2010-04-10 20:03:48Z fivefeetfurther$
 */
public final class JWebSocketLoader {

	private static Logger log = Logging.getLogger(JWebSocketLoader.class);

	private JWebSocketConfigHandler configHandler = new JWebSocketConfigHandler();

	/**
	 * Load all the configurations based on jWebSocket.xml file at the given
	 * <tt>configFilePath</tt> location.
	 * 
	 * @param configFilePath the path to jWebSocket.xml file
	 * @return the web socket config object with all the configuration
	 * @throws WebSocketException
	 *             if there's any while loading configuration
	 */
	public JWebSocketConfig loadConfiguration(final String configFilePath)
			throws WebSocketException {
		JWebSocketConfig config = null;
		File lFile = new File(configFilePath);
		String lMsg;
		try {
			FileInputStream fis = new FileInputStream(lFile);
			XMLInputFactory factory = XMLInputFactory.newInstance();
			XMLStreamReader streamReader = null;
			streamReader = factory.createXMLStreamReader(fis);
			config = configHandler.processConfig(streamReader);
		} catch (XMLStreamException ex) {
			lMsg = "Exception occurred while creating XML stream";
			if (log.isDebugEnabled()) {
				log.debug(lMsg);
			}
			throw new WebSocketException(lMsg);
		} catch (FileNotFoundException ex) {
			lMsg = "jWebSocket config not found while creating XML stream";
			if (log.isDebugEnabled()) {
				log.debug(lMsg);
			}
			throw new WebSocketException(lMsg);
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
		String configPath = getConfigurationPath();
		//load configuration
		JWebSocketConfig config = loadConfiguration(configPath);
		//initialize security
		SecurityFactory.initFromConfig(config);
		
		return true;
	}

	/**
	 * private method that checks the path of the jWebSocket.xml file
	 * @return the path to jWebSocket.xml
	 */
	private String getConfigurationPath() {
		// try to obtain JWEBSOCKET_HOME environment variable
		String lWebSocketHome = System.getenv(JWEBSOCKET_HOME);
		String lFileSep = System.getProperty("file.separator");
		String lWebSocketXML = "";
		if (lWebSocketHome != null) {
			// append trailing slash if needed
			if (!lWebSocketHome.endsWith(lFileSep)) {
				lWebSocketHome += lFileSep;
			}
			// jWebSocket.xml has to be located in %JWEBSOCKET_HOME%/conf
			lWebSocketXML = lWebSocketHome + "conf" + lFileSep
					+ "jWebSocket.xml";
		} else {
			throw new WebSocketRuntimeException(
					"JWEBSOCKET_HOME variable not set");
		}
		return lWebSocketXML;
	}
}
