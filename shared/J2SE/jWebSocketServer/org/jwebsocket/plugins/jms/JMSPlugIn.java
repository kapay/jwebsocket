//	---------------------------------------------------------------------------
//	jWebSocket - JMSPlugIn
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
import org.jwebsocket.plugins.jms.util.ActionJms;
import org.jwebsocket.plugins.jms.util.FieldJms;
import org.jwebsocket.plugins.jms.util.RightJms;
import org.jwebsocket.security.SecurityFactory;
import org.jwebsocket.token.Token;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.FileSystemResource;

public class JMSPlugIn extends TokenPlugIn {

	private Logger mLog = Logging.getLogger(getClass());
	private static final String NS_JMS = JWebSocketServerConstants.NS_BASE
			+ ".plugins.jms";
	private BeanFactory mBeanFactory;
	private JmsManager mJmsManager = null;

	public JMSPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating JMS plug-in...");
		}

		this.setNamespace(NS_JMS);
		try {
			createBeanFactory();
			mJmsManager = JmsManager.getInstance(aConfiguration.getSettings(),
					mBeanFactory);
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
		mJmsManager.stopListeners(aConnector.getId());
	}

	@Override
	public void processToken(PlugInResponse aResponse,
			WebSocketConnector aConnector, Token aToken) {
		if (null == mBeanFactory) {
			sendMissingBeanFactoryResponseToken(aConnector, aToken);
		} else if (null == mJmsManager) {
			sendMissingJmsManagerResponseToken(aConnector, aToken);
		} else {
			processToken(aConnector, aToken);
		}
	}

	public void processToken(WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();

		if (lType != null && getNamespace().equals(lNS)) {
			if (ActionJms.LISTEN.equals(lType)) {
				listen(aConnector, aToken);
			} else if (ActionJms.SEND_TEXT.equals(lType)) {
				sendText(aConnector, aToken);
			} else if (ActionJms.SEND_MAP.equals(lType)) {
				sendMap(aConnector, aToken);
			} else if (ActionJms.UNLISTEN.equals(lType)) {
				unlisten(aConnector, aToken);
			}
		}
	}

	private void sendMissingJmsManagerResponseToken(
			WebSocketConnector aConnector, Token aToken) {
		Token lResponseToken = createResponse(aToken);
		lResponseToken.setInteger("code", -1);
		lResponseToken.setString("msg",
				"missing jms manager: correct your config");
		sendToken(aConnector, aConnector, lResponseToken);
	}

	private void sendMissingBeanFactoryResponseToken(
			WebSocketConnector aConnector, Token aToken) {
		Token lResponseToken = createResponse(aToken);
		lResponseToken.setInteger("code", -1);
		lResponseToken.setString("msg",
				"missing jms spring beanfactory: correct your config");
		sendToken(aConnector, aConnector, lResponseToken);
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

		mJmsManager.deregisterConnectorFromListener(aConnector.getId(),
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

		try {
			mJmsManager.registerConnectorWithListener(aConnector.getId(),
					aToken, lDestinationIdentifier, this);
		} catch (Exception e) {
			lResponseToken.setInteger("code", -1);
			lResponseToken.setString("msg", e.getMessage());
			// send the response
			sendToken(aConnector, aConnector, lResponseToken);
			return;
		}

		lResponseToken.setInteger("code", 1);
		lResponseToken.setString("msg", "Successfully got jms listener");
		// send the response
		sendToken(aConnector, aConnector, lResponseToken);
	}

	private void sendText(WebSocketConnector aConnector, Token aToken) {
		mLog.debug("Processing 'sendText'...");

		Token lResponseToken = createResponse(aToken);
		DestinationIdentifier lDestinationIdentifier = DestinationIdentifier.valueOf(aToken);

		if (lDestinationIdentifier.isMissingData()) {
			lResponseToken.setInteger("code", -1);
			lResponseToken.setString("msg",
					"Missing destination input data for sending a string message");
			sendToken(aConnector, aConnector, lResponseToken);
			return;
		}

		if (!hasRight(aConnector, lDestinationIdentifier.isPubSubDomain(),
				lDestinationIdentifier.getDestinationName(), RightJms.SEND,
				RightJms.SEND_AND_LISTEN)) {
			sendToken(aConnector, aConnector, createAccessDenied(aToken));
			return;
		}

		try {
			mJmsManager.sendText(lDestinationIdentifier,
					aToken.getString(FieldJms.TEXT.getValue()));
		} catch (Exception e) {
			lResponseToken.setInteger("code", -1);
			lResponseToken.setString("msg", e.getMessage());
			// send the response
			sendToken(aConnector, aConnector, lResponseToken);
		}

		lResponseToken.setInteger("code", 1);
		lResponseToken.setString("msg", "Jms string message successfully sent");
		// send the response
		sendToken(aConnector, aConnector, lResponseToken);
	}

	private void sendMap(WebSocketConnector aConnector, Token aToken) {
		mLog.debug("Processing 'sendMap'...");

		Token lResponseToken = createResponse(aToken);
		DestinationIdentifier lDestinationIdentifier = DestinationIdentifier.valueOf(aToken);

		if (lDestinationIdentifier.isMissingData()) {
			lResponseToken.setInteger("code", -1);
			lResponseToken.setString("msg",
					"Missing destination input data for sending a map message");
			sendToken(aConnector, aConnector, lResponseToken);
			return;
		}

		if (!hasRight(aConnector, lDestinationIdentifier.isPubSubDomain(),
				lDestinationIdentifier.getDestinationName(), RightJms.SEND,
				RightJms.SEND_AND_LISTEN)) {
			sendToken(aConnector, aConnector, createAccessDenied(aToken));
			return;
		}

		try {
			mJmsManager.sendMap(lDestinationIdentifier,
					aToken.getMap(FieldJms.MAP.getValue()));
		} catch (Exception e) {
			lResponseToken.setInteger("code", -1);
			lResponseToken.setString("msg", e.getMessage());
			// send the response
			sendToken(aConnector, aConnector, lResponseToken);
		}

		lResponseToken.setInteger("code", 1);
		lResponseToken.setString("msg", "Jms map message successfully sent");
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
