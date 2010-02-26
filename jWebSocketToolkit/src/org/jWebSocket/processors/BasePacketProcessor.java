/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jWebSocket.processors;

import org.jWebSocket.api.IDataPacket;
import org.jWebSocket.api.IPacketProcessor;
import org.jWebSocket.kit.Token;

/**
 *
 * @author aschulze
 */
public abstract class BasePacketProcessor implements IPacketProcessor {

	public abstract Token packetToToken(IDataPacket aDataPacket);
	public abstract IDataPacket tokenToPacket(Token aToken);
}
