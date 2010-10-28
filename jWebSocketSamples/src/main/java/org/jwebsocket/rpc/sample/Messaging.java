package org.jwebsocket.rpc.sample;

import java.util.ArrayList;
import java.util.List;

import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.plugins.rpc.BaseConnectorRPCCallable;
import org.jwebsocket.plugins.rpc.rrpc.Rrpc;

public class Messaging extends BaseConnectorRPCCallable {
	private List<String> messages = new ArrayList<String>();
	
	public Messaging(WebSocketConnector aConnector) {
		super(aConnector);
		messages.add("Hello Quentin");
		messages.add("How Are You ?");
	}

	/**
	 * Send the list of messages to the connector throw a RRPC. We will supposed this
	 * task can be long (access to the messages from a hudge database, for instance)
	 */
	public void getMyMessages() {
		//We get the current connector:
		WebSocketConnector connector = getConnector();
		for (int i = 0; i < messages.size(); i++) {
			String message = messages.get(i);
			new Rrpc("org.jwebsocket.android.demo.RPCDemo", "receiveMessage").to(connector).send(message).call();
		}
	}
}
