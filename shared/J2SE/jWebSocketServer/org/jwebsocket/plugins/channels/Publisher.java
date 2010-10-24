//  ---------------------------------------------------------------------------
//  jWebSocket - Channel Publisher
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

import java.util.Date;

import org.jwebsocket.api.WebSocketConnector;

/**
 * Represents the single publisher connected the the particular channel
 * @author puran
 * @version $Id$
 */
public final class Publisher {
  private String id;
  private String channel;
  private WebSocketConnector connector;
  private Date authorizedDate;
  private Date lastPublishedDate;
  private boolean authorized;

  public Publisher(WebSocketConnector connector, String channel, Date authorizedDate, Date lastPublishedDate, boolean authorized) {
    this.id = connector.getId();
    this.channel = channel;
    this.connector = connector;
    this.authorizedDate = authorizedDate;
    this.lastPublishedDate = lastPublishedDate;
    this.authorized = authorized;
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }
  /**
   * @return the channel
   */
  public String getChannel() {
    return channel;
  }

  /**
   * @return the connector
   */
  public WebSocketConnector getConnector() {
    return connector;
  }

  /**
   * @return the authorizedDate
   */
  public Date getAuthorizedDate() {
    return authorizedDate;
  }

  /**
   * @return the authorized
   */
  public boolean isAuthorized() {
    return authorized;
  }

  /**
   * @return the lastPublishedDate
   */
  public Date getLastPublishedDate() {
    return lastPublishedDate;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((channel == null) ? 0 : channel.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
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
    Publisher other = (Publisher) obj;
    if (channel == null) {
      if (other.channel != null)
        return false;
    } else if (!channel.equals(other.channel))
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
  }
}
