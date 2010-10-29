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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.config.xml.ChannelConfig;
import org.jwebsocket.security.Right;
import org.jwebsocket.security.Rights;
import org.jwebsocket.security.SecurityFactory;
import org.jwebsocket.security.User;

/**
 * Manager class responsible for all the channel operations within the
 * jWebSocket server system.
 * 
 * @author puran
 * @version $Id$
 */
public class ChannelManager implements ChannelListener {
	/** id for the logger channel */
	private static final String LOGGER_CHANNEL_ID = "jws.logger.channel";
	/** id for the admin channel */
	private static final String ADMIN_CHANNEL_ID = "jws.admin.channel";

	/** settings key strings */
	private static final String USE_PERSISTENT_STORE = "use_persistent_store";
	private static final String ALLOW_NEW_CHANNELS = "allow_new_channels";

	/** persistent storage objects */
	private final ChannelStore channelStore = new BaseChannelStore();
	private final SubscriberStore subscriberStore = new BaseSubscriberStore();
	private final PublisherStore publisherStore = new BasePublisherStore();

	/** in-memory store maps */
	private final Map<String, Channel> systemChannels = new ConcurrentHashMap<String, Channel>();
	private final Map<String, Channel> privateChannels = new ConcurrentHashMap<String, Channel>();
	private final Map<String, Channel> publicChannels = new ConcurrentHashMap<String, Channel>();

	private Map<String, String> channelPluginSettings = null;
	/**
	 * Logger channel that publish all the logs in jWebSocket system
	 */
	private Channel loggerChannel = null;
	/**
	 * admin channel to monitor channel plugin activity
	 */
	private Channel adminChannel = null;

	/** setting to check for persistent storage or not */
	private boolean usePersistentStore = false;

	/** setting to check if new channel creation or registration is allowed */
	private boolean allowNewChannels = false;

	/**
	 * don't allow this
	 */
	private ChannelManager(Map<String, String> settings) {
		this.channelPluginSettings = new ConcurrentHashMap<String, String>(
				settings);
		String value1 = channelPluginSettings.get(USE_PERSISTENT_STORE);
		if (value1 != null && value1.equals("true")) {
			usePersistentStore = true;
		}
		String value2 = channelPluginSettings.get(ALLOW_NEW_CHANNELS);
		if (value2 != null && value2.equals("true")) {
			allowNewChannels = true;
		}
	}

	/**
	 * @return the static manager instance
	 */
	public static ChannelManager getChannelManager(Map<String, String> settings) {
		return new ChannelManager(settings);
	}

	/**
	 * Starts the system channels within the jWebSocket system configured via
	 * jWebSocket.xml, Note that it doesn't insert the system channels to the
	 * channel store.
	 * 
	 * @throws ChannelLifeCycleException
	 *             if exception starting the system channels
	 */
	public void startSystemChannels() throws ChannelLifeCycleException {
		User root = SecurityFactory.getRootUser();
		JWebSocketConfig config = JWebSocketConfig.getConfig();
		for (ChannelConfig cfg : config.getChannels()) {
			if (cfg.isSystemChannel()) {
				cfg.validate();
				if (LOGGER_CHANNEL_ID.equals(cfg.getId())) {
					loggerChannel = new Channel(cfg);
					loggerChannel.start(root.getLoginname());
				} else if (ADMIN_CHANNEL_ID.equals(cfg.getId())) {
					adminChannel = new Channel(cfg);
					adminChannel.start(root.getLoginname());
				} else {
					Channel channel = new Channel(cfg);
					channel.start(root.getLoginname());
					// put in system channels map
					systemChannels.put(channel.getId(), channel);
				}
			}
		}
	}

	/**
	 * Stops all the system channels running in the system and clears the system
	 * channels map
	 */
	public void stopSystemChannels() throws ChannelLifeCycleException {
		User root = SecurityFactory.getRootUser();
		for (Map.Entry<String, Channel> entry : systemChannels.entrySet()) {
			Channel channel = entry.getValue();
			channel.stop(root.getLoginname());
		}
		systemChannels.clear();
		if (loggerChannel != null) {
			loggerChannel.stop(root.getLoginname());
		}
		if (adminChannel != null) {
			adminChannel.stop(root.getLoginname());
		}
	}

	/**
	 * Returns the channel registered in the jWebSocket system based on channel
	 * id it does a various lookup and then if it doesn't find anywhere from the
	 * memory it loads the channel from the database. If it doesn' find anything
	 * then it returns the null object
	 * 
	 * @param channelId
	 * @return channel object, null if not found
	 */
	public Channel getChannel(String channelId) {
		if (systemChannels.containsKey(channelId)) {
			return systemChannels.get(channelId);
		}
		if (privateChannels.containsKey(channelId)) {
			return privateChannels.get(channelId);
		}
		if (publicChannels.containsKey(channelId)) {
			return publicChannels.get(channelId);
		}
		if (usePersistentStore) {
			// if not anywhere then look in the channel store
			Channel channel = channelStore.getChannel(channelId);
			if (channel != null) {
				publicChannels.put(channelId, channel);
			}
			return channel;
		}
		return null;
	}

