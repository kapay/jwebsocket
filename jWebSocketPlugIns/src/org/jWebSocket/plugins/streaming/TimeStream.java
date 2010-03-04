//	---------------------------------------------------------------------------
//	jWebSocket - Time Stream
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
package org.jWebSocket.plugins.streaming;

import java.util.Date;

import org.apache.log4j.Logger;
import org.jWebSocket.api.WebSocketConnector;
import org.jWebSocket.server.TokenServer;
import org.jWebSocket.token.Token;

/**
 *
 * @author aschulze
 */
public class TimeStream extends TokenStream {

	private static Logger log = Logger.getLogger(TimeStream.class);
	private Boolean isRunning = false;
	private TimerThread timer = null;

	/**
	 *
	 */
	public TimeStream(String aStreamID, TokenServer aServer) {
		super(aStreamID, aServer);
		startTimerThread();
	}

	public void startTimerThread() {
		timer = new TimerThread();
		Thread queueThread = new Thread(timer);
		queueThread.start();
	}

	public void stopTimerThread() {
		isRunning = false;
	}

	@Override
	protected void processConnector(WebSocketConnector aConnector, Object aObject) {
		try {
			getServer().sendToken(aConnector, (Token)aObject);
		} catch (Exception ex) {
			log.error("(processConnector) " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
		}
	}

	public class TimerThread implements Runnable {

		@Override
		public void run() {
			log.debug("Starting time stream...");
			isRunning = true;
			while (isRunning) {
				try {
					sleep(1000);

					Token lEventToken = new Token("event");
					lEventToken.put("name", "stream");
					lEventToken.put("msg", new Date().toString());
					lEventToken.put("streamID", getStreamID());

					// log.debug("Time streamer queues '" + lData + "'...");
					put(lEventToken);
				} catch (InterruptedException ex) {
					log.error("(run) " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
				}
			}
			log.debug("Time stream stopped.");
		}
	}
}
