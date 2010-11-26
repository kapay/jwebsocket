
package org.jwebsocket.eventmodel.factory;

import org.jwebsocket.eventmodel.events.WebSocketEvent;
import org.jwebsocket.eventmodel.events.WebSocketResponseEvent;
import org.jwebsocket.token.Token;
import org.jwebsocket.eventmodel.events.EventsMap;
import org.jwebsocket.logging.Logging;
import org.apache.log4j.Logger;
import org.jwebsocket.eventmodel.context.EventModel;


/**
 *
 * @author Itachi
 */
public class EventFactory {

  private EventModel em;
  private EventsMap events;
  public static final String CONNECTOR_KEY = "CONNECTOR";
  private static Logger mLog = Logging.getLogger(EventFactory.class);

  public Token eventToToken(WebSocketEvent aEvent){
    return aEvent.getToken();
  }

  public Token eventToToken(WebSocketResponseEvent aEvent){
    return aEvent.getToken();
  }

  public WebSocketEvent tokenToEvent(Token aToken) throws Exception{
    String aType = aToken.getType();
    WebSocketEvent event = stringToEvent(aType);
    event.setSubject(aToken.getNS());
    event.setToken(aToken);

    return event;
  }

  public WebSocketEvent stringToEvent(String aEventId) throws Exception{
    if (mLog.isDebugEnabled())
      mLog.debug(">> Creating instance for event: '" + aEventId + "'...");
    
    WebSocketEvent e  = getEvents().getMap().get(aEventId).getClass().newInstance();
    e.setId(aEventId);
    e.initialize();
    
    return e;
  }

  public String eventToString(WebSocketEvent aEvent){
    return aEvent.getId();
  }

  public String eventToString(Class aEventClass){
    Object[] keys   = getEvents().getMap().keySet().toArray();
    Object[] values = getEvents().getMap().values().toArray();

    int index = 0;
    for (Object o : values){
      if (o.getClass().equals(aEventClass))
        return (String)keys[index];

      index++;
    }
    throw new IndexOutOfBoundsException();
  }

  public WebSocketResponseEvent createResponseEvent(WebSocketEvent aEvent){
    WebSocketResponseEvent aResponse = new WebSocketResponseEvent();
    aResponse.setId(aEvent.getId());
    aResponse.setToken(getEm().getParent().getServer().createResponse(aEvent.getToken()));

    return aResponse;
  }

  /**
   * @return the events
   */
  public EventsMap getEvents() {
    return events;
  }

  /**
   * @param events the events to set
   */
  public void setEvents(EventsMap events) {
    this.events = events;
  }

  public boolean hasEvent(String aEventId){
    return events.getMap().containsKey(aEventId);
  }

  /**
   * @return the em
   */
  public EventModel getEm() {
    return em;
  }

  /**
   * @param em the em to set
   */
  public void setEm(EventModel em) {
    this.em = em;
  }
}

