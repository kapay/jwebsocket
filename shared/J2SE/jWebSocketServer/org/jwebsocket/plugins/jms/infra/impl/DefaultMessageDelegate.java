//	---------------------------------------------------------------------------
//	jWebSocket - DefaultMessageDelegate
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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.plugins.jms.infra.MessageConsumerRegistry;
import org.jwebsocket.plugins.jms.infra.MessageDelegate;
import org.jwebsocket.plugins.jms.util.EventJms;
import org.jwebsocket.plugins.jms.util.FieldJms;
import org.jwebsocket.token.Token;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.SimpleMessageConverter;

public class DefaultMessageDelegate implements MessageDelegate, MessageConsumerRegistry {

	private Logger mLog = Logging.getLogger(getClass());
	private final Map<String, MessageListenerToken> mTokens = new ConcurrentHashMap<String, MessageListenerToken>();
	private final TokenPlugIn mTokenPlugin;
	// private final Set<String> mConnectionIds = Collections.newSetFromMap(new
	// ConcurrentHashMap<String, Boolean>());
	private final Boolean mIsPubSubDomain;
	private MessageConverter mMsgConverter = new SimpleMessageConverter();

	private static class MessageListenerToken {
		private Token mToken;
		private boolean getsMessagePayloadOnly;

		private MessageListenerToken(Token token, boolean getsMessagePayloadOnly) {
			super();
			mToken = token;
			this.getsMessagePayloadOnly = getsMessagePayloadOnly;
		}

	}

	public DefaultMessageDelegate() {
		mTokenPlugin = null;
		mIsPubSubDomain = null;
	}

	public DefaultMessageDelegate(TokenPlugIn aTokenPlugin, boolean aIsPubSubDomain) {
		mTokenPlugin = aTokenPlugin;
		mIsPubSubDomain = aIsPubSubDomain;
	}

	private static String getDestinationName(Destination aDestination) throws JMSException {
		if (null == aDestination)
			return null;

		if (aDestination instanceof Queue) {
			return ((Queue) aDestination).getQueueName();
		} else if (aDestination instanceof Topic) {
			return ((Topic) aDestination).getTopicName();
		}

		return null;
	}

	private static class MessageDto {
		protected String mJmsCorrelationId;
		protected String mJmsReplyTo;
		protected String mJmsType;
		protected Boolean mOk = true;
		protected String mLog;
		protected String mJmsDestination;
		protected Integer mJmsDeliveryMode;
		protected String mJmsMessageId;
		protected Long mJmsTimestamp;
		protected Boolean mJmsRedelivered;
		protected Long mJmsExpiration;
		protected Integer mJmsPriority;

		private MessageDto(Message aMessage) {
			try {
				mJmsCorrelationId = aMessage.getJMSCorrelationID();
				mJmsReplyTo = getDestinationName(aMessage.getJMSReplyTo());
				mJmsType = aMessage.getJMSType();
				mJmsDestination = getDestinationName(aMessage.getJMSDestination());
				mJmsDeliveryMode = aMessage.getJMSDeliveryMode();
				mJmsMessageId = aMessage.getJMSMessageID();
				mJmsTimestamp = aMessage.getJMSTimestamp();
				mJmsRedelivered = aMessage.getJMSRedelivered();
				mJmsExpiration = aMessage.getJMSExpiration();
				mJmsPriority = aMessage.getJMSPriority();
			} catch (JMSException e) {
				mOk = false;
				mLog = e.getMessage();
			}
		}
	}

	private static class TextMessageDto extends MessageDto {
		private String mMsgPayLoad;

		private TextMessageDto(TextMessage aMessage) {
			super(aMessage);
			try {
				mMsgPayLoad = aMessage.getText();
			} catch (JMSException e) {
				mOk = false;
				mLog = e.getMessage();
			}
		}
	}

	@SuppressWarnings({ "rawtypes" })
	private class MapMessageDto extends MessageDto {
		private Map mMsgPayLoad;

		private MapMessageDto(MapMessage aMessage) {
			super(aMessage);
			try {
				mMsgPayLoad = (Map) mMsgConverter.fromMessage(aMessage);
			} catch (JMSException e) {
				mOk = false;
				mLog = e.getMessage();
			}
		}
	}

