//	---------------------------------------------------------------------------
//	jWebSocket - Copyright (c) 2010 jwebsocket.org
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
package org.jWebSocket.api;

import org.jWebSocket.kit.Header;

/**
 *
 * @author aschulze
 */
public interface IWebSocketConnector {

	/**
	 * starts and initializes the connector.
	 */
	void startConnector();

	/**
	 * stops and cleans up the connector.
	 */
	void stopConnector();

	/**
	 * processes an incoming datapacket from a WebSocket client.
	 * @param aDataPacket
	 */
	void processPacket(IDataPacket aDataPacket);

	/**
	 * sends a datapacket to a WebSocket client.
	 * @param aDataPacket
	 */
	void sendPacket(IDataPacket aDataPacket);

	/**
	 *
	 * @return
	 */
	Header getHeader();

	/**
	 *
	 * @param aHeader
	 */
	void setHeader(Header aHeader);

	Object getVar(String aKey);
	void setVar(String aKey, Object aValue);

	boolean getBoolean(String aKey);
	void setBoolean(String aKey, boolean aValue);

	String getString(String aKey);
	void setString(String aKey, String aValue);

	int getInt(String aKey);
	void setInt(String aKey, int aValue);

}
