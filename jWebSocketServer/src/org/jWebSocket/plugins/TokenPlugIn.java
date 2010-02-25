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

import org.jWebSocket.connectors.BaseConnector;
import org.jWebSocket.connectors.TokenConnector;
import org.jWebSocket.server.Token;

/**
 *
 * @author aschulze
 */
public class TokenPlugIn extends BasePlugIn {

	private String namespace = null;

	/**
	 *
	 * @param aConnector
	 */
	public void connectorStarted(BaseConnector aConnector) {
	}

	/**
	 *
	 * @param aConnector
	 * @param aToken
	 * @return
	 */
	public void processToken(PlugInResponse aAction, TokenConnector aConnector, Token aToken) {
	}

	@Override
	public void processData(PlugInResponse aAction, BaseConnector aConnector, Object aObject) {
		processToken(aAction, (TokenConnector) aConnector, (Token) aObject);
	}

	/**
	 *
	 * @param aConnector
	 */
	public void connectorTerminated(BaseConnector aConnector) {
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
}
