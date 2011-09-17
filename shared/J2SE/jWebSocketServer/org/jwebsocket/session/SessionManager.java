//	---------------------------------------------------------------------------
//	jWebSocket - Copyright (c) 2011 jwebsocket.org
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
package org.jwebsocket.session;

import java.util.Map;
import java.util.Random;
import javolution.util.FastMap;
import org.jwebsocket.api.IBasicStorage;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.IInitializable;
import org.jwebsocket.api.ISessionReconnectionManager;
import org.jwebsocket.api.IStorageProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jwebsocket.api.ISessionManager;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.api.WebSocketServerListener;
import org.jwebsocket.kit.WebSocketServerEvent;
import org.jwebsocket.util.Tools;

/**
 *
 * @author kyberneees
 */
public class SessionManager implements ISessionManager, IInitializable, WebSocketServerListener {

	private IStorageProvider storageProvider;
	private ISessionReconnectionManager reconnectionManager;
	private static Log logger = LogFactory.getLog(SessionManager.class);
	private Map<String, IBasicStorage<String, Object>> sessions;

	public ISessionReconnectionManager getReconnectionManager() {
		return reconnectionManager;
	}

	public void setReconnectionManager(ISessionReconnectionManager reconnectionManager) {
		this.reconnectionManager = reconnectionManager;
	}

	public IStorageProvider getStorageProvider() {
		return storageProvider;
	}

	public void setStorageProvider(IStorageProvider storageProvider) {
		this.storageProvider = storageProvider;
	}

	@Override
	public IBasicStorage<String, Object> getSession(WebSocketConnector aConnector) throws Exception {
		return getSession(aConnector.getSession().getSessionId());
	}

	@Override
	public IBasicStorage<String, Object> getSession(String aSessionId) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("Getting session for: " + aSessionId + "...");
		}

		if (sessions.containsKey(aSessionId)) {
			//Getting the local cached storage instance if exists
			return sessions.get(aSessionId);
		}

		if (reconnectionManager.isExpired(aSessionId)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Creating a blank storage for session: " + aSessionId + "...");
			}
			IBasicStorage<String, Object> s = storageProvider.getStorage(aSessionId);
			s.clear();
			sessions.put(aSessionId, s);

			return s;
		} else {
			//Avoid security holes 
			reconnectionManager.getReconnectionIndex().remove(aSessionId);
			//Recovered session, require to be removed from the trash
			reconnectionManager.getSessionIdsTrash().remove(aSessionId);

			IBasicStorage<String, Object> s = storageProvider.getStorage(aSessionId);
			sessions.put(aSessionId, s);

			return s;
		}
	}

	@Override
	public void initialize() throws Exception {
		sessions = new FastMap<String, IBasicStorage<String, Object>>();
	}

	@Override
	public void shutdown() throws Exception {
		sessions.clear();
	}

	@Override
	public void processClosed(WebSocketServerEvent aEvent) {
		//Allowing all connectors for a reconnection
		String sid = aEvent.getConnector().getSession().getSessionId();

		if (logger.isDebugEnabled()) {
			logger.debug("Putting the session: " + sid + ", in reconnection mode...");
		}

		synchronized (this) {
			//Removing the local cached storage instance. Free space if 
			//the client never gets reconnected
			sessions.remove(sid);
			reconnectionManager.putInReconnectionMode(sid);
		}
	}

	@Override
	public void processOpened(WebSocketServerEvent aEvent) {
		try {
			// set session id first, so that it can be processed in the connectorStarted
			// method
			Random lRand = new Random(System.nanoTime());

			//@TODO: if unique node id is passed check if already assigned in the
			// network and reject connect if so!

			if (logger.isDebugEnabled()) {
				logger.debug("Setting the session identifier: " + aEvent.getConnector().getId());
			}
			aEvent.getSession().setSessionId(
					Tools.getMD5(aEvent.getConnector().generateUID()
					+ "." + lRand.nextInt()));

			if (logger.isDebugEnabled()) {
				logger.debug("Creating the WebSocketSession persistent storage "
						+ "for connector: " + aEvent.getConnector().getId());
			}
			aEvent.getSession().setStorage((Map<String, Object>) (getSession(aEvent.getSessionId())));
		} catch (Exception ex) {
			/**
			 * @TODO try this with the ExceptionHandler
			 */
		}
	}

	@Override
	public void processPacket(WebSocketServerEvent aEvent, WebSocketPacket aPacket) {
	}
}
