//	---------------------------------------------------------------------------
//	jWebSocket - RPC PlugIn
//	Copyright (c) 2010 Innotrade GmbH, jWebSocket.org
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
package org.jwebsocket.plugins.rpc;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javolution.util.FastList;

import javolution.util.FastMap;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.factory.JWebSocketJarClassLoader;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.plugins.rpc.util.JSONArrayHandler;
import org.jwebsocket.plugins.rpc.util.RPCRightNotGrantedException;
import org.jwebsocket.plugins.rpc.util.TypeConverter;
import org.jwebsocket.security.SecurityFactory;
import org.jwebsocket.token.JSONToken;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 * This plug-in provides all the functionality for remote procedure calls
 * (RPC) for client-to-server (C2S) apps, and reverse remote procedure calls
 * (RRPC) for server-to-client (S2C) or client-to-client apps (C2C).
 * @author aschulze
 * @author Quentin Ambard
 */
public class RPCPlugIn extends TokenPlugIn {

	private static Logger mLog = Logging.getLogger(RPCPlugIn.class);
	//Store the parameters type allowed for rpc method.
	private Map<String, RPCCallableClassLoader> mRpcCallableClassLoader = new FastMap<String, RPCCallableClassLoader>();
	// if namespace changed update client plug-in accordingly!
	private String NS_RPC_DEFAULT = JWebSocketServerConstants.NS_BASE + ".plugins.rpc";

	// TODO: RRPC calls do not yet allow quotes in arguments
	// TODO: We need simple unique IDs to address a certain target, session id not suitable here.
	// TODO: Show target(able) clients in a drop down box
	// TODO: RPC demo does not show other clients logging in
	/**
	 *
	 */
	public RPCPlugIn() {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating rpc plug-in...");
		}
		// specify default name space
		this.setNamespace(NS_RPC_DEFAULT);

