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
package org.jwebsocket.client.nio;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;

/**
 * This class handles all the websocket connection operation for {@code
 * JWebSocketClient}
 * 
 * @author <a href="http://www.purans.net/">Puran Singh</a>
 */
public class JWebSocketConnectionManager {

    private URI uri = null;

    // Configure the client.
    ClientBootstrap bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(Executors.newCachedThreadPool(),
            Executors.newCachedThreadPool()));

    public Channel openWebSocektConnection(String aURL, JWebSocketClient jWebSocketClient) {
        try {
            uri = new URI(aURL);
        } catch (URISyntaxException e) {
            return null;
        }
        String scheme = uri.getScheme() == null ? "ws" : uri.getScheme();
        String host = uri.getHost() == null ? "localhost" : uri.getHost();
        int port = uri.getPort();
        if (port == -1) {
            if (scheme.equalsIgnoreCase("ws")) {
                port = 80;
            } else if (scheme.equalsIgnoreCase("wss")) {
                port = 443;
            }
        }

        boolean secure = scheme.equalsIgnoreCase("wss");

        // Set up the event pipeline factory.
        bootstrap.setPipelineFactory(new JWebSocketChannelPipelineFactory(secure, jWebSocketClient));

        // Start the connection attempt.
        ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));

        // Wait until the connection attempt succeeds or fails.
        Channel channel = future.awaitUninterruptibly().getChannel();
        if (!future.isSuccess()) {
            future.getCause().printStackTrace();
            bootstrap.releaseExternalResources();
            return null;
        }
        return channel;
    }

    public void closeOperation(Channel channel) {
        // Wait for the server to close the connection.
        channel.getCloseFuture().awaitUninterruptibly();
        // Shut down executor threads to exit.
        bootstrap.releaseExternalResources();
    }
    /**
     * Constructs the WebSocket Handshake request 
     * @return the http handshaking request 
     */
    public HttpRequest getWebSocketHandShakeRequest() {
        // Prepare the HTTP request.
        HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uri.getPath());
        request.setHeader(HttpHeaders.Names.UPGRADE, HttpHeaders.Values.WEBSOCKET);
        request.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.UPGRADE);
        request.setHeader(HttpHeaders.Names.HOST, uri.getHost());
        request.setHeader(HttpHeaders.Names.ORIGIN, "http://" + uri.getHost());
        // Send the HTTP request.
        return request;
    }
}
