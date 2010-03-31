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
package org.jwebsocket.config.xml;

import java.util.HashMap;
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
public class PluginConfigHandler implements ConfigHandler {

	private static final String ELEMENT_PLUGIN = "plugin";
	private static final String ID = "id";
	private static final String NAME = "name";
	private static final String JAR = "jar";
	private static final String NAMESPACE = "ns";
	private static final String SETTINGS = "settings";
	private static final String SETTING = "setting";

	@Override
	public Config processConfig(XMLStreamReader streamReader)
			throws XMLStreamException {
		String id = "", name = "", jar = "", namespace = "";
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
				} else {
					// ignore
				}
			}
			if (streamReader.isEndElement()) {
				String elementName = streamReader.getLocalName();
				if (elementName.equals(ELEMENT_PLUGIN)) {
					break;
				}
			}
		}
		
		return new PluginConfig(id, name, jar, namespace, settings);
	}

	/**
	 * Read the list of domains
	 * 
	 * @param streamReader
	 *            the stream reader object
	 * @return the list of domains for the engine
	 * @throws XMLStreamException
	 *             in case of stream exception
	 */
	private Map<String, String> getSettings(XMLStreamReader streamReader)
			throws XMLStreamException {
		Map<String, String> settings = new HashMap<String, String>();
		while (streamReader.hasNext()) {
		}
		return null;
	}

}
