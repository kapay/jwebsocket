package org.jwebsocket.eventmodel.api;

import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.eventmodel.events.WebSocketEvent;
import org.jwebsocket.eventmodel.events.WebSocketResponseEvent;
import org.jwebsocket.eventmodel.context.EventModel;

/**
 *
 * @author Itachi
 */
public interface IEventModelFilter {

	public String getId();

	public void setId(String id);

	public EventModel getEm();

	public void setEm(EventModel em);

	public void firstCall(WebSocketConnector aConnector, WebSocketEvent aEvent) throws Exception;

	public void secondCall(WebSocketConnector aConnector, WebSocketResponseEvent aEvent) throws Exception;
	
}
