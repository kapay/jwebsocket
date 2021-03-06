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

import javolution.util.FastMap;
import org.jwebsocket.eventmodel.observable.Event;
import org.jwebsocket.api.IInitializable;
import org.jwebsocket.api.WebSocketConnector;

/**
 *
 * @author kyberneees
 */
public abstract class WebSocketEvent extends Event implements IInitializable {

	private WebSocketConnector connector;
	private int requestId;

	@Override
	public void initialize() {
		FastMap<Object, Object> m = new FastMap<Object, Object>();
		for (Object key : getArgs().getMap().keySet()) {
			if (!"utid".equals(key) && !"usid".equals(key)) {
				m.put(key, getArgs().getMap().get(key));
			}
		}

		requestId = m.hashCode();
	}

	@Override
	public void shutdown() {
	}

	/**
	 * @return the connector
	 */
	public WebSocketConnector getConnector() {
		return connector;
	}

	/**
	 * @param connector the connector to set
	 */
	public void setConnector(WebSocketConnector connector) {
		this.connector = connector;
	}

	/**
	 * @return the requestId
	 */
	public int getRequestId() {
		return requestId;
	}
}
