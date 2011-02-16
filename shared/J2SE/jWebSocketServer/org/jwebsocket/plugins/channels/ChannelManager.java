//  ---------------------------------------------------------------------------
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

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.channels.Channel.ChannelState;
import org.jwebsocket.security.SecurityFactory;
import org.jwebsocket.security.User;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 * Manager class responsible for all the channel operations within the
 * jWebSocket server system.
 * 
 * @author puran
 * @version $Id$
 */
public class ChannelManager {

	/** logger */
	private static Logger mLog = Logging.getLogger(ChannelManager.class);
	/** id for the logger channel */
	private static final String LOGGER_CHANNEL_ID = "jws.logger.channel";
	/** id for the admin channel */
	private static final String ADMIN_CHANNEL_ID = "jws.admin.channel";
	/** settings key strings */
	private static final String USE_PERSISTENT_STORE = "use_persistent_store";
	private static final String ALLOW_NEW_CHANNELS = "allow_new_channels";
	/** persistent storage objects */
	private final ChannelStore mChannelStore = new BaseChannelStore();
	private final SubscriberStore mSubscriberStore = new BaseSubscriberStore();
	private final PublisherStore mPublisherStore = new BasePublisherStore();
	/** in-memory store maps */
	private final Map<String, Channel> mChannels = new ConcurrentHashMap<String, Channel>();
	private Map<String, Object> mChannelPluginSettings = null;
	/**
	 * Logger channel that publish all the logs in jWebSocket system
	 */
	private static Channel mLoggerChannel = null;
	/**
	 * admin channel to monitor channel plugin activity
	 */
	private static Channel mAdminChannel = null;
	/** setting to check for persistent storage or not */
	private static boolean mUsePersistentStore;
	/** setting to check if new channel creation or registration is allowed */
	private static boolean mAllowNewChannels;

	private ChannelManager(Map<String, Object> aSettings) {
		this.mChannelPluginSettings = new ConcurrentHashMap<String, Object>(aSettings);
		Object lUsePersisentStore = mChannelPluginSettings.get(USE_PERSISTENT_STORE);
		if (lUsePersisentStore != null && lUsePersisentStore.equals("true")) {
			mUsePersistentStore = true;
		}
		Object lAllowNewChannels = mChannelPluginSettings.get(ALLOW_NEW_CHANNELS);
		if (lAllowNewChannels != null && lAllowNewChannels.equals("true")) {
			mAllowNewChannels = true;
		}
		int lSuccess = 0;
		for (String lOption : aSettings.keySet()) {
			if (lOption.startsWith("channel:")) {
				String lChannelId = lOption.substring(8);
				Object lObj = aSettings.get(lOption);
				JSONObject lJSON = null;
				if (lObj instanceof JSONObject) {
					lJSON = (JSONObject) lObj;
				} else {
					lJSON = new JSONObject();
				}

				String lName = null;
				String lKey = null;
				String lSecret = null;
				String lOwner = null;
				boolean lIsPrivate = false;
				boolean lIsSystem = false;
				try {
					lName = lJSON.getString("name");
				} catch (Exception lEx) {
				}
				try {
					lKey = lJSON.getString("access_key");
				} catch (Exception lEx) {
				}
				try {
					lSecret = lJSON.getString("secret_key");
				} catch (Exception lEx) {
				}
				try {
					lOwner = lJSON.getString("owner");
				} catch (Exception lEx) {
				}
				try {
					lIsPrivate = lJSON.getBoolean("isPrivate");
				} catch (Exception lEx) {
				}
				try {
					lIsSystem = lJSON.getBoolean("isSystem");
				} catch (Exception lEx) {
				}

				if (mLog.isDebugEnabled()) {
					mLog.debug("Instantiating channel '"
							+ lChannelId + "' by configuration"
							+ " (private: "
							+ lIsPrivate
							+ ", system: " + lIsSystem + ")...");
				}

				Channel lChannel = new Channel(
						lChannelId, // String aId,
						lName, // String aName,
						lIsPrivate, // boolean aPrivateChannel,
						lIsSystem, // boolean aSystemChannel,
						lKey, // String aSecretKey,
						lSecret, // String aAccessKey,
						lOwner, // String aOwner,
						new Date(), // long aCreatedDate,
						ChannelState.INITIALIZED // ChannelState aState,
						);
				// put in channels map
				mChannels.put(lChannel.getId(), lChannel);
				lSuccess++;
			}
		}
		if (mLog.isDebugEnabled()) {
			mLog.debug(lSuccess + " channels successfully instantiated.");
		}
	}

