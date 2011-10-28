//	---------------------------------------------------------------------------
//	jWebSocket - TimeoutOutputStreamNIOWriter
//	Copyright (c) 2011 Alexander Schulze, Innotrade GmbH
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
package org.jwebsocket.tcp;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.kit.CloseReason;

/**
 * This works OK, the only pending question is that the write method
 * of the native OutputStream never gets locked as expected.
 * 
 * Advises:
 * - Notify the connector stopped event in a thread pool instead of the same thread
 * - Check the connection state before send a packet
 *
 * @author kyberneees
 * @author aschulze
 */
public class TimeoutOutputStreamNIOWriter {

	/**
	 * Singleton Timer instance to control all timeout tasks
	 */
	private int mTimeout;
	private static Timer mTimer = new Timer();
	// the size of this executor service should be adjusted to the maximum
	// of expected client send operations that concurrently might get 
	// to a timeout case.
	private static ExecutorService mPool = Executors.newScheduledThreadPool(25); // @TODO make this configurable after
	private OutputStream mOut = null;
	private WebSocketConnector mConnector = null;

	/**
	 * 
	 * @param aConnector 
	 * @param aTimeout
	 * @param aOut  
	 */
	public TimeoutOutputStreamNIOWriter(WebSocketConnector aConnector,
			OutputStream aOut, int aTimeout) {
		mTimeout = aTimeout;
		mOut = aOut;
		mConnector = aConnector;
	}

	/**
	 * 
	 * @return
	 */
	public static ExecutorService getPool() {
		return mPool;
	}

	/**
	 * 
	 * @return
	 */
	public int getTimeout() {
		return mTimeout;
	}

	/**
	 * 
	 * @param aTimeout
	 */
	public void setTimeout(int aTimeout) {
		this.mTimeout = aTimeout;
	}

	/**
	 * 
	 * @return
	 */
	public static Timer getTimer() {
		return mTimer;
	}

	/**
	 * Write operation thread to execute write operations in non-blocking mode.
	 */
	class SendOperation implements Callable<Object> {

		private int mTimeout;
		private WebSocketPacket mPacket;
		private TimerTask mTimeoutTask;

		public OutputStream getOut() {
			return mOut;
		}

		public int getTimeout() {
			return mTimeout;
		}

		public SendOperation(WebSocketPacket aDataPacket) {
			this.mPacket = aDataPacket;
		}

		@Override
		public Object call() throws Exception {
			// @TODO This always is being executed quickly even when the connector get's stopped
			// this sends the packet to the socket output stream
			((TCPConnector) mConnector)._sendPacket(mPacket);
			// this cancels the timeout task in case 
			// the send operation did not block for the given timeout
			mTimeoutTask.cancel();
			return null;
		}
	}

	class TimeoutTimerTask extends TimerTask {

		private SendOperation mSendOperation;

		public TimeoutTimerTask(SendOperation aSendOperation) {
			this.mSendOperation = aSendOperation;
			this.mSendOperation.mTimeoutTask = this;
		}

		@Override
		public void run() {
			try {
				// close the outbound stream to fire exception
				// timed out write operation
				mSendOperation.getOut().close();
				mConnector.getEngine().connectorStopped(
						mConnector, CloseReason.CLIENT);
			} catch (IOException ex) {
				// TODO check this
			}
		}
	}

	/**
	 * Send a data packet with timeout control.
	 * @param aDataPacket
	 */
	public void sendPacket(WebSocketPacket aDataPacket) {
		// create a timer task to send the packet
		SendOperation lSend = new SendOperation(aDataPacket);
		// create a timeout timer task to watch the send operation
		TimerTask lTask = new TimeoutTimerTask(lSend);
		// schedule the watcher for the send operation
		mTimer.schedule(lTask, getTimeout());
		// finally execute the send operation
		mPool.submit(lSend);
	}
}
