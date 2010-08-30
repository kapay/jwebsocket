// ---------------------------------------------------------------------------
// jWebSocket - JSON Token Processor
// Copyright (c) 2010 jWebSocket.org, Alexander Schulze, Innotrade GmbH
// ---------------------------------------------------------------------------
// This program is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published by the
// Free Software Foundation; either version 3 of the License, or (at your
// option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
// more details.
// You should have received a copy of the GNU Lesser General Public License along
// with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
// ---------------------------------------------------------------------------
package org.jwebsocket.packetProcessors;

import java.util.Collection;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.kit.RawPacket;
import org.jwebsocket.token.MapToken;
import org.jwebsocket.token.Token;

/**
 * converts JSON formatted data packets into tokens and vice versa.
 * @author Alexander Schulze, Roderick Baier (improvements regarding JSON array).
 */
public class JSONProcessor {

	// TODO: Logging cannot be used in common module because not supported on all clients
	// private static Logger log = Logging.getLogger(JSONProcessor.class);
	/**
	 * converts a JSON formatted data packet into a token.
	 * @param aDataPacket
	 * @return
	 */
/*
	public static Token packetToToken(WebSocketPacket aDataPacket) {
		Token lToken;
		try {
			String lStr = aDataPacket.getString("UTF-8");
			JSONTokener lJSONTokener = new JSONTokener(lStr);
			JSONObject lJO = new JSONObject(lJSONTokener);
			lToken = new Token(lJO);
		} catch (Exception ex) {
			// TODO: handle exception
			// log.error(ex.getClass().getSimpleName() + ": " + ex.getMessage());
			lToken = null;
		}
		return lToken;
	}
*/
/*
	public static WebSocketPacket tokenToPacket(Token aToken) {
		WebSocketPacket lPacket = null;
		try {
			JSONObject lJSON = aToken.getJSONObject();
			String lData = lJSON.toString();
			lPacket = new RawPacket(lData, "UTF-8");
		} catch (Exception ex) {
			// TODO: process exception
			// log.error(ex.getClass().getSimpleName() + ": " + ex.getMessage());
		}
		return lPacket;
	}
*/

	// TODO: Logging cannot be used in common module because not supported on all clients
	// private static Logger log = Logging.getLogger(JSONProcessor.class);
	/**
	 * converts a JSON formatted data packet into a token.
	 * @param aDataPacket
	 * @return
	 */
	public static Token packetToToken(WebSocketPacket aDataPacket) {
		Token lToken = new MapToken();
		try {
			String lStr = aDataPacket.getString("UTF-8");
			JSONTokener lJT = new JSONTokener(lStr);
			JSONObject lJO = new JSONObject(lJT);
			for (Iterator lIterator = lJO.keys(); lIterator.hasNext();) {
				String lKey = (String) lIterator.next();
				lToken.setValidated(lKey, lJO.get(lKey));
			}
		} catch (Exception ex) {
			// // TODO: process exception
			// log.error(ex.getClass().getSimpleName() + ": " + ex.getMessage());
		}
		return lToken;
	}

	public static WebSocketPacket tokenToPacket(Token token) {
		WebSocketPacket packet = null;
		try {
			JSONObject lJO = tokenToJSON(token);
			String data = lJO.toString();
			packet = new RawPacket(data, "UTF-8");
		} catch (Exception ex) {
			// TODO: process exception
			// log.error(ex.getClass().getSimpleName() + ": " + ex.getMessage());
		}

		return packet;
	}

	private static JSONObject tokenToJSON(Token aToken) throws JSONException {
		JSONObject lJSO = new JSONObject();

		Iterator<String> iterator = aToken.getKeyIterator();
		while (iterator.hasNext()) {
			String key = iterator.next();
			Object value = aToken.getObject(key);
			if (value instanceof Collection) {
				JSONArray lArray = new JSONArray();
				for (Object item : (Collection) value) {
					if (item instanceof Token) {
						JSONObject object = tokenToJSON((Token) item);
						lArray.put(object);
					} else {
						lArray.put(item);
					}
				}
				lJSO.put(key, lArray);
			} else if (value instanceof Token) {
				lJSO.put(key, tokenToJSON((Token) value));
			} else if (value instanceof Object[]) {
				JSONArray lArray = new JSONArray();
				Object[] lObjs = (Object[]) value;
				for (int lIdx = 0; lIdx < lObjs.length; lIdx++) {
					Object lObj = lObjs[lIdx];
					lArray.put(lObj);
				}
				lJSO.put(key, lArray);
			} else {
				lJSO.put(key, value);
			}
		}

		return lJSO;
	}

	/**
	 * Creates a token based on a JSON object
	 */
/*
	public static Token JSON2Token(JSONObject aJSON) {
		Token lToken = new Token();
		Iterator lIterator = aJSON.keys();
		while( lIterator.hasNext() ) {
			String lKey = (String)lIterator.next();
			try {
				lToken.put(lKey, aJSON.get(lKey));
			} catch (JSONException ex) {
				// TODO: handle exception!
			}
		}
		return lToken;
	}
*/
}
