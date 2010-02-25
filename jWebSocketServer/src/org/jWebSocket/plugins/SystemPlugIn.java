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
package org.jWebSocket.plugins;

import java.util.List;
import org.apache.log4j.Logger;
import org.jWebSocket.config.Config;
import org.jWebSocket.connectors.BaseConnector;
import org.jWebSocket.connectors.TokenConnector;
import org.jWebSocket.server.Token;

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
	public void connectorStarted(BaseConnector aConnector) {
	}

	;

	@Override
	public void processToken(PlugInResponse aAction, TokenConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();

		if (lType != null && (lNS == null || lNS.equals(getNamespace()))) {
			if (lType.equals("getClients")) {
				getClients(aConnector, aToken);
				aAction.abortChain();
			}
		}
	}

	@Override
	public void connectorTerminated(BaseConnector aConnector) {
	}

	;

	/**
	 *
	 * @param aConnector
	 * @param aToken
	 */
	public void getClients(TokenConnector aConnector, Token aToken) {
		Token lResponseToken = aConnector.createResponse(aToken);

		log.debug("Processing 'getClients'...");

		if (aConnector.isLoggedIn()) {
			String lPool = aToken.getString("pool");
			Integer lMode = aToken.getInteger("mode", 0);
			List lClients = aConnector.getTokenServer().getClients(lPool, lMode);
			lResponseToken.put("clients", lClients);
			lResponseToken.put("count", lClients.size());
		} else {
			lResponseToken.put("code", -1);
			lResponseToken.put("msg", "not logged in");
		}

		aConnector.sendResponse(lResponseToken);
	}
}
