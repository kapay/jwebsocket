//	---------------------------------------------------------------------------
//	jWebSocket - Copyright (c) 2010 Innotrade GmbH
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
package org.jwebsocket.client.token;

import org.jwebsocket.api.WebSocketClientListener;
import org.jwebsocket.api.WebSocketClientTokenListener;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.client.java.BaseClient;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.listener.WebSocketClientEvent;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.packetProcessors.CSVProcessor;
import org.jwebsocket.packetProcessors.JSONProcessor;
import org.jwebsocket.packetProcessors.XMLProcessor;
import org.jwebsocket.token.Token;

/**
 *
 * @author aschulze
 */
public class TokenClient {

	public final static int DISCONNECTED = 0;
	public final static int CONNECTED = 1;
	public final static int AUTHENTICATED = 2;
	private int CUR_TOKEN_ID = 0;
	private BaseClient client = null;
	private String lSubProt = JWebSocketCommonConstants.SUB_PROT_DEFAULT;
	private final static String NS_BASE = "org.jWebSocket";
	private String fUsername = null;
	private String fClientId = null;
	private String fSessionId = null;
	private String fRestoreSessionId = null;

	public TokenClient(BaseClient aClient) {
		client = aClient;
		client.addListener(new Listener());
	}

	public boolean isConnected() {
		return client.isConnected();
	}

	/**
	 * @return the fUsername
	 */
	public String getUsername() {
		return fUsername;
	}

	/**
	 * @return the fClientId
	 */
	public String getClientId() {
		return fClientId;
	}

	/**
	 * @return the fSessionId
	 */
	public String getfSessionId() {
		return fSessionId;
	}

	class Listener implements WebSocketClientListener {

		@Override
		public void processOpened(WebSocketClientEvent aEvent) {
			fUsername = null;
			fClientId = null;
			fSessionId = null;
		}

		@Override
		public void processPacket(WebSocketClientEvent aEvent, WebSocketPacket aPacket) {
			for (WebSocketClientListener lListener : client.getListeners()) {
				if (lListener instanceof WebSocketClientTokenListener) {
					Token lToken = packetToToken(aPacket);

					String lNS = lToken.getNS();
					String lType = lToken.getType();
					String lReqType = lToken.getString("reqType");

					if (lType != null) {
						if ("welcome".equals(lType)) {
							fClientId = lToken.getString("sourceId");
							fSessionId = lToken.getString("usid");
						} else if ("goodBye".equals(lType)) {
							fUsername = null;
						}
					}
					if (lReqType != null) {
						if ("login".equals(lReqType)) {
							fUsername = lToken.getString("username");
						} else if ("logout".equals(lReqType)) {
							fUsername = null;
						}
					}
					((WebSocketClientTokenListener) lListener).processToken(aEvent, lToken);
				}
			}
		}

		@Override
		public void processClosed(WebSocketClientEvent aEvent) {
			fUsername = null;
			fClientId = null;
			fRestoreSessionId = fSessionId;
			fSessionId = null;
		}
	}

	public void addListener(WebSocketClientTokenListener aListener) {
		client.addListener(aListener);
	}

	public void removeListener(WebSocketClientTokenListener aListener) {
		client.removeListener(aListener);
	}

	public void open(String aURL) throws WebSocketException {
		client.open(aURL);
	}

	public void send(String aData, String aEncoding) throws WebSocketException {
		client.send(aData, aEncoding);
	}

	public void send(byte[] aData) throws WebSocketException {
		client.send(aData);
	}

	public void send(WebSocketPacket aPacket) throws WebSocketException {
		client.send(aPacket.getByteArray());
	}

	public void close() throws WebSocketException {
		fUsername = null;
		fClientId = null;
		fRestoreSessionId = fSessionId;
		fSessionId = null;
		client.close();
	}

