
package org.jwebsocket.eventmodel.plugin.auth;

import org.jwebsocket.eventmodel.plugin.EventModelPlugIn;
import org.jwebsocket.eventmodel.events.WebSocketResponseEvent;
import org.jwebsocket.eventmodel.events.Logon;
import org.jwebsocket.eventmodel.events.Logoff;
import java.util.LinkedList;
import java.util.HashMap;

/**
 *
 * @author Itachi
 */
public class AuthPlugIn extends EventModelPlugIn{

  @Override
  public void initialize() throws Exception{
    //My incomming events
    LinkedList<Class> events = new LinkedList<Class>();
    events.add(Logon.class);
    events.add(Logoff.class);

    //Registering my events in the global EM for incomming events notification
    getEm().addEvents(events);

    //My registration as listener
    getEm().on(events, this);

    //My API from the client
    HashMap m = new HashMap();
    m.put("logon", Logon.class);
    m.put("logoff", Logoff.class);
    setClientAPI(m);
  }

  public void processEvent(Logon aEvent, WebSocketResponseEvent aResponseEvent){

  }

  public void processEvent(Logoff aEvent, WebSocketResponseEvent aResponseEvent){

  }

}

