/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.plugins.statistics;

import org.apache.log4j.Logger;
import org.jwebsocket.api.FilterConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.filter.TokenFilter;
import org.jwebsocket.filters.system.SystemFilter;
import org.jwebsocket.kit.FilterResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.Token;

/**
 *
 * @author aschulze
 */
public class StatisticsFilter extends TokenFilter {

	private static Logger mLog = Logging.getLogger(SystemFilter.class);

	/**
	 *
	 * @param aConfiguration
	 */
	public StatisticsFilter(FilterConfiguration aConfiguration) {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating statistics filter...");
		}
	}

	/**
	 *
	 * @param aResponse
	 * @param aConnector
	 * @param aToken
	 */
	@Override
	public void processTokenIn(FilterResponse aResponse,
			WebSocketConnector aConnector, Token aToken) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Analyzing incoming token from "
					+ (aConnector != null
					? aConnector.getId()
					: "[not given]")
					+ ": " + aToken.toString() + "...");
		}

		TokenServer lServer = getServer();
		String lUsername = lServer.getUsername(aConnector);

		// TODO: very first security test, replace by user's locked state!
		if ("locked".equals(lUsername)) {
			Token lToken = lServer.createAccessDenied(aToken);
			lServer.sendToken(aConnector, lToken);
			aResponse.rejectMessage();
			return;
		}
	}

	/**
	 *
	 * @param aResponse
	 * @param aSource
	 * @param aTarget
	 * @param aToken
	 */
	@Override
	public void processTokenOut(FilterResponse aResponse,
			WebSocketConnector aSource, WebSocketConnector aTarget, Token aToken) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Analyzing incoming token from "
					+ (aSource != null
					? aSource.getId()
					: "[not given]") + " to "
					+ (aTarget != null ? aTarget.getId() : "[not given]")
					+ ": " + aToken.toString() + "...");
		}
	}
}