	/**
	 * @param aSettings
	 * @return the static manager instance
	 */
	public static ChannelManager getChannelManager(Map<String, Object> aSettings) {
		return new ChannelManager(aSettings);
	}

	/**
	 * Starts the system channels within the jWebSocket system configured via
	 * jWebSocket.xml, Note that it doesn't insert the system channels to the
	 * channel store.
	 *
	 * @throws ChannelLifeCycleException
	 *             if exception starting the system channels
	 */
	public void startChannels() throws ChannelLifeCycleException {
		User lRoot = SecurityFactory.getRootUser();
		for (Channel lChannel : mChannels.values()) {
			try {
				lChannel.start(lRoot.getLoginname());
			} catch (Exception lEx) {
				mLog.error(lEx.getClass().getSimpleName()
						+ " on starting channel '" + lChannel.getId() + "': "
						+ lEx.getMessage());
			}
		}
	}

	/**
	 * Stops all the system channels running in the system and clears the system
	 * channels map
	 *
	 * @throws ChannelLifeCycleException
	 */
	public void stopChannels() throws ChannelLifeCycleException {
		User lRoot = SecurityFactory.getRootUser();
		for (Channel lChannel : mChannels.values()) {
			try {
				lChannel.stop(lRoot.getLoginname());
			} catch (Exception lEx) {
				mLog.error(lEx.getClass().getSimpleName()
						+ " on stopping channel '" + lChannel.getId() + "': "
						+ lEx.getMessage());
			}
		}
	}

	/**
	 * Returns the channel registered in the jWebSocket system based on channel
	 * id it does a various lookup and then if it doesn't find anywhere from the
	 * memory it loads the channel from the database. If it doesn' find anything
	 * then it returns the null object
	 *
	 * @param aChannelId
	 * @return channel object, null if not found
	 */
	public Channel getChannel(String aChannelId) {
		return mChannels.get(aChannelId);
	}

	/**
	 *
	 * @param aChannelId
	 * @return
	 */
	public Channel removeChannel(String aChannelId) {
		Channel lChannel = getChannel(aChannelId);
		if (lChannel != null) {
			mChannels.remove(aChannelId);
			if (mUsePersistentStore) {
				mChannelStore.removeChannel(aChannelId);
			}

		}
		return lChannel;
	}

	/**
	 *
	 * @param aChannel
	 * @return
	 */
	public Channel removeChannel(Channel aChannel) {
		if (aChannel != null) {
			return removeChannel(aChannel.getId());
		}
		return null;
	}

	/**
	 * Adds the given channel to the list of channels maintained by the
	 * jWebSocket system.
	 *
	 * @param aChannel
	 *            the channel to store.
	 */
	public void addChannel(Channel aChannel) {
		mChannels.put(aChannel.getId(), aChannel);
		if (mUsePersistentStore) {
			mChannelStore.storeChannel(aChannel);
		}
	}

	/**
	 * Returns the registered subscriber object for the given subscriber id
	 *
	 * @param aSubscriberId
	 *            the subscriber id
	 * @return the subscriber object
	 */
	public Subscriber getSubscriber(String aSubscriberId) {
		return mSubscriberStore.getSubscriber(aSubscriberId);
	}

	/**
	 * Stores the registered subscriber information in the channel store
	 *
	 * @param aSubscriber
	 *            the subscriber to register
	 */
	public void storeSubscriber(Subscriber aSubscriber) {
		mSubscriberStore.storeSubscriber(aSubscriber);
	}

