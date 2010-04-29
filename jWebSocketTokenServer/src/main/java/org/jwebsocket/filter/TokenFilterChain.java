//	---------------------------------------------------------------------------
//	jWebSocket - TokenFilterChain Implementation
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
package org.jwebsocket.filter;

import org.jwebsocket.api.FilterResponse;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketFilter;
import org.jwebsocket.api.WebSocketPaket;
import org.jwebsocket.token.Token;

/**
 *
 * @author aschulze
 */
public class TokenFilterChain extends BaseFilterChain {

	@Override
	public void addFilter(WebSocketFilter aFilter) {
	}

	@Override
	public void removeFilter(WebSocketFilter aFilter) {
	}

	@Override
	public FilterResponse processPacketIn(WebSocketConnector aConnector, WebSocketPaket aDataPacket) {
		return null;
	}

	@Override
	public FilterResponse processPacketOut(WebSocketConnector aConnector, WebSocketPaket aDataPacket) {
		return null;
	}

	public FilterResponse processTokenIn(WebSocketConnector aConnector, Token aToken) {
		return null;
	}

	public FilterResponse processTokenOut(WebSocketConnector aConnector, Token aToken) {
		return null;
	}
}
