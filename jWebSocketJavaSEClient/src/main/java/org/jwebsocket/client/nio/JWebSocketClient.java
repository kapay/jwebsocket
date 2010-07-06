//  ---------------------------------------------------------------------------
//  jWebSocket - Copyright (c) 2010 jwebsocket.org
//  ---------------------------------------------------------------------------
//  This program is free software; you can redistribute it and/or modify it
//  under the terms of the GNU General Public License as published by the
//  Free Software Foundation; either version 3 of the License, or (at your
//  option) any later version.
//  This program is distributed in the hope that it will be useful, but WITHOUT
//  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
//  more details.
//  You should have received a copy of the GNU General Public License along
//  with this program; if not, see <http://www.gnu.org/licenses/>.
//  ---------------------------------------------------------------------------
package org.jwebsocket.client.nio;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jwebsocket.client.se.BaseClient;
import org.jwebsocket.kit.WebSocketException;

/**
 * NIO based implementation of {@code WebSocketClient}
 * 
 * @author puran
 * @version $Id$
 */
public class JWebSocketClient extends BaseClient {

    private volatile boolean isConnected = false;
    private Channel channel = null;

    private JWebSocketConnectionManager connectionManager = new JWebSocketConnectionManager();
    ExecutorService service = Executors.newCachedThreadPool();

    @Override
    public void close() throws WebSocketException {

    }

    @Override
    public boolean isConnected() {
        return (isConnected && (channel != null && channel.isConnected()));
    }

    @Override
    public void open(String aURL) throws WebSocketException {
        //open the channel
        channel = connectionManager.openWebSocektConnection(aURL, this);
        if (channel == null) {
            isConnected = false;
            return;
        }
        //send the client handshake request
        HttpRequest request = connectionManager.getWebSocketHandShakeRequest();
        channel.write(request);

        // wait for the channel to close normally or abnormally
        // and clean up the resources.
        service.execute(new Runnable() {
            @Override
            public void run() {
                connectionManager.closeOperation(channel);
            }
        });
    }

    @Override
    public void received(String aData, String aEncoding) throws WebSocketException {
    }

    @Override
    public void received(byte[] aData) throws WebSocketException {
    }

    @Override
    public void send(String aData, String aEncoding) throws WebSocketException {

    }

    @Override
    public void send(byte[] aData) throws WebSocketException {
    }
}
