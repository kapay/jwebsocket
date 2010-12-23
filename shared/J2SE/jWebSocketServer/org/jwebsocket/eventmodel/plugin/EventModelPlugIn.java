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
import org.jwebsocket.eventmodel.context.EventModel;
import org.jwebsocket.eventmodel.observable.Event;
import org.jwebsocket.eventmodel.observable.ResponseEvent;
import java.util.Map;

/**
 *
 * @author Itachi
 */
public abstract class EventModelPlugIn extends ObservableObject implements IEventModelPlugIn {

	private String id;
	private EventModel em;
	private Map clientAPI;

	@Override
	public void initialize() throws Exception {
	}

	//Just for compatibility with the IObservable interface
	@Override
	public void processEvent(Event aEvent, ResponseEvent aResponseEvent) {
		System.out.println(">> Response from '" + this.getClass().getName() + "', please override this method!");
	}

	@Override
	public void shutdown() throws Exception {
	}

	/**
	 * Event Model events registration
	 *
	 * @param emEvents
	 * @throws Exception
	 */
	public void setEmEvents(Collection<Class> emEvents) throws Exception{
		getEm().addEvents(emEvents);
		getEm().on(emEvents, this);
	}

	/**
	 * Event Model events registration & Client API definition
	 *
	 * @param emEvents
	 * @throws Exception
	 */
	public void setEmEvents(Map<String, Class> emEvents) throws Exception{
		setEmEvents(emEvents.values());
		setClientAPI(emEvents);
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
	public Map getClientAPI() {
		return clientAPI;
	}

	/**
	 * @param clientAPI the clientAPI to set
	 */
	public void setClientAPI(Map clientAPI) {
		this.clientAPI = clientAPI;
	}

	@Override
	public String toString(){
		return getId();
	}
}
