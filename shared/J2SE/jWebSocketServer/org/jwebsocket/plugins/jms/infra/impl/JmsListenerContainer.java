package org.jwebsocket.plugins.jms.infra.impl;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;

import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.jms.infra.MessageConsumerRegistry;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.adapter.MessageListenerAdapter;

public class JmsListenerContainer extends DefaultMessageListenerContainer {
	private static Logger mLog = Logging.getLogger(JmsListenerContainer.class);

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
