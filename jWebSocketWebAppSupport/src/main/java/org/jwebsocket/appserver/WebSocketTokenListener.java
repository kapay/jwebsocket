/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jwebsocket.appserver;

import org.jwebsocket.api.WebSocketListener;
import org.jwebsocket.kit.WebSocketEvent;
import org.jwebsocket.token.Token;

/**
 * This interface is used to be implemented within a servlet. It allows servlets
 * to process events from WebSocket clients
 * @author aschulze
 */
public interface WebSocketTokenListener extends WebSocketListener {

	/**
	 * This methods is called, when a token from a WebSocket token 
	 * client has been received.
	 */
	public void processWebSocketToken(WebSocketEvent aEvent, Token aToken);


}
