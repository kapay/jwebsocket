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
import org.jwebsocket.api.WebSocketEngine;
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
	private static final String NS_JMS = JWebSocketServerConstants.NS_BASE + ".plugins.jms";
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
			mJmsManager = JmsManager.getInstance(aConfiguration.getSettings(), mBeanFactory);
		} catch (Exception lEx) {
			mLog.error(lEx.getClass().getSimpleName() + " instantiation: " + lEx.getMessage());
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
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
		mJmsManager.stopListener(aConnector.getId());
	}

	@Override
	public void engineStopped(WebSocketEngine aEngine) {
		mJmsManager.shutDownListeners();
	}

	@Override
	public void processToken(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {
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
			} else if (ActionJms.LISTEN_MESSAGE.equals(lType)) {
				listenMessage(aConnector, aToken);
			} else if (ActionJms.SEND_TEXT.equals(lType)) {
				sendText(aConnector, aToken);
			} else if (ActionJms.SEND_TEXT_MESSAGE.equals(lType)) {
				sendTextMessage(aConnector, aToken);
			} else if (ActionJms.SEND_MAP.equals(lType)) {
				sendMap(aConnector, aToken);
			} else if (ActionJms.SEND_MAP_MESSAGE.equals(lType)) {
				sendMapMessage(aConnector, aToken);
			} else if (ActionJms.UNLISTEN.equals(lType)) {
				unlisten(aConnector, aToken);
			}
			// else if (ActionJms.UNLISTEN_MESSAGE.equals(lType)) {
			// unlistenMessage(aConnector, aToken);
			// }
		}
	}

	private void sendMissingJmsManagerResponseToken(WebSocketConnector aConnector, Token aToken) {
		Token lResponseToken = createResponse(aToken);
		lResponseToken.setInteger("code", -1);
		lResponseToken.setString("msg", "missing jms manager: correct your config");
		sendToken(aConnector, aConnector, lResponseToken);
	}

	private void sendMissingBeanFactoryResponseToken(WebSocketConnector aConnector, Token aToken) {
		Token lResponseToken = createResponse(aToken);
		lResponseToken.setInteger("code", -1);
		lResponseToken.setString("msg", "missing jms spring beanfactory: correct your config");
		sendToken(aConnector, aConnector, lResponseToken);
	}

	private void unlisten(WebSocketConnector aConnector, Token aToken) {
		mLog.debug("Processing 'unlisten'...");

		Token lResponseToken = createResponse(aToken);

		DestinationIdentifier lDestinationIdentifier = DestinationIdentifier.valueOf(aToken);

		if (lDestinationIdentifier.isMissingData()) {
			lResponseToken.setInteger("code", -1);
			lResponseToken.setString("msg", "Missing input data for unlisten a jms listener");
			sendToken(aConnector, aConnector, lResponseToken);
			return;
		}

		mJmsManager.deregisterConnectorFromMessageListener(aConnector.getId(), lDestinationIdentifier);

		lResponseToken.setInteger("code", 1);
		lResponseToken.setString("msg", "Successfully unlisten jms listener");
		// send the response
		sendToken(aConnector, aConnector, lResponseToken);
	}

	// private void unlistenMessage(WebSocketConnector aConnector, Token aToken)
	// {
	// mLog.debug("Processing 'unlistenMessage'...");
	//
	// Token lResponseToken = createResponse(aToken);
	//
	// DestinationIdentifier lDestinationIdentifier =
	// DestinationIdentifier.valueOf(aToken);
	//
	// if (lDestinationIdentifier.isMissingData()) {
	// lResponseToken.setInteger("code", -1);
	// lResponseToken.setString("msg",
	// "Missing input data for unlisten a jms message listener");
	// sendToken(aConnector, aConnector, lResponseToken);
	// return;
	// }
	//
	// mJmsManager.deregisterConnectorFromMessageListener(aConnector.getId(),
	// lDestinationIdentifier);
	//
	// lResponseToken.setInteger("code", 1);
	// lResponseToken.setString("msg",
	// "Successfully unlisten jms message listener");
	// // send the response
	// sendToken(aConnector, aConnector, lResponseToken);
	// }

	private void listen(WebSocketConnector aConnector, Token aToken) {
		mLog.debug("Processing 'listen'...");

		Token lResponseToken = createResponse(aToken);
		DestinationIdentifier lDestinationIdentifier = DestinationIdentifier.valueOf(aToken);

		if (lDestinationIdentifier.isMissingData()) {
			lResponseToken.setInteger("code", -1);
			lResponseToken.setString("msg", "Missing input data for establishing a jms listener");
			sendToken(aConnector, aConnector, lResponseToken);
			return;
		}

		if (!hasRight(aConnector, lDestinationIdentifier.isPubSubDomain(), lDestinationIdentifier.getDestinationName(),
				RightJms.LISTEN, RightJms.SEND_AND_LISTEN)) {
			sendToken(aConnector, aConnector, createAccessDenied(aToken));
			return;
		}

		try {
			mJmsManager.registerConnectorWithListener(aConnector.getId(), aToken, lDestinationIdentifier, this);
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

	private void listenMessage(WebSocketConnector aConnector, Token aToken) {
		mLog.debug("Processing 'listenMessage'...");

		Token lResponseToken = createResponse(aToken);
		DestinationIdentifier lDestinationIdentifier = DestinationIdentifier.valueOf(aToken);

		if (lDestinationIdentifier.isMissingData()) {
			lResponseToken.setInteger("code", -1);
			lResponseToken.setString("msg", "Missing input data for establishing a jms message listener");
			sendToken(aConnector, aConnector, lResponseToken);
			return;
		}

		if (!hasRight(aConnector, lDestinationIdentifier.isPubSubDomain(), lDestinationIdentifier.getDestinationName(),
				RightJms.LISTEN, RightJms.SEND_AND_LISTEN)) {
			sendToken(aConnector, aConnector, createAccessDenied(aToken));
			return;
		}

		try {
			mJmsManager.registerConnectorWithMessageListener(aConnector.getId(), aToken, lDestinationIdentifier, this);
		} catch (Exception e) {
			lResponseToken.setInteger("code", -1);
			lResponseToken.setString("msg", e.getMessage());
			// send the response
			sendToken(aConnector, aConnector, lResponseToken);
			return;
		}

		lResponseToken.setInteger("code", 1);
		lResponseToken.setString("msg", "Successfully got jms message listener");
		// send the response
		sendToken(aConnector, aConnector, lResponseToken);
	}

	private void sendText(WebSocketConnector aConnector, Token aToken) {
		mLog.debug("Processing 'sendText'...");

		Token lResponseToken = createResponse(aToken);
		DestinationIdentifier lDestinationIdentifier = DestinationIdentifier.valueOf(aToken);

		if (lDestinationIdentifier.isMissingData()) {
			lResponseToken.setInteger("code", -1);
			lResponseToken.setString("msg", "Missing destination input data for sending text");
			sendToken(aConnector, aConnector, lResponseToken);
			return;
		}

		if (!hasRight(aConnector, lDestinationIdentifier.isPubSubDomain(), lDestinationIdentifier.getDestinationName(),
				RightJms.SEND, RightJms.SEND_AND_LISTEN)) {
			sendToken(aConnector, aConnector, createAccessDenied(aToken));
			return;
		}

		try {
			mJmsManager.sendText(lDestinationIdentifier, aToken.getString(FieldJms.MESSSAGE_PAYLOAD.getValue()));
		} catch (Exception e) {
			lResponseToken.setInteger("code", -1);
			lResponseToken.setString("msg", e.getMessage());
			// send the response
			sendToken(aConnector, aConnector, lResponseToken);
		}

		lResponseToken.setInteger("code", 1);
		lResponseToken.setString("msg", "Text successfully sent");
		// send the response
		sendToken(aConnector, aConnector, lResponseToken);
	}

	private void sendTextMessage(WebSocketConnector aConnector, Token aToken) {
		mLog.debug("Processing 'sendTextMessage'...");

		Token lResponseToken = createResponse(aToken);
		DestinationIdentifier lDestinationIdentifier = DestinationIdentifier.valueOf(aToken);

		if (lDestinationIdentifier.isMissingData()) {
			lResponseToken.setInteger("code", -1);
			lResponseToken.setString("msg", "Missing destination input data for sending a text message");
			sendToken(aConnector, aConnector, lResponseToken);
			return;
		}

		if (!hasRight(aConnector, lDestinationIdentifier.isPubSubDomain(), lDestinationIdentifier.getDestinationName(),
				RightJms.SEND, RightJms.SEND_AND_LISTEN)) {
			sendToken(aConnector, aConnector, createAccessDenied(aToken));
			return;
		}

		try {
			mJmsManager.sendTextMessage(lDestinationIdentifier, aToken.getString(FieldJms.MESSSAGE_PAYLOAD.getValue()),
					aToken.getMap(FieldJms.JMS_HEADER_PROPERTIES.getValue()));
		} catch (Exception e) {
			lResponseToken.setInteger("code", -1);
			lResponseToken.setString("msg", e.getMessage());
			// send the response
			sendToken(aConnector, aConnector, lResponseToken);
		}

		lResponseToken.setInteger("code", 1);
		lResponseToken.setString("msg", "Jms text message successfully sent");
		// send the response
		sendToken(aConnector, aConnector, lResponseToken);
	}

	private void sendMap(WebSocketConnector aConnector, Token aToken) {
		mLog.debug("Processing 'sendMap'...");

		Token lResponseToken = createResponse(aToken);
		DestinationIdentifier lDestinationIdentifier = DestinationIdentifier.valueOf(aToken);

		if (lDestinationIdentifier.isMissingData()) {
			lResponseToken.setInteger("code", -1);
			lResponseToken.setString("msg", "Missing destination input data for sending a map");
			sendToken(aConnector, aConnector, lResponseToken);
			return;
		}

		if (!hasRight(aConnector, lDestinationIdentifier.isPubSubDomain(), lDestinationIdentifier.getDestinationName(),
				RightJms.SEND, RightJms.SEND_AND_LISTEN)) {
			sendToken(aConnector, aConnector, createAccessDenied(aToken));
			return;
		}

		try {
			mJmsManager.sendMap(lDestinationIdentifier, aToken.getMap(FieldJms.MESSSAGE_PAYLOAD.getValue()));
		} catch (Exception e) {
			lResponseToken.setInteger("code", -1);
			lResponseToken.setString("msg", e.getMessage());
			// send the response
			sendToken(aConnector, aConnector, lResponseToken);
		}

		lResponseToken.setInteger("code", 1);
		lResponseToken.setString("msg", "Map message successfully sent");
		// send the response
		sendToken(aConnector, aConnector, lResponseToken);
	}

	private void sendMapMessage(WebSocketConnector aConnector, Token aToken) {
		mLog.debug("Processing 'sendMapMessage'...");

		Token lResponseToken = createResponse(aToken);
		DestinationIdentifier lDestinationIdentifier = DestinationIdentifier.valueOf(aToken);

		if (lDestinationIdentifier.isMissingData()) {
			lResponseToken.setInteger("code", -1);
			lResponseToken.setString("msg", "Missing destination input data for sending a map message");
			sendToken(aConnector, aConnector, lResponseToken);
			return;
		}

		if (!hasRight(aConnector, lDestinationIdentifier.isPubSubDomain(), lDestinationIdentifier.getDestinationName(),
				RightJms.SEND, RightJms.SEND_AND_LISTEN)) {
			sendToken(aConnector, aConnector, createAccessDenied(aToken));
			return;
		}

		try {
			mJmsManager.sendMapMessage(lDestinationIdentifier, aToken.getMap(FieldJms.MESSSAGE_PAYLOAD.getValue()),
					aToken.getMap(FieldJms.JMS_HEADER_PROPERTIES.getValue()));
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

	private boolean hasRight(WebSocketConnector aConnector, boolean aPubSubDomain, String aDestinationName,
			RightJms... aRights) {
		for (RightJms next : aRights) {
			if (SecurityFactory.hasRight(getUsername(aConnector), NS_JMS + "." + next + "."
					+ (aPubSubDomain ? "topic" : "queue") + "." + aDestinationName)) {
				return true;
			}
		}

		return false;
	}
}
