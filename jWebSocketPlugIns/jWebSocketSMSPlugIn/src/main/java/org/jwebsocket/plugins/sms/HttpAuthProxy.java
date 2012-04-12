//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket http authentication against Proxy
//  Copyright (c) 2012 Innotrade GmbH, jWebSocket.org
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
package org.jwebsocket.plugins.sms;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 *
 * @author mayra
 */
public class HttpAuthProxy extends Authenticator {

	private String user, password;

	public HttpAuthProxy(String user, String password) {
		this.user = user;
		this.password = password;
	}

	protected PasswordAuthentication setAuthentication() {
		return new PasswordAuthentication("usser", "password".toCharArray());
	}
	/* 
	 * System.setProperty("http.proxyHost", getHTTPHost());
	System.setProperty("http.proxyPort", getHTTPPort());
	 * 
	 */
}
