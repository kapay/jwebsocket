//  ---------------------------------------------------------------------------
//  jWebSocket - EventsPlugIn
//  Copyright (c) 2010 Innotrade GmbH, jWebSocket.org
//  ---------------------------------------------------------------------------
//  This program is free software; you can redistribute it and/or modify it
//  under the terms of the GNU Lesser General Public License as published by the
//  Free Software Foundation; either version 3 of the License, or (at your
//  option) any later version.
//  This program is distributed in the hope that it will be useful, but WITHOUT
//  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//  more details.
//  You should have received a copy of the GNU Lesser General Public License along
//  with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//  ---------------------------------------------------------------------------
package org.jwebsocket.eventmodel.filter.cache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.jwebsocket.eventmodel.filter.EventModelFilter;
import org.jwebsocket.eventmodel.event.WebSocketEvent;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.eventmodel.api.IListener;
import org.jwebsocket.eventmodel.event.WebSocketEventDefinition;
import org.jwebsocket.eventmodel.event.filter.BeforeRouteResponseToken;
import org.jwebsocket.eventmodel.event.filter.ResponseFromCache;
import org.jwebsocket.eventmodel.exception.CachedResponseException;
import org.jwebsocket.eventmodel.observable.ResponseEvent;
import org.jwebsocket.token.Token;

/**
 *
 * @author kyberneees
 */
public class CacheFilter extends EventModelFilter implements IListener {

	private CacheManager cacheManager;
	private String cacheNamespace;

	@Override
	public void beforeCall(WebSocketConnector aConnector, WebSocketEvent aEvent) throws Exception {
		WebSocketEventDefinition def = getEm().getEventFactory().getEventDefinitions().getDefinition(aEvent.getId());
		if (!def.isCacheEnabled()) {
			return;
		}

		if (def.getCacheTime() > 0) {
			//Getting the cached response Token
			Element e = getCache(aEvent.getId()).get(aEvent.getRequestId());

			if (e != null && !e.isExpired()) {

				//Setting the correct "utid" value in the cached response token
				Token cachedToken = (Token) e.getObjectValue();
				Token newtoken = getEm().getParent().createResponse(aEvent.getArgs());
				cachedToken.setInteger("utid", newtoken.getInteger("utid"));

				//ResponseFromCache event notification
				ResponseFromCache event = new ResponseFromCache();
				event.setId("response.from.cache");
				event.setCachedResponse(cachedToken);
				event.setEvent(aEvent);
				notify(event, null, true);

				//Sending the cached response to the connector
				getEm().getParent().sendToken(aConnector, aConnector, cachedToken);

				//Stopping the filter chain
				throw new CachedResponseException();
			}
		}
	}

	/**
	 * @return the filter cache
	 */
	public Cache getCache(String aEventId) {
		if (!getCacheManager().cacheExists(cacheNamespace + aEventId)) {
			getCacheManager().addCache(cacheNamespace + aEventId);
		}

		return getCacheManager().getCache(cacheNamespace + aEventId);
	}

	public void processEvent(BeforeRouteResponseToken aEvent, ResponseEvent aResponseEvent) {
		if (aEvent.getEventDefinition().isCacheEnabled()
				&& aEvent.getEventDefinition().getCacheTime() > 0) {

			//Putting the response token in cache using the event cache time
			getCache(aEvent.getEventDefinition().getId()).put(new Element(
					aEvent.getRequestId(),
					aEvent.getArgs(),
					false,
					aEvent.getEventDefinition().getCacheTime(),
					aEvent.getEventDefinition().getCacheTime()));
		}
	}

	/**
	 * @return the cacheManager
	 */
	public CacheManager getCacheManager() {
		return cacheManager;
	}

	/**
	 * @param cacheManager the cacheManager to set
	 */
	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	/**
	 * @return the cacheNamespace
	 */
	public String getCacheNamespace() {
		return cacheNamespace;
	}

	/**
	 * @param cacheNamespace the cacheNamespace to set
	 */
	public void setCacheNamespace(String cacheNamespace) {
		this.cacheNamespace = cacheNamespace;
	}
}
