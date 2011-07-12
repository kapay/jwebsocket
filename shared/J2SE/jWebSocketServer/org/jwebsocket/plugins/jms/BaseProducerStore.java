//  ---------------------------------------------------------------------------
//  jWebSocket - BasePublisherStore
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
package org.jwebsocket.plugins.jms;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;

/**
 * 
 * @author jsmutny
 * holds references to all connected jms producers
 */
public class BaseProducerStore implements ProducerStore {

	/** logger object */
	private static Logger mLog = Logging.getLogger(BaseConsumerStore.class);
	private Map<String, Producer> mProducers = new ConcurrentHashMap<String, Producer>();

	@Override
	public Producer getProducer(String aId) {
		return mProducers.get(aId);
	}

	@Override
	public void storeProducer(Producer aProducer) {
		mProducers.put(aProducer.getId(), aProducer);
	}

	@Override
	public void removeProducer(String id) {
		mProducers.remove(id);
	}

	@Override
	public void clearProducers() {
		mProducers.clear();
	}

	@Override
	public int getProducersStoreSize() {
		return mProducers.size();
	}
}
