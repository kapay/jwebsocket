//  ---------------------------------------------------------------------------
//  jWebSocket - ChannelPlugIn
//  Copyright (c) 2010 Innotrade GmbH, jWebSocket.org
//  ---------------------------------------------------------------------------
//  This program is free software; you can redistribute it and/or modify it
//  under the terms of the GNU Lesser General Public License as published by the
//  Free Software Foundation; either version 3 of the License, or (at your
//  option) any later version.
//  This program is distributed in the hope that it will be useful, but WITHOUT
//  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//  more details.
//  You should have received a copy of the GNU Lesser General Public License along
//  with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//  ---------------------------------------------------------------------------
package org.jwebsocket.plugins.channels;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javolution.util.FastList;
import javolution.util.FastMap;

import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.security.SecurityFactory;
import org.jwebsocket.security.User;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.util.Tools;

/**
 * Token based implementation of the channel plugin. It's based on a
 * publisher/subscriber model where channels can be either used to publish the
 * data by one or more registered publishers and subscribed by multiple
 * subscribers. The operation of channel is best handled by channel sub-protocol
 * that has to be followed by clients to publish data to the channel or
 * subscribe for receiving the data from the channels.
 * 
 ************************ PUBLISHER OPERATION***********************************
 * 
 * Token Type : <tt>publisher</tt> 
 * Namespace :  <tt>org.jwebsocket.plugins.channel</tt>
 * 
 * Token Key :  <tt>event</tt> 
 * Token Value : <tt>[authorize][publish][stop]</tt>
 * 
 * <tt>authorize</tt> event command is used for authorization of client before
 * publishing a data to the channel, publisher client has to authorize itself
 * using <tt>secret_key</tt>, <tt>access_key</tt> and <tt>login</tt> which is
 * registered in the jWebSocket server system via configuration file or from
 * other jWebSocket components.
 * 
 * <tt>Token Request Includes:</tt>
 * 
 * Token Key    : <tt>channel<tt>
 * Token Value  : <tt>channel id to authorize for</tt>
 * 
 * Token Key    : <tt>secret_key<tt>
 * Token Value  : <tt>value of the secret key</tt>
 * 
 * Token Key    : <tt>access_key<tt>
 * Token Value  : <tt>value of the access key</tt>
 * 
 * Token Key    : <tt>login<tt>
 * Token Value  : <tt>login name or id of the jWebSocket registered user</tt>
 * 
 * <tt>publish</tt>: publish event means publisher client has been authorized
 * and ready to publish the data. Data is received from the token string of key
 * <tt>data</tt>. If the channel registered is not started then it is started
 * when publish command is received for the first time.
 * 
 * <tt>Token Request Includes:</tt> 
 * 
 * Token Key    : <tt>channel<tt>
 * Token Value  : <tt>channel id to publish the data</tt>
 * 
 * Token Key    : <tt>data<tt>
 * Token Value  : <tt>data to publish to the channel</tt>
 * 
 * <tt>stop</tt>: stop event means proper shutdown of channel and no more data
 * will be received from the publisher.
 * 
 ************************ SUBSCRIBER OPERATION *****************************************
 * 
 * Token Type : <tt>subscriber</tt> Namespace :
 * <tt>org.jwebsocket.plugins.channel</tt>
 * 
 * Token Key : <tt>operation</tt> Token Value : <tt>[subscribe][unsubscribe]</tt>
 * 
 * <tt>subscribe</tt> subscribe event is to register the client as a subscriber
 * for the passed in channel and access_key if the channel is private and needs
 * access_key for subscription
 * 
 * <tt>Token Request Includes:</tt> Token Key : <tt>channel<tt>
 * Token Value  : <tt>channel id to publish the data</tt>
 * 
 * Token Key : <tt>access_key<tt>
 * Token Value  : <tt>access_key value required for subscription</tt>
 * 
 * <tt>unsubscribe</tt> removes the client from the channel so no data will be
 * broadcasted to the unsuscribed clients.
 * 
 * <tt>Token Request Includes:</tt> Token Key : <tt>channel<tt>
 * Token Value  : <tt>channel id to unsubscribe</tt>
 * 
 * @author puran
 * @version $Id$
 */
