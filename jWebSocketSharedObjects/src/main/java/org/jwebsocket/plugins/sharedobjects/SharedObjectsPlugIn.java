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

import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.Config;
import org.jwebsocket.plugins.PlugInResponse;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.token.Token;

/**
 * Pending...
 * @author aschulze
 */
public class SharedObjectsPlugIn extends TokenPlugIn {

	private static Logger log = Logger.getLogger(SharedObjectsPlugIn.class);
	// if namespace changed update client plug-in accordingly!
	private String NS_SHARED_OBJ = Config.NS_BASE + ".plugins.sharedObjs";
	private String NS_SHARED_LISTS = Config.NS_BASE + ".plugins.sharedLists";
	private String NS_SHARED_SETS = Config.NS_BASE + ".plugins.sharedSets";
	private String NS_SHARED_MAPS = Config.NS_BASE + ".plugins.sharedMaps";
	private SharedLists sharedLists = new SharedLists();
	private SharedSets sharedSets = new SharedSets();
	private SharedMaps sharedMaps = new SharedMaps();

	/**
	 *
	 */
	public SharedObjectsPlugIn() {
		// specify default name space
		this.setNamespace(NS_SHARED_OBJ);
	}

	@Override
	public void processToken(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();
		String lID = aToken.getString("id");

		Token lResponse = getServer().createResponse(aToken);
		boolean doRespond = true;

		if (lType != null && (lNS == null || lNS.equals(NS_SHARED_LISTS))) {
			if (lType.equals("create")) {
				sharedLists.create(lResponse, lID);
			} else if (lType.equals("clear")) {
				sharedLists.clear(lResponse, lID);
			} else if (lType.equals("get")) {
				sharedLists.get(lResponse, lID, aToken.getInteger("index", -1));
			} else if (lType.equals("add")) {
				sharedLists.add(lResponse, lID, aToken.getString("data"));
			} else if (lType.equals("remove")) {
				sharedLists.remove(lResponse, lID, aToken.getInteger("index", -1));
			} else if (lType.equals("destroy")) {
				sharedLists.destroy(lResponse, lID);
			} else {
				doRespond = false;
			}
		} else {
			doRespond = false;
		}
		if (doRespond) {
			getServer().sendToken(aConnector, lResponse);
		}
	}
}
