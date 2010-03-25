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
package org.jwebsocket.plugins.system;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketConstants;
import org.jwebsocket.connectors.BaseConnector;
import org.jwebsocket.kit.BroadcastOptions;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.PlugInResponse;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.Token;
import org.jwebsocket.util.Tools;

/**
 * implements the jWebSocket system tokens like login, logout, send,
 * broadcast etc...
 * @author aschulze
 */
public class SystemPlugIn extends TokenPlugIn {

	private static Logger log = Logging.getLogger(SystemPlugIn.class);
	// specify name space for system plug-in
	private static final String NS_SYSTEM_DEFAULT = JWebSocketConstants.NS_BASE + ".plugins.system";
	// specify token types processed by system plug-in
	private static final String TT_SEND = "send";
	private static final String TT_BROADCAST = "broadcast";
	private static final String TT_WELCOME = "welcome";
	private static final String TT_GOODBYE = "goodBye";
	private static final String TT_EVENT = "event";
	private static final String TT_LOGIN = "login";
	private static final String TT_LOGOUT = "logout";
	private static final String TT_CLOSE = "close";
	private static final String TT_GETCLIENTS = "getClients";
	private static final String TT_PING = "ping";
	// specify shared connector variables
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
			if (lType.equals(TT_SEND)) {
				send(aConnector, aToken);
				aResponse.abortChain();
			} else if (lType.equals(TT_BROADCAST)) {
				broadcast(aConnector, aToken);
				aResponse.abortChain();
			} else if (lType.equals(TT_LOGIN)) {
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

		broadcastConnectEvent(aConnector);
	}

	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
		super.connectorStopped(aConnector, aCloseReason);
		// send good bye message to connector
		sendGoodBye(aConnector, aCloseReason);
		// notify other clients that client disconnected
		broadcastDisconnectEvent(aConnector);
	}

	private String getSessionId(WebSocketConnector aConnector) {
		return aConnector.getString(BaseConnector.VAR_SESSIONID);
	}

	private void setSessionId(WebSocketConnector aConnector, String aSessionId) {
		aConnector.setString(BaseConnector.VAR_SESSIONID, aSessionId);
	}

	private String getUsername(WebSocketConnector aConnector) {
		return aConnector.getString(BaseConnector.VAR_USERNAME);
	}

	private void setUsername(WebSocketConnector aConnector, String aUsername) {
		aConnector.setString(BaseConnector.VAR_USERNAME, aUsername);
	}

	private void removeUsername(WebSocketConnector aConnector) {
		aConnector.removeVar(BaseConnector.VAR_USERNAME);
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
	 *
	 * @param aConnector
	 */
	public void broadcastConnectEvent(WebSocketConnector aConnector) {
		if (log.isDebugEnabled()) {
			log.debug("Broadcasting connect...");
		}
		TokenServer lServer = getServer();

		// broadcast connect event to other clients of the jWebSocket network
		Token lConnect = new Token(TT_EVENT);
		lConnect.put("name", "connect");
		// lConnect.put("usid", getSessionId(aConnector));
		lConnect.put("sourceId", aConnector.getId());
		lConnect.put("clientCount", lServer.getAllConnectors().size());

		// broadcast to all except source
		lServer.broadcastToken(aConnector, lConnect);
	}

	/**
	 *
	 *
	 * @param aConnector
	 */
	public void broadcastDisconnectEvent(WebSocketConnector aConnector) {
		if (log.isDebugEnabled()) {
			log.debug("Broadcasting disconnect...");
		}
		TokenServer lServer = getServer();

		// broadcast connect event to other clients of the jWebSocket network
		Token lDisconnect = new Token(TT_EVENT);
		lDisconnect.put("name", "disconnect");
		// lDisconnect.put("usid", getSessionId(aConnector));
		lDisconnect.put("sourceId", aConnector.getId());
		lDisconnect.put("clientCount", lServer.getAllConnectors().size());

		// broadcast to all except source
		lServer.broadcastToken(aConnector, lDisconnect);
	}

	private void sendWelcome(WebSocketConnector aConnector) {
		if (log.isDebugEnabled()) {
			log.debug("Sending welcome...");
		}
		TokenServer lServer = getServer();

		// send "welcome" token to client
		Token lWelcome = new Token(TT_WELCOME);
		lWelcome.put("vendor", JWebSocketConstants.VENDOR);
		lWelcome.put("version", JWebSocketConstants.VERSION_STR);
		// here the session id is MANDATORY! to pass to the client!
		lWelcome.put("usid", getSessionId(aConnector));
		lWelcome.put("sourceId", aConnector.getId());
		lWelcome.put("timeout", aConnector.getEngine().getSessionTimeout());

		lServer.sendToken(aConnector, lWelcome);
	}

	/**
	 *
	 */
	private void broadcastLoginEvent(WebSocketConnector aConnector) {
		if (log.isDebugEnabled()) {
			log.debug("Broadcasting login event...");
		}
		TokenServer lServer = getServer();

		// broadcast login event to other clients of the jWebSocket network
		Token lLogin = new Token(TT_EVENT);
		lLogin.put("name", "login");
		lLogin.put("username", getUsername(aConnector));
		lLogin.put("clientCount", lServer.getAllConnectors().size());
		// lLogin.put("usid", getSessionId(aConnector));
		lLogin.put("sourceId", aConnector.getId());

		// broadcast to all except source
		lServer.broadcastToken(aConnector, lLogin);
	}

	/**
	 *
	 */
	private void broadcastLogoutEvent(WebSocketConnector aConnector) {
		if (log.isDebugEnabled()) {
			log.debug("Broadcasting logout event...");
		}
		TokenServer lServer = getServer();

		// broadcast login event to other clients of the jWebSocket network
		Token lLogout = new Token(TT_EVENT);
		lLogout.put("name", "logout");
		lLogout.put("username", getUsername(aConnector));
		lLogout.put("clientCount", lServer.getAllConnectors().size());
		// lLogout.put("usid", getSessionId(aConnector));
		lLogout.put("sourceId", aConnector.getId());

		// broadcast to all except source
		lServer.broadcastToken(aConnector, lLogout);
	}

	/**
	 *
	 * @param aConnector
	 * @param aCloseReason
	 */
	private void sendGoodBye(WebSocketConnector aConnector, CloseReason aCloseReason) {
		if (log.isDebugEnabled()) {
			log.debug("Sending good bye...");
		}
		TokenServer lServer = getServer();

		// send "goodBye" token to client
		Token lGoodBye = new Token(TT_GOODBYE);
		lGoodBye.put("vendor", JWebSocketConstants.VENDOR);
		lGoodBye.put("version", JWebSocketConstants.VERSION_STR);
		lGoodBye.put("sourceId", aConnector.getId());
		if (aCloseReason != null) {
			lGoodBye.put("reason", aCloseReason.toString().toLowerCase());
		}

		// don't send session-id on good bye, neither required nor desired
		lServer.sendToken(aConnector, lGoodBye);
	}

	private void login(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		Token lResponse = lServer.createResponse(aToken);

		String lUsername = aToken.getString("username");
		// TODO: Add authentication and password check
		String lPassword = aToken.getString("password");
		String lGroup = aToken.getString("group");

		if (log.isDebugEnabled()) {
			log.debug("Processing 'login' (username='" + lUsername + "', group='" + lGroup + "') from '" + aConnector + "'...");
		}

		if (lUsername != null) {
			lResponse.put("username", lUsername);
			// lResponse.put("usid", getSessionId(aConnector));
			lResponse.put("sourceId", aConnector.getId());
			// set shared variables
			setUsername(aConnector, lUsername);
			setGroup(aConnector, lGroup);
		} else {
			lResponse.put("code", -1);
			lResponse.put("msg", "missing arguments for 'login' command");
		}

		// send response to client
		lServer.sendToken(aConnector, lResponse);

		// if successfully logged in...
		if (lUsername != null) {
			// broadcast "login event" to other clients
			broadcastLoginEvent(aConnector);
		}
	}

	private void logout(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		Token lResponse = lServer.createResponse(aToken);

		if (log.isDebugEnabled()) {
			log.debug("Processing 'logout' (username='" + getUsername(aConnector) + "') from '" + aConnector + "'...");
		}

		if (getUsername(aConnector) != null) {
			// send good bye token as response to client
			sendGoodBye(aConnector, CloseReason.CLIENT);
			// and broadcast the logout event
			broadcastLogoutEvent(aConnector);
			// resetting the username is the only required signal for logout
			// lResponse.put("usid", getSessionId(aConnector));
			lResponse.put("sourceId", aConnector.getId());
			removeUsername(aConnector);
			removeGroup(aConnector);
		} else {
			lResponse.put("code", -1);
			lResponse.put("msg", "not logged in");
			lServer.sendToken(aConnector, lResponse);
		}
	}

	private void send(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		Token lResponse = lServer.createResponse(aToken);

		// get the target
		String lTargetId = aToken.getString("targetId");

		if (log.isDebugEnabled()) {
			log.debug("Processing 'send' (username='" + getUsername(aConnector) + "') from '" + aConnector + "' to " + lTargetId + "...");
		}

		// TODO: find solutions for hardcoded engine id
		WebSocketConnector lTargetConnector =
			lServer.getConnector("tcp0", lTargetId);
		/*
		if (getUsername(aConnector) != null) {
		 */
		if (lTargetConnector != null) {
			aToken.put("sourceId", aConnector.getId());
			lServer.sendToken(lTargetConnector, aToken);
		} else {
			log.warn("Target connector '" + lTargetId + "' not found.");
		}
		/*
		} else {
		lResponse.put("code", -1);
		lResponse.put("msg", "not logged in");
		lServer.sendToken(aConnector, lResponse);
		}
		 */
	}

	private void broadcast(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		Token lResponse = lServer.createResponse(aToken);

		if (log.isDebugEnabled()) {
			log.debug("Processing 'broadcast' (username='" + getUsername(aConnector) + "') from '" + aConnector + "'...");
		}
		/*
		if (getUsername(aConnector) != null) {
		 */
		aToken.put("sourceId", aConnector.getId());
		// don't distribute session id here!
		aToken.remove("usid");
		String lSenderIncluded = aToken.getString("senderIncluded");
		String lResponseRequested = aToken.getString("responseRequested");
		boolean bSenderIncluded = (lSenderIncluded != null
			&& lSenderIncluded.equals("true"));
		boolean bResponseRequested = (lResponseRequested != null
			&& lResponseRequested.equals("true"));
		lServer.broadcastToken(aConnector, aToken,
			new BroadcastOptions(bSenderIncluded, bResponseRequested));
		if (bResponseRequested) {
			lServer.sendToken(aConnector, lResponse);
		}
		/*
		} else {
		lResponse.put("code", -1);
		lResponse.put("msg", "not logged in");
		lServer.sendToken(aConnector, lResponse);
		}
		 */
	}

	private void close(WebSocketConnector aConnector, Token aToken) {
		// if logged in...
		if (getUsername(aConnector) != null) {
			// broadcast the logout event.
			broadcastLogoutEvent(aConnector);
		}
		// reset the username, we're no longer logged in
		removeUsername(aConnector);

		if (log.isDebugEnabled()) {
			log.debug("Closing client...");
		}

		// don't send a response here! We're about to close the connection!
		// broadcasts disconnect event to other clients
		aConnector.stopConnector(CloseReason.CLIENT);
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
			if (log.isDebugEnabled()) {
				log.debug("echo " + lData);
			}
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

		if (log.isDebugEnabled()) {
			log.debug("Processing 'Ping' (echo='" + lEcho + "') from '" + aConnector + "'...");
		}

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

		if (log.isDebugEnabled()) {
			log.debug("Processing 'getClients' from '" + aConnector + "'...");
		}

		if (getUsername(aConnector) != null) {
			String lGroup = aToken.getString("group");
			Integer lMode = aToken.getInteger("mode", 0);
			HashMap lFilter = new HashMap();
			lFilter.put(BaseConnector.VAR_USERNAME, ".*");
			List<String> listOut = new ArrayList<String>();
			for (WebSocketConnector lConnector : lServer.selectConnectors(lFilter).values()) {
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
