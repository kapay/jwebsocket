//	---------------------------------------------------------------------------
//	jWebSocket - WebSocket Client Interface
//	Copyright (c) 2010 jWebSocket.org, Alexander Schulze, Innotrade GmbH
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
package org.jwebsocket.client.java;

import org.jwebsocket.api.WebSocketMessage;

/**
 * Based on the implementation from http://weberknecht.googlecode.com
 * 
 * @author Roderick Baier
 * @author agali
 * @version $Id:$
 */
public class BaseWebSocketMessage implements WebSocketMessage {

    private Byte[] message;

    /**
     * contruct the websocketMessage
     * @param message client message
     */
    public BaseWebSocketMessage(final Byte[] message) {
        this.message = message;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getByteData() {
        byte[] message = new byte[this.message.length];
        for (int i = 0; i < this.message.length; i++) {
            message[i] = this.message[i];
        }
        return message;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getText() {
        try {
            return new String(getByteData(), "UTF-8");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * generic implementation for all objects
     * @return object
     */
    @Override
    public Object getObject() {
        return null; // TODO implement
    }

}
