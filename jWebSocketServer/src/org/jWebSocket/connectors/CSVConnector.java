//	---------------------------------------------------------------------------
//	jWebSocket - CSV Connector
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
public class CSVConnector extends TokenConnector {

	/**
	 * 
	 * @param aServerSocket
	 * @param aClientSocket
	 * @param aHeader
	 */
	public CSVConnector(BaseServer aServerSocket, Socket aClientSocket, Header aHeader) {
		super(aServerSocket, aClientSocket, aHeader);
	}

	@Override
	protected Token streamToToken(String aData) {
		Token lToken = new Token();
		String[] lItems = aData.split(",");
		for (int i = 0; i < lItems.length; i++) {
			String[] lKeyVal = lItems[i].split("=", 2);
			if (lKeyVal.length == 2) {
				String lVal = lKeyVal[1];
				if( lVal.length() <= 0 ) {
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
		return lToken;
	}

	private String stringToCSV(String aString) {
		// escape commata by \x2C
		aString = aString.replace(",", "\\x2C");
		// escape quotes by \x22
		aString = aString.replace("\"", "\\x22");
		return ("\"" + aString + "\"");
	}

	private String listToCSV(List aList) {
		String lRes = "";
		for (Object lItem : aList) {
			String llRes = objectToCSV(lItem);
			lRes += llRes + "|";
		}
		if (lRes.length() > 1) {
			lRes = lRes.substring(0, lRes.length() - 1);
		}
		lRes = "[" + lRes + "]";
		return lRes;
	}

	private String objectToCSV(Object aObj) {
		String lRes;
		if (aObj instanceof TokenConnector) {
			lRes = "\"" + ((TokenConnector) aObj).getUsername() + "@" + ((TokenConnector) aObj).getSessionId() + "\"";
		} else if (aObj instanceof String) {
			lRes = stringToCSV((String) aObj);
		} else if (aObj instanceof List) {
			lRes = listToCSV((List) aObj);
		} else {
			lRes = aObj.toString();
		}
		return lRes;
	}

	@Override
	protected String tokenToStream(Token aToken) {
		String lData = "";
		Iterator lIterator = aToken.keySet().iterator();
		while (lIterator.hasNext()) {
			String lKey = (String) lIterator.next();
			Object lVal = aToken.get(lKey);
			lData +=
					lKey + "=" + objectToCSV(lVal)
					+ (lIterator.hasNext() ? "," : "");
		}
		return lData;
	}
}
