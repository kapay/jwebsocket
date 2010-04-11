//	---------------------------------------------------------------------------
//	jWebSocket - Monitor Stream
//	Copyright (c) 2010 jWebSocket.org by Innotrade GmbH, Alexander Schulze.
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
package org.jwebsocket.plugins.streaming;

import java.io.File;
import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.Token;

/**
 * implements the jWebSocket monitor stream for demonstration purposes.
 * Reads certain system parameters in a predefined interval of 1 second and
 * reports it to all registered clients.
 * @author aschulze
 */
public class MonitorStream extends TokenStream {

	private static Logger log = Logging.getLogger(MonitorStream.class);
	private Boolean isRunning = false;
	private MonitorProcess monitorProcess = null;
	private Thread monitorThread = null;

	/**
	 * creates a new instance of the monitor stream.
	 * @param aStreamID The unique ID of the stream.
	 * @param aServer The Token Server associated with this stream.
 	 */
	public MonitorStream(String aStreamID, TokenServer aServer) {
		super(aStreamID, aServer);
		startMonitorThread();
	}

	/**
	 * starts the internal monitor thread to check for certain system
	 * parameters in a predefined interval of 1 second.
	 */
	public void startMonitorThread() {
		monitorProcess = new MonitorProcess();
		monitorThread = new Thread(monitorProcess);
		monitorThread.start();
	}

	/**
	 * stops the monitor thread.
	 */
	public void stopMonitorThread() {
		isRunning = false;
	}

	private class MonitorProcess implements Runnable {

		@Override
		public void run() {
			if (log.isDebugEnabled()) {
				log.debug("Starting monitor stream...");

				isRunning = true;
				while (isRunning) {
					try {
						Thread.sleep(1000);

						Token lToken = new Token("event");
						lToken.put("name", "stream");
						lToken.put("streamID", getStreamID());

						// collect some data to monitor
						Runtime lRT = Runtime.getRuntime();
						lToken.put("totalMem", lRT.totalMemory());
						lToken.put("freeMem", lRT.freeMemory());

						TokenServer lServer = getServer();
						lToken.put("clientCount", lServer.getAllConnectors().size());

						File lFile = new File(".");
						lToken.put("freeDisk", lFile.getFreeSpace());
						lToken.put("totalDisk", lFile.getTotalSpace());
						lToken.put("usableDisk", lFile.getUsableSpace());

						// : further tags to be continued....

						put(lToken);
					} catch (InterruptedException ex) {
						log.error("(run) " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
					}
				}

				if (log.isDebugEnabled()) {
					log.debug("Monitor stream stopped.");
				}
			}
		}
	}
}
