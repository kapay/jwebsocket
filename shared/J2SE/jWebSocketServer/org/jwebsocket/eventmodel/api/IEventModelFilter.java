
package org.jwebsocket.eventmodel.api;

import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.eventmodel.events.WebSocketEvent;
import org.jwebsocket.eventmodel.events.WebSocketResponseEvent;

/**
 *
 * @author Itachi
 */
public interface IEventModelFilter {

  public void firstCall(WebSocketConnector aConnector, WebSocketEvent aEvent) throws Exception;
  public void secondCall(WebSocketConnector aConnector, WebSocketResponseEvent aEvent) throws Exception;

}
