//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket Administration Plug-In
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
package org.jwebsocket.plugins.admin;

import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.connectors.BaseConnector;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.security.SecurityFactory;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.Token;

/**
 *
 * @author aschulze
 */
public class AdminPlugIn extends TokenPlugIn {

	private static Logger log = Logging.getLogger(AdminPlugIn.class);
	// if namespace changed update client plug-in accordingly!
	private String NS_ADMIN = JWebSocketServerConstants.NS_BASE + ".plugins.admin";

	/**
	 *
	 */
	public AdminPlugIn() {
		if (log.isDebugEnabled()) {
			log.debug("Instantiating admin plug-in...");
		}
		// specify default name space for admin plugin
		this.setNamespace(NS_ADMIN);
	}

	@Override
	public void processToken(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();

		if (lType != null && (lNS == null || lNS.equals(getNamespace()))) {
			// remote shut down server
			if (lType.equals("shutdown")) {
				shutdown(aConnector, aToken);
			} else if (lType.equals("getConnections")) {
				getConnections(aConnector, aToken);
			}
		}
	}

	/**
	 * shutdown server
	 * @param aConnector
	 * @param aToken
	 */
	public void shutdown(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		if (log.isDebugEnabled()) {
			log.debug("Processing 'shutdown'...");
		}

		// check if user is allowed to run 'shutdown' command
		if (!SecurityFactory.checkRight(lServer.getUsername(aConnector), NS_ADMIN + ".shutdown")) {
			lServer.sendToken(aConnector, lServer.createAccessDenied(aToken));
			return;
		}

		Token lResponseToken = lServer.createResponse(aToken);
		lResponseToken.put("msg", "Shutdown in progress...");
		lServer.sendToken(aConnector, lResponseToken);

		for (WebSocketEngine lEngine : lServer.getEngines().values()) {
			try {
				lEngine.stopEngine(CloseReason.SHUTDOWN);
			} catch (WebSocketException ex) {
				log.error(ex.getClass().getSimpleName()
						+ " on shutdown: " + ex.getMessage());
			}
		}

	}

	/**
	 * return all session
	 * @param aConnector
	 * @param aToken
	 */
	public void getConnections(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		if (log.isDebugEnabled()) {
			log.debug("Processing 'getConnections'...");
		}

		// check if user is allowed to run 'shutdown' command
		/*
		if (!SecurityFactory.checkRight(lServer.getUsername(aConnector), NS_ADMIN + ".shutdown")) {
		lServer.sendToken(aConnector, lServer.createAccessDenied(aToken));
		return;
		}
		 */

		Token lResponse = lServer.createResponse(aToken);
		try {
			FastList<JSONObject> lResultList = new FastList<JSONObject>();
			FastMap lConnectors = lServer.getAllConnectors();
			for (FastMap.Entry<String, WebSocketConnector> lItem = lConnectors.head(), end = lConnectors.tail();
					(lItem = lItem.getNext()) != end;) {
				// String key = lItem.getKey(); 
				WebSocketConnector lConnector = lItem.getValue();
				// TODO: should work for for (sub-)tokens as well!
				JSONObject lResultItem = new JSONObject();
				lResultItem.put("port", lConnector.getRemotePort());
				lResultItem.put("usid", lConnector.getSession().getSessionId());
				lResultItem.put("username", lConnector.getString(BaseConnector.VAR_USERNAME));
				lResultItem.put("isToken", lConnector.getBoolean(TokenServer.VAR_IS_TOKENSERVER));
				lResultList.add(lResultItem);
			}
			lResponse.put("connections", lResultList);
		} catch (Exception ex) {
			log.error(ex.getClass().getSimpleName()
					+ " on getConnections: " + ex.getMessage());
		}

		lServer.sendToken(aConnector, lResponse);
	}
}
