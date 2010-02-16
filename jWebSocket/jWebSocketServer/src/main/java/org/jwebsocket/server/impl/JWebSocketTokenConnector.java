//	---------------------------------------------------------------------------
//	jWebSocket - Main Class: Command line IF, Logger Init and Args-Parser
//	Copyright (c) 2010 jwebsocket.org
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
package org.jwebsocket.server.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jwebsocket.server.JWebSocketBaseServer;
import org.jwebsocket.server.api.ConnectorContext;
import org.jwebsocket.server.api.JWebSocketConnector;

/**
 * @author Puran Singh
 * @version $Id: JWebSocketTokenConnector.java 58 2010-02-15 19:21:56Z
 *          mailtopuran $
 * 
 */
public class JWebSocketTokenConnector extends JWebSocketBaseConnector {

	private static Logger log = Logger
			.getLogger(JWebSocketTokenConnector.class);
	public int counter = 0;
	private String pool = null;
	private String sessionId = null;
	private String username = null;

	/**
	 * 
	 * @param aServerSocket
	 * @param aClientSocket
	 */
	public JWebSocketTokenConnector(ConnectorContext context) {
		super(context);
	}

	/**
	 *
	 */
	@Override
	public void onHandShakeResponse() {
		log.debug("Sending handshake...");
		try {
			super.onHandShakeResponse();
		} catch (Exception ex) {
			log.error("Exception on handshake " + ex.getMessage());
		}
	}

	/**
	 * 
	 * @param aData
	 */
	@Override
	public void sendString(String aData) {
		log.debug("Sending packet (" + aData.length() + " bytes): " + aData);
		try {
			super.sendString(aData);
		} catch (Exception ex) {
			log.error("Exception on send packet " + ex.getMessage());
		}
	}

	/**
	 *
	 */
	@Override
	public void clientThreadStarted() {
	}

	/**
	 * 
	 * @param aHeader
	 */
	@Override
	public void headerReceived(String aHeader) {
		log.debug("Header received: " + aHeader.replace("\n", "\\n"));
	}

	/**
	 * 
	 */
	@Override
	public void headerParsed() {
		log.debug("header parsed: origin: " + getOrigin() + ", location: "
				+ getLocation());
	}

	/**
	 *
	 */
	@Override
	public void handshakeSent() {
		// generate and return session id to client to allow multiple sessions
		// per user...
		counter++;
		String lUID = "jWebSocketUID-" + System.nanoTime() + "-" + counter
				+ "-" + getContext().getLocalPort();
		sessionId = getContext().getSessionId();
		log.debug("generated UID: " + lUID + ", session-id: " + getSessionId());

		HashMap<String, Object> lArgs = new HashMap<String, Object>();
		lArgs.put("vendor", JWebSocketBaseServer.VENDOR);
		lArgs.put("version", JWebSocketBaseServer.VERSION_STR);
		lArgs.put("sid", getSessionId());
		sendToken("welcome", lArgs);

		// broadcast connect event
		lArgs = new HashMap<String, Object>();
		lArgs.put("token", "event");
		lArgs.put("type", "connect");
		mBroadcast(lArgs);

	}
	
	private void send(HashMap<String, Object> aArgs) {
		mBroadcast(aArgs);
	}

	/**
	 * private method that broadcast the message to output
	 * 
	 * @param aArgs
	 *            the argument with broadcast message
	 */
	private void mBroadcast(Map<String, Object> aArgs) {
		String lPool = (String) aArgs.get("pool");
		String lUsername = (String) aArgs.get("receiver");
		String lCmd = (String) aArgs.get("token");
		String lEcho = (String) aArgs.get("echo");

		String lLogStr = lCmd;
		if (lCmd.equals("event")) {
			lLogStr += " (" + (String) aArgs.get("type") + ")";
		}
		log.debug(lLogStr);
		aArgs.remove("token");
		if (username != null) {
			aArgs.put("sender", username);
		} else if (sessionId != null) {
			aArgs.put("sid", sessionId);
		}
		List<JWebSocketConnector> lClients = getContext().getJWebSocketServer()
				.getClients();
		for (JWebSocketConnector client : lClients) {
			JWebSocketTokenConnector jws = (JWebSocketTokenConnector) client;
			if (jws.isLoggedIn()
					&& (lPool == null || lPool.equals(jws.getPool()))
					&& (lUsername == null || lUsername
							.equals(jws.getUsername()))
					&& (client != this || (lEcho != null && lEcho
							.equals("true")))) {
				jws.sendToken(lCmd, aArgs);
			}
		}
	}

	/**
	 * broad cast the data
	 * 
	 * @param aArgs
	 *            the map of data
	 */
	private void broadcast(HashMap<String, Object> aArgs) {
		HashMap<String, Object> lRes = createDefaultResult(aArgs);
		mBroadcast(aArgs);
		sendResponse(lRes);
	}

	/**
	 * login
	 * 
	 * @param aArgs
	 */
	private void login(Map<String, Object> aArgs) {
		String lUsername = (String) aArgs.get("username");
		String lPassword = (String) aArgs.get("password");
		String lPoolId = (String) aArgs.get("pool");
		HashMap<String, Object> lRes = createDefaultResult(aArgs);
		if (lUsername != null) {
			log.debug("login " + lUsername);
			lRes.put("username", lUsername);
			username = lUsername;
			pool = lPoolId;

			HashMap<String, Object> lArgs = new HashMap<String, Object>();
			lArgs.put("token", "event");
			lArgs.put("type", "login");
			mBroadcast(lArgs);
		} else {
			lRes.put("code", -1);
			lRes.put("msg", "missing arguments for 'login' command");
		}
		sendResponse(lRes);
	}
	
