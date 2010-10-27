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

/**
 *
 * @author aschulze
 */
public class jWebSocket extends WebSocketServlet {

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		System.out.print("@doGet");
		getServletContext().getNamedDispatcher("default").forward(request, response);
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

		// Outbound _outbound;
		@Override
		public void onConnect(Outbound outbound) {
			// _outbound = outbound;
		}

		@Override
		public void onMessage(byte frame, byte[] data, int offset, int length) {
		}

//		@Override
		public void onFragment(boolean aLast, byte frame, byte[] data, int offset, int length) {
		}

		@Override
		public void onMessage(byte frame, String data) {
		}

		@Override
		public void onDisconnect() {
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
