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
package org.jwebsocket.plugins.events;

import org.jwebsocket.logging.Logging;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.PlugInResponse;

import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.token.Token;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.eventmodel.core.EventModel;
import org.jwebsocket.eventmodel.event.em.ConnectorStarted;
import org.jwebsocket.eventmodel.event.em.ConnectorStopped;
import org.jwebsocket.eventmodel.event.em.EngineStarted;
import org.jwebsocket.eventmodel.event.em.EngineStopped;
import org.jwebsocket.eventmodel.event.WebSocketEvent;
import org.jwebsocket.eventmodel.util.EmConstants;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 * @author kyberneees
 */
public class EventsPlugIn extends TokenPlugIn {

	private String xmlConfigPath;
	private EventModel em;
	private static ApplicationContext context;
	private static Logger mLog = Logging.getLogger(EventsPlugIn.class);

	public EventsPlugIn(PluginConfiguration configuration) {
		super(configuration);
		if (mLog.isDebugEnabled()) {
			mLog.debug(">> Instantiating events plug-in...");
		}
		this.setNamespace(JWebSocketServerConstants.NS_BASE + ".plugins.events");
		setXmlConfigPath(getString("SPRING_XML_CONFIG_PATH"));

		initialize();
	}

	public void initialize() {
		try {
			//Creating the Spring context
			setContext(new ClassPathXmlApplicationContext(getXmlConfigPath()));

			//Getting the EventModel service instance
			setEm((EventModel) getContext().getBean("EventModel"));

			//Initializing the event model
			getEm().setParent(this);
			getEm().initialize();
		} catch (Exception ex) {
			mLog.error(ex.toString(), ex);
		}
	}

	@Override
	public void engineStarted(WebSocketEngine aEngine) {
		//Engine started event notification
		try {
			if (mLog.isDebugEnabled()) {
				mLog.debug(">> 'engine.started(" + aEngine.toString() + ")' event notification...");
			}
			EngineStarted e = (EngineStarted) getEm().getEventFactory().stringToEvent("engine.started");
			e.setEngine(aEngine);
			e.initialize();
			getEm().notify(e, null, true);
		} catch (Exception ex) {
			mLog.error(ex.toString(), ex);
		}
	}

	@Override
	public void engineStopped(WebSocketEngine aEngine) {
		//Engine started event notification
		try {
			if (mLog.isDebugEnabled()) {
				mLog.debug(">> 'engine.stopped(" + aEngine.toString() + ")' event notification...");
			}
			EngineStopped e = (EngineStopped) getEm().getEventFactory().stringToEvent("engine.stopped");
			e.setEngine(aEngine);
			e.initialize();
			getEm().notify(e, null, true);
		} catch (Exception ex) {
			mLog.error(ex.toString(), ex);
		}
	}

	@Override
	public void connectorStarted(WebSocketConnector aConnector) {
		//Connector started event notification
		try {
			if (mLog.isDebugEnabled()) {
				mLog.debug(">> 'connector.started(" + aConnector.toString() + ")' event notification...");
			}
			ConnectorStarted e = (ConnectorStarted) getEm().getEventFactory().stringToEvent("connector.started");
			e.setConnector(aConnector);
			e.initialize();
			getEm().notify(e, null, true);
		} catch (Exception ex) {
			mLog.error(ex.toString(), ex);
		}
	}

	@Override
	public void processToken(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {
		boolean isEm = false;
		try {
			isEm = aToken.getBoolean(EmConstants.IS_EVENT_MODEL);
		} catch (Exception ex1) {
			//Nothing. Just is not a Event ;)
		}
		if (isEm) {
			try {
				if (mLog.isDebugEnabled()) {
					mLog.debug(">> Processing event: '" + aToken.toString() + "'...");
				}
				aToken.remove(EmConstants.IS_EVENT_MODEL);
				WebSocketEvent e = getEm().getEventFactory().tokenToEvent(aToken);
				e.setConnector(aConnector);
				e.initialize();
				processEvent(aConnector, e);
				aResponse.abortChain();
			} catch (Exception ex) {
				mLog.error(ex.toString(), ex);
			}
		}
	}

	public void processEvent(WebSocketConnector aConnector, WebSocketEvent aEvent) {
		if (mLog.isDebugEnabled()) {
			mLog.debug(">> EventModel initialization... ");
		}
		aEvent.setConnector(aConnector);
		getEm().processEvent(aEvent, null);
	}

	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
		//Connector stopped event notification
		try {
			if (mLog.isDebugEnabled()) {
				mLog.debug(">> 'connector.stopped(" + aConnector.toString() + ")' event notification...");
			}
			ConnectorStopped e = (ConnectorStopped) getEm().getEventFactory().stringToEvent("connector.stopped");
			e.setConnector(aConnector);
			e.setCloseReason(aCloseReason);
			e.initialize();
			getEm().notify(e, null, true);
		} catch (Exception ex) {
			mLog.error(ex.toString(), ex);
		}
	}

	/**
	 * @return the xmlConfigPath
	 */
	public String getXmlConfigPath() {
		return xmlConfigPath;
	}

	/**
	 * @param xmlConfigPath the xmlConfigPath to set
	 */
	public void setXmlConfigPath(String xmlConfigPath) {
		this.xmlConfigPath = xmlConfigPath;
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
	 * @return the context
	 */
	public static ApplicationContext getContext() {
		return context;
	}

	/**
	 * @param context the context to set
	 */
	public void setContext(ApplicationContext aContext) {
		context = aContext;
	}
}
