
package org.jwebsocket.eventmodel.context;

import org.jwebsocket.eventmodel.api.IEventModelFilter;
import org.jwebsocket.eventmodel.api.IEventModelPlugIn;
import org.jwebsocket.eventmodel.api.IInitializable;
import org.jwebsocket.eventmodel.api.IListener;
import org.jwebsocket.eventmodel.observable.ObservableObject;
import org.jwebsocket.eventmodel.observable.ResponseEvent;
import org.jwebsocket.eventmodel.factory.EventFactory;
import org.jwebsocket.eventmodel.events.WebSocketResponseEvent;
import org.jwebsocket.eventmodel.observable.Event;
import org.jwebsocket.eventmodel.events.WebSocketEvent;
import org.jwebsocket.plugins.TokenPlugIn;

import org.jwebsocket.eventmodel.events.BeforeProcessEvent;
import org.jwebsocket.eventmodel.events.AfterProcessEvent;
import org.jwebsocket.eventmodel.events.ConnectorStarted;
import org.jwebsocket.eventmodel.events.ConnectorStopped;
import org.jwebsocket.eventmodel.events.EngineStarted;
import org.jwebsocket.eventmodel.events.EngineStopped;

import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;

import java.util.Set;
import java.util.LinkedList;
import org.jwebsocket.api.WebSocketConnector;

/**
 *
 * @author Itachi
 */
public class EventModel extends ObservableObject implements IInitializable, IListener{
  private Set<IEventModelFilter> filterChain;
  private Set<IEventModelPlugIn> plugIns;
  private TokenPlugIn parent;
  private EventFactory eventFactory;
  private static Logger mLog = Logging.getLogger(EventModel.class);

  public void initialize(){
    //Local events registration
    LinkedList<Class> myEventsList = new LinkedList<Class>();
    myEventsList.add(BeforeProcessEvent.class);
    myEventsList.add(AfterProcessEvent.class);
    myEventsList.add(ConnectorStarted.class);
    myEventsList.add(ConnectorStopped.class);
    myEventsList.add(EngineStarted.class);
    myEventsList.add(EngineStopped.class);

    addEvents(myEventsList);
  }

  public void processEvent(Event aEvent, WebSocketResponseEvent aResponseEvent){
    try{
      if (null == aResponseEvent){
        aResponseEvent = getEventFactory().createResponseEvent((WebSocketEvent)aEvent);
      }
      // ***************** FilterChain iteration. First call.
      for (IEventModelFilter f : getFilterChain())
        f.firstCall((WebSocketConnector)aEvent.getArgs().get(EventFactory.CONNECTOR_KEY), (WebSocketEvent)aEvent);

              //"before.process.event" notification
              if (mLog.isDebugEnabled()) {
                mLog.debug(">> before.process.event notification...");
              }
              Event e = getEventFactory().stringToEvent("before.process.event");
              e.setSubject(this);
              e.getArgs().put("event", aEvent);
              notify(e, null, true);

      //++++++++++++++ Listeners notification
      if (mLog.isDebugEnabled()) {
        mLog.debug(">> Executing EM listeners notifications...");
      }
      notify(aEvent, aResponseEvent, false);

              //"after.process.event" notification
              if (mLog.isDebugEnabled()) {
                mLog.debug(">> after.process.event notification...");
              }
              e = getEventFactory().stringToEvent("after.process.event");
              e.setSubject(this);
              e.getArgs().put("event", aEvent);
              notify(e, aResponseEvent, true);

      // ***************** FilterChain interation. Second call.
      int index = getFilterChain().size() - 1;
      while (index >= 0){
        ((IEventModelFilter)getFilterChain().
          toArray()[index]).
          secondCall((WebSocketConnector)aEvent.getArgs().get(EventFactory.CONNECTOR_KEY), aResponseEvent);
        index--;
      }
    }
    catch(Exception ex){
      trace(ex);

    }
  }

  private void trace(Exception ex){
    mLog.error(ex.toString());
    ex.printStackTrace();
  }

  public void shutdown(){

  }

  /**
   * @return the filterChain
   */
  public Set<IEventModelFilter> getFilterChain() {
    return filterChain;
  }

  /**
   * @param filterChain the filterChain to set
   */
  public void setFilterChain(Set<IEventModelFilter> filterChain) {
    this.filterChain = filterChain;
  }

  /**
   * @return the plugIns
   */
  public Set<IEventModelPlugIn> getPlugIns() {
    return plugIns;
  }

  /**
   * @param plugIns the plugIns to set
   */
  public void setPlugIns(Set<IEventModelPlugIn> plugIns) {
    this.plugIns = plugIns;
  }

  /**
   * @return the parent
   */
  public TokenPlugIn getParent() {
    return parent;
  }

  /**
   * @param parent the parent to set
   */
  public void setParent(TokenPlugIn parent) {
    this.parent = parent;
  }

  /**
   * @return the eventFactory
   */
  public EventFactory getEventFactory() {
    return eventFactory;
  }

  /**
   * @param eventFactory the eventFactory to set
   */
  public void setEventFactory(EventFactory eventFactory) {
    this.eventFactory = eventFactory;
  }

  public void processEvent(Event aEvent, ResponseEvent aResponseEvent){}

}

