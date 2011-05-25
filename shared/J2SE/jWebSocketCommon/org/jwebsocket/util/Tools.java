//	---------------------------------------------------------------------------
//	jWebSocket - WebSocket Tools
//	Copyright (c) 2010 Alexander Schulze, Innotrade GmbH
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
package org.jwebsocket.util;

import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javolution.util.FastMap;

/**
 * Provides some convenience methods to support the web socket
 * development.
 * @author aschulze
 */
public class Tools {

	private static final Map<String,String> JAVA_2_GENERIC_MAP = new FastMap<String,String>();
	Map GENERIC_2_JAVA_MAP = new FastMap<String,String>();
	
	static {
		JAVA_2_GENERIC_MAP.put("java.lang.Boolean", "boolean");
		JAVA_2_GENERIC_MAP.put("java.lang.Byte", "integer");
		JAVA_2_GENERIC_MAP.put("java.lang.Short", "integer");
		JAVA_2_GENERIC_MAP.put("java.lang.Integer", "integer");
		JAVA_2_GENERIC_MAP.put("java.lang.Long", "long");
		JAVA_2_GENERIC_MAP.put("java.lang.Float", "float");
		JAVA_2_GENERIC_MAP.put("java.lang.Double", "double");
		JAVA_2_GENERIC_MAP.put("java.math.BigDecimal", "double");
	
		JAVA_2_GENERIC_MAP.put("java.sql.Timestamp", "datetime");
		JAVA_2_GENERIC_MAP.put("java.sql.Date", "date");
		JAVA_2_GENERIC_MAP.put("java.sql.Time", "time");

		JAVA_2_GENERIC_MAP.put("java.util.Collection", "list");
		JAVA_2_GENERIC_MAP.put("java.util.Collection", "list");
		JAVA_2_GENERIC_MAP.put("java.util.Set", "list");
		JAVA_2_GENERIC_MAP.put("java.util.Map", "map");
	}
	
	/**
	 * 
	 */
	public final static boolean EXPAND_CASE_SENSITIVE = false;
	/**
	 * 
	 */
	public final static boolean EXPAND_CASE_INSENSITIVE = true;

