//	---------------------------------------------------------------------------
//	jWebSocket - WebSocket Exception
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
package org.jWebSocket.kit;

/**
 * Exception class to represent JWebSocketServer related exception
 * @author Puran Singh
 * @version $Id: JWebSocketServerException.java 61 2010-02-16 07:20:51Z mailtopuran $
 *
 */
public class WebSocketException extends Exception {

	/**
	 * creates the exception with given message
	 * @param error the error messae
	 */
	public WebSocketException(String error) {
		super(error);
	}
	
	/**
	 * creates the exception with given message
	 * @param error the error messae
	 * @param throwable the cause 
	 */
	public WebSocketException(String error, Throwable throwable) {
		super(error, throwable);
	}

	private static final long serialVersionUID = 1L;
	
}
