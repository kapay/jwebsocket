//	---------------------------------------------------------------------------
//	jWebSocket - JSON Token Processor
//	Copyright (c) 2010 jWebSocket.org, Alexander Schulze, Innotrade GmbH
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.jwebsocket.api.WebSocketPaket;
import org.jwebsocket.kit.RawPacket;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.token.Token;

/**
 * converts JSON formatted data packets into tokens and vice versa.
 * @author Alexander Schulze, Roderick Baier (improvements regarding JSON array).
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
			JSONTokener jsonTokener = new JSONTokener(lStr);
			JSONObject jsonObject = new JSONObject(jsonTokener);
			for (Iterator lIterator = jsonObject.keys(); lIterator.hasNext();) {
				String lKey = (String) lIterator.next();
				lToken.put(lKey, jsonObject.get(lKey));
			}
		} catch (UnsupportedEncodingException ex) {
			log.error(ex.getClass().getSimpleName() + ": " + ex.getMessage());
		} catch (JSONException ex) {
			log.error(ex.getClass().getSimpleName() + ": " + ex.getMessage());
		}
		return lToken;
	}

	public static WebSocketPaket tokenToPacket(Token token) {
		WebSocketPaket packet = null;

		try {
			JSONObject json = tokenToJSON(token);
			String data = json.toString();
			packet = new RawPacket(data, "UTF-8");
		} catch (JSONException ex) {
			log.error(ex.getClass().getSimpleName() + ": " + ex.getMessage());
		} catch (UnsupportedEncodingException ex) {
			log.error(ex.getClass().getSimpleName() + ": " + ex.getMessage());
		}

		return packet;
	}

	private static JSONObject tokenToJSON(Token token)
			throws JSONException {
		JSONObject json = new JSONObject();

		Iterator<String> iterator = token.getKeys();
		while (iterator.hasNext()) {
			String key = iterator.next();
			Object value = token.get(key);
			if (value instanceof Collection) {
				JSONArray array = new JSONArray();
				for (Object item : (Collection) value) {
					if (item instanceof Token) {
						JSONObject object = tokenToJSON((Token) item);
						array.put(object);
					} else {
						array.put(item);
					}
				}
				json.put(key, array);
			} else {
				json.put(key, value);
			}
		}

		return json;
	}
}
