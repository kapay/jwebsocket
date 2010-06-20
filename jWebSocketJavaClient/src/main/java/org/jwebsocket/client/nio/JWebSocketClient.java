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

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jwebsocket.client.BaseClient;
import org.jwebsocket.kit.WebSocketException;

/**
 * NIO based implementation of {@code WebSocketClient}
 * 
 * @author puran
 * @version $Id:$
 */
public class JWebSocketClient extends BaseClient {

    @Override
    public void close() throws WebSocketException {

    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public void open(String aURL) throws WebSocketException {

        URI uri = null;
        try {
            uri = new URI(aURL);
        } catch (URISyntaxException e) {
        }
        String scheme = uri.getScheme() == null ? "http" : uri.getScheme();
        String host = uri.getHost() == null ? "localhost" : uri.getHost();
        int port = uri.getPort();
        if (port == -1) {
            if (scheme.equalsIgnoreCase("http")) {
                port = 80;
            } else if (scheme.equalsIgnoreCase("https")) {
                port = 443;
            }
        }

        if (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https")) {
            System.err.println("Only HTTP(S) is supported.");
            return;
        }

        boolean ssl = scheme.equalsIgnoreCase("https");

        // Configure the client.
        ClientBootstrap bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));

        // Set up the event pipeline factory.
        bootstrap.setPipelineFactory(new JWebSocketChannelPipelineFactory(ssl));

        // Start the connection attempt.
        ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));

        // Wait until the connection attempt succeeds or fails.
        Channel channel = future.awaitUninterruptibly().getChannel();
        if (!future.isSuccess()) {
            future.getCause().printStackTrace();
            bootstrap.releaseExternalResources();
            return;
        }
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
