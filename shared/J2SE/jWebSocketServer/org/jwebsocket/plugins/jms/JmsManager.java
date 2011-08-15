//	---------------------------------------------------------------------------
//	jWebSocket
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
package org.jwebsocket.plugins.jms;

import java.util.HashMap;
import java.util.Map;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.plugins.jms.JMSPlugIn.ActionInput;
import org.jwebsocket.plugins.jms.infra.impl.DefaultMessageDelegate;
import org.jwebsocket.plugins.jms.infra.impl.JmsListenerContainer;
import org.jwebsocket.plugins.jms.util.Configuration;
import org.jwebsocket.plugins.jms.util.FieldJms;
import org.jwebsocket.token.Token;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.SimpleMessageConverter;

/**
 * 
 * @author Johannes Smutny
 */
public class JmsManager {

	/** logger */
	private Logger mLog = Logging.getLogger(getClass());

	private final Map<String, ConnectionFactory> mConnectionFactories = new HashMap<String, ConnectionFactory>();
	private final Map<String, Queue> mQueues = new HashMap<String, Queue>();
	private final Map<String, Topic> mTopics = new HashMap<String, Topic>();

	private ListenerStore mMessageListenerStore = new BaseListenerStore();
	private final SenderStore mSenderStore = new BaseSenderStore();
	private final MessageConverter mMsgConverter = new SimpleMessageConverter();

	private JmsManager() {

	}

	private void initJmsAssets(Map<String, Object> aSettings, BeanFactory aBeanFactory) {
		for (String lOption : aSettings.keySet()) {
			if (lOption.startsWith(Configuration.CF_PREFIX.getValue()))
				initConnectionFactory(aSettings, aBeanFactory, lOption);
			else if (lOption.startsWith(Configuration.DESTINATION_PREFIX.getValue()))
				initDestination(aSettings, aBeanFactory, lOption);
		}
	}

	public static JmsManager getInstance(Map<String, Object> aSettings, BeanFactory aBeanFactory) {
		JmsManager lManager = new JmsManager();
		lManager.initJmsAssets(aSettings, aBeanFactory);
		return lManager;
	}

	private void initConnectionFactory(Map<String, Object> aSettings, BeanFactory aBeanFactory, String aOption) {
		JSONObject lJSON = getJSONObject(aSettings, aOption);
		String lName = getName(lJSON);
		mConnectionFactories.put(lName, (ConnectionFactory) aBeanFactory.getBean(lName));
		if (mLog.isDebugEnabled()) {
			mLog.debug("added jms connectionFactory with bean name: '" + lName + "'");
		}
	}

	private void initDestination(Map<String, Object> aSettings, BeanFactory aBeanFactory, String aOption) {
		JSONObject lJSON = getJSONObject(aSettings, aOption);
		String name = getName(lJSON);
		String cfName = getConnectionFactoryName(lJSON);
		Boolean pubSubDomain = getPubSubDomain(lJSON);

		if (pubSubDomain)
			storeTopic(aBeanFactory, name, cfName);
		else
			storeQueue(aBeanFactory, name, cfName);
	}

	private JSONObject getJSONObject(Map<String, Object> aSettings, String aOption) {
		Object lObj = aSettings.get(aOption);
		JSONObject lJSON = null;
		if (lObj instanceof JSONObject) {
			lJSON = (JSONObject) lObj;
		} else {
			lJSON = new JSONObject();
		}
		return lJSON;
	}

	private void storeQueue(BeanFactory aBeanFactory, String aName, String aCfName) {
		mQueues.put(aName, (Queue) aBeanFactory.getBean(aName));
		if (mLog.isDebugEnabled())
			mLog.debug("added jms queue with bean name: '" + aName + "'");
	}

	private void storeTopic(BeanFactory aBeanFactory, String aName, String aCfName) {
		mTopics.put(aName, (Topic) aBeanFactory.getBean(aName));
		if (mLog.isDebugEnabled())
			mLog.debug("added jms topic with bean name: '" + aName + "'");
	}

	private String getConnectionFactoryName(JSONObject aJson) {
		try {
			return aJson.getString(Configuration.CONNECTION_FACTORY_NAME.getValue());
		} catch (Exception lEx) {
			throw new IllegalArgumentException("JMSPlugIn configuration error: missing cfName Property", lEx);
		}
	}

	private String getName(JSONObject aJson) {
		try {
			return aJson.getString(Configuration.NAME.getValue());
		} catch (Exception lEx) {
			throw new IllegalArgumentException("JMSPlugIn configuration error: missing name Property", lEx);
		}
	}

	private Boolean getPubSubDomain(JSONObject aJson) {
		try {
			return aJson.getBoolean(Configuration.PUB_SUB_DOMAIN.getValue());
		} catch (Exception lEx) {
			throw new IllegalArgumentException("JMSPlugIn configuration error: missing pubSubDomain Property", lEx);
		}
	}