	/**
	 * Removes the given subscriber information from channel store
	 *
	 * @param aSubscriber
	 *            the subscriber object
	 */
	public void removeSubscriber(Subscriber aSubscriber) {
		mSubscriberStore.removeSubscriber(aSubscriber.getId());
	}

	/**
	 * Returns the registered publisher for the given publisher id
	 *
	 * @param aPublisherId
	 *            the publisher id
	 * @return the publisher object
	 */
	public Publisher getPublisher(String aPublisherId) {
		return mPublisherStore.getPublisher(aPublisherId);
	}

	/**
	 * Stores the given publisher to the channel store
	 *
	 *
	 * @param aPublisher
	 */
	public void storePublisher(Publisher aPublisher) {
		mPublisherStore.storePublisher(aPublisher);
	}

	/**
	 * Removes the publisher from the channel store permanently
	 *
	 * @param aPublisher
	 *            the publisher to remove
	 */
	public void removePublisher(Publisher aPublisher) {
		mPublisherStore.removePublisher(aPublisher.getId());
	}

	/**
	 * Returns the instance of the logger channel.If not initialized for some
	 * reason returns null.
	 *
	 * @return the logger channel
	 */
	public static Channel getLoggerChannel() {
		return mLoggerChannel;
	}

	/**
	 * Returns the instance of the admin channel. If not initialized for some
	 * reasons returns null.
	 *
	 * @return the admin channel
	 */
	public static Channel getAdminChannel() {
		return mAdminChannel;
	}

	/**
	 *
	 * @param aToken
	 */
	public void publishToLoggerChannel(Token aToken) {
		Channel lLoggerChannel = getLoggerChannel();
		// Added by Alex:
		if (lLoggerChannel != null) {
			lLoggerChannel.broadcastToken(aToken);
		}
	}

	/**
	 * Returns the error token
	 *
	 * @param aConnector
	 *            the target connector object
	 * @param aChannelId
	 *            the channelId
	 * @param aMessage
	 *            the error message
	 * @return the error token
	 */
	public Token getErrorToken(WebSocketConnector aConnector, String aChannelId, String aMessage) {
		Token logToken = getBaseChannelResponse(aConnector, aChannelId);
		logToken.setString("event", "error");
		logToken.setString("error", aMessage);

		return logToken;
	}

	/**
	 * Returns the basic response token for a channel
	 *
	 * @param aConnector
	 *            the target connector object
	 * @param aChannel
	 *            the target channel
	 * @return the base token of type channel
	 */
	public Token getBaseChannelResponse(WebSocketConnector aConnector, String aChannel) {
		Token channelToken = TokenFactory.createToken("channel");

		// TODO: In clusters, especially for service nodes we will post these fields on a lower level!
		// check! Commented out by Alex
		// channelToken.setString("vendor", JWebSocketCommonConstants.VENDOR);
		// channelToken.setString("version", JWebSocketServerConstants.VERSION_STR);
		// Alex: These fields are mandatory
		channelToken.setString("sourceId", aConnector.getId());
		channelToken.setString("channelId", aChannel);

		return channelToken;
	}

	/**
	 *
	 * @param aConnector
	 * @param aChannel
	 * @param aEventType
	 * @return
	 */
	public Token getChannelSuccessToken(WebSocketConnector aConnector, String aChannel, ChannelEventEnum aEventType) {
		Token lToken = getBaseChannelResponse(aConnector, aChannel);
		String lEvent = "";
		switch (aEventType) {
			case LOGIN:
				lEvent = "login";
				break;
			case AUTHORIZE:
				lEvent = "authorize";
				break;
			case PUBLISH:
				lEvent = "publish";
				break;
			case SUSCRIBE:
				lEvent = "subscribe";
			case UNSUSCRIBE:
				lEvent = "unsuscribe";
				break;
			default:
				break;
		}
		lToken.setString("event", lEvent);
		lToken.setString("status", "ok");

		return lToken;
	}

	/**
	 * @return the channels
	 */
	public Map<String, Channel> getChannels() {
		return Collections.unmodifiableMap(mChannels);
	}
}
