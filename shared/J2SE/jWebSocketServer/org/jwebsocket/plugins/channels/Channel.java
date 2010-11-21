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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jwebsocket.async.IOFuture;
import org.jwebsocket.config.xml.ChannelConfig;
import org.jwebsocket.security.Right;
import org.jwebsocket.security.Rights;
import org.jwebsocket.security.SecurityFactory;
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
 * Also <tt>CopyOnWriteArrayList</tt> has been used for the list of subscribers,
 * publishers and channel listeners for the concurrent access. Although it is
 * expensive but considering the fact that number of traversal for broadcasting
 * data or callback on listeners on events would be more than insertion and
 * removal.
 * 
 * @author puran
 * @version $Id$
 */
public final class Channel implements ChannelLifeCycle {
    private String id;
    private String name;
    private boolean privateChannel;
    private boolean systemChannel;
    private String secretKey;
    private String accessKey;
    private long createdDate;
    private String owner;
    private boolean authenticated = false;

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

    /**
     * Initialize the new channel but it doesn't start.
     * 
     * @param config
     *            the channel config
     */
    public Channel(ChannelConfig config) {
        this.id = config.getId();
        this.name = config.getName();
        this.privateChannel = config.isPrivateChannel();
        this.systemChannel = config.isSystemChannel();
        this.secretKey = config.getSecretKey();
        this.accessKey = config.getAccessKey();
        this.owner = config.getOwner();
        this.createdDate = System.currentTimeMillis();
        this.state = ChannelState.INITIALIZED;
        this.authenticated = false;
    }

