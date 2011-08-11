//	---------------------------------------------------------------------------
//	jWebSocket - Common Configuration Constants
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
 *
 * @author aschulze
 */
public class JWebSocketCommonConstants {

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
	 * Default protocol
	 * @deprecated
	 */
	public static String DEFAULT_PROTOCOL = "json";
	/**
	 * JSON sub protocol
	 * @deprecated
	 */
	public final static String SUB_PROT_JSON = "json";
	/**
	 * CSV sub protocol
	 * @deprecated
	 */
	public final static String SUB_PROT_CSV = "csv";
	/**
	 * XML sub protocol
	 * @deprecated
	 */
	public final static String SUB_PROT_XML = "xml";
	/**
	 * Custom specific binary sub protocol
	 * @deprecated
	 */
	public final static String SUB_PROT_BINARY = "binary";
	/**
	 * Custom specific sub protocol
	 * @deprecated
	 */
	public final static String SUB_PROT_CUSTOM = "custom";
	/**
	 * Default sub protocol if not explicitely specified by client (json).
	 * @deprecated
	 */
	public final static String SUB_PROT_DEFAULT = SUB_PROT_JSON;
	/**
	 * jWebSocket JSON sub protocol
	 */
	public final static String WS_SUBPROT_JSON = "jWebSocket-JSON";
	/**
	 * jWebSocket CSV sub protocol
	 */
	public final static String WS_SUBPROT_CSV = "jWebSocket-CSV";
	/**
	 * jWebSocket XML sub protocol
	 */
	public final static String WS_SUBPROT_XML = "jWebSocket-XML";
	/**
	 * jWebSocket Custom specific sub protocol
	 */
	public final static String WS_SUBPROT_CUSTOM = "jWebSocket-Custom";
	/**
	 * Default protocol
	 */
	public static String WS_SUBPROT_DEFAULT = WS_SUBPROT_JSON;
	/**
	 * JSON sub protocol format
	 */
	public final static String WS_FORMAT_JSON = "json";
	/**
	 * CSV sub protocol format
	 */
	public final static String WS_FORMAT_CSV = "csv";
	/**
	 * XML sub protocol format
	 */
	public final static String WS_FORMAT_XML = "xml";
	/**
	 * Binary sub protocol format
	 */
	public final static String WS_FORMAT_BINARY = "binary";
	/**
	 * Custom specific sub protocol format
	 */
	public final static String WS_FORMAT_CUSTOM = "custom";
	/**
	 * Default sub protocol format
	 */
	public static String WS_FORMAT_DEFAULT = WS_FORMAT_JSON;
	/**
	 * Default sub protocol domain (used when client doesn't set its own subprotocol)
	 */
	public static String WS_SUBPROTOCOL_DEFAULT = "jwebsocket.org";
	/**
	 * WebSocket protocol hybi draft 03 (http://tools.ietf.org/html/draft-ietf-hybi-thewebsocketprotocol-02)
	 */
	public final static String WS_DRAFT_02 = "2";
	/**
	 * WebSocket protocol hybi draft 03 (http://tools.ietf.org/html/draft-ietf-hybi-thewebsocketprotocol-03)
	 */
	public final static String WS_DRAFT_03 = "3";
	/**
	 * WebSocket protocol hybi draft 07 (http://tools.ietf.org/html/draft-ietf-hybi-thewebsocketprotocol-07)
	 */
	public final static String WS_DRAFT_07 = "7";
	/**
	 * WebSocket protocol hybi draft 08 (http://tools.ietf.org/html/draft-ietf-hybi-thewebsocketprotocol-08)
	 */
	public final static String WS_DRAFT_08 = "8";
	/**
	 * WebSocket protocol hybi draft 10 (http://tools.ietf.org/html/draft-ietf-hybi-thewebsocketprotocol-10)
	 */
	public final static String WS_DRAFT_10 = "10";
	/**
	 * WebSocket protocol hixie draft 76 (http://tools.ietf.org/html/draft-hixie-thewebsocketprotocol-76)
	 */
	public final static String WS_DRAFT_DEFAULT = "76";
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
	/**
	 * Minimum allow outgoing TCP Socket port.
	 */
	public static int MIN_IN_PORT = 1024;
	/**
	 * Maximum allow outgoing TCP Socket port.
	 */
	public static int MAX_IN_PORT = 65535;
	/**
	 * the default maximum frame size if not configured
	 */
	public static final int DEFAULT_MAX_FRAME_SIZE = 16384;
	/**
	 * Default socket port for jWebSocket clients.
	 */
	public static int DEFAULT_PORT = 8787;
	/**
	 * Default socket port for jWebSocket clients.
	 */
	public static int DEFAULT_SSLPORT = 9797;
	/**
	 * Default context on app servers and servlet containers
	 */
	public static final String JWEBSOCKET_DEF_CONTEXT = "/jWebSocket";
	/**
	 * Default servlet on app servers and servlet containers
	 */
	public static final String JWEBSOCKET_DEF_SERVLET = "/jWebSocket";
	/**
	 * Default Session Timeout for client connections (120000ms = 2min)
	 */
	public static int DEFAULT_TIMEOUT = 120000;
	/**
	 * private scope, only authenticated user can read and write his personal items
	 */
	public static final String SCOPE_PRIVATE = "private";
	/**
	 * public scope, everybody can read and write items from this scope
	 */
	public static final String SCOPE_PUBLIC = "public";
}
