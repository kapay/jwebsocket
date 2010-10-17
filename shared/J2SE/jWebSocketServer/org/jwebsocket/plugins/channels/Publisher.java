package org.jwebsocket.plugins.channels;

import org.jwebsocket.api.WebSocketConnector;

/**
 * Publisher class 
 * @author puran
 * @version $Id$
 */
public class Publisher {
    private String id;
    private String channel;
    private WebSocketConnector connector;
    
    public Publisher(String id, String channel) {
        this.id = id;
        this.channel = channel;
        this.connector = null;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }
    /**
     * @return the channel
     */
    public String getChannel() {
        return channel;
    }

    /**
     * @param channel  the channel to set
     */
    public void setChannel(String channel) {
        this.channel = channel;
    }

    /**
     * @return the connector
     */
    public WebSocketConnector getConnector() {
        return connector;
    }

    /**
     * @param connector the connector to set
     */
    public void setConnector(WebSocketConnector connector) {
        this.connector = connector;
    }

}
