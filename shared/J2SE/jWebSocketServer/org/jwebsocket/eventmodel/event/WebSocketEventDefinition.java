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
package org.jwebsocket.eventmodel.event;

import org.jwebsocket.eventmodel.filter.validator.Argument;
import javolution.util.FastSet;
import java.util.Set;
import org.jwebsocket.api.IInitializable;

/**
 *
 * @author kyberneees
 */
public class WebSocketEventDefinition implements IInitializable {

	private String id;
	private Set<Argument> incomingArgsValidation = new FastSet<Argument>();
	private Set<Argument> outgoingArgsValidation = new FastSet<Argument>();
	private boolean responseRequired = false;
	private boolean responseToOwnerConnector = false;
	private boolean cacheEnabled = false;
	private boolean notificationConcurrent = true;
	private int cacheTime = 0;
	private boolean securityEnabled = false;
	private Set<String> roles = new FastSet<String>();
	private String ns;

	public void initialize() {
	}

	public void shutdown() {
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
	 * @return the incomingArgsValidation
	 */
	public Set<Argument> getIncomingArgsValidation() {
		return incomingArgsValidation;
	}

	/**
	 * @param incomingArgsValidation the incomingArgsValidation to set
	 */
	public void setIncomingArgsValidation(Set<Argument> incomingArgsValidation) {
		this.incomingArgsValidation = incomingArgsValidation;
	}

	/**
	 * @return the outgoingArgsValidation
	 */
	public Set<Argument> getOutgoingArgsValidation() {
		return outgoingArgsValidation;
	}

	/**
	 * @param outgoingArgsValidation the outgoingArgsValidation to set
	 */
	public void setOutgoingArgsValidation(Set<Argument> outgoingArgsValidation) {
		this.outgoingArgsValidation = outgoingArgsValidation;
	}

	/**
	 * @return the responseRequired
	 */
	public boolean isResponseRequired() {
		return responseRequired;
	}

	/**
	 * @param responseRequired the responseRequired to set
	 */
	public void setResponseRequired(boolean responseRequired) {
		this.responseRequired = responseRequired;
	}

	/**
	 * @return the cacheEnabled
	 */
	public boolean isCacheEnabled() {
		return cacheEnabled;
	}

	/**
	 * @param cacheEnabled the cacheEnabled to set
	 */
	public void setCacheEnabled(boolean cacheEnabled) {
		this.cacheEnabled = cacheEnabled;
	}

	/**
	 * @return the cacheTime
	 */
	public int getCacheTime() {
		return cacheTime;
	}

	/**
	 * @param cacheTime the cacheTime to set
	 */
	public void setCacheTime(int cacheTime) {
		this.cacheTime = cacheTime;
	}

	/**
	 * @return the securityEnabled
	 */
	public boolean isSecurityEnabled() {
		return securityEnabled;
	}

	/**
	 * @param securityEnabled the securityEnabled to set
	 */
	public void setSecurityEnabled(boolean securityEnabled) {
		this.securityEnabled = securityEnabled;
	}

	/**
	 * @return the roles
	 */
	public Set<String> getRoles() {
		return roles;
	}

	/**
	 * @param roles the roles to set
	 */
	public void setRoles(Set<String> roles) {
		this.roles = roles;
	}

	/**
	 * @return the eventClass
	 */
	public Class getEventClass() throws Exception {
		return Class.forName(getNs());
	}

	/**
	 * @return the ns
	 */
	public String getNs() {
		return ns;
	}

	/**
	 * @param ns the ns to set
	 */
	public void setNs(String ns) {
		this.ns = ns;
	}

	/**
	 * @return the responseToOwnerConnector
	 */
	public boolean isResponseToOwnerConnector() {
		return responseToOwnerConnector;
	}

	/**
	 * @param responseToOwnerConnector the responseToOwnerConnector to set
	 */
	public void setResponseToOwnerConnector(boolean responseToOwnerConnector) {
		this.responseToOwnerConnector = responseToOwnerConnector;
	}

	/**
	 * @return the notificationConcurrent
	 */
	public boolean isNotificationConcurrent() {
		return notificationConcurrent;
	}

	/**
	 * @param notificationConcurrent the notificationConcurrent to set
	 */
	public void setNotificationConcurrent(boolean notificationConcurrent) {
		this.notificationConcurrent = notificationConcurrent;
	}
}
