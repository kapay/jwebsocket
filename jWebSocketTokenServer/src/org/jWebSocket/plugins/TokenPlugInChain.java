/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jWebSocket.plugins;

import org.jWebSocket.api.IWebSocketConnector;
import org.jWebSocket.api.IWebSocketServer;
import org.jWebSocket.server.TokenServer;
import org.jWebSocket.token.Token;

/**
 *
 * @author aschulze
 */
public class TokenPlugInChain extends PlugInChain {

	public TokenPlugInChain(IWebSocketServer aServer) {
		super(aServer);
	}

	public void processToken(IWebSocketConnector aConnector, Token aToken) {
		
	}

	/**
	 * @return the server
	 */
	@Override
	public TokenServer getServer() {
		return (TokenServer)super.getServer();
	}
}
