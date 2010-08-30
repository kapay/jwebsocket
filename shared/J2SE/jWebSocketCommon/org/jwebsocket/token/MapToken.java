//	---------------------------------------------------------------------------
//	jWebSocket - Token Implementation
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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javolution.util.FastList;
import javolution.util.FastMap;

/**
 * A token is ...
 * @author aschulze
 */
public class MapToken extends BaseToken implements Token {

	private FastMap mData = null;

	/**
	 * Creates a new empty instance of a token.
	 * The token does not contain any items.
	 */
	public MapToken() {
		mData = new FastMap();
	}

	/**
	 *
	 * @param aType
	 */
	public MapToken(String aType) {
		mData = new FastMap();
		setType(aType);
	}

	/**
	 *
	 * @param aMap
	 */
	public MapToken(FastMap aMap) {
		mData = aMap;
	}

	/**
	 *
	 * @param aNS
	 * @param aType
	 */
	public MapToken(String aNS, String aType) {
		mData = new FastMap();
		setNS(aNS);
		setType(aType);
	}

	/**
	 *
	 * @param aMap
	 */
	public void setMap(FastMap aMap) {
		mData = aMap;
	}

	/**
	 *
	 *
	 * @return
	 */
	public FastMap getMap() {
		return mData;
	}

	private Object getValue(Object aValue) {
		if (aValue instanceof MapToken) {
			aValue = ((MapToken) aValue).getMap();
		} else if (aValue instanceof Collection) {
			List lList = new FastList();
			for (Object lItem : (Collection) aValue) {
				lList.add(getValue(lItem));
			}
			aValue = lList;
		} else if (aValue instanceof Map) {
			Map lMap = new FastMap();
			for (Entry<Object, Object> lItem : ((Map<Object, Object>) aValue).entrySet()) {
				lMap.put(lItem.getKey().toString(), getValue(lItem.getValue()));
			}
			aValue = lMap;
		} else if (aValue instanceof Object[]) {
			List lList = new FastList();
			Object[] lOA = (Object[]) aValue;
			for (int i = 0; i < lOA.length; i++) {
				lList.add(getValue(lOA[i]));
			}
			aValue = lList;
		}
		return aValue;
	}

	/**
	 * puts a new key/value pair into the token, in other words it adds a
	 * new item to the token.
	 * @param aKey key of the the token item.
	 * @param aValue value of the token item.
	 */
	private void put(String aKey, Object aValue) {
		mData.put(aKey, getValue(aValue));
	}

	/**
	 *
	 * @param aKey
	 * @return
	 */
	private Object get(String aKey) {
		return mData.get(aKey);
	}

	/**
	 *
	 * @param aKey
	 * @return
	 */
	@Override
	public void remove(String aKey) {
		mData.remove(aKey);
	}

	/**
	 *
	 * @param aKey
	 * @param aDefault
	 * @return
	 */
	@Override
	public String getString(String aKey, String aDefault) {
		String lResult;
		try {
			lResult = (String) mData.get(aKey);
			if (lResult == null) {
				lResult = aDefault;
			}
		} catch (Exception ex) {
			lResult = aDefault;
		}
		return lResult;
	}

	/**
	 *
	 * @param aKey
	 */
	@Override
	public void setString(String aKey, String aValue) {
		try {
			mData.put(aKey, aValue);
		} catch (Exception ex) {
			// TODO: handle exception
		}
	}

	/**
	 *
	 * @param aKey
	 * @return
	 */
	@Override
	public String getString(String aKey) {
		return getString(aKey, null);
	}

	/**
	 *
	 * @param aKey
	 * @param aDefault
	 * @return
	 */
	@Override
	public Integer getInteger(String aKey, Integer aDefault) {
		Integer lResult;
		try {
			lResult = (Integer) mData.get(aKey);
			if (lResult == null) {
				lResult = aDefault;
			}
		} catch (Exception ex) {
			lResult = aDefault;
		}
		return lResult;
	}

