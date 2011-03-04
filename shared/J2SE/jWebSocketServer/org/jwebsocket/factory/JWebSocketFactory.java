//	---------------------------------------------------------------------------
//	jWebSocket - Factory Singleton
//	Copyright (c) 2010 jWebSocket.org, Alexander Schulze, Innotrade GmbH
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
package org.jwebsocket.factory;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketFilter;
import org.jwebsocket.api.WebSocketInitializer;
import org.jwebsocket.api.WebSocketPlugIn;
import org.jwebsocket.api.WebSocketServer;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.instance.JWebSocketInstance;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.server.TokenServer;

/**
 * Factory to initialize and start the jWebSocket components
 * 
 * @author aschulze
 * @version $Id:$
 */
public class JWebSocketFactory {

	// don't instantiate logger here! first read args!
	private static Logger mLog = null;
	private static WebSocketEngine mEngine = null;
	private static List<WebSocketServer> mServers = null;
	private static TokenServer mTokenServer = null;

	/**
	 *
	 */
	public static void printCopyrightToConsole() {
		// the following 3 lines may not be removed due to GNU LGPL 3.0 license!
		System.out.println("jWebSocket Ver. "
				+ JWebSocketServerConstants.VERSION_STR
				+ " (" + System.getProperty("sun.arch.data.model") + "bit)");
		System.out.println(JWebSocketCommonConstants.COPYRIGHT);
		System.out.println(JWebSocketCommonConstants.LICENSE);
	}

	/**
	 *
	 */
	public static void start() {
		start(null);
	}

	/**
	 *
	 * @param aArgs
	 * @return
	 */
	public static String getConfigOverridePath(String[] aArgs) {
		String lConfigOverridePath = null;
		if (aArgs != null && aArgs.length > 0) {
			if (aArgs.length < 2) {
				System.out.println("use [-config <path_to_config_file>] as command line arguments to override default jWebSocket.xml");
			} else if (aArgs.length == 2) {
				if ("-config".equals(aArgs[0])) {
					lConfigOverridePath = aArgs[1];
				}
			}
		}
		return lConfigOverridePath;
	}

	/**
	 *
	 * @param aConfigOverridePath
	 */
	public static void start(String aConfigOverridePath) {

		JWebSocketInstance.setStatus(JWebSocketInstance.STARTING);

		JWebSocketLoader loader = new JWebSocketLoader();
		try {
			WebSocketInitializer lInitializer =
					loader.initialize(aConfigOverridePath);
			if (lInitializer == null) {
				JWebSocketInstance.setStatus(JWebSocketInstance.SHUTTING_DOWN);
				return;
			}
			lInitializer.initializeLogging();

			mLog = Logging.getLogger(JWebSocketFactory.class);
			if (mLog.isDebugEnabled()) {
				mLog.debug("Starting jWebSocket Server Sub System...");
			}
			mEngine = lInitializer.initializeEngine();
			if (mEngine == null) {
				// the loader already logs an error!
				JWebSocketInstance.setStatus(JWebSocketInstance.SHUTTING_DOWN);
				return;
			}

			// initialize and start the server
			if (mLog.isDebugEnabled()) {
				mLog.debug("Initializing servers...");
			}
			mServers = lInitializer.initializeServers();

			Map<String, List<WebSocketPlugIn>> lPluginMap =
					lInitializer.initializePlugins();
			if (mLog.isDebugEnabled()) {
				mLog.debug("Initializing plugins...");
			}
			for (WebSocketServer lServer : mServers) {
				lServer.addEngine(mEngine);
				List<WebSocketPlugIn> lPlugIns = lPluginMap.get(lServer.getId());
				for (WebSocketPlugIn lPlugIn : lPlugIns) {
					lServer.getPlugInChain().addPlugIn(lPlugIn);
				}
				if (mLog.isInfoEnabled()) {
					mLog.info(lPlugIns.size()
							+ " plugin(s) initialized for server '"
							+ lServer.getId() + "'.");
				}
			}

			Map<String, List<WebSocketFilter>> lFilterMap =
					lInitializer.initializeFilters();
			if (mLog.isDebugEnabled()) {
				mLog.debug("Initializing filters...");
			}
			for (WebSocketServer lServer : mServers) {
				lServer.addEngine(mEngine);
				List<WebSocketFilter> lFilters = lFilterMap.get(lServer.getId());
				for (WebSocketFilter lFilter : lFilters) {
					lServer.getFilterChain().addFilter(lFilter);
				}
				if (mLog.isInfoEnabled()) {
					mLog.info(lFilters.size()
							+ " filter(s) initialized for server '"
							+ lServer.getId() + "'.");
				}
			}

			boolean lEngineStarted = false;

			// first start the engine
			if (mLog.isDebugEnabled()) {
				mLog.debug("Starting engine '" + mEngine.getId() + "'...");
			}

			try {
				mEngine.startEngine();
				lEngineStarted = true;
			} catch (Exception lEx) {
				mLog.error("Starting engine '" + mEngine.getId()
						+ "' failed (" + lEx.getClass().getSimpleName() + ": "
						+ lEx.getMessage() + ").");
			}

			// do not start any servers if engine could not be started
			if (lEngineStarted) {
				// now start the servers
				if (mLog.isDebugEnabled()) {
					mLog.debug("Starting servers...");
				}
				for (WebSocketServer lServer : mServers) {
					try {
						lServer.startServer();
					} catch (Exception lEx) {
						mLog.error("Starting server '" + lServer.getId()
								+ "' failed (" + lEx.getClass().getSimpleName()
								+ ": " + lEx.getMessage() + ").");
					}
				}

				if (mLog.isInfoEnabled()) {
					mLog.info("jWebSocket server startup complete");
				}

				// if everything went fine...
				JWebSocketInstance.setStatus(JWebSocketInstance.STARTED);
			} else {
				// if engine couldn't be started due to whatever reasons...
				JWebSocketInstance.setStatus(JWebSocketInstance.SHUTTING_DOWN);
			}

		} catch (WebSocketException lEx) {
			if (mLog != null) {
				if (mLog.isDebugEnabled()) {
					mLog.debug("Exception during startup", lEx);
				}
			} else {
				System.out.println(lEx.getClass().getSimpleName() + " during jWebSocket Server startup: " + lEx.getMessage());
			}
			if (mLog != null && mLog.isInfoEnabled()) {
				mLog.info("jWebSocketServer failed to start.");
			}
			JWebSocketInstance.setStatus(JWebSocketInstance.SHUTTING_DOWN);
		}
	}

