//	---------------------------------------------------------------------------
//	jWebSocket - Basic PlugIn Class
//	Copyright (c) 2010 Alexander Schulze, Innotrade GmbH
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
package org.jwebsocket.plugins;

import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.api.WebSocketPlugIn;
import org.jwebsocket.api.WebSocketPlugInChain;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.api.WebSocketServer;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.server.BaseServer;

/**
 *
 * @author aschulze
 */
public abstract class BasePlugIn implements WebSocketPlugIn {

	private WebSocketPlugInChain plugInChain = null;

	@Override
	public abstract void engineStarted(WebSocketEngine aEngine);

	@Override
	public abstract void engineStopped(WebSocketEngine aEngine);

	/**
	 *
	 * @param aConnector
	 */
	@Override
	public abstract void connectorStarted(WebSocketConnector aConnector);

	/**
	 *
	 * @param aResponse 
	 * @param aConnector
	 * @param aDataPacket
	 */
	@Override
	public abstract void processPacket(PlugInResponse aResponse, WebSocketConnector aConnector, WebSocketPacket aDataPacket);

	/**
	 *
	 * @param aConnector
	 * @param aCloseReason
	 */
	@Override
	public abstract void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason);

	/**
	 *
	 * @param aPlugInChain
	 */
	@Override
	public void setPlugInChain(WebSocketPlugInChain aPlugInChain) {
		plugInChain = aPlugInChain;
	}

	/**
	 * @return the plugInChain
	 */
	@Override
	public WebSocketPlugInChain getPlugInChain() {
		return plugInChain;
	}

	/**
	 *
	 * @return
	 */
	public WebSocketServer getServer() {
		WebSocketServer lServer = null;
		if (plugInChain != null) {
			lServer = plugInChain.getServer();
		}
		return lServer;
	}

	/**
	 * Convenience method, just a wrapper for token server method
	 * <tt>getUsername</tt> to simplify token plug-in code.
	 * @param aConnector
	 * @return
	 */
	public String getUsername(WebSocketConnector aConnector) {
		return getServer().getUsername(aConnector);
	}

	/**
	 * Convenience method, just a wrapper for token server method
	 * <tt>setUsername</tt> to simplify token plug-in code.
	 * @param aConnector
	 * @param aUsername
	 */
	public void setUsername(WebSocketConnector aConnector, String aUsername) {
		getServer().setUsername(aConnector, aUsername);
	}

	/**
	 * Convenience method, just a wrapper for token server method
	 * <tt>removeUsername</tt> to simplify token plug-in code.
	 * @param aConnector
	 */
	public void removeUsername(WebSocketConnector aConnector) {
		getServer().removeUsername(aConnector);
	}

	/**
	 * Convenience method, just a wrapper for token server method
	 * <tt>getNodeId</tt> to simplify token plug-in code.
	 * @param aConnector
	 * @return
	 */
	public String getNodeId(WebSocketConnector aConnector) {
		return getServer().getNodeId(aConnector);
	}

	/**
	 * Convenience method, just a wrapper for token server method
	 * <tt>setNodeId</tt> to simplify token plug-in code.
	 * @param aConnector
	 * @param aNodeId
	 */
	public void setNodeId(WebSocketConnector aConnector, String aNodeId) {
		getServer().setNodeId(aConnector, aNodeId);
	}

	/**
	 * Convenience method, just a wrapper for token server method
	 * <tt>removeNodeId</tt> to simplify token plug-in code.
	 * @param aConnector
	 */
	public void removeNodeId(WebSocketConnector aConnector) {
		getServer().removeNodeId(aConnector);
	}

	/**
	 * Convenience method, just a wrapper for token server method
	 * <tt>getConnector</tt> to simplify token plug-in code.
	 * @param aId
	 * @return
	 */
	public WebSocketConnector getConnector(String aId) {
		return (aId != null ? getServer().getConnector(aId) : null);
	}

	/**
	 * Convenience method, just a wrapper for token server method
	 * <tt>getNode</tt> to simplify token plug-in code.
	 * @param aNodeId
	 * @return
	 */
	public WebSocketConnector getNode(String aNodeId) {
		return (aNodeId != null ? getServer().getNode(aNodeId) : null);
	}

	/**
	 * Convenience method, just a wrapper for token server method
	 * <tt>getServer().getAllConnectors().size()</tt> to simplify token
	 * plug-in code.
	 * @return
	 */
	public int getConnectorCount() {
		return getServer().getAllConnectors().size();
	}
}
