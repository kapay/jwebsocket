//  ---------------------------------------------------------------------------
//  jWebSocket - EventsPlugIn
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
package org.jwebsocket.eventmodel.observable;

import java.lang.reflect.InvocationTargetException;
import org.jwebsocket.eventmodel.api.IObservable;
import org.jwebsocket.eventmodel.api.IListener;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Collection;
import java.util.Map;
import javolution.util.FastMap;
import java.util.Set;
import javolution.util.FastSet;
import java.lang.reflect.Method;
import org.jwebsocket.eventmodel.util.CommonUtil;

/**
 *
 * @author kyberneees
 */
public abstract class ObservableObject implements IObservable {

	private Integer maxExecutionTime = 1; //SECONDS
	private Set<Class<? extends Event>> events = new FastSet<Class<? extends Event>>();
	private Map<Class<? extends Event>, Set<IListener>> listeners = new FastMap<Class<? extends Event>, Set<IListener>>();

	private void checkEvent(Class<? extends Event> aEventClass) throws Exception {
		if (!events.contains(aEventClass)) {
			throw new IndexOutOfBoundsException("The event '" + aEventClass + "' is not registered. Add it first!");
		}
	}

	public void on(Class<? extends Event> aEventClass, IListener aListener) throws Exception {
		checkEvent(aEventClass);
		if (getListeners().containsKey(aEventClass)) {
			getListeners().get(aEventClass).add(aListener);
		} else {
			getListeners().put(aEventClass, new FastSet<IListener>());
			on(aEventClass, aListener);
		}
	}

	public void on(Collection<Class<? extends Event>> aEventClassCollection, IListener aListener) throws Exception {
		for (Class<? extends Event> c : aEventClassCollection) {
			on(c, aListener);
		}
	}

	public void addEvents(Class<? extends Event> aEventClass) {
		if (!getEvents().contains(aEventClass)) {
			getEvents().add(aEventClass);
		}
	}

	public void addEvents(Collection<Class<? extends Event>> aEventClassCollection) {
		for (Class<? extends Event> c : aEventClassCollection) {
			addEvents(c);
		}
	}

	public void removeEvents(Class<? extends Event> aEventClass) {
		if (getEvents().contains(aEventClass)) {
			getEvents().remove(aEventClass);
		}

		if (getListeners().containsKey(aEventClass)) {
			getListeners().remove(aEventClass);
		}
	}

	public void removeEvents(Collection<Class<? extends Event>> aEventClassCollection) {
		for (Class<? extends Event> c : aEventClassCollection) {
			removeEvents(c);
		}
	}

	public void un(Class<? extends Event> aEventClass, IListener aListener) {
		if (getListeners().containsKey(aEventClass)) {
			getListeners().get(aEventClass).remove(aListener);
		}
	}

	public void un(Collection<Class<? extends Event>> aEventClassCollection, IListener aListener) {
		for (Class<? extends Event> c : aEventClassCollection) {
			un(c, aListener);
		}
	}

	public ResponseEvent notify(Event aEvent, ResponseEvent aResponseEvent, boolean useThreads) throws Exception {
		checkEvent((Class<? extends Event>) aEvent.getClass());

		aEvent.setSubject(this);

		if (null == aResponseEvent) {
			aResponseEvent = new ResponseEvent();
			aResponseEvent.setId(aEvent.getId());
		}

		long initTime = System.nanoTime();

		if (getListeners().containsKey(aEvent.getClass()) && null != getListeners().get(aEvent.getClass())) {
			if (getListeners().get(aEvent.getClass()).size() > 0) {
				Set<IListener> calls = getListeners().get(aEvent.getClass());
				if (true == useThreads) {
					ExecutorService pool = Executors.newCachedThreadPool();
					//Running in Threads
					for (IListener it : calls) {
						pool.submit(new CallableListener(it, aEvent, aResponseEvent));
					}
					//Wait for ThreadPool termination
					CommonUtil.shutdownThreadPoolAndAwaitTermination(pool, getMaxExecutionTime());
				} else {
					//Iterative execution
					for (IListener it : calls) {
						ObservableObject.callProcessEvent(it, aEvent, aResponseEvent);
					}
				}
			}
		}

		aResponseEvent.setElapsedTime((System.nanoTime() - initTime));
		return aResponseEvent;
	}

