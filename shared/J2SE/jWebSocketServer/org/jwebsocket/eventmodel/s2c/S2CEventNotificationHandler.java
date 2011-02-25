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
package org.jwebsocket.eventmodel.s2c;

import java.util.Collection;
import java.util.Map;
import org.apache.log4j.Logger;
import javolution.util.FastMap;
import org.jwebsocket.api.IInitializable;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.eventmodel.api.IListener;
import org.jwebsocket.eventmodel.api.IS2COnResponse;
import org.jwebsocket.eventmodel.core.EventModel;
import org.jwebsocket.eventmodel.event.S2CEvent;
import org.jwebsocket.eventmodel.event.WebSocketResponseEvent;
import org.jwebsocket.eventmodel.event.em.ConnectorStopped;
import org.jwebsocket.eventmodel.event.system.S2CEventNotSupportedOnClient;
import org.jwebsocket.eventmodel.event.system.S2CResponse;
import org.jwebsocket.eventmodel.filter.validator.TypesMap;
import org.jwebsocket.eventmodel.observable.Event;
import org.jwebsocket.eventmodel.observable.ResponseEvent;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 *
 * @author kyberneees
 */
public class S2CEventNotificationHandler implements IInitializable, IListener {

	private static Logger mLog = Logging.getLogger(S2CEventNotificationHandler.class);
	private Integer uid = 0;
	private EventModel em;
	private TypesMap typesMap;
	private FastMap<String, FastMap<String, IS2COnResponse>> callsMap = new FastMap<String, FastMap<String, IS2COnResponse>>();

	public void send(S2CEvent aEvent, WebSocketConnector to, IS2COnResponse aOnResponse) {
		if (mLog.isDebugEnabled()) {
			mLog.debug(">> Preparing S2C event notification...");
		}

		if (!getCallsMap().containsKey(to.getId())) {
			getCallsMap().put(to.getId(), new FastMap<String, IS2COnResponse>());
		}

		//Creating the token
		Token token = TokenFactory.createToken("s2c.event_notification");
		aEvent.writeToToken(token);
		token.setString("uid", getNextUID());

		//Saving the callback
		if (aEvent.getResponseType() != null) {
			if (mLog.isDebugEnabled()) {
				mLog.debug(">> Saving the OnResponse callback for the event '" + aEvent.getId() + "'...");
			}
			//Saving the callback
			aOnResponse.setRequiredType(aEvent.getResponseType());
			getCallsMap().get(to.getId()).put(token.getString("uid"), aOnResponse);
		}

		//Sending the token
		if (mLog.isDebugEnabled()) {
			mLog.debug(">> Sending S2C event notification to '" + to.getId() + "' connector...");
		}
		getEm().getParent().getServer().sendToken(to, token);
	}

	public void send(S2CEvent aEvent, Collection<WebSocketConnector> to, IS2COnResponse aOnResponse) {
		for (WebSocketConnector c : to) {
			send(aEvent, c, aOnResponse);
		}
	}

	/**
	 * Executes the OnResponse callback appropiate method when a Response is gotted from the client
	 *
	 * @param aEvent
	 * @throws Exception
	 */
	public void processEvent(S2CResponse aEvent, WebSocketResponseEvent aResponseEvent) throws Exception {
		if (mLog.isDebugEnabled()) {
			mLog.debug(">> Processing S2CResponse from '" + aEvent.getConnector().getId() + "' connector...");
		}
		//Getting the response
		if (aEvent.getArgs().getMap().containsKey("response")) {
			aEvent.setResponse(aEvent.getArgs().getObject("response"));
		}
		String id = aEvent.getConnector().getId();

		//Getting the OnResponse callback
		IS2COnResponse aOnResponse = getCallsMap().get(id).get(aEvent.getReqId());

		//Removing the callback
		getCallsMap().get(id).remove(aEvent.getReqId());
		if (getCallsMap().get(id).isEmpty()) {
			getCallsMap().remove(id);
		}

		//Executing...
		if (getTypesMap().swapType(aOnResponse.getRequiredType()).isInstance(aEvent.getResponse())) {
			aOnResponse.success(aEvent.getResponse());
		} else {
			aOnResponse.failure(FailureReason.INVALID_RESPONSE_TYPE);
		}

	}

	/**
	 * Removing pending callbacks when a client gets disconnected
	 *
	 * @param aEvent
	 * @param aResponseEvent
	 */
	public void processEvent(ConnectorStopped aEvent, ResponseEvent aResponseEvent) {
		if (getCallsMap().containsKey(aEvent.getConnector().getId())) {
			if (mLog.isDebugEnabled()) {
				mLog.debug(">> Removing pending callbacks for '" + aEvent.getConnector().getId() + "' connector...");
			}
			//Getting pending callbacks
			FastMap<String, IS2COnResponse> pending_calls = getCallsMap().get(aEvent.getConnector().getId());
			//Removing from the queue
			getCallsMap().remove(aEvent.getConnector().getId());

			for (Map.Entry<String, IS2COnResponse> e : pending_calls.entrySet()) {
				//Calling the failure method
				e.getValue().failure(FailureReason.CONNECTOR_STOPPED);
			}
		}
	}

	/**
	 * Event fired when the client does not support the S2C event from the server
	 *
	 * @param aEvent
	 * @param aResponseEvent
	 */
	public void processEvent(S2CEventNotSupportedOnClient aEvent, WebSocketResponseEvent aResponseEvent) {
		if (mLog.isDebugEnabled()) {
			mLog.debug(">> Processing the 'S2CEventNotSupportedOnClient' event...");
		}

		//Caching the connector id for performance
		String connector_id = aEvent.getConnector().getId();

		//Removing only if a callback is pending
		if (getCallsMap().containsKey(connector_id)
				&& getCallsMap().get(connector_id).containsKey(aEvent.getReqId())) {
			if (mLog.isDebugEnabled()) {
				mLog.debug(">> Removing pending callback for '" + aEvent.getId() + "' event. Client does not support it!...");
			}

			//Getting the callback
			IS2COnResponse aOnResponse = getCallsMap().get(connector_id).get(aEvent.getReqId());

			//Removing the callback
			getCallsMap().get(connector_id).remove(aEvent.getReqId());

			//Calling the failure method
			aOnResponse.failure(FailureReason.EVENT_NOT_SUPPORTED_BY_CLIENT);
		}
	}

	/**
	 * @return the callsMap
	 */
	public FastMap<String, FastMap<String, IS2COnResponse>> getCallsMap() {
		return callsMap;
	}

	/**
	 * @param callsMap the callsMap to set
	 */
	public void setCallsMap(FastMap<String, FastMap<String, IS2COnResponse>> callsMap) {
		this.callsMap = callsMap;
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
	 * @return the typesMap
	 */
	public TypesMap getTypesMap() {
		return typesMap;
	}

	/**
	 * @param typesMap the typesMap to set
	 */
	public void setTypesMap(TypesMap typesMap) {
		this.typesMap = typesMap;
	}

	synchronized public String getNextUID() {
		if (uid == Integer.MAX_VALUE) {
			uid = 0;
			return Integer.toString(uid);
		}
		uid += 1;
		return Integer.toString(uid);
	}

	public void initialize() throws Exception {
		//Listening this events
		getEm().on(ConnectorStopped.class, this);
		getEm().on(S2CResponse.class, this);
		getEm().on(S2CEventNotSupportedOnClient.class, this);
	}

	public void shutdown() throws Exception {
	}

	public void processEvent(Event aEvent, ResponseEvent aResponseEvent) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
