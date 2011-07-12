//  ---------------------------------------------------------------------------
//  jWebSocket - Channel Publisher
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
import org.jwebsocket.factory.JWebSocketFactory;
import org.springframework.jms.core.JmsTemplate;

/**
 * Class that represents a producer of jms messages
 * 
 * @author jsmutny
 */
public final class Producer {

	private String mConnectionId;
	private List<JmsTemplate> mDestinations = new FastList<JmsTemplate>();

	public Producer(String aConnId) {
		this.mConnectionId = aConnId;
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
		WebSocketConnector lConnector =
				JWebSocketFactory.getTokenServer().getConnector(mConnectionId);
		return lConnector;
	}

	/**
	 * @return the channels
	 */
	public List<JmsTemplate> getDestinations() {
		return mDestinations;
	}

	/**
	 * Add the channel id to the list of channels this subscriber is
	 * subscribed
	 *
	 * @param aChannel
	 */
	public void addDestination(JmsTemplate aDestination) {
		if (this.mDestinations != null) {
			this.mDestinations.add(aDestination);
		}
	}

	/**
	 * Removes the channel from the subscriber list of channels
	 * @param aChannel the channel id to remove.
	 */
	public void removeDestination(String aChannel) {
		if (this.mDestinations != null) {
			this.mDestinations.remove(aChannel);
		}
	}


}