public class ChannelPlugIn extends TokenPlugIn {

	/** logger */
	private static Logger mLog = Logging.getLogger(ChannelPlugIn.class);
	/** channel manager */
	private ChannelManager mChannelManager = null;
	/** name space for channels */
	private static final String NS_CHANNELS_DEFAULT =
			JWebSocketServerConstants.NS_BASE + ".plugins.channels";
	/** empty string */
	private static final String EMPTY_STRING = "";
	/** channel plug-in handshake protocol operation values */
	private static final String AUTHORIZE = "authorize";
	private static final String PUBLISH = "publish";
	private static final String STOP = "stop";
	private static final String SUBSCRIBE = "subscribe";
	private static final String UNSUBSCRIBE = "unsubscribe";
	private static final String GET_CHANNELS = "getChannels";
	private static final String CREATE_CHANNEL = "createChannel";
	private static final String REMOVE_CHANNEL = "removeChannel";
	private static final String GET_SUBSCRIBERS = "getSubscribers";
	private static final String GET_SUBSCRIPTIONS = "getSubscriptions";
	/** channel plug-in handshake protocol parameters */
	private static final String DATA = "data";
	// private static final String EVENT = "event";
	private static final String ACCESS_KEY = "access_key";
	private static final String SECRET_KEY = "secret_key";
	private static final String CHANNEL = "channel";
	private static final String CONNECTED = "connected";

