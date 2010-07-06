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

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpContentDecompressor;

/**
 * Pipeline factory
 * @author puran
 * @version $Id$
 */
public class JWebSocketChannelPipelineFactory implements ChannelPipelineFactory {

    private final boolean secure;
    private final JWebSocketClient webSocketClient;

    /**
     * Constructor 
     * @param ssl secure flag
     * @param client web socket client
     */
    public JWebSocketChannelPipelineFactory(boolean secure, JWebSocketClient client) {
        this.secure = secure;
        this.webSocketClient = client;
    }

    public ChannelPipeline getPipeline() throws Exception {
        // Create a default pipeline implementation.
        ChannelPipeline pipeline = Channels.pipeline();

        // Enable wss if necessary.
        if (secure) {
            //TODO:add ssl handler
        }

        pipeline.addLast("codec", new HttpClientCodec());
        pipeline.addLast("inflater", new HttpContentDecompressor());

        pipeline.addLast("handler", new JWebSocketClientHandler(webSocketClient));
        return pipeline;
    }
}