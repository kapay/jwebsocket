//	---------------------------------------------------------------------------
//	jWebSocket - Shared Objects Plug-In
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
package org.jwebsocket.plugins.sharedobjects;

import java.util.Arrays;
import javolution.util.FastList;
import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketConstants;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.api.PlugInResponse;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.token.Token;

/**
 * Pending...
 * @author aschulze
 */
public class SharedObjectsPlugIn extends TokenPlugIn {

	private static Logger log = Logging.getLogger(SharedObjectsPlugIn.class);
	// if namespace is changed update client plug-in accordingly!
	private String NS_SHARED_OBJECTS = JWebSocketConstants.NS_BASE + ".plugins.sharedObjs";
	private SharedObjects sharedObjects = new SharedObjects();
	// if data types are changed update client plug-in accordingly!
	private FastList DATA_TYPES = new FastList(Arrays.asList(new String[]{
				"number", "string", "boolean", "object",
				"set", "list", "map", "table"}));

	/**
	 *
	 */
	public SharedObjectsPlugIn() {
		// specify default name space
		this.setNamespace(NS_SHARED_OBJECTS);
	}

	@Override
	public void processToken(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();
		String lID = aToken.getString("id");
		String lDataType = aToken.getString("datatype");
		String lValue = aToken.getString("value");

		Token lResponse = getServer().createResponse(aToken);
		boolean doRespond = true;

		if (lDataType == null || !DATA_TYPES.contains(lDataType)) {
			lResponse.put("code", -1);
			lResponse.put("msg", "invalid datatype '" + lDataType + "'");
			getServer().sendToken(aConnector, lResponse);
			return;
		}

		if (lType != null && (lNS == null || lNS.equals(NS_SHARED_OBJECTS))) {
			if (lType.equals("create")) {
				sharedObjects.put(lID, lValue);
			} else if (lType.equals("destroy")) {
				sharedObjects.remove(lID);
			} else if (lType.equals("get")) {
				sharedObjects.get(lID);
			} else if (lType.equals("put")) {
				sharedObjects.put(lID, lValue);
			} /*
			else if (lType.equals("clear")) {
			sharedObjects.clear(lResponse, lID);
			} else if (lType.equals("get")) {
			sharedObjects.get(lResponse, lID, aToken.getInteger("index", -1));
			} else if (lType.equals("add")) {
			sharedObjects.add(lResponse, lID, aToken.getString("data"));
			} else if (lType.equals("remove")) {
			sharedObjects.remove(lResponse, lID, aToken.getInteger("index", -1));
			 */ 
			else {
				lResponse.put("code", -1);
				lResponse.put("msg", "invalid type '" + lType + "'");
			}
		} else {
			doRespond = false;
		}

		if (doRespond) {
			getServer().sendToken(aConnector, lResponse);
		}
	}
}
