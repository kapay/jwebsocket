package org.jWebSocket.connectors;

import java.util.HashMap;
import org.jWebSocket.api.IDataPacket;
import org.jWebSocket.api.IWebSocketConnector;
import org.jWebSocket.api.IWebSocketEngine;
import org.jWebSocket.kit.Header;

/**
 *
 * @author aschulze
 */
public class BaseConnector implements IWebSocketConnector {

	private IWebSocketEngine engine = null;
	private Header header = null;
	private HashMap<String, Object> customVars = new HashMap<String, Object>();

	public BaseConnector(IWebSocketEngine aEngine) {
		engine = aEngine;
	}

	public void startConnector() {
		engine.connectorStarted(this);
	}

	public void stopConnector() {
		engine.connectorStopped(this);
	}

	public void processPacket(IDataPacket aDataPacket) {
		engine.processPacket(this, aDataPacket);
	}

	public void sendPacket(IDataPacket aDataPacket) {
	}

	/**
	 * @return the engine
	 */
	public IWebSocketEngine getEngine() {
		return engine;
	}

	/**
	 * @return the header
	 */
	public Header getHeader() {
		return header;
	}

	/**
	 * @param header the header to set
	 */
	public void setHeader(Header header) {
		this.header = header;
	}

	public Object getVar(String aKey) {
		return customVars.get(aKey);
	}

	public void setVar(String aKey, Object aValue) {
		customVars.put(aKey, aValue);
	}

	public boolean getBoolean(String aKey) {
		return (Boolean) getVar(aKey);
	}

	public void setBoolean(String aKey, boolean aValue) {
		setVar(aKey, aValue);
	}

	public String getString(String aKey) {
		return (String) getVar(aKey);
	}

	public void setString(String aKey, String aValue) {
		setVar(aKey, aValue);
	}

	public int getInt(String aKey) {
		return (Integer) getVar(aKey);
	}

	public void setInt(String aKey, int aValue) {
		setVar(aKey, aValue);
	}
}
