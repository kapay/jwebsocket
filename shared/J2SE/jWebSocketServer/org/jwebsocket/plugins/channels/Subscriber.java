//  ---------------------------------------------------------------------------
//  jWebSocket - RequestHeader Object
//  Copyright (c) 2010 jWebSocket
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
package org.jwebsocket.plugins.channels;

import org.jwebsocket.api.WebSocketConnector;
/**
 * Class that represents the subscriber of a channel
 * @author puran
 * @version $Id$
 */
public class Subscriber {
    private String id;
    private String name;
    private String subscriberKey;
    private WebSocketConnector connector;
    private String channels;
    
    /**
     * Default constructor
     */
    public Subscriber(String id, String name, String subscriberKey, String channels) {
        this.id = id;
        this.name = name;
        this.subscriberKey = subscriberKey;
        this.connector = null;
        this.channels = channels;
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
     * @return the name
     */
    public String getName() {
        return name;
    }
    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    /**
     * @return the subscriberKey
     */
    public String getSubscriberKey() {
        return subscriberKey;
    }
    /**
     * @param subscriberKey the subscriberKey to set
     */
    public void setSubscriberKey(String subscriberKey) {
        this.subscriberKey = subscriberKey;
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
    /**
     * @return the channels
     */
    public String getChannels() {
        return channels;
    }
    /**
     * @param channels the channels to set
     */
    public void setChannels(String channels) {
        this.channels = channels;
    }
    
}
