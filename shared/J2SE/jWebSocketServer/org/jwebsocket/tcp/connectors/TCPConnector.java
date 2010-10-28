//	---------------------------------------------------------------------------
//	jWebSocket - TCP Connector
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
package org.jwebsocket.tcp.connectors;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.async.IOFuture;
import org.jwebsocket.connectors.BaseConnector;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.RawPacket;
import org.jwebsocket.logging.Logging;

/**
 * Implementation of the jWebSocket TCP socket connector.
 * 
 * @author aschulze
 */
public class TCPConnector extends BaseConnector {

    private static Logger mLog = Logging.getLogger(TCPConnector.class);
    private InputStream mIn = null;
    private OutputStream mOut = null;
    private Socket mClientSocket = null;
    private boolean mIsRunning = false;
    private CloseReason mCloseReason = CloseReason.TIMEOUT;

    /**
     * creates a new TCP connector for the passed engine using the passed client
     * socket. Usually connectors are instantiated by their engine only, not by
     * the application.
     * 
     * @param aEngine
     * @param aClientSocket
     */
    public TCPConnector(WebSocketEngine aEngine, Socket aClientSocket) {
        super(aEngine);
        mClientSocket = aClientSocket;
        try {
            mIn = mClientSocket.getInputStream();
            mOut = new PrintStream(mClientSocket.getOutputStream(), true, "UTF-8");
        } catch (Exception lEx) {
            mLog.error(lEx.getClass().getSimpleName() 
					+ " instantiating "
					+ getClass().getSimpleName() + ": "
                    + lEx.getMessage());
        }
    }

    @Override
    public void startConnector() {
        int lPort = -1;
        int lTimeout = -1;
        try {
            lPort = mClientSocket.getPort();
            lTimeout = mClientSocket.getSoTimeout();
        } catch (Exception lEx) {
        }
        if (mLog.isDebugEnabled()) {
            mLog.debug("Starting TCP connector on port " + lPort + " with timeout "
                    + (lTimeout > 0 ? lTimeout + "ms" : "infinite") + "");
        }
        ClientProcessor lClientProc = new ClientProcessor(this);
        Thread lClientThread = new Thread(lClientProc);
        lClientThread.start();
        if (mLog.isInfoEnabled()) {
            mLog.info("Started TCP connector on port " + lPort + " with timeout "
                    + (lTimeout > 0 ? lTimeout + "ms" : "infinite") + "");
        }
    }

    @Override
    public void stopConnector(CloseReason aCloseReason) {
        if (mLog.isDebugEnabled()) {
            mLog.debug("Stopping TCP connector (" + aCloseReason.name() + ")...");
        }
        int lPort = mClientSocket.getPort();
        mCloseReason = aCloseReason;
        mIsRunning = false;

        try {
            mIn.close();
            if (mLog.isInfoEnabled()) {
                mLog.info("Stopped TCP connector (" + aCloseReason.name() + ") on port " + lPort + ".");
            }
        } catch (IOException lEx) {
            if (mLog.isDebugEnabled()) {
                mLog.info(lEx.getClass().getSimpleName() + " while stopping TCP connector (" + aCloseReason.name()
                        + ") on port " + lPort + ": " + lEx.getMessage());
            }
        }
    }

    @Override
    public void processPacket(WebSocketPacket aDataPacket) {
        // forward the data packet to the engine
        getEngine().processPacket(this, aDataPacket);
    }

    @Override
    public synchronized void sendPacket(WebSocketPacket aDataPacket) {
        try {
            if (aDataPacket.getFrameType() == RawPacket.FRAMETYPE_BINARY) {
                // each packet is enclosed in 0xFF<length><data>
                // TODO: for future use! Not yet finally spec'd in IETF drafts!
                mOut.write(0xFF);
                byte[] lBA = aDataPacket.getByteArray();
                // TODO: implement multi byte length!
                mOut.write(lBA.length);
                mOut.write(lBA);
            } else {
                // each packet is enclosed in 0x00<data>0xFF
                mOut.write(0x00);
                mOut.write(aDataPacket.getByteArray());
                mOut.write(0xFF);
            }
            mOut.flush();
        } catch (IOException lEx) {
            mLog.error(lEx.getClass().getSimpleName() + " sending data packet: " + lEx.getMessage());
        }
    }
    
