package org.jwebsocket.eventmodel.api;

import org.jwebsocket.eventmodel.observable.*;
import java.util.concurrent.Callable;
import java.util.Collection;

/**
 *
 * @author Itachi
 */
public interface IObservable{

  public void on(Collection<Class> aEventClassCollection, Callable aCallable) throws Exception;
  public void on(Class aEventClass, Callable aCallable) throws Exception;
  public void on(Collection<Class> aEventClassCollection, IListener aListener) throws Exception;
  public void on(Class aEventClass, IListener aListener) throws Exception;
  public void addEvents(Class aEventClass);
  public void addEvents(Collection<Class> aEventClassCollection);
  public void removeEvents(Class aEventClass);
  public void removeEvents(Collection<Class> aEventClassCollection);
  public void un(Class aEventClass, Callable aCallable);
  public void un(Collection<Class> aEventClassCollection, Callable aCallable);
  public void un(Class aEventClass, IListener aListener);
  public void un(Collection<Class> aEventClassCollection, IListener aListener);
  public ResponseEvent notify(Event aEvent, ResponseEvent aResponseEvent, boolean useThreads) throws Exception;
  public ResponseEvent notifyUntil(Event aEvent, ResponseEvent aResponseEvent) throws Exception;
  public boolean hasListeners(Class aEventClass) throws Exception;
  public boolean hasListener(Class aEventClass, Callable aCallable) throws Exception;
  public boolean hasListener(Class aEventClass, IListener aListener) throws Exception;
  public void purgeListeners();
  public void purgeEvents();
  public boolean hasEvent(Class aEventClass);

}