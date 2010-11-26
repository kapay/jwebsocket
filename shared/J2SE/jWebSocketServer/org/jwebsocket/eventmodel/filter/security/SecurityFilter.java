
package org.jwebsocket.eventmodel.filter.security;

import org.jwebsocket.eventmodel.filter.EventModelFilter;
import org.jwebsocket.eventmodel.events.WebSocketEvent;
import org.jwebsocket.eventmodel.events.WebSocketResponseEvent;
import org.jwebsocket.api.WebSocketConnector;


/**
 *
 * @author Itachi
 */
public class SecurityFilter extends EventModelFilter {

  @Override
  public void firstCall(WebSocketConnector aConnector, WebSocketEvent aEvent) throws Exception{

  }

  @Override
  public void secondCall(WebSocketConnector aConnector, WebSocketResponseEvent aEvent) throws Exception{

  }
}

