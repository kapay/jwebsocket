//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket NetBeans Server Template
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

import org.jwebsocket.factory.JWebSocketFactory;

/**
 *
 * @author aschulze
 */
public class SimpleServerTemplate {

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		// the following line must not be removed due to GNU LGPL 3.0 license!
		JWebSocketFactory.printCopyrightToConsole();

		try {
			JWebSocketFactory.start();
			// run the server until shutdown request
			JWebSocketFactory.run();
		} catch (Exception lEx) {
			System.out.println(
					lEx.getClass().getSimpleName()
					+ " on starting or while running jWebSocket server: "
					+ lEx.getMessage());
		} finally {
			JWebSocketFactory.stop();
		}
	}
}
