//	---------------------------------------------------------------------------
//	jWebSocket - Time Stream
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
package org.jwebsocket.plugins.streaming;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Date;

import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 * implements the JDBCStream, primarily for demonstration purposes but it can
 * also be used for client/server time synchronization. It implements an
 * internal thread which broadcasts the current system time of the server to
 * the registered clients once per second.
 * @author aschulze
 */
public class JDBCStream extends TokenStream {

	private static Logger log = Logging.getLogger(JDBCStream.class);
	private Boolean isRunning = false;
	private DBPollingProcess dbPollingProcess = null;
	private Thread dbPollingThread = null;
	private Connection mConnection = null;

	/**
	 *
	 *
	 * @param aStreamID
	 * @param aServer
	 */
	public JDBCStream(String aStreamID, TokenServer aServer) {
		super(aStreamID, aServer);
		startStream(-1);
	}

	/**
	 *
	 */
	@Override
	public void startStream(long aTimeout) {
		super.startStream(aTimeout);

		if (log.isDebugEnabled()) {
			log.debug("Starting Time stream...");
		}
		/*
		dbPollingProcess = new DBPollingProcess();
		dbPollingThread = new Thread(dbPollingProcess);
		dbPollingThread.start();
		 */
		try {
			// load the class
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			mConnection = DriverManager.getConnection(
					"jdbc:sqlserver://host:15001;database=dbname;integratedSecurity=false;user=username;password=password;",
					"username", "password");
		} catch (Exception ex) {
			log.error("(run) " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
		}
	}

	/**
	 *
	 */
	@Override
	public void stopStream(long aTimeout) {
		if (log.isDebugEnabled()) {
			log.debug("Stopping Time stream...");
		}
		long lStarted = new Date().getTime();
		isRunning = false;

		try {
			mConnection.close();
		} catch (Exception ex) {
			log.error(ex.getClass().getSimpleName() + ": " + ex.getMessage());
		}
		/*
		try {
		dbPollingThread.join(aTimeout);
		} catch (Exception ex) {
		log.error(ex.getClass().getSimpleName() + ": " + ex.getMessage());
		}
		if (log.isDebugEnabled()) {
		long lDuration = new Date().getTime() - lStarted;
		if (dbPollingThread.isAlive()) {
		log.warn("JDBC stream did not stopped after " + lDuration + "ms.");
		} else {
		log.debug("JDBC stream stopped after " + lDuration + "ms.");
		}
		}
		 */
		super.stopStream(aTimeout);
	}

	private class DBPollingProcess implements Runnable {

		@Override
		public void run() {
			if (log.isDebugEnabled()) {
				log.debug("Running JDBC stream...");
			}
			isRunning = true;
			while (isRunning) {
				try {
					Thread.sleep(1000);

					Token lToken = TokenFactory.createToken("event");
					lToken.setString("name", "stream");
					lToken.setString("msg", new Date().toString());
					lToken.setString("streamID", getStreamID());

					// log.debug("Time streamer queues '" + lData + "'...");
					put(lToken);
				} catch (InterruptedException ex) {
					log.error("(run) " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
				}
			}
			if (log.isDebugEnabled()) {
				log.debug("JDBC stream stopped.");
			}
		}
	}
}
