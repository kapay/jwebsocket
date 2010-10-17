//  ---------------------------------------------------------------------------
//  jWebSocket - RequestHeader Object
//  Copyright (c) 2010 jWebSocket
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
package org.jwebsocket.plugins.channels;

import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.async.IOFuture;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.Token;
/**
 * Class that represents the subscriber of a channel
 * @author puran
 * @version $Id$
 */
public class Subscriber {
    private String id;
    private WebSocketConnector connector;
    private TokenServer tokenServer;
    
    /**
     * Default constructor
     */
    public Subscriber(String id) {
        this.id = id;
        this.connector = null;
    }
    public Subscriber(WebSocketConnector theConnector, TokenServer theServer) {
        this.id = theConnector.getId();
        this.connector = theConnector;
        this.tokenServer = theServer;
    }
    /**
     * @return the id
     */
    public String getId() {
        return id;
    }
    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }
    /**
     * @return the connector
     */
    public WebSocketConnector getConnector() {
        return connector;
    }
    /**
     * @param connector the connector to set
     */
    public void setConnector(WebSocketConnector connector) {
        this.connector = connector;
    }
    
    public IOFuture sendTokenAsync(Token token) {
        return tokenServer.sendTokenAsync(connector, token);
    }
    
    public void sendToken(Token token) {
        tokenServer.sendToken(connector, token);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Subscriber other = (Subscriber) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
}
