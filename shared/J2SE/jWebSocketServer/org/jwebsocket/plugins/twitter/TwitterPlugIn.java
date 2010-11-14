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
import javolution.util.FastList;

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
import twitter4j.User;
import twitter4j.http.AccessToken;
import twitter4j.http.RequestToken;

/**
 *
 * @author aschulze
 */
public class TwitterPlugIn extends TokenPlugIn {

	private static Logger mLog = Logging.getLogger(TwitterPlugIn.class);
	private static final String TWITTER_VAR = "$twitter";
	private static final String OAUTH_REQUEST_TOKEN = "$twUsrReqTok";
	private static final String OAUTH_VERIFIER = "$twUsrVerifier";
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
		CONSUMER_KEY = getSetting(CONSUMER_KEY_KEY, null);
		CONSUMER_SECRET = getSetting(CONSUMER_SECRET_KEY, null);
		try {
			APP_ID = Integer.parseInt(getSetting(APP_ID_KEY, "0"));
		} catch (Exception lEx) {
			APP_ID = 0;
		}
		ACCESSTOKEN_KEY = getSetting(ACCESSTOKEN_KEY_KEY, null);
		ACCESSTOKEN_SECRET = getSetting(ACCESSTOKEN_SECRET_KEY, null);
	}

	@Override
	public void processToken(PlugInResponse aResponse,
			WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();

		if (lType != null && (lNS == null || lNS.equals(getNamespace()))) {
			if (lType.equals("tweet")) {
				tweet(aConnector, aToken);
			} else if (lType.equals("login")) {
				login(aConnector, aToken);
			} else if (lType.equals("logout")) {
				logout(aConnector, aToken);
			} else if (lType.equals("getTimeline")) {
				getTimeline(aConnector, aToken);
			} else if (lType.equals("getUserData")) {
				getUserData(aConnector, aToken);
			} else if (lType.equals("setVerifier")) {
				setVerifier(aConnector, aToken);
			}

		}
	}

	public void connectorStopped(WebSocketConnector aConnector) {
		aConnector.removeVar(TWITTER_VAR);
	}

	private boolean mCheckAuth(Token aToken) {
		String lMsg;
		try {
			if (mTwitter == null) {
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

	private void login(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		// instantiate response token
		Token lResponse = lServer.createResponse(aToken);
		String lMsg;
		try {
			if (!mCheckAuth(lResponse)) {
				mLog.error(lResponse.getString("msg"));
			} else {
				TwitterFactory lTwitterFactory = new TwitterFactory();
				Twitter lTwitter = lTwitterFactory.getInstance();
				lTwitter.setOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
				// pass callback URL to Twitter API
				RequestToken lReqToken = lTwitter.getOAuthRequestToken("http://localhost/demos/twitter/twauth.htm?isAuth=true");

				lMsg = "URLs";
				lResponse.setString("authenticationURL", lReqToken.getAuthenticationURL());
				lResponse.setString("authorizationURL", lReqToken.getAuthorizationURL());
				lResponse.setString("msg", lMsg);
				if (mLog.isInfoEnabled()) {
					mLog.info(lMsg);
				}

				// every connector maintains it's own twitter connection
				aConnector.setVar(TWITTER_VAR, lTwitter);
				// persist the request token, it's required
				// to get access token from verifier
				aConnector.setVar(OAUTH_REQUEST_TOKEN, lReqToken);
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
				Twitter lTwitter = (Twitter) aConnector.getVar(TWITTER_VAR);
				if (lTwitter != null) {
					lTwitter.shutdown();
					lResponse.setString("msg", "Twitter instance has been shut down.");
				} else {
					lResponse.setString("msg", "Twitter instance down (not up before).");
				}
				aConnector.removeVar(TWITTER_VAR);
				aConnector.removeVar(OAUTH_REQUEST_TOKEN);
				aConnector.removeVar(OAUTH_VERIFIER);
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

	/*
	 * Gets the Twitter timeline for a given user. If no user is given
	 * the user registered for the app is used as default.
	 */
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
				// getting timelines is public so we can use the mTwitter object here
				if (lUsername != null && lUsername.length() > 0) {
					lStatuses = mTwitter.getUserTimeline(lUsername);
				} else {
					lStatuses = mTwitter.getUserTimeline();
				}
				// return the list of messages as an array of strings...
				FastList<String> lMessages = new FastList<String>();
				for (Status lStatus : lStatuses) {
					lMessages.add(lStatus.getUser().getName() + ": " + lStatus.getText());
					/*
					// If each status is supposed to be sent separately...
					Token lItem = TokenFactory.createToken(NS_TWITTER, BaseToken.TT_EVENT);
					lItem.setString("username", lStatus.getUser().getName());
					lItem.setString("message", lStatus.getText());
					lServer.sendToken(aConnector, lItem);
					 */
				}
				lResponse.setList("messages", lMessages);
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

	private void getUserData(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		// instantiate response token
		Token lResponse = lServer.createResponse(aToken);
		String lUsername = aToken.getString("username");
		Integer lUserId = aToken.getInteger("userid");
		try {
			User lUser = null;
			// if user id is given use this to get user data
			if (lUserId != null && lUserId != 0) {
				lUser = mTwitter.showUser(lUserId);
				// if user name is given use this to get user data
			} else if (lUsername != null && lUsername.length() > 0) {
				lUser = mTwitter.showUser(lUsername);
				// otherwise return user data of provider (ourselves)
			} else {
				lUser = mTwitter.verifyCredentials();
			}
			if (lUser != null) {
				lResponse.setString("screenname", lUser.getScreenName());
				lResponse.setInteger("id", lUser.getId());
				lResponse.setString("description", lUser.getDescription());
				lResponse.setString("location", lUser.getLocation());
				lResponse.setString("lang", lUser.getLang());
				lResponse.setString("name", lUser.getName());
			} else {
				lResponse.setInteger("code", -1);
				lResponse.setString("msg", "Neither UserId nor Username passed.");
			}
		} catch (Exception lEx) {
			String lMsg = lEx.getClass().getSimpleName() + ": " + lEx.getMessage();
			mLog.error(lMsg);
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", lMsg);
		}

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}

	private void setVerifier(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		// instantiate response token
		String lVerifier = aToken.getString("verifier");
		aConnector.setString(OAUTH_VERIFIER, lVerifier);
	}

	private void tweet(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		// instantiate response token
		Token lResponse = lServer.createResponse(aToken);
		String lMsg = aToken.getString("message");
		try {
			// to send tweet we need an authenticated user
			Twitter lTwitter = (Twitter) aConnector.getVar(TWITTER_VAR);
			RequestToken lReqToken = (RequestToken) aConnector.getVar(OAUTH_REQUEST_TOKEN);
			String lVerifier = aConnector.getString(OAUTH_VERIFIER);
			AccessToken lAccessToken = lTwitter.getOAuthAccessToken(lReqToken, lVerifier);
			lTwitter.setOAuthAccessToken(lAccessToken);

			if (lTwitter == null) {
				lResponse.setInteger("code", -1);
				lResponse.setString("msg", "Not yet authenticated against Twitter!");
			} else if (lMsg == null || lMsg.length() <= 0) {
				lResponse.setInteger("code", -1);
				lResponse.setString("msg", "No message passed for tweet.");
			} else {
				lTwitter.updateStatus(lMsg);
				lMsg = "Twitter status successfully updated for user '" + lTwitter.getScreenName() + "'.";
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
