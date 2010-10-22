//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket Twitter Plug-In
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
package org.jwebsocket.plugins.twitter;

import java.util.List;

import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.Token;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.http.AccessToken;
import twitter4j.http.RequestToken;

/**
 *
 * @author aschulze
 */
public class TwitterPlugIn extends TokenPlugIn {

	private static Logger mLog = Logging.getLogger(TwitterPlugIn.class);
	private static String TWITTER_USER = null;
	private static final String TWITTER_USER_KEY = "username";
	private static String TWITTER_PASSWORD = null;
	private static final String TWITTER_PASSWORD_KEY = "password";
	private static String CONSUMER_KEY = null;
	private static final String CONSUMER_KEY_KEY = "consumer_key";
	private static String CONSUMER_SECRET = null;
	private static final String CONSUMER_SECRET_KEY = "consumer_secret";
	private static Integer APP_ID = null;
	private static final String APP_ID_KEY = "app_id";
	private static String ACCESSTOKEN_KEY = null;
	private static final String ACCESSTOKEN_KEY_KEY = "accesstoken_key";
	private static String ACCESSTOKEN_SECRET = null;
	private static final String ACCESSTOKEN_SECRET_KEY = "accesstoken_secret";
	// if namespace changed update client plug-in accordingly!
	private static final String NS_TWITTER = JWebSocketServerConstants.NS_BASE + ".plugins.twitter";
	private Twitter mTwitter = null;

	public TwitterPlugIn() {
		super(null);
	}

	public TwitterPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating Twitter plug-in...");
		}
		// specify default name space for admin plugin
		this.setNamespace(NS_TWITTER);
		mGetSettings();
	}

	private void mGetSettings() {
		TWITTER_USER = getSetting(TWITTER_USER_KEY, null);
		TWITTER_PASSWORD = getSetting(TWITTER_PASSWORD_KEY, null);
		CONSUMER_KEY = getSetting(CONSUMER_KEY_KEY, null);
		CONSUMER_SECRET = getSetting(CONSUMER_SECRET_KEY, null);
		APP_ID = Integer.parseInt(getSetting(APP_ID_KEY, "0"));
		ACCESSTOKEN_KEY = getSetting(ACCESSTOKEN_KEY_KEY, null);
		ACCESSTOKEN_SECRET = getSetting(ACCESSTOKEN_SECRET_KEY, null);
	}

	@Override
	public void processToken(PlugInResponse aResponse,
			WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();

		if (lType != null && (lNS == null || lNS.equals(getNamespace()))) {
			// select from database
			mGetSettings();
			if (lType.equals("tweet")) {
				tweet(aConnector, aToken);
			} else if (lType.equals("auth")) {
				auth(aConnector, aToken);
			} else if (lType.equals("getTimeline")) {
				getTimeline(aConnector, aToken);
			}
		}
	}

	private boolean mCheckAuth(Token aToken) {
		String lMsg;
		try {
			if (mTwitter == null) {
				mGetSettings();
				if (mLog.isDebugEnabled()) {
					mLog.debug("Authenticating against Twitter...");
				}
				// The factory instance is re-useable and thread safe.
				TwitterFactory lTwitterFactory = new TwitterFactory();
				mTwitter = lTwitterFactory.getInstance();
				mTwitter.setOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
				AccessToken lAccessToken = new AccessToken(ACCESSTOKEN_KEY, ACCESSTOKEN_SECRET);
				mTwitter.setOAuthAccessToken(lAccessToken);
				lMsg = "Successfully authenticated against Twitter.";
			} else {
				lMsg = "Already authenticated against Twitter.";
			}
			if (mLog.isInfoEnabled()) {
				mLog.info(lMsg);
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

	private void auth(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		// instantiate response token
		Token lResponse = lServer.createResponse(aToken);
		String lMsg;
		String lUsername = aToken.getString("username");
		String lPassword = aToken.getString("password");
		try {
			if (lUsername == null || lUsername.length() <= 0) {
				lResponse.setInteger("code", -1);
				lResponse.setString("msg", "No username passed for Twitter authentication.");
			} else if (lPassword == null || lPassword.length() <= 0) {
				lResponse.setInteger("code", -1);
				lResponse.setString("msg", "No password passed for Twitter authentication.");
			} else if (!mCheckAuth(lResponse)) {
				mLog.error(lResponse.getString("msg"));
			} else {

				TwitterFactory lTwitterFactory = new TwitterFactory();
				Twitter lTwitter = lTwitterFactory.getInstance();
				lTwitter.setOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
				RequestToken lReqToken = lTwitter.getOAuthRequestToken();

				lMsg = "URLs.";
				lResponse.setString("authenticationURL", lReqToken.getAuthenticationURL());
				lResponse.setString("authorizationURL", lReqToken.getAuthorizationURL());
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

	private void getTimeline(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		// instantiate response token
		Token lResponse = lServer.createResponse(aToken);
		String lMsg = "";
		String lUsername = aToken.getString("username");

		try {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Receiving timeline for user '"
						+ (lUsername != null ? lUsername : "[not given]")
						+ "'...");
			}
			if (!mCheckAuth(lResponse)) {
				mLog.error(lResponse.getString("msg"));
			} else {
				List<Status> lStatuses;
				if (lUsername != null && lUsername.length() > 0) {
					lStatuses = mTwitter.getUserTimeline(lUsername);
				} else {
					lStatuses = mTwitter.getUserTimeline();
				}
				lMsg = "";
				for (Status lStatus : lStatuses) {
					lMsg += lStatus.getUser().getName() + ": " + lStatus.getText() + "<br>";
				}
				lResponse.setString("msg", lMsg);
				if (mLog.isInfoEnabled()) {
					mLog.info("Twitter timeline for user '"
							+ (lUsername != null ? lUsername : "[not given]")
							+ "' successfully received");
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

	private void tweet(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		// instantiate response token
		Token lResponse = lServer.createResponse(aToken);
		String lMsg = aToken.getString("message");
		String lUsername = aToken.getString("username");
		String lPassword = aToken.getString("password");

		try {
			if (lMsg == null || lMsg.length() <= 0) {
				lResponse.setInteger("code", -1);
				lResponse.setString("msg", "No message passed for tweet.");
			} else if (lUsername == null || lUsername.length() <= 0) {
				lResponse.setInteger("code", -1);
				lResponse.setString("msg", "No username passed for tweet.");
			} else if (lPassword == null || lPassword.length() <= 0) {
				lResponse.setInteger("code", -1);
				lResponse.setString("msg", "No password passed for tweet.");
			} else if (!mCheckAuth(lResponse)) {
				mLog.error(lResponse.getString("msg"));
			} else {
				mTwitter.updateStatus(lMsg);
				lMsg = "Twitter status successfully updated for user '" + lUsername + "'.";
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
}
