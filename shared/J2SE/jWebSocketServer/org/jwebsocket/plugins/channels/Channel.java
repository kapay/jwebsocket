//  ---------------------------------------------------------------------------
//  jWebSocket - Channel
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jwebsocket.async.IOFuture;
import org.jwebsocket.token.Token;

/**
 * Channel class represents the data channel which is used by the
 * <tt>Publisher</tt> to publish the data and the number of <tt>Subscriber</tt>
 * 's can subscribe to the given channel to receive the data stream through the
 * channel as soon as it is available to the channel via publisher.
 * 
 * Channel can be of 3 types:
 * 
 * 1. System Channel - The channels which are and can only be initialized and
 * started by the jWebSocket components and are used by it for providing system
 * level information are called system channel. Examples can be
 * <tt>LoggerChannel</tt> for streaming server logs to the client,
 * <tt>AdminChannel<tt>
 * to stream the admin level read only information etc..
 * 
 * 2. Private Channel - These are the channels that can be registered, initialized 
 * and started by user at configuration time using <tt>jWebSocket.xml</tt> or
 * runtime. But to subscribe to this channel the user or client should have
 * valid <tt>api_key</tt> or rights.
 * 
 * 3. Public Channel - Same as private channel except anyone can subscribe to
 * this channel without the use of <tt>access_key</tt> or irrespective of the
 * roles and rights.
 * 
 * @author puran
 * @version $Id$
 */
public final class Channel implements ChannelLifeCycle {
  private String id;
  private String name;
  private int subscriberCount;
  private boolean privateChannel;
  private boolean systemChannel;
  private String secretKey;
  private String accessKey;
  private long createdDate;
  private String owner;
  private List<Subscriber> subscribers;
  private List<Publisher> publishers;
  private ChannelState state = ChannelState.STOPPED;
  private List<ChannelListener> channelListeners;

  public enum ChannelState {

    STOPPED(0), INITIALIZED(1), STARTED(2), SUSPENDED(3);
    private int value;

    ChannelState(int value) {
      this.value = value;
    }

    public int getValue() {
      return value;
    }
  }

  public Channel(String id, String name, int subscriberCount, boolean privateChannel, boolean systemChannel,
      String secretKey, String accessKey, String owner, long createdDate, ChannelState state,
      List<Subscriber> subscribers, List<Publisher> publishers) {
    this.id = id;
    this.name = name;
    this.subscriberCount = subscriberCount;
    this.privateChannel = privateChannel;
    this.systemChannel = systemChannel;
    this.secretKey = secretKey;
    this.accessKey = accessKey;
    this.owner = owner;
    this.createdDate = createdDate;
    this.subscribers = subscribers;
    this.state = state;
  }

  /**
   * Returns the channel unique id.
   * 
   * @return the id
   */
  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public int getSubscriberCount() {
    return subscriberCount;
  }

  public boolean isPrivateChannel() {
    return privateChannel;
  }

  /**
   * @return the systemChannel
   */
  public boolean isSystemChannel() {
    return systemChannel;
  }

  /**
   * @return the secretKey
   */
  public String getSecretKey() {
    return secretKey;
  }

  /**
   * @return the accessKey
   */
  public String getAccessKey() {
    return accessKey;
  }

  /**
   * @return the createdDate
   */
  public long getCreatedDate() {
    return createdDate;
  }

  /**
   * @return the owner
   */
  public String getOwner() {
    return owner;
  }

  /**
   * Returns the unmodifiable list of all the subscribers to this channel
   * 
   * @return the list of subscribers
   */
  public List<Subscriber> getSubscribers() {
    return Collections.unmodifiableList(subscribers);
  }

  /**
   * Set the subscribers to this channel. Note that this method simply replaces
   * the existing list of subscribers.
   * 
   * @param subscribers
   *          the list of subscribers
   */
  public synchronized void setSubscribers(List<Subscriber> subscribers) {
    this.subscribers = subscribers;
  }

