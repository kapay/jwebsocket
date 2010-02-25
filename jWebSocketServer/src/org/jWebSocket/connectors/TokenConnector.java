//	---------------------------------------------------------------------------
//	jWebSocket - WebSocket Token Connector (abstract class)
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
package org.jWebSocket.connectors;

import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import org.apache.log4j.Logger;
import org.jWebSocket.server.BaseServer;
import org.jWebSocket.config.Config;
import org.jWebSocket.server.Header;
import org.jWebSocket.server.Token;
import org.jWebSocket.server.TokenServer;

/**
 *
 * @author aschulze
 */
public abstract class TokenConnector extends BaseConnector {

	private static Logger log = Logger.getLogger(TokenConnector.class);
	private String pool = null;
	private String sessionId = null;
	private String username = null;

	/**
	 *
	 * @param aServerSocket
	 * @param aClientSocket
	 * @param aHeader
	 */
	public TokenConnector(BaseServer aServerSocket, Socket aClientSocket, Header aHeader) {
		super(aServerSocket, aClientSocket, aHeader);
	}

	public TokenServer getTokenServer()	{
		return (TokenServer)getWebSocketServer();
	}

	/**
	 *
	 * @param aData
	 */
	public void send(String aData) {
		log.debug("Sending (" + aData.length() + " bytes): " + aData);
		try {
			super.send(aData);
		} catch (Exception ex) {
			log.error(ex.getClass().getName() + " on send: " + ex.getMessage());
		}
	}

	/**
	 *
	 */
	@Override
	public void clientThreadStarted() {
		log.debug("Client thread started at port " + getClientSocket().getPort() + "...");
	}

	/**
	 *
	 */
	@Override
	public void handshakeSent() {
		// generate session id for client to allow multiple sessions per user...
		sessionId = Integer.toHexString(this.getClientSocket().getPort());
		while (sessionId.length() < 4) {
			sessionId = "0" + sessionId;
		}
		log.debug(
			"Session-Id: " + getSessionId()
			+ ", Subprotocol: " + getHeader().getSubProtocol(Config.SUB_PROT_JSON)
			+ ", Timeout: " + getHeader().getTimeout(Config.DEFAULT_TIMEOUT));

		sendWelcomeToken();
		broadcastConnectEvent();
	}

	/**
	 *
	 */
	public void sendWelcomeToken() {
		// send "welcome" token to client
		Token lOutToken = new Token("welcome");
		lOutToken.put("vendor", Config.VENDOR);
		lOutToken.put("version", Config.VERSION_STR);
		lOutToken.put("usid", getSessionId());
		lOutToken.put("port", this.getClientSocket().getPort());
		try {
			lOutToken.put("timeout", this.getClientSocket().getSoTimeout());
		} catch (SocketException ex) {
			lOutToken.put("timeout", -1);
		}
		sendToken(lOutToken);
	}

	/**
	 *
	 * @param aReason
	 */
	public void sendGoodByeToken(String aReason) {
		// send "goodBye" token to client
		Token lOutToken = new Token("goodBye");
		lOutToken.put("vendor", Config.VENDOR);
		lOutToken.put("version", Config.VERSION_STR);
		lOutToken.put("usid", getSessionId());
		lOutToken.put("port", this.getClientSocket().getPort());
		if (aReason != null) {
			lOutToken.put("reason", aReason);
		}
		sendToken(lOutToken);
	}

	/**
	 *
	 */
	public void broadcastConnectEvent() {
		// broadcast connect event to other clients of the jWebSocket network
		Token lEventToken = new Token("event");
		lEventToken.put("name", "connect");
		lEventToken.put("clientCount", getWebSocketServer().getClients().size());
		mBroadcast(lEventToken);
	}

	/**
	 *
	 * @param aReason
	 */
	public void broadcastDisconnectEvent(String aReason) {
		// broadcast disconnect event to other clients of the jWebSocket network
		Token lEventToken = new Token("event");
		lEventToken.put("name", "disconnect");
		lEventToken.put("clientCount", getWebSocketServer().getClients().size());
		if (aReason != null) {
			lEventToken.put("reason", aReason);
		}
		mBroadcast(lEventToken);
	}

