//	---------------------------------------------------------------------------
//	jWebSocket - RPCPlugIn Plug-In
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
package org.jwebsocket.plugins.rpc;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketConstants;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.security.SecurityFactory;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.Token;

/**
 * This plug-in provides all the functionality for remote procedure calls
 * (RPC) for client-to-server (C2S) apps, and reverse remote procedure calls
 * (RRPC) for server-to-client (S2C) or client-to-client apps (C2C).
 * @author aschulze
 */
public class RPCPlugIn extends TokenPlugIn {

	private static Logger log = Logging.getLogger(RPCPlugIn.class);
	private HashMap<String, Object> grantedProcs = new HashMap<String, Object>();
	private DemoRPCServer rpcServer = null;
	// if namespace changed update client plug-in accordingly!
	private String NS_RPC_DEFAULT = JWebSocketConstants.NS_BASE + ".plugins.rpc";

	// TODO: RRPC calls do not yet allow quotes in arguments
	// TODO: We need simple unique IDs to address a certain target, session id not suitable here.
	// TODO: Show target(able) clients in a drop down box
	// TODO: RPC demo does not show other clients logging in
	/**
	 *
	 */
	public RPCPlugIn() {
		if (log.isDebugEnabled()) {
			log.debug("Instantiating rpc plug-in...");
		}
		// specify default name space
		this.setNamespace(NS_RPC_DEFAULT);
		// specify granted remnote procedure calls
		// todo: Make configurable
		grantedProcs.put("org.jWebSocket.demo.DemoRPCServer.getMD5", null);

		// currently this is the only supported RPCPlugIn server
		rpcServer = new DemoRPCServer();
	}
/*
	@Override
	public void connectorStarted(WebSocketConnector aConnector) {
	}
*/
	@Override
	public void processToken(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();

		if (lType != null && (lNS == null || lNS.equals(getNamespace()))) {
			// remote procedure call
			if (lType.equals("rpc")) {
				rpc(aConnector, aToken);
				// reverse remote procedure call
			} else if (lType.equals("rrpc")) {
				rrpc(aConnector, aToken);
			}
		}
	}

	/**
	 * remote procedure call
	 * @param aConnector 
	 * @param aToken
	 */
	public void rpc(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		if (!SecurityFactory.checkRight(lServer.getUsername(aConnector), NS_RPC_DEFAULT + ".rpc")) {
			lServer.sendToken(aConnector, lServer.createAccessDenied(aToken));
			return;
		}

		Token lResponseToken = lServer.createResponse(aToken);

		// currently rpcServer is the only supported RPCPlugIn server!
		String lClassName = aToken.getString("classname");
		String lMethod = aToken.getString("method");
		String lArgs = aToken.getString("args");
		String lMsg = null;

		if (log.isDebugEnabled()) {
			log.debug("Processing RPC to class '" + lClassName + "', method '" + lMethod + "', args: '" + lArgs + "'...");
		}

		String lKey = lClassName + "." + lMethod;
		if (grantedProcs.containsKey(lKey)) {
			// class is ignored until security restrictions are finished.
			try {
				Object lObj = call(rpcServer, lMethod, lArgs);
				lResponseToken.put("result", lObj.toString());
			} catch (NoSuchMethodException ex) {
				lMsg = "NoSuchMethodException calling '" + lMethod + "' for class " + lClassName + ": " + ex.getMessage();
			} catch (IllegalAccessException ex) {
				lMsg = "IllegalAccessException calling '" + lMethod + "' for class " + lClassName + ": " + ex.getMessage();
			} catch (InvocationTargetException ex) {
				lMsg = "InvocationTargetException calling '" + lMethod + "' for class " + lClassName + ": " + ex.getMessage();
			}
		} else {
			lMsg = "Call to " + lKey + " is not granted!";
		}
		if (lMsg != null) {
			lResponseToken.put("code", -1);
			lResponseToken.put("msg", lMsg);
		}

		lServer.sendToken(aConnector, lResponseToken);
	}

