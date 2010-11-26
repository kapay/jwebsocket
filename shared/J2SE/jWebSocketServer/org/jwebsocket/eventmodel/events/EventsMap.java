
package org.jwebsocket.eventmodel.events;

import org.jwebsocket.eventmodel.api.IInitializable;
import java.util.HashMap;

/**
 *
 * @author Itachi
 */
public class EventsMap implements IInitializable{

  private HashMap<String, WebSocketEvent> map;

  @Override
  public void initialize(){
    map = new HashMap<String, WebSocketEvent>();
    map.put("connector.started"    , new ConnectorStarted());
    map.put("connector.stopped"    , new ConnectorStopped());
    map.put("engine.started"       , new EngineStarted());
    map.put("engine.stopped"       , new EngineStopped());
    map.put("before.process.event" , new BeforeProcessEvent());
    map.put("after.process.event"  , new AfterProcessEvent());
    map.put("auth.logon"           , new Logon());
    map.put("auth.logoff"          , new Logoff());
    map.put("plugin.getapi"        , new GetPlugInAPI());
  }

  @Override
  public void shutdown(){

  }

  /**
   * @return the map
   */
  public HashMap<String, WebSocketEvent> getMap() {
    return map;
  }

  /**
   * @param map the map to set
   */
  public void setMap(HashMap<String, WebSocketEvent> map) {
    this.map = map;
  }
  
}