    public Channel(String id, String name, int subscriberCount, boolean privateChannel, boolean systemChannel,
            String secretKey, String accessKey, String owner, long createdDate, ChannelState state,
            List<Subscriber> subscribers, List<Publisher> publishers) {
        this.id = id;
        this.name = name;
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
        return subscribers.size();
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
     * Set the subscribers to this channel. Note that this method simply
     * replaces the existing list of subscribers.
     * 
     * @param subscribers
     *            the list of subscribers
     */
    public void setSubscribers(List<Subscriber> subscribers) {
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
     *            the publishers to set
     */
    public void setPublishers(List<Publisher> publishers) {
        if (this.publishers == null) {
            this.publishers = new CopyOnWriteArrayList<Publisher>();
        }
        this.publishers = publishers;
    }

    /**
     * Add the publisher to the list of publishers.
     * 
     * @param publisher
     *            the publisher to add
     */
    public void addPublisher(Publisher publisher) {
        if (this.publishers == null) {
            this.publishers = new CopyOnWriteArrayList<Publisher>();
        }
        this.publishers.add(publisher);
    }

    /**
     * Subscribe to this channel
     * 
     * @param subscriber
     *            the subscriber which wants to subscribe
     */
    public void subscribe(Subscriber subscriber, ChannelManager channelManager) {
        if (this.subscribers == null) {
            this.subscribers = new CopyOnWriteArrayList<Subscriber>();
        }
        if (!subscribers.contains(subscriber)) {
            subscribers.add(subscriber);
            subscriber.addChannel(this.getId());

            // persist the subscriber
            channelManager.storeSubscriber(subscriber);
            if (channelListeners != null) {
                for (ChannelListener listener : channelListeners) {
                    try {
                        listener.subscribed(this, subscriber);
                    } catch (Exception es) {
                        // trap for any exception so that if any of the
                        // listener implementation fails or throws exception
                        // we continue with others.
                    }
                }
            }
        }
    }

    /**
     * Unsubscribe from this channel, and updates the channel store information
     * 
     * @param subscriber
     *            the subscriber to unsubscribe
     * @param channelManager
     *            the channel manager
     */
    public void unsubscribe(Subscriber subscriber, ChannelManager channelManager) {
        if (this.subscribers == null) {
            return;
        }
        if (subscribers.contains(subscriber)) {
            subscribers.remove(subscriber);
            if (channelListeners != null) {
                for (ChannelListener listener : channelListeners) {
                    listener.unsubscribed(this, subscriber);
                }
            }
        }
    }

    /**
     * Sends the data to the given subscriber. Note that this send operation
     * will block the current thread until the send operation is complete. for
     * asynchronous send operation use <tt>sendAsync</tt> method.
     * 
     * @param token
     *            the token data to send
     * @param subscriber
     *            the target subscriber
     */
    public void send(Token token, Subscriber subscriber) {
        subscriber.sendToken(token);
    }

    /**
     * Sends the data to the given target subscriber asynchronously.
     * 
     * @param token
     *            the token data to send
     * @param subscriber
     *            the target subscriber
     * @return the future object to keep track of send operation
     */
    public IOFuture sendAsync(Token token, Subscriber subscriber) {
        return subscriber.sendTokenAsync(token);
    }

    /**
     * broadcasts data to the subscribers asynchronously. It performs the
     * concurrent broadcast to all the subscribers and wait for the all the
     * broadcast task to complete only for 1 second maximum.
     * 
     * @param token
     *            the token data for the subscribers
     */
    public void broadcastToken(final Token token) {
        ExecutorService executor = Executors.newCachedThreadPool();
        for (final Subscriber subscriber : subscribers) {
            executor.submit(new Runnable() {

                @Override
                public void run() {
                    subscriber.sendTokenAsync(token);
                }
            });
        }
        try {
            executor.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // just ignore and return
        }
    }

    /**
     * Returns the channel state
     * 
     * @return the state
     */
    public ChannelState getState() {
        return state;
    }

    /**
     * Register the channel listener to the list of listeners
     * 
     * @param channelListener
     *            the channel listener to register
     */
    public void registerListener(ChannelListener channelListener) {
        if (channelListeners == null) {
            channelListeners = new CopyOnWriteArrayList<ChannelListener>();
        }
        channelListeners.add(channelListener);
    }

    public void removeListener(ChannelListener channelListener) {
        if (channelListeners != null) {
            channelListeners.remove(channelListener);
        }
    }

    @Override
    public void init() {
        this.state = ChannelState.INITIALIZED;
    }

    @Override
    public void start(final String user) throws ChannelLifeCycleException {
        if (this.state == ChannelState.STARTED) {
            throw new ChannelLifeCycleException("Channel:[" + this.getName() + "] is already started");
        }
        if (!SecurityFactory.isValidUser(user)) {
            throw new ChannelLifeCycleException("Cannot start the channel:[" + this.getName()
                    + "] for invalid user login [" + user + "]");
        } else {
            Rights rights = SecurityFactory.getUserRights(user);
            Right right = rights.get("org.jwebsocket.plugins.channel.start");
            if (right == null) {
                throw new ChannelLifeCycleException("User:[" + user + "] does not have rights to start the channel");
            } else {
                // verify the owner
                if (!this.getOwner().equals(user)) {
                    throw new ChannelLifeCycleException("User:[" + user + "] is not a owner of this channel,"
                            + "Only owner of the channel can start");
                }
                this.authenticated = true;
            }
        }
        this.state = ChannelState.STARTED;
        final Channel channel = this;
        if (channelListeners != null) {
            ExecutorService pool = Executors.newCachedThreadPool();
            for (final ChannelListener listener : channelListeners) {
                pool.submit(new Runnable() {
                    @Override
                    public void run() {
                        listener.channelStarted(channel, user);
                    }
                });
            }
            pool.shutdown();
        }
    }

    @Override
    public void suspend(final String user) throws ChannelLifeCycleException {
        if (this.state == ChannelState.SUSPENDED) {
            throw new ChannelLifeCycleException("Channel:[" + this.getName() + "] is already suspended");
        }
        if (!SecurityFactory.isValidUser(user) && !authenticated) {
            throw new ChannelLifeCycleException("Cannot suspend the channel:[" + this.getName()
                    + "] for invalid user login [" + user + "]");
        } else {
            Rights rights = SecurityFactory.getUserRights(user);
            Right right = rights.get("org.jwebsocket.plugins.channel.suspend");
            if (right == null) {
                throw new ChannelLifeCycleException("User:[" + user + "] does not have rights to suspend the channel");
            } else {
                // verify the owner
                if (!this.getOwner().equals(user)) {
                    throw new ChannelLifeCycleException("User:[" + user + "] is not a owner of this channel,"
                            + "Only owner of the channel can suspend");
                }
            }
        }
        this.state = ChannelState.SUSPENDED;
        final Channel channel = this;
        if (channelListeners != null) {
            ExecutorService pool = Executors.newCachedThreadPool();
            for (final ChannelListener listener : channelListeners) {
                pool.submit(new Runnable() {
                    @Override
                    public void run() {
                        listener.channelSuspended(channel, user);
                    }
                });
            }
            pool.shutdown();
        }
    }

    @Override
    public void stop(final String user) throws ChannelLifeCycleException {
        if (this.state == ChannelState.STOPPED) {
            throw new ChannelLifeCycleException("Channel:[" + this.getName() + "] is already stopped");
        }
        if (!SecurityFactory.isValidUser(user) && !authenticated) {
            throw new ChannelLifeCycleException("Cannot stop the channel:[" + this.getName()
                    + "] for invalid user login [" + user + "]");
        } else {
            Rights rights = SecurityFactory.getUserRights(user);
            Right right = rights.get("org.jwebsocket.plugins.channel.stop");
            if (right == null) {
                throw new ChannelLifeCycleException("User:[" + user + "] does not have rights to stop the channel");
            } else {
                // verify the owner
                if (!this.getOwner().equals(user)) {
                    throw new ChannelLifeCycleException("User:[" + user + "] is not a owner of this channel,"
                            + "Only owner of the channel can stop");
                }
            }
        }
        if (this.state == ChannelState.INITIALIZED || this.state == ChannelState.STARTED) {
            this.state = ChannelState.STOPPED;
        }
        final Channel channel = this;
        if (channelListeners != null) {
            ExecutorService pool = Executors.newCachedThreadPool();
            for (final ChannelListener listener : channelListeners) {
                pool.submit(new Runnable() {
                    @Override
                    public void run() {
                        listener.channelStopped(channel, user);
                    }
                });
            }
            pool.shutdown();
        }
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @param secretKey
     *            the secretKey to set
     */
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    /**
     * @param accessKey
     *            the accessKey to set
     */
    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    /**
     * @param owner
     *            the owner to set
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }
}
