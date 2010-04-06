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

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.jwebsocket.config.Config;

/**
 * Handler class to read roles
 * @author puran
 * @version $Id$
 * 
 */
public class RoleConfigHandler implements ConfigHandler {

	private static final String ID = "id";
	private static final String DESCRIPTION = "description";
	private static final String ELEMENT_RIGHT = "right";
	private static final String ELEMENT_RIGHTS = "rights";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Config processConfig(XMLStreamReader streamReader)
			throws XMLStreamException {
		String id = "", description = "";
		List<String> rights = new ArrayList<String>();
		while (streamReader.hasNext()) {
			streamReader.next();
			if (streamReader.isStartElement()) {
				String elementName = streamReader.getLocalName();
				if (elementName.equals(ID)) {
					streamReader.next();
					id = streamReader.getText();
				} else if (elementName.equals(DESCRIPTION)) {
					streamReader.next();
					description = streamReader.getText();
				} else if (elementName.equals(ELEMENT_RIGHTS)) {
					rights = getRights(streamReader);
				} else {
					// ignore
				}
			}
			if (streamReader.isEndElement()) {
				String elementName = streamReader.getLocalName();
				if (elementName.equals(ELEMENT_RIGHT)) {
					break;
				}
			}
		}
		return new RoleConfig(id, description, rights);
	}

	/**
	 * private method that reads the list of rights from the role configuration 
	 * @param streamReader the stream reader object
	 * @return the list of right ids
	 * @throws XMLStreamException if exception while reading
	 */
	private List<String> getRights(XMLStreamReader streamReader)
			throws XMLStreamException {
		return null;
	}

}
