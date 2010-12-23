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
package org.jwebsocket.eventmodel.filter.security;

import java.util.Collection;
import org.jwebsocket.eventmodel.filter.EventModelFilter;
import org.jwebsocket.eventmodel.event.WebSocketEvent;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.eventmodel.event.WebSocketEventDefinition;
import org.jwebsocket.eventmodel.exception.NotAuthorizedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 *
 * @author Itachi
 */
public class SecurityFilter extends EventModelFilter {

	@Override
	public void firstCall(WebSocketConnector aConnector, WebSocketEvent aEvent) throws Exception {
		WebSocketEventDefinition def = getEm().getEventFactory().getEventDefinitions().getDefinition(aEvent.getId());
		if (def.isSecurityEnabled() && def.getRoles().size() > 0) {
			if (null != SecurityContextHolder.getContext().getAuthentication()) {
				Collection<GrantedAuthority> connectorRoles = 
						(Collection<GrantedAuthority>) SecurityContextHolder.getContext().
						getAuthentication().getAuthorities();

				for (GrantedAuthority role : connectorRoles) {
					if (def.getRoles().contains(role.getAuthority())) {
						return; //Authorized!
					}
				}
			}

			//Not authorized!
			throw new NotAuthorizedException("Unauthorized notification of '"
					+ aEvent.getId() + "' event. Allowed roles for notify this event: '"
					+ def.getRoles().toString() + "'");
		}
	}
}
