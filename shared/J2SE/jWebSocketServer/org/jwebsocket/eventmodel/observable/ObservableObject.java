
package org.jwebsocket.eventmodel.observable;

import org.jwebsocket.eventmodel.api.IObservable;
import org.jwebsocket.eventmodel.api.IListener;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;
import java.util.Set;
import java.util.LinkedHashSet;

/**
 *
 * @author Itachi
 */
public abstract class ObservableObject implements IObservable{
  
  private Integer maxExecutionTime;
  private List<Class> events            = new LinkedList<Class>();
  private HashMap<Class, Set> listeners = new HashMap<Class, Set>();

  private void checkEvent(Class aEventClass) throws Exception{
    if (!events.contains(aEventClass))
      throw new IndexOutOfBoundsException("The event '"+ aEventClass + "' is not registered. Add it first!");
  }

  public void on(Collection<Class> aEventClassCollection, Callable aCallable) throws Exception{
    for (Class c : aEventClassCollection){
      on(c, aCallable);
    }
  }

  public void on(Class aEventClass, Callable aCallable) throws Exception{
    checkEvent(aEventClass);
    if (getListeners().containsKey(aEventClass)){
      getListeners().get(aEventClass).add(aCallable);
    }else{
      getListeners().put(aEventClass, new LinkedHashSet());
      on(aEventClass, aCallable);
    }
  }

  public void on(Class aEventClass, IListener aListener) throws Exception{
    checkEvent(aEventClass);
    if (getListeners().containsKey(aEventClass)){
      getListeners().get(aEventClass).add(aListener);
    }else{
      getListeners().put(aEventClass, new LinkedHashSet());
      on(aEventClass, aListener);
    }
  }

  public void on(Collection<Class> aEventClassCollection, IListener aListener) throws Exception{
    for (Class c : aEventClassCollection){
      on(c, aListener);
    }
  }

  public void addEvents(Class aEventClass){
    if (!getEvents().contains(aEventClass)){
      getEvents().add(aEventClass);
    }
  }

  public void addEvents(Collection<Class> aEventClassCollection){
    for (Class c : aEventClassCollection){
      addEvents(c);
    }
  }

  public void removeEvents(Class aEventClass){
    if (getEvents().contains(aEventClass))
      getEvents().remove(aEventClass);

    if (getListeners().containsKey(aEventClass))
      getListeners().remove(aEventClass);
  }

  public void removeEvents(Collection<Class> aEventClassCollection){
    for (Class c : aEventClassCollection){
      removeEvents(c);
    }
  }

  public void un(Class aEventClass, Callable aCallable){
    if (getListeners().containsKey(aEventClass))
      getListeners().get(aEventClass).remove(aCallable);
  }

  public void un(Class aEventClass, IListener aListener){
    if (getListeners().containsKey(aEventClass))
      getListeners().get(aEventClass).remove(aListener);
  }

  public void un(Collection<Class> aEventClassCollection, Callable aCallable){
    for (Class c : aEventClassCollection){
      un(c, aCallable);
    }
  }

  public void un(Collection<Class> aEventClassCollection, IListener aListener){
    for (Class c : aEventClassCollection){
      un(c, aListener);
    }
  }

  public ResponseEvent notify(Event aEvent, ResponseEvent aResponseEvent, boolean useThreads) throws Exception{
    checkEvent(aEvent.getClass());

    if (null == aResponseEvent){
      aResponseEvent = new ResponseEvent();
      aResponseEvent.setId(aEvent.getId());
    }

    long initTime = System.currentTimeMillis();

    if (getListeners().containsKey(aEvent.getClass()) && getListeners().get(aEvent.getClass()).size() > 0){
      
      Set calls = getListeners().get(aEvent.getClass());
      if (true == useThreads){
        ExecutorService pool = Executors.newCachedThreadPool();
        //Running in Threads
        for (Object it : calls){
          if (it instanceof IListener){
            IListener aListener = (IListener)it;
            pool.submit(new CallableListener(aListener, aEvent, aResponseEvent));
          }
          else { pool.submit((Callable)it); }
        }
        shutdownAndAwaitTermination(pool);
      }else{
        //Iterative execution
        for (Object it : calls){
          if (it instanceof IListener){
            IListener aListener = (IListener)it;
            aListener.processEvent(aEvent, aResponseEvent);
          }
          else { 
            Callable c = (Callable)it;
            c.call();
          }
        }
      }
    }
    
    aResponseEvent.setElapsedTime(System.currentTimeMillis() - initTime);
    return aResponseEvent;
  }

  private void shutdownAndAwaitTermination(ExecutorService pool) throws Exception{
    pool.shutdown(); // Disable new tasks from being submitted
    try {
      // Wait a while for existing tasks to terminate
      if (!pool.awaitTermination(maxExecutionTime, TimeUnit.SECONDS)) {
        pool.shutdownNow(); // Cancel currently executing tasks
        // Wait a while for tasks to respond to being cancelled
        if (!pool.awaitTermination(maxExecutionTime, TimeUnit.SECONDS))
          throw new Exception("Pool did not terminate!");
      }
    }catch (InterruptedException ie) {
      // (Re-)Cancel if current thread also interrupted
      pool.shutdownNow();
      // Preserve interrupt status
      Thread.currentThread().interrupt();
      throw ie;
    }
  }

  public ResponseEvent notifyUntil(Event aEvent, ResponseEvent aResponseEvent) throws Exception{
    checkEvent(aEvent.getClass());
    if (null == aResponseEvent){
      aResponseEvent = new ResponseEvent();
      aResponseEvent.setId(aEvent.getId());
    }

    long initTime = System.currentTimeMillis();

    if (getListeners().containsKey(aEvent.getClass()) && getListeners().get(aEvent.getClass()).size() > 0){
      Set calls = getListeners().get(aEvent.getClass());

      for (Object it : calls){
        if (it instanceof IListener){
          IListener aListener = (IListener)it;
          aListener.processEvent(aEvent, aResponseEvent);
        }
        else {
          Callable c = (Callable)it;
          c.call();
        }

        if (aEvent.isProcessed())
          break;
      }
    }

    aResponseEvent.setElapsedTime(System.currentTimeMillis() - initTime);
    return aResponseEvent;
  }

  public boolean hasListeners(Class aEventClass) throws Exception{
    checkEvent(aEventClass);
    if (getListeners().containsKey(aEventClass) && getListeners().get(aEventClass).size() > 0)
      return true;

    return false;
  }

  public boolean hasListener(Class aEventClass, Callable aCallable) throws Exception{
    checkEvent(aEventClass);
    if (getListeners().containsKey(aEventClass) && getListeners().get(aEventClass).contains(aCallable))
      return true;

    return false;
  }

  public boolean hasListener(Class aEventClass, IListener aListener) throws Exception{
    checkEvent(aEventClass);
    if (getListeners().containsKey(aEventClass) && getListeners().get(aEventClass).contains(aListener))
      return true;

    return false;
  }

  public void purgeListeners(){
     getListeners().clear();
  }

  public void purgeEvents(){
    getEvents().clear();
    purgeListeners();
  }

  public boolean hasEvent(Class aEventClass){
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
   * @return the events
   */
  public List<Class> getEvents() {
    return events;
  }

  /**
   * @param events the events to set
   */
  public void setEvents(List<Class> events) {
    this.events = events;
  }

  /**
   * @return the listeners
   */
  public HashMap<Class, Set> getListeners() {
    return listeners;
  }

  /**
   * @param listeners the listeners to set
   */
  public void setListeners(HashMap<Class, Set> listeners) {
    this.listeners = listeners;
  }

}
