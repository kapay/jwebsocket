package org.jwebsocket.client;

import org.jwebsocket.client.nio.JWebSocketClient;
import org.jwebsocket.kit.WebSocketException;

public class JWebSocketClientTest {
    JWebSocketClient client = new JWebSocketClient();
    public static void main(String... args) {
        new JWebSocketClientTest().start();
    }
    private void start() {
        try {
            client.open("ws://localhost:8787/resource");
            System.out.println(client.isConnected());
        } catch (WebSocketException e) {
            e.printStackTrace();
        }
    }
}
