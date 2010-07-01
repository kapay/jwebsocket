/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.console;

import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.kit.WebSocketEvent;
import org.jwebsocket.listener.WebSocketTokenListener;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.token.Token;

/**
 * This shown an example of a simple websocket listener
 * @author aschulze
 */
public class JWebSocketTokenListenerSample implements WebSocketTokenListener {

	private static Logger log = Logging.getLogger(JWebSocketTokenListenerSample.class);

	@Override
	public void processOpened(WebSocketEvent aEvent) {
		log.info("Client '" + aEvent.getSessionId() + "' connected.");
	}

	@Override
	public void processPacket(WebSocketEvent aEvent, WebSocketPacket aPacket) {
		log.info("Client '" + aEvent.getSessionId() + "' sent: '" + aPacket.getASCII() + "'.");
		// Here you can answer an arbitrary text package...
		// aEvent.getServer().sendPacket(aEvent.getConnector(), aPacket);
	}

	@Override
	public void processToken(WebSocketEvent aEvent, Token aToken) {
		log.info("Client '" + aEvent.getSessionId() + "' sent Token: '" + aToken.toString() + "'.");
		// here you can simply interpret the token type sent from the client
		// according to your needs.
		String lNS = aToken.getNS();
		String lType = aToken.getType();
	}

	@Override
	public void processClosed(WebSocketEvent aEvent) {
		log.info("Client '" + aEvent.getSessionId() + "' disconnected.");
	}
}
