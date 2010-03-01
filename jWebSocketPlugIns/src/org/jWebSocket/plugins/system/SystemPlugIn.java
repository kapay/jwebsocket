//	---------------------------------------------------------------------------
//	jWebSocket - The jWebSocket SystemPlugIn Listener
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
 * implements the jWebSocket system tokens like login, logout, send,
 * broadcast etc...
 * @author aschulze
 */
public class SystemPlugIn extends TokenPlugIn {

	private static Logger log = Logger.getLogger(SystemPlugIn.class);
	private static String NS_SYSTEM_DEFAULT = Config.NS_BASE + ".plugins.system";

	/**
	 *
	 */
	public SystemPlugIn() {
		// specify default name space for system plugin
		this.setNamespace(NS_SYSTEM_DEFAULT);
	}

	@Override
	public void processToken(PlugInResponse aResponse, IWebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();

		if (lType != null && (lNS == null || lNS.equals(getNamespace()))) {
			if (lType.equals("getClients")) {
				getClients(aConnector, aToken);
				aResponse.abortChain();
			}
		}
	}

	/**
	 *
	 * @param aConnector
	 * @param aToken
	 */
	public void getClients(IWebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		Token lResponseToken = lServer.createResponse(aToken);

		log.debug("Processing 'getClients'...");

		if (aConnector.getBoolean(NS_SYSTEM_DEFAULT + ".isLoggedIn")) {
			String lPool = aToken.getString("pool");
			Integer lMode = aToken.getInteger("mode", 0);
			// List lClients = lServer.getAllClients(lPool, lMode);
			// lResponseToken.put("clients", lClients);
			// lResponseToken.put("count", lClients.size());
		} else {
			lResponseToken.put("code", -1);
			lResponseToken.put("msg", "not logged in");
		}

		lServer.sendToken(aConnector, lResponseToken);
	}
}
