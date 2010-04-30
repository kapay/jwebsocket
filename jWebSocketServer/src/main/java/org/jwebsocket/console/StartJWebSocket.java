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
package org.jwebsocket.console;

import org.apache.log4j.Logger;
import org.jwebsocket.config.JWebSocketConstants;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.server.loader.JWebSocketLoader;

/**
 * @author puran
 * @version $Id:$
 *
 */
public class StartJWebSocket {
	private static Logger log = null;

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		//TODO: get this from xml file
		String loglevel = "debug";

		// initialize log4j logging engine
		// BEFORE instantiating any jWebSocket classes
		Logging.initLogs(loglevel, Logging.CONSOLE);
		log = Logging.getLogger(JWebSocket.class);

		// the following 3 lines may not be removed due to GNU GPL 3.0 license!
		System.out.println("jWebSocket Ver. "
				+ JWebSocketConstants.VERSION_STR);
		System.out.println(JWebSocketConstants.COPYRIGHT);
		System.out.println(JWebSocketConstants.LICENSE);

		JWebSocketLoader loader = new JWebSocketLoader();
		
		try {
			loader.initialize();
		} catch (WebSocketException e) {
			System.out.println("ERROR during JWebSocketServer startup");
			System.exit(0);
		}
		
		log.info("Server(s) successfully terminated.");
	}
}
