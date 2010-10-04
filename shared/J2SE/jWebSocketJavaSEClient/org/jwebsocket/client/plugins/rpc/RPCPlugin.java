package org.jwebsocket.client.plugins.rpc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.jwebsocket.plugins.rpc.CommonRpcPlugin;
import org.jwebsocket.plugins.rpc.MethodMatcher;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

public class RPCPlugin {
	private static boolean annotationAllowed = true ;
	public static void setAnnotationAllowed (boolean aAnnotationAllowedValue){
		annotationAllowed = aAnnotationAllowedValue;
	}
	private static Map<String, Map<String, List<Method>>> mListOfMethod = new FastMap<String, Map<String, List<Method>>>();
	
	/**
	 * Add a Method to the list of rrpc granted method.
	 * @param aMethod
	 */
	public synchronized static void addRrpcMethod (Method aMethod) {
		String lClassName = aMethod.getDeclaringClass().getName();
		if (!mListOfMethod.containsKey(lClassName)) {
			mListOfMethod.put(lClassName, new FastMap<String, List<Method>>());
		}
		if (!mListOfMethod.get(lClassName).containsKey(aMethod.getName())) {
			mListOfMethod.get(lClassName).put(aMethod.getName(), new FastList<Method>());
		}
		mListOfMethod.get(lClassName).get(aMethod.getName()).add(aMethod);
	}
	/**
	 * Process a rrpc call.
	 * TODO: doesn't send back any answer. Do smthg with lMsg & lResponseToken.
	 * @param aClassName
	 * @param aMethodName
	 * @param aArgs
	 */
	public static Token processRrpc(String aClassName, String aMethodName, List aArgs) {
		Token lResponseToken = null; 
		String lMsg = "";
		if (aClassName != null && aMethodName != null) {
			if (mListOfMethod.containsKey(aClassName)) {
				if (mListOfMethod.get(aClassName).containsKey(aMethodName))
				return call(aClassName, aMethodName, aArgs);
			} 
			
			if (annotationAllowed){
				//We try to load the method and check if it has the annotation
				try {
					Class lClass = Class.forName(aClassName);
					Method[] lMethods = lClass.getMethods();
					for (Method lMethod : lMethods) {
						if (lMethod.getName().equals(aMethodName) && lMethod.isAnnotationPresent(RPCCallable.class)) {
							addRrpcMethod(lMethod);
							MethodMatcher lMethodMatcher = new MethodMatcher(lMethod);
							if (lMethodMatcher.isMethodMatchingAgainstParameter(aArgs)) {
								//Add the method to the list to grant a faster access.
								lResponseToken = call(aClassName, aMethodName, aArgs);
							}
						}
					}
					if (lResponseToken != null) {
						return lResponseToken;
					}
				} catch (ClassNotFoundException ex) {
		      lMsg = "ClassNotFoundException calling '" + aMethodName + "' for class " + aClassName + ": " + ex.getMessage();
				}
			}
			else {
				lMsg = "Class not found in the list (annotation are not allowed) calling '" + aMethodName + "' for class " + aClassName + ": " ;
			}
		}
		if (lMsg.equals("")) {
			lMsg = "ClassName or Method name is probably null calling '" + aMethodName + "' for class " + aClassName + ": " ;
		}
		lResponseToken = TokenFactory.createToken(CommonRpcPlugin.NS_RPC_DEFAULT, CommonRpcPlugin.RPC_TYPE);		
		lResponseToken.setString("msg", lMsg);
		return lResponseToken;
	}
	
	private static Token call (String aClassName, String aMethodName, List aArgs) {
		Token lResponseToken = TokenFactory.createToken(CommonRpcPlugin.NS_RPC_DEFAULT, CommonRpcPlugin.RPC_TYPE);
		String lMsg = "";
		List<Method> lListOfMethod = mListOfMethod.get(aClassName).get(aMethodName);
		for (Method lMethod : lListOfMethod) {
			MethodMatcher lMethodMatcher = new MethodMatcher(lMethod);
	    //If lArg is not null, means the method match
	    if (lMethodMatcher.isMethodMatchingAgainstParameter(aArgs)) {
	      //We cast the intance to the correct class.
	      try {
					Object lObj = lMethod.invoke(null, lMethodMatcher.getMethodParameters());
					lResponseToken.setValidated("result", lObj);
					return lResponseToken;
				} catch (IllegalArgumentException ex) {
		      lMsg = "IllegalAccessException calling '" + aMethodName + "' for class " + aClassName + ": " + ex.getMessage();
				} catch (IllegalAccessException ex) {
		      lMsg = "IllegalAccessException calling '" + aMethodName + "' for class " + aClassName + ": " + ex.getMessage();
				} catch (InvocationTargetException ex) {
		      lMsg = "InvocationTargetException calling '" + aMethodName + "' for class " + aClassName + ": " + ex.getMessage();
				} catch (Exception ex) {
		      lMsg = "InvocationTargetException calling '" + aMethodName + "' for class " + aClassName + ": " + ex.getMessage();
				}
	    }
	  }
    lResponseToken.setInteger("code", -1);
    lResponseToken.setString("msg", lMsg);
    return lResponseToken;
	}
}
