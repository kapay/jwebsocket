//  ---------------------------------------------------------------------------
//  jWebSocket - BasePublisherStore
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
package org.jwebsocket.plugins.channels;

import java.util.Date;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.storage.ehcache.EhCacheStorage;

public class BasePublisherStore 
		extends EhCacheStorage
		implements PublisherStore {

	/** logger object */
	private static Logger mLog = Logging.getLogger(BaseSubscriberStore.class);
	/** default table name for the channel store */
	// private static final String TABLE_NAME = "publisher_store_table";
	/** default application column name for channels data store */
	// private static final String APP_COLUMN_NAME = "publishers";
	/** default key column name for channel data store */
	// private static final String KEY_COLUMN_NAME = "publisher_key";
	/** default value column name for channel data store */
	// private static final String VALUE_COLUMN_NAME = "publisher_value";
	/** properties */
	private static final String ID = "id";
	private static final String LOGIN = "login";
	private static final String CHANNEL = "channel";
	private static final String AUTHORIZED_DATE = "authorized_date";
	private static final String LAST_PUBLISHED_DATE = "last_published_date";
	private static final String IS_AUTHORIZED = "authorized";

	/**
	 * default constructor
	 */
	public BasePublisherStore() {
		super("channelPublishers");
		init();
	}

	/**
	 * initialize the JDBC store properties.
	 */
	private void init() {
		/*
		super.tableName = TABLE_NAME;
		super.appColumnName = APP_COLUMN_NAME;
		super.keyColumnName = KEY_COLUMN_NAME;
		super.valueColumnName = VALUE_COLUMN_NAME;
		 */
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Publisher getPublisher(String aId) {
		// TODO: Alex: Added by Alex:
		JSONObject lPublisherObject = null;
		try {
			String lStr = (String) super.get(aId);
			JSONTokener lJT = new JSONTokener(lStr);
			lPublisherObject = new JSONObject(lJT);
		} catch (Exception lEx) {
		}
		// JSONObject lPublisherObject = (JSONObject) super.get(aId);
		Publisher publisher = null;

		// Added by Alex:
		if (lPublisherObject == null) {
			return null;
		}

		try {
			String login = lPublisherObject.getString(LOGIN);
			String channel = lPublisherObject.getString(CHANNEL);
			Long authorizedDate = lPublisherObject.getLong(AUTHORIZED_DATE);
			Long lastPublishedDate = lPublisherObject.getLong(LAST_PUBLISHED_DATE);
			boolean authorized = lPublisherObject.getBoolean(IS_AUTHORIZED);
			publisher = new Publisher(aId, login, channel, new Date(authorizedDate), new Date(lastPublishedDate), authorized);
		} catch (JSONException lEx) {
			mLog.error(
					"Error parsing json response from the channel repository: ",
					lEx);
		}
		
		return publisher;
	}

	@Override
	public boolean storePublisher(Publisher lPublisher) {
		JSONObject lPublisherObject = new JSONObject();
		try {
			lPublisherObject.put(ID, lPublisher.getId());
			lPublisherObject.put(LOGIN, lPublisher.getLogin());
			lPublisherObject.put(CHANNEL, lPublisher.getChannel());
			// TODO: Process date conversion properly!
			lPublisherObject.put(AUTHORIZED_DATE, lPublisher.getAuthorizedDate().getTime());
			// TODO: Process date conversion properly!
			lPublisherObject.put(LAST_PUBLISHED_DATE, lPublisher.getLastPublishedDate().getTime());
			lPublisherObject.put(IS_AUTHORIZED, lPublisher.isAuthorized());
			// TODO: Need to think about how to return potential error (Exception?)
			// TODO: updated by Alex: subscriberObject.toString() instead of subscriberObject (JSONObject is not serializable!)
			// TODO: Need to think about how to return potential error (Exception?)
			super.put(lPublisher.getId(), lPublisherObject.toString());
			return true;
		} catch (JSONException e) {
			mLog.error("Error constructing JSON data for the given publisher '" + lPublisher.getId() + "'", e);
			return false;
		}
	}

	@Override
	public void removePublisher(String aId) {
		super.remove(aId);
	}

	@Override
	public void clearPublishers() {
		super.clear();
	}

	@Override
	public int getPublisherStoreSize() {
		return size();
	}
}
