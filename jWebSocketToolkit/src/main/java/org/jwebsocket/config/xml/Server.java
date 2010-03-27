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
package org.jwebsocket.config.xml;

import org.jwebsocket.config.Config;
/**
 * @author puran
 * @version $Id$
 * 
 */
public class Server implements Config {
	private String id;
	private String name;
	private String jar;

	public Server(String id, String name, String jar) {
		this.id = id;
		this.name = name;
		this.jar = jar;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the jar
	 */
	public String getJar() {
		return jar;
	}

	@Override
	public void validate() {
	}
}
