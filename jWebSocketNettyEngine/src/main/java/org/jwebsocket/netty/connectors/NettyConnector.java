/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.netty.connectors;

import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.connectors.BaseConnector;

/**
 * Netty based implementation of the {@code BaseConnector} 
 *
 * @author puran
 * @version $Id$
 */
public class NettyConnector extends BaseConnector {

    /**
     * The default constructor
     * @param aEngine the websocket engine object
     */
    public NettyConnector(WebSocketEngine aEngine) {
       super(aEngine);
    }
}
