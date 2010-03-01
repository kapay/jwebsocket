/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jWebSocket.plugins;

import java.util.Iterator;
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

	public PlugInResponse processToken(IWebSocketConnector aConnector, Token aToken) {
		PlugInResponse lPluginResponse = new PlugInResponse();
		for (Iterator<IPlugIn> i = getPlugIns().iterator(); i.hasNext();) {
			((TokenPlugIn)i.next()).processToken(lPluginResponse, aConnector, aToken);
			if (lPluginResponse.isChainAborted()) {
				break;
			}
		}
		return lPluginResponse;
	}

	/**
	 * @return the server
	 */
	@Override
	public TokenServer getServer() {
		return (TokenServer)super.getServer();
	}
}
