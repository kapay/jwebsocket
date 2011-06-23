//	---------------------------------------------------------------------------
//	jWebSocket - WebSocket TCP Engine
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
package org.jwebsocket.tcp.nio;

import org.apache.log4j.Logger;
import org.jwebsocket.api.EngineConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.engines.BaseEngine;
import org.jwebsocket.kit.*;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.tcp.EngineUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

/**
 * <p>
 * Tcp engine that uses java non-blocking io api to bind to listening port and handle incoming/outgoing packets.
 * There's one 'selector' thread that is responsible only for handling socket operations. Therefore, every packet that
 * should be sent will be firstly queued into concurrent queue, which is continuously processed by selector thread.
 * Since the queue is concurrent, there's no blocking and a call to send method will return immediately.
 * </p>
 * <p>
 * All packets that are received from remote clients are processed in separate worker threads. This way it's possible to
 * handle many clients simultaneously with just a few threads. Add more worker threads to handle more clients.
 * </p>
 * <p>
 * Before making any changes to this source, note this: it is highly advisable to read from (or write to) a socket
 * only in selector thread. Ignoring this advice may result in strange consequences (threads locking or
 * spinning, depending on actual scenario).
 * </p>
 *
 * @author jang
 */
public class NioTcpEngine extends BaseEngine {
	private static Logger mLog = Logging.getLogger(NioTcpEngine.class);
	// TODO: move following constants to settings
	private static final int READ_BUFFER_SIZE = 2048;
	private static final int NUM_WORKERS = 3;
	private static final int READ_QUEUE_MAX_SIZE = Integer.MAX_VALUE;

	private Selector selector;
	private ServerSocketChannel serverSocketChannel;
	private boolean running;
	private Map<String, Queue<DataFuture>> pendingWrites; // <connector id, data queue>
	private BlockingQueue<ReadBean> pendingReads;

	//worker threads
	private ExecutorService executorService;

	//convenience maps
	private Map<String, SocketChannel> connectorToChannelMap; // <connector id, socket channel>
	private Map<SocketChannel, String> channelToConnectorMap; // <socket channel, connector id>
	private ByteBuffer readBuffer;

	public NioTcpEngine(EngineConfiguration aConfiguration) {
		super(aConfiguration);
	}

	@Override
	public void startEngine() throws WebSocketException {
		try {
			pendingWrites = new ConcurrentHashMap<String, Queue<DataFuture>>();
			pendingReads = new LinkedBlockingQueue<ReadBean>(READ_QUEUE_MAX_SIZE);
			connectorToChannelMap = new ConcurrentHashMap<String, SocketChannel>();
			channelToConnectorMap = new ConcurrentHashMap<SocketChannel, String>();
			readBuffer = ByteBuffer.allocate(READ_BUFFER_SIZE);
			selector = Selector.open();
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.configureBlocking(false);
			ServerSocket socket = serverSocketChannel.socket();
			socket.bind(new InetSocketAddress(getConfiguration().getPort()));
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
			running = true;

			// start worker threads
			executorService = Executors.newFixedThreadPool(NUM_WORKERS);
			for (int i = 0; i < NUM_WORKERS; i++) {
				executorService.submit(new ReadWorker());
			}

			// start selector thread
			new Thread(new SelectorThread()).start();
		}
		catch (IOException e) {
			throw new WebSocketException(e.getMessage(), e);
		}
	}

	@Override
	public void stopEngine(CloseReason aCloseReason) throws WebSocketException {
		super.stopEngine(aCloseReason);
		if (selector != null) {
			try {
				running = false;
				selector.wakeup();
				serverSocketChannel.close();
				selector.close();
				pendingWrites.clear();
				pendingReads.notifyAll();
				pendingReads.clear();
				executorService.shutdown();
				mLog.info("Stopped nio engine");
			} catch (IOException e) {
				throw new WebSocketException(e.getMessage(), e);
			}
		}
	}

	public void send(String connectorId, DataFuture future) {
		if (pendingWrites.containsKey(connectorId)) {
			pendingWrites.get(connectorId).add(future);
			// Wake up waiting selector.
			selector.wakeup();
		} else {
			mLog.debug("Discarding packet for unattached socket channel, remote client is: "
					+ getConnectors().get(connectorId).getRemoteHost());
		}
	}