	// TODO: Check if the following two methods packetToToken and tokenToPacket can be shared for server and client
	/**
	 *
	 * @param aConnector
	 * @param aPacket
	 * @return
	 */
	public Token packetToToken(WebSocketPacket aPacket) {
		Token lToken = null;
		if (lSubProt.equals(JWebSocketCommonConstants.SUB_PROT_JSON)) {
			lToken = JSONProcessor.packetToToken(aPacket);
		} else if (lSubProt.equals(JWebSocketCommonConstants.SUB_PROT_CSV)) {
			lToken = CSVProcessor.packetToToken(aPacket);
		} else if (lSubProt.equals(JWebSocketCommonConstants.SUB_PROT_XML)) {
			lToken = XMLProcessor.packetToToken(aPacket);
		}
		return lToken;
	}

	/**
	 *
	 * @param aConnector
	 * @param aToken
	 * @return
	 */
	public WebSocketPacket tokenToPacket(Token aToken) {
		WebSocketPacket lPacket = null;

		if (lSubProt.equals(JWebSocketCommonConstants.SUB_PROT_JSON)) {
			lPacket = JSONProcessor.tokenToPacket(aToken);
		} else if (lSubProt.equals(JWebSocketCommonConstants.SUB_PROT_CSV)) {
			lPacket = CSVProcessor.tokenToPacket(aToken);
		} else if (lSubProt.equals(JWebSocketCommonConstants.SUB_PROT_XML)) {
			lPacket = XMLProcessor.tokenToPacket(aToken);
		}
		return lPacket;
	}

	public void sendToken(Token aToken) throws WebSocketException {
		CUR_TOKEN_ID++;
		aToken.put("utid", CUR_TOKEN_ID);
		send(tokenToPacket(aToken));
	}
	// TODO: put the following methods into client side plug-ins or separate them in a different way.

	/* functions of the System Plug-in */
	private final static String NS_SYSTEM_PLUGIN = NS_BASE + ".plugins.system";

	public void login(String aUsername, String aPassword) throws WebSocketException {
		Token lToken = new Token();
		lToken.put("type", "login");
		lToken.put("ns", NS_SYSTEM_PLUGIN);
		lToken.put("username", aUsername);
		lToken.put("password", aPassword);
		sendToken(lToken);
	}

	public void logout() throws WebSocketException {
		Token lToken = new Token();
		lToken.put("type", "logout");
		lToken.put("ns", NS_SYSTEM_PLUGIN);
		sendToken(lToken);
	}

	public void ping(boolean aEcho) throws WebSocketException {
		Token lToken = new Token();
		lToken.put("ns", NS_SYSTEM_PLUGIN);
		lToken.put("type", "ping");
		lToken.put("echo", aEcho);
		sendToken(lToken);
	}

	public void sendText(String aTarget, String aData) throws WebSocketException {
		Token lToken = new Token();
		lToken.put("ns", NS_SYSTEM_PLUGIN);
		lToken.put("type", "send");
		lToken.put("targetId", aTarget);
		lToken.put("sourceId", getClientId());
		lToken.put("sender", getUsername());
		lToken.put("data", aData);
		sendToken(lToken);
	}

	public void broadcastText(String aData) throws WebSocketException {
		Token lToken = new Token();
		lToken.put("ns", NS_SYSTEM_PLUGIN);
		lToken.put("type", "broadcast");
		lToken.put("sourceId", getClientId());
		lToken.put("sender", getUsername());
		lToken.put("data", aData);
		lToken.put("senderIncluded", false);
		lToken.put("responseRequested", true);
		sendToken(lToken);
	}

	/* functions of the Admin Plug-in */
	private final static String NS_ADMIN_PLUGIN = NS_BASE + ".plugins.admin";

	public void shutdownServer() throws WebSocketException {
		Token lToken = new Token();
		lToken.put("type", "shutdown");
		lToken.put("ns", NS_ADMIN_PLUGIN);
		sendToken(lToken);
	}
}
