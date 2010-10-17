//  ---------------------------------------------------------------------------
//  jWebSocket - RequestHeader Object
//  Copyright (c) 2010 jWebSocket
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
package org.jwebsocket.plugins.channels;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jwebsocket.data.store.JDBCStore;
import org.jwebsocket.logging.Logging;

/**
 * Base JDBC based implementation of the <tt>ChannelStore</tt>
 * 
 * @author puran
 * @version $Id$
 */
public class BaseChannelStore extends JDBCStore implements ChannelStore {
    /**
     * logger
     */
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

    /** default connection url for the channels data store */
    private static final String CONNECTION_URL = "jdbc:mysql://localhost:3306/jwebsocketdb";

    /** default connection user name for channels data store */
    private static final String CONNECTION_NAME = "jwebsocket";

    /** default connection password for the channels data store */
    private static final String CONNECTION_PASSWORD = "himalayanyoddha";
    
    /** properties */
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String SUBSCRIBER_COUNT = "subscriber_count";
    private static final String PRIVATE = "private";
    private static final String SYSTEM = "system";
    private static final String SUBSCRIBERS = "subscribers";
    private static final String SUBSCRIBER_KEY = "subscriber_key";
    private static final String CHANNELS = "channels";

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
        super.connectionURL = CONNECTION_URL;
        super.connectionName = CONNECTION_NAME;
        super.connectionPassword = CONNECTION_PASSWORD;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Channel getChannel(String id) {
        Channel channel = null;
        List<Subscriber> subscribers = new ArrayList<Subscriber>();
        String channelData = (String) super.get(id);
        try {
            JSONObject jsonObject = new JSONObject(channelData);
            String channelId = jsonObject.getString(ID);
            String channelName = jsonObject.getString(NAME);
            int subscriberCount = jsonObject.getInt(SUBSCRIBER_COUNT);
            boolean privateChannel = jsonObject.getBoolean(PRIVATE);
            boolean systemChannel = jsonObject.getBoolean(SYSTEM);
            
            JSONArray subscribersArray = jsonObject.getJSONArray(SUBSCRIBERS);
            if (subscribersArray != null) {
                for (int i = 0; i < subscribersArray.length(); i++) {
                    JSONObject subscriberObject = subscribersArray.getJSONObject(i);
                    String subscriberId = subscriberObject.getString(ID);
                    String subscriberName = subscriberObject.getString(NAME);
                    String subscriberKey = subscriberObject.getString(SUBSCRIBER_KEY);
                    String channels = subscriberObject.getString(CHANNELS);
                    
                    Subscriber subscriber = new Subscriber(subscriberId, subscriberName, subscriberKey, channels);
                    subscribers.add(subscriber);
                }
            }
            channel = new Channel(channelId, channelName, subscriberCount, privateChannel, systemChannel,
                    subscribers);
        } catch (JSONException e) {
            logger.error("Error parsing json response from the channel store:", e);
        }
        return channel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void storeChannel(Channel channel) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeChannel(String id) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearChannels() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getChannelStoreSize() {
        // TODO Auto-generated method stub
        return 0;
    }
}