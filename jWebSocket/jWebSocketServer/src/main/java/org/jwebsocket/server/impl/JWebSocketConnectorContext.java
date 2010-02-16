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

import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.util.Formatter;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.websocket.DefaultWebSocketFrame;
import org.jwebsocket.server.api.ConnectorContext;
import org.jwebsocket.server.api.JWebSocketServer;

/**
 * Standard Implementation of {@code ConnectorContext} which wraps the {@code
 * ChannelHandlerContext} object so that client connectors of jWebSocket can
 * access all the low level methods provided by Netty Framework.
 * 
 * @author Puran Singh
 * @version $Id: JWebSocketConnectorContext.java 58 2010-02-15 19:21:56Z
 *          mailtopuran $
 * 
 */
public class JWebSocketConnectorContext implements ConnectorContext {

	private ChannelHandlerContext channelHandlerContext;
	private JWebSocketServer server;

	/**
	 * default constructor
	 * 
	 * @param context
	 *            the channel handler context object
	 */
	public JWebSocketConnectorContext(ChannelHandlerContext context, JWebSocketServer server) {
		this.channelHandlerContext = context;
		this.server = server;
	}

	/**
	 * {@inheritDoc}
	 */
	public void sendString(String stringData) {
		channelHandlerContext.getChannel().write(
				new DefaultWebSocketFrame(stringData));
	}

	/**
	 * {@inheritDoc}
	 */
	public int getLocalPort() {
		InetSocketAddress address = (InetSocketAddress) channelHandlerContext
				.getChannel().getLocalAddress();
		return address.getPort();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getSessionId() {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
			byte[] lBufTarget = md.digest();
			Formatter formatter = new Formatter();
			for (byte b : lBufTarget) {
				formatter.format("%02x", b);
			}
			return (formatter.toString());
		} catch (Exception ex) {
			// log.error("getMD5: " + ex.getMessage());
			System.out.println("getMD5: " + ex.getMessage());
		}
		return null;

	}

	/**
	 * {@inheritDoc}
	 */
	public JWebSocketServer getJWebSocketServer() {
		return server;
	}

}
