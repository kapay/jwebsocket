//	---------------------------------------------------------------------------
//	jWebSocket - RPC demo Server
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
package org.jwebsocket.plugins.rpc;

import org.jwebsocket.util.Tools;

/**
 *
 * @author aschulze
 */
public class DemoRPCServer {

	/**
	 * simply returns the MD5 sum of the given string.
	 * @param aArg
	 * @return MD5 sum of the given string.
	 */
	public Object getMD5(String aArg) {
		return (Tools.getMD5(aArg));
	}

	/**
	 * usually protected (i.e. cannot be called from client
	 * until explicitely granted).
	 * @param aArg
	 * @return MD5 sum of the given string.
	 */
	public Object getProtected(String aArg) {
		return "Protected method has now been granted for RPC";
	}
}
