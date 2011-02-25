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
import org.jwebsocket.eventmodel.event.test.S2CPlusXYEvent;
import org.jwebsocket.eventmodel.event.test.SecureEvent;
import org.jwebsocket.eventmodel.event.test.UpdateSiteCounterEvent;
import org.jwebsocket.eventmodel.s2c.FailureReason;
import org.jwebsocket.eventmodel.s2c.OnResponse;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 *
 * @author kyberneees
 */
public class TestPlugIn extends EventModelPlugIn {

	public void processEvent(GetHashCode aEvent, WebSocketResponseEvent aResponseEvent) {
		aResponseEvent.getArgs().setInteger("hash_code", aEvent.getText().hashCode());
	}

	public void processEvent(GetEventsInfo aEvent, WebSocketResponseEvent aResponseEvent) {
		Token table = TokenFactory.createToken();
		table.setString("name", "EventsPlugIn");
		table.setString("version", "1.0");

		aResponseEvent.getArgs().setToken("table", table);
	}

	public void processEvent(S2CNotification aEvent, WebSocketResponseEvent aResponseEvent) {
		//Notification with callbacks
		this.notifyEventToClient(new S2CPlusXYEvent(5, 5)).to(aEvent.getConnector(),
				new OnResponse(null) {

					@Override
					public void success(Object aResponse) {
						System.out.println(">> S2CPlusXYEvent success callback. Response: " + (Integer) aResponse);
					}

					@Override
					public void failure(FailureReason reason) {
						System.out.println(">> S2CPlusXYEvent failure callback. Reason: " + reason.name());
					}
				});

		//Notification w/o callbacks
		UpdateSiteCounterEvent e = new UpdateSiteCounterEvent();
		e.setCounter(Integer.MAX_VALUE);
		//Sending to all connectors
		this.notifyEventToClient(e).to(getEm().getParent().getServer().getAllConnectors().values(), null);
	}

	public void processEvent(SecureEvent aEvent, WebSocketResponseEvent aResponseEvent) {
		//Do something that requires a USER role.
		//See the SecureEvent definition in the 'event_definitions.xml' file
	}
}
