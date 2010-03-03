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

import java.net.InetAddress;
import org.jWebSocket.kit.Header;

/**
 *
 * @author aschulze
 */
public interface WebSocketConnector {

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

	/**
	 *
	 * @param aKey
	 * @return
	 */
	Object getVar(String aKey);

	/**
	 *
	 * @param aKey
	 * @param aValue
	 */
	void setVar(String aKey, Object aValue);

	/**
	 *
	 * @param aKey
	 * @return
	 */
	Boolean getBoolean(String aKey);

	/**
	 *
	 * @param aKey
	 * @return
	 */
	boolean getBool(String aKey);

	/**
	 *
	 * @param aKey
	 * @param aValue
	 */
	void setBoolean(String aKey, Boolean aValue);

	/**
	 *
	 * @param aKey
	 * @return
	 */
	String getString(String aKey);

	/**
	 *
	 * @param aKey
	 * @param aValue
	 */
	void setString(String aKey, String aValue);

	/**
	 *
	 * @param aKey
	 * @return
	 */
	Integer getInteger(String aKey);

	/**
	 *
	 * @param aKey
	 * @param aValue
	 */
	void setInteger(String aKey, Integer aValue);

	/**
	 * 
	 * @param aKey
	 */
	void removeVar(String aKey);

	/**
	 * 
	 * @return
	 */
	String generateUID();

	/**
	 *
	 * @return
	 */
	int getRemotePort();

	/**
	 *
	 * @return
	 */
	InetAddress getRemoteHost();

}
