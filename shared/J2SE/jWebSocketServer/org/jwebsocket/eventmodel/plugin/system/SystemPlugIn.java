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
package org.jwebsocket.eventmodel.plugin.system;

import java.util.Map;
import org.jwebsocket.eventmodel.plugin.EventModelPlugIn;
import org.jwebsocket.eventmodel.event.system.GetPlugInAPI;
import javolution.util.FastMap;
import org.jwebsocket.eventmodel.api.IEventModelPlugIn;
import org.jwebsocket.eventmodel.event.WebSocketResponseEvent;
import org.jwebsocket.logging.Logging;
import org.apache.log4j.Logger;
import org.jwebsocket.eventmodel.event.WebSocketEventDefinition;

/**
 *
 ** @author kyberneees
 */
public class SystemPlugIn extends EventModelPlugIn {

	private static Logger mLog = Logging.getLogger(SystemPlugIn.class);

	@Override
	public void initialize() throws Exception {
	}

	public void processEvent(GetPlugInAPI aEvent, WebSocketResponseEvent aResponseEvent) throws Exception {
		String aPlugInId = aEvent.getArgs().getString("plugin_id");
		if (mLog.isDebugEnabled()) {
			mLog.debug(">> Exporting API for '" + aPlugInId + "' plugIn...");
		}

		IEventModelPlugIn plugIn = getEm().getPlugIn(aPlugInId);
		FastMap<String, Map<String, Object>> api = new FastMap<String, Map<String, Object>>();
		FastMap<String, Object> temp;
		WebSocketEventDefinition def = null;

		try {
			for (String key : plugIn.getClientAPI().keySet()) {
				String aEventId = getEm().getEventFactory().
						eventToString(plugIn.getClientAPI().get(key));

				/**
				 * Getting API events definition
				 */
				def = getEm().getEventFactory().getEventDefinitions().getDefinition(aEventId);

				temp = new FastMap<String, Object>();
				temp.put("type", aEventId);
				temp.put("isCacheEnabled", def.isCacheEnabled());
				temp.put("isSecurityEnabled", def.isSecurityEnabled());
				temp.put("cacheTime", def.getCacheTime());
				temp.put("roles", def.getRoles());
				temp.put("incomingArgsValidation", def.getIncomingArgsValidation());
				temp.put("outgoingArgsValidation", def.getOutgoingArgsValidation());
				api.put(key, temp);
			}

			aResponseEvent.getTo().add(aEvent.getConnector());
			//PlugIn id
			aResponseEvent.getArgs().setString("id", this.getId());
			//PlugIn API
			aResponseEvent.getArgs().setMap("api", api);

		} catch (Exception ex) {
			mLog.error(ex.toString(), ex);
		}
	}
}
