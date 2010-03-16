//	---------------------------------------------------------------------------
//	jWebSocket - CSV Token Processor
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
package org.jwebsocket.packetProcessors;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketPaket;
import org.jwebsocket.kit.RawPacket;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.token.Token;

/**
 * converts CSV formatted data packets into tokens and vice versa.
 * @author aschulze
 */
public class CSVProcessor {

	private static Logger log = Logging.getLogger(CSVProcessor.class);

	/**
	 * converts a CSV formatted data packet into a token.
	 * @param aDataPacket
	 * @return
	 */
	public static Token packetToToken(WebSocketPaket aDataPacket) {
		Token lToken = new Token();
		try {
			String aData = aDataPacket.getString("UTF-8");
			String[] lItems = aData.split(",");
			for (int i = 0; i < lItems.length; i++) {
				String[] lKeyVal = lItems[i].split("=", 2);
				if (lKeyVal.length == 2) {
					String lVal = lKeyVal[1];
					if (lVal.length() <= 0) {
						lToken.put(lKeyVal[0], null);
					} else if (lVal.startsWith("\"") && lVal.endsWith("\"")) {
						// unescape commata by \x2C
						lVal = lVal.replace("\\x2C", ",");
						// unescape quotes by \x22
						lVal = lVal.replace("\\x22", "\"");
						lToken.put(lKeyVal[0], lVal.substring(1, lVal.length() - 1));
					} else {
						lToken.put(lKeyVal[0], lVal);
					}
				}
			}
		} catch (UnsupportedEncodingException ex) {
			log.error(ex.getClass().getSimpleName() + ": " + ex.getMessage());
		}
		return lToken;
	}

	private static String stringToCSV(String aString) {
		// escape commata by \x2C
		aString = aString.replace(",", "\\x2C");
		// escape quotes by \x22
		aString = aString.replace("\"", "\\x22");
		return ("\"" + aString + "\"");
	}

	private static String collectionToCSV(Collection<Object> aCollection) {
		String lRes = "";
		for (Object lItem : aCollection) {
			String llRes = objectToCSV(lItem);
			lRes += llRes + "|";
		}
		if (lRes.length() > 1) {
			lRes = lRes.substring(0, lRes.length() - 1);
		}
		lRes = "[" + lRes + "]";
		return lRes;
	}

	private static String objectToCSV(Object aObj) {
		String lRes;
		if (aObj == null) {
			lRes = "null";
		} else if (aObj instanceof String) {
			lRes = stringToCSV((String) aObj);
		} else if (aObj instanceof Collection) {
			lRes = collectionToCSV((Collection<Object>) aObj);
		} else {
			lRes = "\"" + aObj.toString() + "\"";
		}
		return lRes;
	}

	/**
	 * converts a token into a CSV formatted data packet.
	 * @param aToken
	 * @return
	 */
	public static WebSocketPaket tokenToPacket(Token aToken) {
		String lData = "";
		Iterator<String> lIterator = aToken.getKeys();
		while (lIterator.hasNext()) {
			String lKey = lIterator.next();
			Object lVal = aToken.get(lKey);
			lData +=
				lKey + "=" + objectToCSV(lVal)
				+ (lIterator.hasNext() ? "," : "");
		}
		WebSocketPaket lPacket = null;
		try {
			lPacket = new RawPacket(lData, "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			log.error(ex.getClass().getSimpleName() + ": " + ex.getMessage());
		}
		return lPacket;
	}
}
