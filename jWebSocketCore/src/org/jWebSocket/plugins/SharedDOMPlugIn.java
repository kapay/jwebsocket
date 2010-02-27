//	---------------------------------------------------------------------------
//	jWebSocket - Shared DOM Plug-In
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

import org.apache.log4j.Logger;
import org.jWebSocket.config.Config;
import org.jWebSocket.connectors.BaseConnector;
import org.jWebSocket.connectors.TokenConnector;
import org.jWebSocket.token.Token;

/**
 * Pending...
 * @author aschulze
 */
public class SharedDOMPlugIn extends TokenPlugIn {

	private static Logger log = Logger.getLogger(SharedDOMPlugIn.class);

	// if namespace changed update client plug-in accordingly!
	private String NS_SHAREDDOM_DEFAULT = Config.NS_BASE + ".plugins.sharedDOM";

	/**
	 *
	 */
	public SharedDOMPlugIn() {
		// specify default name space
		this.setNamespace(NS_SHAREDDOM_DEFAULT);
	}

	@Override
	public void connectorStarted(BaseConnector aConnector) {
	}

	@Override
	public void processToken(PlugInResponse aAction, TokenConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();

		if (lType != null && (lNS == null || lNS.equals(getNamespace()))) {
			// remote procedure call
			if (lType.equals("...")) {
			}
		}
	}

	@Override
	public void connectorTerminated(BaseConnector aConnector) {
	}

}
