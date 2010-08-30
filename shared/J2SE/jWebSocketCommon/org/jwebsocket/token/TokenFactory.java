/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jwebsocket.token;

/**
 *
 * @author aschulze
 */
public class TokenFactory {

	public static Token createToken() {
		return new MapToken();
	}

	public static Token createToken(String aType) {
		return new MapToken(aType);
	}

	public static Token createToken(String aNS, String aType) {
		return new MapToken(aNS, aType);
	}

}
