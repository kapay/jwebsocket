/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jWebSocket.client;

/**
 *
 * @author aschulze
 */
public interface WebSocketListener {

	public void processOpened(WebSocketEvent aEvent);

	public void processPacket(WebSocketEvent aEvent);

	public void processClosed(WebSocketEvent aEvent);
}
