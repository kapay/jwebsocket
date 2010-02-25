//	---------------------------------------------------------------------------
//	jWebSocket - Result from a PlugIn in the PlugInChain
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
package org.jWebSocket.plugins;

/**
 *
 * @author aschulze
 */
public class PlugInResponse {

	private boolean chainAborted = false;
	private boolean tokenProcessed = false;

	/**
	 * @return the chainAborted
	 */
	public Boolean isChainAborted() {
		return chainAborted;
	}

	/**
	 * @param chainAborted the chainAborted to set
	 */
	public void abortChain() {
		this.chainAborted = true;
		this.tokenProcessed = false;
	}

	/**
	 * @param chainAborted the chainAborted to set
	 */
	public void breakChain() {
		this.chainAborted = true;
		this.tokenProcessed = true;
	}

	/**
	 * @param chainAborted the chainAborted to set
	 */
	public void continueChain() {
		this.chainAborted = false;
	}

}
