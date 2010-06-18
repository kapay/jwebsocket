//	---------------------------------------------------------------------------
//	jWebSocket - WebSocket Client Interface
//	Copyright (c) 2010 jWebSocket.org, Alexander Schulze, Innotrade GmbH
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

package org.jwebsocket.api;

import org.jwebsocket.kit.WebSocketException;

/**
 *
 * @author aschulze
 */
public interface WebSocketClient {

	/**
	 *
	 * @param aURL
	 * @throws WebSocketException
	 */
	public void open(String aURL) throws WebSocketException;
	/**
	 *
	 * @param aData
	 * @param aEncoding
	 * @throws WebSocketException
	 */
	public void send(String aData, String aEncoding) throws WebSocketException;
	/**
	 *
	 * @param aData
	 * @param aEncoding
	 * @throws WebSocketException
	 */
	public void received(String aData, String aEncoding) throws WebSocketException;
	/**
	 *
	 * @param aData
	 * @throws WebSocketException
	 */
	public void send(byte[] aData) throws WebSocketException;
	/**
	 *
	 * @param aData
	 * @throws WebSocketException
	 */
	public void received(byte[] aData) throws WebSocketException;
	/**
	 *
	 * @throws WebSocketException
	 */
	public void close() throws WebSocketException;

	/**
	 *
	 * @throws WebSocketException
	 */
	public boolean isConnected();
}