	@Override
	public void connectorStopped(WebSocketConnector conn, CloseReason aCloseReason) {
		Queue<DataFuture> wQueue = pendingWrites.remove(conn.getId());
		if (wQueue != null) {
			wQueue.clear();
		}

		if(connectorToChannelMap.containsKey(conn.getId())) {
			SocketChannel channel = connectorToChannelMap.remove(conn.getId());
			channelToConnectorMap.remove(channel);
		}

		super.connectorStopped(conn, aCloseReason);
	}

	/**
	 * Socket operations are permitted only via this thread. Strange behaviour will occur if anything is done to the
	 * socket outside of this thread.
	 */
	private class SelectorThread implements Runnable {
		@Override
		public void run() {
			engineStarted();
			while (running && selector.isOpen()) {
				// check if there's anything to write to any of the clients
				for (String id : pendingWrites.keySet()) {
					if (!pendingWrites.get(id).isEmpty()) {
						connectorToChannelMap.get(id).keyFor(selector).interestOps(SelectionKey.OP_WRITE);
					}
				}

				try {
					// Waits for 500ms for any data from connected clients or for new client connections.
					// We could have indefinite wait (selector.wait()), but it is good to check for 'running' variable
					// fairly often.
					if (selector.select(500) > 0 && running) {
						Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
						while (keys.hasNext()) {
							SelectionKey key = keys.next();
							keys.remove();
							if (key.isValid()) {
								try {
									if (key.isAcceptable()) {
										//accept new client connection
										accept(key);
									} else {
										if (key.isReadable()) {
											read(key);
										}

										if (key.isWritable()) {
											write(key);
										}
									}
								} catch (CancelledKeyException e) {
									// ignore, key was cancelled an instant after isValid() returned true,
									// most probably the client disconnected just at the wrong moment
								}
							}
						}
					} else {
						// nothing happened, continue looping ...
						mLog.trace("No data on listen port in 500ms timeout ...");
					}
				} catch (Exception e) {
					// something happened during socket operation (select, read or write), just log it
					mLog.error("Error during socket operation", e);
				}
			}
			engineStopped();
		}
	}

	// this must be called only from selector thread
	private void write(SelectionKey key) throws IOException {
		SocketChannel sc = (SocketChannel) key.channel();
		Queue<DataFuture> queue = pendingWrites.get(channelToConnectorMap.get(sc));
		while (!queue.isEmpty()) {
			DataFuture future = queue.peek();
			try {
				ByteBuffer data = future.getData();
				sc.write(data);
				if (data.remaining() > 0) {
					// socket's buffer is full, stop writing for now and leave the remaining
					// data in queue for another round of writing
					break;
				}
			} catch (IOException e) {
				future.setFailure(e);
				throw e;
			}

			future.setSuccess();
			// remove the head element of the queue
			queue.poll();
		}

		if (queue.isEmpty()) {
			key.interestOps(SelectionKey.OP_READ);
		}
	}

	// this must be called only from selector thread
	private void accept(SelectionKey key) throws IOException {
		try {
			SocketChannel socketChannel = ((ServerSocketChannel) key.channel()).accept();
			socketChannel.configureBlocking(false);
			socketChannel.register(selector, SelectionKey.OP_READ);

			WebSocketConnector connector = new NioTcpConnector(
					this, socketChannel.socket().getInetAddress(), socketChannel.socket().getPort());
			getConnectors().put(connector.getId(), connector);
			pendingWrites.put(connector.getId(), new ConcurrentLinkedQueue<DataFuture>());
			connectorToChannelMap.put(connector.getId(), socketChannel);
			channelToConnectorMap.put(socketChannel, connector.getId());
			mLog.info("New client accepted - remote ip=" + connector.getRemoteHost());
		} catch (IOException e) {
			mLog.warn("Could not accept new client connection");
			throw e;
		}
	}

	// this must be called only from selector thread
	private void read(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		readBuffer.clear();

		int numRead;
		try {
			numRead = socketChannel.read(readBuffer);
		} catch (IOException e) {
			// remote client probably disconnected uncleanly ?
			clientDisconnect(key);
			return;
		}

		if (numRead == -1) {
			// read channel closed, connection has ended
			clientDisconnect(key);
			return;
		}

		if (numRead > 0 && channelToConnectorMap.containsKey(socketChannel)) {
			String connectorId = channelToConnectorMap.get(socketChannel);
			ReadBean bean = new ReadBean();
			bean.connectorId = connectorId;
			bean.data = Arrays.copyOf(readBuffer.array(), numRead);
			boolean accepted = pendingReads.offer(bean);
			if (!accepted) {
				// Read queue is full, discard the packet.
				// This may happen under continuous heavy load (plugins cannot process packets in time) or
				// if all worker threads are locked up (perhaps a rogue plugin is blocking packet processing).
				mLog.warn("Engine read queue is full, discarding incoming packet");
			}
		}
	}

