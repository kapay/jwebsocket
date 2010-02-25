//	---------------------------------------------------------------------------
//	jWebSocket - Listener Chain for incoming requests (per server)
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
package org.jWebSocket.plugins;

import org.jWebSocket.connectors.BaseConnector;
import javolution.util.FastList;

/**
 *
 * @author aschulze
 */
public class PlugInChain extends FastList<BasePlugIn> {

	/**Plug
	 *
	 * @param aConnector
	 */
	public void connectorStarted(BaseConnector aConnector) {
		for (int i = 0; i < size(); i++) {
			get(i).connectorStarted(aConnector);
		}
	}

	/**
	 *
	 * @param aConnector
	 * @param aObject
	 * @return
	 */
	public Object processData(BaseConnector aConnector, Object aObject) {
		PlugInResponse lPlugResp = new PlugInResponse();
		for (int i = 0; i < size(); i++) {
			get(i).processData(lPlugResp, aConnector, aObject);
			if (lPlugResp.isChainAborted()) {
				break;
			}
		}
		return null;
	}

	/**
	 *
	 * @param aConnector
	 */
	public void connectorTerminated(BaseConnector aConnector) {
		for (int i = 0; i < size(); i++) {
			get(i).connectorTerminated(aConnector);
		}
	}
}
