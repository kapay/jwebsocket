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
package org.jwebsocket.eventmodel.factory;

import org.jwebsocket.eventmodel.event.WebSocketEvent;
import org.jwebsocket.eventmodel.event.WebSocketResponseEvent;
import org.jwebsocket.token.Token;
import org.jwebsocket.eventmodel.event.EventDefinitionManager;
import org.jwebsocket.logging.Logging;
import org.apache.log4j.Logger;
import org.jwebsocket.eventmodel.core.EventModel;
import org.jwebsocket.eventmodel.observable.Event;

/**
 *
 ** @author kyberneees
 */
public class EventFactory {

	private EventModel em;
	private EventDefinitionManager eventDefinitions;
	private static Logger mLog = Logging.getLogger(EventFactory.class);

	public Token eventToToken(Event aEvent) {
		return aEvent.getArgs();
	}

	public WebSocketEvent tokenToEvent(Token aToken) throws Exception {
		String aType = aToken.getType();
		WebSocketEvent event = stringToEvent(aType);
		event.setSubject(aToken.getNS());
		event.setArgs(aToken);

		return event;
	}

	public WebSocketEvent stringToEvent(String aEventId) throws Exception {
		if (mLog.isDebugEnabled()) {
			mLog.debug(">> Creating instance for event: '" + aEventId + "'...");
		}

		WebSocketEvent e = (WebSocketEvent)getEventDefinitions().getDefinition(aEventId).getEventClass().newInstance();
		e.setId(aEventId);

		return e;
	}

	public String eventToString(WebSocketEvent aEvent) {
		return aEvent.getId();
	}

	public String eventToString(Class<? extends Event> aEventClass) throws Exception {
		return getEventDefinitions().getIdByClass(aEventClass);
	}

	public WebSocketResponseEvent createResponseEvent(WebSocketEvent aEvent) {
		if (mLog.isDebugEnabled()) {
			mLog.debug(">> Creating instance for response event: '" + aEvent.getId() + "'...");
		}
		WebSocketResponseEvent aResponse = new WebSocketResponseEvent(aEvent.getRequestId());
		aResponse.setId(aEvent.getId());
		aResponse.setArgs(getEm().getParent().getServer().createResponse(aEvent.getArgs()));

		return aResponse;
	}

	public boolean hasEvent(String aEventId) {
		return getEventDefinitions().hasDefinition(aEventId);
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
	 * @return the eventDefinitions
	 */
	public EventDefinitionManager getEventDefinitions() {
		return eventDefinitions;
	}

	/**
	 * @param eventDefinitions the eventDefinitions to set
	 */
	public void setEventDefinitions(EventDefinitionManager eventDefinitions) {
		this.eventDefinitions = eventDefinitions;
	}
}
