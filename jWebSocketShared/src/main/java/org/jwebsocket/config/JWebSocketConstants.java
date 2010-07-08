//	---------------------------------------------------------------------------
//	jWebSocket - Common Configuration Settings
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
package org.jwebsocket.config;

/**
 * Provides a global shared container for the jWebSocket configuration settings.
 *
 * @author aschulze
 * @version $Id: JWebSocketConstants.java 624 2010-07-06 12:28:44Z fivefeetfurther $
 */
public final class JWebSocketConstants {

	/**
	 * jWebSocket copyright string - MAY NOT BE CHANGED due to GNU LGPL v3.0 license!
	 * Please ask for conditions of a commercial license on demand.
	 */
	public static final String COPYRIGHT = "(c) 2010 Innotrade GmbH (jWebSocket.org), Germany (NRW), Herzogenrath";
	/**
	 * jWebSocket license string - MAY NOT BE CHANGED due to GNU LGPL v3.0 license!
	 * Please ask for conditions of a commercial license on demand.
	 */
	public static final String LICENSE = "Distributed under GNU LGPL License Version 3.0 (http://www.gnu.org/licenses/lgpl.html)";
	/**
	 * jWebSocket vendor string - MAY NOT BE CHANGED due to GNU LGPL v3.0 license!
	 * Please ask for conditions of a commercial license on demand.
	 */
	public static final String VENDOR = "jWebSocket.org";
	/**
	 * Current version string of the jWebSocket package.
	 */
	public static final String VERSION_STR = "0.9.5.0706 beta";
	/**
	 * Namespace base for tokens and plug-ins.
	 */
	public static final String NS_BASE = "org.jWebSocket";
	/**
	 * constant for default installation
	 */
	public static final String DEFAULT_INSTALLATION = "prod";
	/**
	 * Constant for JWEBSOCKET_HOME
	 */
	public static final String JWEBSOCKET_HOME = "JWEBSOCKET_HOME";
	/**
	 * Constant for CATALINA_HOME
	 */
	public static final String CATALINA_HOME = "CATALINA_HOME";
	/**
	 * Constant for jWebSocket.xml configuration file
	 */
	public static final String JWEBSOCKET_XML = "jWebSocket.xml";
	/**
	 * Minimum allow outgoing TCP Socket port.
	 */
	public static int MIN_IN_PORT = 1024;
	/**
	 * Maximum allow outgoing TCP Socket port.
	 */
	public static int MAX_IN_PORT = 65535;
	/**
	 * Default engine for jWebSocket server.
	 */
	public static String DEFAULT_ENGINE = "tcp";
	/**
	 * Default protocol
	 */
	public static String DEFAULT_PROTOCOL = "json";
	/**
	 * the max frame size
	 */
	public static int DEFAULT_MAX_FRAME_SIZE = 16384;
	/**
	 * Default socket port for jWebSocket clients.
	 */
	public static int DEFAULT_PORT = 8787;
	/**
	 * Default Session Timeout for client connections (120000ms = 2min)
	 */
	public static int DEFAULT_TIMEOUT = 120000;
	/**
	 * Minimum session Timeout for client connections in milliseconds
	 * (1000ms = 1sec).
	 */
	// min and max ranged removed since 0.9.0.0602, see config documentation
	// public static int MIN_TIMEOUT = 0;
	/**
	 * Maximum session Timeout for client connections in milliseconds
	 * (3600000ms = 1h).
	 */
	// min and max ranged removed since 0.9.0.0602, see config documentation
	// public static int MAX_TIMEOUT = 3600000;
	/**
	 * JSON sub protocol
	 */
	public final static String SUB_PROT_JSON = "json";
	/**
	 * CSV sub protocol
	 */
	public final static String SUB_PROT_CSV = "csv";
	/**
	 * XML sub protocol
	 */
	public final static String SUB_PROT_XML = "xml";
	/**
	 * Custom specfic sub protocol
	 */
	public final static String SUB_PROT_CUSTOM = "custom";
	/**
	 * Default sub protocol if not explicitely specified by client (json).
	 */
	public final static String SUB_PROT_DEFAULT = SUB_PROT_JSON;
	/**
	 * Separator between the path and the argument list in the URL.
	 */
	public static String PATHARG_SEPARATOR = ";";
	/**
	 * Separator between the various URL arguments.
	 */
	public static String ARGARG_SEPARATOR = ",";
	/**
	 * Separator between the key and the value of each URL argument.
	 */
	public static String KEYVAL_SEPARATOR = "=";
}
