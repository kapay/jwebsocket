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
import java.util.Map;
import java.util.WeakHashMap;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.jwebsocket.config.JWebSocketConfig;

/**
 * @author puran
 * @version $Id$
 * 
 */
public class JWebSocketConfigHandler implements ConfigHandler {

	private static final String ELEMENT_ENGINES = "engines";
	private static final String ELEMENT_ENGINE = "engine";
	private static final String ELEMENT_SERVERS = "servers";
	private static final String ELEMENT_SERVER = "server";
	private static final String ELEMENT_PLUGINS = "plugins";
	private static final String ELEMENT_PLUGIN = "plugin";
	private static final String ELEMENT_RIGHTS = "rights";
	private static final String ELEMENT_RIGHT = "right";
	private static final String ELEMENT_ROLES = "roles";
	private static final String ELEMENT_ROLE = "role";
	private static final String ELEMENT_USERS = "users";
	private static final String ELEMENT_USER = "user";
	
	private static Map<String, ConfigHandler> handlerContext = new WeakHashMap<String, ConfigHandler>();
	
	static {
		handlerContext.put("engine", new EngineConfigHandler());
		handlerContext.put("user", new UserConfigHandler());
		handlerContext.put("role", new RoleConfigHandler());
	}
 
	/**
	 * 
	 * @param streamReader
	 */
	public JWebSocketConfig processConfig(XMLStreamReader streamReader) {
		JWebSocketConfig.Builder configBuilder = new JWebSocketConfig.Builder();
		try {
			while (streamReader.hasNext()) {
				streamReader.next();
				if (streamReader.isStartElement()) {
					String elementName = streamReader.getLocalName();
					if (elementName.equals(ELEMENT_ENGINES)) {
						List<EngineConfig> engines = handleEngines(streamReader);
						configBuilder = configBuilder.addEngines(engines);
					} else if (elementName.equals(ELEMENT_SERVERS)){
						List<ServerConfig> servers = handleServers(streamReader);
						configBuilder = configBuilder.addServers(servers);
					} else if (elementName.equals(ELEMENT_PLUGINS)) {
						List<PluginConfig> plugins = handlePlugins(streamReader);
						configBuilder = configBuilder.addPlugins(plugins);
					} else if (elementName.equals(ELEMENT_RIGHTS)) {
						List<RightConfig> globalRights = handleRights(streamReader);
						configBuilder = configBuilder.addGlobalRights(globalRights);
					} else if (elementName.equals(ELEMENT_ROLES)) {
						List<RoleConfig> roles = handleRoles(streamReader);
						configBuilder = configBuilder.addGlobalRoles(roles);
					} else if (elementName.equals(ELEMENT_USERS)) {
						List<UserConfig> users = handleUsers(streamReader);
						configBuilder = configBuilder.addUsers(users);
					} else {
						//ignore
					}
				} 
			}
		} catch (XMLStreamException e) {
		}
		//we are done with the handler context map, release it for garbage collection
		handlerContext = null;
		//now return the config object, this is the only one config object that should exists
		//in the system
		return configBuilder.buildConfig();
	}

	private List<UserConfig> handleUsers(XMLStreamReader streamReader) {
		return null;
	}

	private List<RoleConfig> handleRoles(XMLStreamReader streamReader) {
		return null;
	}

	private List<RightConfig> handleRights(XMLStreamReader streamReader) {
		return null;
	}

	private List<PluginConfig> handlePlugins(XMLStreamReader streamReader) {
		return null;
	}

	private List<ServerConfig> handleServers(XMLStreamReader streamReader) {
		return null;
	}

	private List<EngineConfig> handleEngines(XMLStreamReader streamReader) throws XMLStreamException {
		List<EngineConfig> engines = new ArrayList<EngineConfig>();
		while (streamReader.hasNext()) {
			streamReader.next();
			if (streamReader.isStartElement()) {
				String elementName = streamReader.getLocalName();
				if (elementName.equals(ELEMENT_ENGINE)) {
					EngineConfig engine = (EngineConfig)handlerContext.get(elementName).processConfig(streamReader);
					engines.add(engine);
					break;
				}
			}
		}
		return engines;
	}
}
