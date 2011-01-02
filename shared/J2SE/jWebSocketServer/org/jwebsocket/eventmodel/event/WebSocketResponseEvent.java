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

import org.jwebsocket.eventmodel.observable.ResponseEvent;
import javolution.util.FastSet;
import org.jwebsocket.api.WebSocketConnector;
import java.util.Set;

/**
 *
 ** @author kyberneees
 */
public class WebSocketResponseEvent extends ResponseEvent {

	private int code = 0;
	public final static int OK = 0;
	public final static int NOT_OK = -1;
	private Set<WebSocketConnector> to = new FastSet<WebSocketConnector>();
	private String message;
	private int requestId;

	public WebSocketResponseEvent(int requestId) {
		super();

		this.requestId = requestId;
	}

	/**
	 * @return the code
	 */
	public int getCode() {
		return code;
	}

	/**
	 * @param code the code to set
	 */
	public void setCode(int code) {
		this.code = code;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the to
	 */
	public Set<WebSocketConnector> getTo() {
		return to;
	}

	/**
	 * @param to the to to set
	 */
	public void setTo(Set<WebSocketConnector> to) {
		this.to = to;
	}

	/**
	 * @return the requestId
	 */
	public int getRequestId() {
		return requestId;
	}

}
