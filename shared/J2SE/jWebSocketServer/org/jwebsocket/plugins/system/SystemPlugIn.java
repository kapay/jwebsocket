//	---------------------------------------------------------------------------
//	jWebSocket - The jWebSocket System Plug-In
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
package org.jwebsocket.plugins.system;

import java.util.List;
import java.util.Random;
import javolution.util.FastList;
import javolution.util.FastMap;

import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.connectors.BaseConnector;
import org.jwebsocket.kit.BroadcastOptions;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.security.SecurityFactory;
import org.jwebsocket.security.User;
import org.jwebsocket.token.BaseToken;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.util.Tools;

/**
 * implements the jWebSocket system tokens like login, logout, send, broadcast
 * etc...
 * 
 * @author aschulze
 */
public class SystemPlugIn extends TokenPlugIn {

	private static Logger log = Logging.getLogger(SystemPlugIn.class);
	// specify name space for system plug-in
	private static final String NS_SYSTEM_DEFAULT = JWebSocketServerConstants.NS_BASE + ".plugins.system";
	// specify token types processed by system plug-in
	private static final String TT_SEND = "send";
	private static final String TT_RESPOND = "respond";
	private static final String TT_BROADCAST = "broadcast";
	private static final String TT_WELCOME = "welcome";
	private static final String TT_GOODBYE = "goodBye";
	private static final String TT_LOGIN = "login";
	private static final String TT_LOGOUT = "logout";
	private static final String TT_CLOSE = "close";
	private static final String TT_GETCLIENTS = "getClients";
	private static final String TT_PING = "ping";
	private static final String TT_ECHO = "echo";
	private static final String TT_WAIT = "wait";
	private static final String TT_ALLOC_CHANNEL = "alloc";
	private static final String TT_DEALLOC_CHANNEL = "dealloc";
	// specify shared connector variables
	private static final String VAR_GROUP = NS_SYSTEM_DEFAULT + ".group";
	private static boolean BROADCAST_OPEN = true;
	private static final String BROADCAST_OPEN_KEY = "broadcastOpenEvent";
	private static boolean BROADCAST_CLOSE = true;
	private static final String BROADCAST_CLOSE_KEY = "broadcastCloseEvent";
	private static boolean BROADCAST_LOGIN = true;
	private static final String BROADCAST_LOGIN_KEY = "broadcastLoginEvent";
	private static boolean BROADCAST_LOGOUT = true;
	private static final String BROADCAST_LOGOUT_KEY = "broadcastLogoutEvent";

