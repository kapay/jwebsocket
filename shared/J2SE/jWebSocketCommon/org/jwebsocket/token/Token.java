//	---------------------------------------------------------------------------
//	jWebSocket - Token Interface
//	Copyright (c) 2010 Alexander Schulze, Innotrade GmbH
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
package org.jwebsocket.token;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author aschulze
 */
public interface Token {


	/**
	 *
	 * @param aKey
	 * @return
	 */
	Object getObject(String aKey);

	/**
	 *
	 * @param aKey
	 * @param aDefault
	 * @return
	 */
	String getString(String aKey, String aDefault);

	/**
	 *
	 * @param aKey
	 * @return
	 */
	String getString(String aKey);

	/**
	 *
	 * @param aKey
	 * @param aValue
	 */
	void setString(String aKey, String aValue);

	/**
	 *
	 * @param aKey
	 * @param aDefault
	 * @return
	 */
	Integer getInteger(String aKey, Integer aDefault);

	/**
	 *
	 * @param aKey
	 * @return
	 */
	Integer getInteger(String aKey);

	/**
	 *
	 * @param aKey
	 * @param aValue
	 */
	void setInteger(String aKey, Integer aValue);

	/**
	 *
	 * @param aKey
	 * @param aDefault
	 * @return
	 */
	Double getDouble(String aKey, Double aDefault);

	/**
	 *
	 * @param aKey
	 * @return
	 */
	Double getDouble(String aKey);

	/**
	 *
	 * @param aKey
	 * @param aValue
	 */
	void setDouble(String aKey, Double aValue);

	/**
	 *
	 * @param aKey
	 * @param aValue
	 */
	void setDouble(String aKey, Float aValue);

	/**
	 *
	 * @param aKey
	 * @param aDefault
	 * @return
	 */
	Boolean getBoolean(String aKey, Boolean aDefault);

	/**
	 *
	 * @param aKey
	 * @return
	 */
	Boolean getBoolean(String aKey);

	/**
	 *
	 * @param aKey
	 * @param aValue
	 */
	void setBoolean(String aKey, Boolean aValue);

	/**
	 *
	 * @param aKey
	 * @param aDefault
	 * @return
	 */
	List getList(String aKey, List aDefault);

	/**
	 *
	 * @param aKey
	 * @return
	 */
	List getList(String aKey);

	/**
	 *
	 * @param aKey
	 * @param aList
	 */
	void setList(String aKey, List aList);

	// TODO: Add list access methods

	/**
	 *
	 * @param aKey
	 * @param aDefault
	 * @return
	 */
	Map getMap(String aKey, Map aDefault);

	/**
	 *
	 * @param aKey
	 * @return
	 */
	Map getMap(String aKey);

	/**
	 *
	 * @param aKey
	 * @param aList
	 */
	void setMap(String aKey, Map aMap);

	// TODO: Add map access methods

	// TODO: Add date/time fields


	/**
	 *
	 * @param aKey
	 * @return
	 */
	void remove(String aKey);


	/**
	 *
	 * @return
	 */
	String getType();

	/**
	 *
	 * @param aType
	 */
	void setType(String aType);

	/**
	 *
	 * @return
	 */
	String getNS();

	/**
	 *
	 * @param aNS
	 */
	void setNS(String aNS);

	/**
	 * returns the token as a map to be used as "sub token"
	 * @param aNS
	 */
	Map asMap();

	/**
	 * validates the passed objects and uses the appropriate assignment method
	 * @param aObject
	 * @return true if value could be set otherwise false
	 */
	boolean setValidated(String aKey, Object aObj);

	Iterator<String> getKeyIterator();

}
