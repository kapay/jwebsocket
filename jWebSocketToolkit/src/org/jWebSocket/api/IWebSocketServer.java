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
	 * starts the server and all underlying engines.
	 *
	 * @throws WebSocketException
	 */
	void startServer() throws WebSocketException;

	/**
	 * states if at least one of the engines is still running.
	 * @return
	 */
	boolean isAlive();

	/**
	 * stops the server and all underlying engines.
	 *
	 * @throws WebSocketException
	 */
	void stopServer() throws WebSocketException;

	/**
	 * adds a new engine to the server.
	 * @param aEngine
	 */
	void addEngine(IWebSocketEngine aEngine);

	/**
	 * removes a already bound engine from the server.
	 * @param aEngine
	 */
	void removeEngine(IWebSocketEngine aEngine);

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

}
