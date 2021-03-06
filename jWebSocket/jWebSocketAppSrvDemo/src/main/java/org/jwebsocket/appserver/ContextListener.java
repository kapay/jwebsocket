//	---------------------------------------------------------------------------
//	jWebSocket - Context Listener for Web Applications
//	Copyright (c) 2010 jWebSocket.org, Alexander Schulze, Innotrade GmbH
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
package org.jwebsocket.appserver;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.server.TokenServer;

/**
 * Web application life cycle listener.
 * 
 * @author aschulze
 */
public class ContextListener implements ServletContextListener {

  /**
   * initializes the web application on startup.
   * @param sce
   */
  @Override
  public void contextInitialized(ServletContextEvent sce) {
    // start the jWebSocket server sub system
    JWebSocketFactory.start("");

	// assign web socket server to servlet bridge
	ServletBridge.setServer((TokenServer)JWebSocketFactory.getServer("ts0"));
  }

  /**
   * cleans up the web application on termination.
   * @param sce
   */
  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    // stop the jWebSocket server sub system
    JWebSocketFactory.stop();
  }
}
