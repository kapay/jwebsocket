//  ---------------------------------------------------------------------------
//  jWebSocket - BaseSubscriberStore
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.storage.ehcache.EhCacheStorage;

/**
 * JDBC store based extension of SubscriberStore interface.
 * 
 * @author puran
 * @version $Id$
 */
public class BaseSubscriberStore
		extends EhCacheStorage
		implements SubscriberStore {

	/** logger object */
	private static Logger logger = Logging.getLogger(BaseSubscriberStore.class);

	/** default table name for the channel store */
	// private static final String TABLE_NAME = "subscriber_store_table";
	/** default application column name for channels data store */
	// private static final String APP_COLUMN_NAME = "subscribers";
	/** default key column name for channel data store */
	// private static final String KEY_COLUMN_NAME = "subscriber_key";
	/** default value column name for channel data store */
	// private static final String VALUE_COLUMN_NAME = "subscriber_value";
	/** properties */
	private static final String ID = "id";
	private static final String CHANNELS = "channels";
	private static final String LOGGED_IN_TIME = "logged_in_time";

	/**
	 * default constructor
	 */
	public BaseSubscriberStore() {
		super("channelSubscribers");
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

	@Override
	public Subscriber getSubscriber(String aId) {
		// TODO: Alex: Added by Alex:
		JSONObject lSubscriberObject = null;
		try {
			String lStr = (String) super.get(aId);
			JSONTokener lJT = new JSONTokener(lStr);
			lSubscriberObject = new JSONObject(lJT);
		} catch (Exception lEx) {
		}
		// JSONObject subscriberObject = (JSONObject) super.get(id);
		// Added by Alex:
		if (lSubscriberObject == null) {
			return null;
		}
		List<String> lChannels = new ArrayList<String>();
		Subscriber lSubscriber = null;
		// TODO: fix: if subscriberObject == null => exception
		try {
			long lLoggedInTime = lSubscriberObject.getLong(LOGGED_IN_TIME);
			JSONArray lSubscribersArray = lSubscriberObject.getJSONArray(CHANNELS);
			if (lSubscribersArray != null) {
				for (int lIdx = 0; lIdx < lSubscribersArray.length(); lIdx++) {
					JSONObject lObj = lSubscribersArray.getJSONObject(lIdx);
					String lChannelId = lObj.getString(ID);
					lChannels.add(lChannelId);
				}
			}
			lSubscriber = new Subscriber(aId, new Date(lLoggedInTime), lChannels);
		} catch (JSONException lEx) {
			logger.error("Error parsing json response from the channel repository:", lEx);
		}
		return lSubscriber;
	}

	@Override
	public boolean storeSubscriber(Subscriber aSubscriber) {
		JSONObject lSubscriberObject = new JSONObject();
		try {
			lSubscriberObject.put(ID, aSubscriber.getId());
			// TODO: Updated by Alex: .getTime()!
			lSubscriberObject.put(LOGGED_IN_TIME, aSubscriber.getLoggedInTime().getTime());

			JSONArray lJSONArray = new JSONArray();
			for (String lChannel : aSubscriber.getChannels()) {
				JSONObject lChannelObject = new JSONObject();
				// TODO: Updated by Alex: channelObject!
				lChannelObject.put(ID, lChannel);
				lJSONArray.put(lChannelObject);
			}
			lSubscriberObject.put(CHANNELS, lJSONArray);
			// TODO: updated by Alex: subscriberObject.toString() instead of subscriberObject (JSONObject is not serializable!)
			// TODO: Need to think about how to return potential error (Exception?)
			super.put(aSubscriber.getId(), lSubscriberObject.toString());
			return true;
		} catch (JSONException lEx) {
			logger.error("Error constructing JSON data for the given subscriber '"
					+ aSubscriber.getId() + "'", lEx);
			return false;
		}
	}

	@Override
	public void removeSubscriber(String id) {
		super.remove(id);
	}

	@Override
	public void clearSubscribers() {
		super.clear();
	}

	@Override
	public int getSubscribersStoreSize() {
		return super.size();
	}
}
