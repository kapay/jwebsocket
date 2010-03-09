//	---------------------------------------------------------------------------
//	jWebSocket - Broadcasting options
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
package org.jwebsocket.kit;

/**
 *
 * @author aschulze
 */
public class BroadcastOptions {

	private boolean senderIncluded = false;
	private boolean responseRequested = false;

	/**
	 *
	 * @param aSenderIncluded
	 * @param aResponseRequested
	 */
	public BroadcastOptions(boolean aSenderIncluded, boolean aResponseRequested) {
		senderIncluded = aSenderIncluded;
		responseRequested = aResponseRequested;
	}

	/**
	 * @return senderIncluded
	 */
	public boolean isSenderIncluded() {
		return senderIncluded;
	}

	/**
	 * @param aSenderIncluded
	 */
	public void setSenderIncluded(boolean aSenderIncluded) {
		this.senderIncluded = aSenderIncluded;
	}

	/**
	 * @return the responseRequested
	 */
	public boolean isResponseRequested() {
		return responseRequested;
	}

	/**
	 * @param responseRequested the responseRequested to set
	 */
	public void setResponseRequested(boolean responseRequested) {
		this.responseRequested = responseRequested;
	}

}
