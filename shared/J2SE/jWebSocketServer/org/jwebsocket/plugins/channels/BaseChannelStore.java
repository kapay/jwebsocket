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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jwebsocket.data.store.JDBCStore;

/**
 * Base JDBC based implementation of the <tt>ChannelStore</tt> 
 * @author puran
 * @version $Id$
 */
public class BaseChannelStore extends JDBCStore implements ChannelStore {
    
    /** default table name for the channel store */
    private static final String TABLE_NAME = "channel_store_table";
    
    /** default mysql driver name for channel store */
    private static final String DRIVER_NAME = "com.mysql.jdbc.Driver";
    
    /** default application column name for channels data store  */
    private static final String APP_COLUMN_NAME = "channels";
    
    /** default key column name for channel data store */
    private static final String KEY_COLUMN_NAME = "channel_key";
    
    /** default value column name for channel data store */
    private static final String VALUE_COLUMN_NAME = "channel_value";
    
    /** default connection url for the channels data store */
    private static final String CONNECTION_URL = "jdbc:mysql://localhost:3306/jwebsocketdb";
    
    /** default connection user name for channels data store*/
    private static final String CONNECTION_NAME = "jwebsocket";
    
    /** default connection password for the channels data store*/
    private static final String CONNECTION_PASSWORD = "himalayanyoddha";
    
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
        String channelData = (String)super.get(id);
        try {
            JSONObject jsonObject = new JSONObject(channelData);
            String channelId = jsonObject.getString("id");
            String channelName = jsonObject.getString("name");
            boolean privateChannel = jsonObject.getBoolean("private");
            
            JSONArray subscribers = jsonObject.getJSONArray("subscribers");
            for (int i = 0; i < subscribers.length(); i++) {
                JSONObject subscriberObject = subscribers.getJSONObject(i);
            }
            
        } catch (JSONException e) {
        }
        return null;
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