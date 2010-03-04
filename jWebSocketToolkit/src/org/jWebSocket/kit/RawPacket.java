//	---------------------------------------------------------------------------
//	jWebSocket - Raw Data Packet Implementation
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

import java.io.UnsupportedEncodingException;
import org.jWebSocket.api.WebSocketPaket;

/**
 * Implements the low level data packets which are interchanged between
 * client and server. Data packets do not have a special format at this
 * communication level.
 * @author aschulze
 */
public class RawPacket implements WebSocketPaket {

	byte[] data = null;

	/**
	 *
	 * @param aByteArray
	 */
	public RawPacket(byte[] aByteArray) {
		setByteArray(aByteArray);
	}

	/**
	 *
	 * @param aString
	 */
	public RawPacket(String aString) {
		setString(aString);
	}

	/**
	 *
	 * @param aString
	 * @param aEncoding
	 * @throws UnsupportedEncodingException
	 */
	public RawPacket(String aString, String aEncoding)
		throws UnsupportedEncodingException {
		setString(aString, aEncoding);
	}

	public void setByteArray(byte[] aByteArray) {
		data = aByteArray;
	}

	public void setString(String aString) {
		data = aString.getBytes();
	}

	public void setString(String aString, String aEncoding)
		throws UnsupportedEncodingException {
		data = aString.getBytes(aEncoding);
	}

	public void setUTF8(String aString) {
		try {
			data = aString.getBytes("UTF-8");
		} catch (UnsupportedEncodingException ex) {
			// ignore exception here
		}
	}

	public byte[] getByteArray() {
		return data;
	}

	public String getString() {
		return new String(data);
	}

	public String getString(String aEncoding)
		throws UnsupportedEncodingException {
		return new String(data, aEncoding);
	}

	public String getUTF8() {
		try {
			return new String(data, "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			return null;
		}
	}
}
