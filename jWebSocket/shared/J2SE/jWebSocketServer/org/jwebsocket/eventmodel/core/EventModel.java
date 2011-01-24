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
package org.jwebsocket.eventmodel.core;

import org.jwebsocket.eventmodel.api.IEventModelFilter;
import org.jwebsocket.eventmodel.api.IEventModelPlugIn;
import org.jwebsocket.api.IInitializable;
import org.jwebsocket.eventmodel.api.IListener;
import org.jwebsocket.eventmodel.observable.ObservableObject;
import org.jwebsocket.eventmodel.observable.ResponseEvent;
import org.jwebsocket.eventmodel.factory.EventFactory;
import org.jwebsocket.eventmodel.event.WebSocketResponseEvent;
import org.jwebsocket.eventmodel.observable.Event;
import org.jwebsocket.eventmodel.event.WebSocketEvent;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.token.Token;
import org.jwebsocket.eventmodel.event.em.BeforeProcessEvent;
import org.jwebsocket.eventmodel.event.em.AfterProcessEvent;
import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;
import java.util.Set;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.eventmodel.api.IExceptionHandler;
import org.jwebsocket.eventmodel.event.WebSocketEventDefinition;
import org.jwebsocket.eventmodel.exception.CachedResponseException;
import org.jwebsocket.eventmodel.exception.ExceptionHandler;

/**
 *
 * @author kyberneees
 */
public class EventModel extends ObservableObject implements IInitializable, IListener {

	private Set<IEventModelFilter> filterChain;
	private Set<IEventModelPlugIn> plugIns;
	private TokenPlugIn parent;
	private EventFactory eventFactory;
	private static Logger mLog = Logging.getLogger(EventModel.class);
	private IExceptionHandler exceptionHandler;

	public void initialize() throws Exception {
	}

	/**
	 *
	 * @param aEvent
	 * @param aResponseEvent
	 */
	public void processEvent(WebSocketEvent aEvent, WebSocketResponseEvent aResponseEvent) {
		try {
			if (null == aResponseEvent) {
				aResponseEvent = getEventFactory().createResponseEvent(aEvent);
			}
			if (mLog.isInfoEnabled()) {
				mLog.info(">> Starting the 'event' workflow...");
			}

			executeFilters1erCall(aEvent.getConnector(), aEvent);

			//"before.process.event" notification
			if (mLog.isDebugEnabled()) {
				mLog.debug(">> 'before.process.event' notification...");
			}
			BeforeProcessEvent e = (BeforeProcessEvent) getEventFactory().stringToEvent("before.process.event");
			e.setEvent(aEvent);
			notify(e, null, true);

			//++++++++++++++ Listeners notification
			if (mLog.isDebugEnabled()) {
				mLog.debug(">> Executing EM listeners notifications...");
			}
			WebSocketEventDefinition def = getEventFactory().getEventDefinitions().getDefinition(aEvent.getId());
			if (def.isNotificationConcurrent()){
				notify(aEvent, aResponseEvent, true);
			}else {
				notify(aEvent, aResponseEvent, false);
			}

			//"after.process.event" notification
			if (mLog.isDebugEnabled()) {
				mLog.debug(">> 'after.process.event' notification...");
			}
			AfterProcessEvent e2 = (AfterProcessEvent) getEventFactory().stringToEvent("after.process.event");
			e2.setEvent(aEvent);
			notify(e2, aResponseEvent, true);

			executeFilters2ndCall(aEvent.getConnector(), aResponseEvent);

			if (mLog.isInfoEnabled()) {
				mLog.info(">> The 'event' workflow has finished successfully!");
			}
		} catch (CachedResponseException ex) {
			if (mLog.isInfoEnabled()) {
				mLog.info(">> The response was recovery from cache!");
				mLog.info(">> The 'event' workflow has finished successfully!");
			}
		} catch (Exception ex) {

			//Creating error response for connector notification
			Token aToken = getParent().getServer().createResponse(aEvent.getArgs());
			aToken.setInteger("code", WebSocketResponseEvent.NOT_OK);
			aToken.setString("msg", ex.toString());

			//Sending the error token...
			getParent().getServer().sendToken(aEvent.getConnector(), aToken);

			if (mLog.isInfoEnabled()) {
				mLog.info(">> The 'event' workflow has finished with errors: " + ex.toString());
			}

			//Calling the exception handler 'process' method
			ExceptionHandler.callProcessException(getExceptionHandler(), ex);

		}
	}

