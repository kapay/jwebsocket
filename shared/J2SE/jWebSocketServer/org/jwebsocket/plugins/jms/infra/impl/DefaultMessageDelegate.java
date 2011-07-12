package org.jwebsocket.plugins.jms.infra.impl;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.JMSException;
import javax.jms.TextMessage;

import javolution.util.FastSet;

import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.plugins.jms.infra.MessageConsumerRegistry;
import org.jwebsocket.plugins.jms.infra.MessageDelegate;
import org.jwebsocket.token.Token;

public class DefaultMessageDelegate implements MessageDelegate,
		MessageConsumerRegistry {

	private static Logger mLog = Logging
			.getLogger(DefaultMessageDelegate.class);
	
	private final Map<String, Token> mTokens = new ConcurrentHashMap<String, Token>();
	private final TokenPlugIn mTokenPlugin;
	private final Set<String> mConnectionIds = new FastSet<String>();
	private final Boolean mIsPubSubDomain;
	private volatile int mMsgCounter = 0;

	public DefaultMessageDelegate() {
		mTokenPlugin = null;
		mIsPubSubDomain = null;
	}

	public DefaultMessageDelegate(TokenPlugIn aTokenPlugin,
			boolean aIsPubSubDomain) {
		mTokenPlugin = aTokenPlugin;
		mIsPubSubDomain = aIsPubSubDomain;
	}
	
	public void handleMessage(TextMessage aMessage) throws JMSException {
		mMsgCounter++;
		mLog.debug(mMsgCounter
				+ "-------------------------------------------------------------handleMessage(String message)");
		for (String lConnectionId : mConnectionIds) {
			Token lResponseToken = mTokenPlugin.createResponse(mTokens
					.get(lConnectionId));
			lResponseToken.setType("event");
			lResponseToken.setString("name", "handleMessageString");
			lResponseToken.setString("msg", (aMessage.getText()));
			lResponseToken.setInteger("code", 1);
			WebSocketConnector lConnector = getConnector(lConnectionId);
			mTokenPlugin.sendToken(lConnector, lConnector, lResponseToken);
			/**
			 * TODO optionally send a message to other waiting point-to-point
			 * consumers, signaling that the current message was sent to the
			 * consumer which was first registered for the current queue
			 */
			if (!mIsPubSubDomain)
				break;
			// if it is a pubSubDomain, then send the message to all subscribers
		}
	}

	@Override
	public void handleMessage(String aMessage) {
		mMsgCounter++;
		mLog.debug(mMsgCounter
				+ "-------------------------------------------------------------handleMessage(String message)");
		for (String lConnectionId : mConnectionIds) {
			Token lResponseToken = mTokenPlugin.createResponse(mTokens
					.get(lConnectionId));
			lResponseToken.setType("event");
			lResponseToken.setString("name", "handleMessageString");
			lResponseToken.setString("msg", (aMessage));
			lResponseToken.setInteger("code", 1);
			WebSocketConnector lConnector = getConnector(lConnectionId);
			mTokenPlugin.sendToken(lConnector, lConnector, lResponseToken);
			/**
			 * TODO optionally send a message to other waiting point-to-point
			 * consumers, signaling that the current message was sent to the
			 * consumer which was first registered for the current queue
			 */
			if (!mIsPubSubDomain)
				break;
			// if it is a pubSubDomain, then send the message to all subscribers
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void handleMessage(Map aMessage) {
		mMsgCounter++;
		mLog.debug("handleMessage(Map message)");
	}

	@Override
	public void handleMessage(byte[] aMessage) {
		mMsgCounter++;
		mLog.debug("handleMessage(byte[] message)");

	}

	@Override
	public void handleMessage(Serializable aMessage) {
		mMsgCounter++;
		mLog.debug("handleMessage(Serializable message)");

	}

	public int getMsgCounter() {
		return mMsgCounter;
	}

	public WebSocketConnector getConnector(String aConnectionId) {
		WebSocketConnector lConnector = JWebSocketFactory.getTokenServer()
				.getConnector(aConnectionId);
		return lConnector;
	}

	@Override
	public void addMessageConsumer(String aConnectionId, Token aToken) {
		if (mConnectionIds.contains(aConnectionId))
			return;

		mConnectionIds.add(aConnectionId);
		/**
		 * TODO save the token in order to create a response token with the
		 * right response headers for each consumer in the above handelMessage
		 * Methods --> is that necessary?
		 */

		mTokens.put(aConnectionId, aToken);
	}

	@Override
	public void removeMessageConsumer(String aConectionId) {
		mConnectionIds.remove(aConectionId);
		mTokens.remove(aConectionId);
	}

	@Override
	public int size() {
		return mConnectionIds.size();
	}
	
	

}
