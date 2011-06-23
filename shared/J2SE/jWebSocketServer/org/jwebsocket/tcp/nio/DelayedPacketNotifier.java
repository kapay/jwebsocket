package org.jwebsocket.tcp.nio;

import java.io.IOException;

/**
 * @author jang
 */
interface DelayedPacketNotifier {
	void handleDelayedPacket() throws IOException;
}
