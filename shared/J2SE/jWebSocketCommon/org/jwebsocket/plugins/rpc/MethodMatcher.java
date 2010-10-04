package org.jwebsocket.plugins.rpc;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

import org.jwebsocket.plugins.rpc.util.ListConverter;
import org.jwebsocket.plugins.rpc.util.MethodMatcherConversionException;
import org.jwebsocket.token.Token;

@SuppressWarnings("rawtypes")
public class MethodMatcher {
	
	private Object[] mMethodParameters ;
	private Method mMethod;
	
	public MethodMatcher (Method aMethod) {
		mMethod = aMethod;
	}
	public Object[] getMethodParameters() {
		return mMethodParameters;
	}
  /**
   * 
   * @param mMethod
   * @param aArgs
   */
  public boolean isMethodMatchingAgainstParameter (List aArgs) {
    Class[] lParametersType = mMethod.getParameterTypes();
    if (aArgs == null) {
    	if (lParametersType.length == 0) {
    		mMethodParameters = null ;
    		return true ;
    	}
    	return false ;
    }
    mMethodParameters = new Object[aArgs.size()];
    // We are looking for a method with the same number of parameters
    if (lParametersType.length == aArgs.size()) {
      // We make sure the types of the method match
      try {
      	for (int j = 0; j < lParametersType.length; j++) {
			    Class lParameterType = lParametersType[j];
			    // Try to guess the object type.
			    // Only support primitive type+wrapper, String, List and Token.
			    // String and Token
			    if (lParameterType == String.class) {
			      mMethodParameters[j] = (String) aArgs.get(j);
			    } else if (lParameterType == Token.class) {
			      mMethodParameters[j] = (Token) aArgs.get(j);
			    } // Special support for primitive type
			    else if (lParameterType == int.class) {
			      mMethodParameters[j] = (Integer) aArgs.get(j);
			    } else if (lParameterType == boolean.class) {
			      mMethodParameters[j] = (Boolean) aArgs.get(j);
			    } else if (lParameterType == double.class) {
			      mMethodParameters[j] = (Double) aArgs.get(j);
			    } // Wrappers of primitive types
			    else if (lParameterType == Integer.class) {
			      mMethodParameters[j] = (Integer) aArgs.get(j);
			    } else if (lParameterType == Boolean.class) {
			      mMethodParameters[j] = (Boolean) aArgs.get(j);
			    } else if (lParameterType == Double.class) {
			      mMethodParameters[j] = (Double) aArgs.get(j);
			    } else if (lParameterType == List.class) {
			      // try to guess which type of object should be on the List:
			      Type genericParameterType = mMethod.getGenericParameterTypes()[j];
			      mMethodParameters[j] = ListConverter.convert((List) aArgs.get(j), genericParameterType);
			    } // any other object are *not* supported !
			    else {
//			      if (mLog.isDebugEnabled()) {
//			        mLog.debug("Can't extract an object with type '" + lParameterType.getName() + "' in a Token Object");
//			      }
			      throw new MethodMatcherConversionException("Can't extract an object whith type '" + lParameterType.getName() + "' in a Token Object");
			    }
		    }
      	//If no exception has been thrown, so the method mat
      	//We return the list of lArgs ready for the request.
      	return true;
      } catch (Exception e) {
      	//TODO: shouldn't catch a global Exception e here. Need to change it in the ListConverter class
        // That's the wrong method.
      	return false;
      }
    }
    return false;
  }
}
