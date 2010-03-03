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
public interface WebSocketServer {

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
	void addEngine(WebSocketEngine aEngine);

	/**
	 * removes a already bound engine from the server.
	 * @param aEngine
	 */
	void removeEngine(WebSocketEngine aEngine);

	/**
	 * is called from the underlying engine when the engine is started.
	 * @param aEngine
	 */
	void engineStarted(WebSocketEngine aEngine);

	/**
	 * is called from the underlying engine when the engine is stopped.
	 * @param aEngine
	 */
	void engineStopped(WebSocketEngine aEngine);

	/**
	 * notifies the application that a client connector has been started.
	 * @param aConnector
	 */
	void connectorStarted(WebSocketConnector aConnector);

	/**
	 * notifies the application that a client connector has been stopped.
	 * @param aConnector
	 */
	void connectorStopped(WebSocketConnector aConnector);

	/**
	 * is called when the underlying engine received a packet from a connector.
	 * @param aEngine
	 * @param aConnector
	 * @param aDataPacket
	 */
	void processPacket(WebSocketEngine aEngine, WebSocketConnector aConnector, IDataPacket aDataPacket);

	/**
	 * sends a packet to a certain connector.
	 * @param aConnector 
	 * @param aDataPacket
	 */
	void sendPacket(WebSocketConnector aConnector, IDataPacket aDataPacket);

	/**
	 * broadcasts a datapacket to all connectors.
	 * @param aDataPacket
	 */
	void broadcastPacket(IDataPacket aDataPacket);

}
