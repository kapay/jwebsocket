/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jWebSocket.appserver;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.jWebSocket.demo.DemoPlugIn;
import org.jWebSocket.plugins.KeepAlivePlugIn;
import org.jWebSocket.plugins.PlugInChain;
import org.jWebSocket.plugins.RPCPlugIn;
import org.jWebSocket.plugins.SystemPlugIn;
import org.jWebSocket.logging.Logging;
import org.jWebSocket.server.TokenServer;
import org.jWebSocket.server.UserServer;

/**
 * Web application lifecycle listener.
 * @author aschulze
 */
public class ContextListener implements ServletContextListener {

	private TokenServer jwss_token = null;
	private UserServer jwss_user = null;

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		System.out.println("Initialising Context...");

		Logging.initLogs("debug");

		// run jWebSocket User Server on Port 8787
		// don't miss to release the according firewall port!
		System.out.println("Starting custom server on 8787...");
		jwss_user = new UserServer(8787, 120000, null);
		jwss_user.start();

		// run jWebSocket Token Server on Port 8788
		// include some plug-ins for demonstration purposes
		// don't miss to release the according firewall port!

		PlugInChain plugins = new PlugInChain();
		plugins.add(new SystemPlugIn());
		plugins.add(new KeepAlivePlugIn());
		plugins.add(new RPCPlugIn());
		plugins.add(new DemoPlugIn());

		// instantiate the JSON server and bind demo listeners to it
		System.out.println("Starting Token server on 8788...");
		jwss_token = new TokenServer(8788, 120000, plugins);
		jwss_token.start();

	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		jwss_token.terminate();
		jwss_user.terminate();
	}
}
