//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket Context Listener
//	Copyright (c) 2011 jWebSocket.org, Alexander Schulze, Innotrade GmbH
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
package org.jwebsocket.templates;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.jwebsocket.factory.JWebSocketFactory;

/**
 * Web application lifecycle listener.
 * @author aschulze
 */
public class ContextListener implements ServletContextListener {

	public void contextInitialized(ServletContextEvent sce) {
		// the following line must not be removed due to GNU LGPL 3.0 license!
		JWebSocketFactory.printCopyrightToConsole();
		// start the server
		JWebSocketFactory.start("");
	}

	public void contextDestroyed(ServletContextEvent sce) {
		// stop the server
		JWebSocketFactory.stop();
	}
}
