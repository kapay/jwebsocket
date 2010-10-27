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

/**
 * Manager class responsible for all the channel operations within the
 * jWebSocket server system.
 * 
 * @author puran
 * @version $Id$
 */
public class ChannelManager {
	/** id for the logger channel */
	private static final String LOGGER_CHANNEL_ID = "jws.logger.channel";
	/** id for the admin channel */
	private static final String ADMIN_CHANNEL_ID = "jws.admin.channel";
    /** channel store */
    private final ChannelStore channelStore = new BaseChannelStore();
    /** subscriber store */
    private final SubscriberStore subscriberStore = new BaseSubscriberStore();
    /** publisher store*/
    private final PublisherStore publisherStore = new BasePublisherStore();
    /** system channels map */
    private final Map<String, Channel> systemChannels = new ConcurrentHashMap<String, Channel>();
    /** private channel map */
    private final Map<String, Channel> privateChannels = new ConcurrentHashMap<String, Channel>();
    /** user channels map */
    private final Map<String, Channel> publicChannels = new ConcurrentHashMap<String, Channel>();
    /**
     * Logger channel
     */
    private Channel loggerChannel = null;
    /**
     * admin channel
     */
    private Channel adminChannel = null;
    /**
     * don't allow this
     */
    private ChannelManager() {
    }
    /**
     * @return the static manager instance
     */
    public static ChannelManager getChannelManager() {
        return new ChannelManager();
    }
    /**
     * Starts the system channels within the jWebSocket system configured 
     * via jWebSocket.xml, Note that it doesn't insert the system channels to
     * the channel store.
     */
	public void startSystemChannels() {
		JWebSocketConfig config = JWebSocketConfig.getConfig();
		for (ChannelConfig cfg : config.getChannels()) {
			if (cfg.isSystemChannel()) {
				cfg.validate();
				if (LOGGER_CHANNEL_ID.equals(cfg.getId())) {
					loggerChannel = new Channel(cfg);
				} else if (ADMIN_CHANNEL_ID.equals(cfg.getId())) {
					adminChannel = new Channel(cfg);
				} else {
					Channel channel = new Channel(cfg);
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
    public void stopSystemChannels() {
    	for (Map.Entry<String, Channel> entry : systemChannels.entrySet()) {
    		Channel channel = entry.getValue();
    		channel.stop();
    	}
    	systemChannels.clear();
    	if (loggerChannel != null) {
    		loggerChannel.stop();
    	}
    	if (adminChannel != null) {
    		adminChannel.stop();
    	}
    }
    
    /**
     * Creates the complete new system channel, this channel is stored in the channel store 
     * by default.
     * @param channelId the id of the channel to create
     * @param name the channel name
     * @param accessKey the access key for the new channel
     * @param secretKey the secretKey for the new channel
     * @param owner the owner of the channel
     * @return the new created channel
     */
    public Channel createSystemChannel(String channelId, String channelName, String accessKey, String secretKey, String owner) {
      return null;
    }
    
    /**
     * Creates the complete new private channel without any publishers and subscribers 
     * @param channelId the id of the channel to create
     * @param accessKey the access key for the new channel
     * @param secretKey the secretKey for the new channel
     * @param owner the owner of the channel
     * @return the new created channel
     */
    public Channel createPrivateChannel(String channelId) {
      return null;
    }
    
    /**
     * Creates the complete new public channel without any publishers and subscribers 
     * @param channelId the channelId for the new channel
     * @param secretKey the secretKey for the new channel
     * @param owner the owner of the channel
     * @return the public channel
     */
    public Channel createPublicChannel(String channelId, String secretKey, String owner) {
      return null;
    }

    /**
     * Returns the channel registered in the jWebSocket system based on channel id
     * it does a various lookup and then if it doesn't find anywhere from the memory
     * it loads the channel from the database.
     * If it doesn' find anything then it returns the null object 
     * @param channelId
     * @return
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
        // if not anywhere then look in the channel store
        Channel channel = channelStore.getChannel(channelId);
        if (channel != null) {
            publicChannels.put(channelId, channel);
        }
        return channel;
    }
    /**
     * Register the given channel to the list of channels maintained by the 
     * jWebSocket system.
     * @param channel the channel to store.
     */
    public void registerChannel(Channel channel) {
      if (channel.isPrivateChannel() && !channel.isSystemChannel()) {
        privateChannels.put(channel.getId(), channel);
      } else if (!channel.isPrivateChannel() && !channel.isSystemChannel()) {
        publicChannels.put(channel.getId(), channel);
      } else {
        systemChannels.put(channel.getId(), channel);
      }
      channelStore.storeChannel(channel);
    }

    /**
     * Returns the registered subscriber object for the given subscriber id
     * @param subscriberId the subscriber id
     * @return the subscriber object
     */
    public Subscriber getSubscriber(String subscriberId) {
        return subscriberStore.getSubscriber(subscriberId);
    }
    /**
     * Stores the registered subscriber information in the channel store
     * @param subscriber the subscriber to register
     */
    public void storeSubscriber(Subscriber subscriber) {
      subscriberStore.storeSubscriber(subscriber);
    }
    /**
     * Removes the given subscriber information from channel store
     * @param subscriber the subscriber object
     */
    public void removeSubscriber(Subscriber subscriber) {
      subscriberStore.removeSubscriber(subscriber.getId());
    }
    /**
     * Returns the registered publisher for the given publisher id
     * @param publisherId the publisher id
     * @return the publisher object
     */
    public Publisher getPublisher(String publisherId) {
      return publisherStore.getPublisher(publisherId);
    }
    /**
     * Stores the given publisher to the channel store
     * @param publisher the publisher object to store
     */
    public void storePublisher(Publisher publisher) {
      publisherStore.storePublisher(publisher);
    }
    /**
     * Removes the publisher from the channel store permanently
     * @param publisher the publisher to remove
     */
    public void removePublisher(Publisher publisher) {
      publisherStore.removePublisher(publisher.getId());
    }
    /**
     * Returns the instance of the logger channel.If not initialized for some 
     * reason returns null.
     * @return the logger channel
     */
    public Channel getLoggerChannel() {
        return loggerChannel;
    }

    /**
     * Returns the instance of the admin channel. If not initialized for some
     * reasons returns null.
     * @return the admin channel
     */
    public Channel getAdminChannel() {
        return adminChannel;
    }
}
