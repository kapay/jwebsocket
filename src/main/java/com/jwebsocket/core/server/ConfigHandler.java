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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.XMLEvent;

import com.jwebsocket.api.WebSocketException;

/**
 * This class reads the configurations from the configuration file websocket.xml
 * 
 * @author <a href="http://blog.purans.net"> Puran Singh</a> &lt;<a
 *         href="mailto:puran@programmer.net"> puran@programmer.net </a>>
 * @version $Id$
 * 
 */
public class ConfigHandler {
	private static final String HOST_STRING = "host";
	private static final String PORT_STRING = "port";
	private static final String DOC_ROOT_STRING = "documentRoot";

	private static final String CONFIGURATIONS = "configurations";
	private static final String HANDLERS = "handlers";

	private static final String HANDLER = "handler";
	private static final String HANDLER_NAME_STRING = "handlerName";
	private static final String PROPERTIES = "properties";
	private static final String PROPERTY = "property";
	private static final String KEY = "key";
	private static final String VALUE = "value";

	/**
	 * parses the handler information from the configuration file.
	 * @param eventReader the event reader object
	 * @param event the xml event object
	 * @return the map of handler name to its properties
	 * @throws Exception if ther's any exception
	 */
	public Map<String, Properties> parseHandlers(XMLEventReader eventReader,
			XMLEvent event) throws Exception {
		Map<String, Properties> handlers = new HashMap<String, Properties>();
		while (eventReader.hasNext()) {
			XMLEvent handlersEvent = eventReader.nextEvent();
			XMLEvent peek = eventReader.peek();
			if (peek.isEndElement()) {
				if (peek.asEndElement().getName().getLocalPart().equals(
						HANDLERS)) {
					break;
				}
			}

			if (handlersEvent.isStartElement()) {
				if (handlersEvent.asStartElement().getName().getLocalPart()
						.equals(HANDLER)) {
					XMLEvent handlerEvent = eventReader.nextEvent();

					String handlerName = getHandlerName(eventReader,
							eventReader.nextEvent());
					Properties properties = getHandlerProperties(eventReader,
							handlerEvent);

					handlers.put(handlerName, properties);
					continue;
				}
			}
		}
		return handlers;
	}

	/**
	 * read the properties for each handler
	 * @param eventReader the event reader object
	 * @param handlerEvent the xml handler event
	 * @return the properties of the handler
	 * @throws Exception in case of exception
	 */
	private Properties getHandlerProperties(XMLEventReader eventReader,
			XMLEvent handlerEvent) throws Exception {
		final Properties properties = new Properties();
		while (eventReader.hasNext()) {
			XMLEvent propsEvent = eventReader.nextEvent();
			XMLEvent peek = eventReader.peek();
			if (peek.isEndElement()) {
				if (peek.asEndElement().getName().getLocalPart().equals(
						PROPERTIES)) {
					break;
				}
			}

			if (propsEvent.isStartElement()) {
				if (propsEvent.asStartElement().getName().getLocalPart()
						.equals(PROPERTY)) {
					XMLEvent propertyEvent = eventReader.nextEvent();
					String[] props = getProperty(eventReader, propertyEvent);
					properties.put(props[0], props[1]);
				}
			}
		}
		return properties;
	}

	/**
	 * Get the single property 
	 * @param eventReader the event reader object
	 * @param propertyEvent the xml event object
	 * @return the key/value pair
	 * @throws Exception if exception
	 */
	private String[] getProperty(XMLEventReader eventReader,
			XMLEvent propertyEvent) throws Exception {
		String key = "";
		String value = "";
		while (eventReader.hasNext()) {
			XMLEvent propEvent = eventReader.nextEvent();
			XMLEvent peek = eventReader.peek();
			if (peek.isEndElement()) {
				if (peek.asEndElement().getName().getLocalPart().equals(
						PROPERTY)) {
					break;
				}
			}
			if (propEvent.isStartElement()) {
				if (propEvent.asStartElement().getName().getLocalPart().equals(
						KEY)) {
					XMLEvent keyEvent = eventReader.nextEvent();
					key = keyEvent.asCharacters().getData();
				}

				if (propEvent.asStartElement().getName().getLocalPart().equals(
						VALUE)) {
					XMLEvent valueEvent = eventReader.nextEvent();
					value = valueEvent.asCharacters().getData();
				}
			}
		}
		if (key.isEmpty() || value.isEmpty()) {
			throw new WebSocketException(
					"Key/Value pair cannot be null in property definition");
		}
		return new String[] { key, value };
	}

	/**
	 * private method to parse handler name
	 * @param eventReader the event reader object
	 * @param xmlEvent the xml event object
	 * @return the handler name 
	 * @throws Exception if exception 
	 */
	private String getHandlerName(XMLEventReader eventReader, XMLEvent xmlEvent)
			throws Exception {
		if (xmlEvent.isStartElement()) {
			if (xmlEvent.asStartElement().getName().getLocalPart().equals(
					HANDLER_NAME_STRING)) {
				XMLEvent nameEvent = eventReader.nextEvent();
				return nameEvent.asCharacters().getData();
			}
		}
		return "";
	}

	/**
	 * parses the configuration values from the websocket.xml file
	 * @param eventReader the event reader object
	 * @param event the xml event object
	 * @return the map of configuration key/value pair
	 * @throws Exception if exception
	 */
	public Map<String, String> parseConfigurations(XMLEventReader eventReader,
			XMLEvent event) throws Exception {
		Map<String, String> configurations = new HashMap<String, String>();
		while (eventReader.hasNext()) {
			XMLEvent confEvent = eventReader.nextEvent();
			XMLEvent peek = eventReader.peek();
			if (peek.isEndElement()) {
				if (peek.asEndElement().getName().getLocalPart().equals(
						CONFIGURATIONS)) {
					break;
				}
			}
			if (confEvent.isStartElement()) {
				if (confEvent.asStartElement().getName().getLocalPart().equals(
						HOST_STRING)) {
					XMLEvent hostEvent = eventReader.nextEvent();
					configurations.put(StartWebSocketServer.HOST_NAME,
							hostEvent.asCharacters().getData());
					continue;
				}
				if (confEvent.asStartElement().getName().getLocalPart().equals(
						PORT_STRING)) {
					XMLEvent portEvent = eventReader.nextEvent();
					configurations.put(StartWebSocketServer.PORT, portEvent
							.asCharacters().getData());
					continue;
				}

				if (confEvent.asStartElement().getName().getLocalPart().equals(
						DOC_ROOT_STRING)) {
					XMLEvent docRootEvent = eventReader.nextEvent();
					configurations.put(StartWebSocketServer.DOCUMENT_ROOT,
							docRootEvent.asCharacters().getData());
					continue;
				}

			}
		}
		return configurations;
	}
}
