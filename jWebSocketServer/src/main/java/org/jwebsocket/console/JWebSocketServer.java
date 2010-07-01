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
package org.jwebsocket.console;

import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.config.JWebSocketConstants;
import org.jwebsocket.factory.JWebSocketFactory;

/**
 * @author puran
 * @version $Id: JWebSocketServer.java 443 2010-05-06 12:03:08Z fivefeetfurther $
 *
 */
public class JWebSocketServer {

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		// the following 3 lines may not be removed due to GNU LGPL 3.0 license!
		System.out.println("jWebSocket Ver. " + JWebSocketConstants.VERSION_STR + " (" + System.getProperty("sun.arch.data.model") + "bit)");
		System.out.println(JWebSocketConstants.COPYRIGHT);
		System.out.println(JWebSocketConstants.LICENSE);
		System.out.println("Log files per default in jWebSocket.log if not overwritten in jWebSocket.xml.");


		JWebSocketFactory.start();
/*
		ArrayList lDomains = new ArrayList();
		lDomains.add("http://jwebsocket.org");
		EngineConfiguration lConfig = new EngineConfig(
				"tcp0", // id
				"org.jwebsocket.tcp.engines.TCPEngine", // name
				"jWebSocketTCPEngine-0.9.5.jar", // jar, if not in classpath
				8787, // port
				120000, // default session timeout
				16384, // max framesize
				lDomains // list of accepted domains
				);
		TCPEngine lEngine = new TCPEngine(lConfig);
*/
		WebSocketEngine engine = JWebSocketFactory.getEngine();
		if (engine != null) {
			while (engine.isAlive()) {
				try {
					Thread.sleep(250);
				} catch (InterruptedException ex) {
					// no handling required here
				}
			}
		}

		JWebSocketFactory.stop();
	}
}
