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
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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

	private Logger mLog = Logging.getLogger(getClass());
	private final Map<String, Token> mTokens = new ConcurrentHashMap<String, Token>();
	private final TokenPlugIn mTokenPlugin;
	private final Set<String> mConnectionIds = new FastSet<String>();
	private final Boolean mIsPubSubDomain;

	public DefaultMessageDelegate() {
		mTokenPlugin = null;
		mIsPubSubDomain = null;
	}

	public DefaultMessageDelegate(TokenPlugIn aTokenPlugin,
			boolean aIsPubSubDomain) {
		mTokenPlugin = aTokenPlugin;
		mIsPubSubDomain = aIsPubSubDomain;
	}

	@Override
	public void handleMessage(String aMessage) {
		for (String lConnectionId : mConnectionIds) {
			Token lResponseToken = mTokenPlugin.createResponse(mTokens.get(lConnectionId));
			lResponseToken.setType("event");
			lResponseToken.setString("name", "handleText");
			lResponseToken.setString("msg", (aMessage));
			lResponseToken.setInteger("code", 1);
			WebSocketConnector lConnector = getConnector(lConnectionId);
			mTokenPlugin.sendToken(lConnector, lConnector, lResponseToken);
			/**
			 * TODO optionally send a message to other waiting point-to-point
			 * consumers, signaling that the current message was sent to the
			 * consumer which was first registered for the current queue
			 */
			if (!mIsPubSubDomain) {
				break;
			}
			// if it is a pubSubDomain, then send the message to all subscribers
		}
	}

	@Override
	public void handleMessage(HashMap<String, Object> aMessage) {
		mLog.debug("handleMessage(Map message)");
		for (String lConnectionId : mConnectionIds) {
			Token lResponseToken = mTokenPlugin.createResponse(mTokens.get(lConnectionId));
			lResponseToken.setType("event");
			lResponseToken.setString("name", "handleMap");
			lResponseToken.setMap("msg", (aMessage));
			lResponseToken.setInteger("code", 1);
			WebSocketConnector lConnector = getConnector(lConnectionId);
			mTokenPlugin.sendToken(lConnector, lConnector, lResponseToken);
			/**
			 * TODO optionally send a message to other waiting point-to-point
			 * consumers, signaling that the current message was sent to the
			 * consumer which was first registered for the current queue
			 */
			if (!mIsPubSubDomain) {
				break;
			}
			// if it is a pubSubDomain, then send the message to all subscribers
		}
	}

	@Override
	public void handleMessage(byte[] aMessage) {
		mLog.debug("handleMessage(byte[] message)");

	}

	@Override
	public void handleMessage(Serializable aMessage) {
		mLog.debug("handleMessage(Serializable message)");

	}

	public WebSocketConnector getConnector(String aConnectionId) {
		WebSocketConnector lConnector = JWebSocketFactory.getTokenServer().getConnector(aConnectionId);
		return lConnector;
	}

	@Override
	public void addMessageConsumer(String aConnectionId, Token aToken) {
		if (mConnectionIds.contains(aConnectionId)) {
			return;
		}

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
