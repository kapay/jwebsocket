/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jwebsocket.jetty;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author aschulze
 */
public class Test extends HttpServlet {
   
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param aRequest servlet request
     * @param aResponse servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest aRequest, HttpServletResponse aResponse)
    throws ServletException, IOException {
        aResponse.setContentType("text/html;charset=UTF-8");
        PrintWriter lOut = aResponse.getWriter();
        try {
            lOut.println("<html>");
            lOut.println("<head>");
            lOut.println("<title>Servlet Test</title>");
            lOut.println("</head>");
            lOut.println("<body>");
            lOut.println("<h1>Servlet Test at " + aRequest.getContextPath () + "</h1>");
            lOut.println("</body>");
            lOut.println("</html>");
        } finally { 
            lOut.close();
        }
    } 

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param aRequest servlet request
     * @param aResponse servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest aRequest, HttpServletResponse aResponse)
    throws ServletException, IOException {
        processRequest(aRequest, aResponse);
    } 

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param aRequest servlet request
     * @param aResponse servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest aRequest, HttpServletResponse aResponse)
    throws ServletException, IOException {
        processRequest(aRequest, aResponse);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
