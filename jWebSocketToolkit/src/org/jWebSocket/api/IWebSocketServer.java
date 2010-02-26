/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jWebSocket.api;

import org.jWebSocket.kit.WebSocketException;

/**
 *
 * @author aschulze
 */
public interface IWebSocketServer {

	/**
	 * starts the server and the underlying engine.
	 */
	void startServer() throws WebSocketException;

	/**
	 * stops the server and the underlying engine.
	 */
	void stopServer() throws WebSocketException;

	/**
	 * is called from the underlying engine when the engine is started.
	 * @param aEngine
	 */
	void engineStarted(IWebSocketEngine aEngine);

	/**
	 * is called from the underlying engine when the engine is stopped.
	 * @param aEngine
	 */
	void engineStopped(IWebSocketEngine aEngine);

	/**
	 * notifies the application that a client connector has been started.
	 * @param aConnector
	 */
	void connectorStarted(IWebSocketConnector aConnector);

	/**
	 * notifies the application that a client connector has been stopped.
	 * @param aConnector
	 */
	void connectorStopped(IWebSocketConnector aConnector);

	/**
	 * is called when the underlying engine received a packet from a connector.
	 * @param aEngine
	 * @param aConnector
	 * @param aDataPacket
	 */
	void processPacket(IWebSocketEngine aEngine, IWebSocketConnector aConnector, IDataPacket aDataPacket);

	/**
	 * sends a packet to a certain connector.
	 * @param aConnector 
	 * @param aDataPacket
	 */
	void sendPacket(IWebSocketConnector aConnector, IDataPacket aDataPacket);

	/**
	 * broadcasts a datapacket to all connectors.
	 * @param aDataPacket
	 */
	void broadcastPacket(IDataPacket aDataPacket);

	/**
	 * adds a plugin to the plugin chain of the server.
	 * @param aPlugIn
	 */
	void addPlugIn(IPlugIn aPlugIn);
}
