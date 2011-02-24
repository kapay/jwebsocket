//	---------------------------------------------------------------------------
//	jWebSocket - WebSocket CGI Token Client
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
package org.jwebsocket.client.cgi;

import java.io.InputStream;
import java.io.OutputStream;

import org.jwebsocket.client.token.BaseTokenClient;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.kit.WebSocketException;

/**
 *
 * @author aschulze
 */
public class CGITokenClient extends BaseTokenClient {

    private final static char START_FRAME = 0x02; // ASCII STX
    private final static char END_FRAME = 0x03; // ASCII ETX
    private boolean mIsRunning = false;
    private Thread mInboundThread;
    private InboundProcess mInboundProcess;
    private InputStream mIn = null;
    private OutputStream mOut = null;
    private OutputStream mError = null;

    /**
     *
     * @param aListener
     */
    public CGITokenClient() {
    }

    @Override
    public void open(String aURL) throws WebSocketException {
        // establish connection to WebSocket Network
        super.open(aURL);

        // assign streams to CGI channels
        mIn = System.in;
        mOut = System.out;
        mError = System.err;

        // instantiate thread to process messages coming from stdIn
        mInboundProcess = new InboundProcess();
        mInboundThread = new Thread(mInboundProcess);
        mInboundThread.start();
    }

    @Override
    public void close() throws WebSocketException {
        // stop CGI listener
        mIsRunning = false;
        // and close WebSocket connection
        super.close();
    }

    private class InboundProcess implements Runnable {

        @Override
        public void run() {
            mIsRunning = true;
            byte[] lBuff = new byte[JWebSocketCommonConstants.DEFAULT_MAX_FRAME_SIZE];
            int lIdx = -1;
            int lStart = -1;

            while (mIsRunning) {
                try {
                    int lByte = mIn.read();
                    // start of frame
                    if (lByte == START_FRAME) {
                        lIdx = 0;
                        lStart = 0;
                        // end of frame
                    } else if (lByte == END_FRAME) {
                        if (lStart >= 0) {
                            byte[] lBA = new byte[lIdx];
                            System.arraycopy(lBuff, 0, lBA, 0, lIdx);
                            // Arrays class is not supported in Android
                            // byte[] lBA = Arrays.copyOf(lBuff, pos);
                            send(lBA);
                        }
                        lStart = -1;
                        // end of stream
                    } else if (lByte < 0) {
                        mIsRunning = false;
                        // any other byte within or outside a frame
                    } else {
                        if (lStart >= 0) {
                            lBuff[lIdx] = (byte) lByte;
                        }
                        lIdx++;
                    }
                } catch (Exception lEx) {
                    mIsRunning = false;
                    // throw new WebSocketException(ex.getClass().getSimpleName() + ": " + ex.getMessage());
                    // System.out.println(ex.getClass().getSimpleName() + ": " + ex.getMessage());
                }
            }

        }
    }
}
