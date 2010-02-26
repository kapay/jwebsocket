//	---------------------------------------------------------------------------
//	jWebSocket - WebSocket Token Server (abstract)
//	Copyright (c) 2010 Alexander Schulze, Innotrade GmbH
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
package org.jWebSocket.server;

import org.jWebSocket.api.IWebSocketEngine;
import org.jWebSocket.kit.Header;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jWebSocket.config.Config;
import org.jWebSocket.processors.CSVProcessor;
import org.jWebSocket.processors.JSONProcessor;
import org.jWebSocket.processors.TokenConnector;
import org.jWebSocket.processors.XMLProcessor;
import org.jWebSocket.plugins.PlugInChain;
import org.jWebSocket.connectors.BaseConnector;
import org.apache.log4j.Logger;

/**
 *
 * @author aschulze
 */
public class TokenServer extends BaseServer {

	private static Logger log = Logger.getLogger(TokenServer.class);

	/**
	 *
	 * @param aPort
	 * @param aSessionTimeout
	 * @param aListeners
	 */
	public TokenServer(IWebSocketEngine aEngine) {
		super(aEngine);
	}

	@Override
	protected void instantiateServer(int aPort, int aSessionTimeout) {
		log.info("Instantiating jWebSocket server...");
		try {
			super.instantiateServer(aPort, aSessionTimeout);
		} catch (Exception ex) {
			log.error("Exception on instantiating jWebSocket server socket: " + ex.getMessage());
		}
	}

	@Override
	public BaseConnector createConnector(Socket aClientSocket, Header aHeader) {
		String lSubProt = aHeader.getSubProtocol(Config.SUB_PROT_JSON);
		log.info("Instantiating " + lSubProt + " connector...");
		if (lSubProt.equalsIgnoreCase(Config.SUB_PROT_XML)) {
			return new XMLProcessor(this, aClientSocket, aHeader);
		} else if (lSubProt.equalsIgnoreCase(Config.SUB_PROT_CSV)) {
			return new CSVProcessor(this, aClientSocket, aHeader);
		} else {
			return new JSONProcessor(this, aClientSocket, aHeader);
		}
	}

	@Override
	public void serverStarted() {
		super.serverStarted();
		log.info("jWebSocket server started.");
	}

	@Override
	public void serverStopped() {
		super.serverStopped();
		log.info("jWebSocket server stopped.");
	}

	@Override
	public void connectorStarted(BaseConnector aClient) {
		super.connectorStarted(aClient);
		log.debug(
				"New client connected "
				+ aClient.getClientSocket().getInetAddress().getHostAddress()
				+ "@" + aClient.getClientSocket().getPort()
				+ ".");
	}

	@Override
	public void connectorTerminated(BaseConnector aClient) {
		super.connectorTerminated(aClient);
		log.debug("Client disconnected.");
	}

	/**
	 *
	 * @param aUsername
	 * @return
	 */
	public TokenConnector getConnectorByUsername(String aUsername) {
		List<BaseConnector> clients = getClients();
		Iterator<BaseConnector> lIterator = clients.iterator();
		while (lIterator.hasNext()) {
			TokenConnector lConnector = (TokenConnector) lIterator.next();
			if (lConnector.isLoggedIn()
					&& lConnector.getUsername().equals(aUsername)) {
				return lConnector;
			}
		}
		return null;
	}

	/**
	 *
	 * @param aSessionId
	 * @return
	 */
	public TokenConnector getConnectorBySessionId(String aSessionId) {
		List<BaseConnector> clients = getClients();
		Iterator<BaseConnector> lIterator = clients.iterator();
		while (lIterator.hasNext()) {
			TokenConnector lConnector = (TokenConnector) lIterator.next();
			if (aSessionId.equals(lConnector.getSessionId())) {
				return lConnector;
			}
		}
		return null;
	}

	public List<TokenConnector> getClients(String aPool, int aMode) {
		List<BaseConnector> lAll = super.getClients();
		List<TokenConnector> lRes = new ArrayList<TokenConnector>();
		TokenConnector lConn = null;
		for (BaseConnector lItem : lAll) {
			lConn = (TokenConnector) lItem;
			if ((aPool == null || aPool.equals(lConn.getPool()))
					&& ((aMode == 0
					|| (aMode == 1 && lConn.isLoggedIn())
					|| (aMode == 2 && !lConn.isLoggedIn())))) {
				lRes.add(lConn);
			}
		}
		return lRes;
	}
}
