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

/**
 * 
 * @author Johannes Smutny
 */
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.TextMessage;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.security.SecurityFactory;
import org.jwebsocket.token.Token;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jms.listener.SimpleMessageListenerContainer;

/**
 * 
 * @author jsmutny
 */
public class JMSPlugIn extends TokenPlugIn {

	private static Logger mLog = Logging.getLogger(JMSPlugIn.class);
	private static final String NS_JMS = JWebSocketServerConstants.NS_BASE
			+ ".plugins.jms";
	private static final String CREATE_CONNECTION = "createConnection";
	private static final String CREATE_SESSION = "createSession";
	private static final String TRANSACTED = "transacted";
	private static final String ACKNOWLEDGEMODE = "acknowledgeMode";
	private static final String CREATE_QUEUE = "createQueue";
	private static final String QUEUE_NAME = "queueName";
	private static final String CREATE_CONSUMER = "createConsumer";
	private static final String DESTINATION_NAME = "destinationName";
	private static BeanFactory mBeanFactory;
	private static ConnectionFactory mConnectionFactory;
	private JwsJmsTemplate mJmsTemplate;
	private SimpleMessageListenerContainer mMessageListenerContainer;

	public JMSPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating JMS plug-in...");
		}
		// specify default name space for admin plugin
		this.setNamespace(NS_JMS);

		String lVal = getString("conn_val_timeout");

		try {
			String lSpringConfig = getString("spring_config");
			String lPath = FilenameUtils.getPath(lSpringConfig);
			if (lPath == null || lPath.length() <= 0) {
				lPath = JWebSocketConfig.getConfigFolder(lSpringConfig);
			} else {
				lPath = lSpringConfig;
			}
			// mLog.debug(new ActiveMQBlobMessage());
			FileSystemResource lFSRes = new FileSystemResource(lPath);
			mBeanFactory = new XmlBeanFactory(lFSRes);
			mConnectionFactory = (ConnectionFactory) mBeanFactory.getBean("connectionFactory");
			if (mLog.isDebugEnabled()) {
				mLog.debug("the jms connection factory: " + mConnectionFactory);
			}

			/*
			 * JMSStockMarketDataProducer producer = mBeanFactory
			 * .getBean(JMSStockMarketDataProducer.class);
			 * SimpleMessageListenerContainer stockMarketListenerContainer =
			 * mBeanFactory .getBean(SimpleMessageListenerContainer.class);
			 * stockMarketListenerContainer.start();
			 * producer.sendStockMarketData();
			 */
		} catch (Exception lEx) {
			mLog.error(lEx.getClass().getSimpleName() + " instantiation: "
					+ lEx.getMessage());
		}

	}

	@Override
	public void processToken(PlugInResponse aResponse,
			WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();
		if (lType != null && getNamespace().equals(lNS)) {
			if (CREATE_CONNECTION.equals(lType)) {
				createConnection(aConnector, aToken);
			} else if (CREATE_SESSION.equals(lType)) {
				createSession(aConnector, aToken);
			} else if (CREATE_QUEUE.equals(lType)) {
				createQueue(aConnector, aToken);
			} else if (CREATE_CONSUMER.equals(lType)) {
				createConsumer(aConnector, aToken);
			} else {
				// ignore
			}
		}
	}

	private void createConnection(WebSocketConnector aConnector, Token aToken) {
		mLog.debug("Processing 'createConnection'...");

		// check if user is allowed to create a connection
		if (!SecurityFactory.hasRight(getUsername(aConnector), NS_JMS
				+ ".createConnection")) {
			sendToken(aConnector, aConnector, createAccessDenied(aToken));
			return;
		}

		Token lResponseToken = createResponse(aToken);

		mLog.debug("creating Connection for user");
		mJmsTemplate = new JwsJmsTemplate();
		mJmsTemplate.setConnectionFactory(mConnectionFactory);
		try {
			mJmsTemplate.createConnection();
		} catch (JMSException e) {
			e.printStackTrace();
			lResponseToken.setInteger("code", -1);
			lResponseToken.setString(
					"msg",
					"Connection to JMS broker could not be established: "
					+ e.getMessage());
		}

		lResponseToken.setInteger("code", 1);
		lResponseToken.setString("msg",
				"Successfully established connection to JMS broker.");
		// send the response
		sendToken(aConnector, aConnector, lResponseToken);
	}

	private void createSession(WebSocketConnector aConnector, Token aToken) {
		mLog.debug("Processing 'createSession'...");

		// check if user is allowed to create a session
		if (!SecurityFactory.hasRight(getUsername(aConnector), NS_JMS
				+ ".createSession")) {
			sendToken(aConnector, aConnector, createAccessDenied(aToken));
			return;
		}
		Token lResponseToken = createResponse(aToken);
		mLog.debug("creating Session for user");
		if (null == mJmsTemplate.getConnection()) {
			lResponseToken.setInteger("code", -1);
			lResponseToken.setString("msg",
					"Session could not be created, because a connection is missing");
		} else {
			Boolean lTransacted = aToken.getBoolean(TRANSACTED);
			Integer lAcknowledgeMode = aToken.getInteger(ACKNOWLEDGEMODE);
			if (null != lTransacted) {
				mJmsTemplate.setSessionTransacted(lTransacted);
			}
			if (null != lAcknowledgeMode) {
				mJmsTemplate.setSessionAcknowledgeMode(lAcknowledgeMode);
			}
			try {
				mJmsTemplate.createSession();
				lResponseToken.setInteger("code", 1);
				lResponseToken.setString("msg", "Successfully created session");
			} catch (JMSException e) {
				e.printStackTrace();
				lResponseToken.setInteger("code", -1);
				lResponseToken.setString("msg",
						"Session could not be created: " + e.getMessage());
			}
		}
		// send the response
		sendToken(aConnector, aConnector, lResponseToken);
	}

	private void createQueue(WebSocketConnector aConnector, Token aToken) {
		mLog.debug("Processing 'createQueue'...");

		// check if user is allowed to create a queue
		if (!SecurityFactory.hasRight(getUsername(aConnector), NS_JMS
				+ ".createQueue")) {
			sendToken(aConnector, aConnector, createAccessDenied(aToken));
			return;
		}
		Token lResponseToken = createResponse(aToken);
		mLog.debug("creating Session for user");
		if (null == mJmsTemplate.getSession()) {
			lResponseToken.setInteger("code", -1);
			lResponseToken.setString("msg",
					"Queue could not be created, because a session is missing");
		} else {
			String lQueueName = aToken.getString(QUEUE_NAME);
			if (null == lQueueName) {
				lResponseToken.setInteger("code", -1);
				lResponseToken.setString("msg", "Missing queue name.");
			} else {
				// check if user is allowed to read and/or write to the queue
				boolean notAllowed = SecurityFactory.hasRight(
						getUsername(aConnector), NS_JMS + ".na." + lQueueName);

				boolean noReadOrWrite = !SecurityFactory.hasRight(
						getUsername(aConnector), NS_JMS + ".rw." + lQueueName)
						&& !SecurityFactory.hasRight(getUsername(aConnector),
						NS_JMS + ".ro." + lQueueName)
						&& !SecurityFactory.hasRight(getUsername(aConnector),
						NS_JMS + ".wo." + lQueueName);

				if (notAllowed || noReadOrWrite) {
					sendToken(aConnector, aConnector,
							createAccessDenied(aToken));
					return;
				}
				try {
					mJmsTemplate.setPubSubDomain(false);
					mJmsTemplate.setDestinationName(lQueueName);
					Queue lQueue = mJmsTemplate.getSession().createQueue(
							lQueueName);
					if (null == lQueue) {
						lResponseToken.setInteger("code", -1);
						lResponseToken.setString("msg", "Could not creat queue");
					} else {
						mJmsTemplate.setDefaultDestination(lQueue);
						lResponseToken.setInteger("code", 1);
						lResponseToken.setString("msg",
								"Successfully created queue");
					}
				} catch (Exception e) {
					e.printStackTrace();
					lResponseToken.setInteger("code", -1);
					lResponseToken.setString("msg",
							"queue could not be created: " + e.getMessage());
				}
			}
		}
		// send the response
		sendToken(aConnector, aConnector, lResponseToken);
	}

	private void createConsumer(final WebSocketConnector aConnector,
			final Token aToken) {
		mLog.debug("Processing 'createConsumer'...");

		// check if user is allowed to create a session
		if (!SecurityFactory.hasRight(getUsername(aConnector), NS_JMS
				+ ".createConsumer")) {
			sendToken(aConnector, aConnector, createAccessDenied(aToken));
			return;
		}
		Token lResponseToken = createResponse(aToken);
		mLog.debug("creating consumer for destination");
		String lDestinationName = aToken.getString(DESTINATION_NAME);
		if (StringUtils.isBlank(lDestinationName)) {
			lResponseToken.setInteger("code", -1);
			lResponseToken.setString("msg", "Missing destination name.");
		} else {
			// check if user is allowed to read the destination
			boolean notAllowed = SecurityFactory.hasRight(getUsername(aConnector), NS_JMS + ".na."
					+ lDestinationName);

			boolean noReadRight = !SecurityFactory.hasRight(getUsername(aConnector), NS_JMS + ".rw."
					+ lDestinationName)
					&& !SecurityFactory.hasRight(getUsername(aConnector),
					NS_JMS + ".ro." + lDestinationName);

			if (notAllowed || noReadRight) {
				sendToken(aConnector, aConnector, createAccessDenied(aToken));
				return;
			}

			if (!mJmsTemplate.getDestinationName().equals(lDestinationName)) {
				lResponseToken.setInteger("code", -1);
				lResponseToken.setString("msg",
						"Missing destination with name: '" + lDestinationName
						+ "'");
			} else {
				mMessageListenerContainer = new SimpleMessageListenerContainer();
				mMessageListenerContainer.setConnectionFactory(mConnectionFactory);
				mMessageListenerContainer.setDestination(mJmsTemplate.getDefaultDestination());
				mMessageListenerContainer.setConcurrentConsumers(10);
				mMessageListenerContainer.setMessageListener(new MessageListener() {

					public void onMessage(Message message) {
						Token lResponseToken = createResponse(aToken);
						try {
							lResponseToken.setString("msg",
									((TextMessage) message).getText());
						} catch (JMSException e) {
							lResponseToken.setInteger("code", -1);
							lResponseToken.setString("msg",
									"Error while getting content of TextMessage.");
						}
						lResponseToken.setInteger("code", 1);
						sendToken(aConnector, aConnector,
								lResponseToken);
					}
				});
				mMessageListenerContainer.start();
				JMSStockMarketDataProducer producer = mBeanFactory.getBean(JMSStockMarketDataProducer.class);
				producer.sendStockMarketData();
				try {
					lResponseToken.setInteger("code", 1);
					lResponseToken.setString("msg",
							"Successfully created session");
				} catch (Exception e) {
					e.printStackTrace();
					lResponseToken.setInteger("code", -1);
					lResponseToken.setString("msg",
							"Session could not be created: " + e.getMessage());
				}
			}
		}
		// send the response
		sendToken(aConnector, aConnector, lResponseToken);
	}
}
