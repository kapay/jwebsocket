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

import java.util.Random;
import org.apache.log4j.Logger;
import org.jWebSocket.api.IWebSocketConnector;
import org.jWebSocket.config.Config;
import org.jWebSocket.plugins.PlugInResponse;
import org.jWebSocket.plugins.TokenPlugIn;
import org.jWebSocket.server.TokenServer;
import org.jWebSocket.token.Token;
import org.jWebSocket.util.Tools;

/**
 * implements the jWebSocket system tokens like login, logout, send,
 * broadcast etc...
 * @author aschulze
 */
public class SystemPlugIn extends TokenPlugIn {

	private static Logger log = Logger.getLogger(SystemPlugIn.class);
	private static final String NS_SYSTEM_DEFAULT = Config.NS_BASE + ".plugins.system";
	private static final String VAR_SESSIONID = NS_SYSTEM_DEFAULT + ".sessionId";
	private static final String VAR_USERNAME = NS_SYSTEM_DEFAULT + ".username";
	private static final String VAR_GROUP = NS_SYSTEM_DEFAULT + ".group";

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
			if (lType.equals("login")) {
				login(aConnector, aToken);
				aResponse.abortChain();
			} else if (lType.equals("logout")) {
				logout(aConnector, aToken);
				aResponse.abortChain();
			} else if (lType.equals("getClients")) {
				getClients(aConnector, aToken);
				aResponse.abortChain();
			}
		}
	}

	@Override
	public void connectorStarted(IWebSocketConnector aConnector) {
		super.connectorStarted(aConnector);
		Random rand = new Random(System.nanoTime());
		setSessionId(aConnector, Tools.getMD5(aConnector.generateUID() + "." + rand.nextInt()));
		sendWelcomeToken(aConnector);
		broadcastConnectEvent();
	}

	private String getSessionId(IWebSocketConnector aConnector) {
		return aConnector.getString(VAR_SESSIONID);
	}

	private void setSessionId(IWebSocketConnector aConnector, String aSessionId) {
		aConnector.setString(VAR_SESSIONID, aSessionId);
	}

	private String getUsername(IWebSocketConnector aConnector) {
		return aConnector.getString(VAR_USERNAME);
	}

	private void setUsername(IWebSocketConnector aConnector, String aUsername) {
		aConnector.setString(VAR_USERNAME, aUsername);
	}

	private void removeUsername(IWebSocketConnector aConnector) {
		aConnector.removeVar(VAR_USERNAME);
	}

	private String getGroup(IWebSocketConnector aConnector) {
		return aConnector.getString(VAR_GROUP);
	}

	private void setGroup(IWebSocketConnector aConnector, String aGroup) {
		aConnector.setString(VAR_GROUP, aGroup);
	}

	private void removeGroup(IWebSocketConnector aConnector) {
		aConnector.removeVar(VAR_GROUP);
	}

	/**
	 *
	 */
	public void broadcastConnectEvent() {
		TokenServer lServer = getServer();
		// broadcast connect event to other clients of the jWebSocket network
		Token lEventToken = new Token("event");
		lEventToken.put("name", "connect");
		lEventToken.put("clientCount", getServer().getAllConnectors().size());

		lServer.broadcastToken(lEventToken);
	}

	private void sendWelcomeToken(IWebSocketConnector aConnector) {
		TokenServer lServer = getServer();

		// send "welcome" token to client
		Token lOutToken = new Token("welcome");
		lOutToken.put("vendor", Config.VENDOR);
		lOutToken.put("version", Config.VERSION_STR);
		lOutToken.put("usid", getSessionId(aConnector));
		/*
		try {
		lOutToken.put("timeout", this.getClientSocket().getSoTimeout());
		} catch (SocketException ex) {
		lOutToken.put("timeout", -1);
		}
		 */
		lServer.sendToken(aConnector, lOutToken);
	}

	private void login(IWebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		Token lResponseToken = lServer.createResponse(aToken);

		log.debug("Processing 'login'...");

		String lUsername = aToken.getString("username");
		String lPassword = aToken.getString("password");
		String lGroup = aToken.getString("group");
		if (lUsername != null) {
			log.debug("login " + lUsername);
			lResponseToken.put("username", lUsername);
			// set shared variables
			setUsername(aConnector, lUsername);
			setGroup(aConnector, lGroup);

			// broadcast "login event" to other clients
			// broadcastLoginEvent();
		} else {
			lResponseToken.put("code", -1);
			lResponseToken.put("msg", "missing arguments for 'login' command");
		}

		// send response to client
		lServer.sendToken(aConnector, lResponseToken);
	}

	private void logout(IWebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		Token lResponseToken = lServer.createResponse(aToken);

		log.debug("Processing 'logout'...");

		if (aConnector.getBoolean(NS_SYSTEM_DEFAULT + ".isLoggedIn")) {
			// send good bye token as response to client
			// sendGoodByeToken("logout");
			// and broadcast the logout event
			// broadcastLogoutEvent();
			// resetting the username is the only required signal for logout
			removeUsername(aConnector);
			removeGroup(aConnector);
		} else {
			lResponseToken.put("code", -1);
			lResponseToken.put("msg", "not logged in");
			lServer.sendToken(aConnector, lResponseToken);
		}
	}

	/**
	 *
	 * @param aToken
	 */
	private void echo(IWebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		Token lResponseToken = lServer.createResponse(aToken);

		String lData = aToken.getString("data");
		if (lData != null) {
			log.debug("echo " + lData);
		} else {
			lResponseToken.put("code", -1);
			lResponseToken.put("msg", "missing 'data' argument for 'echo' command");
		}
		lServer.sendToken(aConnector, lResponseToken);
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
			String lGroup = aToken.getString("group");
			Integer lMode = aToken.getInteger("mode", 0);
			// List lClients = lServer.getAllClients(lGROUP, lMode);
			// lResponseToken.put("clients", lClients);
			// lResponseToken.put("count", lClients.size());
		} else {
			lResponseToken.put("code", -1);
			lResponseToken.put("msg", "not logged in");
		}

		lServer.sendToken(aConnector, lResponseToken);
	}
}
