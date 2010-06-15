/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jWebSocket.client;

import java.io.UnsupportedEncodingException;

/**
 *
 * @author aschulze
 */
public class WebSocketEvent {

	private byte[] data = null;

	/**
	 * @return the data
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(byte[] data) {
		this.data = data;
	}

	public void setData( String aString) {
		try {
			this.data = aString.getBytes("US-ASCII");
		} catch (UnsupportedEncodingException ex) {
		}
	}


}
