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
package org.jwebsocket.eventmodel.plugin.test;

import org.jwebsocket.eventmodel.plugin.EventModelPlugIn;
import org.jwebsocket.eventmodel.event.WebSocketResponseEvent;
import org.jwebsocket.eventmodel.event.test.GetEventsInfo;
import org.jwebsocket.eventmodel.event.test.GetHashCode;
import org.jwebsocket.eventmodel.event.test.S2CNotification;
import org.jwebsocket.eventmodel.event.test.SecureEvent;
import org.jwebsocket.eventmodel.observable.Event;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 *
 * @author kyberneees
 */
public class TestPlugIn extends EventModelPlugIn {

	public void processEvent(GetHashCode aEvent, WebSocketResponseEvent aResponseEvent) {
		String text = aEvent.getArgs().getString("text");

		aResponseEvent.getArgs().setInteger("hash_code", text.hashCode());
	}

	public void processEvent(GetEventsInfo aEvent, WebSocketResponseEvent aResponseEvent) {
		Token table = TokenFactory.createToken();
		table.setString("name", "EventsPlugIn");
		table.setString("version", "1.0");

		aResponseEvent.getArgs().setToken("table", table);
	}

	public void processEvent(S2CNotification aEvent, WebSocketResponseEvent aResponseEvent) {
		Event e = new Event();
		e.setId("setVisitorNumber");
		e.getArgs().setString("counter", "99999999999");   // ;)

		this.notifyEvent(e).to(getEm().getParent().getServer().getAllConnectors().values());
	}

	public void processEvent(SecureEvent aEvent, WebSocketResponseEvent aResponseEvent) {
		//Do something that requires a USER role.
		//See the SecureEvent definition in the 'event_definitions.xml' file
	}
}
