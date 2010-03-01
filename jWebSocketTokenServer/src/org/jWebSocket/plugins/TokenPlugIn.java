//	---------------------------------------------------------------------------
//	jWebSocket - Wrapper for Token based PlugIns (Convenience Class)
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
package org.jWebSocket.plugins;

import org.jWebSocket.api.IDataPacket;
import org.jWebSocket.api.IWebSocketConnector;
import org.jWebSocket.api.IWebSocketEngine;
import org.jWebSocket.server.TokenServer;
import org.jWebSocket.token.Token;

/**
 *
 * @author aschulze
 */
public class TokenPlugIn extends BasePlugIn {

	private String namespace = null;

	public void engineStarted(IWebSocketEngine aEngine) {
	}

	public void engineStopped(IWebSocketEngine aEngine) {
	}

	/**
	 *
	 * @param aConnector
	 */
	public void connectorStarted(IWebSocketConnector aConnector) {
	}

	/**
	 *
	 * @param aConnector
	 * @param aToken
	 * @return
	 */
	public void processToken(PlugInResponse aResponse, IWebSocketConnector aConnector, Token aToken) {
	}

	@Override
	public void processPacket(PlugInResponse aResponse, IWebSocketConnector aConnector, IDataPacket aDataPacket) {
		//
	}

	/**
	 *
	 * @param aConnector
	 */
	public void connectorStopped(IWebSocketConnector aConnector) {
	}

	/**
	 * @return the namespace
	 */
	public String getNamespace() {
		return namespace;
	}

	/**
	 * @param namespace the namespace to set
	 */
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	/**
	 * @return the plugInChain
	 */
	@Override
	public TokenPlugInChain getPlugInChain() {
		return (TokenPlugInChain)super.getPlugInChain();
	}

	public TokenServer getServer() {
		TokenServer lServer = null;
		TokenPlugInChain plugInChain = getPlugInChain();
		if( plugInChain != null ) {
			lServer = plugInChain.getServer();
		}
		return lServer;
	}


}
