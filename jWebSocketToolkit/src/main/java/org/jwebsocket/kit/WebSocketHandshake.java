/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.kit;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URI;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author aschulze
 */
public class WebSocketHandshake {

	/**
	 * Generates the initial handshake request from a client
	 * to the jWebSocket Server.
	 * @param aURI
	 * @return
	 */
	public static byte[] generateC2SRequest(URI aURI) {
		String lPath = aURI.getPath();
		String lHost = aURI.getHost();
		String lOrigin = "http://" + lHost;
		String lHandshake = "GET " + lPath + " HTTP/1.1\r\n"
				+ "Upgrade: WebSocket\r\n"
				+ "Connection: Upgrade\r\n"
				+ "Host: " + lHost + "\r\n"
				+ "Origin: " + lOrigin + "\r\n"
				+ "\r\n";
		byte[] lBA = null;
		try {
			lBA = lHandshake.getBytes("US-ASCII");
		} catch (Exception ex) {
		}
		return lBA;
	}

	private static long calcSecKeyNum(String aKey) {
		StringBuffer lSB = new StringBuffer();
		int lSpaces = 0;
		for (int i = 0; i < aKey.length(); i++) {
			char lC = aKey.charAt(i);
			if (lC == ' ') {
				lSpaces++;
			} else if (lC >= '0' && lC <= '9') {
				lSB.append(lC);
			}
		}
		long lRes = -1;
		if (lSpaces > 0) {
			try {
				lRes = Long.parseLong(lSB.toString()) / lSpaces;
//				log.debug("Key: " + aKey + ", Numbers: " + lSB.toString() + ", Spaces: " + lSpaces + ", Result: " + lRes);
			} catch (NumberFormatException ex) {
				// use default result
			}
		}
		return lRes;
	}

