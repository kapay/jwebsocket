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

import org.jwebsocket.config.Config;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.List;
import javolution.util.FastList;
import org.jwebsocket.config.ConfigHandler;
import org.jwebsocket.util.Tools;

/**
 * Handles the engine configuration
 *
 * @author puran
 * @version $Id: EngineConfigHandler.java 624 2010-07-06 12:28:44Z fivefeetfurther $
 */
public class EngineConfigHandler implements ConfigHandler {

	private static final String ELEMENT_ENGINE = "engine";
	private static final String ID = "id";
	private static final String NAME = "name";
	private static final String JAR = "jar";
	private static final String CONTEXT = "context";
	private static final String SERVLET = "servlet";
	private static final String PORT = "port";
	private static final String SSL_PORT = "sslport";
	private static final String TIMEOUT = "timeout";
	private static final String MAXFRAMESIZE = "maxframesize";
	private static final String DOMAINS = "domains";
	private static final String DOMAIN = "domain";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Config processConfig(XMLStreamReader aStreamReader)
			throws XMLStreamException {
		String lId = "", lName = "", lJar = "", lContext = "", lServlet = "";
		int lPort = 0, lSSLPort = 0, lTimeout = 0, lFramesize = 0;
		List<String> lDomains = null;
		while (aStreamReader.hasNext()) {
			aStreamReader.next();
			if (aStreamReader.isStartElement()) {
				String lElementName = aStreamReader.getLocalName();
				if (lElementName.equals(ID)) {
					aStreamReader.next();
					lId = aStreamReader.getText();
				} else if (lElementName.equals(NAME)) {
					aStreamReader.next();
					lName = aStreamReader.getText();
				} else if (lElementName.equals(JAR)) {
					aStreamReader.next();
					lJar = aStreamReader.getText();
				} else if (lElementName.equals(CONTEXT)) {
					aStreamReader.next();
					lContext = aStreamReader.getText();
				} else if (lElementName.equals(SERVLET)) {
					aStreamReader.next();
					lServlet = aStreamReader.getText();
				} else if (lElementName.equals(PORT)) {
					aStreamReader.next();
					lPort = Tools.stringToInt(aStreamReader.getText(), -1);
				} else if (lElementName.equals(SSL_PORT)) {
					aStreamReader.next();
					lSSLPort = Tools.stringToInt(aStreamReader.getText(), -1);
				} else if (lElementName.equals(TIMEOUT)) {
					aStreamReader.next();
					lTimeout = Integer.parseInt(aStreamReader.getText());
				} else if (lElementName.equals(DOMAINS)) {
					lDomains = getDomains(aStreamReader);
				} else if (lElementName.equals(MAXFRAMESIZE)) {
					aStreamReader.next();
					lFramesize = Integer.parseInt(aStreamReader.getText());
				} else {
					//ignore
				}
			}
			if (aStreamReader.isEndElement()) {
				String lElementName = aStreamReader.getLocalName();
				if (lElementName.equals(ELEMENT_ENGINE)) {
					break;
				}
			}
		}
		return new EngineConfig(lId, lName, lJar, 
				lPort, lSSLPort,
				lContext, lServlet,
				lTimeout, lFramesize, lDomains);
	}

	/**
	 * Read the list of domains
	 *
	 * @param aStreamReader the stream reader object
	 * @return the list of domains for the engine
	 * @throws XMLStreamException in case of stream exception
	 */
	private List<String> getDomains(XMLStreamReader aStreamReader)
			throws XMLStreamException {
		List<String> lDomains = new FastList<String>();
		while (aStreamReader.hasNext()) {
			aStreamReader.next();
			if (aStreamReader.isStartElement()) {
				String lElementName = aStreamReader.getLocalName();
				if (lElementName.equals(DOMAIN)) {
					aStreamReader.next();
					lDomains.add(aStreamReader.getText());
				}
			}
			if (aStreamReader.isEndElement()) {
				String lElementName = aStreamReader.getLocalName();
				if (lElementName.equals(DOMAINS)) {
					break;
				}
			}
		}
		return lDomains;
	}
}
