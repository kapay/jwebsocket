//	---------------------------------------------------------------------------
//	jWebSocket - Java WebSocket Token Client
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
package org.jWebSocket.client;

import org.jwebsocket.api.WebSocketPaket;
import org.jwebsocket.token.Token;

/**
 *
 * @author aschulze
 */
public class TokenClient extends TCPClient {

	/**
	 *
	 * @param aHost
	 * @param aPort
	 */
	public TokenClient(String aHost, int aPort) {
		super(aHost, aPort);
	}

	/**
	 *
	 * @param aToken
	 */
	public void sendToken(Token aToken) {

	}

	/**
	 *
	 * @param aToken
	 */
	public void processToken(Token aToken) {

	}

	@Override
	public void processPacket(WebSocketPaket aPacket) {

		
	}
}
