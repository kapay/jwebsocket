package org.jwebsocket.client.java;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.jwebsocket.api.WebSocketEventHandler;
import org.jwebsocket.api.data.WebSocketMessage;

public class WebSocketReceiver extends Thread {

	private InputStream input = null;
	private BaseWebSocket websocket = null;
	private WebSocketEventHandler eventHandler = null;

	private boolean stop = false;
	
	public WebSocketReceiver(InputStream input, BaseWebSocket websocket) {
		this.input = input;
		this.websocket = websocket;
		this.eventHandler = websocket.getEventHandler();
	}

	public void run() {
		boolean frameStart = false;
		List<Byte> messageBytes = new ArrayList<Byte>();

		while (!stop) {
			try {
				int b = input.read();
				// TODO support binary frames
				if (b == 0x00) {
					frameStart = true;
				}
				else if (b == 0xff && frameStart == true) {
					frameStart = false;
					Byte[] message = messageBytes.toArray(new Byte[messageBytes.size()]);
					eventHandler.onMessage(websocket, new WebSocketMessage(message));
					messageBytes.clear();
				}
				else if (frameStart == true){
					messageBytes.add((byte)b);
				}
				else if (b == -1) {
					handleError();
				}
			}
			catch (IOException ioe) {
				handleError();
			}
		}
	}
	
	public void stopit() {
		stop = true;
	}
	
	public boolean isRunning() {
		return !stop;
	}
	
	private void handleError() {
		stopit();
		websocket.handleReceiverError();
	}

}
