//	---------------------------------------------------------------------------
//	jWebSocket - Context Listener for Jetty Web Applications
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
package org.jwebsocket.jetty;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.jwebsocket.console.JWebSocketTokenListenerSample;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.server.TokenServer;

/**
 * Web application life cycle listener.
 * @author alexanderschulze
 */
public class ContextListener implements ServletContextListener {

	/**
	 * initializes the web application on startup.
	 * @param aSCE
	 */
	@Override
	public void contextInitialized(ServletContextEvent aSCE) {
		// start the jWebSocket server sub system
		System.out.println("Starting the jWebSocket sub system for Jetty...");
		JWebSocketFactory.start("");
		System.out.println("jWebSocket sub system started.");

		// get the token server
		// and and a listener to it (for demo purposes)
		TokenServer lTS0 = (TokenServer) JWebSocketFactory.getServer("ts0");
		if (lTS0 != null) {
			// and add the sample listener to the server's listener chain
			lTS0.addListener(new JWebSocketTokenListenerSample());
		}
	}

	/**
	 * cleans up the web application on termination.
	 * @param aSCE
	 */
	@Override
	public void contextDestroyed(ServletContextEvent aSCE) {
		// stop the jWebSocket server sub system
		System.out.println("Stopping the jWebSocket sub system for Jetty...");
		JWebSocketFactory.stop();
		System.out.println("jWebSocket sub system stopped.");
	}
}
