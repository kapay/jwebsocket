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
 * Base interface that represents the <tt>jWebSocket</tt> java client and that implements 
 * all the jWebSocket specific protocols and listeners for different types of 
 * communication and data format. The implementation of this interface handles all 
 * the data formats and client protocols.
 * @author aschulze
 * @author puran
 * @version $Id$
 */
public interface JWebSocketClient {
    /**
     * Opens the jWebSocket connection
     * @param uriString the websocket connection url
     * @throws WebSocketException if therre's an 
     */
    void open(String uriString) throws WebSocketException;

    /**
     * Sends the data to the jWebSocket server
     * @param aData the data to send
     * @param aEncoding the encoding type
     * @throws WebSocketException if there's any exception while sending the data
     */
    void send(String aData, String aEncoding) throws WebSocketException;

    /**
     * @param aData
     * @param aEncoding
     * @throws WebSocketException
     */
    public void received(String aData, String aEncoding) throws WebSocketException;

    /**
     * 
     * @param aData
     * @throws WebSocketException
     */
    public void send(byte[] aData) throws WebSocketException;

    /**
     * 
     * @param aData
     * @throws WebSocketException
     */
    public void received(byte[] aData) throws WebSocketException;

    /**
     * 
     * @throws WebSocketException
     */
    public void close() throws WebSocketException;

    /**
     * 
     * @return
     */
    public boolean isConnected();

    /**
    *
    */
    void notifyOpened(WebSocketClientEvent aEvent);

    /**
    *
    */
    void notifyPacket(WebSocketClientEvent aEvent, WebSocketPacket aPacket);

    /**
    *
    */
    void notifyClosed(WebSocketClientEvent aEvent);

    /**
     * Adds the <tt>listener</tt> for <tt>WebSocket</tt> event notification
     * @param aListener the event listner object
     */
    void addListener(WebSocketClientListener aListener);

    /**
     * Remove the listener from the list of listeners, once the listener is
     * removed it won't be notified of any <tt>WebSocket</tt> events.
     * 
     * @param aListener the listener object to remove
     */
    void removeListener(WebSocketClientListener aListener);

    /**
     * Returns the list of listeners registered.
     * 
     * @return the list of listeners.
     */
    List<WebSocketClientListener> getListeners();
}
