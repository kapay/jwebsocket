//	---------------------------------------------------------------------------
//	jWebSocket - Copyright (c) 2010 jwebsocket.org
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
//	more details.
//	You should have received a copy of the GNU General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/>.
//	---------------------------------------------------------------------------
package org.jwebsocket.netty.engines;

import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketConstants;
import org.jwebsocket.config.xml.EngineConfig;
import org.jwebsocket.engines.BaseEngine;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.logging.Logging;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Netty based implementation of {@code WebSocketEngine} engine.It uses the
 * low-level <tt>ServerBootStrap</tt> to start the server and handles the
 * incoming/outgoing request/response using {@code NettyEngineHandler} class.
 *
 * @author puran
 * @version $Id$
 * @see NettyEngineHandler
 */
public class NettyEngine extends BaseEngine {

    private static Logger log = Logging.getLogger(NettyEngine.class);
    private int listenerPort = 8787;
    private int sessionTimeout = 120000;
    private volatile boolean isRunning = false;
    private static final ChannelGroup allChannels = new DefaultChannelGroup(
            "jWebSocket-NettyEngine");
    private EngineConfig engineConfig;

    /**
     * Constructor of the Netty based engine. The port and the default session
     * timeout have to be passed. The session timout passed here is used only
     * when no explicit timeout per connection is specified.
     *
     * @param aPort           TCP port the engine listens on.
     * @param aSessionTimeout The default server side session time out.
     * @throws WebSocketException if exception creating <tt>NettyEngine</tt>
     */
    public NettyEngine(String aId, Integer aPort, Integer aSessionTimeout)
            throws WebSocketException {
        super(aId);
        listenerPort = aPort;
        sessionTimeout = aSessionTimeout;
    }

    /**
     * Construtor that takes the engine config as the argument
     *
     * @param theEngineConfig the engine config object
     */
/*
    public NettyEngine(EngineConfig theEngineConfig) {
        super(theEngineConfig.getId());
        this.engineConfig = theEngineConfig;
    }
*/

    /**
     * {@inheritDoc}
     */
    @Override
    public void startEngine() throws WebSocketException {
        if (log.isDebugEnabled()) {
            log.debug("Starting Netty engine (" + getId() + ")...");
        }
        // Configure the server.
        ServerBootstrap bootstrap = new ServerBootstrap(
                new NioServerSocketChannelFactory(Executors
                        .newCachedThreadPool(), Executors.newCachedThreadPool()));

        // Set up the event pipeline factory.
        bootstrap.setPipelineFactory(new NettyEnginePipeLineFactory(this));
        // Bind and start to accept incoming connections.
        Channel channel = bootstrap.bind(new InetSocketAddress(listenerPort));

        // fire the engine start event
        engineStarted();

        allChannels.add(channel);

        isRunning = true;

        if (log.isInfoEnabled()) {
            log.info("Netty engine (" + getId() + ") started.");
        }
        // close the engine
        if (!isRunning) {
            ChannelGroupFuture future = allChannels.close();
            future.awaitUninterruptibly();
            channel.getFactory().releaseExternalResources();
            engineStopped();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stopEngine(CloseReason aCloseReason) throws WebSocketException {
        log.debug("Stopping Netty engine (" + getId() + ")...");
        isRunning = false;
        super.stopEngine(aCloseReason);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void connectorStarted(WebSocketConnector aConnector) {
        log.debug("Detected new connector at port "
                + aConnector.getRemotePort() + ".");
        super.connectorStarted(aConnector);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void connectorStopped(WebSocketConnector aConnector,
                                 CloseReason aCloseReason) {
        log.debug("Detected stopped connector at port "
                + aConnector.getRemotePort() + ".");
        super.connectorStopped(aConnector, aCloseReason);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAlive() {
        if (isRunning) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return the engine config
     */
    public EngineConfig getEngineConfig() {
        return engineConfig;
    }

    /**
     * @return the max frame size
     */
    @Override
    public int getMaxFrameSize() {
        if (engineConfig == null || engineConfig.getMaxframesize() == 0) {
            return JWebSocketConstants.DEFAULT_MAX_FRAME_SIZE;
        } else {
            return engineConfig.getMaxframesize();
        }
    }

}
