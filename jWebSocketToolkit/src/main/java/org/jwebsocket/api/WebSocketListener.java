/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.api;

import org.jwebsocket.kit.WebSocketEvent;

/**
 *
 * @author aschulze
 */
public interface WebSocketListener {

	public void processOpened(WebSocketEvent aEvent);

	public void processPacket(WebSocketEvent aEvent, WebSocketPacket aPacket);

	public void processClosed(WebSocketEvent aEvent);
}