	/**
	 *
	 */
	public void shutdown() throws Exception {
	}

	/**
	 * Filter chain iteration. First call
	 *
	 * @param aConnector
	 * @param aEvent
	 * @throws Exception
	 */
	public void executeFilters1erCall(WebSocketConnector aConnector, WebSocketEvent aEvent) throws Exception {
		for (IEventModelFilter f : getFilterChain()) {
			f.firstCall(aConnector, aEvent);
		}
	}

	/**
	 * Filter chain iteration. Second call
	 * 
	 * @param aConnector
	 * @param aResponseEvent
	 * @throws Exception
	 */
	public void executeFilters2ndCall(WebSocketConnector aConnector, WebSocketResponseEvent aResponseEvent) throws Exception {
		int index = getFilterChain().size() - 1;
		while (index >= 0) {
			((IEventModelFilter) getFilterChain().
					toArray()[index]).secondCall(aConnector, aResponseEvent);
			index--;
		}
	}

	/**
	 *
	 * @param aPlugInId
	 * @return
	 * @throws IndexOutOfBoundsException
	 */
	public IEventModelPlugIn getPlugIn(String aPlugInId) throws IndexOutOfBoundsException {
		for (IEventModelPlugIn plugIn : getPlugIns()) {
			if (plugIn.getId().equals(aPlugInId)) {
				return plugIn;
			}
		}
		throw new IndexOutOfBoundsException("The plugIn with id: '" + aPlugInId + "', does not exists!");
	}

	/**
	 *
	 * @param aPlugInId
	 * @return
	 * @throws IndexOutOfBoundsException
	 */
	public IEventModelFilter getFilter(String aFilterId) throws IndexOutOfBoundsException {
		for (IEventModelFilter filter : getFilterChain()) {
			if (filter.getId().equals(aFilterId)) {
				return filter;
			}
		}
		throw new IndexOutOfBoundsException("The filter with id: " + aFilterId + ", does not exists!");
	}

	/**
	 * @return the filterChain
	 */
	public Set<IEventModelFilter> getFilterChain() {
		return filterChain;
	}

	/**
	 * @param filterChain the filterChain to set
	 */
	public void setFilterChain(Set<IEventModelFilter> filterChain) {
		this.filterChain = filterChain;
	}

	/**
	 * @return the plugIns
	 */
	public Set<IEventModelPlugIn> getPlugIns() {
		return plugIns;
	}

	/**
	 * @param plugIns the plugIns to set
	 */
	public void setPlugIns(Set<IEventModelPlugIn> plugIns) {
		this.plugIns = plugIns;
	}

	/**
	 * @return the parent
	 */
	public TokenPlugIn getParent() {
		return parent;
	}

	/**
	 * @param parent the parent to set
	 */
	public void setParent(TokenPlugIn parent) {
		this.parent = parent;
	}

	/**
	 * @return the eventFactory
	 */
	public EventFactory getEventFactory() {
		return eventFactory;
	}

	/**
	 * @param eventFactory the eventFactory to set
	 */
	public void setEventFactory(EventFactory eventFactory) {
		this.eventFactory = eventFactory;
	}

	/**
	 *
	 * @param aEvent
	 * @param aResponseEvent
	 */
	public void processEvent(Event aEvent, ResponseEvent aResponseEvent) {
		// IListener interface compatibility. Do not delete!
	}

	/**
	 * @param events the events to set
	 */
	@Override
	public void setEvents(Set<Class<? extends Event>> events) {
		addEvents(events);
	}

	/**
	 * @return the exceptionHandler
	 */
	public IExceptionHandler getExceptionHandler() {
		return exceptionHandler;
	}

	/**
	 * @param exceptionHandler the exceptionHandler to set
	 */
	public void setExceptionHandler(IExceptionHandler exceptionHandler) {
		this.exceptionHandler = exceptionHandler;
	}
}