	/**
	 * reverse remote procedure call
	 * @param aConnector
	 * @param aToken
	 */
	public void rrpc(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		String lNS = aToken.getNS();

		// get the target
		String lTargetId = aToken.getString("targetId");
		// get the remote classname
		String lClassname = aToken.getString("classname");
		// get the remote method name
		String lMethod = aToken.getString("method");
		// get the remote arguments
		String lArgs = aToken.getString("args");

		// TODO: find solutions for hardcoded engine id
		WebSocketConnector lTargetConnector =
			lServer.getConnector("tcp0", lTargetId);

		if (log.isDebugEnabled()) {
			log.debug("Processing 'rrpc'...");
		}
		if (lTargetConnector != null) {
			Token lRRPC = new Token(lNS, "rrpc");
			lRRPC.put("classname", lClassname);
			lRRPC.put("method", lMethod);
			lRRPC.put("args", lArgs);
			lRRPC.put("sourceId", aConnector.getRemotePort());

			lServer.sendToken(lTargetConnector, lRRPC);
		} else {
			Token lResponse = lServer.createResponse(aToken);
			lResponse.put("code", -1);
			lResponse.put("error", "Target " + lTargetId + " not found.");
			lServer.sendToken(aConnector, lResponse);
		}
	}

	/**
	 *
	 * @param aClassName
	 * @param aURL
	 * @return
	 */
	public static Class loadClass(String aClassName, String aURL) {
		Class lClass = null;
		try {
			URLClassLoader lUCL = new URLClassLoader(new URL[]{new URL(aURL)});
			// load class using previously defined class loader
			lClass = Class.forName(aClassName, true, lUCL);
			if (log.isDebugEnabled()) {
				log.debug("Class '" + lClass.getName() + "' loaded!");
			}
		} catch (ClassNotFoundException ex) {
			log.error("Class not found exception: " + ex.getMessage());
		} catch (MalformedURLException ex) {
			log.error("MalformesURL exception: " + ex.getMessage());
		}
		return lClass;
	}

	/**
	 *
	 * @param aClassName
	 * @return
	 */
	public static Class loadClass(String aClassName) {
		// return loadClass(aClassName, "file:/" + JWebSocketConstants.CLASS_OUT_PATH);
		return null;
	}

	/**
	 *
	 * @param aClass
	 * @param aArgs
	 * @return
	 */
	public static Object createInstance(Class aClass, Object[] aArgs) {
		Object lObj = null;
		try {
			Class[] lCA = new Class[aArgs != null ? aArgs.length : 0];
			for (int i = 0; i < lCA.length; i++) {
				lCA[i] = aArgs[i].getClass();
			}
			Constructor lConstructor = aClass.getConstructor(lCA);
			lObj = lConstructor.newInstance(aArgs);
			if (log.isDebugEnabled()) {
				log.debug("Object '" + aClass.getName() + "' instantiated!");
			}
		} catch (Exception ex) {
			log.error("Exception instantiating class " + aClass.getName() + ": " + ex.getMessage());
		}
		return lObj;
	}

	/**
	 *
	 * @param aClass
	 * @return
	 */
	public static Object createInstance(Class aClass) {
		return createInstance(aClass, null);
	}

	/**
	 *
	 * @param aClassName
	 * @return
	 */
	public static Object createInstance(String aClassName) {
		Class lClass = loadClass(aClassName);
		return createInstance(lClass, null);
	}

	/**
	 *
	 * @param aClassName
	 * @param aArgs
	 * @return
	 */
	public static Object createInstance(String aClassName, Object[] aArgs) {
		Class lClass = loadClass(aClassName);
		return createInstance(lClass, aArgs);
	}

	/**
	 *
	 * @param aInstance
	 * @param aName
	 * @param aArgs
	 * @return
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static Object call(Object aInstance, String aName, Object aArgs)
		throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		Object lObj = null;

		Class lClass = aInstance.getClass();
		Class[] lCA;
		if (aArgs != null) {
			lCA = new Class[]{aArgs.getClass()};
		} else {
			lCA = new Class[0];
		}
		Method lMethod = lClass.getMethod(aName, lCA);
		Object lArg = aArgs;
		lObj = lMethod.invoke(aInstance, lArg);

		return lObj;
	}

	/*
	public static Object call(Object aInstance, String aName, Object... aArgs) {
	Object lObj = null;
	try {
	Class lClass = aInstance.getClass();
	Method lMethod = lClass.getMethod(aName, new Class[]{Object.class});
	Object lArg = aArgs;
	lObj = lMethod.invoke(aInstance, lArg);
	} catch (NoSuchMethodException ex) {
	log.debug("No such method exception calling '" + aName + "' for class " + aInstance.getClass().getName() + ": " + ex.getMessage());
	} catch (Exception ex) {
	log.debug("Exception calling '" + aName + "' for class " + aInstance.getClass().getName() + ": " + ex.getMessage());
	}
	return lObj;
	}
	 */
}