	/**
	 * Parses the response from the server on an initial handshake request.
	 * @param aResp
	 * @return
	 */
	public static Map parseS2CResponse(byte[] aResp) {
		String lHost = null;
		String lOrigin = null;
		String lLocation = null;
		String lPath = null;
		String lSecKey1 = null;
		String lSecKey2 = null;
		byte[] lSecKey3 = new byte[8];
		Boolean lIsSecure = false;
		Long lSecNum1 = null;
		Long lSecNum2 = null;
		byte[] lSecKeyResp = new byte[8];

		int lRespLen = aResp.length;
		String lResp = "";
		try {
			lResp = new String(aResp, "US-ASCII");
		} catch (Exception ex) {
			// TODO: add exception handling
		}
		lIsSecure = (lResp.indexOf("Sec-WebSocket") > 0);

		if (lIsSecure) {
			lRespLen -= 8;
			for (int i = 0; i < 8; i++) {
				lSecKey3[i] = aResp[lRespLen + i];
			}
		}
		/*
		if (log.isDebugEnabled()) {
		log.debug("Received "
		+ (isSecure ? "secured" : "unsecured")
		+ " Header (" + aResp.replace("\r\n", "\\n") + ")");
		}
		 */
		// now parse header for correct handshake....
		// get host....
		int lPos = lResp.indexOf("Host:");
		lPos += 6;
		lHost = lResp.substring(lPos);
		lPos = lHost.indexOf("\r\n");
		lHost = lHost.substring(0, lPos);
		// get origin....
		lPos = lResp.indexOf("Origin:");
		lPos += 8;
		lOrigin = lResp.substring(lPos);
		lPos = lOrigin.indexOf("\r\n");
		lOrigin = lOrigin.substring(0, lPos);
		// get path....
		lPos = lResp.indexOf("GET");
		lPos += 4;
		lPath = lResp.substring(lPos);
		lPos = lPath.indexOf("HTTP");
		lPath = lPath.substring(0, lPos - 1);

		lLocation = "ws://" + lHost + lPath;

		// the following section implements the sec-key process in WebSocket Draft 76
		/*
		To prove that the handshake was received, the server has to take
		three pieces of information and combine them to form a response.  The
		first two pieces of information come from the |Sec-WebSocket-Key1|
		and |Sec-WebSocket-Key2| fields in the client handshake.

		Sec-WebSocket-Key1: 18x 6]8vM;54 *(5:  {   U1]8  z [  8
		Sec-WebSocket-Key2: 1_ tx7X d  <  nw  334J702) 7]o}` 0

		For each of these fields, the server has to take the digits from the
		value to obtain a number (in this case 1868545188 and 1733470270
		respectively), then divide that number by the number of spaces
		characters in the value (in this case 12 and 10) to obtain a 32-bit
		number (155712099 and 173347027).  These two resulting numbers are
		then used in the server handshake, as described below.
		 */
		lPos = lResp.indexOf("Sec-WebSocket-Key1:");
		if (lPos > 0) {
			lPos += 20;
			lSecKey1 = lResp.substring(lPos);
			lPos = lSecKey1.indexOf("\r\n");
			lSecKey1 = lSecKey1.substring(0, lPos);
			lSecNum1 = calcSecKeyNum(lSecKey1);
//			log.debug("Sec-WebSocket-Key1:" + secKey1 + " => " + secNum1);
		}
		lPos = lResp.indexOf("Sec-WebSocket-Key2:");
		if (lPos > 0) {
			lPos += 20;
			lSecKey2 = lResp.substring(lPos);
			lPos = lSecKey2.indexOf("\r\n");
			lSecKey2 = lSecKey2.substring(0, lPos);
			lSecNum2 = calcSecKeyNum(lSecKey2);
//			log.debug("Sec-WebSocket-Key2:" + secKey2 + " => " + secNum2);
		}

		/*
		The third piece of information is given after the fields, in the last
		eight bytes of the handshake, expressed here as they would be seen if
		interpreted as ASCII: Tm[K T2u
		The concatenation of the number obtained from processing the |Sec-
		WebSocket-Key1| field, expressed as a big-endian 32 bit number, the
		number obtained from processing the |Sec-WebSocket-Key2| field, again
		expressed as a big-endian 32 bit number, and finally the eight bytes
		at the end of the handshake, form a 128 bit string whose MD5 sum is
		then used by the server to prove that it read the handshake.
		 */

		if (lSecNum1 != null && lSecNum2 != null) {

//			log.debug("Sec-WebSocket-Key3:" + new String(secKey3, "UTF-8"));
			BigInteger sec1 = new BigInteger(lSecNum1.toString());
			BigInteger sec2 = new BigInteger(lSecNum2.toString());

			// concatene 3 parts secNum1 + secNum2 + secKey
			byte[] l128Bit = new byte[16];
			byte[] lTmp;
			lTmp = sec1.toByteArray();
			for (int i = 0; i < 4; i++) {
				l128Bit[i] = lTmp[i];
			}
			lTmp = sec2.toByteArray();
			for (int i = 0; i < 4; i++) {
				l128Bit[i + 4] = lTmp[i];
			}
			lTmp = lSecKey3;
			for (int i = 0; i < 8; i++) {
				l128Bit[i + 8] = lTmp[i];
			}
			// build md5 sum of this new 128 byte string
			try {
				MessageDigest md = MessageDigest.getInstance("MD5");
				lSecKeyResp = md.digest(l128Bit);
			} catch (Exception ex) {
//				log.error("getMD5: " + ex.getMessage());
			}
		}

		HashMap lRes = new HashMap();

		lRes.put("path", lPath);
		lRes.put("host", lHost);
		lRes.put("origin", lOrigin);
		lRes.put("location", lLocation);
		lRes.put("secKey1", lSecKey1);
		lRes.put("secKey2", lSecKey2);

		lRes.put("isSecure", lIsSecure);
		lRes.put("secKeyResponse", lSecKeyResp);

		return lRes;
	}

	/**
	 * Generates the response for the server to answer
	 * an initial client request.
	 * @param aRequest
	 * @return
	 */
	public static byte[] generateS2CResponse(Map aRequest) {
		// now that we have parsed the header send handshake...
		// since 0.9.0.0609 considering Sec-WebSocket-Key processing
		Boolean lIsSecure = (Boolean) aRequest.get("isSecure");
		String lOrigin = (String) aRequest.get("origin");
		String lLocation = (String) aRequest.get("location");
		String lRes =
				"HTTP/1.1 101 Web Socket Protocol Handshake\r\n"
				+ "Upgrade: WebSocket\r\n"
				+ "Connection: Upgrade\r\n"
				+ (lIsSecure ? "Sec-" : "") + "WebSocket-Origin: " + lOrigin + "\r\n"
				+ (lIsSecure ? "Sec-" : "") + "WebSocket-Location: " + lLocation + "\r\n"
				+ "\r\n";

		byte[] lBA;
		try {
			lBA = lRes.getBytes("US-ASCII");
			// if Sec-WebSocket-Keys are used send security response first
			if (lIsSecure) {
				byte[] lSecKey = (byte[]) aRequest.get("secKeyResponse");
				byte[] lResult = new byte[lBA.length + lSecKey.length];
				System.arraycopy(lBA, 0, lRes, 0, lBA.length);
				System.arraycopy(lSecKey, lSecKey.length, lRes, lBA.length, lSecKey.length);
				return lResult;
			} else {
				return lBA;
			}
		} catch (UnsupportedEncodingException ex) {
			return null;
		}

	}
}
