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
package org.jwebsocket.eventmodel.s2c;

import java.util.Collection;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.eventmodel.event.S2CEvent;
import org.jwebsocket.eventmodel.s2c.OnResponse;
import org.jwebsocket.eventmodel.s2c.S2CEventNotificationHandler;
import org.jwebsocket.plugins.events.EventsPlugIn;

/**
 *
 * @author kyberneees
 */
public class S2CEventNotification {

	private S2CEvent event;

	public S2CEventNotification(String plugInId, S2CEvent aEvent) {
		aEvent.setPlugInId(plugInId);
		event = aEvent;
	}

	public void to(WebSocketConnector aConnector, OnResponse aOnResponse) {
		((S2CEventNotificationHandler)EventsPlugIn.getBeanFactory().
				getBean("S2CEventNotificationHandler")).send(event, aConnector, aOnResponse);
	}

	public void to(Collection<WebSocketConnector> aConnectorCollection, OnResponse aOnResponse) {
		for (WebSocketConnector c : aConnectorCollection) {
			to(c, aOnResponse);
		}
	}
}
