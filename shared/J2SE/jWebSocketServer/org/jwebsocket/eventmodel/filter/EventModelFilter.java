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
package org.jwebsocket.eventmodel.filter;

import java.util.Set;
import org.jwebsocket.api.IInitializable;
import org.jwebsocket.eventmodel.observable.ObservableObject;
import org.jwebsocket.eventmodel.api.IEventModelFilter;
import org.jwebsocket.eventmodel.event.WebSocketEvent;
import org.jwebsocket.eventmodel.event.WebSocketResponseEvent;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.eventmodel.api.IListener;
import org.jwebsocket.eventmodel.core.EventModel;
import org.jwebsocket.eventmodel.observable.Event;
import org.jwebsocket.eventmodel.observable.ResponseEvent;

/**
 *
 * @author kyberneees
 */
public abstract class EventModelFilter extends ObservableObject implements IEventModelFilter, IInitializable, IListener {

	private String id;
	private EventModel em;

	@Override
	public void beforeCall(WebSocketConnector aConnector, WebSocketEvent aEvent) throws Exception {
	}

	@Override
	public void afterCall(WebSocketConnector aConnector, WebSocketResponseEvent aEvent) throws Exception {
	}

	public void initialize() throws Exception {
	}

	public void shutdown() throws Exception {
	}

	//Just for compatibility with the IObservable interface
	public void processEvent(Event aEvent, ResponseEvent aResponseEvent) {
		System.out.println(">> Response from '" + this.getClass().getName() + "', please override this method!");
	}

	/**
	 * Event Model events registration
	 *
	 * @param emEvents
	 * @throws Exception
	 */
	public void setEmEvents(Set<Class<? extends Event>> emEvents) throws Exception{
		getEm().addEvents(emEvents);
		getEm().on(emEvents, this);
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the em
	 */
	public EventModel getEm() {
		return em;
	}

	/**
	 * @param em the em to set
	 */
	public void setEm(EventModel em) {
		this.em = em;
	}

	@Override
	public String toString() {
		return getId();
	}
}
