/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.jetty;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocket.Outbound;
import org.eclipse.jetty.websocket.WebSocketServlet;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.RawPacket;

/**
 *
 * @author aschulze
 */
public class jWebSocket extends WebSocketServlet {

	WebSocketEngine mEngine = new JettyEngine(null);

	@Override
	protected void doGet(HttpServletRequest aRequest, HttpServletResponse aResponse)
			throws ServletException, IOException {
		System.out.print("@doGet");
		getServletContext().getNamedDispatcher("default").forward(aRequest, aResponse);
	}

	/**
	 * Handles the HTTP <code>POST</code> method.
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		System.out.println("@doPost");
	}

	@Override
	protected WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
		System.out.println("@doWebSocketConnect");
		return new JWebSocketWrapper();
	}

	class JWebSocketWrapper implements WebSocket {

		private WebSocketConnector mConnector = null;
		// Outbound _outbound;

		@Override
		public void onConnect(Outbound aOutbound) {
			// _outbound = aOutbound;
			mConnector = new JettyConnector(mEngine, null);
			mEngine.addConnector(mConnector);
			// inherited BaseConnector.startConnector
			// calls mEngine connector started
			mConnector.startConnector();
		}

		@Override
		public void onMessage(byte frame, byte[] data, int offset, int length) {
			if (mConnector != null) {
				WebSocketPacket lDataPacket = new RawPacket(data);
				mEngine.processPacket(mConnector, null);
			}
		}

		@Override
		public void onMessage(byte frame, String data) {
			if (mConnector != null) {
				WebSocketPacket lDataPacket = new RawPacket(data);
				mEngine.processPacket(mConnector, null);
			}
		}

		@Override
		public void onDisconnect() {
			if (mConnector != null) {
				// inherited BaseConnector.stopConnector
				// calls mEngine connector stopped
				mConnector.stopConnector(CloseReason.SERVER);
				mEngine.removeConnector(mConnector);
			}
		}
	}

	/**
	 * Returns a short description of the servlet.
	 * @return a String containing servlet description
	 */
	@Override
	public String getServletInfo() {
		return "Short description";
	}
}