	private void clientDisconnect(SelectionKey key) throws IOException {
		clientDisconnect(key, CloseReason.CLIENT);
	}

	private void clientDisconnect(SelectionKey key, CloseReason reason) throws IOException {
		SocketChannel channel = (SocketChannel) key.channel();
		key.cancel();
		key.channel().close();
		if(channelToConnectorMap.containsKey(channel)) {
			String id = channelToConnectorMap.remove(channel);
			if(id != null) {
				connectorToChannelMap.remove(id);
				connectorStopped(getConnectors().get(id), reason);
			}
		}
	}

	private void clientDisconnect(WebSocketConnector connector) throws IOException {
		clientDisconnect(connector, CloseReason.CLIENT);
	}

	private void clientDisconnect(WebSocketConnector connector, CloseReason reason) throws IOException {
		if(connectorToChannelMap.containsKey(connector.getId())) {
			clientDisconnect(connectorToChannelMap.get(connector.getId()).keyFor(selector), reason);
		}
	}

	private class ReadBean {
		String connectorId;
		byte[] data;
	}

	private class ReadWorker implements Runnable {
		@Override
		public void run() {
			while (running) {
				try {
					final ReadBean bean = pendingReads.poll(200, TimeUnit.MILLISECONDS);
					if (bean != null) {
						if (getConnectors().containsKey(bean.connectorId)) {
							final NioTcpConnector connector = (NioTcpConnector) getConnectors().get(bean.connectorId);
							if(connector.getWorkerId() > -1 && connector.getWorkerId() != hashCode()) {
								// another worker is right in the middle of packet processing for this connector
								connector.setDelayedPacketNotifier(new DelayedPacketNotifier() {
									@Override
									public void handleDelayedPacket() throws IOException {
										doRead(connector, bean);
									}
								});
							} else {
								doRead(connector, bean);
							}
						} else {
							// connector was already closed ...
							mLog.debug("Discarding incoming packet, because there's no connector to process it");
						}
					}
				} catch (InterruptedException e) {
					// Ignore this exception -- waiting was interrupted, probably during engine stop ...
					break;
				} catch (Exception e) {
					// uncaught exception during packet processing - kill the worker (todo: think about worker restart)
					mLog.error("Unexpected exception during incoming packet processing", e);
					break;
				}
			}
		}