	/**
	 * Constructor with configuration object
	 */
	public SystemPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		if (log.isDebugEnabled()) {
			log.debug("Instantiating system plug-in...");
		}
		// specify default name space for system plugin
		this.setNamespace(NS_SYSTEM_DEFAULT);
		mGetSettings();
	}

	private void mGetSettings() {
		// load global settings, default to "true"
		BROADCAST_OPEN = "true".equals(getString(BROADCAST_OPEN_KEY, "true"));
		BROADCAST_CLOSE = "true".equals(getString(BROADCAST_CLOSE_KEY, "true"));
		BROADCAST_LOGIN = "true".equals(getString(BROADCAST_LOGIN_KEY, "true"));
		BROADCAST_LOGOUT = "true".equals(getString(BROADCAST_LOGOUT_KEY, "true"));
	}

	@Override
	public void processToken(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();

		if (lType != null && getNamespace().equals(lNS)) {
			if (lType.equals(TT_SEND)) {
				send(aConnector, aToken);
				aResponse.abortChain();
			} else if (lType.equals(TT_RESPOND)) {
				respond(aConnector, aToken);
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
			} else if (lType.equals(TT_ECHO)) {
				echo(aConnector, aToken);
			} else if (lType.equals(TT_WAIT)) {
				wait(aConnector, aToken);
			} else if (lType.equals(TT_ALLOC_CHANNEL)) {
				allocChannel(aConnector, aToken);
			} else if (lType.equals(TT_DEALLOC_CHANNEL)) {
				deallocChannel(aConnector, aToken);
			}
		}
	}

	@Override
	public void connectorStarted(WebSocketConnector aConnector) {
		// set session id first, so that it can be processed in the connectorStarted
		// method
		Random lRand = new Random(System.nanoTime());

		// TODO: if unique node id is passed check if already assigned in the
		// network and reject connect if so!

		aConnector.getSession().setSessionId(
				Tools.getMD5(aConnector.generateUID() + "." + lRand.nextInt()));

		// and send the welcome message incl. the session id
		sendWelcome(aConnector);
		// if new connector is active broadcast this event to then network
		broadcastConnectEvent(aConnector);
	}

	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
		// notify other clients that client disconnected
		broadcastDisconnectEvent(aConnector);
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
		// only broadcast if corresponding global plugin setting is "true"
		if (BROADCAST_OPEN) {
			if (log.isDebugEnabled()) {
				log.debug("Broadcasting connect...");
			}
			// broadcast connect event to other clients of the jWebSocket network
			Token lConnect = TokenFactory.createToken(BaseToken.TT_EVENT);
			lConnect.setString("name", "connect");
			// lConnect.put("usid", getSessionId(aConnector));
			lConnect.setString("sourceId", aConnector.getId());
			// if a unique node id is specified for the client include that
			String lNodeId = aConnector.getNodeId();
			if (lNodeId != null) {
				lConnect.setString("unid", lNodeId);
			}
			lConnect.setInteger("clientCount", getConnectorCount());

			// broadcast to all except source
			broadcastToken(aConnector, lConnect);
		}
	}

	/**
	 *
	 *
	 * @param aConnector
	 */
	public void broadcastDisconnectEvent(WebSocketConnector aConnector) {
		// only broadcast if corresponding global plugin setting is "true"
		if (BROADCAST_CLOSE
				&& !aConnector.getBoolean("noDisconnectBroadcast")) {
			if (log.isDebugEnabled()) {
				log.debug("Broadcasting disconnect...");
			}
			// broadcast connect event to other clients of the jWebSocket network
			Token lDisconnect = TokenFactory.createToken(BaseToken.TT_EVENT);
			lDisconnect.setString("name", "disconnect");
			// lDisconnect.put("usid", getSessionId(aConnector));
			lDisconnect.setString("sourceId", aConnector.getId());
			// if a unique node id is specified for the client include that
			String lNodeId = aConnector.getNodeId();
			if (lNodeId != null) {
				lDisconnect.setString("unid", lNodeId);
			}
			lDisconnect.setInteger("clientCount", getConnectorCount());

			// broadcast to all except source
			broadcastToken(aConnector, lDisconnect);
		}
	}

	private void sendWelcome(WebSocketConnector aConnector) {
		if (log.isDebugEnabled()) {
			log.debug("Sending welcome...");
		}
		// send "welcome" token to client
		Token lWelcome = TokenFactory.createToken(TT_WELCOME);
		lWelcome.setString("ns", getNamespace());
		lWelcome.setString("vendor", JWebSocketCommonConstants.VENDOR);
		lWelcome.setString("version", JWebSocketServerConstants.VERSION_STR);
		// here the session id is MANDATORY! to pass to the client!
		lWelcome.setString("usid", aConnector.getSession().getSessionId());
		lWelcome.setString("sourceId", aConnector.getId());
		// if a unique node id is specified for the client include that
		String lNodeId = aConnector.getNodeId();
		if (lNodeId != null) {
			lWelcome.setString("unid", lNodeId);
		}
		lWelcome.setInteger("timeout", aConnector.getEngine().getConfiguration().getTimeout());

		sendToken(aConnector, aConnector, lWelcome);
	}

	/**
	 *
	 */
	private void broadcastLoginEvent(WebSocketConnector aConnector) {
		// only broadcast if corresponding global plugin setting is "true"
		if (BROADCAST_LOGIN) {
			if (log.isDebugEnabled()) {
				log.debug("Broadcasting login event...");
			}
			// broadcast login event to other clients of the jWebSocket network
			Token lLogin = TokenFactory.createToken(BaseToken.TT_EVENT);
			lLogin.setString("name", "login");
			lLogin.setString("username", getUsername(aConnector));
			lLogin.setInteger("clientCount", getConnectorCount());
			// do NEVER broadcast client's session id here!
			// lLogin.put("usid", getSessionId(aConnector));
			lLogin.setString("sourceId", aConnector.getId());
			// if a unique node id is specified for the client include that
			String lNodeId = aConnector.getNodeId();
			if (lNodeId != null) {
				lLogin.setString("unid", lNodeId);
			}
			// broadcast to all except source
			broadcastToken(aConnector, lLogin);
		}
	}

	/**
	 *
	 */
	private void broadcastLogoutEvent(WebSocketConnector aConnector) {
		// only broadcast if corresponding global plugin setting is "true"
		if (BROADCAST_LOGOUT) {
			if (log.isDebugEnabled()) {
				log.debug("Broadcasting logout event...");
			}
			// broadcast login event to other clients of the jWebSocket network
			Token lLogout = TokenFactory.createToken(BaseToken.TT_EVENT);
			lLogout.setString("ns", getNamespace());
			lLogout.setString("name", "logout");
			lLogout.setString("username", getUsername(aConnector));
			lLogout.setInteger("clientCount", getConnectorCount());
			// do NEVER broadcast client's session id here!
			// lLogout.put("usid", getSessionId(aConnector));
			lLogout.setString("sourceId", aConnector.getId());
			// if a unique node id is specified for the client include that
			String lNodeId = aConnector.getNodeId();
			if (lNodeId != null) {
				lLogout.setString("unid", lNodeId);
			}
			// broadcast to all except source
			broadcastToken(aConnector, lLogout);
		}
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
		// send "goodBye" token to client
		Token lGoodBye = TokenFactory.createToken(TT_GOODBYE);
		lGoodBye.setString("ns", getNamespace());
		lGoodBye.setString("vendor", JWebSocketCommonConstants.VENDOR);
		lGoodBye.setString("version", JWebSocketServerConstants.VERSION_STR);
		lGoodBye.setString("sourceId", aConnector.getId());
		if (aCloseReason != null) {
			lGoodBye.setString("reason", aCloseReason.toString().toLowerCase());
		}

		// don't send session-id on good bye, neither required nor desired
		sendToken(aConnector, aConnector, lGoodBye);
	}

	private void login(WebSocketConnector aConnector, Token aToken) {

		// sendWelcome(aConnector);

		Token lResponse = createResponse(aToken);

		String lUsername = aToken.getString("username");
		// TODO: Add authentication and password check
		String lPassword = aToken.getString("password");
		// optionally continue previous session
		String lSessionId = aToken.getString("usid");
		String lGroup = aToken.getString("group");
		Boolean lReturnRoles = aToken.getBoolean("getRoles", Boolean.FALSE);
		Boolean lReturnRights = aToken.getBoolean("getRights", Boolean.FALSE);

		if (log.isDebugEnabled()) {
			log.debug("Processing 'login' (username='" + lUsername
					+ "', group='" + lGroup
					+ "') from '" + aConnector + "'...");
		}

		if (lUsername != null) {

			User lUser = SecurityFactory.getUser(lUsername);

			// TODO: Here we need to check if the user is in the user data base at
			// all.
			lResponse.setString("username", lUsername);
			// if previous session id was passed to continue an aborted session
			// return the session-id to notify client about acceptance
			if (lSessionId != null) {
				lResponse.setString("usid", lSessionId);
			}
			lResponse.setString("sourceId", aConnector.getId());
			// set shared variables
			setUsername(aConnector, lUsername);
			setGroup(aConnector, lGroup);

			if (lUser != null) {
				if (lReturnRoles) {
					lResponse.setList("roles", new FastList(lUser.getRoleIdSet()));
				}
				if (lReturnRights) {
					lResponse.setList("rights", new FastList(lUser.getRightIdSet()));
				}
			}
		} else {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", "missing arguments for 'login' command");
		}

		// send response to client
		sendToken(aConnector, aConnector, lResponse);

		// if successfully logged in...
		if (lUsername != null) {
			// broadcast "login event" to other clients
			broadcastLoginEvent(aConnector);
		}
	}

	private void logout(WebSocketConnector aConnector, Token aToken) {
		Token lResponse = createResponse(aToken);

		if (log.isDebugEnabled()) {
			log.debug("Processing 'logout' (username='"
					+ getUsername(aConnector)
					+ "') from '" + aConnector + "'...");
		}

		if (getUsername(aConnector) != null) {
			// send normal answer token, good bye is for close!
			sendToken(aConnector, aConnector, lResponse);
			// send good bye token as response to client
			// sendGoodBye(aConnector, CloseReason.CLIENT);

			// and broadcast the logout event
			broadcastLogoutEvent(aConnector);
			// resetting the username is the only required signal for logout
			// lResponse.put("usid", getSessionId(aConnector));
			lResponse.setString("sourceId", aConnector.getId());
			removeUsername(aConnector);
			removeGroup(aConnector);
		} else {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", "not logged in");
			sendToken(aConnector, aConnector, lResponse);
		}
	}

	private void send(WebSocketConnector aConnector, Token aToken) {
		// check if user is allowed to run 'send' command
		if (!SecurityFactory.hasRight(getUsername(aConnector), NS_SYSTEM_DEFAULT + ".send")) {
			sendToken(aConnector, aConnector, createAccessDenied(aToken));
			return;
		}
		Token lResponse = createResponse(aToken);

		WebSocketConnector lTargetConnector;
		String lTargetId = aToken.getString("unid");
		String lTargetType;
		if (lTargetId != null) {
			lTargetConnector = getNode(lTargetId);
			lTargetType = "node-id";
		} else {
			// get the target
			lTargetId = aToken.getString("targetId");
			lTargetConnector = getConnector(lTargetId);
			lTargetType = "client-id";
		}

		/*
		 * if (getUsername(aConnector) != null) {
		 */
		if (lTargetConnector != null) {
			if (log.isDebugEnabled()) {
				log.debug("Processing 'send' (username='"
						+ getUsername(aConnector)
						+ "') from '" + aConnector
						+ "' to " + lTargetId + "...");
			}
			// don't distribute session id of the client here!
			// this would open a security hole!
			aToken.remove("usid");

			aToken.setString("sourceId", aConnector.getId());
			sendToken(aConnector, lTargetConnector, aToken);
		} else {
			String lMsg = "No target connector with "
					+ lTargetType + " '"
					+ lTargetId + "' found.";
			log.warn(lMsg);
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", lMsg);
			sendToken(aConnector, aConnector, lResponse);
		}
	}

	private void respond(WebSocketConnector aConnector, Token aToken) {
		Token lResponse = createResponse(aToken);

		WebSocketConnector lTargetConnector;
		String lTargetId = aToken.getString("unid");
		String lTargetType;
		if (lTargetId != null) {
			lTargetConnector = getNode(lTargetId);
			lTargetType = "node-id";
		} else {
			// get the target
			lTargetId = aToken.getString("targetId");
			lTargetConnector = getConnector(lTargetId);
			lTargetType = "client-id";
		}

		if (lTargetConnector != null) {
			if (log.isDebugEnabled()) {
				log.debug("Processing 'respond' (username='"
						+ getUsername(aConnector)
						+ "') from '" + aConnector
						+ "' to " + lTargetId + "...");
			}
			aToken.setType("response");
			aToken.setString("sourceId", aConnector.getId());
			sendToken(aConnector, lTargetConnector, aToken);
		} else {
			String lMsg = "No target connector with "
					+ lTargetType + " '"
					+ lTargetId + "' found.";
			log.warn(lMsg);
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", lMsg);
			sendToken(aConnector, aConnector, lResponse);
		}
	}

	private void broadcast(WebSocketConnector aConnector, Token aToken) {

		// check if user is allowed to run 'broadcast' command
		if (!SecurityFactory.hasRight(getUsername(aConnector), NS_SYSTEM_DEFAULT + ".broadcast")) {
			sendToken(aConnector, aConnector, createAccessDenied(aToken));
			return;
		}

		Token lResponse = createResponse(aToken);

		if (log.isDebugEnabled()) {
			log.debug("Processing 'broadcast' (username='"
					+ getUsername(aConnector)
					+ "') from '" + aConnector + "'...");
		}
		/*
		 * if (getUsername(aConnector) != null) {
		 */
		aToken.setString("sourceId", aConnector.getId());
		// keep senderIncluded beging false as default, apps rely on this!
		Boolean lIsSenderIncluded = aToken.getBoolean("senderIncluded", false);
		Boolean lIsResponseRequested = aToken.getBoolean("responseRequested", true);

		// don't distribute session id of sender connection here!
		aToken.remove("usid");
		// remove further non target related fields
		aToken.remove("senderIncluded");
		aToken.remove("responseRequested");

		// broadcast the token
		broadcastToken(aConnector, aToken,
				new BroadcastOptions(lIsSenderIncluded, lIsResponseRequested));

		// check if response was requested
		if (lIsResponseRequested) {
			sendToken(aConnector, aConnector, lResponse);
		}
		/*
		 * } else { lResponse.put("code", -1); lResponse.put("msg",
		 * "not logged in"); sendToken(aConnector, lResponse); }
		 */
	}

	private void close(WebSocketConnector aConnector, Token aToken) {
		int lTimeout = aToken.getInteger("timeout", 0);

		boolean lNoGoodBye =
				aToken.getBoolean("noGoodBye", false);
		boolean lNoLogoutBroadcast =
				aToken.getBoolean("noLogoutBroadcast", false);
		boolean lNoDisconnectBroadcast =
				aToken.getBoolean("noDisconnectBroadcast", false);

		// only send a good bye message if timeout is > 0 and not to be noed
		if (lTimeout > 0 && !lNoGoodBye) {
			sendGoodBye(aConnector, CloseReason.CLIENT);
		}
		// if logged in...
		if (getUsername(aConnector) != null && !lNoLogoutBroadcast) {
			// broadcast the logout event.
			broadcastLogoutEvent(aConnector);
		}
		// reset the username, we're no longer logged in
		removeUsername(aConnector);

		if (log.isDebugEnabled()) {
			log.debug("Closing client " + (lTimeout > 0 ? "with timeout " + lTimeout + "ms" : "immediately") + "...");
		}

		// don't send a response here! We're about to close the connection!
		// broadcasts disconnect event to other clients
		// if not explicitely noed
		aConnector.setBoolean("noDisconnectBroadcast", lNoDisconnectBroadcast);
		aConnector.stopConnector(CloseReason.CLIENT);
	}

	/**
	 *
	 * @param aToken
	 */
	private void echo(WebSocketConnector aConnector, Token aToken) {
		Token lResponse = createResponse(aToken);

		String lData = aToken.getString("data");
		if (lData != null) {
			if (log.isDebugEnabled()) {
				log.debug("echo " + lData);
			}
			lResponse.setString("data", lData);
		} else {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", "missing 'data' argument for 'echo' command");
		}

		sendToken(aConnector, aConnector, lResponse);
	}

	/**
	 *
	 * @param aConnector
	 * @param aToken
	 */
	public void ping(WebSocketConnector aConnector, Token aToken) {
		Boolean lEcho = aToken.getBoolean("echo", Boolean.TRUE);

		if (log.isDebugEnabled()) {
			log.debug("Processing 'Ping' (echo='" + lEcho
					+ "') from '" + aConnector + "'...");
		}

		if (lEcho) {
			Token lResponse = createResponse(aToken);
			// TODO: here could we optionally send a time stamp
			// TODO: implement response time on client!
			// lResponseToken.put("","");
			sendToken(aConnector, aConnector, lResponse);
		}
	}

	/**
	 * simply waits for a certain amount of time and does not perform any _
	 * operation. This feature is used for debugging and simulation purposes _
	 * only and is not related to any business logic.
	 * @param aToken
	 */
	private void wait(WebSocketConnector aConnector, Token aToken) {
		Token lResponse = createResponse(aToken);

		Integer lDuration = aToken.getInteger("duration", 0);
		Boolean lIsResponseRequested = aToken.getBoolean("responseRequested", true);
		if (lDuration != null && lDuration >= 0) {
			if (log.isDebugEnabled()) {
				log.debug("duration " + lDuration);
			}
			try {
				Thread.sleep(lDuration);
			} catch (Exception lEx) {
				// ignore potential exception here!
			}
			lResponse.setInteger("duration", lDuration);
		} else {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", "missing or invalid 'duration' argument for 'wait' command");
		}

		// for test purposes we need to optionally suppress a response
		// to simulate this error condition
		if (lIsResponseRequested) {
			sendToken(aConnector, aConnector, lResponse);
		}
	}

	/**
	 *
	 * @param aConnector
	 * @param aToken
	 */
	public void getClients(WebSocketConnector aConnector, Token aToken) {
		Token lResponse = createResponse(aToken);

		if (log.isDebugEnabled()) {
			log.debug("Processing 'getClients' from '"
					+ aConnector + "'...");
		}

		if (getUsername(aConnector) != null) {
			String lGroup = aToken.getString("group");
			Integer lMode = aToken.getInteger("mode", 0);
			FastMap lFilter = new FastMap();
			lFilter.put(BaseConnector.VAR_USERNAME, ".*");
			List<String> listOut = new FastList<String>();
			for (WebSocketConnector lConnector : getServer().selectConnectors(lFilter).values()) {
				listOut.add(getUsername(lConnector) + "@" + lConnector.getRemotePort());
			}
			lResponse.setList("clients", listOut);
			lResponse.setInteger("count", listOut.size());
		} else {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", "not logged in");
		}

		sendToken(aConnector, aConnector, lResponse);
	}

	/**
	 * allocates a "non-interruptable" communication channel between two clients.
	 *
	 * @param aConnector
	 * @param aToken
	 */
	public void allocChannel(WebSocketConnector aConnector, Token aToken) {
		Token lResponse = createResponse(aToken);

		if (log.isDebugEnabled()) {
			log.debug("Processing 'allocChannel' from '"
					+ aConnector + "'...");
		}
	}

	/**
	 * deallocates a "non-interruptable" communication channel between two
	 * clients.
	 *
	 * @param aConnector
	 * @param aToken
	 */
	public void deallocChannel(WebSocketConnector aConnector, Token aToken) {
		Token lResponse = createResponse(aToken);

		if (log.isDebugEnabled()) {
			log.debug("Processing 'deallocChannel' from '"
					+ aConnector + "'...");
		}
	}
}
