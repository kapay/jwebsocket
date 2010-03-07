//	---------------------------------------------------------------------------
//	jWebSocket - Chain of Token Plug-Ins
//	Copyright (c) 2010 Alexander Schulze, Innotrade GmbH
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
//	more details.
//	You should have received a copy of the GNU General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/>.
//	---------------------------------------------------------------------------
package org.jwebsocket.plugins;

import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketServer;
import org.jwebsocket.token.Token;

/**
 *
 * @author aschulze
 */
public class TokenPlugInChain extends BasePlugInChain {

	private static Logger log = Logger.getLogger(TokenPlugInChain.class);

	public TokenPlugInChain(WebSocketServer aServer) {
		super(aServer);
	}

	public PlugInResponse processToken(WebSocketConnector aConnector, Token aToken) {
		PlugInResponse lPluginResponse = new PlugInResponse();
		for (PlugIn plugIn : getPlugIns()) {
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
