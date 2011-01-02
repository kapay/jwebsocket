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
package org.jwebsocket.eventmodel.plugin;

import java.util.Collection;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.eventmodel.observable.Event;

public class EventNotification {

	private Event event;
	private EventModelPlugIn plugIn;

	public EventNotification(EventModelPlugIn plugIn, Event aEvent) {
		this.plugIn = plugIn;
		this.event = aEvent;
		this.event.getArgs().setType("s2c.event_notification.to(" + plugIn.getId() + ")");
		this.event.getArgs().setString("eventName", aEvent.getId());
	}

	public void to(WebSocketConnector aConnector) {
		plugIn.getEm().getParent().getServer().sendTokenAsync(aConnector, plugIn.getEm().getEventFactory().eventToToken(event));
	}

	public void to(Collection<WebSocketConnector> aConnectorCollection) {
		for (WebSocketConnector c : aConnectorCollection) {
			to(c);
		}
	}
}
