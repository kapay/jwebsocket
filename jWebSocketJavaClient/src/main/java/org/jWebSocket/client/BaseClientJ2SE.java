/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jWebSocket.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.util.Arrays;
import org.jwebsocket.kit.WebSocketException;

/**
 *
 * @author aschulze
 */
public class BaseClientJ2SE {

	/*
	 * The connection has not yet been established.
	 */
	public static final int CONNECTING = 0;
	/*
	 * The WebSocket connection is established and communication is possible.
	 */
	public static final int OPEN = 1;
	/*
	 * The connection is going through the closing handshake.
	 */
	public static final int CLOSING = 2;
	/*
	 * The connection has been closed or could not be opened.
	 */
	public static final int CLOSED = 3;
	/*
	 * The maximum amount of bytes per frame
	 */
	public static final int MAX_FRAMESIZE = 16384;
	private boolean isRunning = false;
	private Thread inboundThread;
	private InboundProcess inboundProcess;
	private URI url = null;
	private WebSocketListener listener = null;
	private Socket socket = null;
	private InputStream is = null;
	private OutputStream os = null;

	public BaseClientJ2SE(WebSocketListener aListener) {
		// assign listener
		listener = aListener;
	}

	public void open(String aURL) throws WebSocketException {

		try {
			url = new URI(aURL);
			String lHost = url.getHost();
			int lPort = url.getPort();
			socket = new Socket(lHost, lPort);
			is = socket.getInputStream();
			os = socket.getOutputStream();

			// send handshake to server
			String lHandshake = Handshake.createHandshake(url);
			os.write(lHandshake.getBytes("US-ASCII"));
			os.flush();

			// wait on handshake response
			byte[] lBuff = new byte[8192];
			int lRead = is.read(lBuff);
			String lResponse = new String(lBuff, 0, lRead, "US-ASCII");

			System.out.println(lResponse);

			inboundProcess = new InboundProcess();
			inboundThread = new Thread(inboundProcess);
			inboundThread.start();

		} catch (Exception ex) {
			System.out.println(ex.getClass().getSimpleName() + ": " + ex.getMessage());
		}
	}

	public void send(String aData, String aEncoding) {
		try {
			os.write(0x00);
			os.write(aData.getBytes(aEncoding));
			os.write(0xff);
			os.flush();
		} catch (Exception ex) {
			System.out.println(ex.getClass().getSimpleName() + ": " + ex.getMessage());
		}
	}

	public void close() {
		isRunning = false;
		try {
			os.close();
			is.close();
			socket.close();
		} catch (IOException ex) {
			System.out.println(ex.getClass().getSimpleName() + ": " + ex.getMessage());
		}
	}

	private class InboundProcess implements Runnable {

		@Override
		public void run() {
			isRunning = true;
			byte[] lBuff = new byte[MAX_FRAMESIZE];
			int pos = -1;
			int lStart = -1;

			listener.processOpened(null);

			while (isRunning) {
				try {
					int b = is.read();
					if (b == 0x00) {
						pos = 0;
						lStart = 0;
					} else if (b == 0xff) {
						if (lStart >= 0) {
							if (listener != null) {
								WebSocketEvent lEvt = new WebSocketEvent();
								lEvt.setData(Arrays.copyOf(lBuff, pos));
								listener.processPacket(lEvt);
							}
						}
						lStart = -1;
					} else {
						if (lStart >= 0) {
							lBuff[pos] = (byte) b;
						}
						pos++;
					}
				} catch (Exception ex) {
					System.out.println(ex.getClass().getSimpleName() + ": " + ex.getMessage());
				}
			}

			listener.processClosed(null);
		}
	}
}
