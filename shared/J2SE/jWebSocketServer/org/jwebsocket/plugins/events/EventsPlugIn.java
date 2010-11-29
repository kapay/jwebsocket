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
import org.jwebsocket.eventmodel.observable.Event;
import org.jwebsocket.eventmodel.context.EventModel;
import org.jwebsocket.eventmodel.events.WebSocketEvent;
import org.jwebsocket.eventmodel.util.EmConstants;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 * @author itachi
 */
public class EventsPlugIn extends TokenPlugIn {

	private String xmlConfigPath;
	private EventModel em;
	private ApplicationContext context;
	private static Logger mLog = Logging.getLogger(EventsPlugIn.class);
	

	public EventsPlugIn(PluginConfiguration configuration) {
		super(configuration);
		if (mLog.isDebugEnabled()) {
			mLog.debug(">> Instantiating events plug-in...");
		}
		this.setNamespace(JWebSocketServerConstants.NS_BASE + ".plugins.events");
		setXmlConfigPath(getSetting("SPRING_XML_CONFIG_PATH"));

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
			Event e = getEm().getEventFactory().stringToEvent("engine.started");
			e.setSubject(this);
			e.getArgs().put(EmConstants.ENGINE_KEY, aEngine);
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
			Event e = getEm().getEventFactory().stringToEvent("engine.stopped");
			e.setSubject(this);
			e.getArgs().put(EmConstants.ENGINE_KEY, aEngine);
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
			Event e = getEm().getEventFactory().stringToEvent("connector.started");
			e.setSubject(this);
			e.getArgs().put(EmConstants.CONNECTOR_KEY, aConnector);
			getEm().notify(e, null, true);
		} catch (Exception ex) {
			mLog.error(ex.toString(), ex);
		}
	}

	@Override
	public void processToken(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {
		try {
			if (aToken.getBoolean(EmConstants.IS_EVENT_MODEL)) {
				if (mLog.isDebugEnabled()) {
					mLog.debug(">> Processing event: '" + aToken.toString() + "'...");
				}
				aToken.remove(EmConstants.IS_EVENT_MODEL);
				Event e = getEm().getEventFactory().tokenToEvent(aToken);
				processEvent(aConnector, (WebSocketEvent) e);
			}
			aResponse.abortChain();
		} catch (Exception ex) {
			mLog.error(ex.toString(), ex);
		}
	}

	public void processEvent(WebSocketConnector aConnector, WebSocketEvent aEvent) {
		if (mLog.isDebugEnabled()) {
			mLog.debug(">> EventModel initialization... ");
		}
		aEvent.getArgs().put(EmConstants.CONNECTOR_KEY, aConnector);
		getEm().processEvent(aEvent, null);
	}

	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
		//Connector stopped event notification
		try {
			if (mLog.isDebugEnabled()) {
				mLog.debug(">> 'connector.stopped(" + aConnector.toString() + ")' event notification...");
			}
			Event e = getEm().getEventFactory().stringToEvent("connector.stopped");
			e.setSubject(this);
			e.getArgs().put(EmConstants.CONNECTOR_KEY, aConnector);
			e.getArgs().put("closeReason", aCloseReason);
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
	public ApplicationContext getContext() {
		return context;
	}

	/**
	 * @param context the context to set
	 */
	public void setContext(ApplicationContext context) {
		this.context = context;
	}
}
