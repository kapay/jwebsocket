package org.jwebsocket.eventmodel.events;

import org.jwebsocket.eventmodel.api.IInitializable;
import java.util.HashMap;

/**
 *
 * @author Itachi
 */
public class EventsMap implements IInitializable {

	private HashMap<String, WebSocketEvent> map;

	@Override
	public void initialize() {
	}

	@Override
	public void shutdown() {
	}

	/**
	 * @return the map
	 */
	public HashMap<String, WebSocketEvent> getMap() {
		return map;
	}

	/**
	 * @param map the map to set
	 */
	public void setMap(HashMap<String, WebSocketEvent> map) {
		this.map = map;
	}
}