		private void doRead(NioTcpConnector connector, ReadBean bean) throws IOException {
			connector.setWorkerId(hashCode());
			if (connector.isAfterHandshake()) {
				boolean hixie = JWebSocketCommonConstants.WS_DRAFT_DEFAULT.equals(
						connector.getHeader().getDraft());
				if (hixie) {
					readHixie(bean.data, connector);
				} else {
					// assume that #02 and #03 are the same regarding packet processing
					readHybi(bean.data, connector);
				}
			} else {
				// todo: consider ssl connections
				Map headers = WebSocketHandshake.parseC2SRequest(bean.data, false);
				byte[] response = WebSocketHandshake.generateS2CResponse(headers);
				RequestHeader reqHeader = EngineUtils.validateC2SRequest(headers, mLog);
				if (response == null || reqHeader == null) {
					if (mLog.isDebugEnabled()) {
						mLog.warn("TCPEngine detected illegal handshake.");
					}

					// disconnect the client
					clientDisconnect(connector);
				}

				send(connector.getId(), new DataFuture(connector, ByteBuffer.wrap(response)));
				int timeout = reqHeader.getTimeout(getSessionTimeout());
				if(timeout > 0) {
					connectorToChannelMap.get(bean.connectorId).socket().setSoTimeout(timeout);
				}
				connector.handshakeValidated();
				connector.setHeader(reqHeader);
				connector.startConnector();
			}
			connector.setWorkerId(-1);
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
	private void readHybi(byte[] buffer, NioTcpConnector connector) throws IOException {
		try {
			if(connector.isPacketBufferEmpty()) {
				// begin normal packet read
				int lFlags = buffer[0];
				// determine fragmentation
				boolean lFragmented = (0x01 & lFlags) == 0x01;
				// shift 4 bits to skip the first bit and three RSVx bits
				int lType = lFlags >> 4;
				int lPacketType = WebSocketProtocolHandler.toRawPacketType(lType);
				int payloadStartIndex = 2;

				if (lPacketType == -1) {
					// Could not determine packet type, ignore the packet.
					// Maybe we need a setting to decide, if such packets should abort the connection?
					mLog.trace("Dropping packet with unknown type: " + lType);
				} else {
					connector.setPacketType(lPacketType);
					// Ignore first bit. Payload length is next seven bits, unless its value is greater than 125.
					long lPayloadLen = buffer[1] >> 1;
					if (lPayloadLen == 126) {
						// following two bytes are acutal payload length (16-bit unsigned integer)
						lPayloadLen = (buffer[2] << 8) + buffer[3];
						payloadStartIndex = 4;
					} else if (lPayloadLen == 127) {
						// Following eight bytes are actual payload length (64-bit unsigned integer),
						// but that's ridiculously big number for an array size - in fact, such big arrays are
						// unsupported in Java. Feel free to make an array of int arrays to support that. I won't
						// do it, because it's too much work and it's just plain stupid for clients to send
						// such giant packets. So, if payload size is greater than Integer.MAX_VALUE, client will
						// be disconnected.
						lPayloadLen =
							((long) buffer[2] << 56) +
							((long)(buffer[3] & 255) << 48) +
							((long)(buffer[4] & 255) << 40) +
							((long)(buffer[5] & 255) << 32) +
							((long)(buffer[6] & 255) << 24) +
							((buffer[7] & 255) << 16) +
							((buffer[8] & 255) <<  8) +
							((buffer[9] & 255));
						if(lPayloadLen > Integer.MAX_VALUE) {
							clientDisconnect(connector);
							return;
						}
						payloadStartIndex = 10;
					}

					if(lPayloadLen > 0) {
						connector.setPayloadLength((int) lPayloadLen);
						connector.extendPacketBuffer(buffer, payloadStartIndex, buffer.length - payloadStartIndex);
					}
				}

				if (lPacketType == RawPacket.FRAMETYPE_PING) {
					// As per spec, server must respond to PING with PONG (maybe
					// this should be handled higher up in the hierarchy?)
					WebSocketPacket lPong = new RawPacket(connector.getPacketBuffer());
					lPong.setFrameType(RawPacket.FRAMETYPE_PONG);
					connector.sendPacket(lPong);
				} else if (lPacketType == RawPacket.FRAMETYPE_CLOSE) {
					// As per spec, server must respond to CLOSE with acknowledgment CLOSE (maybe
					// this should be handled higher up in the hierarchy?)
					WebSocketPacket lClose = new RawPacket(connector.getPacketBuffer());
					lClose.setFrameType(RawPacket.FRAMETYPE_CLOSE);
					connector.sendPacket(lClose);
					clientDisconnect(connector, CloseReason.CLIENT);
				}
			} else {
				connector.extendPacketBuffer(buffer, 0, buffer.length);
			}
			
			if(connector.isPacketBufferFull()) {
				// Packet was read, pass it forward.
				connector.flushPacketBuffer();
			}
		} catch (Exception e) {
			mLog.error("(other) " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
			clientDisconnect(connector, CloseReason.SERVER);
		}
	}

	private void readHixie(byte[] buffer, NioTcpConnector connector) throws IOException {
		try {
			int start = 0;
			if(connector.isPacketBufferEmpty() && buffer[0] == 0x00) {
				// start of packet
				start = 1;
			}

			boolean stop = false;
			int count = buffer.length;
			for(int i = start;i < buffer.length;i++) {
				if(buffer[i] == (byte)0xFF) {
					// end of packet
					count = i - start;
					stop = true;
					break;
				}
			}

			if(start + count > buffer.length) {
				// ignore -> broken packet (perhaps client disconnected in middle of sending
			} else {
				if(connector.isPacketBufferEmpty() && buffer.length == 1) {
					connector.extendPacketBuffer(buffer, 0, 0);
				} else {
					connector.extendPacketBuffer(buffer, start, count);
				}
			}

			if(stop) {
				connector.flushPacketBuffer();
			}
		} catch(Exception e) {
			mLog.error("Error while processing incoming packet", e);
			clientDisconnect(connector, CloseReason.SERVER);
		}
	}
}
