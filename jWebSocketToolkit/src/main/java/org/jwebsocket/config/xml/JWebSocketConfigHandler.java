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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.log4j.Logger;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.logging.Logging;

/**
 * Handler class that handles the <tt>jWebSocket.xml</tt> configuration. This
 * class starts from the root and delegates the handler to specific config
 * handler, to read the whole config file.
 * 
 * @author puran
 * @version $Id$
 * 
 */
public class JWebSocketConfigHandler implements ConfigHandler {
	
	private static Logger log = Logging.getLogger(JWebSocketConfigHandler.class);

	private static final String ELEMENT_INSTALLATION = "installation";
	private static final String ELEMENT_PROTOCOL = "protocol";
	private static final String ELEMENT_PORT = "port";
	private static final String ELEMENT_JWEBSOCKET_HOME = "jWebSocketHome";
	private static final String ELEMENT_LIBRARY_FOLDER = "libraryFolder";
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
	private static final String JWEBSOCKET = "jWebSocket";

	private static Map<String, ConfigHandler> handlerContext = new HashMap<String, ConfigHandler>();

	// initialize the different config handler implementations
	static {
		handlerContext.put("engine", new EngineConfigHandler());
		handlerContext.put("plugin", new PluginConfigHandler());
		handlerContext.put("server", new ServerConfigHandler());
		handlerContext.put("user", new UserConfigHandler());
		handlerContext.put("role", new RoleConfigHandler());
		handlerContext.put("right", new RightConfigHandler());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JWebSocketConfig processConfig(XMLStreamReader streamReader) {
		JWebSocketConfig.Builder configBuilder = new JWebSocketConfig.Builder();
		try {
			while (streamReader.hasNext()) {
				streamReader.next();
				if (streamReader.isStartElement()) {
					String elementName = streamReader.getLocalName();
					
					if (elementName.equals(ELEMENT_INSTALLATION)) {
						streamReader.next();
						configBuilder.addInstallation(streamReader.getText());
					} else if (elementName.equals(ELEMENT_PROTOCOL)) {
						streamReader.next();
						configBuilder.addProtocol(streamReader.getText());
					} else if (elementName.equals(ELEMENT_JWEBSOCKET_HOME)) {
						streamReader.next();
						configBuilder.addJWebSocketHome(streamReader.getText());
					} else if (elementName.equals(ELEMENT_LIBRARY_FOLDER)) {
						streamReader.next();
						configBuilder.addLibraryFolder(streamReader.getText());
					} else if (elementName.equals(ELEMENT_ENGINES)) {
						if (log.isDebugEnabled()) {
							log.debug("Reading engines configuration");
						}
						List<EngineConfig> engines = handleEngines(streamReader);
						configBuilder = configBuilder.addEngines(engines);
					} else if (elementName.equals(ELEMENT_SERVERS)) {
						if (log.isDebugEnabled()) {
							log.debug("Reading servers configuration");
						}
						List<ServerConfig> servers = handleServers(streamReader);
						configBuilder = configBuilder.addServers(servers);
					} else if (elementName.equals(ELEMENT_PLUGINS)) {
						if (log.isDebugEnabled()) {
							log.debug("Reading plugins configuration");
						}
						List<PluginConfig> plugins = handlePlugins(streamReader);
						configBuilder = configBuilder.addPlugins(plugins);
					} else if (elementName.equals(ELEMENT_RIGHTS)) {
						if (log.isDebugEnabled()) {
							log.debug("Reading rights configuration");
						}
						List<RightConfig> globalRights = handleRights(streamReader);
						configBuilder = configBuilder.addGlobalRights(globalRights);
					} else if (elementName.equals(ELEMENT_ROLES)) {
						if (log.isDebugEnabled()) {
							log.debug("Reading roles configuration");
						}
						List<RoleConfig> roles = handleRoles(streamReader);
						configBuilder = configBuilder.addGlobalRoles(roles);
					} else if (elementName.equals(ELEMENT_USERS)) {
						if (log.isDebugEnabled()) {
							log.debug("Reading users configuration");
						}
						List<UserConfig> users = handleUsers(streamReader);
						configBuilder = configBuilder.addUsers(users);
					} else {
						// ignore
					}
				}
				if (streamReader.isEndElement()) {
					String elementName = streamReader.getLocalName();
					if (elementName.equals(JWEBSOCKET)) {
						if (log.isInfoEnabled()) {
							log.info("jWebSocket configuration successfully processed.");
						}
						break;
					}
				}
			}
		} catch (XMLStreamException e) {
		}
		// we are done with the handler context map, release it for garbage
		// collection
		handlerContext = null;
		// now return the config object, this is the only one config object that
		// should exists
		// in the system
		return configBuilder.buildConfig();
	}

	/**
	 * private method to handle the user config.
	 * @param streamReader the stream reader object
	 * @return the list of user config
	 * @throws XMLStreamException if there's any exception reading configuration
	 */
	private List<UserConfig> handleUsers(XMLStreamReader streamReader)
			throws XMLStreamException {
		List<UserConfig> users = new ArrayList<UserConfig>();
		while (streamReader.hasNext()) {
			streamReader.next();
			if (streamReader.isStartElement()) {
				String elementName = streamReader.getLocalName();
				if (elementName.equals(ELEMENT_USER)) {
					UserConfig user = (UserConfig) handlerContext.get(
							elementName).processConfig(streamReader);
					users.add(user);
				}
			}
			if (streamReader.isEndElement()) {
				String elementName = streamReader.getLocalName();
				if (elementName.equals(ELEMENT_USERS)) {
					break;
				}
			}
		}
		return users;
	}

	/**
	 * method that reads the roles configuration
	 * @param streamReader the stream reader object
	 * @return the list of roles config
	 * @throws XMLStreamException if there's any exception reading configuration
	 */
	private List<RoleConfig> handleRoles(XMLStreamReader streamReader)
			throws XMLStreamException {
		List<RoleConfig> roles = new ArrayList<RoleConfig>();
		while (streamReader.hasNext()) {
			streamReader.next();
			if (streamReader.isStartElement()) {
				String elementName = streamReader.getLocalName();
				if (elementName.equals(ELEMENT_ROLE)) {
					RoleConfig role = (RoleConfig) handlerContext.get(
							elementName).processConfig(streamReader);
					roles.add(role);
				}
			}
			if (streamReader.isEndElement()) {
				String elementName = streamReader.getLocalName();
				if (elementName.equals(ELEMENT_ROLES)) {
					break;
				}
			}
		}
		return roles;
	}
	/**
	 * private method to read the list of rights configuration
	 * @param streamReader the stream reader object
	 * @return the list of rights configuration
	 * @throws XMLStreamException if there's any exception reading configuration
	 */
	private List<RightConfig> handleRights(XMLStreamReader streamReader)
			throws XMLStreamException {
		List<RightConfig> rights = new ArrayList<RightConfig>();
		while (streamReader.hasNext()) {
			streamReader.next();
			if (streamReader.isStartElement()) {
				String elementName = streamReader.getLocalName();
				if (elementName.equals(ELEMENT_RIGHT)) {
					RightConfig right = (RightConfig) handlerContext.get(
							elementName).processConfig(streamReader);
					rights.add(right);
				}
			}
			if (streamReader.isEndElement()) {
				String elementName = streamReader.getLocalName();
				if (elementName.equals(ELEMENT_RIGHTS)) {
					break;
				}
			}
		}
		return rights;
	}

	/**
	 * private method that reads the config for plugins
	 * 
	 * @param streamReader
	 *            the stream reader object
	 * @return the list of plugin configs
	 * @throws XMLStreamException
	 *             if exception occurs while reading
	 */
	private List<PluginConfig> handlePlugins(XMLStreamReader streamReader)
			throws XMLStreamException {
		List<PluginConfig> plugins = new ArrayList<PluginConfig>();
		while (streamReader.hasNext()) {
			streamReader.next();
			if (streamReader.isStartElement()) {
				String elementName = streamReader.getLocalName();
				if (elementName.equals(ELEMENT_PLUGIN)) {
					PluginConfig plugin = (PluginConfig) handlerContext.get(
							elementName).processConfig(streamReader);
					plugins.add(plugin);
				}
			}
			if (streamReader.isEndElement()) {
				String elementName = streamReader.getLocalName();
				if (elementName.equals(ELEMENT_PLUGINS)) {
					break;
				}
			}
		}
		return plugins;
	}

	/**
	 * private method that reads the list of server configs
	 * 
	 * @param streamReader
	 *            the stream reader object
	 * @return the list of server configs
	 * @throws XMLStreamException
	 *             if exception occurs reading xml
	 */
	private List<ServerConfig> handleServers(XMLStreamReader streamReader)
			throws XMLStreamException {
		List<ServerConfig> servers = new ArrayList<ServerConfig>();
		while (streamReader.hasNext()) {
			streamReader.next();
			if (streamReader.isStartElement()) {
				String elementName = streamReader.getLocalName();
				if (elementName.equals(ELEMENT_SERVER)) {
					ServerConfig server = (ServerConfig) handlerContext.get(
							elementName).processConfig(streamReader);
					servers.add(server);
				}
			}
			if (streamReader.isEndElement()) {
				String elementName = streamReader.getLocalName();
				if (elementName.equals(ELEMENT_SERVERS)) {
					break;
				}
			}
		}
		return servers;
	}

	/**
	 * private method that reads the list of engines config from the xml file
	 * 
	 * @param streamReader
	 *            the stream reader object
	 * @return the list of engine configs
	 * @throws XMLStreamException
	 *             if exception occurs while reading
	 */
	private List<EngineConfig> handleEngines(XMLStreamReader streamReader)
			throws XMLStreamException {
		List<EngineConfig> engines = new ArrayList<EngineConfig>();
		while (streamReader.hasNext()) {
			streamReader.next();
			if (streamReader.isStartElement()) {
				String elementName = streamReader.getLocalName();
				if (elementName.equals(ELEMENT_ENGINE)) {
					EngineConfig engine = (EngineConfig) handlerContext.get(
							elementName).processConfig(streamReader);
					engines.add(engine);
				}
			}
			if (streamReader.isEndElement()) {
				String elementName = streamReader.getLocalName();
				if (elementName.equals(ELEMENT_ENGINES)) {
					break;
				}
			}
		}
		return engines;
	}
}
