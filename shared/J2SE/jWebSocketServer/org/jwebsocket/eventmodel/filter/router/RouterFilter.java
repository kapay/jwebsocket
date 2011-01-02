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
package org.jwebsocket.eventmodel.filter.router;

import org.jwebsocket.eventmodel.event.WebSocketEvent;
import org.jwebsocket.eventmodel.event.WebSocketResponseEvent;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.eventmodel.exception.ListenerNotFoundException;
import org.jwebsocket.eventmodel.filter.EventModelFilter;
import org.jwebsocket.token.Token;

import org.apache.log4j.Logger;
import org.jwebsocket.eventmodel.event.WebSocketEventDefinition;
import org.jwebsocket.eventmodel.event.filter.BeforeRouteResponseToken;
import org.jwebsocket.logging.Logging;

/**
 *
 ** @author kyberneees
 */
public class RouterFilter extends EventModelFilter {

	private static Logger mLog = Logging.getLogger(RouterFilter.class);

	@Override
	public void firstCall(WebSocketConnector aConnector, WebSocketEvent aEvent) throws Exception {
		if (mLog.isInfoEnabled()) {
			mLog.info(">> Checking if the event: '" + aEvent.getId() + "' has listener(s) in the server side...");
		}
		//If the incoming event has not listener, reject it!
		if (!getEm().hasListeners(aEvent.getClass())) {
			throw new ListenerNotFoundException("The incoming event '" + aEvent.getId() + "' has not listeners in the server side");
		}
	}

	@Override
	public void secondCall(WebSocketConnector aConnector, WebSocketResponseEvent aEvent) throws Exception {
		WebSocketEvent e = getEm().getEventFactory().stringToEvent(aEvent.getId());
		WebSocketEventDefinition def = getEm().getEventFactory().getEventDefinitions().getDefinition(e.getId());
		if (!def.isResponseRequired()) {
			return;
		}

		//Send the token to the client(s)
		Token aToken = aEvent.getArgs();
		aToken.setInteger("code", aEvent.getCode());
		aToken.setDouble("elapsedTime", (double) aEvent.getElapsedTime());
		aToken.setString("msg", aEvent.getMessage());

		//BeforeSendResponseToken event notification
		BeforeRouteResponseToken event = new BeforeRouteResponseToken(aEvent.getRequestId());
		event.setId("before.route.response.token");
		event.setSubject(this);
		event.setArgs(aToken);
		event.setEventDefinition(def);
		getEm().notify(event, null, true);

		//Sending the response
		if (mLog.isInfoEnabled()) {
			mLog.info(">> Sending the response for '" + aEvent.toString() + "' event to connectors...");
		}
		for (WebSocketConnector connector : aEvent.getTo()) {
			getEm().getParent().getServer().sendTokenAsync(connector, aToken);
		}
	}
}