		// currently this is the only supported RPCPlugIn server
		// mRpcServer = new DemoRPCServer();

	}

	@Override
	public void engineStarted(WebSocketEngine aEngine) {

		if (mLog.isDebugEnabled()) {
			mLog.debug("Roles: " + SecurityFactory.getGlobalRoles().getRoleIdSet().toString());
			mLog.debug("Rights: " + SecurityFactory.getGlobalRights().getRightIdSet().toString());
			mLog.debug("Rights (name space filtered): " + SecurityFactory.getGlobalRights("org.jWebSocket.plugins.rpc").getRightIdSet().toString());
		}

		// TODO: move JWebSocketJarClassLoader into ServerAPI module ?
		JWebSocketJarClassLoader lClassLoader = new JWebSocketJarClassLoader();
		Class lClass = null;

		Map<String, String> lSettings = getSettings();
		String lKey;
		String lValue;
		// load map of RPC libraries first
		for (Entry<String, String> lSetting : lSettings.entrySet()) {
			lKey = lSetting.getKey();
			lValue = lSetting.getValue();
			if (lKey.startsWith("class:")) {
				String lClassName = lKey.substring(6);
				try {
					if (mLog.isDebugEnabled()) {
						mLog.debug("Trying to load class '" + lClassName + "' from classpath...");
					}
					lClass = Class.forName(lClassName);
				} catch (Exception ex) {
					mLog.error(ex.getClass().getSimpleName()
							+ " loading class from classpath: "
							+ ex.getMessage()
							+ ", hence trying to load from jar.");
				}
				// if class could not be loaded from classpath...
				if (lClass == null) {
					String lJarFilePath = null;
					try {
						lJarFilePath = JWebSocketConfig.getLibraryFolderPath(lValue);
						if (mLog.isDebugEnabled()) {
							mLog.debug("Trying to load class '" + lClassName + "' from jar '" + lJarFilePath + "'...");
						}
						lClassLoader.addFile(lJarFilePath);
						lClass = lClassLoader.loadClass(lClassName);
						if (mLog.isDebugEnabled()) {
							mLog.debug("Class '" + lClassName + "' successfully loaded from '" + lJarFilePath + "'.");
						}
					} catch (Exception ex) {
						mLog.error(ex.getClass().getSimpleName() + " loading jar '" + lJarFilePath + "': " + ex.getMessage());
					}
				}
				// could the class be loaded?
				if (lClass != null) {
					try {
						RPCCallable lInstance = null;
						try {
							Constructor lConstructor = lClass.getConstructor(WebSocketConnector.class);
							lInstance = (RPCCallable) lConstructor.newInstance(new Object[]{null});
						} catch (Exception ex) {
							lInstance = (RPCCallable) lClass.newInstance();
						}
						mRpcCallableClassLoader.put(lClassName, new RPCCallableClassLoader(lClass, lInstance));
					} catch (Exception ex) {
						mLog.error(ex.getClass().getSimpleName() + " creating '" + lClassName + "' instance : " + ex.getMessage() + ". RPCCallable class must have a default constructor or a constructor whith a single WebSocketConnector parameter.");
					}
				}
			}
		}
		// Load map of granted procs
		for (Entry<String, String> lSetting : lSettings.entrySet()) {
			String lFullMethodName = lSetting.getKey();
			lValue = lSetting.getValue();
			if (lFullMethodName.startsWith("roles:")) {
				lFullMethodName = lFullMethodName.substring(6);
				//setting with parameters type to handle java method overload
				String lParameterType = null;
				if (lFullMethodName.indexOf("(") != -1 && lFullMethodName.indexOf(")") != -1) {
					lParameterType = lFullMethodName.substring(lFullMethodName.indexOf("(") + 1, lFullMethodName.length() - 1);
					lFullMethodName = lFullMethodName.substring(0, lFullMethodName.indexOf("("));
				}
				String lClassName = lFullMethodName.substring(0, lFullMethodName.lastIndexOf("."));
				String lMethodName = lFullMethodName.substring(lFullMethodName.lastIndexOf(".") + 1);

				Method lMethod = getValidMethod(lClassName, lMethodName, lParameterType);
				if (lMethod != null) {
					// We create the right to access to this method.
					// new Right("rpc:" + lFullMethodName + ":" + lParameterType, "give the right to execute the rpc method " + lFullMethodName + "(" + lParameterType + ")");
					// TODO: implement Rights here
					mRpcCallableClassLoader.get(lClassName).addMethod(lMethodName, lMethod);
				}
			}
		}
		if (mLog.isDebugEnabled()) {
			mLog.debug("RPC methods avaible: " + mRpcCallableClassLoader.toString());
		}
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
	 * remote procedure call (RPC)
	 * @param aConnector 
	 * @param aToken
	 */
	public void rpc(WebSocketConnector aConnector, Token aToken) {
		// check if user is allowed to run 'rpc' command
		if (!SecurityFactory.hasRight(getUsername(aConnector), NS_RPC_DEFAULT + ".rpc")) {
			sendToken(aConnector, aConnector, createAccessDenied(aToken));
			return;
		}

		Token lResponseToken = createResponse(aToken);

		String lClassName = aToken.getString("classname");
		String lMethod = aToken.getString("method");
		List lArgs = aToken.getList("args");

		String lMsg = null;

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing RPC to class '" + lClassName + "', method '" + lMethod + "', args: '" + lArgs + "'...");
		}

		// class is ignored until security restrictions are finished.
		try {
			//The class we try to call is not loaded
			if (mRpcCallableClassLoader.containsKey(lClassName)) {
				if (mRpcCallableClassLoader.get(lClassName).hasMethod(lMethod)) {
					RPCCallableClassLoader lRpcClassLoader = mRpcCallableClassLoader.get(lClassName);
					//We get the instance of the generator
					RPCCallable lInstanceGenerator = lRpcClassLoader.getRpcCallableInstanceGenerator();
					//from this generator, we get an instance of the class we want to call. This part is in the charge of the developper throw the RPCCallable interface.
					RPCCallable lInstance = lInstanceGenerator.getInstance(aConnector);
					if (lInstance != null) {
						Object lObj = call(lRpcClassLoader, lInstance, lMethod, lArgs);
						lResponseToken.setValidated("result", lObj);
					} else {
						lMsg = "Class '"
								+ lClassName
								+ "' found but get a null instance when calling the RPCCallable getInstance() method.";
					}
				} else {
					lMsg = "Class '"
							+ lClassName
							+ "' found but the method "
							+ lMethod
							+ " is not avaible.";
				}
			} else {
				lMsg = "Class '"
						+ lClassName + "' not found or not properly loaded.";
			}
		} catch (NoSuchMethodException ex) {
			lMsg = "NoSuchMethodException calling '"
					+ lMethod + "' for class "
					+ lClassName + ": "
					+ ex.getMessage();
		} catch (IllegalAccessException ex) {
			lMsg = "IllegalAccessException calling '"
					+ lMethod + "' for class "
					+ lClassName + ": "
					+ ex.getMessage();
		} catch (InvocationTargetException ex) {
			lMsg = "InvocationTargetException calling '"
					+ lMethod + "' for class "
					+ lClassName + ": "
					+ ex.getMessage();
		} catch (RPCRightNotGrantedException ex) {
			lMsg = "RPCRightNotGrantedException calling '"
					+ lMethod + "' for class "
					+ lClassName + ": "
					+ ex.getMessage();
		} catch (ClassNotFoundException ex) {
			lMsg = "RPCRightNotGrantedException calling '"
					+ lMethod + "' for class "
					+ lClassName + ": "
					+ ex.getMessage();
		}
		if (lMsg != null) {
			lResponseToken.setInteger("code", -1);
			lResponseToken.setString("msg", lMsg);
		}

		/* just for testing purposes of multi-threaded rpc's
		try {
		Thread.sleep(3000);
		} catch (InterruptedException ex) {
		}
		 */

		sendToken(aConnector, aConnector, lResponseToken);
	}

	/**
	 * reverse remote procedure call (RRPC)
	 * @param aConnector
	 * @param aToken
	 */
	public void rrpc(WebSocketConnector aConnector, Token aToken) {
		// check if user is allowed to run 'rrpc' command
		if (!SecurityFactory.hasRight(getUsername(aConnector), NS_RPC_DEFAULT + ".rrpc")) {
			sendToken(aConnector, aConnector, createAccessDenied(aToken));
			return;
		}

		String lNS = aToken.getNS();

		// get the target
		String lTargetId = aToken.getString("targetId");
		// get the remote classname
		String lClassname = aToken.getString("classname");
		// get the remote method name
		String lMethod = aToken.getString("method");
		// get the remote arguments
		//arguments could be a token or an array of token
		//for safety reason, client except an array (even if it's an array with 1 token)
		List lArgs = aToken.getList("args");
		if (!(lArgs instanceof JSONArray)) {
			lArgs = new FastList();
			lArgs.add(lArgs);
		}

		// TODO: find solutions for hardcoded engine id
		WebSocketConnector lTargetConnector = getServer().getConnector("tcp0", lTargetId);

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'rrpc'...");
		}
		if (lTargetConnector != null) {
			Token lRRPC = TokenFactory.createToken(lNS, "rrpc");
			lRRPC.setString("classname", lClassname);
			lRRPC.setString("method", lMethod);
			lRRPC.setList("args", lArgs);
			lRRPC.setString("sourceId", aConnector.getId());

			sendToken(aConnector, lTargetConnector, lRRPC);
		} else {
			Token lResponse = createResponse(aToken);
			lResponse.setInteger("code", -1);
			lResponse.setString("error", "Target " + lTargetId + " not found.");
			sendToken(aConnector, aConnector, lResponse);
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
			if (mLog.isDebugEnabled()) {
				mLog.debug("Class '" + lClass.getName() + "' loaded!");
			}
		} catch (ClassNotFoundException ex) {
			mLog.error("Class not found exception: " + ex.getMessage());
		} catch (MalformedURLException ex) {
			mLog.error("MalformesURL exception: " + ex.getMessage());
		}
		return lClass;
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
			if (mLog.isDebugEnabled()) {
				mLog.debug("Object '" + aClass.getName() + "' instantiated!");
			}
		} catch (Exception ex) {
			mLog.error("Exception instantiating class " + aClass.getName() + ": " + ex.getMessage());
		}
		return lObj;
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
	 * @throws RPCRightNotGrantedException 
	 * @throws ClassNotFoundException 
	 */
	public Object call(RPCCallableClassLoader aRpcClassLoader, Object aInstance, String aMethodName, Object aArgs)
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, RPCRightNotGrantedException, ClassNotFoundException {
		Object lObj = null;

		Class lClass = aRpcClassLoader.getRpcCallableClass();
		Class[] lCA = null;
		Object[] lArg = null;
		JSONArray lJsonArrayArgs = null;
		Method lMethodToInvoke = null;
		if (aArgs != null) {
			//convert aArgs as a JSONArray
			if (!(aArgs instanceof JSONArray)) {
				lJsonArrayArgs = new JSONArray().put(aArgs);
			} else {
				lJsonArrayArgs = (JSONArray) aArgs;
			}
			//a JSON Array is passed as parameter => we look for the method which match with same parameters
			//initialization
			lArg = new Object[lJsonArrayArgs.length()];
			lCA = new Class[lJsonArrayArgs.length()];
			boolean lMethodFound = false;

			//We look if one of the method we have loaded match
			List<Method> lListMethod = aRpcClassLoader.getMethods(aMethodName);
			for (Method lMethod : lListMethod) {
				Class[] lParametersType = lMethod.getParameterTypes();
				//We are looking for a method with the same number of parameters
				if (lParametersType.length == lJsonArrayArgs.length()) {
					//We make sure the types of the method match
					try {
						for (int j = 0; j < lParametersType.length; j++) {
							Class lParameterType = lParametersType[j];
							//Try to guess the object type.
							//Only support primitive type+wrapper, String, List and Token.
							//String and Token
							if (lParameterType == String.class) {
								lArg[j] = lJsonArrayArgs.getString(j);
							} else if (lParameterType == Token.class) {
								// Alex:
								// TODO: here we need to re-think
								// All other objects should be Token.
								lArg[j] = new JSONToken(lJsonArrayArgs.getJSONObject(j));
							} //Special support for primitive type
							else if (lParameterType == int.class) {
								lArg[j] = lJsonArrayArgs.getInt(j);
							} else if (lParameterType == boolean.class) {
								lArg[j] = lJsonArrayArgs.getBoolean(j);
							} else if (lParameterType == double.class) {
								lArg[j] = lJsonArrayArgs.getDouble(j);
							} //Wrappers of primitive types
							else if (lParameterType == Integer.class) {
								lArg[j] = lJsonArrayArgs.getInt(j);
							} else if (lParameterType == Boolean.class) {
								lArg[j] = lJsonArrayArgs.getBoolean(j);
							} else if (lParameterType == Double.class) {
								lArg[j] = lJsonArrayArgs.getDouble(j);
							} else if (lParameterType == List.class) {
								//try to guess which type of object should be on the List:
								Type[] genericParameterTypes = lMethod.getGenericParameterTypes();
								// Alex:
								// TODO: do we still need the JSONArrayHandler?
								lArg[j] = JSONArrayHandler.JSONArrayToList(lJsonArrayArgs.getJSONArray(j), genericParameterTypes[j]);
							} //any other object are *not* supported !
							else {
								if (mLog.isDebugEnabled()) {
									mLog.debug("Can't extract an object with type '" + lParameterType.getName() + "' in a JSONArray Object");
								}
								throw new JSONException("Can't extract an object whith type '" + lParameterType.getName() + "' in a JSONArray Object");
							}
						}
						//The method is found, all parameters match
						//check if the user has the role to call this method
						//TODO: remplace this verification by something like: aConnector.hasRight("rpc:methodName:methodArguments")
						if (true) {
							lMethodToInvoke = lMethod;
							break;
						} else {
							throw new RPCRightNotGrantedException(lMethod.getName(), aArgs.toString());
						}
					} catch (JSONException e) {
						//That's the wrong method. Do nothing, just pass to the next one. Any idea to do it in a cleaner way ?
					}
				}
			}
			if (lMethodToInvoke == null) {
				throw new NoSuchMethodException();
			}
		} else {
			lCA = new Class[0];
			lMethodToInvoke = lClass.getMethod(aMethodName, lCA);
		}
		aInstance = lClass.cast(aInstance);
		lObj = lMethodToInvoke.invoke(aInstance, lArg);

		return lObj;
	}

	/**
	 * Check if a RPCCallable method has correct parameters type
	 * Store each class methods inside mClassMethods to grant a faster access for each rpc call.
	 * Log an error if on parameter is not valid
	 * Only called during the plugin initialization
	 * @param aMethodName
	 * @return true if the parameters are OK
	 */
	private Method getValidMethod(String aClassName, String aMethodName, String parameterType) {
		//check if the method own to a class which has been loaded.
		if (!mRpcCallableClassLoader.containsKey(aClassName)) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("You try to grant access to a method which own to a class the server can't initialize. Make sure you didn't forget to declare it's jar file. " + aMethodName + " will *not* be loaded");
			}
			return null;
		}

		Class lClass = mRpcCallableClassLoader.get(aClassName).getRpcCallableClass();
		Method[] lMethods = lClass.getMethods();
		//Check if 2 methods have the same name and the same number of arguments (this block just log an error if 2 method have the same number of parameters without specific types)
		ArrayList<Integer> lMethodWithSameNameAndNumberOfArguments = new ArrayList<Integer>();
		if (parameterType == null) {
			for (int i = 0; i < lMethods.length; i++) {
				Method lMethod = lMethods[i];
				if (lMethod.getName().equals(aMethodName)) {
					if (lMethodWithSameNameAndNumberOfArguments.contains(lMethod.getParameterTypes().length)) {
						//2 methods have the same name and number of parameters
						if (mLog.isDebugEnabled()) {
							mLog.debug("Two methods named " + aMethodName + " have the same number of argument. Can't know which one this setting concerns. Please use xml settings such as: MyClass.myMethod(int, boolean, string, double, map, array)");
						}
						return null;
					} else {
						lMethodWithSameNameAndNumberOfArguments.add(lMethod.getParameterTypes().length);
					}
				}
			}
		}
		//Check if on of the method match
		for (int i = 0; i < lMethods.length; i++) {
			//If we are on a method with the same name, we check it's parameters
			if (lMethods[i].getName().equals(aMethodName)) {
				if (checkMethodParameters(lMethods[i], parameterType, aClassName)) {
					return lMethods[i];
				}
			}
		}
		mLog.error("The method "
				+ aMethodName
				+ " could not be loaded. "
				+ "Probably a typo or invalid parameter (check previous error).");
		return null;
	}

	/**
	 * Check if the method aMethod match with aParametersType.
	 * @param aMethod
	 * @param aParametersType
	 * @param aClassName
	 * @return true if the method match, false otherwise
	 */
	private boolean checkMethodParameters(Method aMethod, String aParametersType, String aClassName) {
		Class[] lParametersType = aMethod.getParameterTypes();
		//Look for a method with the same arguments if they are defined in the xml setting...
		if (aParametersType != null) {
			aParametersType = aParametersType.replace(" ", "");
			String[] protocolParameters = aParametersType.split(",");
			boolean methodMatch = true;
			for (int j = 0; j < protocolParameters.length; j++) {
				if (!TypeConverter.matchProtocolTypeToJavaType(protocolParameters[j], lParametersType[j].getName())) {
					methodMatch = false;
					break;
				}
			}
			if (methodMatch) {
				if (mLog.isDebugEnabled()) {
					mLog.debug("Complex method " + aMethod.getName() + " loaded (expect " + lParametersType.length + " parameters: " + aParametersType + ").");
				}
				return true;
			}
			return false;
		}
		//without parameters, always true
		if (lParametersType.length == 0) {
			mLog.debug("method " + aMethod.getName() + "() loaded.");
			return true;
		}

		// Alex: comment
		// TODO: getName().equals(Token.class) are incompatible! see below! use .class.get[Simple]Name ?

		//if not defined in the setting, we only save method with many parameters or a single parameter which is not a Token.
		if (lParametersType.length > 0 && !(lParametersType.length == 1 && lParametersType[0].getName().equals(Token.class))) {
			for (int j = 0; j < lParametersType.length; j++) {
				Class lParameterType = lParametersType[j];
				String lParameterTypeName = lParameterType.getName();
				if (!TypeConverter.isValidProtocolType(lParameterTypeName)) {
					mLog.error("The method " + aMethod.getName() + " has an invalid parameter: " + lParameterTypeName + ". "
							+ "This method will *not* be load."
							+ "Suported parameters type are primitive, primitive's wrapper and Token.");
					return false;
				}
			}
			if (mLog.isDebugEnabled()) {
				mLog.debug("Complex method " + aMethod.getName() + " loaded (expect " + lParametersType.length + " parameters).");
			}
			//store the "complex" method in the Map.
			return true;
		}
		//last option: 1 parameter and it's a Token
		mLog.debug("Simple method " + aMethod.getName() + " loaded (expect a single Token as parameter).");
		return true;
	}

	/**
	 * Alert all the RpcCallableInstance that a connecter has stopped
	 */
	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
		// We alert every instance of the generator that a connector stopped its connection
		for (Entry<String, RPCCallableClassLoader> entry : mRpcCallableClassLoader.entrySet()) {
			entry.getValue().getRpcCallableInstanceGenerator().connectorStopped(aConnector, aCloseReason);
		}
	}
}
