//	---------------------------------------------------------------------------
//	jWebSocket - WebSocket Tools
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
package org.jWebSocket.util;

import java.security.MessageDigest;
import java.util.Formatter;

/**
 *
 * @author aschulze
 */
public class Tools {

	public static String getMD5(String aMsg) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
			byte[] lBufSource = aMsg.getBytes("UTF-8");
			byte[] lBufTarget = md.digest(lBufSource);
			Formatter formatter = new Formatter();
			for (byte b : lBufTarget) {
				formatter.format("%02x", b);
			}
			return (formatter.toString());
		} catch (Exception ex) {
			// log.error("getMD5: " + ex.getMessage());
			System.out.println("getMD5: " + ex.getMessage());
		}
		return null;
	}

}