	/**
	 * Register the given channel to the list of channels maintained by the
	 * jWebSocket system.
	 * 
	 * @param channel
	 *            the channel to store.
	 */
	public void registerChannel(Channel channel) {
		if (channel.isSystemChannel()) {
			systemChannels.put(channel.getId(), channel);
		} else if (channel.isPrivateChannel()) {
			privateChannels.put(channel.getId(), channel);
		} else {
			publicChannels.put(channel.getId(), channel);
		}
		if (usePersistentStore) {
			channelStore.storeChannel(channel);
		}
	}

	/**
	 * Returns the registered subscriber object for the given subscriber id
	 * 
	 * @param subscriberId
	 *            the subscriber id
	 * @return the subscriber object
	 */
	public Subscriber getSubscriber(String subscriberId) {
		return subscriberStore.getSubscriber(subscriberId);
	}

	/**
	 * Stores the registered subscriber information in the channel store
	 * 
	 * @param subscriber
	 *            the subscriber to register
	 */
	public void storeSubscriber(Subscriber subscriber) {
		subscriberStore.storeSubscriber(subscriber);
	}

	/**
	 * Removes the given subscriber information from channel store
	 * 
	 * @param subscriber
	 *            the subscriber object
	 */
	public void removeSubscriber(Subscriber subscriber) {
		subscriberStore.removeSubscriber(subscriber.getId());
	}

	/**
	 * Returns the registered publisher for the given publisher id
	 * 
	 * @param publisherId
	 *            the publisher id
	 * @return the publisher object
	 */
	public Publisher getPublisher(String publisherId) {
		return publisherStore.getPublisher(publisherId);
	}

	/**
	 * Stores the given publisher to the channel store
	 * 
	 * @param publisher
	 *            the publisher object to store
	 */
	public void storePublisher(Publisher publisher) {
		publisherStore.storePublisher(publisher);
	}

	/**
	 * Removes the publisher from the channel store permanently
	 * 
	 * @param publisher
	 *            the publisher to remove
	 */
	public void removePublisher(Publisher publisher) {
		publisherStore.removePublisher(publisher.getId());
	}

	/**
	 * Returns the instance of the logger channel.If not initialized for some
	 * reason returns null.
	 * 
	 * @return the logger channel
	 */
	public Channel getLoggerChannel() {
		return loggerChannel;
	}

	/**
	 * Returns the instance of the admin channel. If not initialized for some
	 * reasons returns null.
	 * 
	 * @return the admin channel
	 */
	public Channel getAdminChannel() {
		return adminChannel;
	}

	/**
	 * Every time channel starts check if the configuration settings allow new
	 * channels to start and see if the given user has rights to start the channel
	 */
	@Override
	public void channelStarted(Channel channel, String user)
			throws ChannelException {
		if (!allowNewChannels) {
			throw new ChannelException(
					"New channels are not allowed to start. Please check the configuration or contact admin");
		}
		User userObj = SecurityFactory.getUser(user);
		if (userObj == null) {
			throw new ChannelException("User:[" + user + "] doesn't exist, please check the configuration");
		} else {
			Rights rights = SecurityFactory.getUserRights(user);
			Right right = rights.get("org.jwebsocket.plugins.channel.start");
			if (right == null) {
				throw new ChannelException("User:[" + user + "] does not have rights to start the channel");
			} else {
				//verify the owner 
				if (!channel.getOwner().equals(user)) {
					throw new ChannelException("User:[" + user + "] is not a owner of this channel," +
							"Only owner of the channel can start the channel");
				}
			}
		}
	}

	@Override
	public void channelStopped(Channel channel, String user)
			throws ChannelException {
		User userObj = SecurityFactory.getUser(user);
		if (userObj == null) {
			throw new ChannelException("User:[" + user + "] doesn't exist, please check the configuration");
		} else {
			Rights rights = SecurityFactory.getUserRights(user);
			Right right = rights.get("org.jwebsocket.plugins.channel.stop");
			if (right == null) {
				throw new ChannelException("User:[" + user + "] does not have rights to stop the channel");
			} else {
				//verify the owner 
				if (!channel.getOwner().equals(user)) {
					throw new ChannelException("User:[" + user + "] is not a owner of this channel," +
							"Only owner of the channel can stop the channel");
				}
			}
		}
	}

	@Override
	public void channelSuspended(Channel channel, String user) throws ChannelException {
		User userObj = SecurityFactory.getUser(user);
		if (userObj == null) {
			throw new ChannelException("User:[" + user + "] doesn't exist, please check the configuration");
		} else {
			Rights rights = SecurityFactory.getUserRights(user);
			Right right = rights.get("org.jwebsocket.plugins.channel.suspend");
			if (right == null) {
				throw new ChannelException("User:[" + user + "] does not have rights to suspend the channel");
			} else {
				//verify the owner 
				if (!channel.getOwner().equals(user)) {
					throw new ChannelException("User:[" + user + "] is not a owner of this channel," +
							"Only owner of the channel can suspend the channel");
				}
			}
		}
	}

	@Override
	public void subscribed(Channel channel, Subscriber subscriber) {
		// ignore for now
	}

	@Override
	public void unsubscribed(Channel channel, Subscriber subscriber) {
		// ignore for now
	}
}
