package org.jwebsocket.plugins.jms.infra;

import java.io.Serializable;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.TextMessage;

public interface MessageDelegate {

	void handleMessage(TextMessage message) throws JMSException;

	void handleMessage(String aMessage);

	@SuppressWarnings("rawtypes")
	void handleMessage(Map aMessage);

	void handleMessage(byte[] aMessage);

	void handleMessage(Serializable aMessage);
}