	/**
	 *
	 */
	public void broadcastLoginEvent() {
		// broadcast login event to other clients of the jWebSocket network
		Token lEventToken = new Token("event");
		lEventToken.put("name", "login");
		lEventToken.put("username", getUsername());
		lEventToken.put("clientCount", getWebSocketServer().getClients().size());
		mBroadcast(lEventToken);
	}

	/**
	 *
	 */
	public void broadcastLogoutEvent() {
		// broadcast logout event to other clients of the jWebSocket network
		Token lEventToken = new Token("event");
		lEventToken.put("name", "logout");
		lEventToken.put("username", getUsername());
		lEventToken.put("clientCount", getWebSocketServer().getClients().size());
		mBroadcast(lEventToken);
	}

	/**
	 *
	 * @param aInToken
	 * @return
	 */
	public Token createResponse(Token aInToken) {
		String lTokenId = aInToken.getString("utid");
		String lType = aInToken.getString("type");
		Token lResToken = new Token("result");
		lResToken.put("code", 0);
		lResToken.put("msg", "ok");
		if (lTokenId != null) {
			lResToken.put("utid", lTokenId);
		}
		if (lType != null) {
			lResToken.put("reqType", lType);
		}
		return lResToken;
	}

	/**
	 *
	 */
	@Override
	public void timeoutExceeded() {
		// if logged in broadcast logout event to other clients
		if (isLoggedIn()) {
			broadcastLogoutEvent();
		}
		// broadcast disconnect event to other clients
		broadcastDisconnectEvent("timeout");
		sendGoodByeToken("timeout");
		log.info("Timeout exceeded for session " + getSessionId());
	}

	/**
	 *
	 */
	@Override
	public void clientClosed() {
		log.info("Client terminated session " + getSessionId() + ".");
	}

	/**
	 *
	 *
	 * @param aObject
	 */
	@Override
	public void dataReceived(Object aObject) {
		String line = (String) aObject;
		int lPos = line.indexOf('{');
		if (lPos > 0) {
			line = line.substring(lPos);
		}
		log.debug("Received stream: '" + line + "'");

		// parse tokens...
		Token lToken = streamToToken(line);
		// successfully parsed ?
		if (getClass().equals(TokenConnector.class)) {
			log.error("Data stream only processed in descendant classes.");
		} else if (lToken != null) {
			String lType = lToken.getType();
			String lNS = lToken.getNS();
			log.debug("Received token: '" + lType + "', ns: '" + lNS + "'");

			// run requested command
			try {
				if (lType.equals("echo")) {
					echo(lToken);
				} else if (lType.equals("send")) {
					send(lToken);
				} else if (lType.equals("broadcast")) {
					broadcast(lToken);
				} else if (lType.equals("login")) {
					login(lToken);
				} else if (lType.equals("logout")) {
					logout(lToken);
				} else if (lType.equals("close")) {
					close(lToken);
				} else {
					super.dataReceived(lToken);
				}
			} catch (Exception ex) {
				log.error("Exception on handling token " + ex.getMessage());
			}
		} else {
			log.error("Tokens could not be parsed.");
		}
	}

	/**
	 *
	 */
	@Override
	public void clientThreadStopped() {
		log.info("Client thread stopped.");
	}

	/**
	 *
	 * @return
	 */
	public boolean isLoggedIn() {
		return username != null;
	}

