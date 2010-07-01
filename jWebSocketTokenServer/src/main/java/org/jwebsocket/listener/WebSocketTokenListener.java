/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.listener;

import org.jwebsocket.api.WebSocketListener;
import org.jwebsocket.kit.WebSocketEvent;
import org.jwebsocket.token.Token;

/**
 *
 * @author aschulze
 */
public interface WebSocketTokenListener extends WebSocketListener {

	public void processToken(WebSocketEvent aEvent, Token aToken);
}
