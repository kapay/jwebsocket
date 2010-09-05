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
package org.jwebsocket.factory;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.apache.log4j.Logger;
import org.jwebsocket.api.EngineConfiguration;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.ServerConfiguration;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketFilter;
import org.jwebsocket.api.WebSocketInitializer;
import org.jwebsocket.api.WebSocketPlugIn;
import org.jwebsocket.api.WebSocketServer;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.config.xml.EngineConfig;
import org.jwebsocket.config.xml.PluginConfig;
import org.jwebsocket.config.xml.ServerConfig;
import org.jwebsocket.filters.custom.CustomTokenFilter;
import org.jwebsocket.filters.system.SystemFilter;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.netty.engines.NettyEngine;
import org.jwebsocket.plugins.system.SystemPlugIn;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.tcp.engines.TCPEngine;

/**
 * Abstract initializer class that performs the initialization
 * 
 * @author puran
 * @version $Id: AbstractJWebSocketInitializer.java 437 2010-05-03 22:10:20Z mailtopuran $
 */
public abstract class AbstractJWebSocketInitializer implements WebSocketInitializer {

  private static Logger log = Logging.getLogger(AbstractJWebSocketInitializer.class);
  
  /**the configuration object*/
  private JWebSocketConfig jWebSocketConfig = null;
  
