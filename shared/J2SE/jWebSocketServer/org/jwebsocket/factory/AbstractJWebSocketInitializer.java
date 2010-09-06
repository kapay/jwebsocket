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

import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketFilter;
import org.jwebsocket.api.WebSocketInitializer;
import org.jwebsocket.api.WebSocketPlugIn;
import org.jwebsocket.api.WebSocketServer;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.config.LoggingConfig;
import org.jwebsocket.logging.Logging;

/**
 * Abstract initializer class that performs the initialization
 * 
 * @author puran
 * @version $Id: AbstractJWebSocketInitializer.java 437 2010-05-03 22:10:20Z mailtopuran $
 */
public abstract class AbstractJWebSocketInitializer implements WebSocketInitializer {
  // don't initialize logger here! Will be initialized with loaded settings!
  protected static Logger mLog = null;

  /** the configuration object */
  protected JWebSocketConfig jWebSocketConfig = null;

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
    LoggingConfig lLoggingConfig = jWebSocketConfig.getLoggingConfig();
    // initialize log4j logging engine
    // BEFORE instantiating any jWebSocket classes
    Logging.initLogs(lLoggingConfig.getLevel(), lLoggingConfig.getAppender(), lLoggingConfig.getFilename(), 
        lLoggingConfig.getPattern(), lLoggingConfig.getBufferSize(), new String[] {"%JWEBSOCKET_HOME%/logs", 
        "%CATALINA_HOME%/logs" });
    mLog = Logging.getLogger(JWebSocketXmlConfigInitializer.class);
    if (mLog.isDebugEnabled()) {
      mLog.debug("Logging settings" + ": appender: " + lLoggingConfig.getAppender() + ", filename: " 
          + lLoggingConfig.getFilename() + ", level: " + lLoggingConfig.getLevel() + ", buffersize: "
          + lLoggingConfig.getBufferSize() + ", pattern: " + lLoggingConfig.getPattern());
    }
    if (mLog.isDebugEnabled()) {
      mLog.debug("Starting jWebSocket Server Sub System...");
    }
  }

  /**
   * Load the engine from the classpath
   * @param engineName the name of the engine to load
   * @return
   */
  @SuppressWarnings("unchecked")
  public Class<WebSocketEngine> loadEngineFromClassPath(String engineName) {
    if (mLog.isDebugEnabled()) {
      mLog.debug("Instantiating engine...");
    }
    try {
      Class<WebSocketEngine> engineClass = (Class<WebSocketEngine>) Class.forName(engineName);
      if (mLog.isDebugEnabled()) {
        mLog.debug("Engine '" + engineName + "' loaded from classpath.");
      }
      return engineClass;
    } catch (ClassNotFoundException e) {
      if (mLog.isDebugEnabled()) {
        mLog.debug("Engine '" + engineName + "' not yet in classpat", e);
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public Class<WebSocketServer> loadServerFromClasspath(String serverName) {
    try {
      Class<WebSocketServer> serverClass = (Class<WebSocketServer>) Class.forName(serverName);
      if (mLog.isDebugEnabled()) {
        mLog.debug("Server '" + serverName + "' loaded from classpath.");
      }
      return serverClass;
    } catch (ClassNotFoundException ex) {
      if (mLog.isDebugEnabled()) {
        mLog.debug("Server '" + serverName + "' not yet in classpath", ex);
      }
    }
    return null;
  }
  
  @SuppressWarnings("unchecked")
  public Class<WebSocketPlugIn> loadPluginFromClasspath(String pluginName) {
    try {
      Class<WebSocketPlugIn> pluginClass = (Class<WebSocketPlugIn>) Class.forName(pluginName);
      if (mLog.isDebugEnabled()) {
        mLog.debug("PlugIn '" + pluginName + "' loaded from classpath.");
      }
      return pluginClass;
    } catch (ClassNotFoundException ex) {
      if (mLog.isDebugEnabled()) {
        mLog.debug("PlugIn '" + pluginName + "' not yet in classpath", ex);
      }
    }
    return null;
  }
  
  @SuppressWarnings("unchecked")
  public Class<WebSocketFilter> loadFilterFromClasspath(String filterName) {
    try {
      Class<WebSocketFilter> filterClass = (Class<WebSocketFilter>) Class.forName(filterName);
      if (mLog.isDebugEnabled()) {
        mLog.debug("Filter '" + filterName + "' loaded from classpath.");
      }
      return filterClass;
    } catch (ClassNotFoundException ex) {
      if (mLog.isDebugEnabled()) {
        mLog.debug("Filter '" + filterName + "' not yet in classpath", ex);
      }
    }
    return null;
  }
  @Override
  public JWebSocketConfig getConfig() {
    return jWebSocketConfig;
  }
}