    @Override
    public IOFuture sendPacketAsync(WebSocketPacket aDataPacket) {
        throw new UnsupportedOperationException("Underlying connector:"+getClass().getName()+" doesn't support asynchronous send operation");
    }

    private class ClientProcessor implements Runnable {

        private WebSocketConnector mConnector = null;

        /**
         * Creates the new socket listener thread for this connector.
         * 
         * @param aConnector
         */
        public ClientProcessor(WebSocketConnector aConnector) {
            mConnector = aConnector;
        }

        @Override
        public void run() {
            WebSocketEngine lEngine = getEngine();
            ByteArrayOutputStream lBuff = new ByteArrayOutputStream();

            try {
                // start client listener loop
                mIsRunning = true;

                // call connectorStarted method of engine
                lEngine.connectorStarted(mConnector);

                while (mIsRunning) {
                    try {
                        int lIn = mIn.read();
                        // start of frame
                        if (lIn == 0x00) {
                            lBuff.reset();
                            // end of frame
                        } else if (lIn == 0xff) {
                            RawPacket lPacket = new RawPacket(lBuff.toByteArray());
                            try {
                                lEngine.processPacket(mConnector, lPacket);
                            } catch (Exception lEx) {
                                mLog.error(lEx.getClass().getSimpleName() + " in processPacket of connector "
                                        + mConnector.getClass().getSimpleName() + ": " + lEx.getMessage());
                            }
                            lBuff.reset();
                        } else if (lIn < 0) {
                            mCloseReason = CloseReason.CLIENT;
                            mIsRunning = false;
                            // any other byte within or outside a frame
                        } else {
                            lBuff.write(lIn);
                        }
                    } catch (SocketTimeoutException lEx) {
                        mLog.error("(timeout) " + lEx.getClass().getSimpleName() + ": " + lEx.getMessage());
                        mCloseReason = CloseReason.TIMEOUT;
                        mIsRunning = false;
                    } catch (Exception lEx) {
                        mLog.error("(other) " + lEx.getClass().getSimpleName() + ": " + lEx.getMessage());
                        mCloseReason = CloseReason.SERVER;
                        mIsRunning = false;
                    }
                }

                // call client stopped method of engine
                // (e.g. to release client from streams)
                lEngine.connectorStopped(mConnector, mCloseReason);

                // br.close();
                mIn.close();
                mOut.close();
                mClientSocket.close();

            } catch (Exception lEx) {
                // ignore this exception for now
                mLog.error("(close) " + lEx.getClass().getSimpleName() + ": " + lEx.getMessage());
            }
        }
    }

    @Override
    public String generateUID() {
        String lUID = mClientSocket.getInetAddress().getHostAddress() + "@" + mClientSocket.getPort();
        return lUID;
    }

    @Override
    public int getRemotePort() {
        return mClientSocket.getPort();
    }

    @Override
    public InetAddress getRemoteHost() {
        return mClientSocket.getInetAddress();
    }

    @Override
    public String toString() {
        // TODO: weird results like... '0:0:0:0:0:0:0:1:61130'... on JDK 1.6u19
        // Windows 7 64bit
        String lRes = getRemoteHost().getHostAddress() + ":" + getRemotePort();
        // TODO: don't hard code. At least use JWebSocketConstants field here.
        String lUsername = getString("org.jwebsocket.plugins.system.username");
        if (lUsername != null) {
            lRes += " (" + lUsername + ")";
        }
        return lRes;
    }
}
