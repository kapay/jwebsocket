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
package org.jwebsocket.server.loader;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * ClassLoader that loads the classes from the jars. Engine, Servers, Plugins
 * all configured via jWebSocket.xml file is loaded using this class.
 * 
 * @author puran
 * @version $Id:$
 */
public class JWebSocketJarClassLoader extends URLClassLoader {
	
	/**
	 * constructor
	 * @param urls the urls of the jar
	 */
	public JWebSocketJarClassLoader(URL[] urls) {
		super(urls);
	}
	/**
	 * {@inheritDoc}
	 */
	public void addFile(String path) throws MalformedURLException {
		String urlPath = "jar:file://" + path + "!/";
		addURL(new URL(urlPath));
	}

	public static void main(String args[]) {
		try {
			System.out.println("First attempt...");
			Class.forName("org.gjt.mm.mysql.Driver");
		} catch (Exception ex) {
			System.out.println("Failed.");
		}

		try {
			URL urls[] = {};

			JWebSocketJarClassLoader cl = new JWebSocketJarClassLoader(urls);
			cl
					.addFile("/opt/mysql-connector-java-5.0.4/mysql-connector-java-5.0.4-bin.jar");
			System.out.println("Second attempt...");
			cl.loadClass("org.gjt.mm.mysql.Driver");
			System.out.println("Success!");
		} catch (Exception ex) {
			System.out.println("Failed.");
			ex.printStackTrace();
		}
	}
}