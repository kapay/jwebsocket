//	---------------------------------------------------------------------------
//	jWebSocket - JmsListenerContainer
//	Copyright (c) 2011, Innotrade GmbH - jWebSocket.org, Alexander Schulze
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
package org.jwebsocket.plugins.jms.infra.impl;

/**
 * 
 * @author Johannes Smutny
 */
import javax.jms.ConnectionFactory;
import javax.jms.Destination;

import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.jms.infra.MessageConsumerRegistry;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.adapter.MessageListenerAdapter;

public class JmsListenerContainer extends DefaultMessageListenerContainer {

	private Logger mLog = Logging.getLogger(getClass());
	private MessageConsumerRegistry mMessageConsumerRegistry;

	private JmsListenerContainer(DefaultMessageDelegate aMessageDelegate,
			ConnectionFactory aConnectionFactory, Destination aDestination) {
		super();
		mMessageConsumerRegistry = aMessageDelegate;
		MessageListenerAdapter lListener = new MessageListenerAdapter(
				aMessageDelegate);
		setMessageListener(lListener);
		setConnectionFactory(aConnectionFactory);
		setDestination(aDestination);
	}

	public static JmsListenerContainer valueOf(
			DefaultMessageDelegate aMessageDelegate,
			ConnectionFactory aConnectionFactory, Destination aDestination) {
		JmsListenerContainer result = new JmsListenerContainer(
				aMessageDelegate, aConnectionFactory, aDestination);
		return result;
	}

	public MessageConsumerRegistry getMessageConsumerRegistry() {
		return mMessageConsumerRegistry;
	}

	public DefaultMessageDelegate getMessageDelegate() {
		return (DefaultMessageDelegate) mMessageConsumerRegistry;
	}
}
