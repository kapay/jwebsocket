//	---------------------------------------------------------------------------
//	jWebSocket - JMS Plug-in
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
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.plugins.jms.action.ActionJms;
import org.jwebsocket.plugins.jms.action.FieldJms;
import org.jwebsocket.plugins.jms.action.RightJms;
import org.jwebsocket.plugins.jms.infra.impl.JmsListenerContainer;
import org.jwebsocket.security.SecurityFactory;
import org.jwebsocket.token.Token;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.FileSystemResource;

/**
 * 
 * @author jsmutny
 */
public class JMSPlugIn extends TokenPlugIn {

	private static Logger mLog = Logging.getLogger(JMSPlugIn.class);
	private static final String NS_JMS = JWebSocketServerConstants.NS_BASE
			+ ".plugins.jms";
	private BeanFactory mBeanFactory;
	private JMSManager mJMSManager = null;

	public JMSPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating JMS plug-in...");
		}
		// specify default name space for jms plugin
		this.setNamespace(NS_JMS);
		try {
			createBeanFactory();
			mJMSManager = JMSManager.getJMSManager(
					aConfiguration.getSettings(), mBeanFactory);
		} catch (Exception lEx) {
			mLog.error(lEx.getClass().getSimpleName() + " instantiation: "
					+ lEx.getMessage());
		}
	}

	private void createBeanFactory() {
		String lSpringConfig = getString("spring_config");
		String lPath = FilenameUtils.getPath(lSpringConfig);
		if (lPath == null || lPath.length() <= 0) {
			lPath = JWebSocketConfig.getConfigFolder(lSpringConfig);
		} else {
			lPath = lSpringConfig;
		}
		mBeanFactory = new XmlBeanFactory(new FileSystemResource(lPath));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void connectorStarted(WebSocketConnector aConnector) {
		// call super connectorStarted
		super.connectorStarted(aConnector);
		// currently no further action required here
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void connectorStopped(WebSocketConnector aConnector,
			CloseReason aCloseReason) {
		// disconnect from the destinations, the associated consumer listens to
		Consumer lConsumer = mJMSManager.getConsumer(aConnector.getId());
		if (lConsumer != null) {
			for (JmsListenerContainer listener : lConsumer.getListenerContainers()) {
				listener.getMessageConsumerRegistry().removeMessageConsumer(
						aConnector.getId());
			}
		}
	}

	@Override
	public void processToken(PlugInResponse aResponse,
			WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();
		if (lType != null && getNamespace().equals(lNS)) {
			if (ActionJms.LISTEN.equals(lType)) {
				listen(aConnector, aToken);
			} else if (ActionJms.SEND_STRING_MESSAGE.equals(lType)) {
				sendStringMessage(aConnector, aToken);
			} else if (ActionJms.UNLISTEN.equals(lType)) {
				unlisten(aConnector, aToken);
			} else {
				// ignore
			}
		}
	}

	private void unlisten(WebSocketConnector aConnector, Token aToken) {
		mLog.debug("Processing 'unlisten'...");

		Token lResponseToken = createResponse(aToken);

		DestinationIdentifier lDestinationIdentifier = DestinationIdentifier.valueOf(aToken);

		if (lDestinationIdentifier.isMissingData()) {
			lResponseToken.setInteger("code", -1);
			lResponseToken.setString("msg",
					"Missing input data for unlisten a jms listener");
			sendToken(aConnector, aConnector, lResponseToken);
			return;
		}

		mJMSManager.deregisterConsumer(aConnector.getId(),
				lDestinationIdentifier);

		lResponseToken.setInteger("code", 1);
		lResponseToken.setString("msg", "Successfully unlisten jms listener");
		// send the response
		sendToken(aConnector, aConnector, lResponseToken);
	}

	private void listen(WebSocketConnector aConnector, Token aToken) {
		mLog.debug("Processing 'listen'...");

		Token lResponseToken = createResponse(aToken);
		DestinationIdentifier lDestinationIdentifier = DestinationIdentifier.valueOf(aToken);

		if (lDestinationIdentifier.isMissingData()) {
			lResponseToken.setInteger("code", -1);
			lResponseToken.setString("msg",
					"Missing input data for establishing a jms listener");
			sendToken(aConnector, aConnector, lResponseToken);
			return;
		}

		if (!hasRight(aConnector, lDestinationIdentifier.isPubSubDomain(),
				lDestinationIdentifier.getDestinationName(), RightJms.LISTEN,
				RightJms.SEND_AND_LISTEN)) {
			sendToken(aConnector, aConnector, createAccessDenied(aToken));
			return;
		}

		mJMSManager.registerConsumer(aConnector.getId(), aToken,
				lDestinationIdentifier, this);

		lResponseToken.setInteger("code", 1);
		lResponseToken.setString("msg", "Successfully got jms listener");
		// send the response
		sendToken(aConnector, aConnector, lResponseToken);
	}

	private void sendStringMessage(WebSocketConnector aConnector, Token aToken) {
		mLog.debug("Processing 'sendStringMessage'...");

		Token lResponseToken = createResponse(aToken);
		DestinationIdentifier lDestinationIdentifier = DestinationIdentifier.valueOf(aToken);

		if (lDestinationIdentifier.isMissingData()) {
			lResponseToken.setInteger("code", -1);
			lResponseToken.setString("msg",
					"Missing input data for sending a string message");
			sendToken(aConnector, aConnector, lResponseToken);
			return;
		}

		if (!hasRight(aConnector, lDestinationIdentifier.isPubSubDomain(),
				lDestinationIdentifier.getDestinationName(), RightJms.SEND,
				RightJms.SEND_AND_LISTEN)) {
			sendToken(aConnector, aConnector, createAccessDenied(aToken));
			return;
		}

		mJMSManager.registerProducer(aConnector.getId(), lDestinationIdentifier);
		mJMSManager.sendStringMessage(lDestinationIdentifier,
				aToken.getString(FieldJms.STRING_MESSAGE.getValue()));

		lResponseToken.setInteger("code", 1);
		lResponseToken.setString("msg", "Jms string message successfully sent");
		// send the response
		sendToken(aConnector, aConnector, lResponseToken);
	}

	private boolean hasRight(WebSocketConnector aConnector,
			boolean aPubSubDomain, String aDestinationName, RightJms... aRights) {
		for (RightJms next : aRights) {
			if (SecurityFactory.hasRight(getUsername(aConnector), NS_JMS + "."
					+ next + "." + (aPubSubDomain ? "topic" : "queue") + "."
					+ aDestinationName)) {
				return true;
			}
		}

		return false;
	}
}
