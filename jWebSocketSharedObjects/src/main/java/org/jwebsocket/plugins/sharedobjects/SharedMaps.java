//	---------------------------------------------------------------------------
//	jWebSocket - SharedMaps Plug-In
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
package org.jwebsocket.plugins.sharedobjects;

import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.jwebsocket.plugins.TokenPlugIn;

/**
 *
 * @author aschulze
 */
public class SharedMaps extends TokenPlugIn {

	private FastMap maps = new FastMap();

}
