//	---------------------------------------------------------------------------
//	jWebSocket - Main Class: Command line IF, Logger Init and Args-Parser
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
package org.jWebSocket.console;

import org.apache.log4j.Logger;
import org.jWebSocket.api.WebSocketEngine;
import org.jWebSocket.config.Config;
import org.jWebSocket.engines.TCPEngine;
import org.jWebSocket.logging.Logging;
import org.jWebSocket.plugins.TokenPlugInChain;
import org.jWebSocket.plugins.rpc.RPCPlugIn;
import org.jWebSocket.plugins.streaming.StreamingPlugIn;
import org.jWebSocket.plugins.system.SystemPlugIn;
import org.jWebSocket.server.TokenServer;
import org.jWebsocket.server.CustomServer;

/**
 * This class is the main class for the jWebSocket server
 * @author aschulze
 * @version $Id:$
 */
public class JWebSocket {

    /**
     *
     */
    private static Logger log = Logger.getLogger(JWebSocket.class);

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        String prot = "json"; // [json|csv|xml|custom]
        String loglevel = "debug";
        int port = Config.DEFAULT_PORT;
        int sessionTimeout = Config.DEFAULT_TIMEOUT;

        // parse optional command line arguments
        int i = 0;
        while (i < args.length) {
            if (i + 1 < args.length) {
                if (args[i].equalsIgnoreCase("prot")) {
                    prot = args[i + 1].toLowerCase();
                } else if (args[i].equalsIgnoreCase("sessiontimeout")) {
                    try {
                        sessionTimeout = Integer.parseInt(args[i + 1].toLowerCase());
                    } catch (NumberFormatException ex) {
                        // ignore execption here
                    }
                } else if (args[i].equalsIgnoreCase("loglevel")) {
                    loglevel = args[i + 1].toLowerCase();
                } else if (args[i].equalsIgnoreCase("port")) {
                    try {
                        port = Integer.parseInt(args[i + 1].toLowerCase());
                    } catch (NumberFormatException ex) {
                        // ignore execption here
                    }
                }
            }
            i += 2;
        }

        // initialize log4j logging engine
        // BEFORE instantiating any jWebSocket classes
        Logging.initLogs(loglevel);


        // the following lines may not be removed due to GNU GPL 3.0 license!
        System.out.println("jWebSocket Ver. " + Config.VERSION_STR);
        System.out.println(Config.COPYRIGHT);
        System.out.println(Config.LICENSE);

        prot = prot.toLowerCase();
        if (!(prot.equals("json")
                || prot.equals("csv")
                || prot.equals("xml")
                || prot.equals("custom"))) {
            System.out.println("Invalid argument.");
            System.out.println("java -jar jWebSocket.jar prot=[json|csv|xml|custom] port=["
                    + Config.MIN_IN_PORT + "-" + Config.MAX_IN_PORT + "]");
            return;
        }
        System.out.println(
                "Listening on port " + port + ", default (sub)prot " + prot + ", "
                + "default session timeout: " + sessionTimeout + ", log-level: " + loglevel.toLowerCase());

        // create the low-level engine
        WebSocketEngine engine = null;
        try {
            engine = new TCPEngine(port, sessionTimeout);
			engine.startEngine();
        } catch (Exception ex) {
            System.out.println("Error instantating engine: " + ex.getMessage());
            return;
        }

        // create the token server (based on the TCP engine)
        TokenServer tokenServer = null;
        try {
            // instantiate the Token server and bind engine to it
            tokenServer = new TokenServer();
            // the token server already instantiates a plug-in chain
            TokenPlugInChain plugInChain = tokenServer.getPlugInChain();
            // let the server support the engine
            tokenServer.addEngine(engine);
            // add the SystemPlugIn listener (for the jWebSocket default functionality)
            plugInChain.addPlugIn(new SystemPlugIn());
            // add the RPCPlugIn plug-in
            plugInChain.addPlugIn(new RPCPlugIn());
            // add the streaming plug-in (e.g. for the time stream demo)
            plugInChain.addPlugIn(new StreamingPlugIn());

            log.debug("Starting token server...");
            tokenServer.startServer();
        } catch (Exception ex) {
            System.out.println("Error instantiating TokenServer: " + ex.getMessage());
        }

        // create the custom server (based on the TCP engine as well)
        CustomServer customServer = null;
        try {
            // instantiate the custom server and bind engine to it
            customServer = new CustomServer();
            // the custom server already instantiates a plug-in chain
            // BasePlugInChain plugInChain = customServer.getPlugInChain();
            // let the server support the engine
            customServer.addEngine(engine);
            // add the SystemPlugIn listener (for the jWebSocket default functionality)
            // customServer.addPlugIn(new SystemPlugIn());
            log.debug("Starting custom server...");
            customServer.startServer();
        } catch (Exception ex) {
            System.out.println("Error instantating CustomServer: " + ex.getMessage());
        }

        while (tokenServer.isAlive()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                log.debug(ex.getClass().getSimpleName() + " " + ex.getMessage());
            }
        }

        log.info("Server(s) successfully terminated.");
    }
}
