//	---------------------------------------------------------------------------
//	jWebSocket - Base Connector Implementation
//	Copyright (c) 2010 Alexander Schulze, Innotrade GmbH
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
//	more details.
//	You should have received a copy of the GNU General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/>.
//	---------------------------------------------------------------------------
package org.jWebSocket.connectors;

import java.net.InetAddress;
import java.util.HashMap;
import org.jWebSocket.api.WebSocketPaket;
import org.jWebSocket.api.WebSocketConnector;
import org.jWebSocket.api.WebSocketEngine;
import org.jWebSocket.kit.RequestHeader;

/**
 * Provides the basic implementation of the jWebSocket connectors.
 * The {@code BaseConnector} is supposed to be used as ancestor for the
 * connector implementations like e.g. the {@code TCPConnector} or the
 * {@code NettyConnector }.
 * @author aschulze
 */
public class BaseConnector implements WebSocketConnector {

	private WebSocketEngine engine = null;
	private RequestHeader header = null;
	private HashMap<String, Object> customVars = new HashMap<String, Object>();

	/**
	 * 
	 * @param aEngine
	 */
	public BaseConnector(WebSocketEngine aEngine) {
		engine = aEngine;
	}

	public void startConnector() {
		engine.connectorStarted(this);
	}

	public void stopConnector() {
		engine.connectorStopped(this);
	}

	public void processPacket(WebSocketPaket aDataPacket) {
		engine.processPacket(this, aDataPacket);
	}

	public void sendPacket(WebSocketPaket aDataPacket) {
	}

	/**
	 * @return the engine
	 */
	public WebSocketEngine getEngine() {
		return engine;
	}

	/**
	 * @return the header
	 */
	public RequestHeader getHeader() {
		return header;
	}

	/**
	 * @param header the header to set
	 */
	public void setHeader(RequestHeader header) {
		this.header = header;
	}

	public Object getVar(String aKey) {
		return customVars.get(aKey);
	}

	public void setVar(String aKey, Object aValue) {
		customVars.put(aKey, aValue);
	}

	public Boolean getBoolean(String aKey) {
		return (Boolean) getVar(aKey);
	}

	public boolean getBool(String aKey) {
		Boolean lBool = getBoolean(aKey);
		return (lBool != null && lBool);
	}

	public void setBoolean(String aKey, Boolean aValue) {
		setVar(aKey, aValue);
	}

	public String getString(String aKey) {
		return (String) getVar(aKey);
	}

	public void setString(String aKey, String aValue) {
		setVar(aKey, aValue);
	}

	public Integer getInteger(String aKey) {
		return (Integer) getVar(aKey);
	}

	public void setInteger(String aKey, Integer aValue) {
		setVar(aKey, aValue);
	}

	public void removeVar(String aKey) {
		customVars.remove(aKey);
	}

	public String generateUID() {
		return null;
	}

	public int getRemotePort() {
		return -1;
	}

	public InetAddress getRemoteHost() {
		return null;
	}
}
