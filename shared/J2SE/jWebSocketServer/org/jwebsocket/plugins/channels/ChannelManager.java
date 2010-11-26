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

import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.config.xml.ChannelConfig;
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
    private static Channel loggerChannel = null;
    /**
     * admin channel to monitor channel plugin activity
     */
    private static Channel adminChannel = null;

    /** setting to check for persistent storage or not */
    public static boolean usePersistentStore;

    /** setting to check if new channel creation or registration is allowed */
    public static boolean allowNewChannels;

    private ChannelManager(Map<String, String> settings) {
        this.channelPluginSettings = new ConcurrentHashMap<String, String>(settings);
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
    public static Channel getLoggerChannel() {
        return loggerChannel;
    }

    /**
     * Returns the instance of the admin channel. If not initialized for some
     * reasons returns null.
     * 
     * @return the admin channel
     */
    public static Channel getAdminChannel() {
        return adminChannel;
    }

    public void publishToLoggerChannel(Token token) {
        getLoggerChannel().broadcastToken(token);
    }

    /**
     * Returns the error token
     * 
     * @param aConnector
     *            the target connector object
     * @param channelId
     *            the channelId
     * @param message
     *            the error message
     * @return the error token
     */
    public Token getErrorToken(WebSocketConnector aConnector, String channelId, String message) {
        Token logToken = getBaseChannelResponse(aConnector, channelId);
        logToken.setString("event", "error");
        logToken.setString("error", message);

        return logToken;
    }

    /**
     * Returns the basic response token for a channel
     * 
     * @param aConnector
     *            the target connector object
     * @param channel
     *            the target channel
     * @return the base token of type channel
     */
    public Token getBaseChannelResponse(WebSocketConnector aConnector, String channel) {
        Token channelToken = TokenFactory.createToken("channel");
        channelToken.setString("vendor", JWebSocketCommonConstants.VENDOR);
        channelToken.setString("version", JWebSocketServerConstants.VERSION_STR);
        channelToken.setString("sourceId", aConnector.getId());
        channelToken.setString("channelId", channel);

        return channelToken;
    }

    public Token getChannelSuccessToken(WebSocketConnector aConnector, String channel, ChannelEventEnum eventType) {
        Token token = getBaseChannelResponse(aConnector, channel);
        String event = "";
        switch (eventType) {
        case LOGIN:
            event = "login";
            break;
        case AUTHORIZE:
            event = "authorize";
            break;
        case PUBLISH:
            event = "publish";
            break;
        case SUSCRIBE:
            event = "subscribe";
        case UNSUSCRIBE:
            event = "unsuscribe";
            break;
        default:
            break;
        }
        token.setString("event", event);
        token.setString("status", "ok");

        return token;
    }
}