  /**
   * @param theConfig the jwebsocket config object
   */
  public AbstractJWebSocketInitializer(JWebSocketConfig theConfig) {
    this.jWebSocketConfig = theConfig;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void initializeLogging() {
  }

  /**
   * Initialize the engine to be started based on configuration.
   * 
   * @return the initialized engine ready to start
   */
  @Override
  public WebSocketEngine initializeEngine() {
    if (log.isDebugEnabled()) {
      log.debug("Instantiating engine...");
    }
    EngineConfiguration oldConfig = jWebSocketConfig.getEngines().get(0);
    EngineConfiguration config = new EngineConfig(oldConfig.getId(), oldConfig.getName(), null, oldConfig.getPort(), 
              oldConfig.getTimeout(), oldConfig.getMaxFramesize(), oldConfig.getDomains());
    WebSocketEngine newEngine = null;
    try {
      if (config.getName().contains("TCPEngine")) {
        newEngine = new TCPEngine(config);
      } else {
        newEngine = new NettyEngine(config);
      }
    } catch (Exception e) {
      System.out.println("Error instantiating engine: " + e.getMessage());
      System.exit(0);
    }
    if (log.isInfoEnabled()) {
      log.info("Engine " + newEngine.getId() + " instantiated.");
    }
    return newEngine;
  }

  /**
   * Initializes all the servers configured via jWebSocket configuration
   * 
   * @return the list of initialized servers
   */
  @Override
  public List<WebSocketServer> initializeServers() {
    if (log.isDebugEnabled()) {
      log.debug("Instantiating default servers...");
    }
    List<WebSocketServer> servers = new FastList<WebSocketServer>();
    List<ServerConfiguration> cfgs = new ArrayList<ServerConfiguration>();
    for (ServerConfiguration cfg : jWebSocketConfig.getServers()) {
      //instantiate the Token server by default
      if ("org.jwebsocket.server.TokenServer".equals(cfg.getName())) {
        //initialize token server by default
        ServerConfiguration config = new ServerConfig(cfg.getId(), cfg.getName(), null);
        TokenServer tokenServer = new TokenServer(config);
        servers.add(tokenServer);
        if (log.isInfoEnabled()) {
          log.info("Default server " + tokenServer.getId() + " instantiated.");
        }
      } else {
        cfgs.add(cfg);
      }
    }
    //initialize custom servers 
    List<WebSocketServer> customServers = initializeCustomServers(cfgs);
    if (customServers != null) {
      if (log.isDebugEnabled()) {
        log.debug("Instantiating custom servers...");
      }
      servers.addAll(customServers);
      if (log.isInfoEnabled()) {
        log.info("Custom servers instantiated.");
      }
    }
    return servers;
  }

  /**
   * intialize the plugins as per the serverss
   * 
   * @return the FastMap of server id to list of plugins
   */
  @SuppressWarnings("unchecked")
  @Override
  public Map<String, List<WebSocketPlugIn>> initializePlugins() {
    if (log.isDebugEnabled()) {
      log.debug("Instantiating default plug-ins...");
    }
    //initialize the plugin map and default list of plugins 
    Map<String, List<WebSocketPlugIn>> pluginMap = new FastMap<String, List<WebSocketPlugIn>>();
    List<WebSocketPlugIn> defaultPlugins = new FastList<WebSocketPlugIn>();
    
    //custom plugin configs
    List<PluginConfiguration> cfgs = new ArrayList<PluginConfiguration>();
    
    List<PluginConfig> pluginConfigs = jWebSocketConfig.getPlugins(); 
    for (PluginConfig config : pluginConfigs) {
      PluginConfiguration configuration = new PluginConfig(config.getId(), config.getName(), config.getPackage(), 
          null, config.getNamespace(),config.getServers(), config.getSettings());
      if (config.getName().equals("org.jwebsocket.plugins.system.SystemPlugin") 
          || config.getName().equals("org.jwebsocket.plugins.rpc.RPCPlugIn")
          || config.getName().equals("org.jwebsocket.plugins.streaming.StreamingPlugIn")
          || config.getName().equals("org.jwebsocket.plugins.flashbridge.FlashBridgePlugIn")
          || config.getName().equals("org.jwebsocket.plugins.admin.RPCPlugIn")) {
        try {
          Class<WebSocketPlugIn> clazz = (Class<WebSocketPlugIn>)Class.forName(config.getName());
          Constructor<WebSocketPlugIn> cons = clazz.getDeclaredConstructor(PluginConfiguration.class);
          cons.setAccessible(true);
            defaultPlugins.add(cons.newInstance(configuration));
        } catch (Exception e) {
          log.error("Could not initialize the plugin:", e);
        }
        defaultPlugins.add(new SystemPlugIn(configuration));
      } else {
        //this is custom plugin 
        cfgs.add(config);
      }
    }
    String tokenServerId = getTokenServerId();
    pluginMap.put(tokenServerId, defaultPlugins);
    if (log.isInfoEnabled()) {
      log.info("Default plug-ins instantiated.");
    }

    if (log.isDebugEnabled()) {
      log.debug("Instantiating custom plug-ins...");
    }

    Map<String, List<WebSocketPlugIn>> customPluginMap = initializeCustomPlugins(cfgs);
    for (Map.Entry<String, List<WebSocketPlugIn>> entry : customPluginMap.entrySet()) {
      String id = entry.getKey();
      if (pluginMap.containsKey(id)) {
        pluginMap.get(id).addAll(entry.getValue());
      } else {
        pluginMap.put(id, entry.getValue());
      }
    }
    if (log.isInfoEnabled()) {
      log.info("Custom plug-ins instantiated.");
    }
    return pluginMap;
  }

  /**
   * @return the token server id
   */
  private String getTokenServerId() {
    for (ServerConfiguration cfg : jWebSocketConfig.getServers()) {
      //instantiate the Token server by default
      if ("org.jwebsocket.server.TokenServer".equals(cfg.getName())) {
        return cfg.getId();
      }
    }
    return "ts0";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, List<WebSocketFilter>> initializeFilters() {

    if (log.isDebugEnabled()) {
      log.debug("Instantiating default filters...");
    }
    Map<String, List<WebSocketFilter>> filterMap = new FastMap<String, List<WebSocketFilter>>();
    List<WebSocketFilter> defaultFilters = new FastList<WebSocketFilter>();
    defaultFilters.add(new SystemFilter("systemFilter"));
    defaultFilters.add(new CustomTokenFilter("userFilter"));

    filterMap.put(getTokenServerId(), defaultFilters);
    if (log.isInfoEnabled()) {
      log.info("Default filters instantiated.");
    }

    if (log.isDebugEnabled()) {
      log.debug("Instantiating custom filters...");
    }
    Map<String, List<WebSocketFilter>> customFilterMap = initializeCustomFilters();
    for (Map.Entry<String, List<WebSocketFilter>> entry : customFilterMap.entrySet()) {
      String id = entry.getKey();
      if (filterMap.containsKey(id)) {
        filterMap.get(id).addAll(entry.getValue());
      } else {
        filterMap.put(id, entry.getValue());
      }
    }

    if (log.isInfoEnabled()) {
      log.info("Custom filters instantiated.");
    }
    return filterMap;
  }
  
  @Override
  public JWebSocketConfig getConfig() {
    return jWebSocketConfig;
  }

  /**
   * Allow subclass of this class to initialize custom plugins. It takes the list of custom plugin configurations
   * in order as defined in jWebSocket.xml file. 
   * @param configurations the list configurations for custom plugins in order.
   * @return the map of custom plugins to server id.
   */
  public abstract Map<String, List<WebSocketPlugIn>> initializeCustomPlugins(List<PluginConfiguration> configurations);

  /**
   * Allow the subclass of this class to initialize custom servers. It takes the list of custom server configurations
   * in order as defined in jWebSocket.xml file as the argument
   * @param cfgs the list of custom server configurations in order
   * @return the list of custom servers
   */
  public abstract List<WebSocketServer> initializeCustomServers(List<ServerConfiguration> cfgs);

  /**
   * Allow the subclass of this class to initialize custom filters
   * 
   * @return the list of custom filters to server id
   */
  public abstract Map<String, List<WebSocketFilter>> initializeCustomFilters();
}
