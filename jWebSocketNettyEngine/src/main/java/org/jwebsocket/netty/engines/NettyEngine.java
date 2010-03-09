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

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.engines.BaseEngine;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.WebSocketException;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jwebsocket.logging.Logging;


/**
 * Netty based implementation of {@code WebSocket} engine.
 * @author puran
 * @version $Id$
 */
public class NettyEngine extends BaseEngine {

    private static Logger log = Logging.getLogger(NettyEngine.class);
    private int listenerPort = 8787;
    private int sessionTimeout = 120000;
    private volatile boolean isRunning = false;
    private static final ChannelGroup allChannels = new DefaultChannelGroup("time-server");

    /**
     * Constructor of the Netty based engine. The port and the default session
     * timeout have to be passed. The session timout passed here is used only 
     * when no explicit timeout per connection is specified.
     * @param aPort TCP port the engine listens on.
     * @param aSessionTimeout The default server side session time out.
     * @throws WebSocketException if exception creating <tt>NettyEngine</tt>
     */
    public NettyEngine(String aId, int aPort, int aSessionTimeout)
            throws WebSocketException {
        super(aId);
        listenerPort = aPort;
        sessionTimeout = aSessionTimeout;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startEngine()
            throws WebSocketException {
        if (log.isDebugEnabled()) {
            log.debug("Starting TCP engine...");
        }
        // Configure the server.
        //TODO: figure out more on how advanced we can configure
        ServerBootstrap bootstrap = new ServerBootstrap(
                new NioServerSocketChannelFactory(Executors.newCachedThreadPool()
                ,Executors.newCachedThreadPool()));

        //initialize the server handler
        NettyEngineHandler handler = new NettyEngineHandler(this);
        //Set up the event pipeline factory.
        bootstrap.setPipelineFactory(new NettyEnginePipeLineFactory(handler));
        //Bind and start to accept incoming connections.
        Channel channel = bootstrap.bind(new InetSocketAddress(listenerPort));

        //fire the engine start event
        engineStarted();

        allChannels.add(channel);

        isRunning = true;

        if (log.isInfoEnabled()) {
            log.info("TCP engine started.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stopEngine(CloseReason aCloseReason)
            throws WebSocketException {
        log.debug("Stopping TCP engine...");

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void connectorStarted(WebSocketConnector aConnector) {
        log.debug("Detected new connector at port " + aConnector.getRemotePort() + ".");
        super.connectorStarted(aConnector);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
        log.debug("Detected stopped connector at port " + aConnector.getRemotePort() + ".");
        super.connectorStopped(aConnector, aCloseReason);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAlive() {
        // TODO: Check isAlive state of TCPEngine
        return true;
    }
}
