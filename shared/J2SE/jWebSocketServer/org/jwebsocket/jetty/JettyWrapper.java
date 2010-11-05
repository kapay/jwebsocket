//	---------------------------------------------------------------------------
//	jWebSocket - Jetty Engine
//	Copyright (c) 2010 Alexander Schulze, Innotrade GmbH
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
package org.jwebsocket.jetty;

import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.eclipse.jetty.websocket.WebSocket;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.RawPacket;
import org.jwebsocket.logging.Logging;


// LOOK AT THIS: http://www.maths.tcd.ie/~dacurran/4ict12/assignment3/

/**
 *
 * @author alexanderschulze
 */
public class JettyWrapper implements WebSocket {

	private static Logger mLog = Logging.getLogger(JettyWrapper.class);
	private WebSocketConnector mConnector = null;
	private WebSocketEngine mEngine = null;
	private HttpServletRequest mRequest = null;
	private String mProtocol = null;

	public JettyWrapper(HttpServletRequest aRequest, String aProtocol) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating Jetty Wrapper with subprotocol '" + aProtocol + "'...");
		}
		mEngine = JWebSocketFactory.getEngine();
		mRequest = aRequest;
		mProtocol = aProtocol;

	}

	@Override
	public void onConnect(Outbound aOutbound) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Connecting Jetty Client...");
		}
		mConnector = new JettyConnector(mEngine, mRequest, mProtocol, aOutbound);
		mEngine.addConnector(mConnector);
		// inherited BaseConnector.startConnector
		// calls mEngine connector started
		mConnector.startConnector();
	}

	@Override
	public void onMessage(byte frame, byte[] data, int offset, int length) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Message (binary) from Jetty client...");
		}
		if (mConnector != null) {
			WebSocketPacket lDataPacket = new RawPacket(data);
			mEngine.processPacket(mConnector, lDataPacket);
		}
	}

	@Override
	public void onMessage(byte aFrame, String aData) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Message (string, opcode " 
					+ aFrame 
					+ ") from Jetty client: '"
					+ aData + "'...");
		}
		if (mConnector != null) {
			WebSocketPacket lDataPacket = new RawPacket(aData);
			mEngine.processPacket(mConnector, lDataPacket);
		}
	}

	@Override
	public void onDisconnect() {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Disconnecting Jetty Client...");
		}
		if (mConnector != null) {
			// inherited BaseConnector.stopConnector
			// calls mEngine connector stopped
			mConnector.stopConnector(CloseReason.SERVER);
			mEngine.removeConnector(mConnector);
		}
	}
}
