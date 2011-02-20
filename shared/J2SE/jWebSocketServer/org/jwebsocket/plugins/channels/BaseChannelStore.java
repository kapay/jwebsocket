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

import java.util.List;
import java.util.Map;
import java.util.Set;
import javolution.util.FastList;
import javolution.util.FastMap;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.storage.ehcache.EhCacheStorage;

/**
 * Base JDBC based implementation of the <tt>ChannelStore</tt>
 * 
 * @author puran, aschulze
 * @version $Id: BaseChannelStore.java 1101 2010-10-19 12:36:12Z fivefeetfurther$
 */
public class BaseChannelStore
		extends EhCacheStorage
		implements ChannelStore {

	/** logger object */
	private static Logger logger = Logging.getLogger(BaseChannelStore.class);
	private static final String ID = "id";
	private static final String NAME = "name";
	private static final String PRIVATE = "private";
	private static final String SYSTEM = "system";
	private static final String SECRET_KEY = "secret_key";
	private static final String ACCESS_KEY = "access_key";
	private static final String OWNER = "owner";
	private static final String STATE = "state";
	private static final String SUBSCRIBERS = "subscribers";
	private static final String PUBLISHERS = "publishers";

	/**
	 * default constructor
	 */
	public BaseChannelStore() {
		super("channelStore");
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param aId
	 */
	@Override
	public Channel getChannel(String aId) {
		return json2Channel((String) super.get(aId));
	}

	private Channel json2Channel(String lJSONStr) {
		Channel lChannel = null;
		try {
			JSONObject lJSONObj = new JSONObject(lJSONStr);
			String lChannelId = lJSONObj.getString(ID);
			String lChannelName = lJSONObj.getString(NAME);
			boolean lPrivate = lJSONObj.getBoolean(PRIVATE);
			boolean lSystem = lJSONObj.getBoolean(SYSTEM);
			String lSecretKey = lJSONObj.getString(SECRET_KEY);
			String lAccessKey = lJSONObj.getString(ACCESS_KEY);
			String lOwner = lJSONObj.getString(OWNER);
			int lStateValue = lJSONObj.getInt(STATE);
			JSONArray lJSSubscribers = lJSONObj.getJSONArray(SUBSCRIBERS);
			JSONArray lJSPublishers = lJSONObj.getJSONArray(PUBLISHERS);
			Channel.ChannelState lState = null;
			for (Channel.ChannelState lChannelState : Channel.ChannelState.values()) {
				if (lChannelState.getValue() == lStateValue) {
					lState = lChannelState;
					break;
				}
			}
			// construct the channel object
			lChannel = new Channel(lChannelId, lChannelName,
					lPrivate, lSystem,
					lAccessKey, lSecretKey,
					lOwner, lState);
			List lSubscribers = new FastList<String>();
			List lPublishers = new FastList<String>();
			for (int i = 0; i < lJSSubscribers.length(); i++) {
				lSubscribers.add(lJSSubscribers.getString(i));
			}
			for (int i = 0; i < lJSPublishers.length(); i++) {
				lPublishers.add(lJSPublishers.getString(i));
			}

			lChannel.setSubscribers(lSubscribers);
			lChannel.setPublishers(lPublishers);
		} catch (JSONException lEx) {
			logger.error("Error parsing JSON response from the channel store:", lEx);
		}
		return lChannel;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param aChannel
	 */
	@Override
	public boolean storeChannel(Channel aChannel) {
		JSONObject lJSON = new JSONObject();
		try {
			lJSON.put(ID, aChannel.getId());
			lJSON.put(NAME, aChannel.getName());
			lJSON.put(PRIVATE, aChannel.isPrivate());
			lJSON.put(SYSTEM, aChannel.isSystem());
			lJSON.put(SECRET_KEY, aChannel.getSecretKey());
			lJSON.put(ACCESS_KEY, aChannel.getAccessKey());
			lJSON.put(OWNER, aChannel.getOwner());
			lJSON.put(STATE, Channel.ChannelState.STARTED.getValue());
			JSONArray lSubscribers = new JSONArray(aChannel.getSubscribers());
			lJSON.put(SUBSCRIBERS, lSubscribers);
			JSONArray lPublishers = new JSONArray(aChannel.getPublishers());
			lJSON.put(PUBLISHERS, lPublishers);

			// now save
			// TODO: Need to think about how to return potential error (Exception?)
			super.put(aChannel.getId(), lJSON.toString());
			return true;
		} catch (JSONException e) {
			logger.error("Error constructing JSON data for the given channel '"
					+ aChannel.getName() + "'", e);
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

	@Override
	public Map<String, Channel> getChannels() {
		// TODO: EhCacheStorage does not yet implement values and entryset! Implement to be more efficient!
		Set lKeys = keySet();
		Map lRes = new FastMap<String, Channel>();
		if (lKeys != null) {
			for (Object lKey : lKeys) {
				Object lValue = get((String) lKey);
				Channel lChannel = json2Channel((String) lValue);
				lRes.put(lChannel.getId(), lChannel);
			}
		}
		return lRes;
	}
}
