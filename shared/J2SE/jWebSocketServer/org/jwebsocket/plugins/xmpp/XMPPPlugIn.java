//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket XMPP/Jabber Plug-In
//  Copyright (c) 2010 Innotrade GmbH, jWebSocket.org
//	---------------------------------------------------------------------------
//  THIS CODE IS FOR RESEARCH, EVALUATION AND TEST PURPOSES ONLY!
//  THIS CODE MAY BE SUBJECT TO CHANGES WITHOUT ANY NOTIFICATION!
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
package org.jwebsocket.plugins.xmpp;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.Token;

/**
 *
 * @author aschulze
 *
 * This Plug-In make heavy use of Smack API 3.1.0
 * http://www.igniterealtime.org/projects/smack/
 *
 */
public class XMPPPlugIn extends TokenPlugIn {

	private static Logger mLog = Logging.getLogger(XMPPPlugIn.class);
	private static String XMPP_USER = null;
	private static final String XMPP_USER_KEY = "username";
	// if namespace changed update client plug-in accordingly!
	private static final String NS_XMPP = JWebSocketServerConstants.NS_BASE + ".plugins.xmpp";

	public XMPPPlugIn() {
		super(null);
	}

	public XMPPPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating XMPP plug-in...");
		}
		// specify default name space for xmpp plugin
		this.setNamespace(NS_XMPP);
		mGetSettings();
	}

	private void mGetSettings() {
		XMPP_USER = getSetting(XMPP_USER_KEY, null);
	}

	@Override
	public void processToken(PlugInResponse aResponse,
			WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();

		if (lType != null && (lNS == null || lNS.equals(getNamespace()))) {
			// select from database
			mGetSettings();
			if (lType.equals("login")) {
				login(aConnector, aToken);
			} else if (lType.equals("logout")) {
				logout(aConnector, aToken);
			}

		}
	}

	private boolean mCheckAuth(Token aToken) {
		String lMsg;
		try {
			if (mLog.isInfoEnabled()) {
				mLog.info("");
			}
			return true;
		} catch (Exception lEx) {
			lMsg = lEx.getClass().getSimpleName() + ": " + lEx.getMessage();
			aToken.setInteger("code", -1);
			aToken.setString("msg", lMsg);
			mLog.error(lMsg);
		}
		return false;
	}

	private void login(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		// instantiate response token
		Token lResponse = lServer.createResponse(aToken);
		String lMsg = "";

		String lUsername = aToken.getString("username");

		try {
			if (!mCheckAuth(lResponse)) {
				mLog.error(lResponse.getString("msg"));
			} else {
				// Create a connection to the xmpp server on a specific port.
				ConnectionConfiguration config = new ConnectionConfiguration("jabber.org", 5222);
				XMPPConnection conn2 = new XMPPConnection(config);
				conn2.connect();
				lMsg = "Successfully connect to XMPP server";
				lResponse.setString("msg", lMsg );
				if (mLog.isInfoEnabled()) {
					mLog.info(lMsg);
				}
			}
		} catch (Exception lEx) {
			lMsg = lEx.getClass().getSimpleName() + ": " + lEx.getMessage();
			mLog.error(lMsg);
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", lMsg);
		}

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}

	private void logout(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		// instantiate response token
		Token lResponse = lServer.createResponse(aToken);
		String lMsg;
		try {
			if (!mCheckAuth(lResponse)) {
				mLog.error(lResponse.getString("msg"));
			} else {
			}
		} catch (Exception lEx) {
			lMsg = lEx.getClass().getSimpleName() + ": " + lEx.getMessage();
			mLog.error(lMsg);
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", lMsg);
		}

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}
}
