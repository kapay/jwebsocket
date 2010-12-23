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
package org.jwebsocket.eventmodel.observable;

import java.io.Serializable;
import org.jwebsocket.token.MapToken;
import org.jwebsocket.token.Token;

/**
 *
 * @author Itachi
 */
public class Event implements Serializable {

	private String id;
	private Token args = new MapToken();
	private Object subject;
	private boolean processed = false;

	@Override
	public String toString() {
		return getId();
	}

	@Override
	public int hashCode() {
		return id.hashCode() + args.hashCode() + subject.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Event other = (Event) obj;
		if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
			return false;
		}
		if (this.args != other.args && (this.args == null || !this.args.equals(other.args))) {
			return false;
		}
		if (this.subject != other.subject && (this.subject == null || !this.subject.equals(other.subject))) {
			return false;
		}
		return true;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the args
	 */
	public Token getArgs() {
		return args;
	}

	/**
	 * @param args the args to set
	 */
	public void setArgs(Token args) {
		this.args = args;
	}

	/**
	 * @return the subject
	 */
	public Object getSubject() {
		return subject;
	}

	/**
	 * @param subject the subject to set
	 */
	public void setSubject(Object subject) {
		this.subject = subject;
	}

	/**
	 * @return the processed
	 */
	public boolean isProcessed() {
		return processed;
	}

	/**
	 * @param processed the processed to set
	 */
	public void setProcessed(boolean processed) {
		this.processed = processed;
	}
}
