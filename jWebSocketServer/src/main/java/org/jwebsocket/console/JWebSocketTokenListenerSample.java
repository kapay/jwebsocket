//	---------------------------------------------------------------------------
//	jWebSocket - Copyright (c) 2010 jwebsocket.org
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
package org.jwebsocket.console;

import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.config.JWebSocketConstants;
import org.jwebsocket.kit.WebSocketEvent;
import org.jwebsocket.listener.WebSocketTokenEvent;
import org.jwebsocket.listener.WebSocketTokenListener;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.token.Token;

/**
 * This shows an example of a simple websocket listener
 * @author aschulze
 */
public class JWebSocketTokenListenerSample implements WebSocketTokenListener {

	private static Logger log = Logging.getLogger(JWebSocketTokenListenerSample.class);

	/**
	 *
	 * @param aEvent
	 */
	@Override
	public void processOpened(WebSocketEvent aEvent) {
		log.info("Client '" + aEvent.getSessionId() + "' connected.");
	}

	/**
	 *
	 * @param aEvent
	 * @param aPacket
	 */
	@Override
	public void processPacket(WebSocketEvent aEvent, WebSocketPacket aPacket) {
		// log.info("Client '" + aEvent.getSessionId() + "' sent: '" + aPacket.getASCII() + "'.");
		// Here you can answer an arbitrary text package...
		// this is how to easily respond to a previous client's request
		// aEvent.sendPacket(aPacket);
		// this is how to send a packet to any connector
		// aEvent.getServer().sendPacket(aEvent.getConnector(), aPacket);
	}

	/**
	 *
	 * @param aEvent
	 * @param aToken
	 */
	@Override
	public void processToken(WebSocketTokenEvent aEvent, Token aToken) {
		log.info("Client '" + aEvent.getSessionId() + "' sent Token: '" + aToken.toString() + "'.");
		// here you can simply interpret the token type sent from the client
		// according to your needs.
		String lNS = aToken.getNS();
		String lType = aToken.getType();

		// check if token has a type and a matching namespace
		if (lType != null && "my.namespace".equals(lNS)) {
			// if type is "getInfo" return some server information
			Token lResponse = aEvent.createResponse(aToken);
			if ("getInfo".equals(lType)) {
				lResponse.put("vendor", JWebSocketConstants.VENDOR);
				lResponse.put("version", JWebSocketConstants.VERSION_STR);
				lResponse.put("copyright", JWebSocketConstants.COPYRIGHT);
				lResponse.put("license", JWebSocketConstants.LICENSE);
			} else {
				// if unknown type in this namespace, return corresponding error message
				lResponse.put("code", -1);
				lResponse.put("msg", "Token type '" + lType + "' not supported in namespace '" + lNS + "'.");
			}
			aEvent.sendToken(lResponse);
		}
	}

	/**
	 *
	 * @param aEvent
	 */
	@Override
	public void processClosed(WebSocketEvent aEvent) {
		log.info("Client '" + aEvent.getSessionId() + "' disconnected.");
	}
}
