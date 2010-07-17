//  ---------------------------------------------------------------------------
//  jWebSocket - Copyright (c) 2010 Innotrade GmbH
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
package org.jwebsocket.api;

import java.util.List;

import org.jwebsocket.kit.WebSocketException;


/**
 * Base interface that represents the <tt>WebSocket</tt> connection to the
 * </tt>jWeSocket</tt> server. It contains all the necessary methods to 
 * communicate with jWebSocket server at the low level. The implementation 
 * of this interface should handle all the low level implementation details of 
 * <tt>WebSocket</tt> connection as per protocol specification client side
 * requirements.
 * 
 * @author Anuradha Gali
 * @author Puran Singh
 * @author Alexander Schulze
 * @version $Id:$
 */
public interface WebSocket {
    /**
     * Opens the <tt>WebSocket</tt> connection on the given URL. URL should 
     * be as per the <tt>WebSocket</tt> URL schema definition, ws://<host>:<port>/resource_name
     * and wss://<host>:<port>/resource_name for the secure connection over port 443.
     * 
     * @param url the WebSocket connection url.
     * @return {@code true} if the connection was successful {@code false} otherwise 
     * @throws WebSocketException if there's any error while opening a connection
     */
    boolean open(String url) throws WebSocketException;
  
    /**
     * Sends the data to the server,data is sent in the form of UTF-8 text. 
     * Also the implementation of the this method should make sure that 
     * <tt>dataFrameSize</tt> should not be more than the maximum size allowed
     * by the <tt>jWebSocket</tt> server. If data frame size exceeds the maximum
     * allowable frameSize then the connection is terminated immediately by both 
     * parties.
     * 
     * @param data the UTF-8 encoded string data
     * @throws WebSocketException if exception during data send
     */
    void send(String data) throws WebSocketException;

    /**
     * Sends the byte data to the server
     * @param data the UTF-8 byte data
     * @throws WebSocketException if there's any exception while send 
     */
    void send(byte[] data) throws WebSocketException;

    /**
     * Close the <tt>WebSocket</tt> connection
     * @throws WebSocketException if exception occurs while closing connection
     */
    void close() throws WebSocketException;

    /**
     * Returns {@code true} if connection status is {@code WebSocketStatus.OPEN}
     * {@code false} otherwise.
     * @return true or false
     */
    boolean isConnected();
    
    /**
     * Returns the <tt>WebSocket</tt> connection status
     * <tt>CONNECTING - 0</tt>
     * <tt>OPEN - 1</tt>
     * <tt>CLOSING - 2</tt>
     * <tt>CLOSED - 3</tt>
     * @return the connect status 
     */
    WebSocketStatus getConnectionStatus();
    
    /**
     * Adds the <tt>listener</tt> for <tt>WebSocket</tt> event notification 
     * @param aListener the event listner object
     */
    void addListener(WebSocketClientListener aListener);

    /**
     * Remove the listener from the list of listeners, once the listener is 
     * removed it won't be notified of any <tt>WebSocket</tt> events.
     * @param aListener the listener object to remove 
     */
    void removeListener(WebSocketClientListener aListener);
    
    /**
     * Returns the list of listeners registered.
     * @return the list of listeners.
     */
    List<WebSocketClientListener> getListeners();
}
