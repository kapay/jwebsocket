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

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.async.IOFuture;
import org.jwebsocket.config.JWebSocketCommonConstants;
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

        if(!isHixieDraft()) {
            // Hybi specs demand that client must be notified with CLOSE control message before disconnect
            WebSocketPacket close = new RawPacket("BYE");
            close.setFrameType(RawPacket.FRAMETYPE_CLOSE);
            sendPacket(close);
        }

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
            if(isHixieDraft()) {
                sendHixie(aDataPacket);
            } else {
                sendHybi(aDataPacket);
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

                if(isHixieDraft()) {
                    readHixie(lBuff, lEngine);
                } else {
                    // assume that #02 and #03 are the same regarding packet processing
                    readHybi(lBuff, lEngine);
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

        private void readHixie(ByteArrayOutputStream lBuff, WebSocketEngine lEngine) throws IOException {
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
        }

        /**
         *  One message may consist of one or more (fragmented message) protocol packets.
         *  The spec is currently unclear whether control packets (ping, pong, close) may
         *  be intermingled with fragmented packets of another message. For now I've
         *  decided to not implement such packets 'swapping', and therefore reading fails
         *  miserably if a client sends control packets during fragmented message read.
         *  TODO: follow next spec drafts and add support for control packets inside fragmented message if needed.
         *  <p>
         *  Structure of packets conforms to the following scheme (copied from spec):
         *  </p>
         *  <pre>
         *  0                   1                   2                   3
         *  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
         * +-+-+-+-+-------+-+-------------+-------------------------------+
         * |M|R|R|R| opcode|R| Payload len |    Extended payload length    |
         * |O|S|S|S|  (4)  |S|     (7)     |             (16/63)           |
         * |R|V|V|V|       |V|             |   (if payload len==126/127)   |
         * |E|1|2|3|       |4|             |                               |
         * +-+-+-+-+-------+-+-------------+ - - - - - - - - - - - - - - - +
         * |     Extended payload length continued, if payload len == 127  |
         * + - - - - - - - - - - - - - - - +-------------------------------+
         * |                               |         Extension data        |
         * +-------------------------------+ - - - - - - - - - - - - - - - +
         * :                                                               :
         * +---------------------------------------------------------------+
         * :                       Application data                        :
         * +---------------------------------------------------------------+
         * </pre>
         * RSVx bits are ignored (reserved for future use).
         * TODO: add support for extension data, when extensions will be defined in the specs.
         *
         * <p>
         * Read section 4.2 of the spec for detailed explanation.
         * </p>
         */
        private void readHybi(ByteArrayOutputStream lBuff, WebSocketEngine lEngine) throws IOException {
            int lPacketType = -1;
            // utilize data input stream, because it has convenience methods for reading
            // signed/unsigned bytes, shorts, ints and longs
            DataInputStream mDis = new DataInputStream(mIn);

            while (mIsRunning) {
                try {
                    // begin normal packet read
                    int flags = mDis.read();
                    // determine fragmentation
                    boolean lFragmented = (0x01 & flags) == 0x01;
                    // shift 4 bits to skip the first bit and three RSVx bits
                    int lType = flags >> 4;
                    switch(lType) {
                        // continuation frame (if we have fragmented packets)
                        case 0: lPacketType = RawPacket.FRAMETYPE_FRAGMENT; break;
                        // connection close
                        case 1: {
                            lPacketType = RawPacket.FRAMETYPE_CLOSE;
                            mCloseReason = CloseReason.CLIENT;
                            mIsRunning = false;
                            break;
                        }
                        // ping, respond with pong!
                        case 2: lPacketType = RawPacket.FRAMETYPE_PING; break;
                        // pong,
                        case 3: lPacketType = RawPacket.FRAMETYPE_PONG; break;
                        // text data
                        case 4: lPacketType = RawPacket.FRAMETYPE_UTF8; break;
                        // binary data
                        case 5: lPacketType = RawPacket.FRAMETYPE_BINARY; break;
                        // other types are reserved for future use
                        default: break;
                    }

                    if(lPacketType == -1) {
                        // Could not determine packet type, ignore the packet.
                        // Maybe we need a setting to decide, if such packets should abort the connection?
                        mLog.trace("Dropping packet with unknown type: " + lType);
                    } else {
                        // Ignore first bit. Payload length is next seven bits, unless its value is greater than 125.
                        long payloadLen = mIn.read() >> 1;
                        if(payloadLen == 126) {
                            // following two bytes are acutal payload length (16-bit unsigned integer)
                            payloadLen = mDis.readUnsignedShort();
                        } else if(payloadLen == 127) {
                            // following eight bytes are actual payload length (64-bit unsigned integer)
                            payloadLen = mDis.readLong();
                        }

                        if(payloadLen > 0) {
                            // payload length may be extremely long, so we read in loop rather
                            // than construct one byte[] array and fill it with read() method,
                            // because java does not allow longs as array size
                            while(payloadLen-- > 0)
                            {
                                lBuff.write(mDis.read());
                            }
                        }

                        if(!lFragmented) {
                            if(lPacketType == RawPacket.FRAMETYPE_PING) {
                                // As per spec, server must respond to PING with PONG (maybe
                                // this should be handled higher up in the hierarchy?)
                                WebSocketPacket pong = new RawPacket(lBuff.toByteArray());
                                pong.setFrameType(RawPacket.FRAMETYPE_PONG);
                                sendPacket(pong);
                            } else if (lPacketType == RawPacket.FRAMETYPE_CLOSE) {
                                // As per spec, server must respond tlPacketType = RawPacket.FRAMETYPEo CLOSE with acknowledgment CLOSE (maybe
                                // this should be handled higher up in the hierarchy?)
                                WebSocketPacket close = new RawPacket(lBuff.toByteArray());
                                close.setFrameType(RawPacket.FRAMETYPE_CLOSE);
                                sendPacket(close);
                            }

                            // Packet was read, pass it forward.
                            WebSocketPacket lPacket = new RawPacket(lBuff.toByteArray());
                            lPacket.setFrameType(lPacketType);
                            try {
                                lEngine.processPacket(mConnector, lPacket);
                            } catch (Exception lEx) {
                                mLog.error(lEx.getClass().getSimpleName() + " in processPacket of connector "
                                        + mConnector.getClass().getSimpleName() + ": " + lEx.getMessage());
                            }
                            lBuff.reset();
                        }
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


    private void sendHixie(WebSocketPacket aDataPacket) throws IOException {
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
    }

    // TODO: implement fragmentation for packet sending
    private void sendHybi(WebSocketPacket aDataPacket) throws IOException {
        int lType = aDataPacket.getFrameType();
        int lTargetType;
        switch(lType) {
            case RawPacket.FRAMETYPE_CLOSE: lTargetType = 0x01; break;
            case RawPacket.FRAMETYPE_PING: lTargetType = 0x02; break;
            case RawPacket.FRAMETYPE_PONG: lTargetType = 0x03; break;
            case RawPacket.FRAMETYPE_UTF8: lTargetType = 0x04; break;
            case RawPacket.FRAMETYPE_BINARY: lTargetType = 0x05; break;
            default: throw new IOException("Cannot construct a packet with unknown packet type: " + lType);
        }

        // just shift four bits to the left (MORE and RSVx bits are not set)
        lTargetType = lTargetType << 4;
        mOut.write(lTargetType);

        int lPayloadLen = aDataPacket.getByteArray().length;
        int lTargetPayloadLen = lPayloadLen << 1;
        mOut.write(lTargetPayloadLen);
        // Here, the spec allows payload length with up to 64-bit integer
        // in size (that is long data type in java):
        // ----
        //   The length of the payload: if 0-125, that is the payload length.
        //   If 126, the following 2 bytes interpreted as a 16 bit unsigned
        //   integer are the payload length.  If 127, the following 8 bytes
        //   interpreted as a 64-bit unsigned integer (the high bit must be 0)
        //   are the payload length.
        // ----
        // However, arrays in java may only have Integer.MAX_VALUE(32-bit) elements.
        // Therefore, we never set target payload length to 127.
        if(lPayloadLen > 126)
        {
            mOut.write((lPayloadLen >>> 8) & 0xFF);
            mOut.write(lPayloadLen & 0xFF);
        }

        mOut.write(aDataPacket.getByteArray());
    }

    private boolean isHixieDraft()
    {
        return JWebSocketCommonConstants.WS_DRAFT_DEFAULT.equals(getHeader().getDraft());
    }
}
