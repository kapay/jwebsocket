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

import org.apache.log4j.Logger;
import org.jWebSocket.api.IDataPacket;
import org.jWebSocket.config.Config;
import org.jWebSocket.plugins.IPlugIn;
import org.jWebSocket.api.IWebSocketConnector;
import org.jWebSocket.api.IWebSocketEngine;
import org.jWebSocket.packetProcessors.CSVProcessor;
import org.jWebSocket.packetProcessors.JSONProcessor;
import org.jWebSocket.plugins.TokenPlugInChain;
import org.jWebSocket.token.Token;
import org.jWebSocket.packetProcessors.XMLProcessor;

/**
 *
 * @author aschulze
 */
public class TokenServer extends BaseServer {

	private static Logger log = Logger.getLogger(TokenServer.class);
	private TokenPlugInChain plugInChain = null;

	/**
	 *
	 */
	public TokenServer() {
		super();
		plugInChain = new TokenPlugInChain(this);
	}

	/**
	 * removes a plugin from the plugin chain of the server.
	 * @param aPlugIn
	 */
	public void removePlugIn(IPlugIn aPlugIn) {
		if (plugInChain != null) {
			plugInChain.removePlugIn(aPlugIn);
		}
	}

	@Override
	public void engineStarted(IWebSocketEngine aEngine) {
		log.debug("Processing engine started...");
		plugInChain.engineStarted(aEngine);
	}

	@Override
	public void engineStopped(IWebSocketEngine aEngine) {
		log.debug("Processing engine stopped...");
		plugInChain.engineStopped(aEngine);
	}

	@Override
	public void connectorStarted(IWebSocketConnector aConnector) {
		log.debug("Processing connector started...");
		// notify plugins that a connector has started,
		// i.e. a client was sconnected.
		if (plugInChain != null) {
			plugInChain.connectorStarted(aConnector);
		}
	}

	@Override
	public void connectorStopped(IWebSocketConnector aConnector) {
		log.debug("Processing connector stopped...");
		// notify plugins that a connector has stopped,
		// i.e. a client was disconnected.
		if (plugInChain != null) {
			plugInChain.connectorStopped(aConnector);
		}
	}

	private Token packetToToken(IWebSocketConnector aConnector, IDataPacket aDataPacket) {
		String lSubProt = aConnector.getHeader().getSubProtocol(Config.SUB_PROT_DEFAULT);
		Token lToken = null;
		if (lSubProt.equals(Config.SUB_PROT_JSON)) {
			lToken = JSONProcessor.packetToToken(aDataPacket);
		} else if (lSubProt.equals(Config.SUB_PROT_CSV)) {
			lToken = CSVProcessor.packetToToken(aDataPacket);
		} else if (lSubProt.equals(Config.SUB_PROT_XML)) {
			lToken = XMLProcessor.packetToToken(aDataPacket);
		}
		return lToken;
	}

	@Override
	public void processPacket(IWebSocketEngine aEngine, IWebSocketConnector aConnector, IDataPacket aDataPacket) {
		Token lToken = packetToToken(aConnector, aDataPacket);
		if (lToken != null) {
			log.debug("Processing token '" + lToken.toString() + "'...");
			plugInChain.processToken(aConnector, lToken);
		} else {
			log.error("Packet could not be converted into token.");
		}
	}

	/**
	 *
	 * @param aConnector
	 * @param aToken
	 */
	public void sendToken(IWebSocketConnector aConnector, Token aToken) {
		String lSubProt = aConnector.getHeader().getSubProtocol(Config.SUB_PROT_DEFAULT);
		IDataPacket lPacket = null;
		if (lSubProt.equals(Config.SUB_PROT_JSON)) {
			lPacket = JSONProcessor.tokenToPacket(aToken);
		} else if (lSubProt.equals(Config.SUB_PROT_CSV)) {
			lPacket = CSVProcessor.tokenToPacket(aToken);
		} else if (lSubProt.equals(Config.SUB_PROT_XML)) {
			lPacket = XMLProcessor.tokenToPacket(aToken);
		}
		log.debug("Sending token '" + aToken.toString() + "'...");
		super.sendPacket(aConnector, lPacket);
	}

	/**
	 * creates a standard response 
	 * @param aInToken
	 * @return
	 */
	public Token createResponse(Token aInToken) {
		String lTokenId = aInToken.getString("utid");
		String lType = aInToken.getString("type");
		Token lResToken = new Token("result");
		lResToken.put("code", 0);
		lResToken.put("msg", "ok");
		if (lTokenId != null) {
			lResToken.put("utid", lTokenId);
		}
		if (lType != null) {
			lResToken.put("reqType", lType);
		}
		return lResToken;
	}

	/**
	 * @return the plugInChain
	 */
	public TokenPlugInChain getPlugInChain() {
		return plugInChain;
	}
}