	/**
	 * @return the sessionId
	 */
	public String getSessionId() {
		return (sessionId != null ? sessionId : "-");
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @return the pool
	 */
	public String getPool() {
		return pool;
	}

	/**
	 * this is just the basic class
	 * overwrite this method to send token in a special format
	 * @param aOutToken
	 */
	public void sendToken(Token aOutToken) {
		send(tokenToStream(aOutToken));
	}

	;

	/**
	 *
	 *
	 * @param aOutToken
	 */
	public void sendResponse(Token aOutToken) {
		// this is just the basic class
		// overwrite this method to send token in a special format
		aOutToken.setType("response");
		sendToken(aOutToken);
	}

	/**
	 *
	 * @param aInToken
	 */
	private void echo(Token aToken) {
		Token lToken = createResponse(aToken);
		String lData = aToken.getString("data");
		if (lData != null) {
			log.debug("echo " + lData);
		} else {
			lToken.put("code", -1);
			lToken.put("msg", "missing 'data' argument for 'echo' command");
		}
		sendResponse(lToken);
	}

	private void mBroadcast(Token aToken) {
		String lPool = aToken.getString("pool");
		String lUsername = aToken.getString("receiver");
		String lType = aToken.getType();
		String lEcho = aToken.getString("echo");

		String lLogStr = lType;
		if (lType.equals("event")) {
			lLogStr += " (" + (String) aToken.get("name") + ")";
		}
		log.debug(lLogStr);
		// aToken.remove("token");
		if (username != null) {
			aToken.put("sender", username);
		} else if (sessionId != null) {
			aToken.put("sid", sessionId);
		}
		List<BaseConnector> lClients = this.getWebSocketServer().getClients();
		for (BaseConnector client : lClients) {
			TokenConnector jws = (TokenConnector) client;
			if (jws.isLoggedIn()
				&& (lPool == null || lPool.equals(jws.getPool()))
				&& (lUsername == null || lUsername.equals(jws.getUsername()))
				&& (client != this || (lEcho != null && lEcho.equals("true")))) {
				jws.sendToken(aToken);
			}
		}
	}

	private void send(Token aToken) {
		String lUsername = aToken.getString("username");
		String lPool = aToken.getString("pool");
		mBroadcast(aToken);
	}

	private void broadcast(Token aToken) {
		Token lRes = createResponse(aToken);
		mBroadcast(aToken);
		sendResponse(lRes);
	}

	private void login(Token aToken) {
		String lUsername = aToken.getString("username");
		String lPassword = aToken.getString("password");
		String lPoolId = aToken.getString("pool");
		Token lRes = createResponse(aToken);
		if (lUsername != null) {
			log.debug("login " + lUsername);
			lRes.put("username", lUsername);
			username = lUsername;
			pool = lPoolId;

			// broadcast "login event" to other clients
			broadcastLoginEvent();
		} else {
			lRes.put("code", -1);
			lRes.put("msg", "missing arguments for 'login' command");
		}

		// send response to client
		sendResponse(lRes);
	}

	private void logout(Token aToken) {
		if (isLoggedIn()) {
			// send good bye token as response to client
			sendGoodByeToken("logout");
			// and broadcast the logout event
			broadcastLogoutEvent();
			// resetting the username is the only required signal for logout
			username = null;
		} else {
			Token lResponseToken = createResponse(aToken);
			lResponseToken.put("code", -1);
			lResponseToken.put("msg", "not logged in");
			sendResponse(lResponseToken);
		}
	}

	private void close(Token aToken) {
		Integer lTimeout = aToken.getInteger("timeout", 0);
		// if timeout > 0 send a good bye token to the client
		if (lTimeout > 0) {
			// send good bye token
			sendGoodByeToken("close");
		}
		if (isLoggedIn()) {
			// and broadcast the logout event
			broadcastLogoutEvent();
		}
		// reset the username, we're no longer logged in
		username = null;

		// broadcast disconnect event to other clients
		broadcastDisconnectEvent("close");

		log.debug("Closing client...");
		terminate();
		// don't send a response here! We're about to close the connection!
	}

	/**
	 *
	 * @param aData
	 * @return
	 */
	protected abstract Token streamToToken(String aData);

	/**
	 *
	 * @param aToken
	 * @return
	 */
	protected abstract String tokenToStream(Token aToken);

	/**
	 *
	 */
	@Override
	public void terminate() {
		log.debug("Terminating client '" + getSessionId() + "'...");
		try {
			super.terminate();
		} catch (Exception ex) {
			log.debug("Exception on terminate " + ex.getMessage());
		}
		log.debug("Terminated.");
	}
}
