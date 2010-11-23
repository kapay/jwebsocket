/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.token;

import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.packetProcessors.CSVProcessor;
import org.jwebsocket.packetProcessors.JSONProcessor;
import org.jwebsocket.packetProcessors.XMLProcessor;

/**
 *
 * @author aschulze
 * @author jang
 */
public class TokenFactory {

	/**
	 *
	 * @return
	 */
	public static Token createToken() {
		return new MapToken();
	}

	/**
	 *
	 * @param aType
	 * @return
	 */
	public static Token createToken(String aType) {
		return new MapToken(aType);
	}

	/**
	 *
	 * @param aNS
	 * @param aType
	 * @return
	 */
	public static Token createToken(String aNS, String aType) {
		return new MapToken(aNS, aType);
	}

	/**
	 *
	 * @param aFormat
	 * @param aDataPacket
	 * @return
	 */
	public static Token packetToToken(String aFormat, WebSocketPacket aDataPacket) {
		Token lToken = null;
		if (JWebSocketCommonConstants.WS_FORMAT_JSON.equals(aFormat)) {
			lToken = JSONProcessor.packetToToken(aDataPacket);
		} else if (JWebSocketCommonConstants.WS_FORMAT_CSV.equals(aFormat)) {
			lToken = CSVProcessor.packetToToken(aDataPacket);
		} else if (JWebSocketCommonConstants.WS_FORMAT_XML.equals(aFormat)) {
			lToken = XMLProcessor.packetToToken(aDataPacket);
		}
		return lToken;
	}

	/**
	 *
	 * @param aFormat
	 * @param aToken
	 * @return
	 */
	public static WebSocketPacket tokenToPacket(String aFormat, Token aToken) {
		WebSocketPacket lPacket = null;
		if (JWebSocketCommonConstants.WS_FORMAT_JSON.equals(aFormat)) {
			lPacket = JSONProcessor.tokenToPacket(aToken);
		} else if (JWebSocketCommonConstants.WS_FORMAT_CSV.equals(aFormat)) {
			lPacket = CSVProcessor.tokenToPacket(aToken);
		} else if (JWebSocketCommonConstants.WS_FORMAT_XML.equals(aFormat)) {
			lPacket = XMLProcessor.tokenToPacket(aToken);
		}
		return lPacket;
	}

}
