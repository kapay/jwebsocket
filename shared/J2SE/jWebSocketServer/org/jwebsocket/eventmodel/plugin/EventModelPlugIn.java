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
import org.jwebsocket.eventmodel.observable.ObservableObject;
import org.jwebsocket.eventmodel.api.IEventModelPlugIn;
import org.jwebsocket.eventmodel.core.EventModel;
import org.jwebsocket.eventmodel.observable.Event;
import org.jwebsocket.eventmodel.observable.ResponseEvent;
import java.util.Map;
import java.util.Set;
import org.jwebsocket.eventmodel.event.WebSocketEventDefinition;

/**
 *
 * @author kyberneees
 */
public abstract class EventModelPlugIn extends ObservableObject implements IEventModelPlugIn {

	private String id;
	private EventModel em;
	private Map<String, Class<? extends Event>> clientAPI;

	public void initialize() throws Exception {
	}

	/**
	 * Short cut to set the plugIn events definitions
	 * @param defs
	 */
	public void setEventsDefinitions(Set<WebSocketEventDefinition> defs) {
		getEm().getEventFactory().getEventDefinitions().getSet().addAll(defs);
	}

	//Just for compatibility with the IObservable interface
	public void processEvent(Event aEvent, ResponseEvent aResponseEvent) {
		System.out.println(">> Response from '" + this.getClass().getName() + "', please override this method!");
	}

	public void shutdown() throws Exception {
	}

	/**
	 * Notify an event in the client-side
	 * 
	 * @param aEvent The event to be sent
	 * @return
	 */
	public EventNotification notifyEvent(Event aEvent) {
		return new EventNotification(this, aEvent);
	}

	/**
	 * Event Model events registration
	 *
	 * @param emEvents
	 * @throws Exception
	 */
	public void setEmEvents(Collection<Class<? extends Event>> emEvents) throws Exception {
		getEm().addEvents(emEvents);
		getEm().on(emEvents, this);
	}

	/**
	 * Event Model events registration & Client API definition
	 *
	 * @param emEvents
	 * @throws Exception
	 */
	public void setEmEventsAndClientAPI(Map<String, Class<? extends Event>> emEvents) throws Exception {
		setClientAPI(emEvents);
		getEm().addEvents(emEvents.values());
		getEm().on(emEvents.values(), this);
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

	/**
	 * @return the clientAPI
	 */
	public Map<String, Class<? extends Event>> getClientAPI() {
		return clientAPI;
	}

	/**
	 * @param clientAPI the clientAPI to set
	 */
	public void setClientAPI(Map<String, Class<? extends Event>> clientAPI) {
		this.clientAPI = clientAPI;
	}

	@Override
	public String toString() {
		return getId();
	}
}
