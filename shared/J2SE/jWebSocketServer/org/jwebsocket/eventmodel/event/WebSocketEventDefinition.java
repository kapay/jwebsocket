/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
	private boolean cacheEnabled = false;
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
	 */ public String getNs() {
		return ns;
	}

	/**
	 * @param ns the ns to set
	 */ public void setNs(String ns) {
		this.ns = ns;
	}
}
