//	---------------------------------------------------------------------------
//	jWebSocket - Copyright (c) 2010 jwebsocket.org
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
//	more details.
//	You should have received a copy of the GNU General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/>.
//	---------------------------------------------------------------------------
package org.jwebsocket.netty.connectors;

import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketPaket;
import org.jwebsocket.connectors.BaseConnector;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.netty.engines.NettyEngineHandler;

/**
 * Netty based implementation of the {@code BaseConnector}. 
 * @author puran
 * @version $Id$
 */
public class NettyConnector extends BaseConnector {

	private static Logger log = Logging.getLogger(NettyConnector.class);
	
	private NettyEngineHandler handler = null;
	/**
	 * The private constructor, netty connector objects are created using 
	 * static factory method:
	 * <tt>getNettyConnector({@code WebSocketEngine}, {@code ChannelHandlerContext})</tt>
	 * 
	 * @param theEngine
	 *            the websocket engine object
	 * @param theHandlerContext
	 *            the netty engine handler context
	 */
	public NettyConnector(WebSocketEngine theEngine,
			NettyEngineHandler theHandler) {
		super(theEngine);
		this.handler = theHandler;
	}

	@Override
	public void startConnector() {
		if (log.isDebugEnabled()) {
			log.debug("Starting Netty connector...");
		}
		if (log.isInfoEnabled()) {
			log.info("Started Netty connector on port.");
		}
	}

	@Override
	public void stopConnector(CloseReason aCloseReason) {
		if (log.isDebugEnabled()) {
			log.debug("Stopping Netty connector (" + aCloseReason.name()
					+ ")...");
		}
		// TODO: Do we need to wait here? At least optionally?
		if (log.isInfoEnabled()) {
			log.info("Stopped Netty connector (" + aCloseReason.name()
					+ ") on port.");
		}
	}

	@Override
	public void processPacket(WebSocketPaket aDataPacket) {
		// forward the data packet to the engine
		getEngine().processPacket(this, aDataPacket);
	}

	@Override
	public void sendPacket(WebSocketPaket aDataPacket) {
	}
}
