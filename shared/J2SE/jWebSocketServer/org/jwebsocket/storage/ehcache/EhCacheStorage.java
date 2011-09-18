//  ---------------------------------------------------------------------------
//  jWebSocket - EhCacheStorage (an IBasicStorage Implementation)
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
package org.jwebsocket.storage.ehcache;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javolution.util.FastSet;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import org.jwebsocket.api.IBasicStorage;
import org.jwebsocket.config.JWebSocketConfig;

/**
 *
 * @author aschulze
 */
public class EhCacheStorage implements IBasicStorage {

	private static final int DEF_MAX_IN_MEMORY = 1000;
	private static int mInstanceCounter = 0;
	private String mName = null;
	private static CacheManager mCacheManager = null;
	private Cache mCache = null;

	/**
	 * 
	 * @param aId
	 */
	public EhCacheStorage(String aId) {
		mInstanceCounter++;
		mName = aId;
		initialize();
	}

	/**
	 *
	 * @return
	 */
	@Override
	public String getName() {
		return mName;
	}

	/**
	 *
	 * @param aName
	 * @throws Exception
	 */
	@Override
	public void setName(String aName) throws Exception {
		mName = aName;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @return
	 */
	@Override
	public Set keySet() {
		Set lKeys = new FastSet();
		lKeys.addAll(mCache.getKeys());
		return lKeys;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @return
	 */
	@Override
	public int size() {
		return mCache.getSize();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param aKey
	 * @return
	 */
	@Override
	public Object get(Object aKey) {
		Element lElement = mCache.get(aKey);
		return (lElement != null ? lElement.getObjectValue() : null);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param aKey
	 * @return
	 */
	@Override
	public Object remove(Object aKey) {
		// TODO: The interface specs that a previous object is supposed to be returned
		// this may not be desired and reduce performance, provide second message
		Object lRes = mCache.get(aKey);
		mCache.remove(aKey);
		return lRes;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() {
		mCache.removeAll();
	}

	/**
	 * Save a key/value pair to the Store.
	 *
	 * @param aKey 
	 * @param aData
	 * @return
	 */
	@Override
	public Object put(Object aKey, Object aData) {
		Element lElement = new Element(aKey, aData);
		mCache.put(lElement);
		return null;
	}

	/**
	 *
	 * @param keys
	 * @return
	 */
	@Override
	public Map getAll(Collection keys) {
		// TODO: to be implemented
		return null;
	}

	/**
	 *
	 * @param aAll
	 */
	@Override
	public void putAll(Map aAll) {
		// TODO: to be implemented
	}

	/**
	 *
	 * @return
	 */
	@Override
	public Set entrySet() {
		// TODO: to be implemented
		return null;
	}

	/**
	 *
	 * @return
	 */
	@Override
	public Collection values() {
		// TODO: to be implemented
		return null;
	}

	/**
	 *
	 * @param aValue
	 * @return
	 */
	@Override
	public boolean containsValue(Object aValue) {
		// TODO: to be implemented
		return false;
	}

	/**
	 *
	 * @param aKey
	 * @return
	 */
	@Override
	public boolean containsKey(Object aKey) {
		return mCache.get(aKey) != null;
	}

	/**
	 *
	 * @return
	 */
	@Override
	public boolean isEmpty() {
		return size() <= 0;
	}

	/**
	 *
	 */
	@Override
	public void initialize() {
		if (mCacheManager == null) {
			mCacheManager = new CacheManager(JWebSocketConfig.getConfigFolder("ehcache.xml"));
		}
		if (mCacheManager != null) {
			// TODO: currenly hard coded, make configurable!
			mCache = mCacheManager.getCache(mName);
			if (mCache == null) {
				mCache = new Cache(new CacheConfiguration(mName, DEF_MAX_IN_MEMORY));
				mCacheManager.addCache(mCache);
			}
			// ;.memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU).overflowToDisk(false).eternal(true).diskPersistent(false).diskExpiryThreadIntervalSeconds(0));
			// makes no sense for eternal setting: timeToLiveSeconds(60).timeToIdleSeconds(30).
		}
	}

	/**
	 *
	 */
	@Override
	public void shutdown() {
		if (mCacheManager != null) {
			mCacheManager.shutdown();
		}
	}
}
