//  ---------------------------------------------------------------------------
//  jWebSocket - Copyright (c) 2010 jwebsocket.org
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
import java.util.List;

import org.jwebsocket.token.Token;

import javolution.util.FastList;

/**
 * Channel class represents the data channel
 * 
 * @author puran
 * @version $Id$
 */
public class Channel implements ChannelLifeCycle {
    private String id;
    private String name;
    private int subscriberCount;
    private boolean privateChannel;
    private boolean systemChannel;
    private String secretKey;
    private String accessKey;
    private List<Subscriber> subscribers;
    private List<Publisher> publishers;

    public Channel(String id, String name, int subscriberCount, boolean privateChannel, boolean systemChannel,
            String secretKey, String accessKey, List<Subscriber> subscribers, List<Publisher> publishers) {
        this.id = id;
        this.name = name;
        this.subscriberCount = subscriberCount;
        this.privateChannel = privateChannel;
        this.systemChannel = systemChannel;
        this.secretKey = secretKey;
        this.accessKey = accessKey;
        this.subscribers = subscribers;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSubscriberCount() {
        return subscriberCount;
    }

    public void setSubscriberCount(int subscriberCount) {
        this.subscriberCount = subscriberCount;
    }

    public boolean isPrivateChannel() {
        return privateChannel;
    }

    public void setPrivateChannel(boolean privateChannel) {
        this.privateChannel = privateChannel;
    }

    /**
     * @return the systemChannel
     */
    public boolean isSystemChannel() {
        return systemChannel;
    }

    /**
     * @param systemChannel the systemChannel to set
     */
    public void setSystemChannel(boolean systemChannel) {
        this.systemChannel = systemChannel;
    }
    
    /**
     * @return the secretKey
     */
    public String getSecretKey() {
        return secretKey;
    }

    /**
     * @param secretKey the secretKey to set
     */
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    /**
     * @return the accessKey
     */
    public String getAccessKey() {
        return accessKey;
    }

    /**
     * @param accessKey the accessKey to set
     */
    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public List<Subscriber> getSubscribers() {
        return Collections.unmodifiableList(subscribers);
    }

    public void setSubscribers(List<Subscriber> subscribers) {
        this.subscribers = subscribers;
    }

    /**
     * @return the publishers
     */
    public List<Publisher> getPublishers() {
        return publishers;
    }

    /**
     * @param publishers the publishers to set
     */
    public void setPublishers(List<Publisher> publishers) {
        this.publishers = publishers;
    }

    public void addSubscriber(Subscriber subscriber) {
        if (this.subscribers == null) {
            this.subscribers = new FastList<Subscriber>();
        }
        subscribers.add(subscriber);
    }

    public void send(Token token, Subscriber subscriber) {

    }

    public void broadcast(Token token) {

    }

    @Override
    public void init() {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((accessKey == null) ? 0 : accessKey.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((secretKey == null) ? 0 : secretKey.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Channel other = (Channel) obj;
        if (accessKey == null) {
            if (other.accessKey != null)
                return false;
        } else if (!accessKey.equals(other.accessKey))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (secretKey == null) {
            if (other.secretKey != null)
                return false;
        } else if (!secretKey.equals(other.secretKey))
            return false;
        return true;
    }
}
