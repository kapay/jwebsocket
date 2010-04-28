/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.plugins.sharedobjects;

import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;

/**
 *
 * @author aschulze
 */
public class SharedObjects {

	private static Logger log = Logging.getLogger(SharedObjects.class);
	private FastMap<String, Object> objects = new FastMap<String, Object>();

	public Object put(String aKey, Object aObject) {
		return objects.put(aKey, aObject);
	}

	public Object remove(String aKey) {
		return objects.remove(aKey);
	}

	public Object get(String aKey) {
		return objects.get(aKey);
	}

	public boolean contains(String aKey) {
		return objects.containsKey(aKey);
	}

}
