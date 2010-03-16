//	---------------------------------------------------------------------------
//	jWebSocket - JSON Token Processor
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

import org.apache.commons.lang.text.StrTokenizer;
import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketPaket;
import org.jwebsocket.kit.RawPacket;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.token.Token;

/**
 * converts JSON formatted data packets into tokens and vice versa.
 * @author aschulze
 */
public class JSONProcessor {

	private static Logger log = Logging.getLogger(JSONProcessor.class);

	/**
	 * converts a JSON formatted data packet into a token.
	 * @param aDataPacket
	 * @return
	 */
	public static Token packetToToken(WebSocketPaket aDataPacket) {
		Token lToken = new Token();
		try {
			String lStr = aDataPacket.getString("UTF-8");
			// cut starting and trailing curly braces
			lStr = lStr.trim().substring(1, lStr.length() - 1);
			// String[] lItems = aData.split(",");
			StrTokenizer lTokens = new StrTokenizer(lStr, ',', '\"');
			String[] lItems = lTokens.getTokenArray();
			for (int i = 0; i < lItems.length; i++) {
				// String[] lKeyVal = lItems[i].split(":", 2);
				lTokens = new StrTokenizer(lItems[i], ':', '\"');
				String[] lKeyVal = lTokens.getTokenArray();
				if (lKeyVal.length == 2) {
					String lVal = lKeyVal[1];
					if (lVal.startsWith("\"") && lVal.endsWith("\"")) {
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

	private static String stringToJSON(String aString) {
		return ("\"" + aString + "\"");
	}

	private static String collectionToJSON(Collection<Object> aCollection) {
		String lRes = "";
		for (Object lItem : aCollection) {
			String llRes = objectToJSON(lItem);
			lRes += llRes + ",";
		}
		if (lRes.length() > 1) {
			lRes = lRes.substring(0, lRes.length() - 1);
		}
		lRes = "[" + lRes + "]";
		return lRes;
	}

	private static String objectToJSON(Object aObj) {
		String lRes;
		if( aObj == null ) {
			lRes = "null";
		} else if (aObj instanceof String) {
			lRes = stringToJSON((String) aObj);
		} else if (aObj instanceof Collection) {
			lRes = collectionToJSON((Collection<Object>) aObj);
		} else {
			lRes = "\"" + aObj.toString() + "\"";
		}
		return lRes;
	}

	/**
	 * converts a token into a JSON formatted data packet.
	 * @param aToken
	 * @return
	 */
	public static WebSocketPaket tokenToPacket(Token aToken) {
		String lData = "{";
		Iterator<String> lIterator = aToken.getKeys();
		while (lIterator.hasNext()) {
			String lKey = lIterator.next();
			Object lVal = aToken.get(lKey);
			lData +=
					lKey + ":" + objectToJSON(lVal)
					+ (lIterator.hasNext() ? "," : "");
		}
		lData += "}";
		WebSocketPaket lPacket = null;
		try {
			lPacket = new RawPacket(lData, "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			log.error(ex.getClass().getSimpleName() + ": " + ex.getMessage());
		}
		return lPacket;
	}
}
