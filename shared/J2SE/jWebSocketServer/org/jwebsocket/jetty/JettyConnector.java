//	---------------------------------------------------------------------------
//	jWebSocket - Jetty Connector
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

import java.net.InetAddress;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.eclipse.jetty.websocket.WebSocket.Outbound;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.async.IOFuture;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.connectors.BaseConnector;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.RawPacket;
import org.jwebsocket.kit.RequestHeader;
import org.jwebsocket.logging.Logging;

/**
 *
 * @author aschulze
 */
public class JettyConnector extends BaseConnector {

	private static Logger mLog = Logging.getLogger(JettyConnector.class);
	private boolean mIsRunning = false;
	private CloseReason mCloseReason = CloseReason.TIMEOUT;
	private Outbound mOutbound;
	private HttpServletRequest mRequest = null;
	private String mProtocol = null;

	/**
	 * creates a new TCP connector for the passed engine using the passed client
	 * socket. Usually connectors are instantiated by their engine only, not by
	 * the application.
	 *
	 * @param aEngine
	 * @param aClientSocket
	 */
	public JettyConnector(WebSocketEngine aEngine, HttpServletRequest aRequest,
			String aProtocol, Outbound aOutbound) {
		super(aEngine);
		mOutbound = aOutbound;
		mRequest = aRequest;
		mProtocol = aProtocol;


		Map<String, String> lArgs = new FastMap<String, String>();
		String lPath = (String) aRequest.getRequestURL().toString();
		if (mLog.isDebugEnabled()) {
			mLog.debug("Requesting '" + lPath
					+ "', " + (aProtocol != null ? "'" + aProtocol + "'" : "[w/o protocol]")
					+ "...");
		}

		// isolate search string
		String lSearchString = "";
		if (lPath != null) {
			int lPos = lPath.indexOf(JWebSocketCommonConstants.PATHARG_SEPARATOR);
			if (lPos >= 0) {
				lSearchString = lPath.substring(lPos + 1);
				if (lSearchString.length() > 0) {
					String[] lArgsArray =
							lSearchString.split(JWebSocketCommonConstants.ARGARG_SEPARATOR);
					for (int lIdx = 0; lIdx < lArgsArray.length; lIdx++) {
						String[] lKeyValuePair =
								lArgsArray[lIdx].split(JWebSocketCommonConstants.KEYVAL_SEPARATOR, 2);
						if (lKeyValuePair.length == 2) {
							lArgs.put(lKeyValuePair[0], lKeyValuePair[1]);
							if (mLog.isDebugEnabled()) {
								mLog.debug("arg" + lIdx + ": "
										+ lKeyValuePair[0] + "="
										+ lKeyValuePair[1]);
							}
						}
					}
				}
			}
		}

		RequestHeader lHeader = new RequestHeader();
		// set default sub protocol if none passed

		if (lArgs.get("prot") == null) {
			lArgs.put("prot", JWebSocketCommonConstants.WS_SUBPROT_DEFAULT);
		}

		// lArgs.put("prot", JWebSocketCommonConstants.WS_SUBPROT_DEFAULT);
		// lHeader.put("host", lRespMap.get("host"));
		// lHeader.put("origin", lRespMap.get("origin"));
		// lHeader.put("location", lRespMap.get("location"));

		// lHeader.put("path", lRespMap.get("path"));
		lHeader.put("searchString", lSearchString);
		lHeader.put("args", lArgs);
		setHeader(lHeader);
	}

	@Override
	public void startConnector() {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Starting Jetty connector...");
		}
		mIsRunning = true;

		if (mLog.isInfoEnabled()) {
			mLog.info("Started Jetty connector.");
		}

		// call connectorStarted method of engine
		getEngine().connectorStarted(this);
	}

	@Override
	public void stopConnector(CloseReason aCloseReason) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Stopping Jetty connector (" + aCloseReason.name() + ")...");
		}

		// call client stopped method of engine
		// (e.g. to release client from streams)
		// getEngine().connectorStopped(this, aCloseReason);

		mOutbound.disconnect();
		mCloseReason = aCloseReason;
		mIsRunning = false;
		if (mLog.isInfoEnabled()) {
			mLog.info("Stopped Jetty connector.");
		}
	}

	@Override
	public void processPacket(WebSocketPacket aDataPacket) {
		// forward the data packet to the engine
		// the engine forwards the packet to all connected servers
		getEngine().processPacket(this, aDataPacket);
	}

	@Override
	public synchronized void sendPacket(WebSocketPacket aDataPacket) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Sending packet '" + aDataPacket.getUTF8() + "'...");
		}

		try {
			if (aDataPacket.getFrameType() == RawPacket.FRAMETYPE_BINARY) {
				mOutbound.sendMessage((byte) 0, aDataPacket.getByteArray());
			} else {
				mOutbound.sendMessage(aDataPacket.getUTF8());
			}
		} catch (Exception lEx) {
			mLog.error(lEx.getClass().getSimpleName() + " sending data packet: " + lEx.getMessage());
		}
		if (mLog.isDebugEnabled()) {
			mLog.debug("Packet '" + aDataPacket.getUTF8() + "' sent.");
		}
	}

	@Override
	public IOFuture sendPacketAsync(WebSocketPacket aDataPacket) {
		throw new UnsupportedOperationException("Underlying connector:"
				+ getClass().getName() + " doesn't support asynchronous send operation");
	}

	@Override
	public String generateUID() {
		String lUID = getRemoteHost() + "@" + getRemotePort();
		return lUID;
	}

	@Override
	public int getRemotePort() {
		return mRequest.getRemotePort();
	}

	@Override
	public InetAddress getRemoteHost() {
		InetAddress lAddr;
		try {
			lAddr = InetAddress.getByName(mRequest.getRemoteAddr());
		} catch (Exception lEx) {
			lAddr = null;
		}
		return lAddr;
	}

	@Override
	public String toString() {
		// TODO: weird results like... '0:0:0:0:0:0:0:1:61130'... on JDK 1.6u19
		// Windows 7 64bit
		String lRes = getRemoteHost().getHostAddress() + ":" + getRemotePort();
		// TODO: don't hard code. At least use JWebSocketConstants field here.
		String lUsername = getString("org.jwebsocket.plugins.system.username");
		if (lUsername != null) {
			lRes += " (" + lUsername + ")";
		}
		return lRes;
	}
}
