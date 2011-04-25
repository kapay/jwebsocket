//  ---------------------------------------------------------------------------
//  jWebSocket - BenchmarkPlugIn
//  Copyright (c) 2010 Innotrade GmbH, jWebSocket.org
//  ---------------------------------------------------------------------------
//  This program is free software; you can redistribute it and/or modify it
//  under the terms of the GNU Lesser General Public License as published by the
//  Free Software Foundation; either version 3 of the License, or (at your
//  option) any later version.
//  This program is distributed in the hope that it will be useful, but WITHOUT
//  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//  more details.
//  You should have received a copy of the GNU Lesser General Public License along
//  with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//  ---------------------------------------------------------------------------
package org.jwebsocket.plugins.benchmark;

import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.apache.log4j.Logger;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 *
 * @author kyberneees
 */
public class BenchmarkPlugIn extends TokenPlugIn {

	private String S2C_PERFORMANCE = "s2c_performance";
	private static final String NS_BENCHMARK =
			JWebSocketServerConstants.NS_BASE + ".plugins.benchmark";
	private static Logger mLog = Logging.getLogger(BenchmarkPlugIn.class);

	public BenchmarkPlugIn(PluginConfiguration aConfiguration) throws Exception {
		super(aConfiguration);

		setNamespace(NS_BENCHMARK);
	}

	/**
	 *
	 * {@inheritDoc }
	 */
	@Override
	public void processToken(PlugInResponse aResponse,
			WebSocketConnector aConnector, Token aToken) {
		if (getNamespace().equals(aToken.getNS())) {
			if (S2C_PERFORMANCE.equals(aToken.getType())) {
				testS2CPerformance(aConnector, aToken);
			}
		}
	}

	/**
	 * Test the performance of a broadcast operation
	 * 
	 * @param aConnector
	 * @param aToken 
	 */
	private void testS2CPerformance(WebSocketConnector aConnector,
			Token aToken) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Starting broadcasting benchmark...");
		}
		TokenServer lServer = getServer();

		String lMessage = aToken.getString("message");
		Integer lCount = aToken.getInteger("count");

		Token lTestToken = TokenFactory.createToken(getNamespace(), S2C_PERFORMANCE);
		lTestToken.setString("data", lMessage);


		for (int lLoop = 0; lLoop < lCount; lLoop++) {
			// lServer.sendToken(aConnector, lTestToken);
			if (mLog.isDebugEnabled()) {
				mLog.debug("Broadcasting test token (loop " + lLoop + "/" + lCount + ")...");
			}
			lServer.broadcastToken(lTestToken);
		}

		//Instantiate response token
		Token lResponse = lServer.createResponse(aToken);

		//Send response to requester
		lServer.sendToken(aConnector, lResponse);
	}
}