	private String getTextMessagePayload(TextMessage aTextMessage) {
		try {
			String ret = aTextMessage.getText();
			return (null == ret) ? "" : ret;
		} catch (JMSException e) {
			return null;
		}
	}

	@Override
	public void handleMessage(TextMessage aMessage) {
		for (String lConnectionId : mTokens.keySet()) {
			MessageListenerToken lmToken = mTokens.get(lConnectionId);
			if (null == lmToken || null == lmToken.mToken)
				continue;

			Token lResponseToken = mTokenPlugin.createResponse(lmToken.mToken);
			if (lmToken.getsMessagePayloadOnly) {
				String payLoad = getTextMessagePayload(aMessage);
				if (null == payLoad) {
					lResponseToken.setInteger(FieldJms.CODE.getValue(), -1);
					lResponseToken.setString(FieldJms.MSG.getValue(), "Could not get payload of TextMessage");
				} else {
					lResponseToken.setType(FieldJms.EVENT.getValue());
					lResponseToken.setString(FieldJms.NAME.getValue(), EventJms.HANDLE_TEXT.getValue());
					lResponseToken.setInteger(FieldJms.CODE.getValue(), 1);
					lResponseToken.setString(FieldJms.MESSSAGE_PAYLOAD.getValue(), payLoad);
				}
			} else {
				TextMessageDto dto = new TextMessageDto(aMessage);
				if (!dto.mOk) {
					lResponseToken.setInteger(FieldJms.CODE.getValue(), -1);
					lResponseToken.setString(FieldJms.MSG.getValue(), "could not get payload of TextMessage: "
							+ dto.mLog);
				} else {
					lResponseToken.setType(FieldJms.EVENT.getValue());
					lResponseToken.setString(FieldJms.NAME.getValue(), EventJms.HANDLE_TEXT_MESSAGE.getValue());
					lResponseToken.setInteger(FieldJms.CODE.getValue(), 1);
					lResponseToken.setString(FieldJms.MESSSAGE_PAYLOAD.getValue(), dto.mMsgPayLoad);
					lResponseToken.setMap(FieldJms.JMS_HEADER_PROPERTIES.getValue(), getMessageHeaders(dto));
					/**
					 * TODO optionally send a message to other waiting
					 * point-to-point consumers, signaling that the current
					 * message was sent to the consumer which was first
					 * registered for the current queue
					 */
				}

			}
			WebSocketConnector lConnector = getConnector(lConnectionId);
			mTokenPlugin.sendToken(lConnector, lConnector, lResponseToken);

			if (!mIsPubSubDomain)
				break;
			// if it is a pubSubDomain, then send the message to all subscribers
		}
	}

	private Map<String, Object> getMessageHeaders(MessageDto dto) {
		Map<String, Object> lHeaders = new HashMap<String, Object>();
		lHeaders.put(FieldJms.JMS_HEADER_CORRELATION_ID.getValue(), dto.mJmsCorrelationId);
		lHeaders.put(FieldJms.JMS_HEADER_REPLY_TO.getValue(), dto.mJmsReplyTo);
		lHeaders.put(FieldJms.JMS_HEADER_TYPE.getValue(), dto.mJmsType);
		lHeaders.put(FieldJms.JMS_HEADER_DESTINATION.getValue(), dto.mJmsDestination);
		lHeaders.put(FieldJms.JMS_HEADER_DELIVERY_MODE.getValue(), dto.mJmsDeliveryMode);
		lHeaders.put(FieldJms.JMS_HEADER_EXPIRATION.getValue(), dto.mJmsExpiration);
		lHeaders.put(FieldJms.JMS_HEADER_MESSAGE_ID.getValue(), dto.mJmsMessageId);
		lHeaders.put(FieldJms.JMS_HEADER_PRIORITY.getValue(), dto.mJmsPriority);
		lHeaders.put(FieldJms.JMS_HEADER_REDELIVERED.getValue(), dto.mJmsRedelivered);
		lHeaders.put(FieldJms.JMS_HEADER_TIMESTAMP.getValue(), dto.mJmsTimestamp);
		return lHeaders;
	}

