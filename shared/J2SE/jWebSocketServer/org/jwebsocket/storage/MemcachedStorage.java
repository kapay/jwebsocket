//  ---------------------------------------------------------------------------
//  jWebSocket - MemcachedStorage
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
package org.jwebsocket.storage;

import java.security.InvalidParameterException;
import java.util.Map.Entry;
import org.jwebsocket.api.IBasicStorage;
import java.util.Collection;
import java.util.Set;
import java.util.Map;
import javolution.util.FastMap;
import net.spy.memcached.MemcachedClient;
import javolution.util.FastSet;
import java.util.Arrays;

/**
 *
 ** @author kyberneees
 */
public class MemcachedStorage<K extends Object, V extends Object>
		implements IBasicStorage<K, V> {

	private MemcachedClient mMemcachedClient;
	private String mName;
	private final static String KEYS_LOCATION = ".KEYS::1234567890";
	private final static String KEY_SEPARATOR = "::-::";
	private final static int NOT_EXPIRE = 0;

	public MemcachedStorage(String aName, MemcachedClient aMemcachedClient) {
		this.mName = aName;
		this.mMemcachedClient = aMemcachedClient;
	}

	@Override
	public void initialize() throws Exception {
		//Key index support
		if (null == get(mName + KEYS_LOCATION)) {
			mMemcachedClient.set(mName + KEYS_LOCATION, NOT_EXPIRE, "");
		}
	}

	@Override
	public void clear() {
		for (Object key : keySet()) {
			remove((K) key);
		}
		//Removing the index
		mMemcachedClient.set(mName + KEYS_LOCATION, NOT_EXPIRE, "");
	}

	@Override
	public Set<K> keySet() {
		String index = (String) get(mName + KEYS_LOCATION);
		if (index.length() == 0) {
			return new FastSet<K>();
		} else {
			String[] lKeys = index.split(KEY_SEPARATOR);
			Set set = new FastSet();
			set.addAll(Arrays.asList(lKeys));

			return set;
		}
	}

	@Override
	public Collection<V> values() {
		return getAll(keySet()).values();
	}

	@Override
	public boolean containsKey(Object aKey) {
		return keySet().contains((K) aKey);
	}

	@Override
	public boolean containsValue(Object aValue) {
		return values().contains((V) aValue);
	}

	@Override
	public Map<K, V> getAll(Collection<K> aKeys) {
		Map<K, V> m = new FastMap<K, V>();
		for (K key : aKeys) {
			m.put(key, get(key));
		}
		return m;
	}

	@Override
	public V get(Object aKey) {
		V lObj = null;
		lObj = (V) mMemcachedClient.get(aKey.toString());
		return lObj;
	}

	@Override
	public V remove(Object aKey) {
		V lObj = (V) get(aKey);
		mMemcachedClient.delete(aKey.toString());

		//Key index update
		String lIndex = (String) get(mName + KEYS_LOCATION);
		lIndex = lIndex.replace(aKey.toString() + KEY_SEPARATOR, "");
		mMemcachedClient.set(mName + KEYS_LOCATION, NOT_EXPIRE, lIndex);

		return lObj;
	}

	@Override
	public V put(K aKey, V aValue) {
		mMemcachedClient.set(aKey.toString(), NOT_EXPIRE, aValue);

		//Key index update
		if (!keySet().contains(aKey)) {
			String lIndex = (String) get(mName + KEYS_LOCATION);
			lIndex = lIndex + aKey.toString() + KEY_SEPARATOR;
			mMemcachedClient.set(mName + KEYS_LOCATION, NOT_EXPIRE, lIndex);
		}

		return aValue;
	}

	@Override
	public boolean isEmpty() {
		return keySet().isEmpty();
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		for (K lKey : m.keySet()) {
			put(lKey, m.get(lKey));
		}
	}

	public MemcachedClient getMemcachedClient() {
		return mMemcachedClient;
	}

	public void setMemcachedClient(MemcachedClient aMemcachedClient) {
		this.mMemcachedClient = aMemcachedClient;
	}

	@Override
	public String getName() {
		return mName;
	}

	@Override
	public void setName(String aName) throws Exception {
		if (aName.length() == 0) {
			throw new InvalidParameterException();
		}
		Map<K, V> lAll = getAll(keySet());
		clear();

		this.mName = aName;
		initialize();
		for (K lKey : lAll.keySet()) {
			put(lKey, lAll.get(lKey));
		}
	}

	@Override
	public int size() {
		return keySet().size();
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		return getAll(keySet()).entrySet();
	}

	@Override
	public void shutdown() {
	}
}
