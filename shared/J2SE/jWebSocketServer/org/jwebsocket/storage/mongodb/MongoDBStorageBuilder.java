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
package org.jwebsocket.storage.mongodb;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import org.jwebsocket.api.IBasicStorage;

/**
 * Create MongoDBStorage instances
 *
 * @author kyberneees
 */
public class MongoDBStorageBuilder {

	private Mongo con;
	private String databaseName;
	private String collectionName;
	public static final String V1 = "v1";
	public static final String V2 = "v2";
	
	private DBCollection col = null;
	private DB db = null;

	/**
	 * 
	 * @return The Mongo database connection
	 */
	public Mongo getCon() {
		return con;
	}

	/**
	 * 
	 * @param con The Mongo database connection to set
	 */
	public void setCon(Mongo con) {
		this.con = con;
	}

	/**
	 * 
	 * @param name The storage name 
	 * @return The MongoDB storage ready to use.
	 */
	public IBasicStorage<String, Object> getStorage(String version, String name) throws Exception {
		IBasicStorage<String, Object> storage = null;
		if (version.equals(V1)) {
			storage = new MongoDBStorageV1<String, Object>(name, db);
			storage.initialize();
		} else if (version.equals(V2)) {
			storage = new MongoDBStorageV2<String, Object>(name, col);
			storage.initialize();
		}

		return storage;
	}

	/**
	 * @return the databaseName
	 */
	public String getDatabaseName() {
		return databaseName;
	}

	/**
	 * @param databaseName the databaseName to set
	 */
	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
		
		//Getting the temporal database instance to improve performance
		db = con.getDB(databaseName);
	}

	/**
	 * @return The database collection name for storages of version 2
	 */
	public String getCollectionName() {
		return collectionName;
	}

	/**
	 * @param collectionName The database collection name for storages of version 2
	 */
	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
		
		//Getting the temporal collection instance to improve performance
		col = con.getDB(databaseName).getCollection(collectionName);
	}
}