//	---------------------------------------------------------------------------
//	jWebSocket - Context Listener for Web Applications
//	Copyright (c) 2010 jWebSocket.org, Alexander Schulze, Innotrade GmbH
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
package org.jwebsocket.appserver;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.sample.SamplePlugIn;
import org.jwebsocket.server.TokenServer;

/**
 * Web application lifecycle listener.
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
		JWebSocketFactory.start(
			null, // use default level
			null, // use default target
			null, // use default filename
			null, // use default pattern
			null  // use default buffersize
		);

		TokenServer lTS = (TokenServer)JWebSocketFactory.getServer("ts0");
		if( lTS != null ) {
			SamplePlugIn lSP = new SamplePlugIn();
			lTS.getPlugInChain().addPlugIn(lSP);
		}

		// ServletBridge.setServer(lTS);
	}

	/**
	 * cleans up the web application on termination.
	 * @param sce
	 */
	@Override
	public void contextDestroyed(ServletContextEvent sce) {

		// stop the jWebSocket server sub system
		JWebSocketFactory.stop(
		);

	}

}
