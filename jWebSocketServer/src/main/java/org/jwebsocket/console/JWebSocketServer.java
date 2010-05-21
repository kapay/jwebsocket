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

import org.jwebsocket.config.JWebSocketConstants;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.plugins.sample.SamplePlugIn;
import org.jwebsocket.server.TokenServer;

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
		// TODO: get this from xml file
		String loglevel = "debug";
		int logTarget = Logging.CONSOLE;

		// parse optional command line arguments
		int i = 0;
		while (i < args.length) {
			String[] keyVal = args[i].split("=", 2);
			if (keyVal.length == 2) {
				String key = keyVal[0];
				String value = keyVal[1];
				if ("logtarget".equalsIgnoreCase(key)) {
					if ("console".equals(value)) {
						logTarget = Logging.CONSOLE;
					} else if ("rollingfile".equals(value)) {
						logTarget = Logging.ROLLING_FILE;
					}
					if ("singlefile".equals(value)) {
						logTarget = Logging.SINGLE_FILE;
					}
				} else if ("loglevel".equalsIgnoreCase(key)) {
					loglevel = value;
				}
			}
			i++;
		}

		// the following 3 lines may not be removed due to GNU GPL 3.0 license!
		System.out.println("jWebSocket Ver. "
				+ JWebSocketConstants.VERSION_STR
				+ " (" + System.getProperty("sun.arch.data.model") + "bit)");
		System.out.println(JWebSocketConstants.COPYRIGHT);
		System.out.println(JWebSocketConstants.LICENSE);
		// System.out.println("Starting with logLevel=" + loglevel + " and logTarget=" + logTarget + "...");

		JWebSocketFactory.start(
				loglevel,
				logTarget);

		TokenServer lTS = (TokenServer) JWebSocketFactory.getServer("ts0");
		if (lTS != null) {
			SamplePlugIn lSP = new SamplePlugIn();
			lTS.getPlugInChain().addPlugIn(lSP);
		}

		while (JWebSocketFactory.getEngine().isAlive()) {
			try {
				Thread.sleep(250);
			} catch (InterruptedException ex) {
			}
		}

		JWebSocketFactory.stop();
	}
}
