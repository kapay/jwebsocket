/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jWebSocket.plugins;

import java.util.Iterator;
import org.apache.log4j.Logger;
import org.jWebSocket.api.IWebSocketConnector;
import org.jWebSocket.api.IWebSocketServer;
import org.jWebSocket.token.Token;

/**
 *
 * @author aschulze
 */
public class TokenPlugInChain extends PlugInChain {

	private static Logger log = Logger.getLogger(TokenPlugInChain.class);

	public TokenPlugInChain(IWebSocketServer aServer) {
		super(aServer);
	}

	public PlugInResponse processToken(IWebSocketConnector aConnector, Token aToken) {
		PlugInResponse lPluginResponse = new PlugInResponse();
		for (IPlugIn plugIn : getPlugIns()) {
			try {
				((TokenPlugIn) plugIn).processToken(lPluginResponse, aConnector, aToken);
			} catch (Exception ex) {
				log.error("(plugin '" + ((TokenPlugIn) plugIn).getNamespace() + "')" + ex.getClass().getSimpleName() + ": " + ex.getMessage());
			}
			if (lPluginResponse.isChainAborted()) {
				break;
			}
		}
		return lPluginResponse;
	}
}
