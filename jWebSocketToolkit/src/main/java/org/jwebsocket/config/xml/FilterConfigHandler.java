//	---------------------------------------------------------------------------
//	jWebSocket - Copyright (c) 2010 jwebsocket.org
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
package org.jwebsocket.config.xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.jwebsocket.config.Config;

/**
 * Config handler for reading plugins configuration
 * @author puran
 * @version $Id$
 * 
 */
public class FilterConfigHandler implements ConfigHandler {

	private static final String ELEMENT_FILTER = "filter";
	private static final String ID = "id";
	private static final String NAME = "name";
	private static final String JAR = "jar";
	private static final String NAMESPACE = "ns";
	private static final String SERVERS = "server-assignments";
	private static final String SERVER = "server-assignment";
	private static final String SETTINGS = "settings";
	private static final String SETTING = "setting";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Config processConfig(XMLStreamReader streamReader)
			throws XMLStreamException {
		String id = "", name = "", packageName = "", jar = "", namespace = "";
		List<String> servers = new ArrayList<String>();
		Map<String, String> settings = null;
		while (streamReader.hasNext()) {
			streamReader.next();
			if (streamReader.isStartElement()) {
				String elementName = streamReader.getLocalName();
				if (elementName.equals(ID)) {
					streamReader.next();
					id = streamReader.getText();
				} else if (elementName.equals(NAME)) {
					streamReader.next();
					name = streamReader.getText();
				} else if (elementName.equals(JAR)) {
					streamReader.next();
					jar = streamReader.getText();
				} else if (elementName.equals(NAMESPACE)) {
					streamReader.next();
					namespace = streamReader.getText();
				} else if (elementName.equals(SETTINGS)) {
					settings = getSettings(streamReader);
				} else if (elementName.equals(SERVERS)) {
					servers = getServers(streamReader);
				} else {
					// ignore
				}
			}
			if (streamReader.isEndElement()) {
				String elementName = streamReader.getLocalName();
				if (elementName.equals(ELEMENT_FILTER)) {
					break;
				}
			}
		}

		return new FilterConfig(id, name, packageName, jar, namespace, servers, settings);
	}

	/**
	 * private method that reads the list of servers from the plugin configuration 
	 * @param streamReader the stream reader object
	 * @return the list of right ids
	 * @throws XMLStreamException if exception while reading
	 */
	private List<String> getServers(XMLStreamReader streamReader)
			throws XMLStreamException {
		List<String> servers = new ArrayList<String>();
		while (streamReader.hasNext()) {
			streamReader.next();
			if (streamReader.isStartElement()) {
				String elementName = streamReader.getLocalName();
				if (elementName.equals(SERVER)) {
					streamReader.next();
					String server = streamReader.getText();
					servers.add(server);
				}
			}
			if (streamReader.isEndElement()) {
				String elementName = streamReader.getLocalName();
				if (elementName.equals(SERVERS)) {
					break;
				}
			}
		}
		return servers;
	}

	/**
	 * Read the list of domains
	 * 
	 * @param streamReader
	 *            the stream reader object
	 * @return the list of domains for the engine
	 * @throws XMLStreamException
	 *             in case of stream exception
	 * TODO: PURAN, FIX THIS!! YOU LAZY GUY!!!            
	 */
	private Map<String, String> getSettings(XMLStreamReader streamReader)
			throws XMLStreamException {
		//TODO: implement this
		return Collections.emptyMap();
	}

}
