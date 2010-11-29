package org.jwebsocket.eventmodel.filter;

import org.jwebsocket.eventmodel.observable.ObservableObject;
import org.jwebsocket.eventmodel.api.IEventModelFilter;
import org.jwebsocket.eventmodel.events.WebSocketEvent;
import org.jwebsocket.eventmodel.events.WebSocketResponseEvent;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.eventmodel.context.EventModel;

/**
 *
 * @author Itachi
 */
public abstract class EventModelFilter extends ObservableObject implements IEventModelFilter {

	private String id;
	private EventModel em;

	@Override
	public void firstCall(WebSocketConnector aConnector, WebSocketEvent aEvent) throws Exception {
	}

	@Override
	public void secondCall(WebSocketConnector aConnector, WebSocketResponseEvent aEvent) throws Exception {
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
	public String toString(){
		return getId();
	}
}
