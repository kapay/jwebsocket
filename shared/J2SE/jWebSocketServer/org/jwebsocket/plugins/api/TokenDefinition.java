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
import javolution.util.FastSet;
import org.jwebsocket.token.ITokenizable;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 * The token definition class
 *
 * @author kyberneees
 */
public class TokenDefinition implements ITokenizable {

	private String type;
	
	/**
	 * wr (with response), nr (no-response), none (no testeable from the client)
	 */
	private String requestType = "wr";  
	private Integer responseCode = 0;
	private Set<TokenArgument> inArguments = new FastSet<TokenArgument>();
	private Set<TokenArgument> outArguments = new FastSet<TokenArgument>();
	private String comment;

	/**
	 * @return The token type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type The token type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return The response code
	 */
	public Integer getResponseCode() {
		return responseCode;
	}

	/**
	 * @param responseCode The response code to set
	 */
	public void setResponseCode(Integer responseCode) {
		this.responseCode = responseCode;
	}

	/**
	 * @return The input arguments 
	 */
	public Set<TokenArgument> getInArguments() {
		return Collections.unmodifiableSet(inArguments);
	}

	/**
	 * @param inArguments The input arguments to set
	 */
	public void setInArguments(Set<TokenArgument> inArguments) {
		this.inArguments = inArguments;
	}

	/**
	 * @return The output arguments
	 */
	public Set<TokenArgument> getOutArguments() {
		return Collections.unmodifiableSet(outArguments);
	}

	/**
	 * @param outArguments The output arguments to set
	 */
	public void setOutArguments(Set<TokenArgument> outArguments) {
		this.outArguments = outArguments;
	}

	/**
	 * @return The comment 
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment The comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * {@inheritDoc }
	 */
	public void writeToToken(Token token) {
		token.setString("type", getType());
		token.setString("comment", getComment());
		token.setInteger("responseCode", getResponseCode());
		token.setString("requestType", getRequestType());

		List<Token> arguments = new FastList<Token>();
		Token tempArg;
		for (TokenArgument t : getInArguments()) {
			tempArg = TokenFactory.createToken();
			t.writeToToken(tempArg);
			arguments.add(tempArg);
		}
		token.setList("inArguments", arguments);

		arguments = new FastList<Token>();
		for (TokenArgument t : getOutArguments()) {
			tempArg = TokenFactory.createToken();
			t.writeToToken(tempArg);
			arguments.add(tempArg);
		}
		token.setList("outArguments", arguments);
	}

	/**
	 * {@inheritDoc }
	 */
	public void readFromToken(Token token) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 * @return The request type
	 */
	public String getRequestType() {
		return requestType;
	}

	/**
	 * @param requestType The request type to set
	 */
	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}
}
