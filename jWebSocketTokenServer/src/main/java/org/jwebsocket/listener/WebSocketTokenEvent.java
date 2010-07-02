/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.listener;

import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketServer;
import org.jwebsocket.kit.WebSocketEvent;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.Token;

/**
 *
 * @author aschulze
 */
public class WebSocketTokenEvent extends WebSocketEvent {

	/**
	 * 
	 * @param aConnector
	 * @param aServer
	 */
	public WebSocketTokenEvent(WebSocketConnector aConnector, WebSocketServer aServer) {
		super(aConnector, aServer);
	}

	/**
	 *
	 * @param aToken
	 */
	public void sendToken(Token aToken) {
		((TokenServer)getServer()).sendToken(getConnector(), aToken);
	}
}
