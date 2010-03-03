//	---------------------------------------------------------------------------
//	jWebSocket - Data Packet
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
package org.jWebSocket.api;

import java.io.UnsupportedEncodingException;

/**
 *
 * @author aschulze
 */
public interface WebSocketPaket {

	/**
	 *
	 * @param aByteArray
	 */
	void setByteArray(byte[] aByteArray);

	/**
	 *
	 * @param aString
	 */
	void setString(String aString);

	/**
	 *
	 * @param aString
	 * @param aEncoding
	 * @throws UnsupportedEncodingException
	 */
	void setString(String aString, String aEncoding) throws UnsupportedEncodingException;

	/**
	 *
	 * @param aString
	 */
	void setUTF8(String aString);


	/**
	 *
	 * @return
	 */
	byte[] getByteArray();
	
	/**
	 *
	 * @return
	 */
	String getString();

	/**
	 *
	 * @param aEncoding
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	String getString(String aEncoding) throws UnsupportedEncodingException;

	/**
	 *
	 * @return
	 */
	String getUTF8();

}
