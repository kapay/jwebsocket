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
import javax.jms.Queue;
import javax.jms.Topic;

import javolution.util.FastMap;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.plugins.jms.infra.impl.DefaultMessageDelegate;
import org.jwebsocket.plugins.jms.infra.impl.JmsListenerContainer;
import org.jwebsocket.plugins.jms.util.Configuration;
import org.jwebsocket.token.Token;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.jms.core.JmsTemplate;

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

	private final ListenerStore mListenerStore = new BaseListenerStore();
	private final SenderStore mSenderStore = new BaseSenderStore();
	private Map<DestinationIdentifier, QueueSettings> mQueueSettings = new FastMap<DestinationIdentifier, JmsManager.QueueSettings>();

	private static class QueueSettings {
		private String mListenerConcurrency;

		public QueueSettings(String aListenerConcurrency) {
			setListenerConcurrency(aListenerConcurrency);
		}

		public void setListenerConcurrency(String listenerConcurrency) {
			mListenerConcurrency = listenerConcurrency;
		}

		public String getListenerConcurrency() {
			return mListenerConcurrency;
		}

	}

	private JmsManager() {

	}

	public void initJmsAssets(Map<String, Object> aSettings,
			BeanFactory aBeanFactory) {
		for (String lOption : aSettings.keySet()) {
			if (lOption.startsWith(Configuration.CF_PREFIX.getValue()))
				initConnectionFactory(aSettings, aBeanFactory, lOption);
			else if (lOption.startsWith(Configuration.QUEUE_PREFIX.getValue()))
				initQueue(aSettings, aBeanFactory, lOption);
			else if (lOption.startsWith(Configuration.TOPIC_PREFIX.getValue()))
				initTopic(aSettings, aBeanFactory, lOption);
		}
	}

	public static JmsManager getInstance(Map<String, Object> aSettings,
			BeanFactory aBeanFactory) {
		JmsManager lManager = new JmsManager();
		lManager.initJmsAssets(aSettings, aBeanFactory);
		return lManager;
	}

	private void initConnectionFactory(Map<String, Object> aSettings,
			BeanFactory aBeanFactory, String aOption) {
		Object lObj = aSettings.get(aOption);
		JSONObject lJSON = null;
		if (lObj instanceof JSONObject) {
			lJSON = (JSONObject) lObj;
		} else {
			lJSON = new JSONObject();
		}

		String lName = null;
		try {
			lName = lJSON.getString(Configuration.NAME.getValue());
		} catch (Exception lEx) {
		}

		mConnectionFactories.put(lName,
				(ConnectionFactory) aBeanFactory.getBean(lName));

		if (mLog.isDebugEnabled()) {
			mLog.debug("added jms connectionFactory with bean name: '" + lName
					+ "'");
		}
	}

	private void initQueue(Map<String, Object> aSettings,
			BeanFactory aBeanFactory, String aOption) {
		JSONObject lJSON = getJSONObject(aSettings, aOption);
		storeQueue(aBeanFactory, getName(lJSON),
				getConnectionFactoryName(lJSON), getlistenerConcurrency(lJSON));
	}

	private void storeQueue(BeanFactory aBeanFactory, String aName,
			String aCfName, String aListenerConcurrency) {
		mQueues.put(aName, (Queue) aBeanFactory.getBean(aName));
		mQueueSettings.put(
				DestinationIdentifier.valueOf(aCfName, aName, false),
				new QueueSettings(aListenerConcurrency));
		if (mLog.isDebugEnabled())
			mLog.debug("added jms queue with bean name: '" + aName + "'");
	}

	private void initTopic(Map<String, Object> aSettings,
			BeanFactory aBeanFactory, String aOption) {
		JSONObject lJSON = getJSONObject(aSettings, aOption);
		storeTopic(aBeanFactory, getName(lJSON),
				getConnectionFactoryName(lJSON));
	}

	private void storeTopic(BeanFactory aBeanFactory, String aName,
			String aCfName) {
		mTopics.put(aName, (Topic) aBeanFactory.getBean(aName));
		if (mLog.isDebugEnabled())
			mLog.debug("added jms topic with bean name: '" + aName + "'");
	}

	private String getlistenerConcurrency(JSONObject aJson) {
		try {
			return aJson.getString(Configuration.LISTENER_CONCURRENCY
					.getValue());
		} catch (Exception lEx) {
			mLog.error("could not get listener concurrency");
			return null;
		}
	}

	private String getConnectionFactoryName(JSONObject aJson) {
		try {
			return aJson.getString(Configuration.CONNECTION_FACTORY_NAME
					.getValue());
		} catch (Exception lEx) {
			mLog.error("could not get connection factory name");
			return null;
		}
	}

	private String getName(JSONObject aJson) {
		try {
			return aJson.getString(Configuration.NAME.getValue());
		} catch (Exception lEx) {
			mLog.error("could not get name");
			return null;
		}
	}

	private JSONObject getJSONObject(Map<String, Object> aSettings,
			String aOption) {
		Object lObj = aSettings.get(aOption);
		JSONObject lJSON = null;
		if (lObj instanceof JSONObject) {
			lJSON = (JSONObject) lObj;
		} else {
			lJSON = new JSONObject();
		}
		return lJSON;
	}

	public void deregisterConnectorFromListener(String aConnectionId,
			DestinationIdentifier aDestinationIdentifier) {
		JmsListenerContainer lListener = mListenerStore
				.getListener(aDestinationIdentifier);

		if (null != lListener)
			lListener.getMessageConsumerRegistry().removeMessageConsumer(
					aConnectionId);

		if (0 == lListener.getMessageConsumerRegistry().size())
			lListener.stop();
	}

	private JmsTemplate getSender(DestinationIdentifier aDestinationIdentifier) {
		JmsTemplate lSender = mSenderStore.getSender(aDestinationIdentifier);
		return null == lSender ? createSender(aDestinationIdentifier) : lSender;
	}

	private JmsTemplate createSender(
			DestinationIdentifier aDestinationIdentifier) {
		JmsTemplate lSender = new JmsTemplate();
		lSender.setConnectionFactory(mConnectionFactories
				.get(aDestinationIdentifier.getConnectionFactoryName()));
		lSender.setDefaultDestination(getDestination(aDestinationIdentifier));
		lSender.setPubSubDomain(aDestinationIdentifier.isPubSubDomain());
		mSenderStore.storeSender(aDestinationIdentifier, lSender);
		return lSender;
	}

	public void sendText(DestinationIdentifier aDestinationIdentifier,
			String aStringMessage) {
		JmsTemplate lSender = getSender(aDestinationIdentifier);

		lSender.convertAndSend(aStringMessage);
	}

	@SuppressWarnings("rawtypes")
	public void sendMap(DestinationIdentifier aDestinationIdentifier, Map aMap) {
		JmsTemplate lSender = getSender(aDestinationIdentifier);
		if (null == lSender)
			throw new IllegalArgumentException(
					"missing sender for destination: isPubSubdomain: "
							+ aDestinationIdentifier.isPubSubDomain()
							+ " name: "
							+ aDestinationIdentifier.getDestinationName());

		lSender.convertAndSend((Map) aMap);
	}

	public void registerConnectorWithListener(String aConnectionId,
			Token aToken, DestinationIdentifier aDestinationIdentifier,
			TokenPlugIn aTokenPlugIn) {
		JmsListenerContainer lListener = mListenerStore
				.getListener(aDestinationIdentifier);
		if (null != lListener) {
			registerConnector(lListener, aConnectionId, aToken);
		} else {
			createListener(aConnectionId, aToken, aDestinationIdentifier,
					aTokenPlugIn);
		}
	}

	private void createListener(String aConnectionId, Token aToken,
			DestinationIdentifier aDestinationIdentifier,
			TokenPlugIn aTokenPlugIn) {
		initializeListener(JmsListenerContainer.valueOf(
				createMessageDelegate(aDestinationIdentifier, aTokenPlugIn),
				mConnectionFactories.get(aDestinationIdentifier
						.getConnectionFactoryName()),
				getDestination(aDestinationIdentifier)),
				aDestinationIdentifier, aConnectionId, aToken);
	}

	private DefaultMessageDelegate createMessageDelegate(
			DestinationIdentifier aDestinationIdentifier,
			TokenPlugIn aTokenPlugIn) {
		return new DefaultMessageDelegate(aTokenPlugIn,
				aDestinationIdentifier.isPubSubDomain());
	}

	private Destination getDestination(
			DestinationIdentifier aDestinationIdentifier) {
		Destination lDestination = aDestinationIdentifier.isPubSubDomain() ? mTopics
				.get(aDestinationIdentifier.getDestinationName()) : mQueues
				.get(aDestinationIdentifier.getDestinationName());
		if (null == lDestination)
			throw new IllegalArgumentException(
					"missing destination: isPubSubdomain: "
							+ aDestinationIdentifier.isPubSubDomain()
							+ " name: "
							+ aDestinationIdentifier.getDestinationName());
		return lDestination;
	}

	private void initializeListener(JmsListenerContainer aJmsListenerContainer,
			DestinationIdentifier aDestinationIdentifier, String aConnectionId,
			Token aToken) {
		mListenerStore.storeListener(aDestinationIdentifier,
				aJmsListenerContainer);
		aJmsListenerContainer.getMessageConsumerRegistry().addMessageConsumer(
				aConnectionId, aToken);
		initDestinationListener(aJmsListenerContainer, aDestinationIdentifier);
		aJmsListenerContainer.afterPropertiesSet();
		aJmsListenerContainer.start();
	}

	private void initDestinationListener(
			JmsListenerContainer aJmsListenerContainer,
			DestinationIdentifier aDestinationIdentifier) {
		if (aDestinationIdentifier.isPubSubDomain())
			initTopicListener(aJmsListenerContainer, aDestinationIdentifier);
		else
			initQueueListener(aJmsListenerContainer, aDestinationIdentifier);
	}

	private void initQueueListener(JmsListenerContainer aJmsListenerContainer,
			DestinationIdentifier aDestinationIdentifier) {
		QueueSettings lSettings = mQueueSettings.get(aDestinationIdentifier);
		aJmsListenerContainer
				.setConcurrency(lSettings.getListenerConcurrency());
	}

	private void initTopicListener(JmsListenerContainer aJmsListenerContainer,
			DestinationIdentifier aDestinationIdentifier) {
		// multithreaded topic listeners are avoided, otherwise each consumer
		// would receive the same message as often as many threads are
		// configured
		// Note: this is not the case with queues
		aJmsListenerContainer.setMaxConcurrentConsumers(1);
	}

	private void registerConnector(JmsListenerContainer aListener,
			String aConnectionId, Token aToken) {
		aListener.getMessageConsumerRegistry().addMessageConsumer(
				aConnectionId, aToken);

		if (!aListener.isRunning())
			aListener.start();
	}

	public void stopListeners(String aConnectionId) {
		for (JmsListenerContainer next : mListenerStore.getAll())
			next.getMessageConsumerRegistry().removeMessageConsumer(
					aConnectionId);
	}

}
