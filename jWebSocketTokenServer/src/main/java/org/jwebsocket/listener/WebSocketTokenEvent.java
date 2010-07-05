/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.listener;

import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketServer;
import org.jwebsocket.kit.BroadcastOptions;
import org.jwebsocket.kit.WebSocketEvent;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.Token;

/**
 *
 * @author aschulze
 */
public class WebSocketTokenEvent extends WebSocketEvent {

	/**
	 * 
	 * @param aConnector
	 * @param aServer
	 */
	public WebSocketTokenEvent(WebSocketConnector aConnector, WebSocketServer aServer) {
		super(aConnector, aServer);
	}

	/**
	 *
	 * @param aToken
	 */
	public void sendToken(Token aToken) {
		((TokenServer) getServer()).sendToken(getConnector(), aToken);
	}

	/**
	 * Convenience method, just a wrapper for token server method
	 * <tt>createResponse</tt> to simplify token plug-in code.
	 * @param aInToken
	 * @return
	 */
	public Token createResponse(Token aInToken) {
		TokenServer lServer = (TokenServer) getServer();
		if (lServer != null) {
			return lServer.createResponse(aInToken);
		} else {
			return null;
		}
	}

	/**
	 * Convenience method, just a wrapper for token server method
	 * <tt>createAccessDenied</tt> to simplify token plug-in code.
	 * @param aInToken
	 * @return
	 */
	public Token createAccessDenied(Token aInToken) {
		TokenServer lServer = (TokenServer) getServer();
		if (lServer != null) {
			return lServer.createAccessDenied(aInToken);
		} else {
			return null;
		}
	}

	/**
	 * Convenience method, just a wrapper for token server method
	 * <tt>sendToken</tt> to simplify token plug-in code.
	 * @param aSource
	 * @param aTarget
	 * @param aToken
	 */
	public void sendToken(WebSocketConnector aSource, WebSocketConnector aTarget, Token aToken) {
		TokenServer lServer = (TokenServer) getServer();
		if (lServer != null) {
			lServer.sendToken(aSource, aTarget, aToken);
		}
	}

	/**
	 * Convenience method, just a wrapper for token server method
	 * <tt>sendToken</tt> to simplify token plug-in code.
	 * @param aSource
	 * @param aTarget
	 * @param aToken
	 */
	/*
	public void sendToken(WebSocketConnector aTarget, Token aToken) {
	TokenServer lServer = getServer();
	if (lServer != null) {
	lServer.sendToken(aTarget, aToken);
	}
	}
	 */
	/**
	 * Convenience method, just a wrapper for token server method
	 * <tt>sendToken</tt> to simplify token plug-in code.
	 * @param aSource
	 * @param aTarget
	 * @param aToken
	 */
	public void broadcastToken(WebSocketConnector aSource, Token aToken) {
		TokenServer lServer = (TokenServer) getServer();
		if (lServer != null) {
			lServer.broadcastToken(aSource, aToken);
		}
	}

	public void broadcastToken(WebSocketConnector aSource, Token aToken,
			BroadcastOptions aBroadcastOptions) {
		TokenServer lServer = (TokenServer) getServer();
		if (lServer != null) {
			lServer.broadcastToken(aSource, aToken, aBroadcastOptions);
		}
	}

	/**
	 * Convenience method, just a wrapper for token server method
	 * <tt>getUsername</tt> to simplify token plug-in code.
	 * @param aSource
	 * @param aTarget
	 * @param aToken
	 */
	public String getUsername(WebSocketConnector aConnector) {
		return ((TokenServer) getServer()).getUsername(aConnector);
	}

	/**
	 * Convenience method, just a wrapper for token server method
	 * <tt>setUsername</tt> to simplify token plug-in code.
	 * @param aSource
	 * @param aTarget
	 * @param aToken
	 */
	public void setUsername(WebSocketConnector aConnector, String aUsername) {
		((TokenServer) getServer()).setUsername(aConnector, aUsername);
	}

	/**
	 * Convenience method, just a wrapper for token server method
	 * <tt>removeUsername</tt> to simplify token plug-in code.
	 * @param aSource
	 * @param aTarget
	 * @param aToken
	 */
	public void removeUsername(WebSocketConnector aConnector) {
		((TokenServer) getServer()).removeUsername(aConnector);
	}

	/**
	 * Convenience method, just a wrapper for token server method
	 * <tt>getConnector</tt> to simplify token plug-in code.
	 * @param aSource
	 * @param aTarget
	 * @param aToken
	 */
	public WebSocketConnector getConnector(String aId) {
		return ((TokenServer) getServer()).getConnector(aId);
	}

	/**
	 * Convenience method, just a wrapper for token server method
	 * <tt>getServer().getAllConnectors().size()</tt> to simplify token
	 * plug-in code.
	 * @param aSource
	 * @param aTarget
	 * @param aToken
	 */
	public int getConnectorCount() {
		return ((TokenServer) getServer()).getAllConnectors().size();
	}
}
