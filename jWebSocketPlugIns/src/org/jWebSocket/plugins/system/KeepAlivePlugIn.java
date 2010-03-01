//	---------------------------------------------------------------------------
//	jWebSocket - RPC Plug-In
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
package org.jWebSocket.plugins.system;

import org.apache.log4j.Logger;
import org.jWebSocket.api.IWebSocketConnector;
import org.jWebSocket.config.Config;
import org.jWebSocket.plugins.PlugInResponse;
import org.jWebSocket.plugins.TokenPlugIn;
import org.jWebSocket.server.TokenServer;
import org.jWebSocket.token.Token;

/**
 *
 * @author aschulze
 */
public class KeepAlivePlugIn extends TokenPlugIn {

	private static Logger log = Logger.getLogger(KeepAlivePlugIn.class);
	private String NS_KEEPALIVE_DEFAULT = Config.NS_BASE + ".plugins.keepAlive";

	/**
	 *
	 */
	public KeepAlivePlugIn() {
		// specify default name space for keep alive plugin
		this.setNamespace(NS_KEEPALIVE_DEFAULT);
	}

	@Override
	public void processToken(PlugInResponse aResponse, IWebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();

		if (lType != null && (lNS == null || lNS.equals(getNamespace()))) {
			if (lType.equals("ping")) {
				ping(aConnector, aToken);
			}
		}
	}

	/**
	 *
	 * @param aConnector
	 * @param aToken
	 */
	public void ping(IWebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		String lEcho = aToken.getString("echo");

		log.debug("Processing 'Ping' (echo='" + lEcho + "')...");

		if (lEcho.equalsIgnoreCase("true")) {
			Token lResponseToken = lServer.createResponse(aToken);
			// todo: here could optionally send a time stamp
			// lResponseToken.put("","");
			lServer.sendToken(aConnector, lResponseToken);
		}
	}
}
