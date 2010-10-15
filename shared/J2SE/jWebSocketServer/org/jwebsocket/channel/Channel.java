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
package org.jwebsocket.channel;

import java.util.Collections;
import java.util.List;

import org.jwebsocket.token.Token;

import javolution.util.FastList;

/**
 * Channel class represents the data channel 
 * @author puran
 * @version $Id$
 */
public class Channel implements ChannelLifeCycle {
  private String id;
  private String name;
  private String dbKey;
  private int subscriberCount;
  private boolean isPrivate;
  private List<Subscriber> subscribers;
  
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
  public String getDbKey() {
    return dbKey;
  }
  public void setDbKey(String dbKey) {
    this.dbKey = dbKey;
  }
  public int getSubscriberCount() {
    return subscriberCount;
  }
  public void setSubscriberCount(int subscriberCount) {
    this.subscriberCount = subscriberCount;
  }
  public boolean isPrivate() {
    return isPrivate;
  }
  public void setPrivate(boolean isPrivate) {
    this.isPrivate = isPrivate;
  }
  public List<Subscriber> getSubscribers() {
    return Collections.unmodifiableList(subscribers);
  }
  public void setSubscribers(List<Subscriber> subscribers) {
    this.subscribers = subscribers;
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
    // TODO Auto-generated method stub
    
  }
  @Override
  public void start() {
    // TODO Auto-generated method stub
    
  }
  @Override
  public void stop() {
    // TODO Auto-generated method stub
    
  }
}
