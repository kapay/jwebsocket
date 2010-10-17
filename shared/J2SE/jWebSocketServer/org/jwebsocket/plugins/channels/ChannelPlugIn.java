//  ---------------------------------------------------------------------------
//  jWebSocket - RequestHeader Object
//  Copyright (c) 2010 Alexander Schulze, Innotrade GmbH
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
package org.jwebsocket.plugins.channels;

import java.util.Random;

import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.async.IOFuture;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.kit.RequestHeader;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.util.Tools;

/**
 * Token based implementation of the channel plugin. It's based on a publisher/subscriber 
 * model where channels can be either used to publish the data by one or more registered 
 * publishers and subscribed by multiple subscribers.
 * @author puran
 * @version $Id$
 */
public class ChannelPlugIn extends TokenPlugIn {
    /**
     * logger
     */
    private static Logger log = Logging.getLogger(ChannelPlugIn.class);
    /**
     * Channel Manager that manages all the channels
     */
    private ChannelManager channelManager = new ChannelManager();
    /**
     * Namespace for channels
     */
    private static String NS_CHANNELS_DEFAULT = JWebSocketServerConstants.NS_BASE + ".plugins.channel";
    /**
     * Logger channel
     */
    private Channel loggerChannel = null;
    /**
     * admin channel
     */
    private Channel adminChannel = null;

    /**
     * Constructor with plugin config
     * @param configuration the plugin configuration for this PlugIn
     */
    public ChannelPlugIn(PluginConfiguration configuration) {
        super(configuration);
        if (log.isDebugEnabled()) {
            log.debug("Instantiating channel plug-in...");
        }
        // specify default name space
        this.setNamespace(NS_CHANNELS_DEFAULT);
    }

    /**
     * {@inheritDoc} When the engine starts perform all the initialization of
     * default and system channels and start it for accepting subscriptions.
     */
    @Override
    public void engineStarted(WebSocketEngine aEngine) {
        channelManager.startSystemChannels(getPluginConfiguration());
    }

    /**
     * {@inheritDoc} Stops the system channels and clean up all the taken
     * resources by those channels.
     */
    @Override
    public void engineStopped(WebSocketEngine aEngine) {
        channelManager.stopSystemChannels(getPluginConfiguration());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void connectorStarted(WebSocketConnector theConnector) {
        // set session id first, so that it can be processed in the
        // connectorStarted method
        Random rand = new Random(System.nanoTime());
        theConnector.getSession().setSessionId(Tools.getMD5(theConnector.generateUID() + "." + rand.nextInt()));
        RequestHeader request = theConnector.getHeader();
        String channel = request.getString("channel");
        String subscriberKey = request.getString("subscriber_key");
        if (channel == null || "".equals(channel)) {
            sendError(theConnector, CloseReason.CLIENT, "Subscribe failed, channel value is null");
        }
        // call super connectorStarted
        super.connectorStarted(theConnector);
    }

    public void processToken(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {
    }

    @Override
    public void processPacket(PlugInResponse aResponse, WebSocketConnector aConnector, WebSocketPacket aDataPacket) {
        //
    }
    @Override
    public void connectorStopped(WebSocketConnector aConnector, CloseReason closeReason) {
    }

    /**
     * Send the error token to the client and stops the connector that means
     * connection is closed
     * 
     * @param theConnector the target connector
     * @param closeReason the reason for error
     * @param error the error message
     */
    private void sendError(WebSocketConnector theConnector, CloseReason closeReason, String error) {
        if (log.isDebugEnabled()) {
            log.debug("Cannot subscribe to a channel, disconnecting..");
        }
        // send "error" token to client
        Token errorToken = TokenFactory.createToken("close");
        errorToken.setString("vendor", JWebSocketCommonConstants.VENDOR);
        errorToken.setString("version", JWebSocketServerConstants.VERSION_STR);
        errorToken.setString("sourceId", theConnector.getId());
        errorToken.setString("error", error);
        if (closeReason != null) {
            errorToken.setString("reason", closeReason.toString().toLowerCase());
        }
        // don't send session-id on good bye, neither required nor desired
        IOFuture future = sendTokenAsync(theConnector, theConnector, errorToken);
        if (future != null) {
            if (future.isDone()) {
                theConnector.stopConnector(CloseReason.CLIENT);
            }
        }
    }
}
