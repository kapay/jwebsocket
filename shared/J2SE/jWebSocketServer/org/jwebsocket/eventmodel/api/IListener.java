package org.jwebsocket.eventmodel.api;

import org.jwebsocket.eventmodel.observable.Event;
import org.jwebsocket.eventmodel.observable.ResponseEvent;

/**
 *
 * @author Itachi
 */
public interface IListener {

	public void processEvent(Event aEvent, ResponseEvent aResponseEvent);
}
