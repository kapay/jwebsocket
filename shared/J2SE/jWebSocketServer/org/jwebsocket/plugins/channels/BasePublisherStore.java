package org.jwebsocket.plugins.channels;

import java.util.Date;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.jwebsocket.data.store.JDBCStore;
import org.jwebsocket.logging.Logging;

public class BasePublisherStore extends JDBCStore implements PublisherStore {
    /** logger object */
    private static Logger logger = Logging.getLogger(BaseSubscriberStore.class);

    /** default table name for the channel store */
    private static final String TABLE_NAME = "publisher_store_table";

    /** default application column name for channels data store */
    private static final String APP_COLUMN_NAME = "publishers";

    /** default key column name for channel data store */
    private static final String KEY_COLUMN_NAME = "publisher_key";

    /** default value column name for channel data store */
    private static final String VALUE_COLUMN_NAME = "publisher_value";

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

    /**
     * {@inheritDoc}
     */
    @Override
    public Publisher getPublisher(String id) {
        JSONObject publisherObject = (JSONObject) super.get(id);
        Publisher publisher = null;
        try {
            String login = publisherObject.getString(LOGIN);
            String channel = publisherObject.getString(CHANNEL);
            Long authorizedDate = publisherObject.getLong(AUTHORIZED_DATE);
            Long lastPublishedDate = publisherObject.getLong(LAST_PUBLISHED_DATE);
            boolean authorized = publisherObject.getBoolean(IS_AUTHORIZED);
            publisher = new Publisher(id, login, channel, new Date(authorizedDate), new Date(lastPublishedDate), authorized);
        } catch (JSONException e) {
          logger.error("Error parsing json response from the channel repository:", e);
        }
        return publisher;
    }

    @Override
    public boolean storePublisher(Publisher publisher) {
        JSONObject publisherObject = new JSONObject();
        try {
            publisherObject.put(ID, publisher.getId());
            publisherObject.put(LOGIN, publisher.getLogin());
            publisherObject.put(CHANNEL, publisher.getChannel());
            publisherObject.put(AUTHORIZED_DATE, publisher.getAuthorizedDate());
            publisherObject.put(LAST_PUBLISHED_DATE, publisher.getLastPublishedDate());
            publisherObject.put(IS_AUTHORIZED, publisher.isAuthorized());
            
            return super.put(publisher.getId(), publisherObject);
        } catch (JSONException e) {
          logger.error("Error constructing JSON data for the given publisher:[" + publisher.getId()+"]", e);
          return false;
        }
    }

    @Override
    public void removePublisher(String id) {
        super.remove(id);
    }

    @Override
    public void clearPublishers() {
        super.clear();
    }

    @Override
    public int getPublisherStoreSize() {
        return super.getSize();
    }

}
