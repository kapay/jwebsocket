//	---------------------------------------------------------------------------
//	jWebSocket - Copyright (c) 2010 Innotrade GmbH
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
package org.jwebsocket.api;

import org.jwebsocket.kit.WebSocketException;

/**
 * Main API Interface for Java client
 * Based on http://weberknecht.googlecode.com code
 * @author agali
 */
public interface WebSocket {

   public void setEventHandler(WebSocketEventHandler eventHandler);
	
   public WebSocketEventHandler getEventHandler();

   public void connect() throws WebSocketException;
	
   public void send(String data) throws WebSocketException;

   public void send(byte[] data) throws WebSocketException;

   public void close() throws WebSocketException;

   public boolean isConnected();

}
