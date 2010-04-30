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

import static org.jwebsocket.config.JWebSocketConstants.JWEBSOCKET_HOME;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketInitializer;
import org.jwebsocket.api.WebSocketPlugIn;
import org.jwebsocket.api.WebSocketServer;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.config.xml.EngineConfig;
import org.jwebsocket.kit.WebSocketRuntimeException;

/**
 * Intialize the engine, servers and plugins based on jWebSocket.xml configuration
 * @author puran
 * @version $Id$
 * 
 */
public final class JWebSocketXmlConfigInitializer implements WebSocketInitializer {

	private final JWebSocketJarClassLoader classLoader = new JWebSocketJarClassLoader();

	private JWebSocketConfig config;

	/**
	 * private constructor
	 */
	private JWebSocketXmlConfigInitializer(JWebSocketConfig theConfig) {
		this.config = theConfig;
	}

	/**
	 * Returns the initializer object
	 * 
	 * @param config
	 *            the jWebSocket config
	 * @return the initializer object
	 */
	public static JWebSocketXmlConfigInitializer getInitializer(
			JWebSocketConfig config) {
		return new JWebSocketXmlConfigInitializer(config);
	}

	@Override
	public Map<String, List<WebSocketPlugIn>> initializePlugins() {
		return null;
	}

	@Override
	public List<WebSocketServer> initializeServers() {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public WebSocketEngine intializeEngine() {
		WebSocketEngine newEngine = null;
		EngineConfig engine = config.getEngines().get(0);
		try {
			String jarFilePath = getLibraryFolderPath(engine.getJar());
			classLoader.addFile(jarFilePath);
			Class<WebSocketEngine> engineClass = (Class<WebSocketEngine>) classLoader
					.loadClass(engine.getName());
			Constructor<WebSocketEngine> ctor = engineClass
					.getDeclaredConstructor(String.class, Integer.class,
							Integer.class);
			ctor.setAccessible(true);
			newEngine = ctor.newInstance(new Object[] { engine.getId(),
					engine.getPort(), engine.getTimeout() });
		} catch (MalformedURLException e) {
			throw new WebSocketRuntimeException(
					"Couldn't Load the Jar file for engine, Make sure jar file exists or name is correct",
					e);
		} catch (ClassNotFoundException e) {
			throw new WebSocketRuntimeException("Engine class not found", e);
		} catch (InstantiationException e) {
			throw new WebSocketRuntimeException(
					"Engine class could not be instantiated", e);
		} catch (IllegalAccessException e) {
			throw new WebSocketRuntimeException(
					"Illegal Access Exception while intializing engine", e);
		} catch (NoSuchMethodException e) {
			throw new WebSocketRuntimeException(
					"No Constructor found with given 3 arguments", e);
		} catch (InvocationTargetException e) {
			throw new WebSocketRuntimeException(
					"Exception invoking engine object", e);
		}

		return newEngine;
	}

	/**
	 * private method that checks the path of the jWebSocket.xml file
	 * 
	 * @return the path to jWebSocket.xml
	 */
	private String getLibraryFolderPath(String fileName) {
		// try to obtain JWEBSOCKET_HOME environment variable
		String lWebSocketHome = System.getenv(JWEBSOCKET_HOME);
		String lFileSep = System.getProperty("file.separator");
		String lWebSocketXML = "";
		if (lWebSocketHome != null) {
			// append trailing slash if needed
			if (!lWebSocketHome.endsWith(lFileSep)) {
				lWebSocketHome += lFileSep;
			}
			// jars has to be located in %JWEBSOCKET_HOME%/libs (or some other
			// folder defined in config file)
			lWebSocketXML = lWebSocketHome + "libs"
					+ lFileSep + fileName;
		} else {
			throw new WebSocketRuntimeException(
					"JWEBSOCKET_HOME variable not set");
		}
		return lWebSocketXML;
	}

}