	/**
	 *
	 * @param aKey
	 * @return
	 */
	@Override
	public Integer getInteger(String aKey) {
		return getInteger(aKey, null);
	}

	@Override
	public void setInteger(String aKey, Integer aValue) {
		try {
			mData.put(aKey, aValue);
		} catch (Exception ex) {
			// TODO: handle exception
		}
	}

	/**
	 *
	 * @param aKey
	 * @param aDefault
	 * @return
	 */
	@Override
	public Double getDouble(String aKey, Double aDefault) {
		Double lResult;
		try {
			lResult = (Double) mData.get(aKey);
			if (lResult == null) {
				lResult = aDefault;
			}
		} catch (Exception ex) {
			lResult = aDefault;
		}
		return lResult;
	}

	/**
	 *
	 * @param aKey
	 * @return
	 */
	@Override
	public Double getDouble(String aKey) {
		return getDouble(aKey, null);
	}

	@Override
	public void setDouble(String aKey, Double aValue) {
		try {
			mData.put(aKey, aValue);
		} catch (Exception ex) {
			// TODO: handle exception
		}
	}

	/**
	 *
	 * @param aKey
	 * @param aDefault
	 * @return
	 */
	@Override
	public Boolean getBoolean(String aKey, Boolean aDefault) {
		Boolean lResult;
		try {
			lResult = (Boolean) mData.get(aKey);
			if (lResult == null) {
				lResult = aDefault;
			}
		} catch (Exception ex) {
			lResult = aDefault;
		}
		return lResult;
	}

	/**
	 *
	 * @param aArg
	 * @return
	 */
	@Override
	public Boolean getBoolean(String aArg) {
		return getBoolean(aArg, null);
	}

	/**
	 *
	 * @param aKey
	 */
	@Override
	public void setBoolean(String aKey, Boolean aValue) {
		try {
			mData.put(aKey, aValue);
		} catch (Exception ex) {
			// TODO: handle exception
		}
	}

	/**
	 *
	 * @param aKey
	 * @param aDefault
	 * @return
	 */
	@Override
	public List getList(String aKey, Boolean aDefault) {
		// TODO: Implement this
		return null;
	}

	/**
	 *
	 * @param aKey
	 * @return
	 */
	@Override
	public List getList(String aKey) {
		// TODO: Implement this
		return null;
	}

	/**
	 *
	 * @param aKey
	 * @param aList
	 */
	@Override
	public void setList(String aKey, List aList) {
		try {
			mData.put(aKey, aList);
		} catch (Exception ex) {
			// TODO: handle exception
		}
	}

	/**
	 *
	 * @param aKey
	 * @param aDefault
	 * @return
	 */
	@Override
	public Map getMap(String aKey, Boolean aDefault) {
		// TODO: Implement this
		return null;
	}

	/**
	 *
	 * @param aKey
	 * @return
	 */
	@Override
	public Map getMap(String aKey) {
		// TODO: Implement this
		return null;
	}

	/**
	 *
	 * @param aKey
	 * @param aList
	 */
	@Override
	public void setMap(String aKey, Map aMap) {
		try {
			mData.put(aKey, aMap);
		} catch (Exception ex) {
			// TODO: handle exception
		}
	}

	/**
	 *
	 * @return
	 */
	@Override
	public String toString() {
		return mData.toString();
	}

	/**
	 *
	 * @return
	 */
	@Override
	public Map asMap() {
		return mData;
	}

	/**
	 *
	 * @return
	 */
	public Iterator<String> getKeyIterator() {
		return mData.keySet().iterator();
	}

	/**
	 *
	 * @param aKey
	 * @return
	 */
	public Object getObject(String aKey) {
		Object lObj = null;
		try {
			lObj = mData.get(aKey);
		} catch (Exception ex) {
			//
		}
		return lObj;
	}
}