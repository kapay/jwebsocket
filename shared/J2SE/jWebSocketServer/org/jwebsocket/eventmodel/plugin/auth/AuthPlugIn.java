//  ---------------------------------------------------------------------------
//  jWebSocket - EventsPlugIn
//  Copyright (c) 2010 Innotrade GmbH, jWebSocket.org
//  ---------------------------------------------------------------------------
//  This program is free software; you can redistribute it and/or modify it
//  under the terms of the GNU Lesser General Public License as published by the
//  Free Software Foundation; either version 3 of the License, or (at your
//  option) any later version.
//  This program is distributed in the hope that it will be useful, but WITHOUT
//  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//  more details.
//  You should have received a copy of the GNU Lesser General Public License along
//  with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//  ---------------------------------------------------------------------------
package org.jwebsocket.eventmodel.plugin.auth;

import org.jwebsocket.eventmodel.plugin.EventModelPlugIn;
import org.jwebsocket.eventmodel.event.WebSocketResponseEvent;
import org.jwebsocket.eventmodel.event.auth.Logon;
import org.jwebsocket.eventmodel.event.auth.Logoff;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 *
 * @author Itachi
 */
public class AuthPlugIn extends EventModelPlugIn {

	private AuthenticationManager am;

	@Override
	public void initialize() throws Exception {
	}

	/**
	 * The login process
	 *
	 * @param aEvent
	 * @param aResponseEvent
	 */
	public void processEvent(Logon aEvent, WebSocketResponseEvent aResponseEvent) {
		String username = aEvent.getArgs().getString("username");
		String password = aEvent.getArgs().getString("password");

		Authentication request = new UsernamePasswordAuthenticationToken(username, password);
		Authentication result = getAm().authenticate(request);
		SecurityContextHolder.getContext().setAuthentication(result);

		aResponseEvent.getTo().add(aEvent.getConnector());
		aResponseEvent.setMessage(">> Login process has finished successfully. Username: '" + username + "'");
	}

	/**
	 * The logout process
	 * 
	 * @param aEvent
	 * @param aResponseEvent
	 */
	public void processEvent(Logoff aEvent, WebSocketResponseEvent aResponseEvent) {
		SecurityContextHolder.clearContext();

		aResponseEvent.getTo().add(aEvent.getConnector());
		aResponseEvent.setMessage("<< Logout process has finished successfully!");
	}

	/**
	 * @return the am
	 */
	public AuthenticationManager getAm() {
		return am;
	}

	/**
	 * @param am the am to set
	 */
	public void setAm(AuthenticationManager am) {
		this.am = am;
	}
}
