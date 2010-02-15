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
package org.jwebsocket.server.impl;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.websocket.DefaultWebSocketFrame;
import org.jwebsocket.server.api.ConnectorContext;

/**
 * Standard Implementation of {@code ConnectorContext} which wraps the {@code
 * ChannelHandlerContext} object so that client connectors of jWebSocket can
 * access all the low level methods provided by Netty Framework.
 * 
 * @author Puran Singh
 * @version $Id$
 * 
 */
public class JWebSocketConnectorContext implements ConnectorContext {

	private ChannelHandlerContext channelHandlerContext;

	/**
	 * default constructor
	 * 
	 * @param context
	 *            the channel handler context object
	 */
	public JWebSocketConnectorContext(ChannelHandlerContext context) {
		this.channelHandlerContext = context;
	}

	/**
	 * {@inheritDoc}
	 */
	public void sendString(String stringData) {
		channelHandlerContext.getChannel().write(
				new DefaultWebSocketFrame(stringData));
	}
}
