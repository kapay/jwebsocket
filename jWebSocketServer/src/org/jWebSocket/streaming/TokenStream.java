//	---------------------------------------------------------------------------
//	jWebSocket - Token In- and Outbound Stream
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
package org.jWebSocket.streaming;

import org.apache.log4j.Logger;
import org.jWebSocket.connectors.BaseConnector;
import org.jWebSocket.processors.TokenConnector;
import org.jWebSocket.kit.Token;

/**
 *
 * @author aschulze
 */
public class TokenStream extends BaseStream {

	private static Logger log = Logger.getLogger(TokenStream.class);

	/**
	 *
	 */
	public TokenStream(String aStreamID) {
		super(aStreamID);
	}

	@Override
	protected void processClient(BaseConnector aClient, Object aObject) {
		Token lEventToken = new Token("event");
		lEventToken.put( "name", "stream" );
		lEventToken.put("msg", aObject);
		lEventToken.put("streamID", getStreamID());
		((TokenConnector) aClient).sendToken(lEventToken);
	}
}
