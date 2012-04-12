//  ---------------------------------------------------------------------------
//  jWebSocket - EventsPlugIn
//  Copyright (c) 2010 Innotrade GmbH, jWebSocket.org
//  ---------------------------------------------------------------------------
//  This program is free software; you can redistribute it and/or modify it
//  under the terms of the GNU Lesser General Public License as published by the
//  Free Software Foundation; either version 3 of the License, or (at your
//  option) any later version.
//  This program is distributed in the hope that it will be useful, but WITHOUT
//  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//  more details.
//  You should have received a copy of the GNU Lesser General Public License along
//  with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//  ---------------------------------------------------------------------------
package org.jwebsocket.eventmodel.event.em;

import org.jwebsocket.eventmodel.event.C2SEvent;

/**
 * Fired before process events from the client
 *
 * @author kyberneees
 */
public class BeforeProcessEvent extends C2SEvent {

	private C2SEvent mEvent;

	/**
	 * @return The event from the client to process
	 */
	public C2SEvent getEvent() {
		return mEvent;
	}

	/**
	 * @param aEvent The event from the client to process
	 */
	public void setEvent(C2SEvent aEvent) {
		this.mEvent = aEvent;
	}
}
