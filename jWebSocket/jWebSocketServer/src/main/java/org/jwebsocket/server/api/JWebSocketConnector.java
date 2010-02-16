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
package org.jwebsocket.server.api;

/**
 * @author Puran Singh
 * @version $Id$
 *
 */
public interface JWebSocketConnector {
	
	/**
	 * method that sends the string data to the output 
	 * @param aData the string data to send
	 */
	void sendString(String aData);
	
	/**
	 * method that is invoked when the jWebSocket server sends the handshake 
	 * response to the client. The connector clients that implement this method 
	 * can perform any action before the WebSocket handshake response is sent.
	 */
	void onHandShakeResponse();
	
	void clientThreadStarted();
	
	void headerReceived(String aHeader);
	
	void headerParsed();
	
	void handshakeSent();
	
	void timeoutExceeded();
	
	void clientClosed();
	
	void dataReceived(String line);
	
	void clientThreadStopped();
	
	void start();
	
	/**
	 * callback method which is invoked when the connector is terminated 
	 * naturally or unnaturally. 
	 */
	void terminate();
	/**
	 * Returns {@code true} if the client connector is running, {@code false} 
	 * otherwise.
	 * @return true or false based on the status of the connector.
	 */
	boolean isAlive();
	
	String getOrigin();
	
	String getLocation();
}
