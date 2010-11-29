
package org.jwebsocket.eventmodel.plugin.system;

import org.jwebsocket.eventmodel.plugin.EventModelPlugIn;
import org.jwebsocket.eventmodel.events.GetPlugInAPI;
import java.util.LinkedList;
import java.util.HashMap;
import javolution.util.FastMap;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.eventmodel.api.IEventModelPlugIn;
import org.jwebsocket.eventmodel.events.WebSocketEvent;
import org.jwebsocket.eventmodel.events.WebSocketResponseEvent;
import org.jwebsocket.logging.Logging;
import org.apache.log4j.Logger;
import org.jwebsocket.eventmodel.util.EmConstants;

/**
 *
 * @author Itachi
 */
public class SystemPlugIn extends EventModelPlugIn{

  private static Logger mLog = Logging.getLogger(SystemPlugIn.class);

  @Override
  public void initialize() throws Exception{
    //My incomming events
    LinkedList<Class> events = new LinkedList<Class>();
    events.add(GetPlugInAPI.class);

    //Registering my events in the global EM for incomming events notification
    getEm().addEvents(events);

    //My registration as listener
    getEm().on(events, this);

    //My API from the client
    HashMap mAPI = new HashMap();
    mAPI.put("getPlugInApi", GetPlugInAPI.class);
    setClientAPI(mAPI);
  }

  public void processEvent(GetPlugInAPI aEvent, WebSocketResponseEvent aResponseEvent){
    String aPlugInId         = aEvent.getToken().getString("plugin_id");
    if (mLog.isDebugEnabled())
      mLog.debug(">> Exporting API for '" + aPlugInId + "' event...");

    IEventModelPlugIn plugIn = getPlugIn(aPlugInId);
    FastMap api              = new FastMap();
    WebSocketEvent e;
    FastMap temp;

    try{
      for (Object key : plugIn.getClientAPI().keySet()){
        String aEventId = getEm().getEventFactory().eventToString((Class)plugIn.getClientAPI().get(key));
        e               = getEm().getEventFactory().stringToEvent(aEventId);
        temp            = new FastMap();
        temp.put("type"               , aEventId);
        temp.put("argsValidation"     , e.getArgsValidation());
        temp.put("responseValidation" , e.getResponseValidation());
        api.put(key, temp);
      }
      aResponseEvent = (WebSocketResponseEvent)aResponseEvent;
      aResponseEvent.getTo().add((WebSocketConnector)aEvent.getArgs().get(EmConstants.CONNECTOR_KEY));
      aResponseEvent.getToken().setMap("api", api);
      aResponseEvent.setCode(WebSocketResponseEvent.OK);
    }
    catch(Exception ex){
      mLog.error(ex.toString(), ex);
    }
  }

  private IEventModelPlugIn getPlugIn(String aPlugInId) throws IndexOutOfBoundsException {
    for (IEventModelPlugIn plugIn : getEm().getPlugIns()){
      if (plugIn.getId().equals(aPlugInId)){
        return plugIn;
      }
    }
    throw new IndexOutOfBoundsException("The plugIn with id: " + aPlugInId + ", does not exists!");
  }

}