	@SuppressWarnings("rawtypes")
	private Map getMapMessagePayload(MapMessage aMapMessage) {
		try {
			Map ret = (Map) mMsgConverter.fromMessage(aMapMessage);
			return (null == ret) ? new HashMap() : ret;
		} catch (JMSException e) {
			return null;
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void handleMessage(MapMessage aMessage) {
		for (String lConnectionId : mTokens.keySet()) {
			MessageListenerToken lmToken = mTokens.get(lConnectionId);
			if (null == lmToken || null == lmToken.mToken)
				continue;

			Token lResponseToken = mTokenPlugin.createResponse(lmToken.mToken);
			if (lmToken.getsMessagePayloadOnly) {
				Map payLoad = getMapMessagePayload(aMessage);
				if (null == payLoad) {
					lResponseToken.setInteger(FieldJms.CODE.getValue(), -1);
					lResponseToken.setString(FieldJms.MSG.getValue(), "Could not get payload of MapMessage");
				} else {
					lResponseToken.setType(FieldJms.EVENT.getValue());
					lResponseToken.setString(FieldJms.NAME.getValue(), EventJms.HANDLE_TEXT.getValue());
					lResponseToken.setInteger(FieldJms.CODE.getValue(), 1);
					lResponseToken.setMap(FieldJms.MESSSAGE_PAYLOAD.getValue(), payLoad);
				}
			} else {
				MapMessageDto dto = new MapMessageDto(aMessage);
				if (!dto.mOk) {
					lResponseToken.setInteger(FieldJms.CODE.getValue(), -1);
					lResponseToken.setString(FieldJms.MSG.getValue(), "could not get contents of MapMessage: "
							+ dto.mLog);
				} else {
					lResponseToken.setType(FieldJms.EVENT.getValue());
					lResponseToken.setString(FieldJms.NAME.getValue(), EventJms.HANDLE_MAP_MESSAGE.getValue());
					lResponseToken.setInteger(FieldJms.CODE.getValue(), 1);
					lResponseToken.setMap(FieldJms.MESSSAGE_PAYLOAD.getValue(), dto.mMsgPayLoad);
					lResponseToken.setMap(FieldJms.JMS_HEADER_PROPERTIES.getValue(), getMessageHeaders(dto));
					/**
					 * TODO optionally send a message to other waiting
					 * point-to-point consumers, signaling that the current
					 * message was sent to the consumer which was first
					 * registered for the current queue
					 */
				}
			}
			WebSocketConnector lConnector = getConnector(lConnectionId);
			mTokenPlugin.sendToken(lConnector, lConnector, lResponseToken);

			if (!mIsPubSubDomain)
				break;
			// if it is a pubSubDomain, then send the message to all subscribers
		}

	}

	public WebSocketConnector getConnector(String aConnectionId) {
		WebSocketConnector lConnector = JWebSocketFactory.getTokenServer().getConnector(aConnectionId);
		return lConnector;
	}

	@Override
	public void addMessageConsumer(String aConnectionId, Token aToken) {
		// if (mConnectionIds.contains(aConnectionId)) {
		// MessageListenerToken lToken =
		// }

		// mConnectionIds.add(aConnectionId);
		/**
		 * TODO save the token in order to create a response token with the
		 * right response headers for each consumer in the above handelMessage
		 * Methods --> is that necessary?
		 */
		mTokens.put(aConnectionId, new MessageListenerToken(aToken, false));
	}

	@Override
	public void addMessagePayloadConsumer(String aConnectionId, Token aToken) {
		// mConnectionIds.add(aConnectionId);
		/**
		 * TODO save the token in order to create a response token with the
		 * right response headers for each consumer in the above handelMessage
		 * Methods --> is that necessary?
		 */
		mTokens.put(aConnectionId, new MessageListenerToken(aToken, true));
	}

	@Override
	public void removeMessageConsumer(String aConectionId) {
		// mConnectionIds.remove(aConectionId);
		mTokens.remove(aConectionId);
	}

	@Override
	public int size() {
		return mTokens.size();
	}
}
