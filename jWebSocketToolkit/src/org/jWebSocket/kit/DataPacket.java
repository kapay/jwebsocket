/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jWebSocket.kit;

import java.io.UnsupportedEncodingException;
import org.jWebSocket.api.IDataPacket;

/**
 *
 * @author aschulze
 */
public class DataPacket implements IDataPacket {

	byte[] data = null;

	public DataPacket(byte[] aByteArray) {
		setByteArray(aByteArray);
	}

	public DataPacket(String aString) {
		setString(aString);
	}

	public DataPacket(String aString, String aEncoding)
		throws UnsupportedEncodingException {
		setString(aString, aEncoding);
	}

	public void setByteArray(byte[] aByteArray) {
		data = aByteArray;
	}

	public void setString(String aString) {
		data = aString.getBytes();
	}

	public void setString(String aString, String aEncoding)
		throws UnsupportedEncodingException {
		data = aString.getBytes(aEncoding);
	}

	public byte[] getByteArray() {
		return data;
	}

	public String getString() {
		return new String(data);
	}

	public String getString(String aEncoding)
		throws UnsupportedEncodingException {
		return new String(data, aEncoding);
	}
}
