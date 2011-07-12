package org.jwebsocket.plugins.jms.infra;

import org.jwebsocket.token.Token;

public interface MessageConsumerRegistry {

	void addMessageConsumer(String aConnectionId, Token aToken);

	void removeMessageConsumer(String aConectionId);
	
	int size();
}
