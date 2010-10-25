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
	 * @param aSubProt
	 * @param aDataPacket
	 * @return
	 */
	public static Token packetToToken(String aSubProt, WebSocketPacket aDataPacket) {
		Token lToken = null;
		if (aSubProt.equals(JWebSocketCommonConstants.WS_SUBPROT_JSON)
				|| aSubProt.equals(JWebSocketCommonConstants.SUB_PROT_JSON)) {
			lToken = JSONProcessor.packetToToken(aDataPacket);
		} else if (aSubProt.equals(JWebSocketCommonConstants.WS_SUBPROT_CSV)
				|| aSubProt.equals(JWebSocketCommonConstants.SUB_PROT_CSV)) {
			lToken = CSVProcessor.packetToToken(aDataPacket);
		} else if (aSubProt.equals(JWebSocketCommonConstants.WS_SUBPROT_XML)
				|| aSubProt.equals(JWebSocketCommonConstants.SUB_PROT_XML)) {
			lToken = XMLProcessor.packetToToken(aDataPacket);
		}
		return lToken;
	}

	/**
	 *
	 * @param aSubProt
	 * @param aToken
	 * @return
	 */
	public static WebSocketPacket tokenToPacket(String aSubProt, Token aToken) {
		WebSocketPacket lPacket = null;
		// TODO: Remove deprecated sub protocol constants one day, when browsers have been updated
		if (aSubProt.equals(JWebSocketCommonConstants.WS_SUBPROT_JSON)
				|| aSubProt.equals(JWebSocketCommonConstants.SUB_PROT_JSON)) {
			lPacket = JSONProcessor.tokenToPacket(aToken);
		} else if (aSubProt.equals(JWebSocketCommonConstants.WS_SUBPROT_CSV)
				|| aSubProt.equals(JWebSocketCommonConstants.SUB_PROT_CSV)) {
			lPacket = CSVProcessor.tokenToPacket(aToken);
		} else if (aSubProt.equals(JWebSocketCommonConstants.WS_SUBPROT_XML)
				|| aSubProt.equals(JWebSocketCommonConstants.SUB_PROT_XML)) {
			lPacket = XMLProcessor.tokenToPacket(aToken);
		}
		return lPacket;
	}

}