	private void logout(Map<String, Object> aArgs) {
		Map<String, Object> lRes = createDefaultResult(aArgs);
		if (isLoggedIn()) {
			log.debug("logout " + getUsername());
			// send good by token
			Map<String, Object> lArgs = new HashMap<String, Object>();
			lArgs.put("reason", "logout");
			sendGoodBye(lArgs);
			username = null;
		} else {
			lRes.put("code", -1);
			lRes.put("msg", "not logged in");
		}
		sendResponse(lRes);
	}
	
	private void sendGoodBye(Map<String, Object> aArgs) {
		if (isLoggedIn()) {
			Map<String, Object> lArgs = new HashMap<String, Object>();
			lArgs.put("token", "event");
			lArgs.put("type", "logout");
			mBroadcast(lArgs);
		}
		// lRes.put("clients", lClients);
		// lRes.put("count", lClients.size());
		sendToken("goodBye", aArgs);
	}


	/**
	 * create default result
	 * 
	 * @param aArgs
	 *            the map of data
	 * @return the map of key/value pair of data
	 */
	private HashMap<String, Object> createDefaultResult(
			Map<String, Object> aArgs) {
		String lTokenId = (String) aArgs.get("tid");
		String lRequest = (String) aArgs.get("token");
		HashMap<String, Object> lRes = new HashMap<String, Object>();
		lRes.put("code", 0);
		lRes.put("msg", "ok");
		if (lTokenId != null) {
			lRes.put("tid", lTokenId);
		}
		if (lRequest != null) {
			lRes.put("req", lRequest);
		}
		return lRes;
	}

	public void sendResponse(Map<String, Object> aArgs) {
		sendToken("response", aArgs);
	}

	private void echo(HashMap<String, Object> aArgs) {
		HashMap<String, Object> lRes = createDefaultResult(aArgs);
		String lData = (String) aArgs.get("data");
		if (lData != null) {
			log.debug("echo " + lData);
		} else {
			lRes.put("code", -1);
			lRes.put("msg", "missing 'data' argument for 'echo' command");
		}
		sendResponse(lRes);
	}

	/**
	 * 
	 * @param aArgs
	 */
	public void getClients(HashMap<String, Object> aArgs) {
		HashMap<String, Object> lRes = createDefaultResult(aArgs);
		if (isLoggedIn()) {
			log.debug("getClients");
			List<JWebSocketConnector> lClients = getContext()
					.getJWebSocketServer().getClients();
			lRes.put("clients", lClients);
			lRes.put("count", lClients.size());
		} else {
			lRes.put("code", -1);
			lRes.put("msg", "not logged in");
		}
		sendResponse(lRes);
	}

	private void sendGoodBye(HashMap<String, Object> aArgs) {
		if (isLoggedIn()) {
			HashMap<String, Object> lArgs = new HashMap<String, Object>();
			lArgs.put("token", "event");
			lArgs.put("type", "logout");
			mBroadcast(lArgs);
		}
		// lRes.put("clients", lClients);
		// lRes.put("count", lClients.size());
		sendToken("goodBye", aArgs);
	}

	private void close(HashMap<String, Object> aArgs) {
		String lString = (String) aArgs.get("timeout");
		int lTimeout = 0;
		// check if timeout has been passed
		if (lString != null) {
			try {
				lTimeout = Integer.parseInt(lString);
			} catch (NumberFormatException ex) {
			}
		}
		// if timeout > 0 send a good bye token to the client
		if (lTimeout > 0) {
			// send good bye token
			HashMap<String, Object> lArgs = new HashMap<String, Object>();
			lArgs.put("reason", "disconnect");
			sendGoodBye(lArgs);
		}
		log.debug("close");
		terminate();
		// don't sendResponse(lRes); here! we're about to close!
	}

	protected HashMap<String, Object> parseToken(String aData) {
		HashMap<String, Object> lArgs = new HashMap<String, Object>();
		return lArgs;
	}

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

	@Override
	public void timeoutExceeded() {
	}

	@Override
	public void clientClosed() {
	}

	@Override
	public void dataReceived(String line) {
		int lPos = line.indexOf('{');
		if (lPos > 0) {
			line = line.substring(lPos);
		}
		log.debug("Received stream: '" + line + "'");

		// parse tokens...
		HashMap<String, Object> lArgs = parseToken(line);
		// successfully parsed ?
		if (getClass().equals(JWebSocketTokenConnector.class)) {
			log.error("Data stream only processed in descendant classes.");
		} else if (lArgs != null) {
			String lToken = (String) lArgs.get("token");
			log.debug("Receivced token: " + lToken + ", args: "
					+ lArgs.toString());

			// run requested command
			try {
				if (lToken.equals("echo")) {
					echo(lArgs);
				} else if (lToken.equals("send")) {
					send(lArgs);
				} else if (lToken.equals("broadcast")) {
					broadcast(lArgs);
				} else if (lToken.equals("login")) {
					login(lArgs);
				} else if (lToken.equals("logout")) {
					logout(lArgs);
				} else if (lToken.equals("getClients")) {
					getClients(lArgs);
				} else if (lToken.equals("close")) {
					close(lArgs);
				}
			} catch (Exception ex) {
				log.error("Exception on handling token " + ex.getMessage());
			}
		} else {
			log.error("Tokens could not be parsed.");
		}

	}

	@Override
	public void clientThreadStopped() {
	}

	private boolean isLoggedIn() {
		return getUsername() != null;
	}

	public String getSessionId() {
		return (sessionId != null ? sessionId : "-");
	}

	public String getUsername() {
		return (username != null ? username : "-");
	}

	public String getPool() {
		return pool;
	}

	public void sendToken(String aToken, Map<String, Object> aArgs) {
		// this is just the basic class
		// overwrite this method to send token in a special format
	}

}
