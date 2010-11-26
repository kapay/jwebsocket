
package org.jwebsocket.eventmodel.filter.validator;

import org.jwebsocket.eventmodel.filter.EventModelFilter;
import org.jwebsocket.eventmodel.events.WebSocketEvent;
import org.jwebsocket.eventmodel.events.WebSocketResponseEvent;
import org.jwebsocket.eventmodel.observable.Event;
import org.jwebsocket.api.WebSocketConnector;

import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;



/**
 *
 * @author Itachi
 */
public class ValidatorFilter extends EventModelFilter {

  private static Logger mLog = Logging.getLogger(ValidatorFilter.class);

  @Override
  public void firstCall(WebSocketConnector aConnector, WebSocketEvent aEvent) throws Exception{
    //Validating incoming event args
    for (Argument aArg : aEvent.getArgsValidation()){
      //validateArg(aArg, aEvent);
    }
  }

  @Override
  public void secondCall(WebSocketConnector aConnector, WebSocketResponseEvent aResponseEvent) throws Exception{
    if (mLog.isDebugEnabled())
      mLog.debug(">> Validating response for '" + aResponseEvent.getId() + "' event ...");

    WebSocketEvent e = getEm().getEventFactory().stringToEvent(aResponseEvent.getId());

    if (e.isResponseRequired()){
      //Response args validation
      for (Argument aArg : e.getResponseValidation()){
        //validateArg(aArg, aResponseEvent);
      }

      //At least 1 connector is needed for delivery
      if (aResponseEvent.getTo().isEmpty()){
        throw new NullPointerException("A 'WebSocketConnector' set with > 0 size is required for delivery the response!");
      }
    }
  }

  private void validateArg(Argument aArg, Event aEvent) throws Exception{
    //Argument validation
    if (!aEvent.getArgs().containsKey(aArg.getName())){
      if (!aArg.isOptional())
        throw new Exception("The argument: '" + aArg.getName() + "' is needed!");
    }else
      if (aEvent.getArgs().get(aArg.getName()).getClass() != (aArg.getType()))
        throw new Exception("The argument: '" + aArg.getName() + "', needs to be " + aArg.getType().toString());
  }

}