  /**
   * @return the publishers who is currently publishing to this channel
   */
  public List<Publisher> getPublishers() {
    return Collections.unmodifiableList(publishers);
  }

  /**
   * @param publishers
   *          the publishers to set
   */
  public synchronized void setPublishers(List<Publisher> publishers) {
    if (this.publishers == null) {
      this.publishers = new ArrayList<Publisher>();
    }
    this.publishers = publishers;
  }

  public void addPublisher(Publisher publisher) {
    if (this.publishers == null) {
      this.publishers = new ArrayList<Publisher>();
    }
    this.publishers.add(publisher);
  }

  /**
   * Subscribe to this channel
   * @param subscriber the subscriber which wants to subscribe
   */
  public synchronized void subscribe(Subscriber subscriber, ChannelManager channelManager) {
    if (this.subscribers == null) {
      this.subscribers = new ArrayList<Subscriber>();
    }
    subscribers.add(subscriber);
    subscriber.addChannel(this.getId());

    // persist the subscriber
    channelManager.storeSubscriber(subscriber);

    if (channelListeners != null) {
      for (ChannelListener listener : channelListeners) {
        listener.subscribed(this, subscriber);
      }
    }
  }

  /**
   * Unsubscribe from this channel, and updates the channel store information
   * 
   * @param subscriber the subscriber to unsubscribe
   * @param channelManager the channel manager
   */
  public synchronized void unsubscribe(Subscriber subscriber, ChannelManager channelManager) {
    if (this.subscribers == null) {
      return;
    }
    subscribers.remove(subscriber);
    if (channelListeners != null) {
      for (ChannelListener listener : channelListeners) {
        listener.unsubscribed(this, subscriber);
      }
    }
  }

  public void send(Token token, Subscriber subscriber) {
    subscriber.sendToken(token);
  }

  public IOFuture sendAsync(Token token, Subscriber subscriber) {
    return subscriber.sendTokenAsync(token);
  }

  public void broadcast(Token token) {
    for (Subscriber subscriber : subscribers) {
      subscriber.sendToken(token);
    }
  }

  public void broadcastAsync(Token token) {
    for (Subscriber subscriber : subscribers) {
      subscriber.sendTokenAsync(token);
    }
  }

  public void setState(ChannelState state) {
    this.state = state;
  }

  public ChannelState getState() {
    return state;
  }

  public synchronized void registerListener(ChannelListener channelListener) {
    if (channelListeners == null) {
      channelListeners = new ArrayList<ChannelListener>();
    }
    channelListeners.add(channelListener);
  }

  public synchronized void removeListener(ChannelListener channelListener) {
    if (channelListeners != null) {
      channelListeners.remove(channelListener);
    }
  }

  @Override
  public void init() {
    this.state = ChannelState.INITIALIZED;
  }

  @Override
  public void start() {
    this.state = ChannelState.STARTED;
    if (channelListeners != null) {
      for (ChannelListener listener : channelListeners) {
        listener.channelStarted(this);
      }
    }
  }

  @Override
  public void suspend() {
    this.state = ChannelState.SUSPENDED;
    if (channelListeners != null) {
      for (ChannelListener listener : channelListeners) {
        listener.channelSuspended(this);
      }
    }
  }

  @Override
  public void stop() {
    this.state = ChannelState.STOPPED;
    if (channelListeners != null) {
      for (ChannelListener listener : channelListeners) {
        listener.channelStopped(this);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
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

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Channel other = (Channel) obj;
    if (accessKey == null) {
      if (other.accessKey != null) {
        return false;
      }
    } else if (!accessKey.equals(other.accessKey)) {
      return false;
    }
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!id.equals(other.id)) {
      return false;
    }
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    if (secretKey == null) {
      if (other.secretKey != null) {
        return false;
      }
    } else if (!secretKey.equals(other.secretKey)) {
      return false;
    }
    return true;
  }
}
