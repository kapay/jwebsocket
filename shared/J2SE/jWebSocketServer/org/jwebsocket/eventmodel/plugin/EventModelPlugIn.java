
package org.jwebsocket.eventmodel.plugin;

import org.jwebsocket.eventmodel.observable.ObservableObject;
import org.jwebsocket.eventmodel.api.IEventModelPlugIn;
import org.jwebsocket.eventmodel.context.EventModel;
import org.jwebsocket.eventmodel.observable.Event;
import org.jwebsocket.eventmodel.observable.ResponseEvent;
import java.util.Map;

/**
 *
 * @author Itachi
 */
public abstract class EventModelPlugIn extends ObservableObject implements IEventModelPlugIn{
  private String id;
  private EventModel em;
  private Map clientAPI;

  @Override
  public void initialize() throws Exception{}

  //Just for compatibility with the IObservable interface
  @Override
  public void processEvent(Event aEvent, ResponseEvent aResponseEvent){
    System.out.println(">> Responding from EventModelPlugIn, please override this method!");
  }

  @Override
  public void shutdown()throws Exception{}

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(String id) {
    this.id = id;
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

  /**
   * @return the clientAPI
   */
  public Map getClientAPI() {
    return clientAPI;
  }

  /**
   * @param clientAPI the clientAPI to set
   */
  public void setClientAPI(Map clientAPI) {
    this.clientAPI = clientAPI;
  }

}