	/**
	 * Returns the MD5 sum of the given string. The output always has 32 digits.
	 * @param aMsg String the string to calculate the MD5 sum for.
	 * @return MD5 sum of the given string.
	 */
	public static String getMD5(String aMsg) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
			byte[] lBufSource = aMsg.getBytes("UTF-8");
			byte[] lBufTarget = md.digest(lBufSource);
			Formatter formatter = new Formatter();
			for (byte b : lBufTarget) {
				formatter.format("%02x", b);
			}
			return (formatter.toString());
		} catch (Exception ex) {
			// log.error("getMD5: " + ex.getMessage());
			System.out.println("getMD5: " + ex.getMessage());
		}
		return null;
	}

	/**
	 * Returns the hex value of the given int as a string. If {@code aLen} is
	 * greater than zero the output is cut or filled to the given length
	 * otherwise the exact number of digits is returned.
	 * @param aInt Integer to be converted into a hex-string.
	 * @param aLen Number of hex digits (optionally filled or cut if needed)
	 * @return Hex-string of the given integer.
	 */
	public static String intToHex(int aInt, int aLen) {
		String lRes = Integer.toHexString(aInt);
		if (aLen > 0 && lRes.length() > aLen) {
			lRes = lRes.substring(0, aLen);
		} else {
			while (lRes.length() < aLen) {
				lRes = "0" + lRes.substring(0, aLen);
			}
		}
		return lRes;
	}

	/**
	 * Returns the hex value of the given int as a string. If {@code aLen} is
	 * greater than zero the output is cut or filled to the given length
	 * otherwise the exact number of digits is returned.
	 * @param aInt Integer to be converted into a string.
	 * @param aLen Number of digits (optionally filled or cut if needed)
	 * @return String of the given integer.
	 */
	public static String intToString(int aInt, int aLen) {
		String lRes = Integer.toString(aInt);
		if (aLen > 0 && lRes.length() > aLen) {
			lRes = lRes.substring(0, aLen);
		} else {
			while (lRes.length() < aLen) {
				lRes = "0" + lRes;
			}
		}
		return lRes;
	}

	/**
	 * Converts a string into an integer value and automatically sets it to
	 * a given default value if the string could not be parsed.
	 * @param aString string to be converted into an integer.
	 * @param aDefault default value assigned to the result in case of an exception.
	 * @return integer value of string or given default value in case of exception.
	 */
	public static int stringToInt(String aString, int aDefault) {
		int lRes;
		try {
			lRes = Integer.parseInt(aString);
		} catch (Exception lEx) {
			lRes = aDefault;
		}
		return lRes;
	}

	/**
	 * Converts a string into a long value and automatically sets it to
	 * a given default value if the string could not be parsed.
	 * @param aString string to be converted into a long.
	 * @param aDefault default value assigned to the result in case of an exception.
	 * @return long value of string or given default value in case of exception.
	 */
	public static long stringToLong(String aString, long aDefault) {
		long lRes;
		try {
			lRes = Long.parseLong(aString);
		} catch (Exception lEx) {
			lRes = aDefault;
		}
		return lRes;
	}

	/**
	 * 
	 * @param aISO8601Date
	 * @return
	 */
	public static Date ISO8601ToDate(String aISO8601Date) {
		SimpleDateFormat lSDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		try {
			return lSDF.parse(aISO8601Date);
		} catch (Exception lEx) {
			return null;
		}
		/* this one cannot be used under Android, thus replacing by above code!
		Calendar lCal = javax.xml.bind.DatatypeConverter.parseDateTime(aISO8601Date);
		return lCal.getTime();
		 */
	}

	/**
	 *
	 * @param aDate
	 * @return
	 */
	public static String DateToISO8601(Date aDate) {
		// we are using UTC times only here, ignoring the timezone of the server location
		// so don't add a Z to the format string here! 'Z' means character Z = UTC
		SimpleDateFormat lSDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		return lSDF.format(aDate);
	}

	/**
	 *
	 * @param aDate
	 * @return
	 */
	public static String DateToISO8601WithMillis(Date aDate) {
		// we are using UTC times only here, ignoring the timezone of the server location
		// so don't add a Z to the format string here! 'Z' means character Z = UTC
		SimpleDateFormat lSDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		return lSDF.format(aDate);
	}

	/**
	 * Tries to convert a given object into the given java data type
	 * @param aValue
	 * @param aFromType
	 * @return
	 */
	public static Object castGenericToJava(Object aValue, String aFromType, String aToType) {
		if (aFromType != null
				&& aToType != null
				&& aValue != null) {
			aFromType = aFromType.toLowerCase();
			aToType = aToType.toLowerCase();

			// convert from datetime (java.sql.Date)
			if ("datetime".equals(aFromType)) {
				if (aValue instanceof String) {
					Date lDate = ISO8601ToDate((String) aValue);
					if ("timestamp".equals(aToType)) {
						return new Timestamp(lDate.getTime());
					} else {
						return lDate;
					}
				}
			}
			
		}
		return null;
	}

	/**
	 * Tries to convert a given object into the given java data type
	 * @param aValue
	 * @param aFromType
	 * @return
	 */
	public static String getGenericTypeStringFromJavaClassname(String aClassname) {
		return JAVA_2_GENERIC_MAP.get(aClassname);
	}
	
	
	/**
	 * 
	 * @param aString
	 * @param aVars
	 * @param aIgnoreCase
	 * @return
	 */
	public static String expandVars(String aString, Map<String, String> aVars,
			boolean aIgnoreCase) {
		String lPattern = "\\$\\{([A-Za-z0-9_]+)\\}";
		Pattern lRegExpr = Pattern.compile(lPattern);
		Matcher lMatcher = lRegExpr.matcher(aString);
		while (lMatcher.find()) {
			String lFoundVal = lMatcher.group(1);
			if (aIgnoreCase) {
				lFoundVal = lFoundVal.toUpperCase();
			}
			String lEnvVal = aVars.get(lFoundVal);
			if (lEnvVal == null) {
				lEnvVal = "";
			} else {
				lEnvVal = lEnvVal.replace("\\", "\\\\");
			}
			Pattern lSubExpr = Pattern.compile(Pattern.quote(lMatcher.group(0)));
			aString = lSubExpr.matcher(aString).replaceAll(lEnvVal);
		}
		return aString;
	}

	/**
	 * 
	 * @param aString
	 * @return
	 */
	public static String expandEnvVars(String aString) {
		Map<String, String> lEnvVars = System.getenv();
		return expandVars(aString, lEnvVars, EXPAND_CASE_INSENSITIVE);
	}

	/**
	 * 
	 * @param aClassName
	 * @param aMethodName
	 * @param aArgs
	 * @return
	 * @throws Exception
	 */
	public static Object invoke(String aClassName, String aMethodName,
			Object[] aArgs) throws Exception {
		Class lClass = Class.forName(aClassName);
		if (lClass == null) {
			throw new Exception("Class '" + aClassName + "' not found.");
		}
		Object lRes = null;

		Class[] lArgClasses = null;
		if (aArgs != null) {
			lArgClasses = new Class[aArgs.length];
		}
		Method lMthd = lClass.getDeclaredMethod(aMethodName, lArgClasses);
		if (lMthd == null) {
			throw new Exception("Method '" + aMethodName + "' not found.");
		}
		lRes = lMthd.invoke(aArgs);

		return lRes;
	}

	/**
	 * 
	 * @param aInstance
	 * @param aMethodName
	 * @param aArgs
	 * @return
	 * @throws Exception
	 */
	public static Object invoke(Object aInstance, String aMethodName,
			Object[] aArgs) throws Exception {
		if (aInstance == null) {
			throw new Exception("No instance passed for call.");
		}
		Class lClass = aInstance.getClass();
		Object lRes = null;

		Class[] lArgClasses;
		if (aArgs != null) {
			lArgClasses = new Class[aArgs.length];
		} else {
			lArgClasses = new Class[0];
		}
		Method lMthd = lClass.getDeclaredMethod(aMethodName, lArgClasses);
		if (lMthd == null) {
			throw new Exception("Method '" + aMethodName + "' not found.");
		}
		if (aArgs == null) {
			aArgs = new Object[0];
		}
		lRes = lMthd.invoke(aInstance, aArgs);

		return lRes;
	}
}
