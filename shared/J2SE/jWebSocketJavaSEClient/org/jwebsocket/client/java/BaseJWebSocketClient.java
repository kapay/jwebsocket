//  ---------------------------------------------------------------------------
//  jWebSocket - Abstract WebSocket Client
//  Copyright (c) 2010 jWebSocket.org, Alexander Schulze, Innotrade GmbH
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
package org.jwebsocket.client.java;

import java.util.List;

import org.jwebsocket.api.WebSocketClientListener;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.kit.WebSocketException;

/**
 * Base implementation of {@code JWebSocketClient} interface to provide the
 * <tt>jWebSocket</tt> protocol specific features.
 * 
 * @author puran
 * @version $Id:$
 */
public class BaseJWebSocketClient extends AbstractJWebSocketClient {
    /**
     * {@inheritDoc}
     */
    @Override
    public void addListener(WebSocketClientListener aListener) {
        super.addListener(aListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeListener(WebSocketClientListener aListener) {
        super.removeListener(aListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<WebSocketClientListener> getListeners() {
        return super.getListeners();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws WebSocketException {
        super.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isConnected() {
        return super.isConnected();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(String aData, String aEncoding) throws WebSocketException {
        super.send(aData, aEncoding);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(byte[] aData) throws WebSocketException {
        super.send(aData);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(WebSocketPacket dataPacket) throws WebSocketException {
        super.send(dataPacket);
    }
}
