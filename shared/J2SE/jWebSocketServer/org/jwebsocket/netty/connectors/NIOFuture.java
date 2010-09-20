// ---------------------------------------------------------------------------
// jWebSocket - Copyright (c) 2010 jwebsocket.org
// ---------------------------------------------------------------------------
// This program is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published by the
// Free Software Foundation; either version 3 of the License, or (at your
// option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
// more details.
// You should have received a copy of the GNU Lesser General Public License along
// with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
// ---------------------------------------------------------------------------
package org.jwebsocket.netty.connectors;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.async.IOFuture;
import org.jwebsocket.async.IOFutureListener;

/**
 * NIO implementation of {@code IOFuture} to support asynchronous
 * I/O operation for the NIO based connector.
 * @author puran
 * @version $Id$
 */
public class NIOFuture implements IOFuture {
  
  private ChannelFuture internalFuture = null; 
  private WebSocketConnector connector = null;
  
  /**
   * The constructor
   * @param theConnector the connector with which this future is associated
   * @param nettyFuture the internal netty future object that does the most 
   * of the work
   */
  public NIOFuture(WebSocketConnector theConnector, ChannelFuture nettyFuture) {
    this.internalFuture = nettyFuture;
    this.connector = theConnector;
  }

  @Override
  public WebSocketConnector getConnector() {
    return connector;
  }

  @Override
  public boolean isDone() {
    return internalFuture.isDone();
  }

  @Override
  public boolean isCancelled() {
    return internalFuture.isCancelled();
  }

  @Override
  public boolean isSuccess() {
    return internalFuture.isSuccess();
  }

  @Override
  public Throwable getCause() {
    return internalFuture.getCause();
  }

  @Override
  public boolean cancel() {
    return internalFuture.cancel();
  }

  @Override
  public boolean setSuccess() {
    return internalFuture.setSuccess();
  }

  @Override
  public boolean setFailure(Throwable cause) {
    return internalFuture.setFailure(cause);
  }

  @Override
  public boolean setProgress(long amount, long current, long total) {
    return internalFuture.setProgress(amount, current, total);
  }

  @Override
  public void addListener(IOFutureListener listener) {
    ChannelFutureListener internalListener = new NIOInternalFutureListener(this, listener);
    internalFuture.addListener(internalListener);
  }

  @Override
  public void removeListener(IOFutureListener listener) {
  }
  
  public ChannelFuture getInternalFuture() {
    return internalFuture;
  }

}
