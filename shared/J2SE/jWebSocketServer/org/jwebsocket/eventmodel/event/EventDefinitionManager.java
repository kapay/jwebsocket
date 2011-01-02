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
package org.jwebsocket.eventmodel.event;

import org.jwebsocket.api.IInitializable;
import java.util.Set;
import org.jwebsocket.eventmodel.observable.Event;

/**
 *
 ** @author kyberneees
 */
public class EventDefinitionManager implements IInitializable {

	private Set<WebSocketEventDefinition> set;

	@Override
	public void initialize() {
	}

	@Override
	public void shutdown() {
	}

	/**
	 * @return the set
	 */
	public Set<WebSocketEventDefinition> getSet() {
		return set;
	}

	/**
	 * @param set the set to set
	 */
	public void setSet(Set<WebSocketEventDefinition> set) {
		this.set = set;
	}

	public boolean hasDefinition(String aEventId) {
		for (WebSocketEventDefinition def : set) {
			if (def.getId().equals(aEventId)) {
				return true;
			}
		}

		return false;
	}

	public WebSocketEventDefinition getDefinition(String aEventId) throws Exception {
		for (WebSocketEventDefinition def : set) {
			if (def.getId().equals(aEventId)) {
				return def;
			}
		}

		throw new IndexOutOfBoundsException("The event definition with id '" + aEventId + "' does not exists!");
	}

	public String getIdByClass(Class<? extends Event> aEventClass) throws Exception {
		for (WebSocketEventDefinition def : set) {
			if (def.getEventClass().equals(aEventClass)) {
				return def.getId();
			}
		}

		throw new IndexOutOfBoundsException("The event definition with class '" + aEventClass.toString() + "' does not exists!");
	}
}
