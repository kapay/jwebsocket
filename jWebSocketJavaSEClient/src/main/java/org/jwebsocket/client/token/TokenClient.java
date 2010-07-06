//	---------------------------------------------------------------------------
//	jWebSocket - Copyright (c) 2010 Innotrade GmbH
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
package org.jwebsocket.client.token;

import org.jwebsocket.api.WebSocketClientListener;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.listener.WebSocketClientTokenListener;
import org.jwebsocket.client.se.BaseClient;
import org.jwebsocket.config.JWebSocketConstants;
import org.jwebsocket.kit.WebSocketClientEvent;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.listener.WebSocketClientTokenEvent;
import org.jwebsocket.packetProcessors.CSVProcessor;
import org.jwebsocket.packetProcessors.JSONProcessor;
import org.jwebsocket.packetProcessors.XMLProcessor;
import org.jwebsocket.token.Token;

/**
 *
 * @author aschulze
 */
public class TokenClient {

	private BaseClient client = null;
	private String lSubProt = JWebSocketConstants.SUB_PROT_DEFAULT;

	class Listener implements WebSocketClientListener {

		@Override
		public void processOpened(WebSocketClientEvent aEvent) {
			// no need to do anything here
		}

		@Override
		public void processPacket(WebSocketClientEvent aEvent, WebSocketPacket aPacket) {
			for (WebSocketClientListener lListener : client.getListeners()) {
				if (lListener instanceof WebSocketClientTokenListener) {
					Token lToken = packetToToken(aPacket);
					((WebSocketClientTokenListener) lListener).processToken(aEvent, lToken);
				}
			}
		}

		@Override
		public void processClosed(WebSocketClientEvent aEvent) {
			// no need to do anything here
		}
	}

	public TokenClient(BaseClient aClient) {
		client = aClient;
		client.addListener(new Listener());
	}

	public void addListener(WebSocketClientTokenListener aListener) {
		client.addListener(aListener);
	}

	public void removeListener(WebSocketClientTokenListener aListener) {
		client.removeListener(aListener);
	}

	public void open(String aURL) throws WebSocketException {
		client.open(aURL);
	}

	public void send(String aData, String aEncoding) throws WebSocketException {
		client.send(aData, aEncoding);
	}

	public void send(byte[] aData) throws WebSocketException {
		client.send(aData);
	}

	public void send(WebSocketPacket aPacket) throws WebSocketException {
		client.send(aPacket.getByteArray());
	}

	public void close() throws WebSocketException {
		client.close();
	}

	/**
	 *
	 * @param aConnector
	 * @param aPacket
	 * @return
	 */
	public Token packetToToken(WebSocketPacket aPacket) {
		Token lToken = null;
		if (lSubProt.equals(JWebSocketConstants.SUB_PROT_JSON)) {
			lToken = JSONProcessor.packetToToken(aPacket);
		} else if (lSubProt.equals(JWebSocketConstants.SUB_PROT_CSV)) {
			lToken = CSVProcessor.packetToToken(aPacket);
		} else if (lSubProt.equals(JWebSocketConstants.SUB_PROT_XML)) {
			lToken = XMLProcessor.packetToToken(aPacket);
		}
		return lToken;
	}

	/**
	 *
	 * @param aConnector
	 * @param aToken
	 * @return
	 */
	public WebSocketPacket tokenToPacket(Token aToken) {
		WebSocketPacket lPacket = null;
		if (lSubProt.equals(JWebSocketConstants.SUB_PROT_JSON)) {
			lPacket = JSONProcessor.tokenToPacket(aToken);
		} else if (lSubProt.equals(JWebSocketConstants.SUB_PROT_CSV)) {
			lPacket = CSVProcessor.tokenToPacket(aToken);
		} else if (lSubProt.equals(JWebSocketConstants.SUB_PROT_XML)) {
			lPacket = XMLProcessor.tokenToPacket(aToken);
		}
		return lPacket;
	}

	public void sendToken(Token aToken) throws WebSocketException {
		send(tokenToPacket(aToken));
	}
}
