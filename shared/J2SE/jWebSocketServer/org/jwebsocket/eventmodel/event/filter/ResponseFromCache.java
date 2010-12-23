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
package org.jwebsocket.eventmodel.event.filter;

import org.jwebsocket.eventmodel.event.WebSocketEvent;
import org.jwebsocket.token.Token;
import org.jwebsocket.eventmodel.observable.Event;

/**
 *
 * @author Itachi
 */
public class ResponseFromCache extends Event {

	private Token cachedResponse;
	private WebSocketEvent event;

	/**
	 * @return the event
	 */
	public WebSocketEvent getEvent() {
		return event;
	}

	/**
	 * @param event the event to set
	 */
	public void setEvent(WebSocketEvent event) {
		this.event = event;
	}

	/**
	 * @return the cachedResponse
	 */
	public Token getCachedResponse() {
		return cachedResponse;
	}

	/**
	 * @param cachedResponse the cachedResponse to set
	 */
	public void setCachedResponse(Token cachedResponse) {
		this.cachedResponse = cachedResponse;
	}
}
