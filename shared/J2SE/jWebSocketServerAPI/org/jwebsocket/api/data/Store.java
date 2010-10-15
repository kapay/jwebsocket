package org.jwebsocket.api.data;


/**
 * A <b>Store</b> is the abstraction of a jWebSocket component that provides
 * persistent storage and loading of session data as well as data specific to
 * plugins, channels and different server implementations. 
 * 
 * It provides a simple primary-key only interface to perform the store and 
 * retrieval operation from the data store. The implementation of this interface 
 * can use any storage that supports key-value system. The design of key-value
 * based store is to allow horizontal scalability. 
 * @author puran
 * @version $Id$
 */
public interface Store {
  /**
   * Return descriptive information about this Store implementation and the
   * corresponding version number, in the format
   * <code>&lt;description&gt;/&lt;version&gt;</code>.
   */
  String getInfo();

  /**
   * Return the number of Sessions present in this Store.
   */
  int getSize();

  /**
   * Return an array containing the list of keys for all the data stored in this
   * Store. If there are no such Sessions, a zero-length array is returned.
   * @return the arrays of keys
   */
   String[] keys();

  /**
   * Return the data for the given key from this Store, without
   * removing it. If there is no data stored, return <code>null</code>.
   * 
   * @param key the data key for which to load the value
   * @return the value object, null if not found
   */
   Object get(String key);
   /**
    * Save the given key/value pair into this Store. Any previously saved
    * value for the same given key will just be replaced by this new value.
    * @param key the key identifier of the data
    * @return {@code true} if the put operation is successful, {@code false}
    * otherwise.
    */
   boolean put(String key, Object data);

  /**
   * Remove the data physically for the given key from this Store,
   * if present. If no such data is present, this method takes no action.
   * 
   * @param key the key of the data to remove from the store
   */
   void remove(String key);

  /**
   * clear the store, use this with caution. This method is extremely dangerous
   * and can delete everything from the store without any remains.
   */
   void clear();
}
