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
import org.jwebsocket.token.Token;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.jms.core.JmsTemplate;

/**
 * 
 * @author Johannes Smutny
 */
public class JMSManager {

	/** logger */
	private static Logger mLog = Logging.getLogger(JMSManager.class);
	private static final String SETTINGS_CF_PREFIX = "connectionFactory:";
	private static final String SETTINGS_QUEUE_PREFIX = "queue:";
	private static final String SETTINGS_TOPIC_PREFIX = "topic:";

	private final Map<String, ConnectionFactory> mConnectionFactories = new HashMap<String, ConnectionFactory>();
	private final Map<String, Queue> mQueues = new HashMap<String, Queue>();
	private final Map<String, Topic> mTopics = new HashMap<String, Topic>();

	private final ListenerStore mListenerStore = new BaseListenerStore();
	private final SenderStore mSenderStore = new BaseSenderStore();
	private final ConsumerStore mConsumerStore = new BaseConsumerStore();
	private final ProducerStore mProducerStore = new BaseProducerStore();
	private Map<String, TopicSettings> mTopicSettings = new FastMap<String, JMSManager.TopicSettings>();
	
	private static class TopicSettings {
		
		private String mTopicName;
		private boolean mIsSingleThreaded;
		
		public TopicSettings(String aTopicName, boolean aISingleThreaded) {
			mTopicName = aTopicName;
			mIsSingleThreaded = aISingleThreaded;
		}

		public String getTopicName() {
			return mTopicName;
		}

		public boolean isIsSingleThreaded() {
			return mIsSingleThreaded;
		}
		
		
	}

	private JMSManager(Map<String, Object> aSettings, BeanFactory aBeanFactory) {
		int lCfSuccess = 0;
		int lQueueSuccess = 0;
		int lTopicSuccess = 0;
		for (String lOption : aSettings.keySet()) {
			if (lOption.startsWith(SETTINGS_CF_PREFIX)) {
				String lcfBeanName = (String) aSettings.get(lOption);
				mConnectionFactories.put(lcfBeanName,
						(ConnectionFactory) aBeanFactory.getBean(lcfBeanName));

				if (mLog.isDebugEnabled()) {
					mLog.debug("added jms connectionFactory with bean name: '"
							+ lcfBeanName + "'");
				}

				lCfSuccess++;
			} else if (lOption.startsWith(SETTINGS_QUEUE_PREFIX)) {
				String lQueueBeanName = (String) aSettings.get(lOption);
				mQueues.put(lQueueBeanName,
						(Queue) aBeanFactory.getBean(lQueueBeanName));

				if (mLog.isDebugEnabled()) {
					mLog.debug("added jms queue with bean name: '"
							+ lQueueBeanName + "'");
				}

				lQueueSuccess++;
			} else if (lOption.startsWith(SETTINGS_TOPIC_PREFIX)) {
				initTopic(aSettings, aBeanFactory, lOption);
				lTopicSuccess++;
			}
		}

		if (mLog.isDebugEnabled()) {
			mLog.debug(lCfSuccess
					+ " jms connectionFactories successfully instantiated."
					+ lQueueSuccess + " jms queues successfully instantiated."
					+ lTopicSuccess + " jms topics successfully instantiated.");
		}
	}
	
	private void initTopic(Map<String, Object> aSettings, BeanFactory aBeanFactory,String aOption) {
		Object lObj = aSettings.get(aOption);
		JSONObject lJSON = null;
		if (lObj instanceof JSONObject) {
			lJSON = (JSONObject) lObj;
		} else {
			lJSON = new JSONObject();
		}

		String lTopicBeanName = null;
		boolean lSingleThreaded = true;
		try {
			lTopicBeanName = lJSON.getString("name");
		} catch (Exception lEx) {
		}
		try {
			lSingleThreaded = lJSON.getBoolean("singleThreadedListener");
		} catch (Exception lEx) {
		}
		
		mTopics.put(lTopicBeanName,
				(Topic) aBeanFactory.getBean(lTopicBeanName));
		mTopicSettings.put(lTopicBeanName, new TopicSettings(lTopicBeanName, lSingleThreaded));
		if (mLog.isDebugEnabled()) {
			mLog.debug("added jms topic with bean name: '"
					+ lTopicBeanName + "'");
		}

	}

	/**
	 * @param aSettings
	 * @param beanFactory
	 * @return the static manager instance
	 */
	public static JMSManager getJMSManager(Map<String, Object> aSettings,
			BeanFactory beanFactory) {
		return new JMSManager(aSettings, beanFactory);
	}

