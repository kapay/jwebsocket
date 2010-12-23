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
import java.util.Random;

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
	/** namespace for channels */
	private static final String NS_CHANNELS_DEFAULT = JWebSocketServerConstants.NS_BASE + ".plugins.channels";
	/** empty string */
	private static final String EMPTY_STRING = "";
	/** publisher request string */
	private static final String PUBLISHER = "publisher";
	/** subscriber request string */
	private static final String SUBSCRIBER = "subscriber";
	/** channel plugin handshake protocol operation values */
	private static final String AUTHORIZE = "authorize";
	private static final String PUBLISH = "publish";
	private static final String STOP = "stop";
	private static final String SUBSCRIBE = "subscribe";
	private static final String UNSUBSCRIBE = "unsubscribe";
	/** channel plugin handshake protocol parameters */
	private static final String DATA = "data";
	private static final String EVENT = "event";
	private static final String ACCESS_KEY = "access_key";
	private static final String SECRET_KEY = "secret_key";
	private static final String CHANNEL = "channel";
	private static final String CONNECTED = "connected";

	/**
	 * Constructor with plugin config
	 *
	 * @param aConfiguration
	 *            the plugin configuration for this PlugIn
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
			mLog.debug("Engine started, starting system channels...");
		}
		try {
			mChannelManager.startSystemChannels();
			if (mLog.isInfoEnabled()) {
				mLog.info("System channels started.");
			}
		} catch (ChannelLifeCycleException e) {
			mLog.error("Failed to start system channels", e);
		}
	}

	/**
	 * {@inheritDoc} Stops the system channels and clean up all the taken
	 * resources by those channels.
	 */
	@Override
	public void engineStopped(WebSocketEngine aEngine) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Engine stopped, stopping system channels...");
		}
		try {
			mChannelManager.stopSystemChannels();
			if (mLog.isInfoEnabled()) {
				mLog.info("System channels stopped.");
			}
		} catch (ChannelLifeCycleException e) {
			mLog.error("Error stopping system channels", e);
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
		Random rand = new Random(System.nanoTime());

		aConnector.getSession().setSessionId(Tools.getMD5(aConnector.generateUID() + "." + rand.nextInt()));
		// call super connectorStarted
		super.connectorStarted(aConnector);
		// and send the welcome message incl. the session id
		sendWelcome(aConnector);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processToken(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {
		String type = aToken.getType();
		String namespace = aToken.getNS();
		if (type != null && (namespace == null || namespace.equals(getNamespace()))) {
			if (type.equals(PUBLISHER)) {
				handlePublisher(aConnector, aToken);
			} else if (type.equals(SUBSCRIBER)) {
				handleSubscriber(aConnector, aToken);
			} else {
				// ignore
			}
		}
	}

	/**
	 * Handles all the operation related to subscriber based on the subscriber
	 * commands
	 *
	 * @param aConnector
	 *            the subscriber connector object
	 * @param aToken
	 *            the the publisher connector object
	 */
	private void handleSubscriber(WebSocketConnector aConnector, Token aToken) {
		String lEvent = aToken.getString(EVENT);
		if (SUBSCRIBE.equals(lEvent)) {
			subscribe(aConnector, aToken);
		} else if (UNSUBSCRIBE.equals(lEvent)) {
			unsubscribe(aConnector, aToken);
		} else {
			// no command, close the connector
			// TODO: Don't close connection here! We'll introduce a "not processed" token.
			aConnector.stopConnector(CloseReason.CLIENT);
		}
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
		String channelId = aToken.getString(CHANNEL);
		if (channelId == null || EMPTY_STRING.equals(channelId)) {
			sendError(aConnector, channelId, "Channel value not specified.");
			return;
		}
		if (!aToken.getNS().equals(getNamespace())) {
			sendError(aConnector, channelId, "Namespace '" + aToken.getNS() + "' not correct.");
			return;
		}
		String event = aToken.getString(EVENT);
		if (AUTHORIZE.equals(event)) {
			// perform the authorization
			authorize(aConnector, aToken, channelId);

		} else if (PUBLISH.equals(event)) {
			Channel channel = mChannelManager.getChannel(channelId);
			Publisher publisher = mChannelManager.getPublisher(
					aConnector.getSession().getSessionId());

			if (publisher == null || !publisher.isAuthorized()) {
				sendError(aConnector, channelId, "Connector '" + aConnector.getId()
						+ "': access denied, publisher not authorized for channelId '"
						+ channelId + "'");
				return;
			}
			String data = aToken.getString(DATA);
			Token dataToken = mChannelManager.getChannelSuccessToken(
					aConnector, channelId, ChannelEventEnum.PUBLISH);
			dataToken.setString("data", data);

			mChannelManager.publishToLoggerChannel(dataToken);
			channel.broadcastToken(dataToken);

		} else if (STOP.equals(event)) {
			Publisher publisher = mChannelManager.getPublisher(
					aConnector.getSession().getSessionId());
			Channel channel = mChannelManager.getChannel(channelId);
			if (channel == null) {
				sendError(aConnector, channelId, "'" + aConnector.getId()
						+ "' channel not found for given channelId '"
						+ channelId + "'");
				return;
			}
			if (publisher == null || !publisher.isAuthorized()) {
				sendError(aConnector, channelId, "Connector: " + aConnector.getId()
						+ ": access denied, publisher not authorized for channelId '"
						+ channelId + "'");
				return;
			}
			try {
				channel.stop(publisher.getLogin());
				Token successToken = mChannelManager.getChannelSuccessToken(
						aConnector, channelId, ChannelEventEnum.STOP);
				sendTokenAsync(aConnector, aConnector, successToken);
			} catch (ChannelLifeCycleException e) {
				mLog.error("Error stopping channel '" + channelId
						+ "' from publisher "
						+ publisher.getId() + "'", e);

				//publish to logger channel
				Token errorToken = mChannelManager.getErrorToken(aConnector,
						channelId, "'" + aConnector.getId()
						+ "' Error stopping channel '" + channelId
						+ "' from publisher '" + publisher.getId() + "'");
				mChannelManager.publishToLoggerChannel(errorToken);
				sendTokenAsync(aConnector, aConnector, errorToken);
			}
		}
	}

	/**
	 * Authorize the publisher before publishing the data to the channel
	 * @param aConnector the connector associated with the publisher
	 * @param aToken the token received from the publisher client
	 * @param channelId the channel id
	 */
	private void authorize(WebSocketConnector aConnector, Token aToken, String channelId) {
		String accessKey = aToken.getString(ACCESS_KEY);
		String secretKey = aToken.getString(SECRET_KEY);
		String login = aToken.getString("login");

		User user = SecurityFactory.getUser(login);
		if (user == null) {
			sendError(aConnector, channelId, "[" + aConnector.getId() + "] Authorization failed for channel:["
					+ channelId + "], channel owner is not registered in the jWebSocket server system");
			return;
		}
		if (secretKey == null || accessKey == null) {
			sendError(aConnector, channelId, "[" + aConnector.getId()
					+ "] Authorization failed, secret_key/access_key pair value is not correct");
			return;
		} else {
			Channel channel = mChannelManager.getChannel(channelId);
			if (channel == null) {
				sendError(aConnector, channelId, "[" + aConnector.getId()
						+ "] channel not found for given channelId:[" + channelId + "]");
				return;
			}
			Publisher publisher = authorizePublisher(aConnector, channel, user, secretKey, accessKey);
			if (!publisher.isAuthorized()) {
				// couldn't authorize the publisher
				sendError(aConnector, channelId, "[" + aConnector.getId() + "] Authorization failed for channel:["
						+ channelId + "]");
			} else {
				channel.addPublisher(publisher);
				Token responseToken = mChannelManager.getChannelSuccessToken(aConnector, channelId,
						ChannelEventEnum.AUTHORIZE);
				mChannelManager.publishToLoggerChannel(responseToken);
				// send the success response
				sendToken(aConnector, aConnector, responseToken);
			}
		}
	}

	/**
	 * Validates the publisher based on the accessKey and secretKey. If the
	 * authorization fails the publisher object will have flag authorized set to
	 * false.
	 *
	 * @param connector
	 *            the connector for the publisher
	 * @param channel
	 *            the channel to publish
	 * @param user
	 *            the user object that represents the publisher
	 * @param secretKey
	 *            the secretKey value from the publisher
	 * @param accessKey
	 *            the accessKey value from the publisher
	 * @return the publisher object
	 */
	private Publisher authorizePublisher(WebSocketConnector connector, Channel channel, User user, String secretKey,
			String accessKey) {
		Publisher publisher = null;
		Date now = new Date();
		if (channel.getAccessKey().equals(accessKey) && channel.getSecretKey().equals(secretKey)
				&& user.getLoginname().equals(channel.getOwner())) {
			publisher = new Publisher(connector, user.getLoginname(), channel.getId(), now, now, true);
			mChannelManager.storePublisher(publisher);
		} else {
			publisher = new Publisher(connector, user.getLoginname(), channel.getId(), now, now, false);
		}
		return publisher;
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
		lChannel.subscribe(lSubscriber, mChannelManager);
		Token lResponseToken = createResponse(aToken);
		lResponseToken.setString("subscribe", "ok");
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
		if (lChannelId == null || EMPTY_STRING.equals(lChannelId)) {
			sendError(aConnector, null, "Channel value is null");
			return;
		}
		Token lResponseToken = createResponse(aToken);
		Subscriber lSubscriber = mChannelManager.getSubscriber(aConnector.getId());
		if (lSubscriber != null) {
			Channel lChannel = mChannelManager.getChannel(lChannelId);
			if (lChannel != null) {
				lChannel.unsubscribe(lSubscriber, mChannelManager);
				lResponseToken.setString("unsubscribe", "ok");
			}
		} else {
			lResponseToken.setInteger("code", -1);
			lResponseToken.setString("msg", "Client not subscribed to channel '"
					+ lChannelId + "'.");
		}
		// send the success response
		sendToken(aConnector, aConnector, lResponseToken);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason closeReason) {
		// unsubscribe from the channel
		Subscriber subscriber = mChannelManager.getSubscriber(aConnector.getId());
		for (String channelId : subscriber.getChannels()) {
			Channel channel = mChannelManager.getChannel(channelId);
			if (channel != null) {
				channel.unsubscribe(subscriber, mChannelManager);
			}
		}
		mChannelManager.removeSubscriber(subscriber);
	}

	/**
	 * Send the error response to the client as well as publish the error log to
	 * the logger channel for monitoring
	 *
	 * @param aConnector
	 *            the target connector object
	 * @param channel
	 *            the target channel id, can be null if channel is not
	 *            initialized
	 * @param error
	 *            the error message
	 */
	private void sendError(WebSocketConnector aConnector, String channel, String error) {
		Token lErrorToken = mChannelManager.getErrorToken(aConnector, channel, error);

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
	private void sendWelcome(WebSocketConnector aConnector) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Sending connected message to the channels");
		}
		// send "welcome" token to client
		Token welcome = TokenFactory.createToken(CONNECTED);
		welcome.setString("vendor", JWebSocketCommonConstants.VENDOR);
		welcome.setString("version", JWebSocketServerConstants.VERSION_STR);
		// here the session id is MANDATORY! to pass to the client!
		welcome.setString("usid", aConnector.getSession().getSessionId());
		welcome.setString("sourceId", aConnector.getId());
		// if a unique node id is specified for the client include that
		String lNodeId = aConnector.getNodeId();
		if (lNodeId != null) {
			welcome.setString("unid", lNodeId);
		}
		welcome.setInteger("timeout", aConnector.getEngine().getConfiguration().getTimeout());

		ChannelManager.getLoggerChannel().broadcastToken(welcome);
		sendTokenAsync(aConnector, aConnector, welcome);
	}
}
