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
package org.jWebSocket.plugins.rpc;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.jWebSocket.api.WebSocketConnector;
import org.jWebSocket.config.Config;
import org.jWebSocket.plugins.PlugInResponse;
import org.jWebSocket.plugins.TokenPlugIn;
import org.jWebSocket.server.TokenServer;
import org.jWebSocket.token.Token;

/**
 *
 * @author aschulze
 */
public class RPCPlugIn extends TokenPlugIn {

	private static Logger log = Logger.getLogger(RPCPlugIn.class);
	private HashMap<String, Object> grantedProcs = new HashMap<String, Object>();
	private DemoRPCServer rpcServer = null;

	// if namespace changed update client plug-in accordingly!
	private String NS_RPC_DEFAULT = Config.NS_BASE + ".plugins.rpc";

	/**
	 *
	 */
	public RPCPlugIn() {
		// specify default name space
		this.setNamespace(NS_RPC_DEFAULT);
		// specify granted remnote procedure calls
		// todo: Make configurable
		grantedProcs.put("org.jWebSocket.demo.DemoRPCServer.getMD5", null);
	}

	@Override
	public void connectorStarted(WebSocketConnector aConnector) {
		// currently this is the only supported RPCPlugIn server
		rpcServer = new DemoRPCServer();
	}

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
		Token lResponseToken = lServer.createResponse(aToken);

		// currently rpcServer is the only supported RPCPlugIn server!
		String lClassName = aToken.getString("classname");
		String lMethod = aToken.getString("method");
		String lArgs = aToken.getString("args");
		String lMsg = null;

		log.debug("Processing RPC to class '" + lClassName + "', method '" + lMethod + "', args: '" + lArgs + "'...");

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
			lResponseToken.put("error", lMsg);
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
		// get the remote namespace
		String lRNS = aToken.getString("rns");
		// get the remote method name
		String lMethod = aToken.getString("rmethod");
		// get the remote arguments
		String lArgs = aToken.getString("rargs");

		log.debug("Processing 'rrpc'...");

		Token lRRPCToken = new Token(lRNS, "rrpc");
		lRRPCToken.put("rns", lRNS);
		lRRPCToken.put("rmethod", lMethod);
		lRRPCToken.put("rargs", lArgs);

		// TokenServer lServer = (TokenServer) aConnector.getWebSocketServer();

		lServer.sendToken(aConnector, lRRPCToken);
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
			log.debug("Class '" + lClass.getName() + "' loaded!");
		} catch (ClassNotFoundException ex) {
			log.debug("Class not found exception: " + ex.getMessage());
		} catch (MalformedURLException ex) {
			log.debug("MalformesURL exception: " + ex.getMessage());
		}
		return lClass;
	}

	/**
	 *
	 * @param aClassName
	 * @return
	 */
	public static Class loadClass(String aClassName) {
		// return loadClass(aClassName, "file:/" + Config.CLASS_OUT_PATH);
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
			log.debug("Object '" + aClass.getName() + "' instantiated!");
		} catch (Exception ex) {
			log.debug("Exception instantiating class " + aClass.getName() + ": " + ex.getMessage());
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