	public void deregisterConnectorFromMessageListener(ActionInput aInput) {
		JmsListenerContainer lListener = mMessageListenerStore.getListener(aInput.mDi);

		if (null != lListener) {
			lListener.getMessageConsumerRegistry().removeMessageConsumer(aInput.mConnector.getId());

			if (0 == lListener.getMessageConsumerRegistry().size())
				lListener.stop();
		}
	}

	private JmsTemplate getSender(DestinationIdentifier aDestinationIdentifier) {
		JmsTemplate lSender = mSenderStore.getSender(aDestinationIdentifier);
		return null == lSender ? createSender(aDestinationIdentifier) : lSender;
	}

	private JmsTemplate createSender(DestinationIdentifier aDestinationIdentifier) {
		JmsTemplate lSender = new JmsTemplate();
		lSender.setConnectionFactory(mConnectionFactories.get(aDestinationIdentifier.getConnectionFactoryName()));
		lSender.setDefaultDestination(getDestination(aDestinationIdentifier));
		lSender.setPubSubDomain(aDestinationIdentifier.isPubSubDomain());
		mSenderStore.storeSender(aDestinationIdentifier, lSender);
		return lSender;
	}

	public void sendText(ActionInput aInput) {
		getSender(aInput.mDi).convertAndSend(aInput.mReqToken.getString(FieldJms.MESSSAGE_PAYLOAD.getValue()));
	}

	public void sendTextMessage(final ActionInput aInput) {
		JmsTemplate lSender = getSender(aInput.mDi);
		lSender.send(lSender.getDefaultDestination(), new MessageCreator() {
			public Message createMessage(Session session) throws JMSException {
				Message result = session.createTextMessage(aInput.mReqToken.getString(FieldJms.MESSSAGE_PAYLOAD.getValue()));
				try {
					enrichMessageWithHeaders(result, aInput.mReqToken.getMap(FieldJms.JMS_HEADER_PROPERTIES.getValue()));
				} catch (JSONException e) {
					mLog.error("could not enrich message with headers: " + e.getMessage());
				}
				return result;
			}
		});
	}

	public void sendMap(ActionInput aInput) {
		JmsTemplate lSender = getSender(aInput.mDi);
		if (null == lSender)
			throw new IllegalArgumentException("missing sender for destination: isPubSubdomain: "
					+ aInput.mDi.isPubSubDomain() + " name: " + aInput.mDi.getDestinationName());

		lSender.convertAndSend(aInput.mReqToken.getMap(FieldJms.MESSSAGE_PAYLOAD.getValue()));
	}

	public void sendMapMessage(final ActionInput aInput) {
		JmsTemplate lSender = getSender(aInput.mDi);
		lSender.send(lSender.getDefaultDestination(), new MessageCreator() {
			public Message createMessage(Session session) throws JMSException {
				Message result = mMsgConverter.toMessage(aInput.mReqToken.getMap(FieldJms.MESSSAGE_PAYLOAD.getValue()),
						session);
				try {
					enrichMessageWithHeaders(result, aInput.mReqToken.getMap(FieldJms.JMS_HEADER_PROPERTIES.getValue()));
				} catch (JSONException e) {
					mLog.error("could not enrich message with headers: " + e.getMessage());
				}
				return result;
			}
		});
	}

	private void enrichMessageWithHeaders(Message msg, @SuppressWarnings("rawtypes") Map jmsHeaderProperties)
			throws JMSException, JSONException {
		if (null == jmsHeaderProperties || jmsHeaderProperties.size() == 0)
			return;

		addHeader(FieldJms.JMS_HEADER_CORRELATION_ID, msg, jmsHeaderProperties);
		addHeader(FieldJms.JMS_HEADER_REPLY_TO, msg, jmsHeaderProperties);
		addHeader(FieldJms.JMS_HEADER_TYPE, msg, jmsHeaderProperties);
	}

	@SuppressWarnings("rawtypes")
	private void addHeader(FieldJms headerName, Message msg, Map jmsHeaderProperties) throws JMSException,
			JSONException {
		Object headerValue = jmsHeaderProperties.get(headerName.getValue());
		if (null == headerValue)
			return;
		switch (headerName) {
		case JMS_HEADER_CORRELATION_ID:
			if (!(headerValue instanceof String))
				return;
			msg.setJMSCorrelationID((String) headerValue);
			break;
		case JMS_HEADER_REPLY_TO:
			if (!(headerValue instanceof Map))
				return;
			msg.setJMSReplyTo(getDestination((Map) headerValue));
			break;
		case JMS_HEADER_TYPE:
			if (!(headerValue instanceof String))
				return;
			msg.setJMSType((String) headerValue);
		}
	}

	private Destination getDestination(@SuppressWarnings("rawtypes") Map headerValue) throws JSONException {
		if (null == headerValue || headerValue.size() != 3)
			return null;

		String lConnectionFactoryName = (String) headerValue.get(FieldJms.CONNECTION_FACTORY_NAME.getValue());
		String lDestinationName = (String) headerValue.get(FieldJms.DESTINATION_NAME.getValue());
		Boolean lPubSubDomain = (Boolean) headerValue.get(FieldJms.PUB_SUB_DOMAIN.getValue());
		return getDestination(DestinationIdentifier.valueOf(lConnectionFactoryName, lDestinationName, lPubSubDomain));
	}

