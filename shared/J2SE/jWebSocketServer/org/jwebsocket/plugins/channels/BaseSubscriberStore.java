//  ---------------------------------------------------------------------------
//  jWebSocket - ChannelManager
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
import org.jwebsocket.data.store.JDBCStore;
import org.jwebsocket.logging.Logging;

/**
 * JDBC store based extension of SubscriberStore interface.
 * 
 * @author puran
 * @version $Id$
 */
public class BaseSubscriberStore extends JDBCStore implements SubscriberStore {
  /** logger object */
  private static Logger logger = Logging.getLogger(BaseSubscriberStore.class);

  /** default table name for the channel store */
  private static final String TABLE_NAME = "subscriber_store_table";

  /** default application column name for channels data store */
  private static final String APP_COLUMN_NAME = "subscribers";

  /** default key column name for channel data store */
  private static final String KEY_COLUMN_NAME = "subscriber_key";

  /** default value column name for channel data store */
  private static final String VALUE_COLUMN_NAME = "subscriber_value";

  /** properties */
  private static final String ID = "id";
  private static final String CHANNELS = "channels";
  private static final String LOGGED_IN_TIME = "logged_in_time";

  /**
   * default constructor
   */
  public BaseSubscriberStore() {
    init();
  }

  /**
   * initialize the JDBC store properties.
   */
  private void init() {
    super.tableName = TABLE_NAME;
    super.appColumnName = APP_COLUMN_NAME;
    super.keyColumnName = KEY_COLUMN_NAME;
    super.valueColumnName = VALUE_COLUMN_NAME;
  }

  @Override
  public Subscriber getSubscriber(String id) {
    JSONObject subscriberObject = (JSONObject) super.get(id);
    List<String> channels = new ArrayList<String>();
    Subscriber subscriber = null;
    try {
      long loggedInTime = subscriberObject.getLong(LOGGED_IN_TIME);
      JSONArray subscribersArray = subscriberObject.getJSONArray(CHANNELS);
      if (subscribersArray != null) {
        for (int i = 0; i < subscribersArray.length(); i++) {
          JSONObject idObject = subscribersArray.getJSONObject(i);
          String channelId = idObject.getString(ID);
          channels.add(channelId);
        }
      }
      subscriber = new Subscriber(id, new Date(loggedInTime), channels);
    } catch (JSONException e) {
      logger.error("Error parsing json response from the channel repository:", e);
    }
    return subscriber;
  }

  @Override
  public boolean storeSubscriber(Subscriber subscriber) {
    JSONObject subscriberObject = new JSONObject();
    try {
      subscriberObject.put(ID, subscriber.getId());
      subscriberObject.put(LOGGED_IN_TIME, subscriber.getLoggedInTime());

      JSONArray jsonArray = new JSONArray();
      for (String channel : subscriber.getChannels()) {
        JSONObject channelObject = new JSONObject();
        subscriberObject.put(ID, channel);
        jsonArray.put(channelObject);
      }
      subscriberObject.put(CHANNELS, jsonArray);
      return super.put(subscriber.getId(), subscriberObject);
    } catch (JSONException e) {
      logger.error("Error constructing JSON data for the given subscriber:[" + subscriber.getId()+"]", e);
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
    return super.getSize();
  }

}
