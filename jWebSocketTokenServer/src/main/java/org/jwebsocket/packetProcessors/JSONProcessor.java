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
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;
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

	public static WebSocketPaket tokenToPacket(Token aToken) {
		WebSocketPaket lPacket = null;
		try {
			JSONStringer jsonStringer = new JSONStringer();
			// start main object
			jsonStringer.object();
			// iterate through all items (fields) of the token
			Iterator<String> lIterator = aToken.getKeys();
			while (lIterator.hasNext()) {
				String lKey = lIterator.next();
				Object lVal = aToken.get(lKey);
				if (lVal instanceof Collection) {
					jsonStringer.key(lKey).array();
					for (Object item : (Collection) lVal) {
						jsonStringer.value(item);
					}
					jsonStringer.endArray();
				} else {
					jsonStringer.key(lKey).value(lVal);
				}
			}
			// end main object
			jsonStringer.endObject();
			String lData = jsonStringer.toString();
			lPacket = new RawPacket(lData, "UTF-8");
		} catch (JSONException ex) {
			log.error(ex.getClass().getSimpleName() + ": " + ex.getMessage());
		} catch (UnsupportedEncodingException ex) {
			log.error(ex.getClass().getSimpleName() + ": " + ex.getMessage());
		}
		return lPacket;
	}

}
