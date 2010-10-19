package org.jwebsocket.plugins.channels;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jwebsocket.api.PluginConfiguration;
/**
 * Manager class responsible for all the channel operations within the jWebSocket 
 * server system.
 * @author puran
 * @version $Id$
 */
public class ChannelManager {
    
    /** channel store */
    private final ChannelStore channelStore = new BaseChannelStore();
    
    /** system channels map */
    private final Map<String, Channel> systemChannels = new ConcurrentHashMap<String, Channel>();
    
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
     * single instance of the channel manager 
     */
    private static final ChannelManager INSTANCE = new ChannelManager();
    
    /**
     * don't allow this
     */
    private ChannelManager() {
        throw new AssertionError();
    }

    /**
     * @return the static manager instance
     */
    public static ChannelManager getChannelManager() {
        return INSTANCE;
    }
    
    /**
     * Starts the system channels within the jWebSocket system
     * @param configuration the channel plugin configuration
     */
    public void startSystemChannels(PluginConfiguration configuration) {

    }
    
    public void stopSystemChannels(PluginConfiguration pluginConfiguration) {
    }
    
    public Channel getChannel(String channelId) {
        if (systemChannels.containsKey(channelId)) {
            return systemChannels.get(channelId);
        }
        if (publicChannels.containsKey(channelId)) {
            return publicChannels.get(channelId);
        }
        //if not anywhere then look in the channel store
        Channel channel = channelStore.getChannel(channelId);
        if (channel != null) {
            publicChannels.put(channelId, channel);
        }
        return channel;
    }
    
    public void registerChannel(Channel channel) {
        
    }
    
    /**
     * Returns the instance of the logger channel.
     * @return the logger channel
     */
    public Channel getLoggerChannel() {
        return loggerChannel;
    }
    
    /**
     * Returns the instance of the admin channel
     * @return the admin channel
     */
    public Channel getAdminChannel() {
        return adminChannel;
    }
}
