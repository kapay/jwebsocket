package org.jwebsocket.tcp.nio;

import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.async.IOFuture;
import org.jwebsocket.async.IOFutureListener;
import org.jwebsocket.logging.Logging;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class DataFuture implements IOFuture {
	private static Logger mLog = Logging.getLogger(DataFuture.class);
	private List<IOFutureListener> listeners;
	private boolean done;
	private boolean success;
	private Throwable cause;
	private WebSocketConnector connector;
	private ByteBuffer data;

	public DataFuture(WebSocketConnector connector, ByteBuffer data) {
		this.connector = connector;
		this.data = data;
		listeners = new ArrayList<IOFutureListener>();
	}

	@Override
	public WebSocketConnector getConnector() {
		return connector;
	}

	@Override
	public boolean isDone() {
		return done;
	}

	@Override
	public boolean isCancelled() {
		return false;  // not implemented
	}

	@Override
	public boolean isSuccess() {
		return success;
	}

	@Override
	public Throwable getCause() {
		return cause;
	}

	@Override
	public boolean cancel() {
		return false;  // not implemented
	}

	@Override
	public boolean setSuccess() {
		success = true;
		done = true;
		notifyListeners();
		return success;
	}

	@Override
	public boolean setFailure(Throwable cause) {
		if (!success && !done) {
			this.cause = cause;
			success = false;
			done = true;
			notifyListeners();
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean setProgress(long amount, long current, long total) {
		return false;  // not implemented
	}

	@Override
	public void addListener(IOFutureListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(IOFutureListener listener) {
		listeners.remove(listener);
	}

	public ByteBuffer getData() {
		return data;
	}

	private void notifyListeners() {
		try {
			for (IOFutureListener listener : listeners) {
				listener.operationComplete(this);
			}
		}
		catch (Exception e) {
			mLog.info("Exception while notifying IOFuture listener", e);
		}
	}
}
