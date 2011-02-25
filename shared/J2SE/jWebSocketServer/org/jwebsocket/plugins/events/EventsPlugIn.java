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

import java.util.Set;
import javolution.util.FastSet;
import org.jwebsocket.logging.Logging;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.PlugInResponse;

import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.token.Token;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.eventmodel.api.IWebSocketSecureObject;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.eventmodel.core.EventModel;
import org.jwebsocket.eventmodel.event.em.ConnectorStarted;
import org.jwebsocket.eventmodel.event.em.ConnectorStopped;
import org.jwebsocket.eventmodel.event.em.EngineStarted;
import org.jwebsocket.eventmodel.event.em.EngineStopped;
import org.jwebsocket.eventmodel.event.WebSocketEvent;
import org.jwebsocket.eventmodel.util.EmConstants;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.FileSystemResource;

/**
 *
 * @author kyberneees
 */
public class EventsPlugIn extends TokenPlugIn implements IWebSocketSecureObject {

	private String xmlConfigFile;
	private EventModel em;
	private static BeanFactory beanFactory;
	private static Logger mLog = Logging.getLogger(EventsPlugIn.class);
	//IWebSocketSecureObject fields
	private boolean securityEnabled = false;
	private Set<String> roles = new FastSet<String>();
	private Set<String> users = new FastSet<String>();
	private Set<String> ipAddresses = new FastSet<String>();

	/**
	 * @return the beanFactory
	 */
	public static BeanFactory getBeanFactory() {
		return beanFactory;
	}

	/**
	 * @param aBeanFactory the beanFactory to set
	 */
	public static void setBeanFactory(BeanFactory aBeanFactory) {
		beanFactory = aBeanFactory;
	}

	public EventsPlugIn(PluginConfiguration configuration) throws Exception {
		super(configuration);
		if (mLog.isDebugEnabled()) {
			mLog.debug(">> Instantiating events plug-in...");
		}
		this.setNamespace(JWebSocketServerConstants.NS_BASE + ".plugins.events");

		// Loading configuration
		JSONObject config = getJSON("config", new JSONObject());
		// previous code: new JSONObject(getSetting("config"));

		//Setting fields values
		xmlConfigFile = config.getString("xml_config");
		if (config.has("security_enabled")) {
			securityEnabled = config.getBoolean("security_enabled");
		}
		if (config.has("ip_addresses")) {
			JSONArray ips = config.getJSONArray("ip_addresses");
			for (int i = 0; i < ips.length(); i++) {
				ipAddresses.add(ips.get(i).toString());
			}
		}
		if (config.has("roles")) {
			JSONArray r = config.getJSONArray("roles");
			for (int i = 0; i < r.length(); i++) {
				roles.add(r.get(i).toString());
			}
		}
		if (config.has("users")) {
			JSONArray u = config.getJSONArray("users");
			for (int i = 0; i < u.length(); i++) {
				users.add(u.get(i).toString());
			}
		}

		//Calling the init method
		initialize();
	}

	public void initialize() {
		try {
			//Creating the Spring Bean Factory
			String path = JWebSocketConfig.getConfigurationPath().subSequence(0,
					JWebSocketConfig.getConfigurationPath().length() - 14).toString();
			BeanFactory factory = new XmlBeanFactory(
					new FileSystemResource(path + "/" + getXmlConfigFile()));
			setBeanFactory(factory);

			//Getting the EventModel service instance
			setEm((EventModel) getBeanFactory().getBean("EventModel"));

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
			//Nothing. Just is not an Event ;)
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
	 * @return the securityEnabled
	 */
	public boolean isSecurityEnabled() {
		return securityEnabled;
	}

	/**
	 * @param securityEnabled the securityEnabled to set
	 */
	public void setSecurityEnabled(boolean securityEnabled) {
		this.securityEnabled = securityEnabled;
	}

	/**
	 * @return the roles
	 */
	public Set<String> getRoles() {
		return roles;
	}

	/**
	 * @param roles the roles to set
	 */
	public void setRoles(Set<String> roles) {
		this.roles.addAll(roles);
	}

	/**
	 * @return the users
	 */
	public Set<String> getUsers() {
		return users;
	}

	/**
	 * @param users the users to set
	 */
	public void setUsers(Set<String> users) {
		this.users.addAll(users);
	}

	/**
	 * @return the ipAddresses
	 */
	public Set<String> getIpAddresses() {
		return ipAddresses;
	}

	/**
	 * @param ipAddresses the ipAddresses to set
	 */
	public void setIpAddresses(Set<String> ipAddresses) {
		this.ipAddresses.addAll(ipAddresses);
	}

	/**
	 * @return the xmlConfigFile
	 */
	public String getXmlConfigFile() {
		return xmlConfigFile;
	}

	/**
	 * @param xmlConfigFile the xmlConfigFile to set
	 */
	public void setXmlConfigFile(String xmlConfigFile) {
		this.xmlConfigFile = xmlConfigFile;
	}
}
