//  ---------------------------------------------------------------------------
//  jWebSocket - Channel Subscriber
//  Copyright (c) 2010 Innotrade GmbH, jWebSocket.org
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
package org.jwebsocket.plugins.jms;

import java.util.List;

import javolution.util.FastList;

import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.async.IOFuture;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.plugins.jms.infra.impl.JmsListenerContainer;
import org.jwebsocket.token.Token;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

/**
 * Class that represents the consumer of jms destinations
 * 
 * @author jsmutny
 * 
 */
public class Consumer {

	private String mConnectionId;
	private List<JmsListenerContainer> mListenerContainers = new FastList<JmsListenerContainer>();

	/**
	 * Default constructor
	 * 
	 * @param aConnectionId
	 */
	public Consumer(String aConnectionId) {
		this.mConnectionId = aConnectionId;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return mConnectionId;
	}

	/**
	 * @return the connector
	 */
	public WebSocketConnector getConnector() {
		WebSocketConnector lConnector = JWebSocketFactory.getTokenServer()
				.getConnector(mConnectionId);
		return lConnector;
	}

	/**
	 * @return the channels
	 */
	public List<JmsListenerContainer> getListenerContainers() {
		return mListenerContainers;
	}

	/**
	 * Add the channel id to the list of channels this subscriber is subscribed
	 * 
	 * @param aChannel
	 */
	public void addListenerContainer(
			JmsListenerContainer aListenerContainer) {
		if (this.mListenerContainers != null) {
			this.mListenerContainers.add(aListenerContainer);
		}
	}

	/**
	 * Removes the channel from the subscriber list of channels
	 * 
	 * @param aChannel
	 *            the channel id to remove.
	 */
	public void removeListenerContainer(
			DefaultMessageListenerContainer aListenerContainer) {
		if (this.mListenerContainers != null) {
			this.mListenerContainers.remove(aListenerContainer);
		}
	}

	/**
	 * Sends the token data asynchronously to the token server
	 * 
	 * @param aToken
	 *            the token data
	 * @return future object for IO status
	 */
	public IOFuture sendTokenAsync(Token aToken) {
		WebSocketConnector lConnector = getConnector();
		if (lConnector != null && aToken != null) {
			return JWebSocketFactory.getTokenServer().sendTokenAsync(
					lConnector, aToken);
		}
		return null;
	}

	/**
	 * 
	 * @param aToken
	 */
	public void sendToken(Token aToken) {
		WebSocketConnector lConnector = getConnector();
		if (lConnector != null && aToken != null) {
			JWebSocketFactory.getTokenServer().sendToken(lConnector, aToken);
		}
	}
}
