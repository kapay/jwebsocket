/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.kit;

/**
 *
 * @author aschulze
 */
public class WebSocketEvent {

	private WebSocketSession session = null;

	public WebSocketEvent(WebSocketSession aSession) {
		session = aSession;
	}

	/**
	 * @return the sessionId
	 */
	public String getSessionId() {
		return session.getSessionId();
	}

	/**
	 * @param sessionId the sessionId to set
	 */
	public void setSessionId(String sessionId) {
		session.setSessionId(sessionId);
	}

	/**
	 * @return the session
	 */
	public WebSocketSession getSession() {
		return session;
	}

	/**
	 * @param session the session to set
	 */
	public void setSession(WebSocketSession session) {
		this.session = session;
	}
}
