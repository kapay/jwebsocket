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
package org.jwebsocket.core.server;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.XMLEvent;
import javax.xml.ws.WebServiceException;

import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.jwebsocket.api.WebSocketException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.core.io.Resource;
import org.xml.sax.SAXException;


/**
 * This is the entry point for the "websocket" server.
 * 
 * @author <a href="http://blog.purans.net"> Puran Singh</a> &lt;<a
 *         href="mailto:puran@programmer.net"> puran@programmer.net </a>>
 * @version $Id$
 * 
 */
public final class StartWebSocketServer {

	/** Tile server revision number */
	public static final String VERSION_STRING = "";

	/** Default HTTP port */
	public static final int DEFAULT_PORT = 8080;

	/** server configuration properties */
	public static final String HOST_NAME = "websocket.configurations.hostname";
	public static final String PORT = "websocket.configuration.port";
	public static final String DOCUMENT_ROOT = "websocket.configuration.docroot";

	/** handler properties */
	public static String HANDLER_NAME = "websocket.configurations.handlername";
	
	private static final String CONFIGURATIONS = "configurations";
	private static final String HANDLERS = "handlers";
	
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	/** initialize the configuration */
	private static Configuration configuration = new StartWebSocketServer.Configuration();

	/** initialize the handlers */
	private static Handlers handlers = new StartWebSocketServer.Handlers();
	
	private ConfigHandler configHandler = new ConfigHandler();

	/**
	 * 
	 * @param filePath
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	private void loadConfiguration(String filePath) throws WebSocketException {
		FileSystemXmlApplicationContext ctx = new FileSystemXmlApplicationContext();
		Resource resource = ctx.getResource(filePath);
		XMLEventReader eventReader = null;
		try {
			XMLInputFactory factory = XMLInputFactory.newInstance();
			eventReader = factory.createXMLEventReader(resource
					.getInputStream());
			while (eventReader.hasNext()) {
				XMLEvent xmlEvent = eventReader.nextEvent();
				if (xmlEvent.isStartElement()) {
					if (xmlEvent.asStartElement().getName().getLocalPart()
							.equals(CONFIGURATIONS)) {
						
						Map<String, String> configurations = configHandler.parseConfigurations(
								eventReader, xmlEvent);

						if (configurations.size() != 3) {
							throw new WebSocketException(
									"Missing required configurations in the config file websocket.xml");
						}
						configuration = new StartWebSocketServer.Configuration(
								configurations);

					} else if (xmlEvent.asStartElement().getName()
							.getLocalPart().equals(HANDLERS)) {
						Map<String, Properties> properties = configHandler.parseHandlers(
								eventReader, xmlEvent);
						if (properties.isEmpty()) {
							throw new WebSocketException(
									"Handler definition is required in websocket.xml");
						}
						handlers = new StartWebSocketServer.Handlers(properties);
					}
				}
			}
			eventReader.close();
		} catch (Exception es) {
			throw new WebSocketException("ERROR Loading configuration", es);
		}
	}
	
	/**
	 * private method to start the server
	 * 
	 * @param filePath
	 *            the configuration file path
	 */
	private void start(String filePath) {
		try {
			LOGGER.info("Loading Server Configuration...");
			//load the configuration from the config file
			loadConfiguration(filePath);
			
			//TODO: need to implement this
			LOGGER.info("Initializing...");
			initialize();
			
			// Create an acceptor
			NioSocketAcceptor acceptor = new NioSocketAcceptor();
			
			//disable the connection of the clients on unbind
			acceptor.setCloseOnDeactivation(false);
			
			//Allow the port to be reused even if the socket is in TIME_WAIT state
			acceptor.setReuseAddress(true);
			
			//No Nagle's algorithm
			acceptor.getSessionConfig().setTcpNoDelay(true);

			// Create a service configuration
			acceptor.getFilterChain()
					.addLast(
							"protocolFilter",
							new ProtocolCodecFilter(
									new WebSocketProtocolCodecFactory()));
			acceptor.getFilterChain().addLast("logger", new LoggingFilter());
			
			String applicationHandler = handlers.getActiveHandler();
			String documentRoot = configuration.getConfiguration(DOCUMENT_ROOT);
			
			acceptor.setHandler(new WebSocketServerHandler(
					applicationHandler, documentRoot));
			String portValue = configuration.getConfiguration(PORT);
			int port = 8080;
			if (portValue != null && portValue.length() > 0) {
				port = Integer.parseInt(portValue);
			}
			acceptor.bind(new InetSocketAddress(port));
			LOGGER.info("Server started and listening on port " + port);
		} catch (Exception ex) {
			throw new WebServiceException(
					"Exception occurred while starting up the WebSocket server",
					ex);
		}
	}

	/**
	 * perform initalization
	 */
	private void initialize() {
		//TODO: implement this
	}

	/**
	 * The main method that starts the web socket server. Each web socket server
	 * instance runs a single web socket application.
	 * 
	 * <p>
	 * The command line argument includes the name of the Handler file, handler
	 * file is a java class which implements {@code WebSocketHandler} interface
	 * to respond to the web socket server events and a port number to run the
	 * web socket server.
	 * </p>
	 * Usage: java StartWebSocketServer -config path-to-config-file eg: java
	 * StartWebSocketServer -config /websocket/example/conf/config.xml
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Usage: -config config-file");
			System.exit(0);
		}
		String filePath = "";
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-config")) {
				filePath = args[i + 1];
				break;
			} else {
				System.out.println("Usage: -config config-file");
				System.exit(0);
			}
		}
		new StartWebSocketServer().start(filePath);
	}



	/**
	 * Returns the server configuration
	 * 
	 * @return the configuration object
	 */
	public static Configuration getConfiguration() {
		return configuration;
	}

	/**
	 * Returns the handlers
	 * 
	 * @return the handlers object
	 */
	public static Handlers getHandlers() {
		return handlers;
	}

	/**
	 * private static class that has the handlers information
	 * 
	 * @author <a href="http://blog.purans.net"> Puran Singh</a> &lt;<a
	 *         href="mailto:puran@programmer.net"> puran@programmer.net </a>>
	 * @version $Id$
	 * 
	 */
	public static class Handlers {

		private Map<String, Properties> handlerMap = null;

		public Handlers() {
		}

		public Handlers(Map<String, Properties> handlerMap) {
			this.handlerMap = handlerMap;
		}
		
		public Properties getProperties(String handler) {
			return handlerMap.get(handler);
		}
		
		public String getActiveHandler() {
			Set<Entry<String, Properties>> entries = handlerMap.entrySet();
			for (Entry<String, Properties> entry : entries) {
				return entry.getKey();
			}
			return "";
		}
	}

	/**
	 * Server configuration
	 * 
	 * @author <a href="http://blog.purans.net"> Puran Singh</a> &lt;<a
	 *         href="mailto:puran@programmer.net"> puran@programmer.net </a>>
	 * @version $Id$
	 * 
	 */
	public static class Configuration {

		private Map<String, String> configurations = null;

		/**
		 * constructors for configuration
		 * 
		 * @param properties
		 *            the property map
		 */
		public Configuration(Map<String, String> configurations) {
			this.configurations = configurations;
		}

		/**
		 * default constructor
		 */
		public Configuration() {

		}

		/**
		 * returns the configuration value
		 * 
		 * @param key
		 *            the key
		 * @return the property value
		 */
		public String getConfiguration(String key) {
			return configurations.get(key);
		}
	}
}
