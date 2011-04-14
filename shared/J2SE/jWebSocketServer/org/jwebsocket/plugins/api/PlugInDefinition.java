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

import java.util.Collections;
import java.util.List;
import java.util.Set;
import javolution.util.FastList;
import org.jwebsocket.token.ITokenizable;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 * The plug-in definition class
 *
 * @author kyberneees
 */
public class PlugInDefinition implements ITokenizable {

	private String id;
	private Set<TokenDefinition> supportedTokens;
	private String comment;

	/**
	 * @return The plug-in identifier
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id The plug-in identifier to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return The supported tokens definition
	 */
	public Set<TokenDefinition> getSupportedTokens() {
		return Collections.unmodifiableSet(supportedTokens);
	}

	/**
	 * @param supportedTokens The supported tokens definitions to set
	 */
	public void setSupportedTokens(Set<TokenDefinition> supportedTokens) {
		this.supportedTokens = supportedTokens;
	}

	/**
	 * @return The plug-in comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * 
	 * @param tokenType The token type
	 * @return <tt>TRUE</tt> if the token is supported, <tt>FALSE</tt> otherwise
	 */
	public boolean supportToken(String tokenType) {
		for (TokenDefinition t : getSupportedTokens()) {
			if (t.getType().equals(tokenType)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * @param comment The plug-in comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * {@inheritDoc }
	 */
	public void writeToToken(Token token) {
		token.setString("id", getId());
		token.setString("comment", getComment());

		List<Token> tokens = new FastList<Token>();
		Token tempToken;
		for (TokenDefinition t : getSupportedTokens()) {
			tempToken = TokenFactory.createToken();
			t.writeToToken(tempToken);
			tokens.add(tempToken);
		}

		token.setList("supportedTokens", tokens);
	}

	public void readFromToken(Token token) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
