package org.jwebsocket.plugins.channels;

import java.util.HashMap;
import java.util.Map;

import org.jwebsocket.api.PluginConfiguration;
/**
 * Manager class responsible for all the channel operations within the jWebSocket 
 * server system.
 * @author puran
 * @version $Id$
 */
public class ChannelManager {
    
    /** channel store */
    private ChannelStore channelStore = new BaseChannelStore();
    
    /** system channels map */
    private Map<String, Channel> systemChannels = new HashMap<String, Channel>();
    
    /** user channels map */
    private Map<String, Channel> publicChannels = new HashMap<String, Channel>();
    
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
        return null;
    }
    
    public void registerChannel(Channel channel) {
        
    }
    
    public void subscribeChannel(Channel channel, Subscriber subscriber) {
        
    }
    
    public void unsuscribeChannel(Channel channel, Subscriber subscriber) {
        
    }
}
