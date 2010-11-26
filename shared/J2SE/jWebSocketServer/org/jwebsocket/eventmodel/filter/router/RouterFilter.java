
package org.jwebsocket.eventmodel.filter.router;

import org.jwebsocket.eventmodel.events.WebSocketEvent;
import org.jwebsocket.eventmodel.events.WebSocketResponseEvent;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.eventmodel.exception.ListenerNotFoundException;
import org.jwebsocket.eventmodel.filter.EventModelFilter;
import org.jwebsocket.token.Token;

import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;

/**
 *
 * @author Itachi
 */
public class RouterFilter extends EventModelFilter {
  private static Logger mLog = Logging.getLogger(RouterFilter.class);

  @Override
  public void firstCall(WebSocketConnector aConnector, WebSocketEvent aEvent) throws Exception{
    if (mLog.isInfoEnabled()){
      mLog.info(">> Checking if the event: '" + aEvent.getId() + "' has listener(s) in the server side...");
    }
    //If the incoming event has not listener, reject it!
    if (!getEm().hasListeners(aEvent.getClass()))
      throw new ListenerNotFoundException();
  }

  @Override
  public void secondCall(WebSocketConnector aConnector, WebSocketResponseEvent aEvent) throws Exception{
    WebSocketEvent e = getEm().getEventFactory().stringToEvent(aEvent.getId());
    if (!e.isResponseRequired())
      return;

    //Send the token to the client(s)
    Token aToken = aEvent.getToken();
    aToken.setInteger("code"        , aEvent.getCode());
    aToken.setDouble("elapsedTime"  , (double)aEvent.getElapsedTime());
    aToken.setString("msg"          , aEvent.getMessage());

    if (mLog.isInfoEnabled()){
      mLog.info(">> Sending the response for '" + aEvent.toString() + "' event to connectors...");
    }
    for (WebSocketConnector connector : aEvent.getTo()){
      getEm().getParent().getServer().sendToken(connector, aToken);
    }
  }
}

