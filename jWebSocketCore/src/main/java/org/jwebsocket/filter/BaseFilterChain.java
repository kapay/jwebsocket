//	---------------------------------------------------------------------------
//	jWebSocket - BaseFilterChain Implementation
//	Copyright (c) 2010 Alexander Schulze, Innotrade GmbH
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
package org.jwebsocket.filter;

import java.util.List;
import javolution.util.FastList;
import org.apache.log4j.Logger;
import org.jwebsocket.kit.FilterResponse;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketFilter;
import org.jwebsocket.api.WebSocketFilterChain;
import org.jwebsocket.api.WebSocketPaket;
import org.jwebsocket.api.WebSocketServer;
import org.jwebsocket.logging.Logging;

/**
 *
 * @author aschulze
 */
public class BaseFilterChain implements WebSocketFilterChain {

	private static Logger log = Logging.getLogger(BaseFilterChain.class);
	private FastList<WebSocketFilter> filters = new FastList<WebSocketFilter>();
	private WebSocketServer server = null;

	/**
	 *
	 * @param aServer
	 */
	public BaseFilterChain(WebSocketServer aServer) {
		server = aServer;
	}

	/**
	 * @return the server
	 */
	public WebSocketServer getServer() {
		return server;
	}

	@Override
	public void addFilter(WebSocketFilter aFilter) {
		filters.add(aFilter);
		aFilter.setFilterChain(this);
	}

	@Override
	public void removeFilter(WebSocketFilter aFilter) {
		filters.remove(aFilter);
		aFilter.setFilterChain(null);
	}

	/**
	 *
	 * @return
	 */
	@Override
	public List<WebSocketFilter> getFilters() {
		return filters;
	}

	@Override
	public FilterResponse processPacketIn(WebSocketConnector aConnector, WebSocketPaket aPacket) {
		FilterResponse lResponse = new FilterResponse();
		for (WebSocketFilter lFilter : filters) {
			lFilter.processPacketIn(lResponse, aConnector, aPacket);
			if (lResponse.isRejected()) {
				break;
			}
		}
		return lResponse;
	}

	@Override
	public FilterResponse processPacketOut(WebSocketConnector aSource, WebSocketConnector aTarget, WebSocketPaket aPacket) {
		FilterResponse lResponse = new FilterResponse();
		for (WebSocketFilter lFilter : filters) {
			lFilter.processPacketOut(lResponse, aSource, aTarget, aPacket);
			if (lResponse.isRejected()) {
				break;
			}
		}
		return lResponse;
	}
}
