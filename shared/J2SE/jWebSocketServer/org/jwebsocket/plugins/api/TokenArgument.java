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
package org.jwebsocket.plugins.api;

import org.jwebsocket.token.ITokenizable;
import org.jwebsocket.token.Token;

/**
 * The token argument definition class
 *
 * @author kyberneees
 */
public class TokenArgument implements ITokenizable {

	private String name;
	private String type;
	private boolean optional = false;
	private String testValue;
	private String comment;

	/**
	 * @return The argument name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param The argument name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return The argument type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type The argument type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return <tt>TRUE</tt> if the argument is optional <tt>FALSE</tt> otherwise
	 */
	public boolean isOptional() {
		return optional;
	}

	/**
	 * @param optional Indicates if the argument is optional
	 */
	public void setOptional(boolean optional) {
		this.optional = optional;
	}

	/**
	 * @return The argument comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment The argument comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * @return The test value for functional tests (JSON format)
	 */
	public String getTestValue() {
		return testValue;
	}

	/**
	 * @param testValue The test value for functional tests (JSON format)
	 */
	public void setTestValue(String testValue) {
		this.testValue = testValue;
	}

	/**
	 * {@inheritDoc }
	 */
	public void writeToToken(Token token) {
		token.setString("name", getName());
		token.setString("comment", getComment());
		token.setString("type", getType());
		token.setBoolean("optionl", isOptional());
		
		if (getTestValue() != null) {
			token.setString("testValue", getTestValue());
		}
	}

	/**
	 * {@inheritDoc }
	 */
	public void readFromToken(Token token) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
