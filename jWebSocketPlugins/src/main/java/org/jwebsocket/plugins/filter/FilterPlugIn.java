//	---------------------------------------------------------------------------
//	jWebSocket - Filter Plug-In
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
package org.jwebsocket.plugins.filter;

import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketConstants;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.token.Token;

/**
 * Provides mechanisms to filter or manipulate packets before they are passed
 * to the plug-ins.
 * @author aschulze
 */
public class FilterPlugIn extends TokenPlugIn {

	private static Logger log = Logging.getLogger(FilterPlugIn.class);
	private String NS_FILTER = JWebSocketConstants.NS_BASE + ".plugins.filter";

	/**
	 *
	 */
	public FilterPlugIn() {
		// specify default name space
		this.setNamespace(NS_FILTER);
	}

	@Override
	public void processToken(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();

		// TODO: implement reasonable conditions here!
		if (false) {
			aResponse.abortChain();
		}
	}
}
