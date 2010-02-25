//	---------------------------------------------------------------------------
//	jWebSocket - WebSocket JSON Connector
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
package org.jWebSocket.connectors;

import java.net.Socket;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang.text.StrTokenizer;
import org.jWebSocket.server.BaseServer;
import org.jWebSocket.server.Header;
import org.jWebSocket.server.Token;

/**
 *
 * @author aschulze
 */
public class JSONConnector extends TokenConnector {

	/**
	 *
	 * @param aServerSocket
	 * @param aClientSocket
	 */
	public JSONConnector(BaseServer aServerSocket, Socket aClientSocket, Header aHeader) {
		super(aServerSocket, aClientSocket, aHeader);
	}

	@Override
	protected Token streamToToken(String aData) {
		// cut starting and trailing curly braces
		aData = aData.trim().substring(1, aData.length() - 1);
		Token lToken = new Token();
		// String[] lItems = aData.split(",");
		StrTokenizer lTokens = new StrTokenizer(aData, ',', '\"');
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
		return lToken;
	}

	private String stringToJSON(String aString) {
		return ("\"" + aString + "\"");
	}

	private String listToJSON(List aList) {
		String lRes = "";
		for (Object lItem : aList) {
			String llRes = objectToJSON(lItem);
			lRes += llRes + ",";
		}
		if (lRes.length() > 1) {
			lRes = lRes.substring(0, lRes.length() - 1);
		}
		return ("[" + lRes + "]");
	}

	private String objectToJSON(Object aObj) {
		String lRes;
		if (aObj instanceof TokenConnector) {
			lRes = "\"" + ((TokenConnector) aObj).getUsername() + "@" + ((TokenConnector) aObj).getSessionId() + "\"";
		} else if (aObj instanceof String) {
			lRes = stringToJSON((String) aObj);
		} else if (aObj instanceof List) {
			lRes = listToJSON((List) aObj);
		} else {
			lRes = aObj.toString();
		}
		return lRes;
	}

	public String tokenToStream( Token aToken) {
		String lData = "{";
		Iterator lIterator = aToken.keySet().iterator();
		while (lIterator.hasNext()) {
			String lKey = (String) lIterator.next();
			Object lVal = aToken.get(lKey);
			lData +=
				lKey + ":" + objectToJSON(lVal)
				+ (lIterator.hasNext() ? "," : "");
		}
		lData += "}";
		return lData;
	}

}
