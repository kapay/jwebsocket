/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.netty.engines;

import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.engines.BaseEngine;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.WebSocketException;

/**
 * Netty based implementation of {@code WebSocket} engine.
 * @author puran
 * @version $Id$
 */
public class NettyEngine extends BaseEngine {

    private static Logger log = Logger.getLogger(NettyEngine.class);
    private int listenerPort = 8787;
    private int sessionTimeout = 120000;
    private volatile boolean isRunning = false;

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
        log.debug("Starting TCP engine...");

        log.info("TCP engine started.");
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
