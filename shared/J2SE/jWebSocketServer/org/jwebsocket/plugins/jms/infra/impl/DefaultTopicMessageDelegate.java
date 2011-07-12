package org.jwebsocket.plugins.jms.infra.impl;

import javax.jms.BytesMessage;
import javax.jms.MapMessage;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import org.jwebsocket.plugins.jms.infra.MessageConsumerRegistry;
import org.jwebsocket.plugins.jms.infra.TopicMessageDelegate;
import org.jwebsocket.token.Token;

public class DefaultTopicMessageDelegate implements TopicMessageDelegate,
		MessageConsumerRegistry {

	@Override
	public void addMessageConsumer(String aConnectionId, Token aToken) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeMessageConsumer(String aConectionId) {
		// TODO Auto-generated method stub

	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void handleMessage(TextMessage aMessage) {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleMessage(MapMessage aMessage) {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleMessage(BytesMessage aMessage) {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleMessage(ObjectMessage aMessage) {
		// TODO Auto-generated method stub

	}

}
