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
//  ---------------------------------------------------------------------------
package org.jwebsocket.plugins.channels;

import org.jwebsocket.data.store.JDBCStore;

/**
 * This class handles all the data store/retrieval operations for channels.
 * @author puran
 * @version $Id$ 
 */
public class ChannelStore extends JDBCStore {
    
    /** default table name for the channel store */
    private static final String TABLE_NAME = "table_channels_store";
    
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
    private static final String CONNECTION_PASSWORD = "himalayantiger";
    
    /**
     * default constructor
     */
    public ChannelStore() {
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

    @Override
    public String getInfo() {
        // TODO Auto-generated method stub
        return super.getInfo();
    }

    @Override
    public Object get(String key) {
        // TODO Auto-generated method stub
        return super.get(key);
    }

    @Override
    public void remove(String key) {
        // TODO Auto-generated method stub
        super.remove(key);
    }

    @Override
    public boolean put(String key, Object data) {
        // TODO Auto-generated method stub
        return super.put(key, data);
    }

}
