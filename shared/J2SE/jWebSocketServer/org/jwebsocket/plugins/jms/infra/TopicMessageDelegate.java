package org.jwebsocket.plugins.jms.infra;

import javax.jms.BytesMessage;
import javax.jms.MapMessage;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

public interface TopicMessageDelegate {

	void handleMessage(TextMessage aMessage);

	void handleMessage(MapMessage aMessage);
	
	void handleMessage(BytesMessage aMessage);
	
	void handleMessage(ObjectMessage aMessage);
}