//	---------------------------------------------------------------------------
//	jWebSocket - FilterChain API
//	Copyright (c) 2010 jWebSocket.org, Alexander Schulze, Innotrade GmbH
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
package org.jwebsocket.api;

import java.util.List;
import org.jwebsocket.kit.FilterResponse;

/**
 *
 * @author aschulze
 */
public interface WebSocketFilterChain {

	void addFilter(WebSocketFilter aFilter);
	void removeFilter(WebSocketFilter aFilter);
	List<WebSocketFilter> getFilters();

	FilterResponse processPacketIn(WebSocketConnector aSource, WebSocketPaket aPacket);
	FilterResponse processPacketOut(WebSocketConnector aSource, WebSocketConnector aTarget, WebSocketPaket aPacket);

}