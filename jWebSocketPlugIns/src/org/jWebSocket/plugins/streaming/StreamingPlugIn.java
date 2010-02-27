//	---------------------------------------------------------------------------
//	jWebSocket - StreamingPlugIn Plug-In
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
package org.jWebSocket.plugins.streaming;

import org.apache.log4j.Logger;
import org.jWebSocket.config.Config;
import org.jWebSocket.connectors.BaseConnector;
import org.jWebSocket.connectors.TokenConnector;
import org.jWebSocket.kit.Token;
import org.jWebSocket.plugins.KeepAlivePlugIn;
import org.jWebSocket.plugins.PlugInResponse;
import org.jWebSocket.plugins.TokenPlugIn;

/**
 *
 * @author aschulze
 */
public class StreamingPlugIn extends TokenPlugIn {

	private static Logger log = Logger.getLogger(KeepAlivePlugIn.class);
	private String NS_STREAMING_DEFAULT = Config.NS_BASE + ".plugins.streaming";

	/**
	 *
	 */
	public StreamingPlugIn() {
		// specify default name space for keep alive plugin
		this.setNamespace(NS_STREAMING_DEFAULT);
	}

	@Override
	public void connectorStarted(BaseConnector aConnector) {
	}

	@Override
	public void processToken(PlugInResponse aAction, TokenConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();

		if (lType != null && (lNS == null || lNS.equals(getNamespace()))) {
			if (lType.equals("register")) {
				//
			} else if (lType.equals("unregister")) {
				//
			}
		}
	}

	@Override
	public void connectorTerminated(BaseConnector aConnector) {
	}

}
