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
package org.jwebsocket.eventmodel.filter.validator;

import javolution.util.FastMap;
import org.springframework.validation.Validator;

/**
 *
 ** @author kyberneees
 */
public class Argument {

	private String name;
	private Class<? extends Object> type;
	private boolean optional;
	private Object value;
	private Validator validator;

	public Argument(String name, Class<? extends Object> type, boolean optional, Validator validator) {
		setName(name);
		setType(type);
		setOptional(optional);
		setValidator(validator);
	}

	public Argument(String name, Class<? extends Object> type, boolean optional) {
		setName(name);
		setType(type);
		setOptional(optional);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the type
	 */
	public Class<? extends Object> getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(Class<? extends Object> type) {
		this.type = type;
	}

	/**
	 * @return the optional
	 */
	public boolean isOptional() {
		return optional;
	}

	/**
	 * @param optional the optional to set
	 */
	public void setOptional(boolean optional) {
		this.optional = optional;
	}

	@Override
	public String toString() {
		FastMap<String, Object> m = new FastMap<String, Object>();
		m.put("name", getName());
		m.put("type", getType().toString());
		m.put("optional", isOptional());

		return m.toString();
	}

	/**
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(Object value) {
		this.value = value;
	}

	/**
	 * @return the validator
	 */
	public Validator getValidator() {
		return validator;
	}

	/**
	 * @param validator the validator to set
	 */
	public void setValidator(Validator validator) {
		this.validator = validator;
	}
}
