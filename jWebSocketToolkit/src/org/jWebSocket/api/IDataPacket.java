/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jWebSocket.api;

import java.io.UnsupportedEncodingException;

/**
 *
 * @author aschulze
 */
public interface IDataPacket {

	/**
	 *
	 * @param aByteArray
	 */
	void setByteArray(byte[] aByteArray);
	/**
	 *
	 * @param aString
	 */
	void setString(String aString);

	/**
	 *
	 * @param aString
	 * @param aEncoding
	 * @throws UnsupportedEncodingException
	 */
	void setString(String aString, String aEncoding) throws UnsupportedEncodingException;

	/**
	 *
	 * @param aString
	 */
	void setUTF8(String aString);


	/**
	 *
	 * @return
	 */
	byte[] getByteArray();
	
	/**
	 *
	 * @return
	 */
	String getString();

	/**
	 *
	 * @param aEncoding
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	String getString(String aEncoding) throws UnsupportedEncodingException;

	/**
	 *
	 * @return
	 */
	String getUTF8();

}
