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
package org.jwebsocket.eventmodel.api;

import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.eventmodel.event.WebSocketEvent;
import org.jwebsocket.eventmodel.event.WebSocketResponseEvent;
import org.jwebsocket.eventmodel.core.EventModel;

/**
 *
 * @author kyberneees
 */
public interface IEventModelFilter {

	public String getId();

	public void setId(String id);

	public EventModel getEm();

	public void setEm(EventModel em);

	public void firstCall(WebSocketConnector aConnector, WebSocketEvent aEvent) throws Exception;

	public void secondCall(WebSocketConnector aConnector, WebSocketResponseEvent aEvent) throws Exception;
	
}
