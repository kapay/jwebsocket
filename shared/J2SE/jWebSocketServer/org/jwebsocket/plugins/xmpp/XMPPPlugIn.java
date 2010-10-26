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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.RosterPacket.ItemStatus;
import org.jivesoftware.smack.packet.RosterPacket.ItemType;
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
 * Nice tips at: http://www.adarshr.com/papers/xmpp and http://www.adarshr.com/papers/xmpp2
 * Smack Java Docs: http://www.igniterealtime.org/builds/smack/docs/latest/javadoc/
 * 
 */
public class XMPPPlugIn extends TokenPlugIn {

	private static Logger mLog = Logging.getLogger(XMPPPlugIn.class);
	private static final String XMPP_CONN_VAR = "$xmpp_connection";
	private static final String XMPP_CRED_VAR = "$xmpp_credentials";
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
	}

	@Override
	public void processToken(PlugInResponse aResponse,
			WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();

		if (lType != null && (lNS == null || lNS.equals(getNamespace()))) {
			// select from database
			mGetSettings();
			if (lType.equals("connect")) {
				connect(aConnector, aToken);
			} else if (lType.equals("login")) {
				login(aConnector, aToken);
			} else if (lType.equals("logout")) {
				logout(aConnector, aToken);
			} else if (lType.equals("disconnect")) {
				disconnect(aConnector, aToken);
			} else if (lType.equals("getRoster")) {
				getRoster(aConnector, aToken);
			} else if (lType.equals("setStatus")) {
				setStatus(aConnector, aToken);
			}
		}
	}

	private XMPPConnection getXMPPConnection(WebSocketConnector aConnector) {
		return (XMPPConnection) aConnector.getVar(XMPP_CONN_VAR);
	}

	private void setXMPPConnection(WebSocketConnector aConnector, XMPPConnection aConnection) {
		aConnector.setVar(XMPP_CONN_VAR, aConnection);
	}

	private void removeXMPPConnection(WebSocketConnector aConnector) {
		aConnector.removeVar(XMPP_CONN_VAR);
	}

	private class Credentials {

		private String mUsername = null;
		private String mPassword = null;
		private String mHost = null;
		private Integer mPort = null;
		private String mDomain = null;
		private Boolean mUseSSL = null;

		public Credentials(String aUsername, String aPassword,
				String aHost, Integer aPort, String aDomain, Boolean aUseSSL) {
			mUsername = aUsername;
			mPassword = aPassword;
			mHost = aHost;
			mPort = aPort;
			mDomain = aDomain;
			mUseSSL = aUseSSL;
		}

		public Credentials(String aHost, Integer aPort,
				String aDomain, Boolean aUseSSL) {
			mHost = aHost;
			mPort = aPort;
			mDomain = aDomain;
			mUseSSL = aUseSSL;
		}

		public Credentials(String aUsername, String aPassword) {
			mUsername = aUsername;
			mPassword = aPassword;
		}

		public boolean validateConnection() {
			return (mHost != null && mHost.length() > 0)
					&& (mPort != null)
					&& (mUseSSL != null);
		}

		public boolean validateUser() {
			return (mUsername != null && mUsername.length() > 0)
					&& (mPassword != null && mPassword.length() > 0);
		}

		/**
		 * @return the mUsername
		 */
		public String getUsername() {
			return mUsername;
		}

		/**
		 * @return the mPassword
		 */
		public String getPassword() {
			return mPassword;
		}

		/**
		 * @return the mHost
		 */
		public String getHost() {
			return mHost;
		}

		/**
		 * @return the mPort
		 */
		public Integer getPort() {
			return mPort;
		}

		/**
		 * @return the mUseSSL
		 */
		public Boolean getUseSSL() {
			return mUseSSL;
		}

		/**
		 * @param aUsername the mUsername to set
		 */
		public void setUsername(String aUsername) {
			this.mUsername = aUsername;
		}

		/**
		 * @param aPassword the mPassword to set
		 */
		public void setPassword(String aPassword) {
			this.mPassword = aPassword;
		}

		/**
		 * @param aHost the mHost to set
		 */
		public void setHost(String aHost) {
			this.mHost = aHost;
		}

		/**
		 * @param aPort the mPort to set
		 */
		public void setPort(Integer aPort) {
			this.mPort = aPort;
		}

		/**
		 * @param aUseSSL the mUseSSL to set
		 */
		public void setUseSSL(Boolean aUseSSL) {
			this.mUseSSL = aUseSSL;
		}

		/**
		 * @return the mDomain
		 */
		public String getDomain() {
			return mDomain;
		}

		/**
		 * @param mDomain the mDomain to set
		 */
		public void setmDomain(String mDomain) {
			this.mDomain = mDomain;
		}
	}

	private Credentials getCredentials(WebSocketConnector aConnector) {
		return (Credentials) aConnector.getVar(XMPP_CRED_VAR);
	}

	private void setCredentials(WebSocketConnector aConnector, Credentials aCredentials) {
		aConnector.setVar(XMPP_CRED_VAR, aCredentials);
	}

	private void removeCredentials(WebSocketConnector aConnector) {
		aConnector.removeVar(XMPP_CRED_VAR);
	}

	private void connect(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		// instantiate response token
		Token lResponse = lServer.createResponse(aToken);
		String lMsg = "";

		Credentials lCredentials = new Credentials(
				aToken.getString("host"),
				aToken.getInteger("port"),
				aToken.getString("domain"),
				aToken.getBoolean("useSSL"));
		if (!lCredentials.validateConnection()) {
			lMsg = "Invalid or incomplete connection data for XMPP server.";
			mLog.error(lMsg);
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", lMsg);
			// send response to requester
			lServer.sendToken(aConnector, lResponse);
			return;
		}

		try {
			// check if already connected to the same or a different server.
			XMPPConnection lXMPPConn = getXMPPConnection(aConnector);
			if (lXMPPConn == null) {
				// Create a connection to the xmpp server on a specific port.
				ConnectionConfiguration lConnCfg =
						new ConnectionConfiguration(
						lCredentials.getHost(),
						lCredentials.getPort(),
						lCredentials.getDomain());
				// especially for google talk!
				lConnCfg.setSASLAuthenticationEnabled(false);
				lXMPPConn = new XMPPConnection(lConnCfg);
			}
			if (!lXMPPConn.isConnected()) {
				lXMPPConn.connect();
			}
			lMsg = "Successfully connected to XMPP server";
			lResponse.setString("msg", lMsg);
			if (mLog.isInfoEnabled()) {
				mLog.info(lMsg);
			}
			setXMPPConnection(aConnector, lXMPPConn);
		} catch (Exception lEx) {
			lMsg = lEx.getClass().getSimpleName() + ": " + lEx.getMessage();
			mLog.error(lMsg);
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", lMsg);

			removeXMPPConnection(aConnector);
		}

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}

	private void disconnect(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		// instantiate response token
		Token lResponse = lServer.createResponse(aToken);
		String lMsg;
		try {
			// check if already connected to the same or a different server.
			XMPPConnection lXMPPConn = getXMPPConnection(aConnector);
			if (lXMPPConn != null && lXMPPConn.isConnected()) {
				lXMPPConn.disconnect();
				lMsg = "Successfully disconnected from XMPP server";
			} else {
				lResponse.setInteger("code", -1);
				lMsg = "Not connected to XMPP server";
			}
			if (mLog.isInfoEnabled()) {
				mLog.info(lMsg);
			}
			lResponse.setString("msg", lMsg);
		} catch (Exception lEx) {
			lMsg = lEx.getClass().getSimpleName() + ": " + lEx.getMessage();
			mLog.error(lMsg);
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", lMsg);
		}
		removeXMPPConnection(aConnector);

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}

	private void login(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		// instantiate response token
		Token lResponse = lServer.createResponse(aToken);
		String lMsg = "";

		String lUsername = aToken.getString("username");
		String lPassword = aToken.getString("password");

		try {
			XMPPConnection lXMPPConn = getXMPPConnection(aConnector);
			if (lUsername == null || lUsername.length() <= 0) {
				lMsg = "No username passed for XMPP login.";
				if (mLog.isDebugEnabled()) {
					mLog.debug(lMsg);
				}
				lResponse.setInteger("code", -1);
				lResponse.setString("msg", lMsg);
			} else if (lPassword == null || lPassword.length() <= 0) {
				lMsg = "No password passed for XMPP login.";
				if (mLog.isDebugEnabled()) {
					mLog.debug(lMsg);
				}
				lResponse.setInteger("code", -1);
				lResponse.setString("msg", lMsg);
			} else if (lXMPPConn == null || !lXMPPConn.isConnected()) {
				lMsg = "Please connect first before trying to log in.";
				if (mLog.isDebugEnabled()) {
					mLog.debug(lMsg);
				}
				lResponse.setInteger("code", -1);
				lResponse.setString("msg", lMsg);
			} else if (lXMPPConn != null && lXMPPConn.isAuthenticated()) {
				lMsg = "You are already logged in, logout and re-login to change account.";
				if (mLog.isDebugEnabled()) {
					mLog.debug(lMsg);
				}
				lResponse.setInteger("code", -1);
				lResponse.setString("msg", lMsg);
			} else {
				lXMPPConn.login(lUsername, lPassword, "athome");
				lMsg = "Successfully authenticated against XMPP server.";
				lResponse.setString("msg", lMsg);
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
			// check if already connected to the same or a different server.
			XMPPConnection lXMPPConn = getXMPPConnection(aConnector);
			if (lXMPPConn != null && lXMPPConn.isConnected()) {
				lXMPPConn.loginAnonymously();
				lMsg = "Successfully logged out from XMPP server";
			} else {
				lResponse.setInteger("code", -1);
				lMsg = "Not logged in into XMPP server";
			}
			if (mLog.isInfoEnabled()) {
				mLog.info(lMsg);
			}
			lResponse.setString("msg", lMsg);
		} catch (Exception lEx) {
			lMsg = lEx.getClass().getSimpleName() + ": " + lEx.getMessage();
			mLog.error(lMsg);
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", lMsg);
		}
		removeXMPPConnection(aConnector);

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}

	private void getRoster(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		// instantiate response token
		Token lResponse = lServer.createResponse(aToken);
		String lMsg;
		try {
			// check if already connected to the same or a different server.
			XMPPConnection lXMPPConn = getXMPPConnection(aConnector);
			if (lXMPPConn != null && lXMPPConn.isConnected()) {
				Roster lRoster = lXMPPConn.getRoster();
				if (lRoster != null) {
					Collection<RosterEntry> lEntries = lRoster.getEntries();
					List lItems = new FastList();
					for (RosterEntry lEntry : lEntries) {
						Map lItem = new FastMap();
						lItem.put("name", lEntry.getName());
						ItemStatus lStatus = lEntry.getStatus();
						lItem.put("status", (lStatus != null ? lStatus.toString() : "?"));
						lItem.put("user", lEntry.getUser());
						ItemType lType = lEntry.getType();
						lItem.put("type", (lType != null ? lType.name() : "?"));
						lItems.add(lItem);
					}
					lResponse.setList("roster", lItems);
					lMsg = "Roster successfully received.";
				} else {
					lResponse.setInteger("code", -1);
					lMsg = "Roster could not be obtained.";
				}
			} else {
				lResponse.setInteger("code", -1);
				lMsg = "Not connected or logged in into XMPP server";
			}
			if (mLog.isInfoEnabled()) {
				mLog.info(lMsg);
			}
			lResponse.setString("msg", lMsg);
		} catch (Exception lEx) {
			lMsg = lEx.getClass().getSimpleName() + ": " + lEx.getMessage();
			mLog.error(lMsg);
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", lMsg);
		}

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}

	private void setStatus(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		// instantiate response token
		Token lResponse = lServer.createResponse(aToken);
		String lMsg;

		String lStatus = aToken.getString("status");

		try {
			// check if already connected to the same or a different server.
			XMPPConnection lXMPPConn = getXMPPConnection(aConnector);
			if (lXMPPConn != null && lXMPPConn.isConnected()) {
				// Create a new presence. Pass in false to indicate we're unavailable.
				Presence lPresence = new Presence(Presence.Type.available);
				// Set the highest priority
				lPresence.setPriority(24);
				lPresence.setStatus(lStatus);
				lPresence.setMode(Presence.Mode.chat);
				// send the update
				lXMPPConn.sendPacket(lPresence);
				lMsg = "Status successfully sent to '" + lStatus + "'.";
			} else {
				lResponse.setInteger("code", -1);
				lMsg = "No XMPP connection or authentication.";
			}
			if (mLog.isInfoEnabled()) {
				mLog.info(lMsg);
			}
			lResponse.setString("msg", lMsg);
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