	public void deregisterConsumer(String aConnectionId,
			DestinationIdentifier aDestinationIdentifier) {
		JmsListenerContainer lListener = mListenerStore
				.getListener(aDestinationIdentifier);

		if (null != lListener)
			lListener.getMessageConsumerRegistry().removeMessageConsumer(
					aConnectionId);

		if (0 == lListener.getMessageConsumerRegistry().size())
			lListener.stop();

		Consumer lConsumer = mConsumerStore.getConsumer(aConnectionId);
		if (null == lConsumer)
			return;

		lConsumer.removeListenerContainer(lListener);

		if (lConsumer.getListenerContainers().size() == 0)
			mConsumerStore.removeConsumer(aConnectionId);
		else
			mConsumerStore.storeConsumer(lConsumer);
	}

	public void registerProducer(String aConnectionId,
			DestinationIdentifier aDestinationIdentifier) {
		JmsTemplate lSender = mSenderStore.getSender(aDestinationIdentifier);
		if (null == lSender) {
			lSender = new JmsTemplate();
			lSender.setConnectionFactory(mConnectionFactories
					.get(aDestinationIdentifier.getConnectionFactoryName()));
			lSender.setDefaultDestination(aDestinationIdentifier
					.isPubSubDomain() ? mTopics.get(aDestinationIdentifier
					.getDestinationName()) : mQueues.get(aDestinationIdentifier
					.getDestinationName()));
			lSender.setPubSubDomain(aDestinationIdentifier.isPubSubDomain());
			mSenderStore.storeSender(aDestinationIdentifier, lSender);
		}

		Producer lProducer = mProducerStore.getProducer(aConnectionId);
		if (null == lProducer)
			lProducer = new Producer(aConnectionId);

		lProducer.addDestination(lSender);
		mProducerStore.storeProducer(lProducer);
	}

	public void sendStringMessage(DestinationIdentifier aDestinationIdentifier,
			String aStringMessage) {
		JmsTemplate lSender = mSenderStore.getSender(aDestinationIdentifier);
		lSender.convertAndSend(aStringMessage);
	}

	public void registerConsumer(String aConnectionId, Token aToken,
			DestinationIdentifier aDestinationIdentifier,
			TokenPlugIn aTokenPlugIn) {
		JmsListenerContainer lListener = mListenerStore
				.getListener(aDestinationIdentifier);
		if (null != lListener) {
			lListener.getMessageConsumerRegistry().addMessageConsumer(
					aConnectionId, aToken);

			if (!lListener.isRunning())
				lListener.start();
		} else {
			DefaultMessageDelegate lDelegate = new DefaultMessageDelegate(
					aTokenPlugIn, aDestinationIdentifier.isPubSubDomain());
			Destination lDestination = aDestinationIdentifier.isPubSubDomain() ? mTopics
					.get(aDestinationIdentifier.getDestinationName()) : mQueues
					.get(aDestinationIdentifier.getDestinationName());
			lListener = JmsListenerContainer.valueOf(lDelegate,
					mConnectionFactories.get(aDestinationIdentifier
							.getConnectionFactoryName()), lDestination);
			mListenerStore.storeListener(aDestinationIdentifier, lListener);
			lListener.getMessageConsumerRegistry().addMessageConsumer(
					aConnectionId, aToken);
			if (aDestinationIdentifier.isPubSubDomain()) {
				lListener.setConcurrency("1");
				lListener.setMaxConcurrentConsumers(1);
			} else {
				lListener.setConcurrency("5-10");
			}
			lListener.afterPropertiesSet();
			lListener.start();
		}

		Consumer lConsumer = mConsumerStore.getConsumer(aConnectionId);
		if (null == lConsumer)
			lConsumer = new Consumer(aConnectionId);

		lConsumer.addListenerContainer(lListener);
		mConsumerStore.storeConsumer(lConsumer);
	}

	/**
	 * Returns the registered consumer object for the given consumer id
	 * 
	 * @param aConsumerId
	 *            the consumer id
	 * @return the consumer object
	 */
	public Consumer getConsumer(String aConsumerId) {
		return mConsumerStore.getConsumer(aConsumerId);
	}

	/**
	 * Removes the given consumer information from destination store
	 * 
	 * @param aconsumer
	 *            the consumer object
	 */
	public void removeConsumer(Consumer aConsumer) {
		mConsumerStore.removeConsumer(aConsumer.getId());
	}

	/**
	 * Removes the given Producer information from destination store
	 * 
	 * @param aProducer
	 *            the Producer object
	 */
	public void removeProducer(Producer aProducer) {
		mProducerStore.removeProducer(aProducer.getId());
	}

	/**
	 * Returns the registered producer object for the given producer id
	 * 
	 * @param aProducerId
	 *            the producer id
	 * @return the producer object
	 */
	public Producer getProducer(String aProducerId) {
		return mProducerStore.getProducer(aProducerId);
	}

	public Object getConnection() {
		// TODO Auto-generated method stub
		return null;
	}

}
