/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jWebSocket.client;

import org.jwebsocket.api.WebSocketPaket;
import org.jwebsocket.token.Token;

/**
 *
 * @author aschulze
 */
public class TokenClient extends TCPClient {

	/**
	 *
	 * @param aHost
	 * @param aPort
	 */
	public TokenClient(String aHost, int aPort) {
		super(aHost, aPort);
	}

	/**
	 *
	 * @param aToken
	 */
	public void sendToken(Token aToken) {

	}

	/**
	 *
	 * @param aToken
	 */
	public void processToken(Token aToken) {

	}

	@Override
	public void processPacket(WebSocketPaket aPacket) {

		
	}
}
