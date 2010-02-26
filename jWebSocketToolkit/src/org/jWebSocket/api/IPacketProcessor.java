/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jWebSocket.api;

import org.jWebSocket.kit.Token;

/**
 *
 * @author aschulze
 */
public interface IPacketProcessor {

	/**
	 *
	 * @param aData
	 * @return
	 */
	Token packetToToken(IDataPacket aDataPacket);

	/**
	 *
	 * @param aToken
	 * @return
	 */
	IDataPacket tokenToPacket(Token aToken);
}
