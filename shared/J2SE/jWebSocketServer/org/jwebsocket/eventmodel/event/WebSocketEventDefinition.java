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
import org.jwebsocket.eventmodel.api.IWebSocketSecureObject;
import org.jwebsocket.eventmodel.observable.Event;

/**
 *
 * @author kyberneees
 */
public class WebSocketEventDefinition implements IInitializable, IWebSocketSecureObject {

	private String id;
	private String ns;
	private Set<Argument> incomingArgsValidation = new FastSet<Argument>();
	private Set<Argument> outgoingArgsValidation = new FastSet<Argument>();
	private boolean responseRequired = false;
	private boolean responseToOwnerConnector = false;
	private boolean cacheEnabled = false;
	private boolean notificationConcurrent = false;
	private int cacheTime = 0;
	private boolean securityEnabled = false;
	private Set<String> roles = new FastSet<String>();
	private Set<String> users = new FastSet<String>();
	private Set<String> ipAddresses = new FastSet<String>();

	public void initialize() {
	}

	public void shutdown() {
	}

	@Override
	public int hashCode() {
		return ((null != id) ? id.hashCode() : 0)
				+ incomingArgsValidation.hashCode()
				+ outgoingArgsValidation.hashCode()
				+ ((responseRequired) ? 1 : 0)
				+ ((responseToOwnerConnector) ? 1 : 0)
				+ ((cacheEnabled) ? 1 : 0)
				+ ((notificationConcurrent) ? 1 : 0)
				+ cacheTime
				+ ((securityEnabled) ? 1 : 0)
				+ roles.hashCode()
				+ users.hashCode()
				+ ((null != ns) ? ns.hashCode() : 0)
				+ ((null != ipAddresses) ? ipAddresses.hashCode() : 0);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final WebSocketEventDefinition other = (WebSocketEventDefinition) obj;
		if ((this.id == null) ? (other.getId() != null) : !this.id.equals(other.getId())) {
			return false;
		}
		if (this.incomingArgsValidation != other.getIncomingArgsValidation() && (this.incomingArgsValidation == null || !this.incomingArgsValidation.equals(other.getIncomingArgsValidation()))) {
			return false;
		}
		if (this.outgoingArgsValidation != other.getOutgoingArgsValidation() && (this.outgoingArgsValidation == null || !this.outgoingArgsValidation.equals(other.getOutgoingArgsValidation()))) {
			return false;
		}
		if (this.responseRequired != other.isResponseRequired()) {
			return false;
		}
		if (this.responseToOwnerConnector != other.isResponseToOwnerConnector()) {
			return false;
		}
		if (this.cacheEnabled != other.isCacheEnabled()) {
			return false;
		}
		if (this.notificationConcurrent != other.isNotificationConcurrent()) {
			return false;
		}
		if (this.cacheTime != other.getCacheTime()) {
			return false;
		}
		if (this.securityEnabled != other.isSecurityEnabled()) {
			return false;
		}
		if (this.roles != other.getRoles() && (this.roles == null || !this.roles.equals(other.getRoles()))) {
			return false;
		}
		if (this.users != other.getRoles() && (this.users == null || !this.users.equals(other.getUsers()))) {
			return false;
		}
		if ((this.ns == null) ? (other.getNs() != null) : !this.ns.equals(other.getNs())) {
			return false;
		}
		if ((this.ipAddresses == null) ? (other.getIpAddresses() != null) : !this.ipAddresses.equals(other.getIpAddresses())) {
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
	 * @return the incomingArgsValidation
	 */
	public Set<Argument> getIncomingArgsValidation() {
		return incomingArgsValidation;
	}

	/**
	 * @param incomingArgsValidation the incomingArgsValidation to set
	 */
	public void setIncomingArgsValidation(Set<Argument> incomingArgsValidation) {
		this.incomingArgsValidation.addAll(incomingArgsValidation);
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
		this.outgoingArgsValidation.addAll(outgoingArgsValidation);
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
	@SuppressWarnings("unchecked")
	public Class<? extends Event> getEventClass() throws Exception {
		return (Class<? extends Event>) Class.forName(getNs());
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

	/**
	 * @return the ipAddresses
	 */
	public Set<String> getIpAddresses() {
		return ipAddresses;
	}

	/**
	 * @param ipAddresses the ipAddresses to set
	 */
	public void setIpAddresses(Set<String> ipAddresses) {
		this.ipAddresses.addAll(ipAddresses);
	}

	/**
	 * @return the users
	 */
	public Set<String> getUsers() {
		return users;
	}

	/**
	 * @param users the users to set
	 */
	public void setUsers(Set<String> users) {
		this.users.addAll(users);
	}
}
