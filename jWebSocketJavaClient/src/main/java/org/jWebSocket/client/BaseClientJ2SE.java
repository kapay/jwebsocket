/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jWebSocket.client;

import org.jwebsocket.kit.WebSocketEvent;
import org.jwebsocket.api.WebSocketListener;
import org.jwebsocket.kit.WebSocketHandshake;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import org.jwebsocket.kit.WebSocketException;

/**
 *
 * @author aschulze
 */
public class BaseClientJ2SE extends BaseClient {

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

	@Override
	public void open(String aURL) throws WebSocketException {
		try {
			url = new URI(aURL);
			String lHost = url.getHost();
			int lPort = url.getPort();

			socket = new Socket(lHost, lPort);
			is = socket.getInputStream();
			os = socket.getOutputStream();

			// send handshake to server
			byte[] lReq = WebSocketHandshake.generateC2SRequest(url);
			os.write(lReq);
			os.flush();

			// wait on handshake response
			byte[] lBuff = new byte[8192];
			int lRead = is.read(lBuff);
			Map lResp = WebSocketHandshake.parseS2CResponse(lBuff);


			inboundProcess = new InboundProcess();
			inboundThread = new Thread(inboundProcess);
			inboundThread.start();

		} catch (Exception ex) {
			throw new WebSocketException(ex.getClass().getSimpleName() + " when opening WebSocket connection: " + ex.getMessage());
			// System.out.println(ex.getClass().getSimpleName() + ": " + ex.getMessage());
		}
	}

	@Override
	public void send(String aData, String aEncoding) throws WebSocketException {
		try {
			os.write(0x00);
			os.write(aData.getBytes(aEncoding));
			os.write(0xff);
			os.flush();
		} catch (Exception ex) {
			throw new WebSocketException(ex.getClass().getSimpleName() + " when sending via WebSocket connection: " + ex.getMessage());
			// System.out.println(ex.getClass().getSimpleName() + ": " + ex.getMessage());
		}
	}

	@Override
	public void close() throws WebSocketException {
		isRunning = false;
		try {
			os.close();
			is.close();
			socket.close();
		} catch (Exception ex) {
			throw new WebSocketException(ex.getClass().getSimpleName() + " when closing WebSocket connection: " + ex.getMessage());
			// System.out.println(ex.getClass().getSimpleName() + ": " + ex.getMessage());
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
					// start of frame
					if (b == 0x00) {
						pos = 0;
						lStart = 0;
					// end of frame
					} else if (b == 0xff) {
						if (lStart >= 0) {
							if (listener != null) {
								WebSocketEvent lEvt = new WebSocketEvent();
								lEvt.setData(Arrays.copyOf(lBuff, pos));
								listener.processPacket(lEvt);
							}
						}
						lStart = -1;
					// end of stream
					} else if( b < 0 ) {
						isRunning = false;
					// any other byte within or outside a frame
					} else {
						if (lStart >= 0) {
							lBuff[pos] = (byte) b;
						}
						pos++;
					}
				} catch (Exception ex) {
					// throw new WebSocketException(ex.getClass().getSimpleName() + ": " + ex.getMessage());
					// System.out.println(ex.getClass().getSimpleName() + ": " + ex.getMessage());
				}
			}

			listener.processClosed(null);
		}
	}
}
