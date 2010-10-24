// ---------------------------------------------------------------------------
// jWebSocket - Copyright (c) 2010 jwebsocket.org
// ---------------------------------------------------------------------------
// This program is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published by the
// Free Software Foundation; either version 3 of the License, or (at your
// option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
// more details.
// You should have received a copy of the GNU Lesser General Public License along
// with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
// ---------------------------------------------------------------------------
package org.jwebsocket.config.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.jwebsocket.config.Config;
import org.jwebsocket.config.ConfigHandler;

/**
 * Channel config handler to read channel configuration values.
 * @author puran
 * @version $Id$
 */
public class ChannelConfigHandler implements ConfigHandler {
  
  private static final String ELEMENT_CHANNEL = "channel";
  private static final String ID = "id";
  private static final String NAME = "name";
  private static final String SYSTEM = "system";
  private static final String PRIVATE = "private";
  private static final String SECRET = "secret";
  private static final String ACCESS = "access";
  private static final String OWNER = "owner";

  @Override
  public Config processConfig(XMLStreamReader streamReader) throws XMLStreamException {
    String id = "", name = "", secret = "", access = "", owner = "";
    boolean isPrivate = false, isSystem = false;
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
            } else if (elementName.equals(SYSTEM)) {
                streamReader.next();
                String value = streamReader.hasText() ? streamReader.getText() : "";
                isSystem = (!value.equals("")) ? true : false; 
            } else if (elementName.equals(PRIVATE)) {
                streamReader.next();
                String value = streamReader.hasText() ? streamReader.getText() : "";
                isPrivate = (!value.equals("")) ? true : false;
            } else if (elementName.equals(SECRET)) {
                streamReader.next();
                secret = streamReader.getText();
            } else if (elementName.equals(ACCESS)) {
              streamReader.next();
              access = streamReader.getText();
            } else if (elementName.equals(OWNER)) {
              streamReader.next();
              owner = streamReader.getText();
            } else {
                //ignore
            }
        }
        if (streamReader.isEndElement()) {
            String elementName = streamReader.getLocalName();
            if (elementName.equals(ELEMENT_CHANNEL)) {
                break;
            }
        }
    }
    return new ChannelConfig(id, name, isPrivate, isSystem, secret, access, owner);
  }

}
