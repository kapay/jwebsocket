//	---------------------------------------------------------------------------
//	jWebSocket - Jetty Engine
//	Copyright (c) 2010 Alexander Schulze, Innotrade GmbH
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
package org.jwebsocket.jetty;

import java.util.Date;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.jwebsocket.api.EngineConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.engines.BaseEngine;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.logging.Logging;

/**
 *
 * @author aschulze
 */
public class JettyEngine extends BaseEngine {

	private static Logger mLog = Logging.getLogger(JettyEngine.class);
	private boolean mIsRunning = false;

	public JettyEngine(EngineConfiguration aConfiguration) {
		super(aConfiguration);

		// We will create our server running at http://localhost:8070
		Server lJettyServer = new Server();
		Connector lJettyConnector = new SelectChannelConnector();
		lJettyConnector.setPort(8080);
		lJettyConnector.setHost("127.0.0.1");
		lJettyServer.addConnector(lJettyConnector);
		/*
		WebAppContext wac = new WebAppContext();
		wac.setContextPath("/");
		//expanded war or path of war file
		// lJettyServer.addHandler(wac);
		wac.setWar("./src/main/resources/web");
		 */
		lJettyServer.setStopAtShutdown(true);
		try {
			lJettyServer.start();
		} catch (Exception lEx) {
			mLog.error(lEx.getClass().getSimpleName()
					+ "Instantiating Embedded Jetty Server: " + lEx.getMessage());
		}
	}

	@Override
	public void startEngine()
			throws WebSocketException {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Starting Jetty engine '"
					+ getId()
					+ "...");
		}

		super.startEngine();

		if (mLog.isInfoEnabled()) {
			mLog.info("Jetty engine '"
					+ getId()
					+ "' started.");
		}
	}

	@Override
	public void stopEngine(CloseReason aCloseReason)
			throws WebSocketException {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Stopping Jetty engine '" + getId() + "...");
		}

		// resetting "isRunning" causes engine listener to terminate
		mIsRunning = false;
		// inherited method stops all connectors

		long lStarted = new Date().getTime();
		int lNumConns = getConnectors().size();
		super.stopEngine(aCloseReason);
		/*
		// now wait until all connectors have been closed properly
		// or timeout exceeds...
		try {
		while (getConnectors().size() > 0 && new Date().getTime() - lStarted < 10000) {
		Thread.sleep(250);
		}
		} catch (Exception lEx) {
		mLog.error(lEx.getClass().getSimpleName() + ": " + lEx.getMessage());
		}
		if (mLog.isDebugEnabled()) {
		long lDuration = new Date().getTime() - lStarted;
		int lRemConns = getConnectors().size();
		if (lRemConns > 0) {
		mLog.warn(lRemConns + " of " + lNumConns
		+ " Jetty connectors '" + getId()
		+ "' did not stop after " + lDuration + "ms.");
		} else {
		mLog.debug(lNumConns
		+ " Jetty connectors '" + getId()
		+ "' stopped after " + lDuration + "ms.");
		}
		}
		 */
	}

	@Override
	public void connectorStarted(WebSocketConnector aConnector) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Detected new connector at port " + aConnector.getRemotePort() + ".");
		}
		super.connectorStarted(aConnector);
	}

	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Detected stopped connector at port " + aConnector.getRemotePort() + ".");
		}
		super.connectorStopped(aConnector, aCloseReason);
	}

	@Override
	/*
	 * Returns {@code true} if the TCP engine is running or {@code false}
	 * otherwise. The alive status represents the state of the TCP engine
	 * listener thread.
	 */
	public boolean isAlive() {
		return mIsRunning;
	}
}