	public void registerConnectorWithListener(ActionInput aInput, TokenPlugIn aTokenPlugIn) {
		JmsListenerContainer lListener = mMessageListenerStore.getListener(aInput.mDi);
		if (null != lListener)
			registerMessagePayloadConnector(lListener, aInput.mConnector.getId(), aInput.mReqToken);
		else
			createListener(aInput.mConnector.getId(), aInput.mReqToken, aInput.mDi, aTokenPlugIn);
	}

	public void registerConnectorWithMessageListener(ActionInput aInput, TokenPlugIn aTokenPlugIn) {
		JmsListenerContainer lListener = mMessageListenerStore.getListener(aInput.mDi);
		if (null != lListener)
			registerConnector(lListener, aInput.mConnector.getId(), aInput.mReqToken);
		else
			createMessageListener(aInput.mConnector.getId(), aInput.mReqToken, aInput.mDi, aTokenPlugIn);

	}

	private void createListener(String aConnectionId, Token aToken, DestinationIdentifier aDestinationIdentifier,
			TokenPlugIn aTokenPlugIn) {
		initializeListener(JmsListenerContainer.valueOf(createMessageDelegate(aDestinationIdentifier, aTokenPlugIn),
				mConnectionFactories.get(aDestinationIdentifier.getConnectionFactoryName()),
				getDestination(aDestinationIdentifier)), aDestinationIdentifier, aConnectionId, aToken);
	}

	private void createMessageListener(String aConnectionId, Token aToken,
			DestinationIdentifier aDestinationIdentifier, TokenPlugIn aTokenPlugIn) {
		initializeMessageListener(JmsListenerContainer.valueOf(
				createMessageDelegate(aDestinationIdentifier, aTokenPlugIn),
				mConnectionFactories.get(aDestinationIdentifier.getConnectionFactoryName()),
				getDestination(aDestinationIdentifier)), aDestinationIdentifier, aConnectionId, aToken);
	}

	private void initializeMessageListener(JmsListenerContainer aJmsListenerContainer,
			DestinationIdentifier aDestinationIdentifier, String aConnectionId, Token aToken) {
		mMessageListenerStore.storeListener(aDestinationIdentifier, aJmsListenerContainer);
		aJmsListenerContainer.getMessageConsumerRegistry().addMessageConsumer(aConnectionId, aToken);
		aJmsListenerContainer.afterPropertiesSet();
		aJmsListenerContainer.start();
	}

	private DefaultMessageDelegate createMessageDelegate(DestinationIdentifier aDestinationIdentifier,
			TokenPlugIn aTokenPlugIn) {
		return new DefaultMessageDelegate(aTokenPlugIn, aDestinationIdentifier);
	}

	private Destination getDestination(DestinationIdentifier aDestinationIdentifier) {
		Destination lDestination = aDestinationIdentifier.isPubSubDomain() ? mTopics.get(aDestinationIdentifier
				.getDestinationName()) : mQueues.get(aDestinationIdentifier.getDestinationName());
		if (null == lDestination)
			throw new IllegalArgumentException("missing destination: isPubSubdomain: "
					+ aDestinationIdentifier.isPubSubDomain() + " name: " + aDestinationIdentifier.getDestinationName());
		return lDestination;
	}

	private void initializeListener(JmsListenerContainer aJmsListenerContainer,
			DestinationIdentifier aDestinationIdentifier, String aConnectionId, Token aToken) {
		mMessageListenerStore.storeListener(aDestinationIdentifier, aJmsListenerContainer);
		aJmsListenerContainer.getMessageConsumerRegistry().addMessagePayloadConsumer(aConnectionId, aToken);
		aJmsListenerContainer.afterPropertiesSet();
		aJmsListenerContainer.start();
	}

	private void registerConnector(JmsListenerContainer aListener, String aConnectionId, Token aToken) {
		aListener.getMessageConsumerRegistry().addMessageConsumer(aConnectionId, aToken);

		if (!aListener.isRunning())
			aListener.start();
	}

	private void registerMessagePayloadConnector(JmsListenerContainer aListener, String aConnectionId, Token aToken) {
		aListener.getMessageConsumerRegistry().addMessagePayloadConsumer(aConnectionId, aToken);

		if (!aListener.isRunning())
			aListener.start();
	}

	public void stopListener(String aConnectionId) {
		for (JmsListenerContainer next : mMessageListenerStore.getAll()) {
			next.getMessageConsumerRegistry().removeMessageConsumer(aConnectionId);

			if (0 == next.getMessageConsumerRegistry().size())
				next.stop();
		}
	}

	public void shutDownListeners() {
		for (JmsListenerContainer next : mMessageListenerStore.getAll())
			next.shutdown();
	}

}
