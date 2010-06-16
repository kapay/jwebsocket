/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jwebsocket.api;

import org.jwebsocket.kit.WebSocketException;

/**
 *
 * @author aschulze
 */
public interface WebSocketClient {

	public void open(String aURL) throws WebSocketException;
	public void send(String aData, String aEncoding) throws WebSocketException;
	public void close() throws WebSocketException;

}
