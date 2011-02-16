//  ---------------------------------------------------------------------------
//  jWebSocket - BaseChannelStore
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
import org.json.JSONException;
import org.json.JSONObject;
import org.jwebsocket.storage.jdbc.JDBCStorage;
import org.jwebsocket.logging.Logging;

/**
 * Base JDBC based implementation of the <tt>ChannelStore</tt>
 * 
 * @author puran
 * @version $Id: BaseChannelStore.java 1101 2010-10-19 12:36:12Z fivefeetfurther$
 */
public class BaseChannelStore extends JDBCStorage implements ChannelStore {

	/** logger object */
	private static Logger logger = Logging.getLogger(BaseChannelStore.class);
	/** default table name for the channel store */
	private static final String TABLE_NAME = "channel_store_table";
	/** default mysql driver name for channel store */
	private static final String DRIVER_NAME = "com.mysql.jdbc.Driver";
	/** default application column name for channels data store */
	private static final String APP_COLUMN_NAME = "channels";
	/** default key column name for channel data store */
	private static final String KEY_COLUMN_NAME = "channel_key";
	/** default value column name for channel data store */
	private static final String VALUE_COLUMN_NAME = "channel_value";
	/** properties */
	private static final String ID = "id";
	private static final String NAME = "name";
	private static final String SUBSCRIBER_COUNT = "subscriber_count";
	private static final String PRIVATE = "private";
	private static final String SYSTEM = "system";
	private static final String SECRET_KEY = "secret_key";
	private static final String ACCESS_KEY = "access_key";
	private static final String OWNER = "owner";
	private static final String CREATED_DATE = "created_date";
	private static final String STATE = "state";

	/**
	 * default constructor
	 */
	public BaseChannelStore() {
		init();
	}

	/**
	 * initialize the JDBC store properties.
	 */
	private void init() {
		super.tableName = TABLE_NAME;
		super.driverName = DRIVER_NAME;
		super.appColumnName = APP_COLUMN_NAME;
		super.keyColumnName = KEY_COLUMN_NAME;
		super.valueColumnName = VALUE_COLUMN_NAME;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Channel getChannel(String id) {
		Channel channel = null;
		List<Subscriber> subscribers = new ArrayList<Subscriber>();
		List<Publisher> publishers = new ArrayList<Publisher>();
		String channelData = (String) super.get(id);
		try {
			JSONObject channelObject = new JSONObject(channelData);
			String channelId = channelObject.getString(ID);
			String channelName = channelObject.getString(NAME);
			int subscriberCount = channelObject.getInt(SUBSCRIBER_COUNT);
			boolean privateChannel = channelObject.getBoolean(PRIVATE);
			boolean systemChannel = channelObject.getBoolean(SYSTEM);
			String secretKey = channelObject.getString(SECRET_KEY);
			String accessKey = channelObject.getString(ACCESS_KEY);
			String owner = channelObject.getString(OWNER);
			Date createdDate = null; // channelObject.getLong(CREATED_DATE);
			int stateValue = channelObject.getInt(STATE);
			Channel.ChannelState state = null;
			for (Channel.ChannelState ch : Channel.ChannelState.values()) {
				if (ch.getValue() == stateValue) {
					state = ch;
					break;
				}
			}
			// construct the channel object
			channel = new Channel(channelId, channelName, privateChannel, systemChannel, secretKey,
					accessKey, owner, createdDate, state);
		} catch (JSONException e) {
			logger.error("Error parsing json response from the channel store:", e);
		}
		return channel;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean storeChannel(Channel channel) {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(ID, channel.getId());
			jsonObject.put(NAME, channel.getName());
			jsonObject.put(SUBSCRIBER_COUNT, channel.getSubscriberCount());
			jsonObject.put(PRIVATE, channel.isPrivate());
			jsonObject.put(SYSTEM, channel.isSystem());
			jsonObject.put(SECRET_KEY, channel.getSecretKey());
			jsonObject.put(ACCESS_KEY, channel.getAccessKey());
			jsonObject.put(CREATED_DATE, channel.getCreatedDate());
			jsonObject.put(OWNER, channel.getOwner());

			// now save
			// TODO: Need to think about how to return potential error (Exception?)
			super.put(channel.getId(), jsonObject);
			return true;
		} catch (JSONException e) {
			logger.error("Error constructing JSON data for the given channel:" + channel.getName(), e);
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeChannel(String id) {
		super.remove(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clearChannels() {
		super.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getChannelStoreSize() {
		return size();
	}
}
