//	---------------------------------------------------------------------------
//	jWebSocket - Copyright (c) 2010 jwebsocket.org
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
//	more details.
//	You should have received a copy of the GNU General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/>.
//	---------------------------------------------------------------------------
package org.jwebsocket.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

import javolution.util.FastList;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jwebsocket.server.api.ConnectorContext;
import org.jwebsocket.server.api.JWebSocketConnector;
import org.jwebsocket.server.api.JWebSocketServer;
import org.jwebsocket.server.api.JWebSocketServerException;
import org.jwebsocket.server.impl.JWebSocketBaseServerHandler;
import org.jwebsocket.server.impl.JWebSocketServerPipeLineFactory;
import org.jwebsocket.server.impl.JWebSocketTokenConnector;

/**
 * Base server class which handles the operation specific to JWebSocket 
 * server and connector clients initialization and also is responsible for
 * triggering the various server and client events.
 * 
 * @author Alexander Schulze 
 * @author Puran Singh
 * @version $Id$
 * 
 */
public abstract class JWebSocketBaseServer implements JWebSocketServer {

	/**
	 * vendor string
	 */
	public static final String VENDOR = "jwebsocket.org";
	/**
	 * version number
	 */
	public static final String VERSION_STR = "0.5.1.0131 beta";

	/**
	 * default minimum port used by jWebSocket server
	 */
	public static final int MIN_PORT = 1024;
	/**
	 * default maximum port used by jWebSocket server
	 */
	public static final int MAX_PORT = 65535;
	/**
	 * default port used by jWebSocket server
	 */
	public static final int DEFAULT_PORT = 8787;
	/**
	 * default time out value
	 */
	public final static int DEFAULT_TIMEOUT = 120000;

	private final List<JWebSocketConnector> clients = new FastList<JWebSocketConnector>();
	
	private static final ChannelGroup allChannels = new DefaultChannelGroup("time-server");
	
	private int port = DEFAULT_PORT;
	
	//TODO: i need to figure this out.. it's weird i haven't used this.
	private int sessionTimeOut = DEFAULT_TIMEOUT;
	
	private volatile boolean isRunning = false;

	/**
	 * the constructor for the base jWebSocket server 
	 * @param port the port number
	 * @param timeout the timeout value
	 */
	public JWebSocketBaseServer(int port, int timeout) {
		this.port = port;
		this.sessionTimeOut = timeout;
	}
	
	////////////EVENT HANDLER METHODS ////////////////////
	/**
	 * this event is fired when the server is started for the first 
	 * time
	 */
	public void serverStarted() {
		// method may be overwriten on demand
	}

	/**
	 * this event is fired when the server is stopped 
	 */
	public void serverStopped() {
		// method may be overwriten on demand
	}

	/**
	 * this event is fired when the client connector is started
	 * @param client  the started client
	 */
	public void clientStarted(JWebSocketConnector client) {
	}
	
    ////////////EVENT HANDLER METHODS ////////////////////
	
	/**
	 *{@inheritDoc}
	 */
	public JWebSocketConnector createJWebSocketConnector(ConnectorContext context) {
		return new JWebSocketTokenConnector(context);
	}
	
	/**
	 * method that terminates the server on demand
	 */
	protected void terminate() {
		isRunning = false;
		try {
			// terminate all client threads
			for (JWebSocketConnector client : getClients()) {
				client.terminate();
			}
		} catch (Exception ex) {
		}
	}
	
	/**
	 * @param aPort
	 * @param aSessionTimeout
	 * @throws IOException
	 */
	protected void instantiateServer(int aPort, int aSessionTimeout) {
		this.port = aPort;
		this.sessionTimeOut = aSessionTimeout;
	}

	/**
	 * Starts the server on demand, initializes the server handler and use 
	 * Netty framework for actual start process.
	 */
	protected void start() {
		// Configure the server.
		//TODO: figure out more on how advanced we can configure
		ServerBootstrap bootstrap = new ServerBootstrap(
				new NioServerSocketChannelFactory(Executors
						.newCachedThreadPool(), Executors.newCachedThreadPool()));
		
		//initialize the server handler
		JWebSocketBaseServerHandler handler = new JWebSocketBaseServerHandler(this);
		
		// Set up the event pipeline factory.
		bootstrap.setPipelineFactory(new JWebSocketServerPipeLineFactory(handler));
		
		// Bind and start to accept incoming connections.
		Channel channel = bootstrap.bind(new InetSocketAddress(port));
		//fire the server started event
		serverStarted();
		
		allChannels.add(channel);
		
		isRunning = true;
		
		//wait for the server to terminate
        while(isRunning) {
        	;;
        }
        
        ChannelGroupFuture future = allChannels.close();
        future.awaitUninterruptibly();
        bootstrap.getFactory().releaseExternalResources();
	}

	/**
	 * returns {@code true} if server is running {@code false} otherwise.
	 * @return true or false 
	 */
	public boolean isAlive() {
		return true;
	}
	
	/**
	 * Returns the list of clients running against this server
	 * @return the read only client list
	 */
	public List<JWebSocketConnector> getClients() {
		return Collections.unmodifiableList(clients);
	}
	
	/**
	 * add the client to the list of clients
	 * @param client the connector client object
	 * @throws JWebSocketServerException 
	 */
	public void addClient(JWebSocketConnector client) throws JWebSocketServerException {
		if (client != null) {
			clients.add(client);
		} else {
			throw new JWebSocketServerException ("Cannot add null to the client list");
		}
	}

	/**
	 * removes the client from the list of clients being served by
	 * this server
	 * @param connector the connector client to remove from server list.
	 */
	public void remove(JWebSocketConnector connector) {
		getClients().remove(connector);
	}
}