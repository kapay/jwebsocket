/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.kit;

import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketServer;

/**
 *
 * @author aschulze
 */
public class WebSocketEvent {

	private WebSocketServer server = null;
	private WebSocketConnector connector = null;

	public WebSocketEvent(WebSocketConnector aConnector, WebSocketServer aServer) {
		connector = aConnector;
		server = aServer;
	}

	/**
	 * @return the sessionId
	 */
	public String getSessionId() {
		return connector.getSession().getSessionId();
	}

	/**
	 * @return the session
	 */
	public WebSocketSession getSession() {
		return connector.getSession();
	}

	/**
	 * @return the server
	 */
	public WebSocketServer getServer() {
		return server;
	}

	/**
	 * @return the connector
	 */
	public WebSocketConnector getConnector() {
		return connector;
	}
}
