/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jWebSocket.client;

import org.jwebsocket.api.WebSocketClient;
import org.jwebsocket.kit.WebSocketException;

/**
 *
 * @author aschulze
 */
public abstract class BaseClient implements WebSocketClient {

	/*
	 * The connection has not yet been established.
	 */
	public static final int CONNECTING = 0;
	/*
	 * The WebSocket connection is established and communication is possible.
	 */
	public static final int OPEN = 1;
	/*
	 * The connection is going through the closing handshake.
	 */
	public static final int CLOSING = 2;
	/*
	 * The connection has been closed or could not be opened.
	 */
	public static final int CLOSED = 3;
	/*
	 * The maximum amount of bytes per frame
	 */
	public static final int MAX_FRAMESIZE = 16384;

}