	public static void callProcessEvent(IListener aListener, Event aEvent, ResponseEvent aResponseEvent) throws Exception{
		Class<? extends Event> aEventClass = aEvent.getClass();
		Class<? extends IListener> aListenerClass = aListener.getClass();
		Class<? extends ResponseEvent> aResponseClass = aResponseEvent.getClass();

		try {
			Method aMethod = aListenerClass.getMethod("processEvent", aEventClass, aResponseClass);
			aMethod.invoke(aListener, aEventClass.cast(aEvent), aResponseClass.cast(aResponseEvent));
		} catch (NoSuchMethodException ex) {
			//Calling the base method
			aListener.processEvent(aEvent, aResponseEvent);
		} catch (InvocationTargetException ex){
			throw (Exception)ex.getTargetException();
		}
	}

	public ResponseEvent notifyUntil(Event aEvent, ResponseEvent aResponseEvent) throws Exception {
		checkEvent(aEvent.getClass());

		aEvent.setSubject(this);

		if (null == aResponseEvent) {
			aResponseEvent = new ResponseEvent();
			aResponseEvent.setId(aEvent.getId());
		}

		long initTime = System.nanoTime();

		if (getListeners().containsKey(aEvent.getClass()) && null != getListeners().get(aEvent.getClass())) {
			if (getListeners().get(aEvent.getClass()).size() > 0) {
				Set<IListener> calls = getListeners().get(aEvent.getClass());

				for (IListener it : calls) {
					ObservableObject.callProcessEvent(it, aEvent, aResponseEvent);

					if (aEvent.isProcessed()) {
						break;
					}
				}
			}
		}

		aResponseEvent.setElapsedTime(System.nanoTime() - initTime);
		return aResponseEvent;
	}

	public boolean hasListeners(Class<? extends Event> aEventClass) throws Exception {
		checkEvent(aEventClass);
		if (getListeners().containsKey(aEventClass) && getListeners().get(aEventClass).size() > 0) {
			return true;
		}

		return false;
	}

	public boolean hasListener(Class<? extends Event> aEventClass, IListener aListener) throws Exception {
		checkEvent(aEventClass);
		if (getListeners().containsKey(aEventClass) && getListeners().get(aEventClass).contains(aListener)) {
			return true;
		}

		return false;
	}

	public void purgeListeners() {
		getListeners().clear();
	}

	public void purgeEvents() {
		getEvents().clear();
		purgeListeners();
	}

	public boolean hasEvent(Class<? extends Event> aEventClass) {
		return getEvents().contains(aEventClass);
	}

	/**
	 * @return the maxExecutionTime
	 */
	public Integer getMaxExecutionTime() {
		return maxExecutionTime;
	}

	/**
	 * @param maxExecutionTime the maxExecutionTime to set
	 */
	public void setMaxExecutionTime(Integer maxExecutionTime) {
		this.maxExecutionTime = maxExecutionTime;
	}

	/**
	 * @return the listeners
	 */
	public Map<Class<? extends Event>, Set<IListener>> getListeners() {
		return listeners;
	}

	/**
	 * @param listeners the listeners to set
	 */
	public void setListeners(Map<Class<? extends Event>, Set<IListener>> listeners) {
		this.listeners = listeners;
	}

	/**
	 * @return the events
	 */
	public Set<Class<? extends Event>> getEvents() {
		return events;
	}

	/**
	 * @param events the events to set
	 */
	public void setEvents(Set<Class<? extends Event>> events) {
		this.events = events;
	}
}
