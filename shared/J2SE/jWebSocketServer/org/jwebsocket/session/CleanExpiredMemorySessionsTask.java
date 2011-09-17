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

import java.util.Iterator;
import java.util.TimerTask;
import org.jwebsocket.api.IBasicStorage;
import org.jwebsocket.storage.memory.MemoryStorage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author kyberneees
 */
public class CleanExpiredMemorySessionsTask extends TimerTask {

	private IBasicStorage<String, Object> sessionIdsTrash;
	private static Log logger = LogFactory.getLog(CleanExpiredMemorySessionsTask.class);

	public CleanExpiredMemorySessionsTask(IBasicStorage<String, Object> sessionIdsTrash) {
		this.sessionIdsTrash = sessionIdsTrash;
	}

	@Override
	public void run() {
		if (logger.isDebugEnabled()) {
			logger.debug(">> Cleaning expired sessions ...");
		}

		Iterator<String> keys = sessionIdsTrash.keySet().iterator();
		while (keys.hasNext()) {
			String k = keys.next();

			if (MemoryStorage.getContainer().containsKey(k) && ((Long) (sessionIdsTrash.get(k)) < System.currentTimeMillis())) {
				MemoryStorage.getContainer().remove(k);
			}
		}
	}
}