	/**
	 * Constructor with plug-in config
	 *
	 * @param aConfiguration
	 *            the plug-in configuration for this PlugIn
	 */
	public ChannelPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating channel plug-in...");
		}
		// specify default name space
		this.setNamespace(NS_CHANNELS_DEFAULT);
		mChannelManager = ChannelManager.getChannelManager(aConfiguration.getSettings());
	}

	/**
	 * {@inheritDoc} When the engine starts perform the initialization of
	 * default and system channels and start it for accepting subscriptions.
	 */
	@Override
	public void engineStarted(WebSocketEngine aEngine) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Engine started, starting channels...");
		}
		try {
			mChannelManager.startChannels();
			if (mLog.isInfoEnabled()) {
				mLog.info("Channels started.");
			}
		} catch (ChannelLifeCycleException lEx) {
			mLog.error("Failed to start channels", lEx);
		}
	}

	/**
	 * {@inheritDoc} Stops the system channels and clean up all the taken
	 * resources by those channels.
	 */
	@Override
	public void engineStopped(WebSocketEngine aEngine) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Engine stopped, stopping channels...");
		}
		try {
			// if channel manager has started at all
			// (maybe engine didn't come up)
			if (mChannelManager != null) {
				mChannelManager.stopChannels();
				if (mLog.isInfoEnabled()) {
					mLog.info("Channels stopped.");
				}
			} else
				if (mLog.isInfoEnabled()) {
					mLog.info("Channels were not yet started, properly terminated.");
				}
		} catch (ChannelLifeCycleException lEx) {
			mLog.error("Error stopping channels", lEx);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void connectorStarted(WebSocketConnector aConnector) {
		// set session id first, so that it can be processed in the
		// connectorStarted method set session id first, so that it can be
		// processed in the connectorStarted method
		// Random lRand = new Random(System.nanoTime());
		// do not set session id here!
		// aConnector.getSession().setSessionId(Tools.getMD5(aConnector.generateUID() + "." + lRand.nextInt()));
		// call super connectorStarted
		super.connectorStarted(aConnector);

		// and send the welcome message incl. the session id
		// sendWelcome(aConnector);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
		// unsubscribe from the channel, if subscribed
		Subscriber lSubscriber = mChannelManager.getSubscriber(aConnector.getId());
		if( lSubscriber != null ) {
			for (String lChannelId : lSubscriber.getChannels()) {
				Channel lChannel = mChannelManager.getChannel(lChannelId);
				if (lChannel != null) {
					lChannel.unsubscribe(lSubscriber, mChannelManager);
				}
			}
			mChannelManager.removeSubscriber(lSubscriber);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processToken(PlugInResponse aResponse,
			WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();
		if (lType != null && getNamespace().equals(lNS)) {
			if (SUBSCRIBE.equals(lType)) {
				subscribe(aConnector, aToken);
			} else if (UNSUBSCRIBE.equals(lType)) {
				unsubscribe(aConnector, aToken);
			} else if (GET_CHANNELS.equals(lType)) {
				getChannels(aConnector, aToken);
			} else if (AUTHORIZE.equals(lType)) {
				// perform the authorization
				authorize(aConnector, aToken);
			} else if (PUBLISH.equals(lType)) {
				// perform the authorization
				publish(aConnector, aToken);
			} else if (CREATE_CHANNEL.equals(lType)) {
				// perform the authorization
				createChannel(aConnector, aToken);
			} else if (REMOVE_CHANNEL.equals(lType)) {
				// perform the authorization
				removeChannel(aConnector, aToken);
			} else if (GET_SUBSCRIBERS.equals(lType)) {
				// return all subscribers for a given channel
				getSubscribers(aConnector, aToken);
			} else if (GET_SUBSCRIPTIONS.equals(lType)) {
				// return all subscriptions for a given client
				getSubscriptions(aConnector, aToken);
			} else {
				// ignore
			}
		}
	}

	/**
	 * Subscribes the connector to the channel given by the subscriber
	 *
	 * @param aConnector
	 *            the connector for this client
	 * @param aToken
	 *            the request token object
	 */
	private void subscribe(WebSocketConnector aConnector, Token aToken) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'subscribe'...");
		}
		String lChannelId = aToken.getString(CHANNEL);
		if (lChannelId == null || EMPTY_STRING.equals(lChannelId)) {
			sendError(aConnector, null, "Channel value is null");
			return;
		}
		String lAccessKey = aToken.getString(ACCESS_KEY);
		Channel lChannel = mChannelManager.getChannel(lChannelId);
		if (lChannel == null
				|| lChannel.getState() == Channel.ChannelState.STOPPED) {
			sendError(aConnector, lChannelId,
					"Channel doesn't exists for the channelId: '"
					+ lChannelId + "'");
			return;
		}
		if (lChannel.isPrivateChannel() && EMPTY_STRING.equals(lAccessKey)) {
			sendError(aConnector, lChannelId,
					"Access_key required for subscribing to a private channel: '"
					+ lChannelId + "'");
			return;
		}
		if (lChannel.isPrivateChannel() && !EMPTY_STRING.equals(lAccessKey)) {
			if (lChannel.getAccessKey().equals(lAccessKey)) {
				sendError(aConnector, lChannelId,
						"Access_key not valid for the given channel id: '"
						+ lChannelId + "'");
				return;
			}
		}
		Subscriber lSubscriber = mChannelManager.getSubscriber(aConnector.getId());
		Date lDate = new Date();
		if (lSubscriber == null) {
			lSubscriber = new Subscriber(aConnector, getServer(), lDate);
		}
		// Added by Alex: If already subscribed, return error message
		Token lResponseToken = createResponse(aToken);
		if (lSubscriber.getChannels().contains(lChannelId)) {
			lResponseToken.setInteger("code", -1);
			lResponseToken.setString("msg",
					"Client already subscribed to channel '"
					+ lChannelId + "'.");
		} else {
			lChannel.subscribe(lSubscriber, mChannelManager);
			lResponseToken.setString("subscribe", "ok");
		}
		// send the success response
		sendToken(aConnector, aConnector, lResponseToken);
	}

	/**
	 * Method for subscribers to unsubscribe from the channel. If the unsubscribe
	 * operation is successful it sends the unsubscriber - ok response to the
	 * client.
	 *
	 * @param aConnector
	 *            the connector associated with the subscriber
	 * @param aToken
	 *            the token object
	 */
	private void unsubscribe(WebSocketConnector aConnector, Token aToken) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'unsubscribe'...");
		}
		String lChannelId = aToken.getString(CHANNEL);
		if (lChannelId == null
				|| EMPTY_STRING.equals(lChannelId)) {
			sendError(aConnector, null, "Channel value is null");
			return;
		}
		Token lResponseToken = createResponse(aToken);
		Subscriber lSubscriber =
				mChannelManager.getSubscriber(aConnector.getId());
		if (lSubscriber != null) {
			Channel lChannel = mChannelManager.getChannel(lChannelId);
			if (lChannel != null) {
				lChannel.unsubscribe(lSubscriber, mChannelManager);
				lResponseToken.setString("unsubscribe", "ok");
			}
		} else {
			lResponseToken.setInteger("code", -1);
			lResponseToken.setString("msg",
					"Client not subscribed to channel '"
					+ lChannelId + "'.");
		}
		// send the success response
		sendToken(aConnector, aConnector, lResponseToken);
	}

	/**
	 * Returns all channels available to the client
	 *
	 * @param aConnector
	 *            the connector for this client
	 * @param aToken
	 *            the request token object
	 */
	private void getChannels(WebSocketConnector aConnector, Token aToken) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'getChannels'...");
		}

		// TODO: Here we probably have to introduce restrictions
		// not all clients should be allowed to retreive system or private channels
		Token lResponseToken = createResponse(aToken);

		List lChannels = new FastList();
		Map<String, Channel> lCMChannels = mChannelManager.getChannels();
		for (Map.Entry<String, Channel> lEntry : lCMChannels.entrySet()) {
			Map lItem = new FastMap();
			Channel lChannel = lEntry.getValue();
			lItem.put("id", lChannel.getId());
			lItem.put("name", lChannel.getName());
			lChannels.add(lItem);
		}
		lResponseToken.setList("channels", lChannels);

		// send the response
		sendToken(aConnector, aConnector, lResponseToken);
	}

	/**
	 * Authorize the publisher before publishing the data to the channel
	 * @param aConnector the connector associated with the publisher
	 * @param aToken the token received from the publisher client
	 * @param aChannelId the channel id
	 */
	private void authorize(WebSocketConnector aConnector, Token aToken) {
		String lChannelId = aToken.getString(CHANNEL);
		String lAccessKey = aToken.getString(ACCESS_KEY);
		String lSecretKey = aToken.getString(SECRET_KEY);
		String lLogin = aToken.getString("login");

		User lUser = SecurityFactory.getUser(lLogin);
		if (lUser == null) {
			sendError(aConnector, lChannelId,
					"'" + aConnector.getId()
					+ "' Authorization failed for channel '"
					+ lChannelId
					+ "', channel owner is not registered in the jWebSocket server system");
			return;
		}
		if (lSecretKey == null || lAccessKey == null) {
			sendError(aConnector, lChannelId, "'" + aConnector.getId()
					+ "' Authorization failed, secret_key/access_key pair value is not correct");
			return;
		} else {
			Channel lChannel = mChannelManager.getChannel(lChannelId);
			if (lChannel == null) {
				sendError(aConnector, lChannelId, "'" + aConnector.getId()
						+ "' channel not found for given channelId '" + lChannelId + "'");
				return;
			}
			Publisher lPublisher = authorizePublisher(aConnector, lChannel,
					lUser, lSecretKey, lAccessKey);
			if (!lPublisher.isAuthorized()) {
				// couldn't authorize the publisher
				sendError(aConnector, lChannelId,
						"'" + aConnector.getId()
						+ "' Authorization failed for channel '"
						+ lChannelId + "'");
			} else {
				lChannel.addPublisher(lPublisher);
				Token lResponseToken =
						mChannelManager.getChannelSuccessToken(
						aConnector, lChannelId,
						ChannelEventEnum.AUTHORIZE);
				mChannelManager.publishToLoggerChannel(lResponseToken);
				// send the success response
				sendToken(aConnector, aConnector, lResponseToken);
			}
		}
	}

	private void createChannel(WebSocketConnector aConnector, Token aToken) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'createChannel'...");
		}

		Token lResponseToken = createResponse(aToken);
		lResponseToken.setString("msg", "pending...");

		// send the response
		sendToken(aConnector, aConnector, lResponseToken);
	}

	private void removeChannel(WebSocketConnector aConnector, Token aToken) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'removeChannel'...");
		}

		Token lResponseToken = createResponse(aToken);
		lResponseToken.setString("msg", "pending...");

		// send the response
		sendToken(aConnector, aConnector, lResponseToken);
	}

	private void getSubscribers(WebSocketConnector aConnector, Token aToken) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'getSubscribers'...");
		}
		String lChannelId = aToken.getString(CHANNEL);
		Channel lChannel = mChannelManager.getChannel(lChannelId);
		if (lChannel == null) {
			sendError(aConnector, lChannelId, "'" + aConnector.getId()
					+ "' channel not found for given channelId '" + lChannelId + "'");
			return;
		}
		List<Subscriber> lChannelSubscribers = lChannel.getSubscribers();
		List<Map> lSubscribers = new FastList<Map>();
		if (null != lChannelSubscribers) {
			for (Subscriber lSubscriber : lChannelSubscribers) {
				Map<String, Object> lItem = new FastMap<String, Object>();
				lItem.put("id", lSubscriber.getId());
				lSubscribers.add(lItem);
			}
		}
		Token lResponseToken = createResponse(aToken);
		// return channel Id for client's convenience
		lResponseToken.setString("channel", lChannelId);
		lResponseToken.setList("subscribers", lSubscribers);

		// send the response
		sendToken(aConnector, aConnector, lResponseToken);
	}

	private void getSubscriptions(WebSocketConnector aConnector, Token aToken) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'getSubscriptions'...");
		}


		Subscriber lSubscriber = mChannelManager.getSubscriber(aConnector.getId());
		List<Map> lSubscriptions = new FastList<Map>();

		if (null != lSubscriber) {
			for (String lChannelId : lSubscriber.getChannels()) {
				Map lItem = new FastMap();
				lItem.put("id", lChannelId);
				lSubscriptions.add(lItem);
			}
		}
		Token lResponseToken = createResponse(aToken);
		lResponseToken.setList("subscriptions", lSubscriptions);

		// send the response
		sendToken(aConnector, aConnector, lResponseToken);
	}

	private void publish(WebSocketConnector aConnector, Token aToken) {
		String lChannelId = aToken.getString(CHANNEL);
		// String lAccessKey = aToken.getString(ACCESS_KEY);
		// String lSecretKey = aToken.getString(SECRET_KEY);
		// String lLogin = aToken.getString("login");

		Channel lChannel = mChannelManager.getChannel(lChannelId);
		Publisher lPublisher = mChannelManager.getPublisher(
				aConnector.getSession().getSessionId());

		if (lPublisher == null || !lPublisher.isAuthorized()) {
			sendError(aConnector, lChannelId, "Connector '" + aConnector.getId()
					+ "': access denied, publisher not authorized for channelId '"
					+ lChannelId + "'");
			return;
		}
		String lData = aToken.getString(DATA);
		Token lToken = mChannelManager.getChannelSuccessToken(
				aConnector, lChannelId, ChannelEventEnum.PUBLISH);
		lToken.setString("data", lData);

		// mChannelManager.publishToLoggerChannel(lToken);
		lChannel.broadcastToken(lToken);
	}

	/**
	 * Handles the operations related to the publisher.
	 *
	 * @param aConnector
	 *            the connector for this publisher
	 * @param aToken
	 *            the token data
	 */
	private void handlePublisher(WebSocketConnector aConnector, Token aToken) {
		String lChannelId = aToken.getString(CHANNEL);
		if (lChannelId == null || EMPTY_STRING.equals(lChannelId)) {
			sendError(aConnector, lChannelId, "Channel value not specified.");
			return;
		}
		if (!aToken.getNS().equals(getNamespace())) {
			sendError(aConnector, lChannelId, "Namespace '" + aToken.getNS() + "' not correct.");
			return;
		}
		// String lEvent = aToken.getString(EVENT);
		/*
		if (STOP.equals(lEvent)) {
		Publisher lPublisher = mChannelManager.getPublisher(
		aConnector.getSession().getSessionId());
		Channel lChannel = mChannelManager.getChannel(lChannelId);
		if (lChannel == null) {
		sendError(aConnector, lChannelId, "'" + aConnector.getId()
		+ "' channel not found for given channelId '"
		+ lChannelId + "'");
		return;
		}
		if (lPublisher == null || !lPublisher.isAuthorized()) {
		sendError(aConnector, lChannelId, "Connector: " + aConnector.getId()
		+ ": access denied, publisher not authorized for channelId '"
		+ lChannelId + "'");
		return;
		}
		try {
		lChannel.stop(lPublisher.getLogin());
		Token lSuccessToken = mChannelManager.getChannelSuccessToken(
		aConnector, lChannelId, ChannelEventEnum.STOP);
		sendTokenAsync(aConnector, aConnector, lSuccessToken);
		} catch (ChannelLifeCycleException lEx) {
		mLog.error("Error stopping channel '" + lChannelId
		+ "' from publisher "
		+ lPublisher.getId() + "'", lEx);

		//publish to logger channel
		Token lErrorToken = mChannelManager.getErrorToken(aConnector,
		lChannelId, "'" + aConnector.getId()
		+ "' Error stopping channel '" + lChannelId
		+ "' from publisher '" + lPublisher.getId() + "'");
		mChannelManager.publishToLoggerChannel(lErrorToken);
		sendTokenAsync(aConnector, aConnector, lErrorToken);
		}
		}
		 */
	}

	/**
	 * Validates the publisher based on the accessKey and secretKey. If the
	 * authorization fails the publisher object will have flag authorized set to
	 * false.
	 *
	 * @param aConnector
	 *            the connector for the publisher
	 * @param aChannel
	 *            the channel to publish
	 * @param aUser
	 *            the user object that represents the publisher
	 * @param aSecretKey
	 *            the secretKey value from the publisher
	 * @param aAccessKey
	 *            the accessKey value from the publisher
	 * @return the publisher object
	 */
	private Publisher authorizePublisher(WebSocketConnector aConnector,
			Channel aChannel, User aUser, String aSecretKey,
			String aAccessKey) {
		Publisher lPublisher = null;
		Date lNow = new Date();
		// TODO: Commented our by Alex: Why may only the owner publish something ?
		if (aChannel.getAccessKey().equals(aAccessKey)
				&& aChannel.getSecretKey().equals(aSecretKey) /* && user.getLoginname().equals(channel.getOwner())*/) {
			lPublisher = new Publisher(aConnector, aUser.getLoginname(),
					aChannel.getId(), lNow, lNow, true);
			mChannelManager.storePublisher(lPublisher);
		} else {
			lPublisher = new Publisher(aConnector, aUser.getLoginname(),
					aChannel.getId(), lNow, lNow, false);
		}
		return lPublisher;
	}

	/**
	 * Send the error response to the client as well as publish the error log to
	 * the logger channel for monitoring
	 *
	 * @param aConnector
	 *            the target connector object
	 * @param aChannel
	 *            the target channel id, can be null if channel is not
	 *            initialized
	 * @param error
	 *            the error message
	 */
	private void sendError(WebSocketConnector aConnector, String aChannel, String aError) {
		Token lErrorToken = mChannelManager.getErrorToken(
				aConnector, aChannel, aError);

		// publish to logger channel
		mChannelManager.publishToLoggerChannel(lErrorToken);

		// send the error to the client
		sendTokenAsync(aConnector, aConnector, lErrorToken);
	}

	/**
	 * Send connected message to the publisher/subscriber after successful
	 * session id creation remember that this doesn't mean the client the
	 * publisher or subscriber is authorized
	 *
	 * @param aConnector
	 *            the connector object
	 */
	// TODO: We need to implement the channel welcome a bit different!
	// TODO: No separate session id here!
	private void sendWelcome(WebSocketConnector aConnector) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Sending connected message to the channels");
		}
		// send "welcome" token to client
		Token lWelcome = TokenFactory.createToken(CONNECTED);
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

		ChannelManager.getLoggerChannel().broadcastToken(lWelcome);
		sendTokenAsync(aConnector, aConnector, lWelcome);
	}
}