	/**
	 *
	 */
	public static void stop() {

		JWebSocketInstance.setStatus(JWebSocketInstance.STOPPING);

		if (mLog != null && mLog.isDebugEnabled()) {
			mLog.debug("Stopping jWebSocket Sub System...");
		}

		// String lEngineId = "?";
		// stop engine if previously started successfully
		if (mEngine != null) {
			// now stop the servers
			if (mLog != null && mLog.isDebugEnabled()) {
				mLog.debug("Stopping engine...");
			}
			try {
				mEngine.stopEngine(CloseReason.SHUTDOWN);
				if (mLog != null && mLog.isInfoEnabled()) {
					mLog.info("jWebSocket engine '" + mEngine.getId() + "' stopped");
				}
			} catch (WebSocketException lEx) {
				if (mLog != null) {
					mLog.error("Stopping engine: " + lEx.getMessage());
				}
			}
		}

		if (mServers != null) {
			// now stop the servers
			if (mLog != null && mLog.isDebugEnabled()) {
				mLog.debug("Stopping servers...");
			}
			for (WebSocketServer lServer : mServers) {
				try {
					lServer.stopServer();
					if (mLog != null && mLog.isInfoEnabled()) {
						mLog.info("jWebSocket server '" + lServer.getId() + "' stopped");
					}
				} catch (WebSocketException lEx) {
					if (mLog != null) {
						mLog.error("Stopping server: " + lEx.getMessage());
					}
				}
			}
		}

		if (mLog != null && mLog.isInfoEnabled()) {
			mLog.info("jWebSocket Server Sub System stopped.");
		}
		Logging.exitLogs();

		JWebSocketInstance.setStatus(JWebSocketInstance.STOPPED);
	}

	/**
	 *
	 * @return
	 */
	public static WebSocketEngine getEngine() {
		return mEngine;
	}

	/**
	 *
	 * @return
	 */
	public static List<WebSocketServer> getServers() {
		return mServers;
	}

	/**
	 * Returns the server identified by it's id or <tt>null</tt> if no server with
	 * that id could be found in the factory.
	 *
	 * @param aId
	 *          id of the server to be returned.
	 * @return WebSocketServer with the given id or <tt>null</tt> if not found.
	 */
	public static WebSocketServer getServer(String aId) {
		if (aId != null && mServers != null) {
			for (WebSocketServer lServer : mServers) {
				if (lServer != null && aId.equals(lServer.getId())) {
					return lServer;
				}
			}
		}
		return null;
	}

	/**
	 *
	 * @return
	 */
	public static TokenServer getTokenServer() {
		if (mTokenServer == null) {
			mTokenServer = (TokenServer) getServer("ts0");
		}
		return mTokenServer;
	}
}
