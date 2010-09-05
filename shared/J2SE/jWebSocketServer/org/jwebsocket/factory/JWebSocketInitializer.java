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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.ServerConfiguration;
import org.jwebsocket.api.WebSocketFilter;
import org.jwebsocket.api.WebSocketPlugIn;
import org.jwebsocket.api.WebSocketServer;
import org.jwebsocket.config.JWebSocketConfig;

/**
 * Class that performs the default servers and plugins initialization
 * 
 * @author puran
 * @version $Id: JWebSocketInitializer.java 399 2010-04-30 01:00:57Z mailtopuran$
 */
public class JWebSocketInitializer extends AbstractJWebSocketInitializer {
  
    public JWebSocketInitializer(JWebSocketConfig theConfig) {
      super(theConfig);
    }

    @Override
    public Map<String, List<WebSocketPlugIn>> initializeCustomPlugins(List<PluginConfiguration> configurations) {
      return Collections.emptyMap();
    }

    @Override
    public List<WebSocketServer> initializeCustomServers(List<ServerConfiguration> cfgs) {
      return Collections.emptyList();
    }

    @Override
    public Map<String, List<WebSocketFilter>> initializeCustomFilters() {
      return Collections.emptyMap();
    }
}
