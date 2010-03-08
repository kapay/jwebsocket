/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.netty.engines;

import org.jboss.netty.channel.ChannelConfig;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ServerChannel;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;

/**
 * Creates a channel pipeline to handle the incoming requests and
 * outgoing responses.
 * <p>
 *   When a {@linkplain ServerChannel server-side channel} accepts a new incoming
 *   connection, a new child channel is created for each newly accepted connection.
 *   A new child channel uses a new {@link ChannelPipeline}, which is created by
 *   the {@link ChannelPipelineFactory} specified in the server-side channel's
 *   {@link ChannelConfig#getPipelineFactory() "pipelineFactory"} option.
 * </p>
 * @author Puran Singh
 * @version $Id$
 *
 */
public class NettyEnginePipeLineFactory implements ChannelPipelineFactory {

    private NettyEngineHandler handler;

    /**
     * default constructor
     * @param handler the server handler
     */
    public NettyEnginePipeLineFactory(NettyEngineHandler handler) {
        this.handler = handler;
    }

    /**
     * {@inheritDoc}
     *
     * NOTE: initially when the server is started http encoder/decoder are
     * added in the channel pipeline which is required for the initial handshake
     * request for WebSocket connection. Once the connection is made by sending
     * the appropriate response the encoder/decoder is replaced at runtime by
     * {@code WebSocketFrameDecoder} and {@code WebSocketFrameEncoder}.
     */
    @Override
    public ChannelPipeline getPipeline() throws Exception {
        // Create a default pipeline implementation.
        ChannelPipeline pipeline = getPipeline();
        pipeline.addLast("decoder", new HttpRequestDecoder());
        pipeline.addLast("aggregator", new HttpChunkAggregator(65536));
        pipeline.addLast("encoder", new HttpResponseEncoder());
        pipeline.addLast("handler", handler);
        return pipeline;
    }
}
