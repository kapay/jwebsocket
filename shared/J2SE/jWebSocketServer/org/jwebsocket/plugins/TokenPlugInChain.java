//	---------------------------------------------------------------------------
//	jWebSocket - Chain of Token Plug-Ins
//	Copyright (c) 2010 Alexander Schulze, Innotrade GmbH
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
package org.jwebsocket.plugins;

import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.api.WebSocketPlugIn;
import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketServer;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.token.Token;

/**
 * instantiates the chain of token plug-ins.
 * @author aschulze
 */
public class TokenPlugInChain extends BasePlugInChain {

	private static Logger mLog = Logging.getLogger(TokenPlugInChain.class);

	/**
	 *
	 * @param aServer
	 */
	public TokenPlugInChain(WebSocketServer aServer) {
		super(aServer);
	}

	/**
	 *
	 * @param aConnector
	 * @param aToken
	 * @return
	 */
	public PlugInResponse processToken(WebSocketConnector aConnector, Token aToken) {
		PlugInResponse lPluginResponse = new PlugInResponse();
		for (WebSocketPlugIn lPlugIn : getPlugIns()) {
			// TODO: introduce optimization: only pass token to plug-ins that match the token name space!
			try {
				((TokenPlugIn) lPlugIn).processToken(lPluginResponse, aConnector, aToken);
			} catch (Exception lEx) {
				mLog.error("(plugin '"
						+ ((TokenPlugIn) lPlugIn).getNamespace() + "')"
						+ lEx.getClass().getSimpleName() + ": "
						+ lEx.getMessage());
			}
			if (lPluginResponse.isChainAborted()) {
				break;
			}
		}
		return lPluginResponse;
	}
}
