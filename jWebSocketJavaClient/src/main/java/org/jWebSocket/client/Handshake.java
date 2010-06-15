/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jWebSocket.client;

import java.net.URI;

/**
 *
 * @author aschulze
 */
public class Handshake {

	public static String createHandshake(URI aURI) {
		String lPath = aURI.getPath();
		String lHost = aURI.getHost();
		String lOrigin = "http://" + lHost;
		String lHandshake = "GET " + lPath + " HTTP/1.1\r\n"
				+ "Upgrade: WebSocket\r\n"
				+ "Connection: Upgrade\r\n"
				+ "Host: " + lHost + "\r\n"
				+ "Origin: " + lOrigin + "\r\n"
				+ "\r\n";
		return lHandshake;
	}



}
