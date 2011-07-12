//  ---------------------------------------------------------------------------
//  jWebSocket - JDBCStorage
//  Copyright (c) 2011 Innotrade GmbH, jWebSocket.org
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
package org.jwebsocket.storage.mongodb;

import org.jwebsocket.api.IBasicStorage;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author rbetancourt
 */
public class MongoDBStorage<K extends String, V extends Object> implements IBasicStorage<K, V> {

	private DB db;
	private String name;
	private DBCollection myCollection;

	/**
	 * Create a new MongoDBStorage instance
	 *
	 * @param name The name of the storage container
	 * @param db The MongoDB database instance
	 */
	public MongoDBStorage(String name, DB db) {
		this.db = db;
		this.name = name;
		myCollection = db.getCollection(name);
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public void initialize() throws Exception {
		myCollection.ensureIndex(new BasicDBObject().append("k", 1),
				new BasicDBObject().append("unique", true));
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public void shutdown() throws Exception {
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public void setName(String newName) throws Exception {
		db.createCollection(newName, null);
		DBCollection newCollection = db.getCollection(newName);
		Map<K, V> map = getAll(keySet());
		for (K key : map.keySet()) {
			newCollection.insert(new BasicDBObject().append("k", key).append("v", map.get(key)));
		}
		myCollection.drop();
		myCollection = newCollection;
		name = newName;
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public Map<K, V> getAll(Collection<K> keys) {
		HashMap<K, V> map = new HashMap<K, V>();
		for (K key : keys) {
			map.put((K) key, get(key));
		}
		return map;
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public int size() {
		return (int) myCollection.count();
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public boolean isEmpty() {
		if (myCollection.count() == 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public boolean containsKey(Object key) {
		DBObject obj = myCollection.findOne(new BasicDBObject().append("k", (String) key));
		if (obj != null && obj.get("k").equals(key)) {
			return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public boolean containsValue(Object value) {
		DBObject obj = myCollection.findOne(new BasicDBObject().append("v", value));
		if (obj != null && obj.get("v").equals(value)) {
			return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public V get(Object key) {
		return (V) myCollection.findOne(new BasicDBObject().append("k", key)).get("v");
	}

	@Override
	public V put(K key, V value) {
		BasicDBObject obj = new BasicDBObject();
		obj.append("k", key);
		DBCursor cur = myCollection.find(obj);
		if (!cur.hasNext()) {
			obj.append("v", value);
			myCollection.insert(obj);
		} else {
			DBObject upd = cur.next();
			upd.put("v", value);
			myCollection.save(upd);
		}
		return value;
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public V remove(Object key) {
		if (containsKey(key)) {
			V val = get(key);
			myCollection.remove(new BasicDBObject().append("k", key));
			return val;
		} else {
			throw new IndexOutOfBoundsException();
		}
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		for (K key : m.keySet()) {
			put(key, m.get(key));
		}
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public void clear() {
		myCollection.drop();
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public Set<K> keySet() {
		Set<K> s = new HashSet<K>();
		DBCursor cur = myCollection.find();
		while (cur.hasNext()) {
			s.add((K) cur.next().get("k"));
		}
		return s;
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public Collection<V> values() {
		List<V> l = new ArrayList<V>();
		DBCursor cur = myCollection.find();
		while (cur.hasNext()) {
			l.add((V) cur.next().get("v"));
		}
		return l;
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	public Set<Entry<K, V>> entrySet() {
		return getAll(keySet()).entrySet();
	}
}
