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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import org.apache.log4j.Logger;
import org.jWebSocket.api.WebSocketConnector;
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
	// specify name space for system plug-in
	private static final String NS_SYSTEM_DEFAULT = Config.NS_BASE + ".plugins.system";
	// specify token types processed by system plug-in
	private static final String TT_WELCOME = "welcome";
	private static final String TT_GOODBYE = "goodBye";
	private static final String TT_EVENT = "event";
	private static final String TT_LOGIN = "login";
	private static final String TT_LOGOUT = "logout";
	private static final String TT_CLOSE = "close";
	private static final String TT_GETCLIENTS = "getClients";
	private static final String TT_PING = "ping";
	// specify shared connector variables
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
	public void processToken(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();

		if (lType != null && (lNS == null || lNS.equals(getNamespace()))) {
			if (lType.equals(TT_LOGIN)) {
				login(aConnector, aToken);
				aResponse.abortChain();
			} else if (lType.equals(TT_LOGOUT)) {
				logout(aConnector, aToken);
				aResponse.abortChain();
			} else if (lType.equals(TT_CLOSE)) {
				close(aConnector, aToken);
				aResponse.abortChain();
			} else if (lType.equals(TT_GETCLIENTS)) {
				getClients(aConnector, aToken);
				aResponse.abortChain();
			} else if (lType.equals(TT_PING)) {
				ping(aConnector, aToken);
			}
		}
	}

	@Override
	public void connectorStarted(WebSocketConnector aConnector) {
		super.connectorStarted(aConnector);
		Random rand = new Random(System.nanoTime());
		setSessionId(aConnector, Tools.getMD5(aConnector.generateUID() + "." + rand.nextInt()));
		sendWelcome(aConnector);

		broadcastConnectEvent();
	}

	private String getSessionId(WebSocketConnector aConnector) {
		return aConnector.getString(VAR_SESSIONID);
	}

	private void setSessionId(WebSocketConnector aConnector, String aSessionId) {
		aConnector.setString(VAR_SESSIONID, aSessionId);
	}

	private String getUsername(WebSocketConnector aConnector) {
		return aConnector.getString(VAR_USERNAME);
	}

	private void setUsername(WebSocketConnector aConnector, String aUsername) {
		aConnector.setString(VAR_USERNAME, aUsername);
	}

	private void removeUsername(WebSocketConnector aConnector) {
		aConnector.removeVar(VAR_USERNAME);
	}

	private String getGroup(WebSocketConnector aConnector) {
		return aConnector.getString(VAR_GROUP);
	}

	private void setGroup(WebSocketConnector aConnector, String aGroup) {
		aConnector.setString(VAR_GROUP, aGroup);
	}

	private void removeGroup(WebSocketConnector aConnector) {
		aConnector.removeVar(VAR_GROUP);
	}

	/**
	 *
	 */
	public void broadcastConnectEvent() {
		log.debug("Broadcasting connect...");
		TokenServer lServer = getServer();

		// broadcast connect event to other clients of the jWebSocket network
		Token lEventToken = new Token(TT_EVENT);
		lEventToken.put("name", "connect");
		lEventToken.put("clientCount", lServer.getAllConnectors().size());

		lServer.broadcastToken(lEventToken);
	}

	/**
	 *
	 */
	public void broadcastDisconnectEvent() {
		log.debug("Broadcasting disconnect...");
		TokenServer lServer = getServer();

		// broadcast connect event to other clients of the jWebSocket network
		Token lEventToken = new Token(TT_EVENT);
		lEventToken.put("name", "disconnect");
		lEventToken.put("clientCount", lServer.getAllConnectors().size());

		lServer.broadcastToken(lEventToken);
	}

	private void sendWelcome(WebSocketConnector aConnector) {
		log.debug("Sending welcome...");
		TokenServer lServer = getServer();

		// send "welcome" token to client
		Token lWelcome = new Token(TT_WELCOME);
		lWelcome.put("vendor", Config.VENDOR);
		lWelcome.put("version", Config.VERSION_STR);
		lWelcome.put("usid", getSessionId(aConnector));
		/*
		try {
		lOutToken.put("timeout", this.getClientSocket().getSoTimeout());
		} catch (SocketException ex) {
		lOutToken.put("timeout", -1);
		}
		 */
		lServer.sendToken(aConnector, lWelcome);
	}

	/**
	 *
	 */
	private void broadcastLoginEvent(WebSocketConnector aConnector) {
		log.debug("Broadcasting login event...");
		TokenServer lServer = getServer();

		// broadcast login event to other clients of the jWebSocket network
		Token lEvent = new Token(TT_EVENT);
		lEvent.put("name", "login");
		lEvent.put("username", getUsername(aConnector));
		lEvent.put("clientCount", lServer.getAllConnectors().size());

		lServer.broadcastToken(lEvent);
	}

	/**
	 *
	 */
	private void broadcastLogoutEvent(WebSocketConnector aConnector) {
		log.debug("Broadcasting logout event...");
		TokenServer lServer = getServer();

		// broadcast login event to other clients of the jWebSocket network
		Token lEvent = new Token(TT_EVENT);
		lEvent.put("name", "logout");
		lEvent.put("username", getUsername(aConnector));
		lEvent.put("clientCount", lServer.getAllConnectors().size());

		lServer.broadcastToken(lEvent);
	}

	/**
	 *
	 * @param aConnector
	 * @param aReason
	 */
	private void sendGoodBye(WebSocketConnector aConnector, String aReason) {
		log.debug("Sending good bye...");
		TokenServer lServer = getServer();

		// send "goodBye" token to client
		Token lGoodBye = new Token(TT_GOODBYE);
		lGoodBye.put("vendor", Config.VENDOR);
		lGoodBye.put("version", Config.VERSION_STR);
		lGoodBye.put("usid", getSessionId(aConnector));
		// lGoodBye.put("port", this.getClientSocket().getPort());
		if (aReason != null) {
			lGoodBye.put("reason", aReason);
		}

		lServer.sendToken(aConnector, lGoodBye);
	}

	private void login(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		Token lResponseToken = lServer.createResponse(aToken);

		String lUsername = aToken.getString("username");
		// TODO: Add authentication and password check
		String lPassword = aToken.getString("password");
		String lGroup = aToken.getString("group");

		log.debug("Processing 'login' (username='" + lUsername + "', group='" + lGroup + "') from '" + aConnector + "'...");

		if (lUsername != null) {
			lResponseToken.put("username", lUsername);
			// set shared variables
			setUsername(aConnector, lUsername);
			setGroup(aConnector, lGroup);
		} else {
			lResponseToken.put("code", -1);
			lResponseToken.put("msg", "missing arguments for 'login' command");
		}

		// send response to client
		lServer.sendToken(aConnector, lResponseToken);

		// if successfully logged in...
		if (lUsername != null) {
			// broadcast "login event" to other clients
			broadcastLoginEvent(aConnector);
		}
	}

	private void logout(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		Token lResponse = lServer.createResponse(aToken);

		log.debug("Processing 'logout' (username='" + getUsername(aConnector) + "') from '" + aConnector + "'...");

		if (getUsername(aConnector) != null) {
			// send good bye token as response to client
			sendGoodBye(aConnector, "logout");
			// and broadcast the logout event
			broadcastLogoutEvent(aConnector);
			// resetting the username is the only required signal for logout
			removeUsername(aConnector);
			removeGroup(aConnector);
		} else {
			lResponse.put("code", -1);
			lResponse.put("msg", "not logged in");
			lServer.sendToken(aConnector, lResponse);
		}
	}

	private void close(WebSocketConnector aConnector, Token aToken) {
		Integer lTimeout = aToken.getInteger("timeout", 0);
		// if timeout > 0 send a good bye token to the client
		if (lTimeout > 0) {
			// send good bye token
			sendGoodBye(aConnector, "close");
		}
		if (getUsername(aConnector) != null) {
			// and broadcast the logout event
			broadcastLogoutEvent(aConnector);
		}
		// reset the username, we're no longer logged in
		removeUsername(aConnector);

		// broadcast disconnect event to other clients
		broadcastDisconnectEvent();

		log.debug("Closing client...");
		// terminate();
		// don't send a response here! We're about to close the connection!
	}

	/**
	 *
	 * @param aToken
	 */
	private void echo(WebSocketConnector aConnector, Token aToken) {
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
	public void ping(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		String lEcho = aToken.getString("echo");

		log.debug("Processing 'Ping' (echo='" + lEcho + "') from '" + aConnector + "'...");

		if (lEcho.equalsIgnoreCase("true")) {
			Token lResponseToken = lServer.createResponse(aToken);
			// TODO: here could optionally send a time stamp
			// TODO: implement response time on client!
			// lResponseToken.put("","");
			lServer.sendToken(aConnector, lResponseToken);
		}
	}

	/**
	 *
	 * @param aConnector
	 * @param aToken
	 */
	public void getClients(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		Token lResponseToken = lServer.createResponse(aToken);

		log.debug("Processing 'getClients' from '" + aConnector + "'...");

		if (getUsername(aConnector) != null) {
			String lGroup = aToken.getString("group");
			Integer lMode = aToken.getInteger("mode", 0);
			HashMap lFilter = new HashMap();
			lFilter.put(VAR_USERNAME, ".*");
			List<String> listOut = new ArrayList();
			for (WebSocketConnector lConnector : lServer.selectConnectors(lFilter)) {
				listOut.add(getUsername(lConnector) + "@" + lConnector.getRemotePort());
			}
			lResponseToken.put("clients", listOut);
			lResponseToken.put("count", listOut.size());
		} else {
			lResponseToken.put("code", -1);
			lResponseToken.put("msg", "not logged in");
		}

		lServer.sendToken(aConnector, lResponseToken);
	}
}
